(ns horizons.parsing.time
  "Parses and transforms times and dates"
  (:require [clj-time.core :as t]
            [clj-time.format :as f]))

(def ^:private month-formatter (f/formatter "MMM"))

(defn month->int
  [s]
  (t/month (f/parse month-formatter s)))

(defn datemap->date
  [m]
  (apply t/date-time ((juxt :year :month :day) m)))

(def ^:private midnight
  {:horizons.core/hour-of-day 0
   :horizons.core/minute-of-hour 0
   :horizons.core/second-of-minute 0
   :horizons.core/millisecond-of-second 0})

(defn- date-and-time->datetime [date time]
  (apply t/date-time
     (flatten
       [((juxt t/year t/month t/day) date)
        (map (merge midnight time) [:horizons.core/hour-of-day
                                    :horizons.core/minute-of-hour
                                    :horizons.core/second-of-minute
                                    :horizons.core/millisecond-of-second])])))

(def iso-8601-date-time-formatter
  (f/formatters :date-time))

(def iso-8601-date-formatter
  (f/formatters :date))

(defn format-date
  [date]
  (f/unparse iso-8601-date-formatter date))

(defn format-date-time
  [date]
  (f/unparse iso-8601-date-time-formatter date))

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
