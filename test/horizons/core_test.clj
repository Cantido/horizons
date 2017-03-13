(ns horizons.core-test
  (:require [clojure.test :refer :all]
            [horizons.core :refer :all]
            [instaparse.core :as insta]))

(deftest get-mercury-test
  (is (not (insta/failure? (get-planetary-body 199)))))

(deftest get-earth-test
  (is (not (insta/failure? (get-planetary-body 399)))))

(deftest get-mars-test
  (is (not (insta/failure? (get-planetary-body 499)))))
