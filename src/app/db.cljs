(ns app.db)

(def default-db
  {:todos (sorted-map-by >)
   :webui-config-loaded false
   :org-agenda-files []
   :org-agenda-commands {}
   }
  )
