#!/usr/bin/env boot

(set-env!
 :source-paths #{}
 :resource-paths #{"src"}
 :dependencies '[[clj-jgit "0.8.8"]])

(task-options!
 pom {:project 'gitiom
      :version "0.1.0-SNAPSHOT"})

(deftask uberjar
  []
  (comp (pom)
        (uber)
        (jar)))

(deftask deploy
  []
  (comp (pom)
        (jar)
        (install)))
