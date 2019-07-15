(ns horizons.parsing.transform
  "Transforms output from the HORIZONS telnet client."
  (:require [horizons.parsing.transform.time :as t]
            [horizons.parsing.transform.units :as units]
            [horizons.parsing.transform.numbers :as numbers]))

(def transform-rules
  (merge
    {:ephemeredes (fn [& more] [:ephemeredes (into #{} more)])
     :ephemeris (fn [& more] (into {} more))
     :heat-flow-mass (partial numbers/value-with-exponent-map->bigdec :heat-flow-mass)
     :mass (partial numbers/value-with-exponent-map->bigdec :mass)
     :rotation-rate (partial numbers/value-with-exponent-map->bigdec :rotation-rate)
     :volume (partial numbers/value-with-exponent-map->bigdec :volume)}
    numbers/transform-rules
    t/transform-rules
    units/transform-rules))
