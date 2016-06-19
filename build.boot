(set-env!
 :source-paths #{"src/cljs"}
 :resource-paths #{"html"}

 :dependencies '[[adzerk/boot-cljs "1.7.228-1"]
                 [cljs-ajax "0.5.5"]
                 [reagent-utils "0.1.8"]
                 [reagent "0.6.0-rc"]
                 [reagent-forms "0.5.24"]])

(require '[adzerk.boot-cljs :refer [cljs]])
