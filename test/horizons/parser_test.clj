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
  (is (= (tree->map
           [:revision-date
            [:month "Jul"]
            [:day "31"]
            [:year "2013"]])
         {:revision-date
          {:month "Jul"
           :day "31"
           :year "2013"}}))
  (is (= (tree->map
           [:file-header
            [:revision-date
             [:month "Jul"]
             [:day "31"]
             [:year "2013"]]])
         {:file-header
          {:revision-date
           {:month "Jul"
            :day "31"
            :year "2013"}}}))
  (is (= (tree->map
           [:file-header
            [:revision-date
             [:month "Jul"]
             [:day "31"]
             [:year "2013"]]
            [:body-name "Mars"]
            [:body-id "499"]])
         {:file-header
          {:revision-date
           {:month "Jul"
            :day "31"
            :year "2013"}
           :body-name "Mars"
           :body-id "499"}}))
  (is (= (tree->map
           [:geophysical-data
            [:mean-radius "3389.9(2+-4)"]
            [:density "3.933(5+-4)"]
            [:mass "6.4185"]])
         {:geophysical-data
          {:mean-radius "3389.9(2+-4)"
           :density "3.933(5+-4)"
           :mass "6.4185"}})))
  ;(is (= (tree->map
  ;         [:ephemeris
  ;          [:line-item "first"]
  ;          [:line-item "second"]
  ;          [:line-item "third"]])
  ;       {:ephemeris #{
  ;                     {:line-item "first"}
  ;                     {:line-item "second"}
  ;                     {:line-item "third"}}})))

(deftest set-of-maps-test
  (is (= (set-of-maps
            [:data [:k "x"]]
            [:data [:k "y"]]
            [:data [:k "z"]])
         #{
           {:data [:k "x"]}
           {:data [:k "y"]}
           {:data [:k "z"]}})))

;(deftest parse-to-map-test
;  (is (= (tree->map (get-edn "mars-results-parsed.edn"))
;         (get-edn "mars-results-map.edn"))))