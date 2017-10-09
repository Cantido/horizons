(ns horizons.web
  (:require [compojure.core :as routes]
            [compojure.route :as route]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [horizons.core :as horizons]
            [liberator.core :as liberator]
            [ring.middleware.defaults :as defaults]
            [ring.util.response :as response]
            [horizons.parsing.time :as t]))

(defn- handle-exception [web-app-component e]
  (log/error e)
  (-> "500.json"
      (response/resource-response {:root "public"})
      (response/status 500)
      (liberator.representation/ring-response)))

(defn- planetary-body-resource [web-app-component id]
  (liberator/resource
    :allowed-methods [:get]
    :available-media-types ["application/json"]
    :available-languages ["en-US"]
    :exists? (fn [_] (horizons/supported? (:horizons-client web-app-component) id))
    :handle-ok (fn [_] (horizons/get-planetary-body (:horizons-client web-app-component) id))
    :handle-exception (partial handle-exception web-app-component)))

(defn- ephemeris-options [horizons-client m]
  (-> m
    (get-in [:request :params])
    (select-keys #{:start :end :step-size})))

(defn- ephemeris-resource [web-app-component id]
  (liberator/resource
    :allowed-methods [:get]
    :available-media-types ["application/json"]
    :available-languages ["en-US"]
    :exists? (fn [_] (horizons/supported? (:horizons-client web-app-component) id))
    :handle-ok (fn [ctx] (horizons/get-ephemeris (:horizons-client web-app-component) id (ephemeris-options ephemeris-resource ctx)))
    :handle-exception (partial handle-exception web-app-component)))

(defn- app-routes [web-app-component]
  (routes/routes
    (routes/GET "/" [] (response/redirect "https://cantido.github.io/horizons/"))
    (routes/context ["/bodies/:id", :id #"[0-9]+"] [id]
      (routes/ANY "/" [] (planetary-body-resource web-app-component id))
      (routes/ANY "/ephemeris" [] (ephemeris-resource web-app-component id)))
    (route/not-found (response/not-found "Resource not found."))))

(defn app-handler [web-app-component]
  (-> (app-routes web-app-component)
      (defaults/wrap-defaults
        (assoc defaults/api-defaults :static {:resources "public"}))))

(defrecord WebApp [horizons-client])

(defn web-app []
  (component/using (map->WebApp {}) [:horizons-client]))
