(require
 '[figwheel-sidecar.repl-api :as ra]
 '[com.stuartsierra.component :as component]
 '[ring.component.jetty :refer [jetty-server]]
 '[ring.middleware.json :refer [wrap-json-response wrap-json-params]]
 '[ring.middleware.params :refer [wrap-params]]
 '[ring.util.response :refer [response]]
 '[redgenes.routes :as routes])

(def figwheel-config
   {:figwheel-options {} ;; <-- figwheel server config goes here
    :build-ids ["dev"]   ;; <-- a vector of build ids to start autobuilding
    :all-builds          ;; <-- supply your build configs here
    [{:id           "dev"
      :source-paths ["src/cljs"]
      :figwheel     {:on-jsload "redgenes.core/mount-root"}
      :compiler     {:main                 "redgenes.core"
                     :output-to            "resources/public/js/compiled/app.js"
                     :output-dir           "resources/public/js/compiled/out"
                     :asset-path           "js/compiled/out"
                     :source-map-timestamp true
                     ;:foreign-libs [{:file "resources/public/vendor/im.min.js"
                     ;                :provides ["intermine.imjs"]}
                     ;               {:file "resources/public/vendor/imtables.js"
                     ;                :provides ["intermine.imtables"]}]
                     }}

     {:id           "min"
      :source-paths ["src/cljs"]
      :jar          true
      :compiler     {:main            "redgenes.core"
                     :output-to       "resources/public/js/compiled/app.js"
                     :externs         ["externs/imjs.js"
                                       "externs/imtables.js"]
                     :optimizations   :advanced
                     :closure-defines {"goog.DEBUG" false}
                     :pretty-print    false
                     ;:foreign-libs [{:file "resources/public/vendor/im.min.js"
                     ;                :provides ["intermine.imjs"]}
                     ;               {:file "resources/public/vendor/imtables.min.js"
                     ;                :provides ["intermine.imtables"]}]
                     }}
     {:id           "test"
      :source-paths ["src/cljs" "test/cljs"]
      :compiler     {:output-to     "resources/public/js/compiled/test.js"
                     :main          "redgenes.runner"
                     :optimizations :none}}
     ]})

(defrecord Figwheel []
  component/Lifecycle
  (start [config]
    (ra/start-figwheel! config)
    config)
  (stop [config]
    ;; you may want to restart other components but not Figwheel
    ;; consider commenting out this next line if that is the case
    (ra/stop-figwheel!)
    config))

(defn api [request]
  (println "request" request)
  (println "body" (:body request) )
;  (println "Test " (first (get (:params request) "paths")))
  (println "params " (:params request))
  (routes/routes request))

(def system
  (atom
   (component/system-map
    :app-server (jetty-server {:app {:handler (wrap-json-response (wrap-params api))}, :port 3000})
    :figwheel   (map->Figwheel figwheel-config))))

(defn start []
  (swap! system component/start))

(defn stop []
  (swap! system component/stop))

(defn reload []
  (stop)
  (start))

(defn repl []
  (ra/cljs-repl))
