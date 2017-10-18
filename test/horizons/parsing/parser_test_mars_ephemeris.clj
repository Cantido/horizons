(ns horizons.parsing.parser-test-mars-ephemeris
  (:require
    [horizons.core :as h]
    [clj-time.core :as t]))

(def mars-map
  {::h/S
   {::h/time-frame
    {::h/start-time {::h/timestamp (t/date-time 2017 3 26)}
     ::h/stop-time {::h/timestamp (t/date-time 2017 4 9)}
     ::h/step-size (.toPeriod (t/minutes 60))}
    ::h/ephemeredes
    #{{
       ::h/julian-day-number 2457838.5M
       ::h/timestamp (t/date-time 2017 03 26)
       ::h/x-position 2.469362459741699E+8M
       ::h/y-position 2.163270043150773E+8M
       ::h/z-position 1.830259394692242E+6M
       ::h/x-velocity -1.998813030648808E+6M
       ::h/y-velocity 3.666065138258934E+6M
       ::h/z-velocity 6.696681569533017E+4M
       ::h/lt 1.267450720461575E-2M
       ::h/range 3.282959521852242E+8M
       ::h/range-rate 9.126279731446180E+5M}}}})
