(ns horizons.parsing.time-test
  (:require [clojure.test :refer :all]
            [clj-time.core :as t]
            [horizons.core :as h]
            [horizons.parsing.time :refer :all])
  (:import (org.joda.time DateTime Period PeriodType)))

(deftest json-test
  (clojure.data.json/read-str (clojure.data.json/write-str (DateTime.))))

(deftest timestamp-transformer-test
  (is (= (timestamp-transformer
           [(t/date-time 2017 03 23)]
           {::h/time
            {::h/hour-of-day 1
             ::h/minute-of-hour 2
             ::h/second-of-minute 3
             ::h/millisecond-of-second 4}})
         {::h/timestamp (t/date-time 2017 03 23 1 2 3 4)})))

(deftest period-test
  (is (= (.toPeriod (t/years 1)) (period-of :years 1)))
  (is (= (.toPeriod (t/years 1)) (period-of :years (float 1))))
  (is (= (.toPeriod (t/days 1)) (period-of :years 1/365)))
  (is (= (.toPeriod (t/days 1)) (period-of :days 1)))
  (is (= (.toPeriod (t/hours 1)) (period-of :hours 1)))
  (is (= (.toPeriod (t/minutes 1)) (period-of :minutes 1)))
  (is (= (.toPeriod (t/seconds 1)) (period-of :seconds 1)))
  (is (= (.toPeriod (t/millis 1)) (period-of :milliseconds 1)))
  (is (= (.toPeriod (t/millis 1)) (period-of :milliseconds 1.4)))
  (is (= (.toPeriod (t/millis 2)) (period-of :milliseconds 1.5)))
  (is (= (.toPeriod (t/millis 0)) (period-of :milliseconds 0.1)))
  (is (= (.toPeriod (t/millis 0)) (period-of :milliseconds 0))))

(deftest iso-format-duration-test
  (is (= (iso-format-duration
           {::h/years 1
            ::h/months 2
            ::h/days 3
            ::h/hours 4
            ::h/minutes 5
            ::h/seconds 6
            ::h/milliseconds 7})
         "P1Y2M3DT4H5M6.007S"))
  (testing "absent fields are set to zero"
    (is (= (iso-format-duration
             {::h/years 1
              ::h/months 2
              ::h/days 3
              ::h/hours 4})
           "P1Y2M3DT4H0M0.000S"))
    (is (= (iso-format-duration {})
           "P0Y0M0DT0H0M0.000S"))))
