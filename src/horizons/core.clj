(ns horizons.core
  (:require [horizons.telnet-client :refer :all]
            [horizons.parser :refer :all]
            [liberator.core :refer [resource defresource]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-params wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :refer [resource-response]]
            [compojure.core :refer :all]
            [compojure.route :as route]))

(def supported-bodies
  #{499})

(defn supported? [id]
  (contains? supported-bodies (int (bigdec id))))

(defn get-planetary-body [id]
  (let [body-data (parse (get-body id))]
    (if (instaparse.core/failure? body-data)
      body-data
      (:S (restructure body-data)))))