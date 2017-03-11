(ns horizons.core
  (:require [horizons.telnet-client :refer :all]
            [horizons.parser :refer :all]
            [liberator.core :refer [resource defresource]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-params wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer :all]))

(defroutes handler
           (GET "/bodies/:id" [id] {:body (:S (restructure (parse (get-body id))))}))

(def app
  (-> handler
      wrap-json-body
      wrap-json-params
      wrap-json-response
      wrap-params))
