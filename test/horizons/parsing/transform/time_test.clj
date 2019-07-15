(ns horizons.parsing.transform.time-test
  (:require [clojure.test :refer :all]
            [clj-time.core :as t]
            [instaparse.transform :as transform]
            [horizons.core :as h]
            [horizons.parsing.transform.time :refer :all])
  (:import (org.joda.time DateTime Period PeriodType)))

(defn transform [xs] (transform/transform transform-rules xs))

(deftest json-test
  (clojure.data.json/read-str (clojure.data.json/write-str (DateTime.)))
  (clojure.data.json/read-str (clojure.data.json/write-str (Period.))))

(def timestamp-tree
  [:timestamp
   [:era "A.D."]
   [:date
    [:year 2017]
    [:month "Feb"]
    [:day 24]]
   [:time
    [:hour-of-day 0]
    [:minute-of-hour 0]
    [:second-of-minute 0]
    [:millisecond-of-second 0]]
   [:time-zone "UT"]])

(def timestamp-map
  {::h/timestamp (t/date-time 2017 2 24 0 0 0 0)})

(deftest transformations
  (testing "periods"
    (are [tree period] (= (transform tree) (.toPeriod period))
      [:years 1]  (t/years 1)
      [:years 1.0] (t/years 1)
      [:years 1.1] (t/plus (t/years 1) (t/days 36) (t/hours 12))
      [:days 1] (t/days 1)
      [:days 1.5] (t/plus (t/days 1) (t/hours 12))
      [:hours 1] (.toPeriod (t/hours 1))
      [:hours 1.5] (t/plus (t/hours 1) (t/minutes 30))
      [:minutes 1] (t/minutes 1)
      [:minutes 1.5] (t/plus (t/minutes 1) (t/seconds 30))
      [:seconds 1] (t/seconds 1)
      [:seconds 1.5] (t/plus (t/seconds 1) (t/millis 500))
      [:milliseconds 1] (t/millis 1)
      [:milliseconds 1] (t/millis 1)
      [:milliseconds 1.4] (t/millis 1)
      [:milliseconds 1.5] (t/millis 2)
      [:milliseconds 0.1] (t/millis 0)
      [:milliseconds 0] (t/millis 0)
      [:duration [:hours 9] [:minutes 55] [:seconds 29.685]]
      (t/plus (t/hours 9) (t/minutes 55) (t/seconds 29) (t/millis 685))))
  (testing "durations"
    (is (= (transform [:duration [:years 1] [:days 1]])
           (t/plus (t/years 1) (t/days 1)))))
  (testing "timestamp"
    (is (= (transform timestamp-tree) timestamp-map))))
