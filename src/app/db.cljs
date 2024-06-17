(ns app.db)

(def default-db
  {:todos (sorted-map-by >)
   :org-agenda-files-loaded false
   :org-agenda-files []
   }
  )
