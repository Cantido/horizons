(ns horizons.web
  (:require [horizons.core :as h]
            [horizons.time :as t]
            [liberator.core :refer [resource defresource]]
            [ring.middleware.json :refer [wrap-json-body wrap-json-params wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :refer [resource-response]]
            [compojure.core :refer :all]
            [compojure.route :as route]))

(defn iso-format-dates
  [tree]
  (clojure.walk/postwalk
    (fn [form]
      (cond
        (not (map? form)) form
        (contains? form ::h/date) (update-in form [::h/date] t/format-date)
        (contains? form ::h/timestamp) (update-in form [::h/timestamp] t/format-date-time)
        (contains? form ::h/duration) (update-in form [::h/duration] t/iso-format-duration)
        :else form))
    tree))

(defresource planetary-body-resource [id]
  :allowed-methods [:get]
  :available-media-types ["application/json"]
  :available-languages ["en-US"]
  :exists? (fn [_] (h/supported? id))
  :handle-ok (fn [ctx]
                 (iso-format-dates (h/get-planetary-body id)))
  :handle-exception (fn [e]
                        (println e)
                        (liberator.representation/ring-response (resource-response "500.json" {:root "public"}))))

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
