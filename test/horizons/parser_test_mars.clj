(ns horizons.parser-test-mars
  (:require
    [clj-time.core :as t]))

(def mars-map
  {:S {
       :file-header {
                     :revision-date {:date (t/date-time 2012 9 28)}
                     :body-name "Mars"
                     :body-id 499}
       :geophysical-data {
                          :mean-radius "3389.9(2+-4)"
                          :density "3.933(5+-4)"
                          :mass 6.4185M
                          :flattening "1/154.409"
                          :volume 16.318M
                          :semi-major-axis "3397+-4"
                          :sidereal-rotation-period {:duration {:hours 24.622962M}}
                          :rotation-rate 7.088218M
                          :mean-solar-day 1.0274907M
                          :polar-gravity 3.758M
                          :moment-of-inertia 0.366M
                          :equatorial-gravity 3.71M
                          :core-radius "~1700"
                          :potential-love-k2 "0.153 +-.017"

                          :grav-spectral-fact "14 (x10^5)"
                          :topo-spectral-fact "96 (x10^5)"
                          :fig-offset "2.50+-0.07"
                          :offset "62d / 88d"
                          :standard-gravitational-parameter 42828.3M
                          :equatorial-radius 3394.0M
                          :gm-1-sigma "+- 0.1"
                          :mass-ratio-from-sun  "3098708+-9"

                          :atmospheric-pressure 0.0056M
                          :maximum-angular-diameter "17.9\""
                          :mean-temperature 210
                          :visual-magnitude -1.52M
                          :geometric-albedo 0.150M
                          :obliquity-to-orbit 25.19M
                          :mean-sidereal-orbital-period-years 1.88081578M
                          :orbit-velocity 24.1309M
                          :mean-sidereal-orbital-period-days 686.98M
                          :escape-velocity 5.027M
                          :hill-sphere-radius 319.8M
                          :magnetic-moment "< 1x10^-4"}

       :targeting {
                   :target-body {
                                 :body-name "Mars"
                                 :body-id 499
                                 :source "mar097"}
                   :center-body {
                                 :body-name "Earth"
                                 :body-id 399
                                 :source "mar097"}
                   :center-site "GEOCENTRIC"}
       :time-frame {
                    :start-time { :timestamp (t/date-time 2017 2 24)}
                    :stop-time { :timestamp (t/date-time 2017 3 26)}
                    :step-size {:duration {:minutes 1440}}}
       :constants {
                   :target-pole-equ "IAU_MARS"
                   :target-radii {
                                  :equator 3396.2M
                                  :meridian 3396.2M
                                  :pole 3376.2M}
                   :center-geodetic {
                                     :longitude 0E-8M
                                     :latitude 0E-8M
                                     :altitude 0E-7M}
                   :center-cylindric {
                                      :longitude 0E-8M
                                      :xy-dimension 0E-8M
                                      :z-dimension 0E-7M}
                   :center-pole-equ "High-precision EOP model"
                   :center-radii "6378.1 x 6378.1 x 6356.8 km"
                   :target-primary "Sun"
                   :visibility-interferer "MOON (R_eq= 1737.400) km"
                   :rel-light-bend "Sun, EARTH"
                   :rel-light-bend-gm "1.3271E+11, 3.9860E+05 km^3/s^2"
                   :atmospheric-refraction "NO (AIRLESS)"
                   :ra-format "HMS"
                   :time-format "CAL"
                   :eop-file "eop.170306.p170528"
                   :eop-coverage "DATA-BASED 1962-JAN-20 TO 2017-MAR-06. PREDICTS-> 2017-MAY-27"
                   :units-conversion "1 au= 149597870.700 km, c= 299792.458 km/s, 1 day= 86400.0 s"
                   :table-cutoffs-1 "Elevation (-90.0deg=NO ),Airmass (>38.000=NO), Daylight (NO )"
                   :table-cutoffs-2 "Solar Elongation (  0.0,180.0=NO ),Local Hour Angle( 0.0=NO )"}
       :ephemeris #{
                    {
                     :measurement-time { :timestamp (t/date-time 2017 2 24)}
                     :ascension-declination "01 12 18.78 +07 36 55.3"
                     :apparent-magnitude 1.27M
                     :surface-brightness 4.29M
                     :range 2.00321056835551M
                     :range-rate 11.4427302M
                     :sun-observer-target-angle 44.2386M
                     :sun-observer-target-angle-direction "/T"
                     :sun-target-observer-angle 28.0789M}
                    {:measurement-time { :timestamp (t/date-time 2017 2 25) } :ascension-declination "01 15 01.66 +07 54 13.7" :apparent-magnitude 1.28M :surface-brightness 4.29M :range 2.00981380013486M :range-rate 11.4227410M :sun-observer-target-angle 43.9639M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 27.9095M}
                    {:measurement-time { :timestamp (t/date-time 2017 2 26) } :ascension-declination "01 17 44.64 +08 11 27.0" :apparent-magnitude 1.28M :surface-brightness 4.29M :range 2.01640513408896M :range-rate 11.4015385M :sun-observer-target-angle 43.6889M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 27.7398M}
                    {:measurement-time { :timestamp (t/date-time 2017 2 27) } :ascension-declination "01 20 27.73 +08 28 35.1" :apparent-magnitude 1.29M :surface-brightness 4.29M :range 2.02298387584947M :range-rate 11.3791510M :sun-observer-target-angle 43.4138M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 27.5698M}
                    {:measurement-time { :timestamp (t/date-time 2017 2 28) } :ascension-declination "01 23 10.92 +08 45 37.8" :apparent-magnitude 1.29M :surface-brightness 4.29M :range 2.02954935871518M :range-rate 11.3556468M :sun-observer-target-angle 43.1386M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 27.3994M}
                    {:measurement-time { :timestamp (t/date-time 2017 3  1) } :ascension-declination "01 25 54.22 +09 02 35.0" :apparent-magnitude 1.30M :surface-brightness 4.29M :range 2.03610096725679M :range-rate 11.3311345M :sun-observer-target-angle 42.8632M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 27.2287M}
                    {:measurement-time { :timestamp (t/date-time 2017 3  2) } :ascension-declination "01 28 37.63 +09 19 26.5" :apparent-magnitude 1.31M :surface-brightness 4.29M :range 2.04263815900201M :range-rate 11.3057563M :sun-observer-target-angle 42.5877M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 27.0578M}
                    {:measurement-time { :timestamp (t/date-time 2017 3  3) } :ascension-declination "01 31 21.16 +09 36 12.2" :apparent-magnitude 1.31M :surface-brightness 4.29M :range 2.04916047992581M :range-rate 11.2796738M :sun-observer-target-angle 42.3121M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 26.8865M}
                    {:measurement-time { :timestamp (t/date-time 2017 3  4) } :ascension-declination "01 34 04.81 +09 52 51.9" :apparent-magnitude 1.32M :surface-brightness 4.29M :range 2.05566757090948M :range-rate 11.2530511M :sun-observer-target-angle 42.0364M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 26.7150M}
                    {:measurement-time { :timestamp (t/date-time 2017 3  5) } :ascension-declination "01 36 48.58 +10 09 25.6" :apparent-magnitude 1.32M :surface-brightness 4.29M :range 2.06215916462462M :range-rate 11.2260395M :sun-observer-target-angle 41.7606M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 26.5433M}
                    {:measurement-time { :timestamp (t/date-time 2017 3  6) } :ascension-declination "01 39 32.48 +10 25 53.0" :apparent-magnitude 1.33M :surface-brightness 4.29M :range 2.06863507430212M :range-rate 11.1987650M :sun-observer-target-angle 41.4847M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 26.3713M}
                    {:measurement-time { :timestamp (t/date-time 2017 3  7) } :ascension-declination "01 42 16.51 +10 42 14.0" :apparent-magnitude 1.34M :surface-brightness 4.29M :range 2.07509517679921M :range-rate 11.1713210M :sun-observer-target-angle 41.2086M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 26.1990M}
                    {:measurement-time { :timestamp (t/date-time 2017 3  8) } :ascension-declination "01 45 00.67 +10 58 28.5" :apparent-magnitude 1.34M :surface-brightness 4.29M :range 2.08153939231075M :range-rate 11.1437638M :sun-observer-target-angle 40.9324M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 26.0266M}
                    {:measurement-time { :timestamp (t/date-time 2017 3  9) } :ascension-declination "01 47 44.97 +11 14 36.4" :apparent-magnitude 1.35M :surface-brightness 4.29M :range 2.08796766255679M :range-rate 11.1161115M :sun-observer-target-angle 40.6561M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 25.8539M}
                    {:measurement-time { :timestamp (t/date-time 2017 3 10) } :ascension-declination "01 50 29.41 +11 30 37.4" :apparent-magnitude 1.35M :surface-brightness 4.29M :range 2.09437992890070M :range-rate 11.0883453M :sun-observer-target-angle 40.3797M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 25.6810M}
                    {:measurement-time { :timestamp (t/date-time 2017 3 11) } :ascension-declination "01 53 14.00 +11 46 31.5" :apparent-magnitude 1.36M :surface-brightness 4.29M :range 2.10077611182764M :range-rate 11.0604126M :sun-observer-target-angle 40.1030M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 25.5079M}
                    {:measurement-time { :timestamp (t/date-time 2017 3 12) } :ascension-declination "01 55 58.75 +12 02 18.6" :apparent-magnitude 1.37M :surface-brightness 4.29M :range 2.10715609336015M :range-rate 11.0322340M :sun-observer-target-angle 39.8263M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 25.3346M}
                    {:measurement-time { :timestamp (t/date-time 2017 3 13) } :ascension-declination "01 58 43.65 +12 17 58.5" :apparent-magnitude 1.37M :surface-brightness 4.29M :range 2.11351970392719M :range-rate 11.0037118M :sun-observer-target-angle 39.5493M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 25.1610M}
                    {:measurement-time { :timestamp (t/date-time 2017 3 14) } :ascension-declination "02 01 28.72 +12 33 31.2" :apparent-magnitude 1.38M :surface-brightness 4.29M :range 2.11986671468170M :range-rate 10.9747401M :sun-observer-target-angle 39.2721M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 24.9873M}
                    {:measurement-time { :timestamp (t/date-time 2017 3 15) } :ascension-declination "02 04 13.96 +12 48 56.4" :apparent-magnitude 1.38M :surface-brightness 4.29M :range 2.12619683533491M :range-rate 10.9452134M :sun-observer-target-angle 38.9948M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 24.8133M}
                    {:measurement-time { :timestamp (t/date-time 2017 3 16) } :ascension-declination "02 06 59.37 +13 04 14.1" :apparent-magnitude 1.39M :surface-brightness 4.29M :range 2.13250971657745M :range-rate 10.9150336M :sun-observer-target-angle 38.7173M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 24.6390M}
                    {:measurement-time { :timestamp (t/date-time 2017 3 17) } :ascension-declination "02 09 44.97 +13 19 24.2" :apparent-magnitude 1.39M :surface-brightness 4.29M :range 2.13880495546021M :range-rate 10.8841134M :sun-observer-target-angle 38.4395M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 24.4646M}
                    {:measurement-time { :timestamp (t/date-time 2017 3 18) } :ascension-declination "02 12 30.75 +13 34 26.5" :apparent-magnitude 1.40M :surface-brightness 4.29M :range 2.14508210190519M :range-rate 10.8523769M :sun-observer-target-angle 38.1616M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 24.2899M}
                    {:measurement-time { :timestamp (t/date-time 2017 3 19) } :ascension-declination "02 15 16.72 +13 49 20.9" :apparent-magnitude 1.40M :surface-brightness 4.29M :range 2.15134066476118M :range-rate 10.8197573M :sun-observer-target-angle 37.8834M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 24.1149M}
                    {:measurement-time { :timestamp (t/date-time 2017 3 20) } :ascension-declination "02 18 02.89 +14 04 07.4" :apparent-magnitude 1.41M :surface-brightness 4.29M :range 2.15758011632925M :range-rate 10.7861941M :sun-observer-target-angle 37.6051M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 23.9397M}
                    {:measurement-time { :timestamp (t/date-time 2017 3 21) } :ascension-declination "02 20 49.26 +14 18 45.8" :apparent-magnitude 1.41M :surface-brightness 4.29M :range 2.16379989488025M :range-rate 10.7516294M :sun-observer-target-angle 37.3265M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 23.7642M}
                    {:measurement-time { :timestamp (t/date-time 2017 3 22) } :ascension-declination "02 23 35.83 +14 33 16.0" :apparent-magnitude 1.42M :surface-brightness 4.29M :range 2.16999940528009M :range-rate 10.7160051M :sun-observer-target-angle 37.0478M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 23.5885M}
                    {:measurement-time { :timestamp (t/date-time 2017 3 23) } :ascension-declination "02 26 22.60 +14 47 37.9" :apparent-magnitude 1.43M :surface-brightness 4.29M :range 2.17617801842483M :range-rate 10.6792616M :sun-observer-target-angle 36.7688M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 23.4126M}
                    {:measurement-time { :timestamp (t/date-time 2017 3 24) } :ascension-declination "02 29 09.59 +15 01 51.3" :apparent-magnitude 1.43M :surface-brightness 4.29M :range 2.18233507080696M :range-rate 10.6413401M :sun-observer-target-angle 36.4897M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 23.2364M}
                    {:measurement-time { :timestamp (t/date-time 2017 3 25) } :ascension-declination "02 31 56.79 +15 15 56.2" :apparent-magnitude 1.44M :surface-brightness 4.29M :range 2.18846986617632M :range-rate 10.6021873M :sun-observer-target-angle 36.2104M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 23.0599M}
                    {:measurement-time { :timestamp (t/date-time 2017 3 26) } :ascension-declination "02 34 44.21 +15 29 52.5" :apparent-magnitude 1.44M :surface-brightness 4.29M :range 2.19458168176036M :range-rate 10.5617667M :sun-observer-target-angle 35.9310M :sun-observer-target-angle-direction "/T" :sun-target-observer-angle 22.8832M}}}})


