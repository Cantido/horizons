(ns horizons.parser-test-mercury
  (:require
    [clj-time.core :as t]))

(def mercury-map
  {:S {
       :file-header {
                     :revision-date {
                                     :date (t/date-time 2013 07 31)}
                     :body-name "Mercury"
                     :body-id 199}
       :geophysical-data {
                          :mean-radius "2440(+-1)"
                          :density 5.427M
                          :mass 3.302M
                          :flattening nil
                          :volume 6.085M
                          :semi-major-axis nil
                          :sidereal-rotation-period {:duration {:days 58.6462M}}
                          :rotation-rate 0.124001M
                          :mean-solar-day 175.9421M
                          :polar-gravity nil
                          :moment-of-inertia 0.33M
                          :equatorial-gravity 3.701M
                          :core-radius "~1600"
                          :potential-love-k2 nil

                          :standard-gravitational-parameter 22032.09M
                          :equatorial-radius 2440
                          :gm-1-sigma "+-0.91"
                          :mass-ratio-from-sun 6023600

                          :atmospheric-pressure nil
                          :maximum-angular-diameter "11.0\""
                          :mean-temperature nil
                          :visual-magnitude -0.42M
                          :geometric-albedo 0.106M
                          :obliquity-to-orbit "2.11' +/- 0.1'"
                          :mean-sidereal-orbital-period-years 0.2408467M
                          :orbit-velocity 47.362M
                          :mean-sidereal-orbital-period-days 87.969257M
                          :escape-velocity 4.435M
                          :hill-sphere-radius 94.4M
                          :solar-constant 9936.9M}}})
