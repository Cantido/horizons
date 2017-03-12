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
  (assert-parse-result "mars-full.txt" "mars-full-parsed.edn"))

(deftest prelim-section-grammar-test
  (assert-parse-result "mars-geophysical.txt" "mars-geophysical.edn"))

(deftest earth-grammar-test
  (assert-parse-result "earth-geophysical.txt" "earth-geophysical-parsed.edn"))

(deftest mercury-grammar-test
  (assert-parse-result "mercury-geophysical.txt" "mercury-geophysical-parsed.edn"))

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

(def ephemeris-input
  [ :ephemeris
   [ :ephemeris-line-item
    [ :measurement-time
     [:year "2017"],
     [:month "Feb"],
     [:day "24"]
     [:time "00:00"]]
    [:ascension-declination "01 12 18.78 +07 36 55.3"],
    [:apparent-magnitude "1.27"],
    [:surface-brightness "4.29"],
    [:range "2.00321056835551"],
    [:range-rate "11.4427302"],
    [:sun-observer-target-angle "44.2386"],
    [:sun-observer-target-angle-direction "/T"],
    [:sun-target-observer-angle "28.0789"]]])

(def ephemeris-output
  {:ephemeris #{
                {
                 :measurement-time {
                                    :year "2017"
                                    :month "Feb"
                                    :day "24"
                                    :time "00:00"}
                 :ascension-declination "01 12 18.78 +07 36 55.3"
                 :apparent-magnitude "1.27"
                 :surface-brightness "4.29"
                 :range "2.00321056835551"
                 :range-rate "11.4427302"
                 :sun-observer-target-angle "44.2386"
                 :sun-observer-target-angle-direction "/T"
                 :sun-target-observer-angle "28.0789"}}})

(deftest ephemeris-to-set-test
  (is (= (transform ephemeris-input)
         ephemeris-output)))


(deftest transform-test
  (is (= (restructure ephemeris-input)
         ephemeris-output))
  (is (= (restructure (get-edn "mars-full-parsed.edn"))
         (get-edn "mars-full-map.edn")))
  (is (= (restructure (get-edn "mercury-geophysical-parsed.edn"))
         (get-edn "mercury-geophysical-map.edn"))))