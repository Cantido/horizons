(ns horizons.integration-test
  (:require [clojure.test :refer :all]
            [horizons.core :refer :all]
            [instaparse.core :as insta]
            [horizons.web :refer :all]
            [ring.mock.request :as mock]
            [com.stuartsierra.component :as component]
            [horizons.main :as main]
            [horizons.web :as web]
            [horizons.telnet.client :as telnet]
            [horizons.telnet.connect :as connect]
            [horizons.core :as core]
            [horizons.telnet.pool :as pool]
            [clojure.java.io :as io]))
;
;(deftest get-planetary-body-integration-test
;  (testing "Getting Mercury (ID 199)"
;    (is (not (insta/failure? (get-planetary-body 199)))))
;  (testing "Getting Earth (ID 499)"
;    (is (not (insta/failure? (get-planetary-body 399)))))
;  (testing "Getting Mars (ID 599)"
;    (is (not (insta/failure? (get-planetary-body 499))))))
;
;(deftest get-ephemeris-integration-test
;  (testing "Getting Mercury (ID 199) with default parameters"
;    (let [response (get-ephemeris 199)]
;      (is (not (insta/failure? response)))
;      (is (not (nil? response)))
;      (is (not (nil? (get-in response [:horizons.core/time-frame])))))))
;
;(deftest mars-test
;  (let [response (app (mock/request :get "/bodies/499"))]
;    (is (= (:status response) 200))
;    (is (not (empty? (:body response))))))
;
;(deftest mars-ephemeredes-test
;  (let [response (app (mock/request :get "/bodies/499/ephemeris"))]
;    (is (= (:status response) 200))
;    (is (not (empty? (:body response))))))

(deftest valid-system
  (let [system (main/horizons-system {:http-listen-port      3000
                                      :telnet-host           "host"
                                      :telnet-port           1
                                      :grammar-specification (io/resource "horizons.bnf")})]
    (is (some? system))
    (is (some? (component/start-system system)))
    (is (some? (component/stop-system system)))))
