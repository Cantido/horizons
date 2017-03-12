(ns horizons.core
  (:require [horizons.telnet-client :refer :all]
            [horizons.parser :refer :all]))

(def supported-bodies
  #{199 499 399})

(defn supported? [id]
  (contains? supported-bodies (int (bigdec id))))

(defn throw-parse-exception
  [body-string failure]
  (throw (Exception. (str "Unable to parse HORIZONS response. While parsing:\n\n" body-string
                          "\n\nGot the following failure: \n" (with-out-str (print failure))))))

(defn get-planetary-body [id]
  (let [body-string (get-body id)
        body-parse-tree (parse body-string)]
    (if (instaparse.core/failure? body-parse-tree)
        (throw-parse-exception body-string body-parse-tree)
        (:S (restructure body-parse-tree)))))
