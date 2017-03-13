(ns horizons.parser-test
  (:require
    [clj-time.core :as t]
    [clojure.java.io :as io]
    [clojure.test :refer :all]
    [horizons.parser :refer :all]
    [horizons.parser-test-mercury :refer :all]
    [horizons.parser-test-mars :refer :all]))

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



(deftest full-grammar-test
  (assert-parse-result "mars-full.txt" "mars-full-parsed.edn"))

(deftest geophysical-grammar-test
  (assert-parse-result "mars-geophysical.txt" "mars-geophysical.edn")
  (assert-parse-result "earth-geophysical.txt" "earth-geophysical-parsed.edn")
  (assert-parse-result "mercury-geophysical.txt" "mercury-geophysical-parsed.edn")
  (assert-parse-result "jupiter-geophysical.txt" "jupiter-geophysical-parsed.edn"))

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
  [:ephemeris
   [:ephemeris-line-item
    [:measurement-time
     [:timestamp
      [:date
       [:year "2017"]
       [:month "Feb"]
       [:day "24"]]
      [:time
       [:hour-of-day "00"]
       [:minute-of-hour "00"]
       [:second-of-minute "00"]
       [:millisecond-of-second "0000"]]]]
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
                                    :timestamp (t/date-time 2017 2 24 0 0 0 0)}
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
  (is (= (restructure ephemeris-input) ephemeris-output))
  (is (= (restructure (get-edn "mars-full-parsed.edn")) mars-map))
  (is (= (restructure (get-edn "mercury-geophysical-parsed.edn")) mercury-map)))

(def timestamp-tree
  [:timestamp
   [:era "A.D."]
   [:date
    [:year "2017"]
    [:month "Feb"]
    [:day "24"]]
   [:time
    [:hour-of-day "00"]
    [:minute-of-hour "00"]
    [:second-of-minute "00"]
    [:millisecond-of-second "0000"]]
   [:time-zone "UT"]])

(def timestamp-map
  {:timestamp (t/date-time 2017 2 24 0 0 0 0)})

(deftest timestamp-transformation-test
  (is (= (restructure timestamp-tree) timestamp-map)))
