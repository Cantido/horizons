(ns horizons.parsing.units
  "Parses and transforms units of measurement")

(defn- unit-code
  ([s] (constantly {:unit-code s}))
  ([s t] (constantly {:unit-code s :unit-text t})))

(def transform-rules
  {:unit-23 (unit-code "23" "g/cm³")
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
   :unit-SEC (unit-code "SEC")})
