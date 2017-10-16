(ns horizons.parsing.time
  "Parses and transforms times and dates"
  (:require [clj-time.core :as t]
            [clj-time.format :as f]
            [clojure.data.json :as json])
  (:import (java.io PrintWriter)
           (org.joda.time DateTime Duration Period PeriodType Seconds ReadablePeriod)))

(def ^:private month-formatter (f/formatter "MMM"))

(defn month->int
  [s]
  (t/month (f/parse month-formatter s)))

(defn datemap->date
  [m]
  (apply t/date-time ((juxt :year :month :day) m)))

(extend-protocol t/DateTimeProtocol
  ReadablePeriod
  (plus- [this ^ReadablePeriod period] (.plus (.toPeriod this) period)))

(extend-protocol t/InTimeUnitProtocol
  org.joda.time.ReadableDuration
  (in-millis [this] (-> this .getMillis))
  (in-seconds [this] (-> this .toPeriod .getSeconds))
  (in-minutes [this] (-> this .toPeriod .getMinutes))
  (in-hours [this] (-> this .toPeriod .getHours))
  (in-days [this] (-> this .toPeriod .getDays))
  (in-weeks [this] (-> this .toPeriod .getWeeks))
  (in-months [this] (-> this .toPeriod .getMonths))
  (in-years [this] (-> this .toPeriod .getYears)))


(def ^:private midnight
  {:horizons.core/hour-of-day 0
   :horizons.core/minute-of-hour 0
   :horizons.core/second-of-minute 0
   :horizons.core/millisecond-of-second 0})

(def days-per {:years 365})

(def successors {:years :days
                 :days :hours
                 :hours :minutes
                 :minutes :seconds
                 :seconds :milliseconds
                 :milliseconds nil})

(def units
  {:years {:years 1
           :days 365
           :hours 8766
           :minutes 525960
           :seconds 31557600
           :milliseconds 31557600000}
   :days {:years 1/365
          :days 1
          :hours 24
          :minutes 1440
          :seconds 86400
          :milliseconds 86400000}
   :hours {:years 1/8766
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

(def joda-fns {:years t/years
               :days t/days
               :hours t/hours
               :minutes t/minutes
               :seconds t/seconds
               :milliseconds t/millis})

(defn convert [from to n]
  (* n (get-in units [from to])))

(def ms-per {:years (convert :years :milliseconds 1)
             :days (convert :days :milliseconds 1)
             :hours (convert :hours :milliseconds 1)
             :minutes (convert :minutes :milliseconds 1)
             :seconds (convert :seconds :milliseconds 1)
             :milliseconds 1})

(defn ms ^long [unit x]
  (* (get ms-per unit) x))

(defn frac "Returns the fractional part of x"
  [x] (rem x 1))


(defn period-of
  "clj-time's period can't take floats. This can."
  ^Period [type x]
  (let [to (type successors)
        joda-fn (type joda-fns)]
    (if (= :milliseconds type)
      (.toDuration (joda-fn (Math/round (double x))))
      (t/plus (.toPeriod(joda-fn (int x)))
              (period-of to (convert type to (frac x)))))))

(defn milliseconds ^Period [x] (period-of :milliseconds x))
(defn seconds ^Period [x] (period-of :seconds x))
(defn minutes ^Period [x] (period-of :minutes x))
(defn hours ^Period [x] (period-of :hours x))
(defn years ^Period [x] (period-of :years x))

(defn date-ms-reduce ^long [x k v]
  (+ x (ms k v)))

(defn duration-map->millis ^long [m]
  (reduce-kv date-ms-reduce 0 m))

(defn duration-map->period ^Period [m]
  (Period. (duration-map->millis m)))

(defn- date-and-time->datetime [date time]
  (apply t/date-time
     (flatten
       [((juxt t/year t/month t/day) date)
        (map (merge midnight time) [:horizons.core/hour-of-day
                                    :horizons.core/minute-of-hour
                                    :horizons.core/second-of-minute
                                    :horizons.core/millisecond-of-second])])))

(defn normalize-date-string [s]
  (f/unparse (f/formatters :date-time) (DateTime/parse s)))

(defn- write-datetime [^DateTime datetime ^PrintWriter out]
  (let [datestring (f/unparse (f/formatters :date-time) datetime)]
    (.print out (str \" datestring \"))))

(extend DateTime json/JSONWriter {:-write write-datetime})

(defn iso-format-duration
  [duration]
  (let [years (get duration :horizons.core/years 0)
        months (get duration :horizons.core/months 0)
        days (get duration :horizons.core/days 0)
        hours (get duration :horizons.core/hours 0)
        minutes (get duration :horizons.core/minutes 0)
        seconds (get duration :horizons.core/seconds 0)
        milliseconds (get duration :horizons.core/milliseconds 0)]
    (str "P"
         years "Y"
         months "M"
         days "D"
         "T"
         hours "H"
         minutes "M"
         (format "%d.%03dS" seconds milliseconds))))

(defn timestamp-transformer
  ([date time]
   {:horizons.core/timestamp
    (date-and-time->datetime (last date) (:horizons.core/time time))})
  ([era date time time-zone]
   {:horizons.core/timestamp
    (date-and-time->datetime (last date) (:horizons.core/time time))}))
