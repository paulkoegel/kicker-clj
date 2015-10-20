(ns kicker-clj.handler
  (:require [compojure.core :refer [GET defroutes]]
            [compojure.route :refer [not-found resources]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [selmer.parser :refer [render-file]]
            [prone.middleware :refer [wrap-exceptions]]
            [environ.core :refer [env]]
            [datomic.api :as d]))

(d/create-database "datomic:mem://db")

(defonce con (d/connect "datomic:mem://db"))

(d/transact
  con
  [{:db/id (d/tempid :db.part/db)
    :db/ident :person/name
    :db/cardinality :db.cardinality/one
    :db/valueType :db.type/string
    :db.install/_attribute :db.part/db}])

(d/transact
  con
  [{:db/id (d/tempid :db.part/user)
    :person/name "Jim"}
   {:db/id (d/tempid :db.part/user)
    :person/name "Sarah"}])

(d/q
  '[:find ?e
    :where [?e :person/name "Jim"]]
  (d/db con))

(d/q
  '[:find ?name
    :where [_ :person/name ?name]]
  (d/db con))

(defn all-persons [db]
  (sort
    (map
      first
      (d/q
        '[:find ?name
          :where [_ :person/name ?name]]
        db)))

  )

(defroutes routes
           (GET "/" [] (render-file "templates/index.html" {:dev (env :dev?)}))
           (GET "/hello" [] "world 1")
           (GET "/persons" [] {:body    (pr-str (all-persons (d/db con)))
                               :headers {"Content-Type" "application/edn"}}
                              )
           (resources "/")
           (not-found "Not Found") )


(def app
  (let [handler (wrap-defaults routes site-defaults)]
    (if (env :dev?) (wrap-exceptions handler) handler)))
