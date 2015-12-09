(ns gitiom.reference
  (:import [org.eclipse.jgit.lib Constants RefUpdate RefUpdate$Result])
  (:refer-clojure :exclude [load])
  (:require [clojure.string :as str]
            [gitiom.coerce :refer [to-oid]]
            [gitiom.commit :as commit]
            [gitiom.repo :refer [object-type rev-walk]]
            [gitiom.tag :as tag]))

(defrecord Reference [name type tag head])

(defn to-reference [repo jref]
  (when jref
    (let [name       (.getName jref)
          oid        (.getObjectId jref)
          tag?       (re-matches #"refs/tags/.+" name)
          annotated? (when oid (= Constants/OBJ_TAG (object-type repo oid)))
          tag        (when annotated? (tag/load repo (.getObjectId jref)))
          head-oid   (if tag
                       (->> (to-oid repo (:sha1 tag))
                            (.parseTag (rev-walk repo))
                            (.getObject)
                            (.getId))
                       (.getObjectId (.getLeaf jref)))
          head       (when head-oid (commit/load repo head-oid))]
      (->Reference name (if tag? :tag :branch) tag head))))

(defn load [repo name]
  (some->> name
           (.getRef (.getRepository repo))
           (to-reference repo)))

(defn load-all [repo]
  (into []
        (map (fn [[_ ref]] (to-reference repo ref)))
        (.getAllRefs (.getRepository repo))))

(defn update! [repo ref commit]
  (let [update (.updateRef (.getRepository repo) (:name ref))]
    (.setNewObjectId update (to-oid repo (:sha1 commit)))
    (let [result (.update update)]
      (condp = result
        RefUpdate$Result/FAST_FORWARD            true
        RefUpdate$Result/FORCED                  true
        RefUpdate$Result/NEW                     true
        RefUpdate$Result/NO_CHANGE               true
        RefUpdate$Result/RENAMED                 true
        RefUpdate$Result/IO_FAILURE              false
        RefUpdate$Result/LOCK_FAILURE            false
        RefUpdate$Result/REJECTED                false
        RefUpdate$Result/REJECTED_CURRENT_BRANCH false
        :else                                    false))))
