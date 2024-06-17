(ns app.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub :app/todos
  (fn [db _]
    (:todos db)))

(rf/reg-sub :app/org-agenda-files-loaded
  (fn [db _]
    (:org-agenda-files-loaded db)))

(rf/reg-sub :app/org-agenda-files
  (fn [db _]
    (:org-agenda-files db)))
