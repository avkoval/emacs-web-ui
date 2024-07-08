(ns app.handlers
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]
            [app.fx :as fx]
            [app.utils :as utils]))

(def load-todos (rf/inject-cofx :store/todos "uix-starter/todos"))
(def store-todos (fx/store-todos "uix-starter/todos"))

(rf/reg-event-fx :app/init-db
  [load-todos]
  (fn [{:store/keys [todos]} [_ default-db]]
    {:db (update default-db :todos into todos)}))

(rf/reg-event-fx :todo/add
  [(rf/inject-cofx :time/now) store-todos]
  (fn [{:keys [db]
        :time/keys [now]}
       [_ todo]]
    {:db (assoc-in db [:todos now] todo)}))

(rf/reg-event-db :todo/remove
  [store-todos]
  (fn [db [_ created-at]]
    (update db :todos dissoc created-at)))

(rf/reg-event-db :todo/set-text
  [store-todos]
  (fn [db [_ created-at text]]
    (assoc-in db [:todos created-at :text] text)))

(rf/reg-event-db :todo/toggle-status
  [store-todos]
  (fn [db [_ created-at]]
    (update-in db [:todos created-at :status] {:unresolved :resolved
                                               :resolved :unresolved})))

(rf/reg-event-db
 :app/assoc-webui-config
 (fn [db [_ response]]
   (let [data (js->clj response)]
     (js/console.log "1- " response)
     (js/console.log "2-" data)
     (-> db
         (assoc-in [:webui-config-loaded] true)
         (utils/deep-merge data)
         ))))

(rf/reg-event-fx
 :app/webui-config-call-failure
 (fn [db [_ response]]
   (js/console.log "webui-config-call-failure" response)
   (assoc db :webui-config-call-failure true)))


(rf/reg-event-fx                             ;; note the trailing -fx
  :app/webui-load-config                        ;; usage:  (dispatch [:handler-with-http])
  (fn [{:keys [db]} _]                    ;; the first param will be "world"
    {:db (assoc db :org-agenda-files-loaded false)   ;; causes the twirly-waiting-dialog to show??
     :http-xhrio {:method          :get
                  :uri             "webui-config/"
                  :response-format (ajax/json-response-format {:keywords? true})  ;; IMPORTANT!: You must provide this.
                  :on-success      [:app/assoc-webui-config]
                  :on-failure      [:app/webui-config-call-failure]}}))

