(ns horizons.time
  "Parses and transforms times and dates"
  (:require
    [clj-time.core :as t]
    [clj-time.format :as f]))

(def ^:private month-formatter (f/formatter "MMM"))

(defn month->int
  [s]
  (t/month (f/parse month-formatter s)))

(defn datemap->date
  [m]
  (t/date-time (:year m) (:month m) (:day m)))

(defn- date-and-time->datetime [date time]
  (t/date-time
    (t/year date)
    (t/month date)
    (t/day date)
    (get time :hour-of-day 0)
    (get time :minute-of-hour 0)
    (get time :second-of-minute 0)
    (get time :millisecond-of-second 0)))

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
    (str "P" years "Y" months "M" days "DT" hours "H" minutes "M" (format "%d.%03dS" seconds milliseconds))))

(defn timestamp-transformer
  ([date time] {:timestamp (date-and-time->datetime (last date) (:time time))})
  ([era date time time-zone] {:timestamp (date-and-time->datetime (last date) (:time time))}))



