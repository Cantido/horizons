(ns horizons.parsing.transform-test
  (:require
    [clojure.test :refer :all]
    [clojure.java.io :as io]
    [instaparse.transform :as it]
    [horizons.parsing.transform :refer :all])
  (:import (org.joda.time Years Duration)))

(defn get-file [name]
  (slurp
    (io/file
      (io/resource name))))

(defn get-edn [name]
  (read-string (get-file name)))

(defn transform [xs] (it/transform transform-rules xs))

(deftest transform-test
  (testing "measurement values"
    (testing "as a set (like ephemeredes)"
      (is (= (transform
               [:ephemeredes
                [:ephemeris [:x-position 1]]
                [:ephemeris [:x-position 2]]
                [:ephemeris [:x-position 3]]])
             [:ephemeredes
              #{
                {:x-position 1}
                {:x-position 2}
                {:x-position 3}}])))))
