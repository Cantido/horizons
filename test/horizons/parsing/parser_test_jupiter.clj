(ns horizons.parsing.parser-test-jupiter
  (require
    [horizons.core :as h]
    [clj-time.core :as t]))

(def jupiter-map
  {::h/S
   {::h/file-header
    {::h/revision-date {::h/date (t/date-time 2016 9 13)}
     ::h/body-name "Jupiter"
     ::h/body-id 599}
    ::h/geophysical-data
    {::h/mass {::h/exponent 24 ::h/value "1898.13+-.19"}
     ::h/density 1.326M
     ::h/equatorial-radius "71492+-4"
     ::h/polar-radius "66854+-10"
     ::h/mean-radius "69911+-6"
     ::h/flattening 0.06487M
     ::h/sidereal-rotation-period
      {::h/duration
       {::h/hours 9
        ::h/minutes 55
        ::h/seconds 29
        ::h/milliseconds 685}}
     ::h/rotation-rate 1.75865E-4M
     ::h/m 0.089195M
     ::h/hydrostatic-flattening 0.06509M
     ::h/inferred-rotation-period {::h/duration {::h/hours "9.894+-0.02"}}
     ::h/ks 0.494M
     ::h/moment-of-inertia 0.254M
     ::h/monent-of-inertia-upper-bound 0.267M
     ::h/rocky-core-mass 0.0261M
     ::h/y-factor "0.18+-0.04"
     ::h/standard-gravitational-parameter 126686511
     ::h/gm-1-sigma "+-100"
     ::h/g-equatorial 24.79M
     ::h/g-polar 28.34M
     ::h/geometric-albedo 0.52M
     ::h/visual-magnitude -9.40M
     ::h/visual-magnitude-opposition -2.70M
     ::h/obliquity-to-orbit 3.12M
     ::h/mean-sidereal-orbital-period-years 11.862615M
     ::h/mean-sidereal-orbital-period-days 4332.820M
     ::h/mean-daily-motion 0.0831294M
     ::h/orbit-velocity 13.0697M
     ::h/mean-temperature "165+-5"
     ::h/heat-flow-mass 15E7M
     ::h/solar-constant 50.5M
     ::h/dipole-tilt-offset "9.6deg/0.1Rp"
     ::h/escape-velocity 59.5M
     ::h/magnetic-moment 4.2M
     ::h/a-roche-ice 2.76M
     ::h/hill-sphere-radius 740}}})
