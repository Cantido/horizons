(ns horizons.core-test
  (:require [clojure.test :refer :all]
            [horizons.core :as core]
            [instaparse.core :as insta]
            [horizons.async-utils :as asu]
            [clojure.core.async :as async]
            [clojure.java.io :as io]))

(def full-geo-text
  (slurp (io/file (io/resource "full-geophysical-interaction.txt"))))

(def full-ephem-text
  (slurp (io/file (io/resource "full-ephem-interaction.txt"))))


(deftest get-planetary-body
  (is (nil? (core/get-planetary-body 199 [asu/closed-chan asu/closed-chan])))
  (is (some? (core/get-planetary-body 199 [(async/chan) (async/to-chan full-geo-text)]))))

(deftest get-ephem
  (testing "with two closed channels"
    (is (nil? (core/get-ephemeris 199  [asu/closed-chan asu/closed-chan]))))
  (testing "with an example of a full ephemeris text"
    (is (some? (core/get-ephemeris 199  [(async/chan) (async/to-chan full-ephem-text)])))))
