(ns horizons.core-test
  (:require [clojure.test :refer :all]
            [horizons.core :as core]
            [instaparse.core :as insta]
            [horizons.async-utils :as asu]
            [horizons.test-utils :as test]
            [clojure.core.async :as async]
            [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [horizons.main :as main]
            [horizons.parsing.parser :as parser])
  (:import (java.io IOException)))

(defn component
  ([] (:horizons-client (test/build-test-system)))
  ([s] (:horizons-client (test/build-test-system s))))

(deftest get-planetary-body
  (testing "get geophysical data"
    (testing "with input and output channels both closed"
      (is (thrown? IOException (core/get-planetary-body (component) 199))))
    (testing "with a full example geophysical Telnet interaction"
      (is (some? (core/get-planetary-body (component "full-geophysical-interaction.txt") 199))))))

(deftest get-ephem
  (testing "with input and output channels both closed"
    (is (nil? (core/get-ephemeris (component) 199))))
  (testing "with an example of a full ephemeris text"
    (testing "under default options"
      (is (some? (core/get-ephemeris (component "full-ephem-interaction.txt") 199))))
    (testing "under a user-specified time-frame"
      (let [result (core/get-ephemeris
                     (component "full-ephem-with-dates.txt")
                     199
                     {:start "1990-10-27T22:17"
                      :end "1990-10-28T22:17"
                      :step-size "1h"})]
        (is (map? result))
        (is (= #{::core/time-frame ::core/ephemeredes} (set (keys result))))
        (let [ephemeredes (::core/ephemeredes result)]
          (is (set? ephemeredes))
          (is (= 25 (count ephemeredes))))))))
