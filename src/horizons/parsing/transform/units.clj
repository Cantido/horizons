(ns horizons.parsing.transform.units
  "Parses and transforms units of measurement")

(defn- unit-code
  ([s] (constantly {:unit-code s}))
  ([s t] (constantly {:unit-code s :unit-text t})))

(def transform-rules
  {:unit-23 (unit-code "23" "g/cm³")
   :unit-2A (unit-code "2A" "rad/s")
   :unit-A62 (unit-code "A62" "erg/g·s")
   :unit-BAR (unit-code "BAR" "bar")
   :unit-D54 (unit-code "D54" "W/m²")
   :unit-D61 (unit-code "D61" "'")
   :unit-D62 (unit-code "D62" "\"")
   :unit-DD (unit-code "DD" "°") ;
   :unit-H20 (unit-code "H20" "km³")
   :unit-KEL (unit-code "KEL" "K")
   :unit-KGM (unit-code "KGM" "kg")
   :unit-KMT (unit-code "KMT" "km")
   :unit-M62 (unit-code "M62" "km/s")
   :unit-MSK (unit-code "MSK" "m/s²")
   :unit-SEC (unit-code "SEC" "s")})
