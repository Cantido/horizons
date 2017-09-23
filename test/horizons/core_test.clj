(ns horizons.core-test
  (:require [clojure.test :refer :all]
            [horizons.core :as core]
            [instaparse.core :as insta]
            [horizons.async-utils :as asu]
            [clojure.core.async :as async]
            [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [horizons.main :as main]
            [horizons.parsing.parser :as parser]))

(def full-geo-text
  (slurp (io/file (io/resource "full-geophysical-interaction.txt"))))

(def full-ephem-text
  (slurp (io/file (io/resource "full-ephem-interaction.txt"))))


(defn system []
  (component/start-system
    (component/system-map
      :web-server {}
      :horizons-client (core/horizons-client)
      :telnet-client {}
      :connection-factory {}
      :connection-pool {}
      :parser (parser/new-parser (io/resource "horizons.bnf")))))

(deftest get-planetary-body
  (is (nil? (core/get-planetary-body
              (:horizons-client (system))
              [asu/closed-chan asu/closed-chan]
              199)))
  (is (some? (core/get-planetary-body
               (:horizons-client (system))
               [(async/chan) (async/to-chan full-geo-text)]
               199))))

(deftest get-ephem
  (testing "with two closed channels"
    (is (nil? (core/get-ephemeris
                (:horizons-client (system))
                [asu/closed-chan asu/closed-chan]
                199))))
  (testing "with an example of a full ephemeris text"
    (is (some? (core/get-ephemeris
                 (:horizons-client (system))
                 [(async/chan) (async/to-chan full-ephem-text)]
                 199)))))
