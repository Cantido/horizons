(ns horizons.parsing.parser-test
  (:require
    [clj-time.core :as t]
    [clojure.java.io :as io]
    [clojure.test :refer :all]
    [instaparse.core :as insta]
    [horizons.core :as h]
    [horizons.parsing.parser :as parser]
    [horizons.parsing.parser-test-mercury :refer :all]
    [horizons.parsing.parser-test-jupiter :refer :all]
    [instaparse.transform :as transform]
    [com.stuartsierra.component :as component]
    [horizons.test-utils :as test])
  (:import (org.joda.time Years Duration)))

(defn component
  ([] (:parser (test/build-test-system)))
  ([s] (:parser (test/build-test-system s))))

(defn get-file [name]
  (slurp
    (io/file
      (io/resource name))))

(defn get-edn [name]
  (read-string (get-file name)))

(defn parser-component
  ([]
   {:post [(some? %)]}
   (component/start (parser/new-parser (io/resource "horizons.bnf")))))


(defn parse-with-rule [kw s]
  (-> "resources/horizons.bnf"
    parser/new-parser
    (assoc :parser-opts [:start kw])
    component/start
    (parser/parse s)))

(defn parse-file [name]
  (parser/parse (component) (get-file name)))

(defn success? [x]
  (not (insta/failure? x)))

(deftest geophysical-grammar-test
  (is (= (parse-file "mercury-geophysical.txt") (get-edn "mercury-geophysical-parsed.edn")))
  (is (= (parse-file "venus-geophysical.txt") (get-edn "venus-geophysical-parsed.edn")))
  (is (= (parse-file "earth-geophysical.txt") (get-edn "earth-geophysical-parsed.edn")))
  (is (= (parse-file "mars-geophysical.txt") (get-edn "mars-geophysical.edn")))
  (is (= (parse-file "jupiter-geophysical.txt") (get-edn "jupiter-geophysical-parsed.edn")))
  (is (= (parse-file "saturn-geophysical.txt") (get-edn "saturn-geophysical-parsed.edn")))
  (testing "uranus-geophysical.txt"
    (is (success? (parse-file "uranus-geophysical.txt"))))
  (testing "neptune-geophysical.txt"
    (is (success? (parse-file "neptune-geophysical.txt")))))

(deftest ephemeredes-grammar-test
  (is (= (parse-file "mars-ephemeredes.txt") (get-edn "mars-ephemeredes-parsed.edn")))
  (testing "mercury-ephemeredes.txt"
    (is (success? (parse-file "mercury-ephemeredes.txt"))))
  (testing "jupiter-ephemeredes.txt"
    (is (success? (parse-file "jupiter-ephemeredes.txt")))))

(deftest put-keyword-in-ns-test
  (is (= (parser/put-keyword-in-ns :label)
         ::h/label)))

