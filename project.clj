(defproject hearth-chronicle "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [org.postgresql/postgresql "42.7.5"]
                 [com.github.seancorfield/next.jdbc "1.3.994"]
                 [migratus "1.6.3"]
                 [ring/ring-core "1.13.0"]
                 [ring/ring-jetty-adapter "1.8.2"]
                 [ring/ring-json "0.5.1"]
                 [compojure "1.7.1"]
                 [com.layerware/hugsql "0.5.3"]
                 [com.github.seancorfield/honeysql "2.6.1270"]]
  :plugins [[migratus-lein "0.7.3"]]
  :migratus {:store :database
             :migration-dir "migrations"
             :db {:dbtype "postgresql"
                  :dbname "hearth_chronicle"
                  :host "localhost"
                  :port "5432"
                  :user "postgres"
                  :password "postgres"}}
  :main ^:skip-aot hearth-chronicle.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
