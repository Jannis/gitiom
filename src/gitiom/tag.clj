(ns gitiom.tag
  (:refer-clojure :exclude [load])
  (:require [gitiom.ident :as ident]
            [gitiom.repo :refer [rev-walk]]))

(defrecord Tag [sha1 tagger subject message])

(defn to-tag [repo jtag]
  (let [sha1    (.getName (.getId jtag))
        tagger  (ident/load (.getTaggerIdent jtag))
        subject (.getShortMessage jtag)
        message (.getFullMessage jtag)]
    (->Tag sha1 tagger subject message)))

(defn load [repo oid]
  (some->> (.parseTag (rev-walk repo) oid)
           (to-tag repo)))
