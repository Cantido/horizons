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
    {::h/mean-radius "2440(+-1)"
     ::h/density 5.427M
     ::h/mass 3.302E23M
     ::h/flattening nil
     ::h/volume 6.085E10M
     ::h/semi-major-axis nil
     ::h/sidereal-rotation-period {::h/duration {::h/days 58.6462M}}
     ::h/rotation-rate 12400.1M
     ::h/mean-solar-day 175.9421M
     ::h/polar-gravity nil
     ::h/moment-of-inertia 0.33M
     ::h/equatorial-gravity 3.701M
     ::h/core-radius "~1600"
     ::h/potential-love-k2 nil
     ::h/standard-gravitational-parameter 22032.09M
     ::h/equatorial-radius 2440
     ::h/gm-1-sigma "+-0.91"
     ::h/mass-ratio-from-sun 6023600
     ::h/atmospheric-pressure nil
     ::h/maximum-angular-diameter "11.0\""
     ::h/mean-temperature nil
     ::h/visual-magnitude -0.42M
     ::h/geometric-albedo 0.106M
     ::h/obliquity-to-orbit "2.11' +/- 0.1'"
     ::h/mean-sidereal-orbital-period-years 0.2408467M
     ::h/orbit-velocity 47.362M
     ::h/mean-sidereal-orbital-period-days 87.969257M
     ::h/escape-velocity 4.435M
     ::h/hill-sphere-radius 94.4M
     ::h/solar-constant 9936.9M}}})
