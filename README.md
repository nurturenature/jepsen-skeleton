# jepsen-skeleton

A skelton Jepsen repository for bootstrapping new projects.

----

## no-op

Test is a minimal no-op.

```clj
{:db       db/noop
:nemesis   nemesis/noop
:generator (repeat {:type  :invoke
                    :f     :node
                    :value nil})}
```

----

### Client

Augments op map with `{:type :ok :value node}`.

May `sleep` `--op-latency` ms.

```clj
(invoke!
  [{:keys [node op-latency] :as _this} _test op]
  ; simulate any desired latency?
  (when (< 0 op-latency)
    (u/sleep op-latency))  
  
  ; op is ok with a value of node
  (assoc op
         :type  :ok
         :value node))
```

----

### Checker

Calculates total ops by node.

```clj
(->> history
     (reduce (fn [summary {:keys [value] :as _op}]
               (update summary value (fn [old] (+ 1 (or old 0)))))
             (sorted-map)))
```

----

## --workload

### --workload ops-by-node

Counts ops by node.

```clj
{:valid? true,
 :nodes ["n1" "n2" "n3" "n4" "n5"],
 :ops-by-node {"n1" 1919,
               "n2" 1009,
               "n3" 978,
               "n4" 977,
               "n5" 977}}
```

----

Try manually specifying nodes in reverse order.

`--nodes n5,n4,n3,n2,n1`

```clj
{:valid? true,
 :nodes ["n5" "n4" "n3" "n2" "n1"],
 :ops-by-node {"n1" 960,
               "n2" 960,
               "n3" 963,
               "n4" 983,
               "n5" 1895}}
```

----

### --workload odd-nodes-only

Only odd numbered nodes as `(gen/on-threads #{0 2 4})` is a set of the corresponding threads.

```clj
{:valid? true,
 :nodes ["n1" "n2" "n3" "n4" "n5"],
 :ops-by-node {"n1" 1936,
               "n3" 1952,
               "n5" 1949}}
```

----

### --workload on-threads-any

All nodes with `(gen/on-threads any?)` as `any?` always returns `true`.  

```clj
{:valid? true,
 :nodes ["n1" "n2" "n3" "n4" "n5"],
 :ops-by-node {"n1" 1893,
               "n2" 993,
               "n3" 964,
               "n4" 962,
               "n5" 962}}
```

----

## --op-latency ms

The amount of time, simulated latency, an op should take in ms.

`--op-latency 15`

```clj
{:valid? true,
 :nodes ["n1" "n2" "n3" "n4" "n5"],
 :ops-by-node {"n1" 1299,
               "n2" 1240,
               "n3" 1141,
               "n4" 1102,
               "n5" 1040}}
```

![--op-latency 15](doc/latency-raw-op-latency-15ms.png)

----

Try a latency designed to remove a node from availability equal to all other nodes having a chance,
e.g. 1000 / rate * #-nodes.

`--op-latency 50`

```clj
{:valid? true,
 :nodes ["n1" "n2" "n3" "n4" "n5"],
 :ops-by-node {"n1" 1029,
               "n2" 1021,
               "n3" 1019,
               "n4" 1008,
               "n5" 991}}
```

![--op-latency 50](doc/latency-raw-op-latency-50ms.png)
