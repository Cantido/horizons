(ns horizons.parsing.parser-test
  (:require
    [clj-time.core :as t]
    [clojure.java.io :as io]
    [clojure.test :refer :all]
    [instaparse.core :as insta]
    [horizons.core :as h]
    [horizons.parsing.parser :refer :all]
    [horizons.parsing.parser-test-mercury :refer :all]
    [horizons.parsing.parser-test-jupiter :refer :all]))


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
    (is (success? (parse-file "mercury-ephemeredes.txt"))))
  (testing "jupiter-ephemeredes.txt"
    (is (success? (parse-file "jupiter-ephemeredes.txt")))))


(deftest tree->map-test
  (testing "date trees to maps"
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
             ::h/body-id 499}})))
  (testing "geophysical data tree to map"
    (is (= (tree->map
             [::h/geophysical-data
              [::h/mean-radius [:value "3389.9(2+-4)"]]
              [::h/density "3.933(5+-4)"]
              [::h/mass "6.4185"]])
           {::h/geophysical-data
            {::h/mean-radius {::h/value "3389.9(2+-4)"}
             ::h/density "3.933(5+-4)"
             ::h/mass "6.4185"}}))))

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

(deftest restructure-test
  (testing "restructuring mercury-geophysical-parsed.edn"
    (is (= (restructure (get-edn "mercury-geophysical-parsed.edn")) mercury-map)))
  (testing "restructuring jupiter-geophysical-parsed.edn"
    (is (= (restructure (get-edn "jupiter-geophysical-parsed.edn")) jupiter-map)))
  (testing "scientific notation"
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
  (testing "timestamps"
    (is (= (restructure timestamp-tree) timestamp-map))))

(deftest transform-test
  (testing "comma-separated integers"
    (is (= (transform [:integer [:comma-separated-integer "123,456,789"]])
           123456789)))
  (testing "ephemeredes tree to set"
    (is (= (transform  [:ephemeredes
                        [:ephemeris [:x-position 1]]
                        [:ephemeris [:x-position 2]]
                        [:ephemeris [:x-position 3]]])
           [:ephemeredes
            #{
              {:x-position 1}
              {:x-position 2}
              {:x-position 3}}])))
  (testing "value with units"
    (is (= (transform [:mean-radius [:unit-KMT] [:value "2440(+-1)"]])
           [:mean-radius [:unit-code "KMT"] [:value "2440(+-1)"]]))
    (is (= (transform
             [:atmospheric-mass
              [:value [:sci-not [:significand [:float "5.1"]] [:exponent [:integer "18"]]]]
              [:unit-KGM]])
           [:atmospheric-mass
            [:value 5.1E+18M]
            [:unit-code "KGM"]])))
  (testing "value with exponents"
    (is (= (transform
             [:heat-flow-mass
              [:exponent [:integer "7"]]
              [:value [:integer "15"]]])
           [:heat-flow-mass {:value 15E7M}])))
  (is (= (transform [:rotation-rate [:exponent [:integer "-4"]] [:unit-2A] [:value [:float "1.75865"]]])
         [:rotation-rate {:value 0.000175865M :unit-code "2A"}]))
  (testing "unit codes"
    (is (= (transform [:unit-23]) [:unit-code "23"]))
    (is (= (transform [:unit-2A]) [:unit-code "2A"]))
    (is (= (transform [:unit-A62]) [:unit-code "A62"]))
    (is (= (transform [:unit-BAR]) [:unit-code "BAR"]))
    (is (= (transform [:unit-D54 "wm2"]) [:unit-code "D54"]))
    (is (= (transform [:unit-D61]) [:unit-code "D61"]))
    (is (= (transform [:unit-D62]) [:unit-code "D62"]))
    (is (= (transform [:unit-DD "deg"]) [:unit-code "DD"]))
    (is (= (transform [:unit-H20]) [:unit-code "H20"]))
    (is (= (transform [:unit-KEL]) [:unit-code "KEL"]))
    (is (= (transform [:unit-KGM]) [:unit-code "KGM"]))
    (is (= (transform [:unit-KMT]) [:unit-code "KMT"]))
    (is (= (transform [:unit-M62]) [:unit-code "M62"]))
    (is (= (transform [:unit-MSK]) [:unit-code "MSK"]))
    (is (= (transform [:unit-SEC]) [:unit-code "SEC"]))))
