(set-env!
 :source-paths #{"src/cljs"}
 :resource-paths #{"html"}

 :dependencies '[[adzerk/boot-cljs "1.7.228-1"]
                 [pandeiro/boot-http "0.7.3"]
                 [adzerk/boot-reload "0.4.8"]
                 [org.clojure/clojurescript "1.9.76"]
                 [cljs-ajax "0.5.5"]
                 [reagent-utils "0.1.8"]
                 [reagent "0.6.0-rc"]
                 [reagent-forms "0.5.24"]])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[pandeiro.boot-http :refer [serve]]
         '[adzerk.boot-reload :refer [reload]])

(deftask dev
  "Fake a Figwheel"
  []
  (comp 
   (serve :dir "target")
   (watch)
   (reload)
   (cljs)
   (target :dir #{"target"})))

