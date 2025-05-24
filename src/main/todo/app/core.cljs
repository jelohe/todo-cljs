(ns todo.app.core
  (:require [reagent.core :as r]
            [cljs.pprint :as pp]
            [clojure.string :refer [trim]]
            [reagent.dom :as rdom]))

;; --- STATE ---
(def initial-state {
                    1 {:id 1 :title "Do laundry" :done false}
                    3 {:id 3 :title "Buy groceries" :done false}
                    2 {:id 2 :title "Wash dishes" :done false}
                    })

(def initial-state-sorted (into (sorted-map) initial-state))

(defonce todos (r/atom initial-state-sorted))

(defonce counter (r/atom 3))

;; --- UTILITIES ---

(add-watch todos :todos
           (fn [key _atom _old-state new-state]
             (println "---" key "atom changed ---")
             (pp/pprint new-state)))

(add-watch counter :counter
           (fn [key _atom _old-state new-state]
             (println "---" key "atom changed ---")
             (pp/pprint new-state)))

(defn add-todo [text]
  (let [sanitazed-text (trim text)]
    (when-not (empty? sanitazed-text)
      (let [id (swap! counter inc)
            new-todo {:id id
                      :title sanitazed-text
                      :done false}]
        (swap! todos assoc id new-todo)))))

(defn save-todo [id title]
  (swap! todos assoc-in [id title] title))

(defn toggle-todo [id]
  (swap! todos update-in [id :done] not))

(defn delete-todo [id]
  (swap! todos dissoc id))

;; --- VIEWS ---
(defn todo-input [{:keys [title on-save on-stop]}]
  (let [input-text (r/atom "")
        update-text #(reset! input-text %)
        stop #(do (reset! input-text "")
                  (when on-stop (on-stop)))
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

(defn todo-item [_]
  (let [editing (r/atom false)]
    (fn [{:keys [id title done]}]
      [:li {:class (str (when done "completed ")
                        (when @editing "editing"))}
       [:div.view
        [:input {:class "toggle"
                 :type "checkbox"
                 :checked done
                 :on-change #(toggle-todo id)}]
        [:label title]
        [:button.destroy {:on-click #(delete-todo id)}]
        (when @editing
          [todo-input {:class "edit"
                       :title title
                       :on-save (fn [text] (save-todo id text))
                       :on-stop #(reset! editing false)}])]])))

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
    (when (seq @todos)
      [:div
       [task-list]
       [footer-controls]])]
   [:footer.info
    [:p "Double-click to edit a todo"]]])

;; --- RENDER ---
(defn render []
  (rdom/render [todo-app] (.getElementById js/document "root")))

(defn ^:export main []
  (render))

(defn ^:dev/after-load reload! []
  (render))
