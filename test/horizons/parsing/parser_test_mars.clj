(ns horizons.parsing.parser-test-mars
  (:require
    [horizons.core :as h]
    [clj-time.core :as t]))

(def mars-map
  {::h/S
   {::h/file-header
    {::h/revision-date {::h/date (t/date-time 2012 9 28)}
     ::h/body-name "Mars"
     ::h/body-id 499}
    ::h/geophysical-data
    {::h/mean-radius "3389.9(2+-4)"
     ::h/density "3.933(5+-4)"
     ::h/mass 6.4185M
     ::h/flattening "1/154.409"
     ::h/volume 16.318M
     ::h/semi-major-axis "3397+-4"
     ::h/sidereal-rotation-period {::h/duration {::h/hours 24.622962M}}
     ::h/rotation-rate 7.088218M
     ::h/mean-solar-day 1.0274907M
     ::h/polar-gravity 3.758M
     ::h/moment-of-inertia 0.366M
     ::h/equatorial-gravity 3.71M
     ::h/core-radius "~1700"
     ::h/potential-love-k2 "0.153 +-.017"
     ::h/grav-spectral-fact 14E5M
     ::h/topo-spectral-fact 96E5M
     ::h/fig-offset "2.50+-0.07"
     ::h/offset "62d / 88d"
     ::h/standard-gravitational-parameter 42828.3M
     ::h/equatorial-radius 3394.0M
     ::h/gm-1-sigma "+- 0.1"
     ::h/mass-ratio-from-sun  "3098708+-9"
     ::h/atmospheric-pressure 0.0056M
     ::h/maximum-angular-diameter "17.9\""
     ::h/mean-temperature 210
     ::h/visual-magnitude -1.52M
     ::h/geometric-albedo 0.150M
     ::h/obliquity-to-orbit 25.19M
     ::h/mean-sidereal-orbital-period-years 1.88081578M
     ::h/orbit-velocity 24.1309M
     ::h/mean-sidereal-orbital-period-days 686.98M
     ::h/escape-velocity 5.027M
     ::h/hill-sphere-radius 319.8M
     ::h/magnetic-moment "< 1x10^-4"}
    ::h/targeting
    {::h/target-body
     {::h/body-name "Mars"
      ::h/body-id 499
      ::h/source "mar097"}
     ::h/center-body
     {::h/body-name "Earth"
      ::h/body-id 399
      ::h/source "mar097"}
     ::h/center-site "GEOCENTRIC"}
    ::h/time-frame
    {::h/start-time { ::h/timestamp (t/date-time 2017 2 24)}
     ::h/stop-time { ::h/timestamp (t/date-time 2017 3 26)}
     ::h/step-size { ::h/duration {::h/minutes 1440}}}
    ::h/constants
    {::h/target-pole-equ "IAU_MARS"
     ::h/target-radii
     {::h/equator 3396.2M
      ::h/meridian 3396.2M
      ::h/pole 3376.2M}
     ::h/center-geodetic
     {::h/longitude 0E-8M
      ::h/latitude 0E-8M
      ::h/altitude 0E-7M}
     ::h/center-cylindric
     {::h/longitude 0E-8M
      ::h/xy-dimension 0E-8M
      ::h/z-dimension 0E-7M}
     ::h/center-pole-equ "High-precision EOP model"
     ::h/center-radii "6378.1 x 6378.1 x 6356.8 km"
     ::h/target-primary "Sun"
     ::h/visibility-interferer "MOON (R_eq= 1737.400) km"
     ::h/rel-light-bend "Sun, EARTH"
     ::h/rel-light-bend-gm "1.3271E+11, 3.9860E+05 km^3/s^2"
     ::h/atmospheric-refraction "NO (AIRLESS)"
     ::h/ra-format "HMS"
     ::h/time-format "CAL"
     ::h/eop-file "eop.170306.p170528"
     ::h/eop-coverage "DATA-BASED 1962-JAN-20 TO 2017-MAR-06. PREDICTS-> 2017-MAY-27"
     ::h/units-conversion "1 au= 149597870.700 km, c= 299792.458 km/s, 1 day= 86400.0 s"
     ::h/table-cutoffs-1 "Elevation (-90.0deg=NO ),Airmass (>38.000=NO), Daylight (NO )"
     ::h/table-cutoffs-2 "Solar Elongation (  0.0,180.0=NO ),Local Hour Angle( 0.0=NO )"}
    ::h/ephemeris
    #{
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 2 24)}
       ::h/ascension-declination "01 12 18.78 +07 36 55.3"
       ::h/apparent-magnitude 1.27M
       ::h/surface-brightness 4.29M
       ::h/range 2.00321056835551M
       ::h/range-rate 11.4427302M
       ::h/sun-observer-target-angle 44.2386M
       ::h/sun-observer-target-angle-direction "/T"
       ::h/sun-target-observer-angle 28.0789M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 2 25) } ::h/ascension-declination "01 15 01.66 +07 54 13.7" ::h/apparent-magnitude 1.28M ::h/surface-brightness 4.29M ::h/range 2.00981380013486M ::h/range-rate 11.4227410M ::h/sun-observer-target-angle 43.9639M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 27.9095M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 2 26) } ::h/ascension-declination "01 17 44.64 +08 11 27.0" ::h/apparent-magnitude 1.28M ::h/surface-brightness 4.29M ::h/range 2.01640513408896M ::h/range-rate 11.4015385M ::h/sun-observer-target-angle 43.6889M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 27.7398M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 2 27) } ::h/ascension-declination "01 20 27.73 +08 28 35.1" ::h/apparent-magnitude 1.29M ::h/surface-brightness 4.29M ::h/range 2.02298387584947M ::h/range-rate 11.3791510M ::h/sun-observer-target-angle 43.4138M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 27.5698M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 2 28) } ::h/ascension-declination "01 23 10.92 +08 45 37.8" ::h/apparent-magnitude 1.29M ::h/surface-brightness 4.29M ::h/range 2.02954935871518M ::h/range-rate 11.3556468M ::h/sun-observer-target-angle 43.1386M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 27.3994M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 3  1) } ::h/ascension-declination "01 25 54.22 +09 02 35.0" ::h/apparent-magnitude 1.30M ::h/surface-brightness 4.29M ::h/range 2.03610096725679M ::h/range-rate 11.3311345M ::h/sun-observer-target-angle 42.8632M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 27.2287M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 3  2) } ::h/ascension-declination "01 28 37.63 +09 19 26.5" ::h/apparent-magnitude 1.31M ::h/surface-brightness 4.29M ::h/range 2.04263815900201M ::h/range-rate 11.3057563M ::h/sun-observer-target-angle 42.5877M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 27.0578M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 3  3) } ::h/ascension-declination "01 31 21.16 +09 36 12.2" ::h/apparent-magnitude 1.31M ::h/surface-brightness 4.29M ::h/range 2.04916047992581M ::h/range-rate 11.2796738M ::h/sun-observer-target-angle 42.3121M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 26.8865M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 3  4) } ::h/ascension-declination "01 34 04.81 +09 52 51.9" ::h/apparent-magnitude 1.32M ::h/surface-brightness 4.29M ::h/range 2.05566757090948M ::h/range-rate 11.2530511M ::h/sun-observer-target-angle 42.0364M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 26.7150M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 3  5) } ::h/ascension-declination "01 36 48.58 +10 09 25.6" ::h/apparent-magnitude 1.32M ::h/surface-brightness 4.29M ::h/range 2.06215916462462M ::h/range-rate 11.2260395M ::h/sun-observer-target-angle 41.7606M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 26.5433M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 3  6) } ::h/ascension-declination "01 39 32.48 +10 25 53.0" ::h/apparent-magnitude 1.33M ::h/surface-brightness 4.29M ::h/range 2.06863507430212M ::h/range-rate 11.1987650M ::h/sun-observer-target-angle 41.4847M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 26.3713M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 3  7) } ::h/ascension-declination "01 42 16.51 +10 42 14.0" ::h/apparent-magnitude 1.34M ::h/surface-brightness 4.29M ::h/range 2.07509517679921M ::h/range-rate 11.1713210M ::h/sun-observer-target-angle 41.2086M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 26.1990M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 3  8) } ::h/ascension-declination "01 45 00.67 +10 58 28.5" ::h/apparent-magnitude 1.34M ::h/surface-brightness 4.29M ::h/range 2.08153939231075M ::h/range-rate 11.1437638M ::h/sun-observer-target-angle 40.9324M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 26.0266M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 3  9) } ::h/ascension-declination "01 47 44.97 +11 14 36.4" ::h/apparent-magnitude 1.35M ::h/surface-brightness 4.29M ::h/range 2.08796766255679M ::h/range-rate 11.1161115M ::h/sun-observer-target-angle 40.6561M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 25.8539M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 3 10) } ::h/ascension-declination "01 50 29.41 +11 30 37.4" ::h/apparent-magnitude 1.35M ::h/surface-brightness 4.29M ::h/range 2.09437992890070M ::h/range-rate 11.0883453M ::h/sun-observer-target-angle 40.3797M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 25.6810M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 3 11) } ::h/ascension-declination "01 53 14.00 +11 46 31.5" ::h/apparent-magnitude 1.36M ::h/surface-brightness 4.29M ::h/range 2.10077611182764M ::h/range-rate 11.0604126M ::h/sun-observer-target-angle 40.1030M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 25.5079M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 3 12) } ::h/ascension-declination "01 55 58.75 +12 02 18.6" ::h/apparent-magnitude 1.37M ::h/surface-brightness 4.29M ::h/range 2.10715609336015M ::h/range-rate 11.0322340M ::h/sun-observer-target-angle 39.8263M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 25.3346M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 3 13) } ::h/ascension-declination "01 58 43.65 +12 17 58.5" ::h/apparent-magnitude 1.37M ::h/surface-brightness 4.29M ::h/range 2.11351970392719M ::h/range-rate 11.0037118M ::h/sun-observer-target-angle 39.5493M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 25.1610M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 3 14) } ::h/ascension-declination "02 01 28.72 +12 33 31.2" ::h/apparent-magnitude 1.38M ::h/surface-brightness 4.29M ::h/range 2.11986671468170M ::h/range-rate 10.9747401M ::h/sun-observer-target-angle 39.2721M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 24.9873M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 3 15) } ::h/ascension-declination "02 04 13.96 +12 48 56.4" ::h/apparent-magnitude 1.38M ::h/surface-brightness 4.29M ::h/range 2.12619683533491M ::h/range-rate 10.9452134M ::h/sun-observer-target-angle 38.9948M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 24.8133M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 3 16) } ::h/ascension-declination "02 06 59.37 +13 04 14.1" ::h/apparent-magnitude 1.39M ::h/surface-brightness 4.29M ::h/range 2.13250971657745M ::h/range-rate 10.9150336M ::h/sun-observer-target-angle 38.7173M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 24.6390M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 3 17) } ::h/ascension-declination "02 09 44.97 +13 19 24.2" ::h/apparent-magnitude 1.39M ::h/surface-brightness 4.29M ::h/range 2.13880495546021M ::h/range-rate 10.8841134M ::h/sun-observer-target-angle 38.4395M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 24.4646M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 3 18) } ::h/ascension-declination "02 12 30.75 +13 34 26.5" ::h/apparent-magnitude 1.40M ::h/surface-brightness 4.29M ::h/range 2.14508210190519M ::h/range-rate 10.8523769M ::h/sun-observer-target-angle 38.1616M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 24.2899M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 3 19) } ::h/ascension-declination "02 15 16.72 +13 49 20.9" ::h/apparent-magnitude 1.40M ::h/surface-brightness 4.29M ::h/range 2.15134066476118M ::h/range-rate 10.8197573M ::h/sun-observer-target-angle 37.8834M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 24.1149M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 3 20) } ::h/ascension-declination "02 18 02.89 +14 04 07.4" ::h/apparent-magnitude 1.41M ::h/surface-brightness 4.29M ::h/range 2.15758011632925M ::h/range-rate 10.7861941M ::h/sun-observer-target-angle 37.6051M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 23.9397M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 3 21) } ::h/ascension-declination "02 20 49.26 +14 18 45.8" ::h/apparent-magnitude 1.41M ::h/surface-brightness 4.29M ::h/range 2.16379989488025M ::h/range-rate 10.7516294M ::h/sun-observer-target-angle 37.3265M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 23.7642M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 3 22) } ::h/ascension-declination "02 23 35.83 +14 33 16.0" ::h/apparent-magnitude 1.42M ::h/surface-brightness 4.29M ::h/range 2.16999940528009M ::h/range-rate 10.7160051M ::h/sun-observer-target-angle 37.0478M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 23.5885M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 3 23) } ::h/ascension-declination "02 26 22.60 +14 47 37.9" ::h/apparent-magnitude 1.43M ::h/surface-brightness 4.29M ::h/range 2.17617801842483M ::h/range-rate 10.6792616M ::h/sun-observer-target-angle 36.7688M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 23.4126M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 3 24) } ::h/ascension-declination "02 29 09.59 +15 01 51.3" ::h/apparent-magnitude 1.43M ::h/surface-brightness 4.29M ::h/range 2.18233507080696M ::h/range-rate 10.6413401M ::h/sun-observer-target-angle 36.4897M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 23.2364M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 3 25) } ::h/ascension-declination "02 31 56.79 +15 15 56.2" ::h/apparent-magnitude 1.44M ::h/surface-brightness 4.29M ::h/range 2.18846986617632M ::h/range-rate 10.6021873M ::h/sun-observer-target-angle 36.2104M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 23.0599M}
      {::h/measurement-time { ::h/timestamp (t/date-time 2017 3 26) } ::h/ascension-declination "02 34 44.21 +15 29 52.5" ::h/apparent-magnitude 1.44M ::h/surface-brightness 4.29M ::h/range 2.19458168176036M ::h/range-rate 10.5617667M ::h/sun-observer-target-angle 35.9310M ::h/sun-observer-target-angle-direction "/T" ::h/sun-target-observer-angle 22.8832M}}}})


