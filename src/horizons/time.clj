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

(defn- date-and-time->datetime
  [date time]
  (cond
    (nil? (:hour-of-day time))  (t/date-time
                                  (t/year date)
                                  (t/month date)
                                  (t/day date))
    (nil? (:minute-of-hour time)) (t/date-time
                                    (t/year date)
                                    (t/month date)
                                    (t/day date)
                                    (:hour-of-day time))
    (nil? (:second-of-minute time)) (t/date-time
                                      (t/year date)
                                      (t/month date)
                                      (t/day date)
                                      (:hour-of-day time)
                                      (:minute-of-hour time))
    (nil? (:millisecond-of-second  time)) (t/date-time
                                            (t/year date)
                                            (t/month date)
                                            (t/day date)
                                            (:hour-of-day time)
                                            (:minute-of-hour time)
                                            (:second-of-minute time))
    :else (t/date-time
            (t/year date)
            (t/month date)
            (t/day date)
            (:hour-of-day time)
            (:minute-of-hour time)
            (:second-of-minute time)
            (:millisecond-of-second time))))

(defn timestamp-transformer
  ([date time] {:timestamp (date-and-time->datetime (last date) (:time time))})
  ([era date time time-zone] {:timestamp (date-and-time->datetime (last date) (:time time))}))



