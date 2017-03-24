(ns horizons.web
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.tools.logging :as log]
            [environ.core :as environ]
            [horizons.core :as h]
            [horizons.parsing.time :as t]
            [immutant.web :as web]
            [liberator.core :refer [defresource]]
            [ring.middleware.json :refer [wrap-json-body
                                          wrap-json-params
                                          wrap-json-response]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :as response]))

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
                 (-> id h/get-planetary-body iso-format-dates))
  :handle-exception (fn [e]
                        (log/error e)
                        (-> "500.json"
                          (response/resource-response {:root "public"})
                          (response/status 500)
                          (liberator.representation/ring-response))))

(defroutes handler
  (GET "/" [] (response/resource-response "index.html" {:root "public"}))
  (ANY ["/bodies/:id", :id #"[0-9]+"] [id] (planetary-body-resource id))
  (route/resources "/"))

(def app
  (-> handler
      wrap-json-body
      wrap-json-params
      wrap-json-response
      wrap-params))

(defn -main [& [port]]
  (let [port (or port (environ/env :port) 3000)]
    (log/info "Starting HORIZONS on port" port)
    (web/run app
             :host "0.0.0.0"
             :port port
             :path "/")))
