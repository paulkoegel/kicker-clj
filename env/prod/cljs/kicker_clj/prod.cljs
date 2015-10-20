(ns kicker-clj.prod
  (:require [kicker-clj.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
