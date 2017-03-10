(ns horizons.parser-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer :all]
            [horizons.parser :refer :all]))

(defn get-file [name]
  (slurp
    (io/file
      (io/resource name))))

(defn get-edn [name]
  (read-string (get-file name)))

(defn parse-file [name]
  (parse (get-file name)))

(defn assert-parse-result [txt-name edn-name]
  (is (= (parse-file txt-name)
         (get-edn edn-name))))

(deftest all-sections-grammar-test
  (assert-parse-result "mars-results.txt" "mars-results-parsed.edn"))

(deftest prelim-section-grammar-test
  (assert-parse-result "mars-initial-results.txt" "mars-initial-results-parsed.edn"))

(deftest earth-grammar-test
  (assert-parse-result "earth-initial-results.txt" "earth-initial-results-parsed.edn"))

(deftest tree->map-test
  (is (= (tree->map []) {}))
  (is (= (tree->map [:S []]) {:S {}}))
  (is (= (tree->map [:S [:key "value"]]) {:S {:key "value"}}))
  (is (= (tree->map [:S [:key [:subkey "value"]] {:S {:key {:subkey "value"}}}]))))
