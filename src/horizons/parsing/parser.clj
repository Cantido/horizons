(ns horizons.parsing.parser
  "Parses and transforms output from the HORIZONS telnet client."
  (:require [horizons.parsing.transform :as t]
            [horizons.parsing.tree :as tree]
            [instaparse.core :as core]
            [instaparse.transform :as it]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [clojure.string :as string]))


(defn- throw-parse-exception
  "Throw an exception documenting a parse exception"
  ([] (throw (Exception. (str "Unable to parse HORIZONS response."))))
  ([failure]
   (throw (Exception. (str "Unable to parse HORIZONS response."
                          "\n\nGot the following failure: \n" (with-out-str (print failure)))))))

(defn parse
  "Parse a string into a parse tree."
  [parser-component s]
  {:pre [(not (string/blank? s))
         (some? parser-component)
         (some? (:parser-fn parser-component))]}
  (let [parser-fn (:parser-fn parser-component)]
    (parser-fn s)))

(defn transform
  [tree]
  (->> tree
    (it/transform t/transform-rules)
    (tree/tree->map)))

(defn parse-horizons-response
  "Parses and transforms a response from HORIZONS into a useful data structure."
  [parser-component s]
  {:pre [(some? parser-component)]
   :post [(not (empty? %))]}
  (let [result (parse parser-component s)]
    (if (instaparse.core/failure? result)
      (throw-parse-exception (instaparse.core/get-failure result))
      (transform result))))

(defn supported?
  "Check if the given body ID is definitely supported by this parser."
  [component id]
  (let [{:keys [supported-bodies]} component]
    (->> id
         bigdec
         int
         (get supported-bodies))))

(defrecord Parser [grammar-specification supported-bodies parser-fn parser-opts]
  component/Lifecycle

  (start [this]
    {:pre [(some? grammar-specification)]
     :post [(some? parser-fn)]}
    ;; instaparse/parser tries to slurp the given URL,
    ;; so it's probably best to wait until startup call it.
    (log/trace "Using grammar spec" grammar-specification)
    (when parser-opts (log/trace "Using parser options:" parser-opts))
    (let [new-parser-fn (apply core/parser grammar-specification parser-opts)]
      (assoc this :parser-fn new-parser-fn)))

  (stop [this]
    this))

(defn new-parser [grammar-specification supported-bodies]
  (map->Parser {:grammar-specification grammar-specification
                :supported-bodies supported-bodies}))
