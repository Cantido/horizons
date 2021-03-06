(defproject horizons "0.2.0-SNAPSHOT"
  :description "A RESTful-style API for NASA's HORIZONS system"
  :url "https://github.com/Cantido/horizons"
  :license {:name "GNU General Public License"
            :url "http://www.gnu.org/licenses/gpl.txt"}
  :uberjar-name "horizons-standalone.jar"
  :profiles {:dev {:resource-paths ["test-resources"]}
             :production {:env {:production true}}
             :uberjar {:aot :all}}
  :min-lein-version "2.0.0"
  :plugins [[lein-ring "0.8.11"]]
  :ring {:handler horizons.main/lein-ring-handler}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.3.443"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.logging "0.4.0"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [clj-time "0.13.0"]
                 [com.stuartsierra/component "0.3.2"]
                 [commons-net/commons-net "3.6"]
                 [compojure "1.6.0"]
                 [aero "1.1.2"]
                 [instaparse "1.4.9"]
                 [liberator "0.15.2"]
                 [ring "1.6.1"]
                 [ring/ring-defaults "0.3.0"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-mock "0.3.0"]])
