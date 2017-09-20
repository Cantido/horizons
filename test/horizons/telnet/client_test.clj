(ns horizons.telnet.client-test
  (:require [clojure.test :refer :all]
            [horizons.telnet.client :as client]
            [clojure.core.async :as async]
            [clojure.java.io :as io]
            [clojure.string :as string]))

(def closed-chan
  (let [chan (async/chan)]
    (async/close! chan)
    chan))

(defn closed? [chan]
  (nil? (async/<!! chan)))

(deftest next-token
  (is (closed? (client/next-token closed-chan)))
  (is (closed? (client/next-token (async/to-chan ""))))
  (is (= "word " (async/<!! (client/next-token (async/to-chan "word "))))))

(deftest next-block
  (is (closed? (client/next-block closed-chan))))

(deftest wait-for-prompt
  (is (closed? (client/wait-for-prompt closed-chan))))

(deftest swallow-echo
  (testing "returns false if the channel was closed before it could swallow the entire string"
    (is (false? (client/swallow-echo "s" closed-chan)))
    (is (false? (client/swallow-echo "12345" (async/to-chan "1\r\n")))))
  (testing "returns true if the entire echo could be consumed"
    (is (true? (client/swallow-echo "s" (async/to-chan "s\r\n"))))))

(deftest reset-client
  (is (false? (client/reset-client closed-chan))))

(deftest connect
  (is (vector? (client/connect [closed-chan closed-chan]))))

(deftest wait-for-prompt
  (is (closed? (client/wait-for-prompt closed-chan)))
  (is (closed? (client/wait-for-prompt (async/to-chan "Horizons>")))))

(deftest transmit
  (testing "returns the next block"
    (is (= "Horizons> " (client/transmit (async/chan) (async/to-chan "s\r\nHorizons> ") "s"))))
  (testing "returns false if either channels were closed"
      (is (false? (client/transmit closed-chan closed-chan "s")))
      (is (false? (client/transmit closed-chan (async/to-chan "s\r\nHorizons> ") "s")))
      (is (false? (client/transmit (async/chan) closed-chan "s")))))

(def full-geo-text
  (slurp (io/file (io/resource "full-geophysical-interaction.txt"))))

(def full-ephem-text
  (slurp (io/file (io/resource "full-ephem-interaction.txt"))))

(deftest get-body
  (is (not (string/blank? (client/get-body 199 [(async/chan) (async/to-chan full-geo-text)])))))

(deftest get-ephemeris
  (is (not (string/blank? (client/get-ephemeris-data 199 [(async/chan) (async/to-chan full-ephem-text)])))))
