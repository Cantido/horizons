(ns horizons.parsing.tree-test
  (:require
    [clojure.test :refer :all]
    [horizons.core :as h]
    [horizons.parsing.tree :refer :all]))

(deftest tree->map-test
  (testing "walking date trees"
    (testing "with one level of nesting"
      (is (= (tree->map
               [:revision-date
                [:month "Jul"]
                [:day 31]
                [:year 2013]])
             {::h/revision-date
              {::h/month "Jul"
               ::h/day 31
               ::h/year 2013}})))
    (testing "with two levels of nesting"
      (is (= (tree->map
               [:file-header
                [:revision-date
                 [:month "Jul"]
                 [:day 31]
                 [:year 2013]]])
             {::h/file-header
              {::h/revision-date
               {::h/month "Jul"
                ::h/day 31
                ::h/year 2013}}})))
    (testing "with a level of nesting in the middle of a vector"
      (is (= (tree->map
               [:file-header
                [:revision-date
                 [:month "Jul"]
                 [:day 31]
                 [:year 2013]]
                [:body-name "Mars"]
                [:body-id 499]])
             {::h/file-header
              {::h/revision-date
               {::h/month "Jul"
                ::h/day 31
                ::h/year 2013}
               ::h/body-name "Mars"
               ::h/body-id 499}}))))
  (testing "walking geophysical data"
    (is (= (tree->map
             [:geophysical-data
              [:mean-radius [:value "3389.9(2+-4)"]]
              [:density "3.933(5+-4)"]
              [:mass "6.4185"]])
           {::h/geophysical-data
            {::h/mean-radius {::h/value "3389.9(2+-4)"}
             ::h/density "3.933(5+-4)"
             ::h/mass "6.4185"}}))))
