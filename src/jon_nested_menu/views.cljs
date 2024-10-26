(ns jon-nested-menu.views
  (:require [reagent-mui.material.menu :refer [menu]]
            [reagent-mui.material.menu-item :refer [menu-item]]
            [reagent.core :as r]
            [reagent-mui.icons.arrow-right :refer [arrow-right]]
            [reagent-mui.material.list-item-text :refer [list-item-text]]
            [reagent-mui.material.button :refer [button]]
            [reagent-mui.icons.arrow-drop-down :refer [arrow-drop-down]]))

(def direction->transform-origin-hor 
  {:right :left
   :left :right})
(defn- nested-menu-item [{:keys [label items parent-anchor-els parent-menu-open? callback parent-id
                                 value direction]}
                         selected-value]
  (r/with-let
    [anchor-el (r/atom nil)
     sub-menu-open? (r/atom false)]
    (let [open? (and parent-menu-open? @sub-menu-open?)
          close-menu (fn []
                       (doseq [parent-anchor-el (conj parent-anchor-els anchor-el)]
                         (reset! parent-anchor-el nil)))]
      [:div
       {:on-mouse-enter (fn [e]
                          (when (seq items)
                            (reset! sub-menu-open? true)
                            (reset! anchor-el (.-currentTarget e))))
        :on-mouse-leave (fn []
                          (reset! sub-menu-open? false))}
       [menu-item {:class "nested-menu-item"
                   :selected (and value (= selected-value value))
                   :on-click (fn []
                               (callback)
                               (close-menu))}
        [list-item-text {:primary label}]
        (when (seq items)
          [arrow-right])]
       [menu
        {:class "nested-menu"
         :anchor-el @anchor-el
         :open open?
         :anchor-origin {:vertical "top" :horizontal direction}
         :transform-origin {:vertical "top" :horizontal (direction->transform-origin-hor direction)}
         :on-close close-menu
         :auto-focus false
         :disable-auto-focus-item true}
        (doall
         (map-indexed
          (fn [i item]
            ^{:key (str parent-id "-" i)}
            [nested-menu-item
             (assoc item :parent-anchor-els (conj parent-anchor-els anchor-el)
                    :parent-menu-open? @sub-menu-open?
                    :parent-id (str parent-id "-" i)
                    :direction direction)
             selected-value])
          items))]])))

(defn- dropdown [{:keys [label] :as button-props} anchor-el]
  [button (merge {:id "select-tag-button"
                  :aria-haspopup true
                  :on-click #(reset! anchor-el (. % -target))
                  :on-close #(reset! anchor-el nil)
                  :color "primary"
                  :variant "outlined"
                  :end-icon (r/as-element [arrow-drop-down {:class (cond-> ["expand-group"]
                                                                     @anchor-el
                                                                     (conj "expanded"))}])}
                 (dissoc button-props :label))
   label])

(defn nested-menu [{:keys [items button-props direction]
                    :or {direction :right} selected-value :value}]
  (r/with-let
    [anchor-el (r/atom nil)]
    (let [open? (boolean @anchor-el)]
      [:<>
       [dropdown button-props anchor-el]
       [menu
        {:open (boolean @anchor-el)
         :anchor-el @anchor-el
         :on-close #(reset! anchor-el nil)}
        (doall
         (map-indexed
          (fn [i item]
            ^{:key (str "main-menu-" i)}
            [nested-menu-item
             (assoc item :parent-anchor-els [anchor-el]
                    :parent-menu-open? open?
                    :sub-menu-open? (r/atom false)
                    :parent-id (str "main-menu-" i)
                    :direction direction)
             selected-value])
          items))]])))

(comment
  [nested-menu {:items [{:label "Menu item 1"
                         :left-icon ""
                         :callback #(prn "clicked 1")}
                        {:label "Menu item 2"
                         :left-icon ""
                         :callback #(prn "clicked 2")
                         :items [{:label "Menu item 2.1"
                                  :left-icon ""
                                  :callback #(prn "clicked 3")
                                  :items [{:label "Menu item 2.1.1"
                                           :left-icon ""
                                           :callback #(prn "clicked 2.1.1")}]}
                                 {:label "Menu item 2.2"
                                  :left-icon ""
                                  :callback #(prn "clicked 2.2")}]}]}])
