(ns skeleton.stats
  "A checker that generates stats for total ops by node."
  (:require
   [jepsen
    [checker :as checker]
    [history :as h]]))

(defn ops-by-node
  "Calculate total ops by node."
  []
  (reify checker/Checker
    (check [_this {:keys [nodes] :as _test} history _opts]
      (let [; client completions only
            history' (->> history
                          h/client-ops
                          (h/remove (fn [{:keys [type] :as _op}] (= type :invoke))))

            ; count ops by node
            summary  (->> history'
                          (reduce (fn [summary {:keys [value] :as _op}]
                                    (update summary value (fn [old] (+ 1 (or old 0)))))
                                  (sorted-map)))]

        ; result map
        {:valid?      true
         :nodes       nodes
         :ops-by-node summary}))))
