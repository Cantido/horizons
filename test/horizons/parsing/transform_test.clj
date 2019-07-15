(ns horizons.parsing.parser-test
  (:require
    [clojure.test :refer :all]
    [instaparse.transform :as it])
    [horizons.parsing.transform :refer :all]
    [horizons.parsing.parser-test-mercury :refer :all]
    [horizons.parsing.parser-test-jupiter :refer :all]
    [horizons.parsing.parser-test-mars-ephemeris :refer :all]
  (:import (org.joda.time Years Duration)))

(defn transform [xs] (it/transform parser/transform-rules xs))

(deftest transform-test
  (testing "measurement values"
    (testing "with units"
      (is (= (transform [:mean-radius [:unit-KMT] [:value "2440(+-1)"]])
             [:mean-radius {:unit-code "KMT"} [:value "2440(+-1)"]]))
      (is (= (transform
               [:atmospheric-mass
                [:value [:sci-not [:significand [:float "5.1"]] [:exponent [:integer "18"]]]]
                [:unit-KGM]])
             [:atmospheric-mass
              [:value 5.1E+18M]
              {:unit-code "KGM"}])))

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

(deftest full-transformation-test
  (is (= (transform (get-edn "mercury-geophysical-parsed.edn")) mercury-map))
  (is (= (transform (get-edn "jupiter-geophysical-parsed.edn")) jupiter-map))
  (is (= (transform (get-edn "mars-ephemeredes-parsed.edn")) mars-map)))
