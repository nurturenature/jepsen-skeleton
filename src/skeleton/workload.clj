(ns skeleton.workload
  "Jepsen Workloads"
  (:require
   [jepsen.generator :as gen]
   [skeleton
    [client :as client]
    [stats :as stats]]))

(def node-op
  "An op that sets {:value node}"
  {:type  :invoke
   :f     :node
   :value nil})

(defn ops-by-node
  "A workload to summarize ops by node."
  [_opts]
  {:client          (client/->ops-by-node nil)
   :generator       (repeat node-op)
   :final-generator nil
   :checker         (stats/ops-by-node)})

(defn odd-nodes-only
  "An ops-by-node workload for odd numbered nodes only."
  [{:keys [nodes] :as opts}]
  (let [odd-node-threads (->> nodes
                              ; n# -> #
                              (map (fn [node] (-> node
                                                  (subs 1)
                                                  parse-long)))
                              (filter odd?)
                              ; threads are 0 based, e.g. n1 is thread 0
                              (map #(- % 1))
                              (into #{}))
        ops-by-node (ops-by-node opts)
        generator   (->> (:generator ops-by-node)
                         (gen/on-threads odd-node-threads))]

    (assoc ops-by-node
           :generator generator)))
