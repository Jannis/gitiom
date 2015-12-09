#!/usr/bin/env boot

(set-env!
 :source-paths #{}
 :resource-paths #{"src"}
 :dependencies '[[adzerk/bootlaces "0.1.13"]
                 [clj-jgit "0.8.8"]])

(require '[adzerk.bootlaces :refer :all]
         '[boot.git :refer [last-commit]])

(def version "0.1.1")

(bootlaces! version)

(task-options!
 push {:repo "deploy"
       :ensure-branch "master"
       :ensure-clean true
       :ensure-tag (last-commit)
       :ensure-version version}
 pom {:project 'gitiom
      :version version
      :description "Idiomatic Git for Clojure"
      :url "https://github.com/jannis/gitiom"
      :scm {:url "https://github.com/jannis/gitiom"}
      :license {"GNU LGPL v2.1"
                "http://www.gnu.org/licenses/lgpl-2.1.en.html"}})

(deftask deploy
  []
  (comp (pom)
        (jar)
        (install)))
