(ns horizons.parsing.transform.units-test
  (:require
    [clojure.test :refer :all]
    [instaparse.transform :as transform]
    [horizons.parsing.transform.units :refer :all]))

(deftest unit-codes-test
  (are [tree result] (= (transform/transform transform-rules tree) result)
    [:unit-23] {:unit-code "23" :unit-text "g/cm³"}
    [:unit-2A] {:unit-code "2A"}
    [:unit-A62] {:unit-code "A62"}
    [:unit-BAR] {:unit-code "BAR"}
    [:unit-D54 "wm2"] {:unit-code "D54"}
    [:unit-D61] {:unit-code "D61"}
    [:unit-D62] {:unit-code "D62"}
    [:unit-DD "deg"] {:unit-code "DD"}
    [:unit-H20] {:unit-code "H20"}
    [:unit-KEL] {:unit-code "KEL"}
    [:unit-KGM] {:unit-code "KGM"}
    [:unit-KMT] {:unit-code "KMT"}
    [:unit-M62] {:unit-code "M62"}
    [:unit-MSK] {:unit-code "MSK"}
    [:unit-SEC] {:unit-code "SEC"}))
