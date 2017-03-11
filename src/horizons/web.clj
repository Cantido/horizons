(ns horizons.web
  (:require [horizons.core :refer :all]
            [liberator.core :refer [resource defresource]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-params wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :refer [resource-response]]
            [compojure.core :refer :all]
            [compojure.route :as route]))

(defresource planetary-body-resource [id]
             :allowed-methods [:get]
             :available-media-types ["application/json"]
             :exists? (fn [_] (supported? id))
             :handle-ok (fn [ctx]
                          {:body (get-planetary-body id)}))

(defroutes handler
           (GET "/" [] (resource-response "index.html" {:root "public"}))
           (ANY ["/bodies/:id", :id #"[0-9]+"] [id] (planetary-body-resource id))
           (route/resources "/"))

(def app
  (-> handler
      wrap-json-body
      wrap-json-params
      wrap-json-response
      wrap-params))
