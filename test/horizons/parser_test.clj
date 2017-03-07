(ns horizons.parser-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [horizons.parser :refer :all]))

(def mars-results
  (slurp
    (io/file
      (io/resource "mars-results.txt"))))

(def mars-results-parsed
  (read-string
    (slurp
      (io/file
        (io/resource "mars-results-parsed.edn")))))

(deftest grammar-test
  (is (= (parse mars-results)
         mars-results-parsed)))
