(ns horizons.parsing.transform.units
  "Parses and transforms units of measurement")

(defn- unit-code
  ([s] (constantly {:unit-code s}))
  ([s t] (constantly {:unit-code s :unit-text t})))

(def transform-rules
  {:unit-23 (unit-code "23" "g/cm³") ; already normalized
   :unit-2A (unit-code "2A" "rad/s") ; 10³ kg/m³, code: 28
   :unit-A62 (unit-code "A62" "erg/g·s") ; E-4 W/kg
   :unit-BAR (unit-code "BAR" "bar") ; 0⁵ Pa, code: PAL
   :unit-D54 (unit-code "D54" "W/m²") ; already normalized
   :unit-D61 (unit-code "D61" "'") ; 2,908 882 x 10⁻⁴ rad code: C81
   :unit-D62 (unit-code "D62" "\""); 4,848 137 x 10⁻⁶ rad, code: C81
   :unit-DD (unit-code "DD" "°") ; 1,745 329 x 10⁻² rad, code: C81
   :unit-H20 (unit-code "H20" "km³") ; 10⁹ m³, code: MTQ
   :unit-KEL (unit-code "KEL" "K") ; already normalized
   :unit-KGM (unit-code "KGM" "kg") ; already normalized
   :unit-KMT (unit-code "KMT" "km") ; 10³ m, code: MTR
   :unit-M62 (unit-code "M62" "km/s"); 10³ m/s, code: MTS
   :unit-MSK (unit-code "MSK" "m/s²") ; already normalized
   :unit-SEC (unit-code "SEC" "s")}) ; already normalized
