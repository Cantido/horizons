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
    {::h/mass {::h/unit-code "KGM" ::h/value 1.89813E+27M}
     ::h/density {::h/unit-code "23" ::h/value 1.326M}
     ::h/equatorial-radius {::h/value "71492+-4" ::h/unit-code "KMT"}
     ::h/polar-radius {::h/unit-code "KMT" ::h/value "66854+-10"}
     ::h/mean-radius {::h/unit-code "KMT" ::h/value "69911+-6"}
     ::h/flattening {::h/value 0.06487M}
     ::h/sidereal-rotation-period
     {::h/value
      {::h/duration
       {::h/hours 9
        ::h/minutes 55
        ::h/seconds 29.685M}}}
     ::h/rotation-rate {::h/unit-code "2A" ::h/value 1.75865E-4M}
     ::h/m {::h/value 0.089195M}
     ::h/hydrostatic-flattening {::h/value 0.06509M}
     ::h/inferred-rotation-period {::h/value {::h/duration {::h/hours "9.894+-0.02"}}}
     ::h/ks {::h/value 0.494M}
     ::h/moment-of-inertia {::h/value 0.254M}
     ::h/monent-of-inertia-upper-bound {::h/value 0.267M}
     ::h/rocky-core-mass {::h/value 0.0261M}
     ::h/y-factor {::h/value 0.18M}
     ::h/standard-gravitational-parameter {::h/value 126686511}
     ::h/gm-1-sigma {::h/value "+-100"}
     ::h/equatorial-gravity {::h/unit-code "MSK" ::h/value 24.79M}
     ::h/g-polar {::h/unit-code "MSK" ::h/value 28.34M}
     ::h/geometric-albedo {::h/value 0.52M}
     ::h/visual-magnitude {::h/value -9.40M}
     ::h/visual-magnitude-opposition {::h/value -2.70M}
     ::h/obliquity-to-orbit {::h/value 3.12M ::h/unit-code "DD"}
     ::h/mean-sidereal-orbital-period-years 11.862615M
     ::h/mean-sidereal-orbital-period-days 4332.820M
     ::h/mean-daily-motion {::h/value 0.0831294M}
     ::h/orbit-velocity {::h/value 13.0697M ::h/unit-code "M62"}
     ::h/mean-temperature {::h/value "165+-5" ::h/unit-code "KEL"}
     ::h/heat-flow-mass {::h/value 15E7M ::h/unit-code "A62"}
     ::h/solar-constant {::h/value 50.5M ::h/unit-code "D54"}
     ::h/dipole-tilt-offset {::h/value "9.6deg/0.1Rp"}
     ::h/escape-velocity {::h/unit-code "M62" ::h/value 59.5M}
     ::h/magnetic-moment {::h/value 4.2M}
     ::h/a-roche-ice {::h/value 2.76M}
     ::h/hill-sphere-radius {::h/value 740}}}})
