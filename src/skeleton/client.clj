(ns skeleton.client
  (:require [jepsen
             [client :as client]
             [util :as u]]))

; Augments op map with {:type :ok :value node}.
(defrecord ops-by-node [conn]
  client/Client
  (open!
    [this {:keys [op-latency] :as _test} node]
    (assoc this
           :node       node
           :op-latency op-latency))

  (setup!
    [_this _test])

  (invoke!
    [{:keys [node op-latency] :as _this} _test op]
    ; simulate any desired latency?
    (when (< 0 op-latency)
      (u/sleep op-latency))

    ; op is ok with a value of node
    (assoc op
           :type  :ok
           :value node))

  (teardown!
    [_this _test])

  (close!
    [this _test]
    (dissoc this
            :node
            :op-latency)))
