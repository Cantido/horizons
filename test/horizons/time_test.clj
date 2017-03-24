(ns horizons.time-test
  (:require [clojure.test :refer :all]
            [clj-time.core :as t]
            [horizons.core :as h]
            [horizons.parsing.time :refer :all]))

(deftest timestamp-transformer-test
  (is (= (timestamp-transformer
           [(t/date-time 2017 03 23)]
           {::h/time
            {::h/hour-of-day 1
             ::h/minute-of-hour 2
             ::h/second-of-minute 3
             ::h/millisecond-of-second 4}})
         {::h/timestamp (t/date-time 2017 03 23 1 2 3 4)})))

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
