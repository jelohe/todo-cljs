(ns todo.app.core
  (:require [reagent.core :as r]
            [cljs.pprint :as pp]
            [reagent.dom :as rdom]))

;; --- STATE ---
(def initial-state {
            1 {:id 1, :title "Do laundry", :done false}
            3 {:id 3, :title "Buy groceries", :done false}
            2 {:id 2, :title "Wash dishes", :done false}
})

(def initial-state-sorted (into (sorted-map) initial-state))

(defonce todos (r/atom initial-state-sorted))

(defonce counter (r/atom 3))

(add-watch todos :todos
           (fn [key _atom _old-state new-state]
               (println "---" key "atom changed ---")
               (pp/pprint new-state)))

;; --- UTILITIES ---

(defn add-todo [text]
    (let [id (swap! counter inc)
          new-todo {:id id, :title text, :done false}]
        (swap! todos assoc id new-todo)))

;; --- VIEWS ---
(defn todo-input []
    (let [input-text (r/atom "")
          update-text #(reset! input-text %)
          stop #(reset! input-text "")
          save #(do
                (add-todo @input-text)
                (stop))
          key-pressed #(case %
                           "Enter" (save)
                           "Esc" (stop)
                           "Escape" (stop)
                           nil)]
        (fn []
            [:input {:class "new-todo"
                 :placeholder "Todo input"
                 :type "text"
                 :value @input-text
                 :on-blur save
                 :on-change #(update-text (.. % -target -value))
                 :on-key-down #(key-pressed (.. % -key))
             }])))

(defn todo-item [{:keys [title]}]
    [:li
     [:div.view
      [:label title]]])

(defn task-entry []
    [:header.header
     [:h1 "todos"]
     [todo-input]])

(defn task-list []
    (let [items (vals @todos)]
        [:section.main
         [:ul.todo-list
          (for [todo items]
              ^{:key (:id todo)} [todo-item todo])]]))

(defn footer-controls []
    [:footer.footer
     [:div "Footer controls"]])

(defn todo-app []
  [:div 
   [:section.todoapp
    [task-entry]
    [:div
     [task-list]
     [footer-controls]]]
   [:footer.info
    [:p "Footer info"]]])

;; --- RENDER ---
(defn render []
  (rdom/render [todo-app] (.getElementById js/document "root")))

(defn ^:export main []
  (render))

(defn ^:dev/after-load reload! []
  (render))
