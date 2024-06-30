(ns app.handlers
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]
            [app.fx :as fx]))

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
 :app/assoc-org-agenda-files
 (fn [db [_ response]]
   (js/console.log response)
   (-> db
       (assoc-in [:org-agenda-files-loaded] true)
       (assoc-in [:org-agenda-files] (js->clj response))
       ))
)

(rf/reg-event-fx
 :app/load-org-agenda-files-failure
 (fn [db [_ response]]
   (js/alert "failure")
   (assoc db :org-agenda-files-load-failure true))
)


(rf/reg-event-fx                             ;; note the trailing -fx
  :app/load-agenda-files                        ;; usage:  (dispatch [:handler-with-http])
  (fn [{:keys [db]} _]                    ;; the first param will be "world"
    {:db (assoc db :org-agenda-files-loaded false)   ;; causes the twirly-waiting-dialog to show??
     :http-xhrio {:method          :get
                  :uri             "org-agenda-files/"
                  :response-format (ajax/json-response-format {:keywords? true})  ;; IMPORTANT!: You must provide this.
                  :on-success      [:app/assoc-org-agenda-files]
                  :on-failure      [:app/load-org-agenda-files-failure]}}))
