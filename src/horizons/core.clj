(ns horizons.core
  (:require [liberator.core :refer [resource defresource]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer :all]))

(defroutes app (ANY "/" [] (str "success")))

(def handler
  (-> app
      wrap-json-response
      wrap-params))
