(ns horizons.web-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :as async]
            [clojure.java.io :as io]
            [horizons.main :as main]
            [horizons.web :as web]
            [horizons.test-utils :as test]
            [com.stuartsierra.component :as component]
            [ring.mock.request :as mock]))

(defn component [s]
  (:web-app (test/build-test-system s)))

(deftest web-test
  (testing "get ephemeris"
    (testing "with default options"
      (let [web-app-component (component "full-ephem-interaction.txt")
            app (web/app-handler web-app-component)
            response (app (mock/request :get "/bodies/499/ephemeris"))]
        (is (= (:status response) 200))
        (is (not (empty? (:body response))))))
    (testing "with specified start, stop, and step time"
      (let [web-app-component (component "full-ephem-with-dates.txt")
            app (web/app-handler web-app-component)
            response (app (mock/request :get "/bodies/499/ephemeris" {:start "1990-10-27T22:17"
                                                                      :end "1990-10-28T22:17"
                                                                      :step-size "1h"}))]
        (is (= (:status response) 200))
        (is (not (empty? (:body response))))
        (is (= true (clojure.string/includes? (:body response) "timestamp\":\"1990-10-27")))
        (is (= true (clojure.string/includes? (:body response) "timestamp\":\"1990-10-28")))
        (is (= true (clojure.string/includes? (:body response) "duration\":{\"minutes\":60"))))))
  (testing "get geophysical data"
    (testing "with default options"
      (let [web-app-component (component "full-geophysical-interaction.txt")
            app (web/app-handler web-app-component)
            response (app (mock/request :get "/bodies/499"))]
        (is (= (:status response) 200))
        (is (not (empty? (:body response))))))))
