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
  #{499 399})

(defn supported? [id]
  (contains? supported-bodies (int (bigdec id))))

(defn build-error-response [body-string failure]
  (do
    (throw (Exception. (str "Unable to parse HORIZONS response. While parsing:\n\n" body-string
                            "\n\nGot the following failure: \n" (with-out-str (print failure)))))))

(defn get-planetary-body [id]
  (let [body-string (get-body id)
        body-parse-tree (parse body-string)]
    (if (instaparse.core/failure? body-parse-tree)
        (build-error-response body-string body-parse-tree)
        (:S (restructure body-parse-tree)))))
