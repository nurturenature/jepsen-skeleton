(ns skeleton.cli
  "Command-line entry point for Jepsen tests."
  (:require [jepsen
             [checker :as checker]
             [cli :as cli]
             [db :as db]
             [generator :as gen]
             [nemesis :as nemesis]
             [tests :as tests]]
            [jepsen.checker.timeline :as timeline]
            [jepsen.os.debian :as debian]
            [skeleton.workload :as workload]))

(def workloads
  "A map of workload names to functions
   that take CLI options and return workload maps."
  {:ops-by-node    workload/ops-by-node
   :odd-nodes-only workload/odd-nodes-only
   :on-threads-any workload/on-threads-any
   :none           (fn [_] tests/noop-test)})

(defn test-name
  "Given opts, return a friendly test name."
  [{:keys [workload rate time-limit op-latency] :as _opts}]
  (str (name workload)
       "-" rate "tps"
       "-" time-limit "s"
       (when (< 0 op-latency) (str "-op-latency-" op-latency "ms"))))

(defn skeleton-test
  "Given options from the CLI, constructs a test map."
  [opts]
  (let [workload-name (:workload opts)
        workload      ((workloads workload-name) opts)
        db            db/noop
        nemesis       nemesis/noop]
    (merge tests/noop-test
           opts
           {:name      (test-name opts)
            :os        debian/os
            :db        db
            :checker   (checker/compose
                        {:perf       (checker/perf
                                      {:nemeses (:perf nemesis)})
                         :timeline   (timeline/html)
                         :stats      (checker/stats)
                         :exceptions (checker/unhandled-exceptions)
                         :workload   (:checker workload)})
            :client    (:client workload)
            :nemesis   nemesis
            :generator (gen/phases
                        (gen/log "Workload with nemesis")
                        (->> (:generator workload)
                             (gen/stagger    (/ (:rate opts)))
                             (gen/nemesis    (:generator nemesis))
                             (gen/time-limit (:time-limit opts)))

                        (gen/log "Final nemesis")
                        (gen/nemesis (:final-generator nemesis))

                        (gen/log "Final workload")
                        (->> (:final-generator workload)
                             (gen/stagger (/ (:rate opts)))))})))

(def cli-opts
  "Command line options"
  [[nil "--op-latency NUM" "The amount of time, simulated latency, an op should take in ms."
    :default  0
    :parse-fn parse-long
    :validate [nat-int? "Must be a non-negative integer"]]

   ["-r" "--rate HZ" "Approximate request rate, in hz"
    :default 100
    :parse-fn read-string
    :validate [pos-int? "Must be a positive integer."]]

   ["-w" "--workload NAME" "What workload should we run?"
    :default  :ops-by-node
    :parse-fn keyword
    :validate [workloads (cli/one-of workloads)]]])

(defn -main
  "CLI.
   `lein run` to list commands."
  [& args]
  (cli/run! (merge (cli/single-test-cmd {:test-fn  skeleton-test
                                         :opt-spec cli-opts})
                   (cli/serve-cmd))
            args))
