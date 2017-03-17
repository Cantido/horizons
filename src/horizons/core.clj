(ns horizons.core
  "Provides access to NASA's HORIZONS solar system body database."
  (:require [horizons.parser :refer :all]
            [horizons.telnet.client :refer :all]))

(def supported-bodies
  #{199 399 499})

(defn supported?
  "Check if the given body ID is definitely supported by this system."
  [id]
  (contains? supported-bodies (int (bigdec id))))

(defn- throw-parse-exception
  "Throw an exception documenting a parse exception"
  [body-string failure]
  (throw (Exception. (str "Unable to parse HORIZONS response. While parsing:\n\n" body-string
                          "\n\nGot the following failure: \n" (with-out-str (print failure))))))

(defn get-planetary-body
  "Get geophysical data about a solar system body with the given ID."
  [id]
  (let [body-string (get-body id)
        body-parse-tree (parse body-string)]
    (if (instaparse.core/failure? body-parse-tree)
        (throw-parse-exception body-string body-parse-tree)
        (::S (restructure body-parse-tree)))))
