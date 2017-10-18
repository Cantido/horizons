(ns horizons.parsing.parser-test-mercury
  (:require
    [horizons.core :as h]
    [clj-time.core :as t]))

(def mercury-map
  {::h/S
   {::h/file-header
    {::h/revision-date {::h/date (t/date-time 2013 7 31)}
     ::h/body-name "Mercury"
     ::h/body-id 199}
    ::h/geophysical-data
    {::h/mean-radius {::h/unit-code "KMT" ::h/value "2440(+-1)"}
     ::h/density {::h/unit-code "23" ::h/value 5.427M}
     ::h/mass {::h/unit-code "KGM" ::h/value 3.302E23M}
     ::h/flattening {}
     ::h/volume {::h/value 6.085E10M ::h/unit-code "H20"}
     ::h/semi-major-axis {}
     ::h/sidereal-rotation-period {::h/value (t/plus
                                               (t/days 58)
                                               (t/hours 15)
                                               (t/minutes 30)
                                               (t/seconds 31)
                                               (t/millis 680))}
     ::h/rotation-rate {::h/unit-code "SEC" ::h/value 12400.1M}
     ::h/mean-solar-day {::h/value (t/plus
                                     (t/days 175)
                                     (t/hours 22)
                                     (t/minutes 36)
                                     (t/seconds 37)
                                     (t/millis 440))}
     ::h/polar-gravity {::h/unit-code "MSK"}
     ::h/moment-of-inertia {::h/value 0.33M}
     ::h/equatorial-gravity {::h/unit-code "MSK" ::h/value 3.701M}
     ::h/core-radius {::h/unit-code "KMT" ::h/value "~1600"}
     ::h/potential-love-k2 {}
     ::h/standard-gravitational-parameter {::h/value 22032.09M}
     ::h/equatorial-radius {::h/value 2440 ::h/unit-code "KMT"}
     ::h/gm-1-sigma {::h/value "+-0.91"}
     ::h/mass-ratio-from-sun {::h/value 6023600}
     ::h/atmospheric-pressure {::h/unit-code "BAR"}
     ::h/maximum-angular-diameter {::h/value "11.0\""}
     ::h/mean-temperature {::h/unit-code "KEL"}
     ::h/visual-magnitude {::h/value -0.42M}
     ::h/geometric-albedo {::h/value 0.106M}
     ::h/obliquity-to-orbit {::h/value "2.11' +/- 0.1'"}
     ::h/mean-sidereal-orbital-period-years 0.2408467M
     ::h/orbit-velocity {::h/value 47.362M ::h/unit-code "M62"}
     ::h/mean-sidereal-orbital-period-days 87.969257M
     ::h/escape-velocity {::h/unit-code "M62" ::h/value 4.435M}
     ::h/hill-sphere-radius {::h/value 94.4M}
     ::h/solar-constant {::h/unit-code "D54" ::h/value 9936.9M}}}})
