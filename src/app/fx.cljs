(ns app.fx
  (:require
    [clojure.edn :as edn]
    [ajax.core :as ajax]
    [re-frame.core :as rf]))

(rf/reg-cofx :time/now
  (fn [cofx]
    (assoc cofx :time/now (js/Date.now))))

(rf/reg-cofx :store/todos
  (fn [cofx store-key]
    (let [todos (edn/read-string (js/localStorage.getItem store-key))]
      (assoc cofx :store/todos todos))))

(defn store-todos [store-key]
  (rf/->interceptor
    :id :store/set-todos
    :after (fn [context]
             (js/localStorage.setItem store-key (-> context :effects :db :todos str)))))


(rf/reg-event-db 
 :app/assoc-org-agenda-files
 (fn [db [_ response]]
   (-> db
       (assoc-in [:org-agenda-files-loaded] true)
       (assoc-in [:org-agenda-files] (js->clj response))
       ))
)

(rf/reg-event-fx                             ;; note the trailing -fx
  :app/load-agenda-files                        ;; usage:  (dispatch [:handler-with-http])
  (fn [{:keys [db]} _]                    ;; the first param will be "world"
    {:db (assoc db :org-agenda-files-loaded false)   ;; causes the twirly-waiting-dialog to show??
     :http-xhrio {:method          :get
                  :uri             "org-agenda-files/"
                  :response-format (ajax/json-response-format {:keywords? true})  ;; IMPORTANT!: You must provide this.
                  :on-success      [:app/assoc-org-agenda-files]
                  :on-failure      [::load-personal-data-failure]}}))
