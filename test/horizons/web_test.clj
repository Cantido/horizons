(ns horizons.web-test
  (:require [clojure.test :refer :all]
    [horizons.web :refer :all]
    [ring.mock.request :as mock]))

(deftest mars-test
  (let [response (app (mock/request :get "/bodies/499"))]
    (is (= (:status response) 200))
    (is (not (empty? (:body response))))))

(deftest mars-ephemeredes-test
  (let [response (app (mock/request :get "/bodies/499/ephemeris"))]
    (is (= (:status response) 200))
    (is (not (empty? (:body response))))))

