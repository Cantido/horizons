(ns horizons.core-test
  (:require [clojure.test :refer :all]
            [horizons.core :refer :all]
            [ring.mock.request :as mock]))

(deftest index-test
  (is (= (:status (horizons.core/handler (mock/request :get "/")))
         200)))
