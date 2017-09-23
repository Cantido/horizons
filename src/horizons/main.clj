(ns horizons.main
  "Defines, starts, and stops the Horizons system."
  (:require [horizons.web :as web]
            [com.stuartsierra.component :as component]))

(defn horizons-system [config-options]
  (let [{:keys [port]} config-options]
    (component/system-map
      :web-server (web/web-server port))))

(defn -main [& [port]]
  (component/start-system (horizons-system {:port port})))
