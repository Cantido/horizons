(ns horizons.telnet.client-test
  (:require [clojure.test :refer :all]
            [horizons.telnet.client :as client]
            [clojure.core.async :as async]
            [clojure.java.io :as io]
            [clojure.string :as string]
            [horizons.async-utils :as asu]
            [horizons.test-utils :as test]))


(defn component
  ([] (:telnet-client (test/build-test-system)))
  ([s] (:telnet-client (test/build-test-system s)))
  ([to-telnet from-telnet] (:telnet-client (test/build-test-system to-telnet from-telnet))))

(deftest next-token-test
  (testing "returns a closed channel when given a closed or empty channel"
    (is (asu/closed? (client/next-token asu/closed-chan)))
    (is (asu/closed? (client/next-token (async/to-chan "")))))
  (is (= "word " (async/<!! (client/next-token (async/to-chan "word ")))))
  (let [token-chan (async/to-chan "first second third ")]
    (is (= ["first " "second " "third "] (repeatedly 3 #(async/<!! (client/next-token token-chan)))))))

(deftest next-block-test
  (testing "returns a closed channel when given a closed or empty channel"
    (is (asu/closed? (client/next-block asu/closed-chan)))
    (is (asu/closed? (client/next-block (async/to-chan "")))))
  (testing "returns everything up to the next block"
    (is (= "word Horizons> " (async/<!! (client/next-block (async/to-chan "word Horizons> word")))))))

(deftest wait-for-prompt-test
  (is (asu/closed? (client/wait-for-prompt asu/closed-chan)))
  (is (asu/closed? (client/wait-for-prompt (async/to-chan "Horizons>")))))

(deftest swallow-echo-test
  (testing "returns false if the channel was closed before it could swallow the entire string"
    (is (false? (client/swallow-echo "s" asu/closed-chan)))
    (is (false? (client/swallow-echo "12345" (async/to-chan "1\r\n")))))
  (testing "returns true if the entire echo could be consumed"
    (is (true? (client/swallow-echo "s" (async/to-chan "s\r\n"))))))

(deftest reset-client-test
  (is (false? (client/reset-client asu/closed-chan))))

(deftest connect-test
  (is (vector? (client/connect (component))))
  (is (vector? (client/connect (component "full-geophysical-interaction.txt")))))

(deftest transmit-test
  (testing "returns the next block"
    (is (= "Horizons> " (client/transmit
                               (component)
                               (asu/dropping-channel)
                               (async/to-chan "s\r\nHorizons> ") "s"))))
  (testing "returns false if either channels were closed"
      (is (false? (client/transmit
                    (component)
                    asu/closed-chan
                    asu/closed-chan
                    "s")))
      (is (false? (client/transmit
                    (component)
                    asu/closed-chan
                    (async/to-chan "s\r\nHorizons> ")
                    "s")))
      (is (false? (client/transmit
                    (component)
                    (asu/dropping-channel)
                    asu/closed-chan
                    "s")))))

(deftest get-body-test
  (let [result (client/get-body (component "full-geophysical-interaction.txt") 199)]
    (is (some? result))
    (is (string/includes? result "Mean radius (km)      =  2440(+-1)"))))

(deftest get-ephemeris-test
  (let [result (client/get-ephemeris-data (component "full-ephem-interaction.txt") 199)]
    (is (string/includes? result "X =-1.314107467485864E+00"))))

(deftest get-ephemeris-with-dates
  (let [to-telnet (async/chan 1000)
        from-telnet (-> "full-ephem-with-dates.txt" io/resource io/file slurp async/to-chan)
        result (client/get-ephemeris-data
                 (component to-telnet from-telnet)
                 199
                 {:start "1990-10-27T22:17"
                  :end "1990-10-28T22:17"
                  :step-size "1h"})]
    (is (string/includes? result "Start time      : A.D. 1990-Oct-27 22:17:00.0000 TDB"))
    (is (string/includes? result "Stop  time      : A.D. 1990-Oct-28 22:17:00.0000 TDB"))
    (is (string/includes? result "Step-size       : 60 minute"))
    (is (string/includes? result "X =-1.105406745766281E+00"))
    (let [test-channel (async/take 10 to-telnet)
          actually-sent (repeatedly 10 #(async/<!! test-channel))]
      (is (= 199 (nth actually-sent 0)))
      (is (= "E" (nth actually-sent 1)))
      (is (= "v" (nth actually-sent 2)))
      (is (= "" (nth actually-sent 3)))
      (is (= "eclip" (nth actually-sent 4)))
      (is (= "1990-10-27T22:17" (nth actually-sent 5)))
      (is (= "1990-10-28T22:17" (nth actually-sent 6)))
      (is (= "1h" (nth actually-sent 7)))
      (is (= "" (nth actually-sent 8)))
      (is (= "N" (nth actually-sent 9))))))
