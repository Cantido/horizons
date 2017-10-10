(ns horizons.telnet.new-client-test
  (:require [clojure.test :refer :all]
            [horizons.test-utils :as test]
            [horizons.telnet.new-client :as client]
            [clojure.string :as string]))

(defn component
  ([] (:telnet-client (test/build-test-system)))
  ([s] (:telnet-client (test/build-test-system s)))
  ([to-telnet from-telnet] (:telnet-client (test/build-test-system to-telnet from-telnet))))

(deftest get-body-test
  (let [result (client/get-body (component "full-geophysical-interaction.txt") 199)]
    (is (some? result))
    (is (string/includes? result "Mean radius (km)      =  2440(+-1)"))))
