(ns horizons.parsing.parser-test
  (:require
    [clj-time.core :as t]
    [clojure.java.io :as io]
    [clojure.test :refer :all]
    [instaparse.core :as insta]
    [horizons.core :as h]
    [horizons.parsing.parser :refer :all]
    [horizons.parsing.parser-test-mercury :refer :all]))


(defn get-file [name]
  (slurp
    (io/file
      (io/resource name))))

(defn get-edn [name]
  (read-string (get-file name)))

(defn parse-file [name]
  (parse (get-file name)))

(defn assert-parse-result [txt-name edn-name]
  (testing txt-name
    (is (= (parse-file txt-name)
           (get-edn edn-name)))))

(defn success? [x]
  (not (insta/failure? x)))

(deftest geophysical-grammar-test
  (assert-parse-result "mercury-geophysical.txt" "mercury-geophysical-parsed.edn")
  (assert-parse-result "venus-geophysical.txt" "venus-geophysical-parsed.edn")
  (assert-parse-result "earth-geophysical.txt" "earth-geophysical-parsed.edn")
  (assert-parse-result "mars-geophysical.txt" "mars-geophysical.edn")
  (assert-parse-result "jupiter-geophysical.txt" "jupiter-geophysical-parsed.edn")
  (assert-parse-result "saturn-geophysical.txt" "saturn-geophysical-parsed.edn")
  (testing "uranus-geophysical.txt"
    (is (success? (parse-file "uranus-geophysical.txt"))))
  (testing "neptune-geophysical.txt"
    (is (success? (parse-file "neptune-geophysical.txt")))))

(deftest ephemeredes-grammar-test
  (assert-parse-result "mars-ephemeredes.txt" "mars-ephemeredes-parsed.edn")
  (testing "mercury-ephemeredes.txt"
    (is (success? (parse-file "mercury-ephemeredes.txt")))))


(deftest tree->map-test
  (is (= (tree->map
           [::h/revision-date
            [::h/month "Jul"]
            [::h/day 31]
            [::h/year 2013]])
         {::h/revision-date
          {::h/month "Jul"
           ::h/day 31
           ::h/year 2013}}))
  (is (= (tree->map
           [::h/file-header
            [::h/revision-date
             [::h/month "Jul"]
             [::h/day 31]
             [::h/year 2013]]])
         {::h/file-header
          {::h/revision-date
           {::h/month "Jul"
            ::h/day 31
            ::h/year 2013}}}))
  (is (= (tree->map
           [::h/file-header
            [::h/revision-date
             [::h/month "Jul"]
             [::h/day 31]
             [::h/year 2013]]
            [::h/body-name "Mars"]
            [::h/body-id 499]])
         {::h/file-header
          {::h/revision-date
           {::h/month "Jul"
            ::h/day 31
            ::h/year 2013}
           ::h/body-name "Mars"
           ::h/body-id 499}}))
  (is (= (tree->map
           [::h/geophysical-data
            [::h/mean-radius "3389.9(2+-4)"]
            [::h/density "3.933(5+-4)"]
            [::h/mass "6.4185"]])
         {::h/geophysical-data
          {::h/mean-radius "3389.9(2+-4)"
           ::h/density "3.933(5+-4)"
           ::h/mass "6.4185"}})))

(deftest transform-test
  (is (= (restructure (get-edn "mercury-geophysical-parsed.edn")) mercury-map)))

(deftest sci-not-transform-test
  (is (= (restructure
           [:sci-not
            [:significand [:float "5.05"]]
            [:mantissa [:integer "22"]]])
         5.05E22M))
  (is (= (restructure
           [:sci-not
            [:significand [:integer "5"]]
            [:mantissa [:integer "22"]]])
         5E22M)))

(def timestamp-tree
  [:timestamp
   [:era "A.D."]
   [:date
    [:year [:integer "2017"]]
    [:month "Feb"]
    [:day [:integer "24"]]]
   [:time
    [:hour-of-day [:integer "00"]]
    [:minute-of-hour [:integer "00"]]
    [:second-of-minute [:integer "00"]]
    [:millisecond-of-second [:integer "0000"]]]
   [:time-zone "UT"]])

(def timestamp-map
  {::h/timestamp (t/date-time 2017 2 24 0 0 0 0)})

(deftest timestamp-transformation-test
  (is (= (restructure timestamp-tree) timestamp-map)))

(deftest comma-separated-integer-transform-test
  (is (= (transform [:integer [:comma-separated-integer "123,456,789"]])
         123456789)))
