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
  :hooks [environ.leiningen.hooks]
  :plugins [[lein-ring "0.8.11"]
            [environ/environ.lein "0.3.1"]]
  :ring {:handler horizons.web/app}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.3.441"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.logging "0.3.1"]
                 [clj-time "0.13.0"]
                 [commons-net/commons-net "3.6"]
                 [compojure "1.3.4"]
                 [environ "1.0.0"]
                 [instaparse "1.4.5"]
                 [liberator "0.13"]
                 [org.immutant/web "2.1.6"]
                 [ring/ring-core "1.2.1"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-mock "0.3.0"]])
