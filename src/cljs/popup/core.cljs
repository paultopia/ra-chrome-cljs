(ns popup.core
  (:require [reagent.core :as r]
            [reagent.cookies :as coo]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [clojure.string :refer[upper-case]]
            [ajax.core :refer [GET POST]]))

(enable-console-print!)

(println "Hello, World!")



(def doc-state (r/atom
  {:coder "unanswered", :detected-case "unanswered", :input-case "unanswered",
    :mass-tort "unanswered", :cross-border "unanswered", :submitted? false}))

(defn update-doc [k v]
  (swap! doc-state assoc k v))

(defn incomplete-answers? [answermap]
  (if (some #{"unanswered"} (vals answermap)) true false))

(defn flag-answer [answer]
  (if (= "unanswered" answer)
    [:b (upper-case answer)]
    answer))

(defn scold [answermap]
  (if (incomplete-answers? answermap)
    [:i "All questions must be answered, and you must be logged in, to submit."[:br] "You can change answers just by pressing a different button/entering different data."]
    (if (:submitted? @doc-state)
     [:b "You can close the window now."]
     [:i "Please " [:span {:style {"color" "red"}} [:b "make sure your answers are correct "]] "before submitting." [:br] "You can change answers just by pressing a different button/entering different data."])))

(defn submit-coding [answermap]
  (POST "/submit" {:params @doc-state
                   :handler #(do
                              (update-doc :submitted? true)
                              (.log js/console (str "response: " %)))
                   :error-handler #(.log js/console (str "error: " %))}))

(defn yes-button [s]
  (if (= s "yes")
    :button.btn.btn-danger
    :button.btn.btn-default
    ))

(defn no-button [s]
  (if (= s "no")
    :button.btn.btn-danger
    :button.btn.btn-default
    ))

(defn coding-page []
  [:div.container
    [:div.row
      [:div.col-md-12
        [:p "What is the case number? "
         [:input {:on-change #(update-doc :input-case (-> % .-target .-value))}]] ]]
    [:div.row
      [:div.col-md-4
       [:b "Is this a MASS TORT?" [:br]]
       [:ButtonToolbar {:field :multi-select}
        [(yes-button (:mass-tort @doc-state)) {:on-click #(update-doc :mass-tort "yes")} [:b "YES"]] " "
        [(no-button (:mass-tort @doc-state)) {:on-click #(update-doc :mass-tort "no")} [:b "NO"]]]]
      [:div.col-md-5
       [:b "Is this a CROSS-BORDER case?" [:br]]
       [:ButtonToolbar {:field :multi-select}
       [(yes-button (:cross-border @doc-state)) {:on-click #(update-doc :cross-border "yes")} [:b "YES"]] " "
       [(no-button (:cross-border @doc-state)) {:on-click #(update-doc :cross-border "no")} [:b "NO"]]
       ]]]
   [:div.row
    [:div.col-md-12
     [:hr]
     [:h5 "Answers so far:"]
     [:ul
     [:li "Coder ID: " (:coder @doc-state)]
     [:li "Auto-detected case number (UNIQUE ID): " (:detected-case @doc-state)]
     [:li "Human-suggested case number: " (flag-answer (:input-case @doc-state))]
     [:li "Is mass tort?: " (flag-answer (:mass-tort @doc-state))]
     [:li "Is cross-border?: " (flag-answer (:cross-border @doc-state))]]]]
   [:div.row
    [:div.col-md-12
     [:div.btn-group
     [:button.btn.btn-default {:disabled (incomplete-answers? @doc-state)
                               :on-click #(do
                                           (.log js/console (pr-str @doc-state))
                                           (submit-coding @doc-state))}
      [:b "SUBMIT"]]]
     [:p (scold @doc-state)]]]])

(r/render [coding-page] (.getElementById js/document "app"))