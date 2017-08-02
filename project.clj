(defproject license-form "0.1.0-SNAPSHOT"
  :jvm-opts ["-Dfile.encoding=UTF-8"]
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [hiccup "1.0.5"]
                 [org.clojure/data.json "0.2.6"]
                 [ring/ring-core "1.5.0"]
                 [ring/ring-jetty-adapter "1.5.0"]
                 [ring/ring-json "0.4.0"]]
  :main ^:skip-aot license-form.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
