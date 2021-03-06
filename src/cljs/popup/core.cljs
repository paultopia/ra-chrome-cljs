(ns popup.core
  (:require [reagent.core :as r]
            [reagent.cookies :as coo]
            [clojure.string :refer [upper-case]]
            [popup.ws :refer [start-router! send-message! response-handler errors-component]]
            [ajax.core :refer [GET POST]]))

(declare test-start-page) ; just a little forward declaration to get rid of an annoying warning

(declare test-ws-page)

(enable-console-print!)



(defn load [page]
  (r/render [page] (.getElementById js/document "app")))

(def doc-state
  (r/atom
   {:coder "unanswered", :detected-case "missing", :input-case "unanswered",
    :number-plaintiffs "unanswered", :foreign-govt-party "unanswered",
    :foreign-corp-party "unanswered", :filed-elsewhere "unanswered" :submitted? false}))

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
    [:span {:style {:font-size "smaller"}} "All questions must be answered, and you must be logged in, to submit. You can change answers just by pressing a different button/entering different data."]
    (if (:submitted? @doc-state)
      [:span {:style {:font-size "smaller"}} "You can close the window now."]
      [:span {:style {:font-size "smaller"}} "Please " [:span {:style {"color" "red"}} [:b "make sure your answers are correct "]] "before submitting. You can change answers just by pressing a different button/entering different data."])))

(defn test-submit [answermap]
  (do
    (update-doc :submitted? true)
    (coo/set! :coder (:coder @doc-state))))

(defn submit-coding [answermap]
  (POST "/submit" {:params @doc-state
                   :handler #(do
                               (update-doc :submitted? true)
                               (coo/set! :coder (:coder @doc-state))
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

(defn word-question [prompt valholder]
  [:p (str prompt " ")
   [:input {:placeholder (valholder @doc-state) :on-change #(update-doc valholder (-> % .-target .-value))}]])

(defn button-binary [prompt valholder]
  [:div
   [:hr]
   [:p prompt]
  [:ButtonToolbar {:field :multi-select}
   [(yes-button (valholder @doc-state)) {:on-click #(update-doc valholder "yes")} [:b "YES"]] " "
   [(no-button (valholder @doc-state)) {:on-click #(update-doc valholder "no")} [:b "NO"]]]])

(defn submission-form []
  [:div.row
   [:div.col-md-6
    [:p (scold @doc-state)]]
   [:div.col-md-1
    [:div.btn-group
     [:button.btn.btn-default {:disabled (incomplete-answers? @doc-state)
                               :on-click #(do
                                            (.log js/console (pr-str @doc-state))
                                            (test-submit @doc-state))}  ; replace with submit-coding to get communication in.  this is just to test cookies.
      [:b "SUBMIT"]]]]])

(defn coding-page []
  [:div.container
   [:div.row
    [:div.col-md-12
     [word-question "Enter your last name." :coder]
     [word-question "What is the case number?" :input-case]
     [word-question "How many plaintiffs are there?" :number-plaintiffs]
     [button-binary "Are any of the parties foreign government(s)?" :foreign-govt-party]
     [button-binary "Are any of the parties foreign corporation(s)?" :foreign-corp-party]
     [button-binary "Was the case originally filed in a foreign court?" :filed-elsewhere]]]
   [submission-form]])



;; ;; (defn old-coding-page []
;;   ;; [:div.container
;;     ;; [:div.row
;;     ;;   [:div.col-md-12
;;     ;;     [:p "What is the case number? "
;;          [:input {:on-change #(update-doc :input-case (-> % .-target .-value))}]] ]]
;;     [:div.row
;;       [:div.col-md-4
;;        [:b "Is this a MASS TORT?" [:br]]
;;        [:ButtonToolbar {:field :multi-select}
;;         [(yes-button (:mass-tort @doc-state)) {:on-click #(update-doc :mass-tort "yes")} [:b "YES"]] " "
;;         [(no-button (:mass-tort @doc-state)) {:on-click #(update-doc :mass-tort "no")} [:b "NO"]]]]
;;       [:div.col-md-5
;;        [:b "Is this a CROSS-BORDER case?" [:br]]
;;        [:ButtonToolbar {:field :multi-select}
;;        [(yes-button (:cross-border @doc-state)) {:on-click #(update-doc :cross-border "yes")} [:b "YES"]] " "
;;        [(no-button (:cross-border @doc-state)) {:on-click #(update-doc :cross-border "no")} [:b "NO"]]
;;        ]]]
;;    [:div.row
;;     [:div.col-md-6
     ;; [:p (scold @doc-state)]
     ;; [:p (str @doc-state)
     ;;  [:button {:on-click #(load test-start-page)} "test"]]
     ;; ]
     ;; [:div.col-md-1
     ;; [:div.btn-group
     ;; [:button.btn.btn-default {:disabled (incomplete-answers? @doc-state)
     ;;                           :on-click #(do
     ;;                                       (.log js/console (pr-str @doc-state))
     ;;                                       (submit-coding @doc-state))}
     ;;  [:b "SUBMIT"]]]
     ;; ]]])


(defn test-start-page []
  [:div
   [:p "this is a test  Live reloading here! Now. Maybe."]
   [:button {:on-click #(load coding-page)} "code"]
   [:p "new button!" ]
   [:button {:on-click #(load test-ws-page)} "code"]])

(update-doc :coder (coo/get :coder "unanswered"))

(load test-start-page)


;; this stuff below will be refactored into separate ns once I verify it works.

(defn message-button [errors]
  [:div.container
   [:div.row
    [:div.col-md-12
     [errors-component errors :message]
     [:button.btn.btn-default
      {:on-click #(send-message! [:rawebsite/wstest 1] 8000)} "Send 1 to server"]]]])

(defn test-ws-page []
  (let [messages (r/atom nil)
        errors   (r/atom nil)
        fields   (r/atom nil)]
    (start-router! (response-handler messages errors))
    (fn []
      [:div.container
       [:div.row
        [:div.col-md-12
         (str @messages)]
        [:div.col-md-12
         [message-button errors]]]])))