(defn tree->map [tree]
  (clojure.walk/postwalk #(parser/do-if parser/coll-of-colls? parser/tree-vec->map %) tree))

(deftest tree->map-test
  (testing "converting a basic vector"
    (is (= (parser/tree-vec->map [:label [:one 1] [:two 2]])
           {:label {:one 1 :two 2}})))
  (testing "walking date trees"
    (testing "with one level of nesting"
      (is (= (tree->map
               [:revision-date
                [:month "Jul"]
                [:day 31]
                [:year 2013]])
             {:revision-date
              {:month "Jul"
               :day 31
               :year 2013}})))
    (testing "with two levels of nesting"
      (is (= (tree->map
               [:file-header
                [:revision-date
                 [:month "Jul"]
                 [:day 31]
                 [:year 2013]]])
             {:file-header
              {:revision-date
               {:month "Jul"
                :day 31
                :year 2013}}})))
    (testing "with a level of nesting in the middle of a vector"
      (is (= (tree->map
               [:file-header
                [:revision-date
                 [:month "Jul"]
                 [:day 31]
                 [:year 2013]]
                [:body-name "Mars"]
                [:body-id 499]])
             {:file-header
              {:revision-date
               {:month "Jul"
                :day 31
                :year 2013}
               :body-name "Mars"
               :body-id 499}}))))
  (testing "walking geophysical data"
    (is (= (tree->map
             [:geophysical-data
              [:mean-radius [:value "3389.9(2+-4)"]]
              [:density "3.933(5+-4)"]
              [:mass "6.4185"]])
           {:geophysical-data
            {:mean-radius {:value "3389.9(2+-4)"}
             :density "3.933(5+-4)"
             :mass "6.4185"}}))))

(defn transform [xs] (transform/transform parser/transform-rules xs))

(deftest transform-test
  (testing "numbers"
    (testing "in scientific notation"
      (is (= (transform
               [:sci-not
                [:significand [:float "5.05"]]
                [:mantissa [:integer "22"]]])
             5.05E22M))
      (is (= (transform
               [:sci-not
                [:significand [:integer "5"]]
                [:mantissa [:integer "22"]]])
             5E22M)))
    (testing "as comma-separated integers"
      (is (= (transform [:integer [:comma-separated-integer "123,456,789"]])
             123456789))))
  (testing "measurement values"
    (testing "with units"
      (is (= (transform [:mean-radius [:unit-KMT] [:value "2440(+-1)"]])
             [:mean-radius [:unit-code "KMT"] [:value "2440(+-1)"]]))
      (is (= (transform
               [:atmospheric-mass
                [:value [:sci-not [:significand [:float "5.1"]] [:exponent [:integer "18"]]]]
                [:unit-KGM]])
             [:atmospheric-mass
              [:value 5.1E+18M]
              [:unit-code "KGM"]])))
    (testing "with exponents"
      (is (= (transform
               [:heat-flow-mass
                [:exponent [:integer "7"]]
                [:value [:integer "15"]]])
             [:heat-flow-mass {:value 15E7M}]))
      (is (= (transform [:rotation-rate [:exponent [:integer "-4"]] [:unit-2A] [:value [:float "1.75865"]]])
             [:rotation-rate {:value 0.000175865M :unit-code "2A"}])))
    (testing "as a set (like ephemeredes)"
      (is (= (transform
               [:ephemeredes
                [:ephemeris [:x-position 1]]
                [:ephemeris [:x-position 2]]
                [:ephemeris [:x-position 3]]])
             [:ephemeredes
              #{
                {:x-position 1}
                {:x-position 2}
                {:x-position 3}}]))))
  (testing "periods"
    (is (= (transform [:years [:integer "1"]])
           (.toPeriod (t/years 1))))
    (is (= (transform [:years [:float "1"]])
           (.toPeriod (t/years 1))))
    (is (= (transform [:years [:float "1.1"]])
           (t/plus (t/years 1) (t/days 36) (t/hours 12))))
    (is (= (transform [:days [:integer "1"]])
           (.toPeriod (t/days 1))))
    (is (= (transform [:days [:float "1.5"]])
           (t/plus (t/days 1) (t/hours 12))))
    (is (= (transform [:hours [:integer "1"]])
           (.toPeriod (t/hours 1))))
    (is (= (transform [:hours [:float "1.5"]])
           (t/plus (t/hours 1) (t/minutes 30))))
    (is (= (transform [:minutes [:integer "1"]])
           (.toPeriod (t/minutes 1))))
    (is (= (transform [:minutes [:float "1.5"]])
           (t/plus (t/minutes 1) (t/seconds 30))))
    (is (= (transform [:seconds [:integer "1"]])
           (.toPeriod (t/seconds 1))))
    (is (= (transform [:seconds [:float "1.5"]])
           (t/plus (t/seconds 1) (t/millis 500))))
    (is (= (transform [:milliseconds [:integer "1"]])
           (.toPeriod (t/millis 1)))))
  (testing "durations"
    (is (= (transform [:duration [:years [:integer "1"]]
                                 [:days [:integer "1"]]])
           (t/plus (t/years 1) (t/days 1)))))
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

(defn pd [s]
  (parse-with-rule :duration s))

(deftest duration-parsing
  (is (= (pd "1.0y") [:duration [:years [:float "1.0"]]]))
  (is (= (pd "1d") [:duration [:days [:integer "1"]]]))
  (is (= (pd "1h") [:duration [:hours [:integer "1"]]]))
  (is (= (pd "1m") [:duration [:minutes [:integer "1"]]]))
  (is (= (pd "3.4s") [:duration [:seconds [:float "3.4"]]]))
  (is (= (pd "1.234s") [:duration [:seconds [:float "1.234"]]])))

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

(defn restructure [xs]
  (->> xs
       (transform/transform parser/transform-rules)
       (clojure.walk/postwalk #(parser/do-if parser/coll-of-colls? parser/tree-vec->map %))
       (clojure.walk/postwalk #(parser/do-if keyword? parser/put-keyword-in-ns %))))

(deftest full-transformation-test
  (is (= (restructure (get-edn "mercury-geophysical-parsed.edn")) mercury-map))
  (is (= (restructure (get-edn "jupiter-geophysical-parsed.edn")) jupiter-map))
  (is (= (restructure timestamp-tree) timestamp-map)))
