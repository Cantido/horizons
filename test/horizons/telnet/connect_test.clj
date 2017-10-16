(ns horizons.telnet.connect-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [horizons.telnet.connect :as connect]
            [clojure.core.async :as async])
  (:import (java.io StringWriter)))
;; @#'

(defn byte-reader [coll]
  (io/reader (byte-array coll)))

(defn abc-reader []
  (byte-reader [97 98 99]))

(defn empty-reader []
  (io/reader (byte-array 0)))

(deftest next-char-test
  (is (= "a" (@#'connect/next-char! (abc-reader))))
  (is (= nil (@#'connect/next-char! (empty-reader)))))

(deftest char-seq-test
  (is (= ["a" "b" "c"] (take 3 (@#'connect/char-seq! (abc-reader)))))
  (is (= [] (take 1 (@#'connect/char-seq! (empty-reader))))))


(defn run-reader-channel
  "Puts coll in a reader, and returns the coll that is put on a reader-channel.
  Coll must be able to form a byte array (i.e. contents must all be ints between 0-255)."
  [coll]
  (let [chan (async/chan 1024)
        reader-chan (@#'connect/reader-channel! (byte-reader coll) chan)]
    (take (count coll) (repeatedly #(async/<!! reader-chan)))))

(deftest reader-channel-test
    (is (= ["a" "b" "c"] (run-reader-channel [97 98 99]))))


(defn run-writer-channel
  "Puts coll on a writer channel, and returns the string that the channel writes."
  [coll]
  (let [writer (StringWriter.)
        chan (async/chan 1024)
        writer-chan (@#'connect/writer-channel! writer chan)]
    (async/onto-chan writer-chan coll)
    (Thread/sleep 100) ; need time for the thread to put it into the writer
    (.toString writer)))

(deftest writer-channel-test
  (is (= "a\n" (run-writer-channel ["a"])))
  (is (= "a\nb\n" (run-writer-channel ["a" "b"]))))
