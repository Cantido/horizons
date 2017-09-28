(ns horizons.parsing.time
  "Parses and transforms times and dates"
  (:require [clj-time.core :as t]
            [clj-time.format :as f]
            [clojure.data.json :as json])
  (:import (java.io PrintWriter)
           (org.joda.time DateTime Duration Period)))

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

(defn duration-map->period [m]
  (let [years-float (get m :years 0)
        years (int years-float)
        days-float (+ (get m :days 0) (* 356 (- years-float years)))
        days (int days-float)
        hours-float (+ (get m :hours 0) (* 24 (- days-float days)))
        hours (int hours-float)
        minutes-float (+ (get m :minutes 0) (* 60 (- hours-float hours)))
        minutes (int minutes-float)
        seconds-float (+ (get m :seconds 0) (* 60 (- minutes-float minutes)))
        seconds (int seconds-float)
        milliseconds (int (* 1000 (- seconds-float seconds)))]
    (Period.
      years
      (get m :months 0)
      0 ;; weeks
      days
      hours
      minutes
      seconds
      milliseconds)))

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
