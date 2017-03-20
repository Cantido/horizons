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
  {:hour-of-day 0
   :minute-of-hour 0
   :second-of-minute 0
   :millisecond-of-second 0})

(defn- date-and-time->datetime [date time]
  (apply t/date-time
     (flatten
       [((juxt t/year t/month t/day) date)
        (map (merge midnight time) [:hour-of-day
                                    :minute-of-hour
                                    :second-of-minute
                                    :millisecond-of-second])])))

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
  (let [years (get duration :years 0)
        months (get duration :months 0)
        days (get duration :days 0)
        hours (get duration :hours 0)
        minutes (get duration :minutes 0)
        seconds (get duration :seconds 0)
        milliseconds (get duration :milliseconds 0)]
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
   {:timestamp
    (date-and-time->datetime (last date) (:time time))})
  ([era date time time-zone]
   {:timestamp
    (date-and-time->datetime (last date) (:time time))}))
