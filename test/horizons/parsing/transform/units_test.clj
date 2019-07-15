(ns horizons.parsing.transform.units-test
  (:require
    [clojure.test :refer :all]
    [instaparse.transform :as transform]
    [horizons.parsing.transform.units :refer :all]))

(deftest unit-codes-test
  (are [tree result] (= (transform/transform transform-rules tree) result)
    [:unit-23] {:unit-code "23" :unit-text "g/cm³"}
    [:unit-2A] {:unit-code "2A" :unit-text "rad/s"}
    [:unit-A62] {:unit-code "A62" :unit-text "erg/g·s"}
    [:unit-BAR] {:unit-code "BAR" :unit-text "bar"}
    [:unit-D54 "wm2"] {:unit-code "D54" :unit-text "W/m²"}
    [:unit-D61] {:unit-code "D61" :unit-text "'"}
    [:unit-D62] {:unit-code "D62" :unit-text "\""}
    [:unit-DD "deg"] {:unit-code "DD" :unit-text "°"}
    [:unit-H20] {:unit-code "H20" :unit-text "km³"}
    [:unit-KEL] {:unit-code "KEL" :unit-text "K"}
    [:unit-KGM] {:unit-code "KGM" :unit-text "kg"}
    [:unit-KMT] {:unit-code "KMT" :unit-text "km"}
    [:unit-M62] {:unit-code "M62" :unit-text "km/s"}
    [:unit-MSK] {:unit-code "MSK" :unit-text "m/s²"}
    [:unit-SEC] {:unit-code "SEC" :unit-text "s"}))
