(ns horizons.core
  (:require [horizons.telnet-client :refer :all]
            [liberator.core :refer [resource defresource]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer :all]))

(defroutes app
           (ANY "/" []
             (do
               (get-body 499))))

(def handler
  (-> app
      wrap-json-response
      wrap-params))
