(defproject horizons "0.2.0-SNAPSHOT"
  :description "A RESTful-style API for NASA's HORIZONS system"
  :url "https://github.com/Cantido/horizons"
  :license {:name "GNU General Public License"
            :url "http://www.gnu.org/licenses/gpl.txt"}
  :plugins [[lein-ring "0.8.11"]]
  :ring {:handler horizons.core/handler}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.3.441"]
                 [org.clojure/data.json "0.2.6"]
                 [commons-net/commons-net "3.6"]
                 [compojure "1.3.4"]
                 [instaparse "1.4.5"]
                 [liberator "0.13"]
                 [ring/ring-core "1.2.1"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-mock "0.3.0"]])
