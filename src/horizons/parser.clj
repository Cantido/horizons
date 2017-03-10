(ns horizons.parser
  (:require [instaparse.core :as insta]))

(defn tree->map
  [tree]
  (if (empty? tree)
      {}
      (let [root (apply hash-map tree)]
           {:S (apply hash-map (:S root))})))

(def parse
  (insta/parser (clojure.java.io/resource "horizons.bnf")))
