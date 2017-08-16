(ns horizons.core-test
  (:require [clojure.test :refer :all]
            [horizons.core :refer :all]
            [instaparse.core :as insta]))

(deftest get-planetary-body-integration-test
  (testing "Getting Mercury (ID 199)"
    (is (not (insta/failure? (get-planetary-body 199)))))
  (testing "Getting Earth (ID 499)"
    (is (not (insta/failure? (get-planetary-body 399)))))
  (testing "Getting Mars (ID 599)"
    (is (not (insta/failure? (get-planetary-body 499))))))

(deftest get-ephemeris-integration-test
  (testing "Getting Mercury (ID 199) with default parameters"
    (let [response (get-ephemeris 199)]
      (is (not (insta/failure? response)))
      (is (not (nil? response)))
      (is (not (nil? (get-in response [:horizons.core/time-frame])))))))
