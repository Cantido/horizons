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
        (is (not (empty? (:body response)))))))
  (testing "get geophysical data"
    (testing "with default options"
      (let [web-app-component (component "full-geophysical-interaction.txt")
            app (web/app-handler web-app-component)
            response (app (mock/request :get "/bodies/499"))]
        (is (= (:status response) 200))
        (is (not (empty? (:body response))))))))
