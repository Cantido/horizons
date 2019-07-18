(ns horizons.parsing.parser-test
  (:require
    [clj-time.core :as t]
    [clojure.java.io :as io]
    [clojure.test :refer :all]
    [instaparse.core :as insta]
    [horizons.core :as h]
    [horizons.parsing.parser :as parser]
    [instaparse.transform :as transform]
    [com.stuartsierra.component :as component]
    [horizons.parsing.parser-test-mercury :refer :all]
    [horizons.parsing.parser-test-jupiter :refer :all]
    [horizons.parsing.parser-test-mars-ephemeris :refer :all]
    [horizons.test-utils :as test])
  (:import (org.joda.time Years Duration)))

(defn component
  ([] (:parser (test/build-test-system)))
  ([s] (:parser (test/build-test-system s))))

(defn get-file [name]
  (slurp
    (io/file
      (io/resource name))))

(defn get-edn [name]
  (read-string (get-file name)))

(defn parse-with-rule [kw s]
  (-> "resources/horizons.bnf"
    (parser/new-parser #{})
    (assoc :parser-opts [:start kw])
    component/start
    (parser/parse s)))

(defn parse-file [name]
  (parser/parse (component) (get-file name)))

(defn success? [x]
  (not (insta/failure? x)))

(deftest geophysical-grammar-test
  (is (= (parse-file "mercury-geophysical.txt") (get-edn "mercury-geophysical-parsed.edn")))
  (is (= (parse-file "venus-geophysical.txt") (get-edn "venus-geophysical-parsed.edn")))
  (is (= (parse-file "earth-geophysical.txt") (get-edn "earth-geophysical-parsed.edn")))
  (is (= (parse-file "mars-geophysical-2012-09-28.txt") (get-edn "mars-geophysical-2012-09-28.edn")))
  (is (= (parse-file "jupiter-geophysical.txt") (get-edn "jupiter-geophysical-parsed.edn")))
  (is (= (parse-file "saturn-geophysical.txt") (get-edn "saturn-geophysical-parsed.edn")))
  (testing "uranus-geophysical.txt"
    (is (success? (parse-file "uranus-geophysical.txt"))))
  (testing "mars-geophysical-2016-06-21.txt"
    (is (success? (parse-file "mars-geophysical-2016-06-21.txt"))))
  (testing "neptune-geophysical.txt"
    (is (success? (parse-file "neptune-geophysical.txt"))))
  (is (= (parse-with-rule :physical-properties-header "GEOPHYSICAL PROPERTIES (revised Aug 15, 2018):") [:physical-properties-header [:date [:month "Aug"] [:day [:integer "15"]] [:year [:integer "2018"]]]])))

(deftest geophysical-values-test
  (testing "multiline solar-constant info is ignored, for now"
    (= (parse-with-rule :solar-constant
        "Perihelion  Aphelion    Mean
         Solar Constant (W/m^2)         717         493         589
         Maximum Planetary IR (W/m^2)   470         315         390
         Minimum Planetary IR (W/m^2)    30          30          30")
     nil)
    (= (parse-with-rule :solar-constant
        "Perihelion  Aphelion    Mean
          Solar Constant (W/m^2)         14462       6278        9126
          Maximum Planetary IR (W/m^2)   12700       5500        8000
          Minimum Planetary IR (W/m^2)   6           6           6")
     nil))
  (are [text result] (= (parse-with-rule (first result) text) result)
    "A_roche(ice)/Rp       =  2.76" [:a-roche-ice [:value [:float "2.76"]]]
    "Aroche(ice)/Rp        =  2.71" [:a-roche-ice [:value [:float "2.71"]]]
    "Atm. pressure    = 1.0 bar" [:atmospheric-pressure [:value [:float "1.0"]] [:unit-BAR]]
    "Atmos. pressure (bar) = < 5x10^-15" [:atmospheric-pressure [:unit-BAR] [:value [:sci-not [:significand [:integer "5"]] [:exponent [:integer "-15"]]]]]
    "Atmos          = 5.1   x 10^18 kg" [:atmospheric-mass [:value [:sci-not [:significand [:float "5.1"]] [:exponent [:integer "18"]]]] [:unit-KGM]]
    "Mass of atmosphere, kg= ~ 2.5 x 10^16" [:atmospheric-mass [:unit-KGM] [:value [:sci-not [:significand [:float "2.5"]] [:exponent [:integer "16"]]]]]
    "Core radius (km)      =  ~1700" [:core-radius [:unit-KMT] [:value "~1700"]]
    "Vol. mean radius (km) = 3389.92+-0.04" [:mean-radius [:unit-KMT] [:value [:float "3389.92"]]]
    "Mean radius (km)      = 3389.9(2+-4)" [:mean-radius [:unit-KMT] [:value [:float "3389.9"]]]
    "crust          = 2.6   x 10^22 kg" [:crust-mass [:value [:sci-not [:significand [:float "2.6"]] [:exponent [:integer "22"]]]] [:unit-KGM]]
    "Density, gm cm^-3        = 5.515" [:density [:unit-23] [:value [:float "5.515"]]]
    "Dipole tilt/offset     = 9.6deg/0.1Rp" [:dipole-tilt-offset [:value "9.6deg/0.1Rp"]]
    "Equ. gravity  ms^-2   =  3.71" [:equatorial-gravity [:unit-MSK] [:value [:float "3.71"]]]
    "Equatorial Radius, Re = 3394.0 km" [:equatorial-radius [:value [:float "3394.0"]] [:unit-KMT]]
    "Escape velocity (km/s)=  59.5" [:escape-velocity [:unit-M62] [:value [:float "59.5"]]]
    "Fig. offset (Rcf-Rcm) = 2.50+-0.07 km" [:fig-offset [:value [:float "2.50"]] [:unit-KMT]]
    "Flattening, f         =  1/154.409" [:flattening [:value "1/154.409"]]
    "Flattening               = 1/298.257223563" [:flattening [:value "1/298.257223563"]]
    "Fluid core rad   = 3480 km" [:fluid-core-radius [:value [:integer "3480"]] [:unit-KMT]]
    "go, m s^-2               = 9.82022" [:g-o [:unit-MSK] [:value [:float "9.82022"]]]
    "gp, m s^-2 (polar)       = 9.8321863685" [:g-polar [:unit-MSK] [:value [:float "9.8321863685"]]]
    "Geometric albedo      =    0.150" [:geometric-albedo [:value [:float "0.150"]]]
    "ge, m s^-2 (equatorial)  = 9.7803267715" [:equatorial-gravity [:unit-MSK] [:value [:float "9.7803267715"]]]
    "Sidereal orb. per., y =   0.61519726" [:mean-sidereal-orbital-period-years [:float "0.61519726"]]
    "Sidereal orb. per., d = 224.70079922" [:mean-sidereal-orbital-period-days [:float "224.70079922"]]
    "Surface emissivity    = 0.77+-0.06" [:surface-emissivity [:value [:float "0.77"]]]
    "Sid. rot. rate, rad/s =  0.0000708822" [:rotation-rate [:unit-2A] [:value [:float "0.0000708822"]]]
    "Sid. rot. rate (rad/s)= 0.00000124001" [:rotation-rate [:unit-2A] [:value [:float "0.00000124001"]]]
    "Vol. Mean Radius (km) =  2440+-1" [:mean-radius [:unit-KMT] [:value [:integer "2440"]]]))

(deftest ephemeredes-grammar-test
  (is (= (parse-file "mars-ephemeredes.txt") (get-edn "mars-ephemeredes-parsed.edn")))
  (testing "mercury-ephemeredes.txt"
    (is (success? (parse-file "mercury-ephemeredes.txt"))))
  (testing "jupiter-ephemeredes.txt"
    (is (success? (parse-file "jupiter-ephemeredes.txt")))))

(deftest bodies-grammar-test
  (is (= (parse-file "bodies.txt") (get-edn "bodies-parsed.edn"))))

(deftest duration-parsing
  (are [text tree] (= (parse-with-rule :duration text) tree)
    "1.0y" [:duration [:years [:float "1.0"]]]
    "1d" [:duration [:days [:integer "1"]]]
    "1h" [:duration [:hours [:integer "1"]]]
    "1m" [:duration [:minutes [:integer "1"]]]
    "3.4s" [:duration [:seconds [:float "3.4"]]]
    "1.234s" [:duration [:seconds [:float "1.234"]]]))

(deftest month-parsing
  (are [text tree] (= (parse-with-rule :month text) tree)
    "Jul" [:month "Jul"]
    "July" [:month "July"]))

(deftest full-transformation-test
  (is (= (parser/transform (get-edn "mercury-geophysical-parsed.edn")) mercury-map))
  (is (= (parser/transform (get-edn "jupiter-geophysical-parsed.edn")) jupiter-map))
  (is (= (parser/transform (get-edn "mars-ephemeredes-parsed.edn")) mars-map)))
