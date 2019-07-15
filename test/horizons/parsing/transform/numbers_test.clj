(ns horizons.parsing.transform.numbers-test
  (:require
    [clojure.test :refer :all]
    [instaparse.transform :as transform]
    [horizons.core :as h]
    [horizons.parsing.transform.numbers :as n]))

(defn transform [xs] (transform/transform n/transform-rules xs))

(deftest sci-not-to-bigdec-test
  (is (= (n/value-with-exponent-map->bigdec :rotation-rate [:exponent 3] [:value 1])
         [:rotation-rate {:value (bigdec 1000)}]))
  (is (= (n/value-with-exponent-map->bigdec :rotation-rate [:value 1])
         [:rotation-rate {:value 1}])))

(deftest numbers-test
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
