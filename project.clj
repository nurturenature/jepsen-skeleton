(defproject skeleton "0.1.0-SNAPSHOT"
  :description "A skelton Jepsen codebase for bootstrapping new projects."
  :url "https://github.com/nurturenature/jepsen-skeleton"
  :license {:name "Apache License, Version 2.0, January 2004"
            :url "http://www.apache.org/licenses/"}
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [jepsen "0.3.8"]]
  :main skeleton.cli
  :plugins [[lein-localrepo "0.5.4"]
            [lein-codox "0.10.8"]]
  :repl-options {:init-ns skeleton.cli}
  :codox {:output-path "target/doc/"
          :source-uri "../../{filepath}#L{line}"
          :metadata {:doc/format :markdown}})
