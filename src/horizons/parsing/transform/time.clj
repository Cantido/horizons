(ns horizons.parsing.transform.time
  "Parses and transforms times and dates"
  (:require [clj-time.core :as t]
            [clj-time.format :as f]
            [clojure.data.json :as json])
  (:import (java.io PrintWriter)
           (org.joda.time DateTime Duration Period PeriodType Seconds ReadablePeriod)))

(def ^:private month-formatter (f/formatter "MMM"))

(defn- add
  ([x] x)
  ([x & more] (apply t/plus x more)))

(defn- month->int
  [s]
  (t/month (f/parse month-formatter s)))

(defn- datemap->date
  [m]
  (apply t/date-time ((juxt :year :month :day) m)))

(def ^:private midnight
  {:horizons.core/hour-of-day 0
   :horizons.core/minute-of-hour 0
   :horizons.core/second-of-minute 0
   :horizons.core/millisecond-of-second 0})

(def ^:private successors
  {:years :days
   :days :hours
   :hours :minutes
   :minutes :seconds
   :seconds :milliseconds
   :milliseconds nil})

(def ^:private units
  {:years {:years 1
           :weeks (/ 365 7)
           :days 365
           :hours 8766
           :minutes 525960
           :seconds 31557600
           :milliseconds 31557600000}
   :weeks {:days 7}
   :days {:years 1/365
          :weeks 1/7
          :days 1
          :hours 24
          :minutes 1440
          :seconds 86400
          :milliseconds 86400000}
   :hours {:years 1/8766
           :weeks 1/168
           :days 1/24
           :hours 1
           :minutes 60
           :seconds 3600
           :milliseconds 3600000}
   :minutes {:years 1/525960
             :days 1/1440
             :hours 1/60
             :minutes 1
             :seconds 60
             :milliseconds 60000}
   :seconds {:years 1/31557600
             :days 1/86400
             :hours 1/3600
             :minutes 1/60
             :seconds 1
             :milliseconds 1000}
   :milliseconds {:years 1/31557600000
                  :days 1/86400000
                  :hours 1/3600000
                  :minutes 1/60000
                  :seconds 1/1000
                  :milliseconds 1}})

(def ^:private period-fns
  {:years        t/years
   :days         t/days
   :hours        t/hours
   :minutes      t/minutes
   :seconds      t/seconds
   :milliseconds t/millis})

(defn- convert [from to n]
  (* n (get-in units [from to])))

(defn- frac "Returns the fractional part of x"
  [x] (rem x 1))

(defn- period-of
  "clj-time's period can't take floats. This can."
  ^Period [type x]
  (when-not (number? x) (throw (IllegalArgumentException. (str "Can't parse this value into a Period, it is not a number: " x))))
  (let [to (type successors)
        joda-fn (type period-fns)]
    (if (= :milliseconds type)
      (joda-fn (Math/round (double x)))
      (t/plus (joda-fn (int x))
              (period-of to (convert type to (frac x)))))))

(defn- date-and-time->datetime [date time]
  (apply t/date-time
     (flatten
       [((juxt t/year t/month t/day) date)
        (map (merge midnight time) [:horizons.core/hour-of-day
                                    :horizons.core/minute-of-hour
                                    :horizons.core/second-of-minute
                                    :horizons.core/millisecond-of-second])])))

(defn- write-datetime [^DateTime datetime ^PrintWriter out]
  (let [datestring (f/unparse (f/formatters :date-time) datetime)]
    (.print out (str \" datestring \"))))

(extend-protocol json/JSONWriter
  DateTime
  (-write [object out] (write-datetime object out))
  ReadablePeriod
  (-write [object out] (.print out (str \" object \"))))

(extend-protocol t/DateTimeProtocol
  ReadablePeriod
  (plus- [this ^ReadablePeriod period] (.plus (.toPeriod this) period)))

(defn- timestamp-transformer
  ([date time]
   {:horizons.core/timestamp
    (date-and-time->datetime (last date) (:horizons.core/time time))})
  ([era date time time-zone]
   {:horizons.core/timestamp
    (date-and-time->datetime (last date) (:horizons.core/time time))}))

(def transform-rules
  {:date (fn [& more] [:date (datemap->date (into {} more))])
   :days (partial period-of :days)
   :duration add
   :hours (partial period-of :hours)
   :milliseconds (partial period-of :milliseconds)
   :minutes (partial period-of :minutes)
   :month (fn [s] [:month (month->int s)])
   :seconds (partial period-of :seconds)
   :time (fn [& more]  {:time (into {} more)})
   :timestamp timestamp-transformer
   :years (partial period-of :years)})
