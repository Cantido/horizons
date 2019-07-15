(ns horizons.parsing.numbers
  "Parses and transforms numeric values")

(defn- string->int [s]
  (Integer/parseInt s))

(defn- sci-not-coll->bigdec [significand-coll mantissa-coll]
  (->> [significand-coll mantissa-coll]
    (map last)
    (interpose "E")
    (apply str)
    bigdec))

(defn- sci-not->bigdec [significand mantissa]
  (when (not-any? nil? [significand mantissa])
    (.scaleByPowerOfTen (bigdec significand) mantissa)))

(defn value-with-exponent-map->bigdec
  ([label & more]
   (let [fields (into {} more)]
     (if (and (number? (:value fields)) (number? (:exponent fields)))
       [label
        (-> fields
         (assoc :value (sci-not->bigdec (:value fields) (:exponent fields)))
         (dissoc :exponent)
         (merge (dissoc fields :exponent :value)))]
       [label fields]))))

(def transform-rules
  {:comma-separated-integer #(clojure.string/replace % "," "")
   :float bigdec
   :integer string->int
   :sci-not sci-not-coll->bigdec})
