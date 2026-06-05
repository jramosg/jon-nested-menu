(ns reagent-mui-nested-menu.core-test
  "Unit tests for the data-driven menu builder. These cover the pure logic
  (no DOM): how item maps become `nested-menu-item` / `icon-menu-item`
  elements, key assignment, selection highlighting and callback wiring."
  (:require [clojure.test :refer [deftest is testing]]
            [reagent-mui-nested-menu.core :as core]))

(defn- elements [opts]
  (vec (core/menu-items-from-data opts)))

(deftest leaves-vs-submenus
  (testing "items with children become nested-menu-item, leaves icon-menu-item"
    (let [els (elements {:items [{:label "Leaf"}
                                 {:label "Parent" :items [{:label "Child"}]}]
                         :parent-menu-open? true})]
      (is (= 2 (count els)))
      (is (= core/icon-menu-item (first (first els))))
      (is (= core/nested-menu-item (first (second els)))))))

(deftest stable-keys
  (testing "key comes from :uid, then :label, then index"
    (let [els (elements {:items [{:uid "u1" :label "A"}
                                 {:label "B"}
                                 {}]})]
      (is (= "u1" (:key (meta (nth els 0)))))
      (is (= "B" (:key (meta (nth els 1)))))
      (is (= 2 (:key (meta (nth els 2))))))))

(deftest submenu-propagates-context
  (testing "parent-menu-open?, close!, direction and selection flow down"
    (let [close! (fn [])
          el (first (elements {:items [{:label "P" :items [{:label "C"}]}]
                               :parent-menu-open? true
                               :close! close!
                               :direction :left
                               :selected-value :x}))
          props (second el)]
      (is (true? (:parent-menu-open? props)))
      (is (identical? close! (:close! props)))
      (is (= :left (:direction props)))
      (is (= :x (:selected-value props))))))

(deftest leaf-selection
  (testing "a leaf is :selected only when its :value matches selected-value"
    (let [els (elements {:items [{:label "A" :value :a}
                                 {:label "B" :value :b}]
                         :selected-value :b})]
      (is (false? (:selected (second (nth els 0)))))
      (is (true? (:selected (second (nth els 1))))))))

(deftest leaf-callback-and-close
  (testing "clicking a leaf calls :callback with [event item] then :close!"
    (let [calls (atom [])
          closed (atom 0)
          item {:label "Run" :callback (fn [e it] (swap! calls conj [e it]))}
          el (first (elements {:items [item]
                               :close! #(swap! closed inc)}))
          on-click (:on-click (second el))]
      (on-click :event)
      (is (= 1 (count @calls)))
      (is (= :event (ffirst @calls)))
      (is (= "Run" (:label (second (first @calls)))))
      (is (= 1 @closed) "close! runs after the callback"))))

(deftest empty-items
  (testing "no items yields an empty seq"
    (is (empty? (elements {:items []})))
    (is (empty? (elements {:items nil})))))
