(ns horizons.core-test
  (:require [clojure.test :refer :all]
            [horizons.core :refer :all]
            [ring.mock.request :as mock]))

(deftest mars-test
  (let [response (horizons.core/app (mock/request :get "/bodies/499"))]
    (is (= (:status response) 200)
        (not (empty? (:body response))))))
