(ns horizons.parsing.parser
  "Parses and transforms output from the HORIZONS telnet client."
  (:require [horizons.parsing.time :as t]
            [instaparse.core :as core]
            [instaparse.transform :as transform]
            [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [clojure.string :as string]))

(defn string->int [s]
  (Integer/parseInt s))

(defn sci-not-coll->bigdec [significand-coll mantissa-coll]
  (->> [significand-coll mantissa-coll]
    (map last)
    (interpose "E")
    (apply str)
    bigdec))

(defn sci-not->bigdec [significand mantissa]
  (when (not-any? nil? [significand mantissa])
    (.scaleByPowerOfTen (bigdec significand) mantissa)))

(defn value-with-exponent-map->bigdec
  ([label & more]
   (let [fields (into {} more)]
     (if (number? (:value fields))
       [label
        (-> fields
         (assoc :value (sci-not->bigdec (:value fields) (:exponent fields)))
         (dissoc :exponent)
         (merge (dissoc fields :exponent :value)))]
       [label fields]))))

(defn unit-code [s]
  (constantly [:unit-code s]))

(def transform-rules
  {:comma-separated-integer #(clojure.string/replace % "," "")
   :date (fn [& more] [:date (t/datemap->date (into {} more))])
   :ephemeredes (fn [& more] [:ephemeredes (into #{} more)])
   :ephemeris (fn [& more] (into {} more))
   :float bigdec
   :heat-flow-mass (partial value-with-exponent-map->bigdec :heat-flow-mass)
   :integer string->int
   :mass (partial value-with-exponent-map->bigdec :mass)
   :month (fn [s] [:month (t/month->int s)])
   :rotation-rate (partial value-with-exponent-map->bigdec :rotation-rate)
   :sci-not sci-not-coll->bigdec
   :time (fn [& more]  {:time (into {} more)})
   :timestamp t/timestamp-transformer
   :unit-23 (unit-code "23")
   :unit-2A (unit-code "2A")
   :unit-A62 (unit-code "A62")
   :unit-BAR (unit-code "BAR")
   :unit-D54 (unit-code "D54")
   :unit-D61 (unit-code "D61")
   :unit-D62 (unit-code "D62")
   :unit-DD (unit-code "DD")
   :unit-H20 (unit-code "H20")
   :unit-KEL (unit-code "KEL")
   :unit-KMT (unit-code "KMT")
   :unit-KGM (unit-code "KGM")
   :unit-MSK (unit-code "MSK")
   :unit-M62 (unit-code "M62")
   :unit-SEC (unit-code "SEC")
   :volume (partial value-with-exponent-map->bigdec :volume)
   :years t/years})

(defn do-if
  "Calls (fn x) if pred is true, returning the result. Otherwise, returns x untouched."
  [pred fn x]
  (if (pred x) (fn x) x))

(defn throw-parse-exception
  "Throw an exception documenting a parse exception"
  [failure]
  (throw (Exception. (str "Unable to parse HORIZONS response."
                          "\n\nGot the following failure: \n" (with-out-str (print failure))))))

(defn parse
  "Parse a string into a parse tree."
  [parser-component s]
  {:pre [(not (string/blank? s))
         (some? parser-component)
         (some? (:parser-fn parser-component))]}
  (let [parser-fn (:parser-fn parser-component)]
    (parser-fn s)))

(defn coll-of-colls?
  "Returns true if every element of coll except the first is a collection."
  [xs]
  (and
   (vector? xs)
   (every? (every-pred coll? (complement set?)) (rest xs))))

(defn put-keyword-in-ns
  "Stick a keyword into the horizons.core namespace."
  [x]
  (keyword "horizons.core" (name x)))

(defn tree-vec->map
  "Transforms a list into a nested map, where the first list element is the key,
  and the rest of the list is put into a map under that key."
  [xs]
  {(first xs) (into {} (rest xs))})

(defn parse-horizons-response
  "Parses and transforms a response from HORIZONS into a useful data structure."
  [parser-component s]
  {:pre [(some? parser-component)]
   :post [(not (empty? %))]}
  (->> s
    (parse parser-component)
    (do-if instaparse.core/failure? throw-parse-exception)
    (transform/transform transform-rules)
    (clojure.walk/postwalk #(do-if coll-of-colls? tree-vec->map %))
    (clojure.walk/postwalk #(do-if keyword? put-keyword-in-ns %))))


(defrecord Parser [grammar-specification parser-fn parser-opts]
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

(defn new-parser [grammar-specification]
  (map->Parser {:grammar-specification grammar-specification}))
