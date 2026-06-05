(ns jon-nested-menu.react
  "React-facing wrappers for npm consumers.

  Reagent components return hiccup, not React elements, so plain React apps
  can't use them directly. Each wrapper converts JS props to ClojureScript
  and calls `r/as-element` to produce a real React element.

  CLJS/Reagent users: require `jon-nested-menu.nested-menu` instead."
  (:require [clojure.string :as str]
            [goog.object :as gobj]
            [reagent.core :as r]
            [jon-nested-menu.variants.js-mui :as impl]))

(defn- react-element? [x]
  (and (object? x) (some? (gobj/get x "$$typeof"))))

(defn- camel->kebab [s]
  (-> s
      (str/replace #"([a-z0-9])([A-Z])" "$1-$2")
      str/lower-case))

(defn- js->props
  "Recursively convert a JS prop value to ClojureScript: plain objects become
  maps with kebab-cased keyword keys, arrays become vectors. React elements
  and functions are passed through untouched so icons, custom labels and
  callbacks keep working."
  [x]
  (cond
    (nil? x) x
    (array? x) (mapv js->props x)
    (react-element? x) x
    (object? x) (let [m (transient {})]
                  (gobj/forEach
                   x (fn [v k _]
                       (assoc! m (keyword (camel->kebab k)) (js->props v))))
                  (persistent! m))
    :else x))

(defn- prep
  "Convert JS props to a CLJS opts map and coerce string `:direction` to a
  keyword (`\"left\"` -> `:left`)."
  [js-props]
  (let [m (js->props js-props)]
    (cond-> m
      (string? (:direction m)) (update :direction keyword))))

(defn ^:export NestedMenu [js-props]
  (r/as-element [impl/nested-menu (prep js-props)]))

(defn ^:export ContextMenu [js-props]
  (let [m (prep js-props)
        children (:children m)
        children (cond
                   (nil? children) nil
                   (sequential? children) children
                   :else [children])]
    (r/as-element
     (into [impl/context-menu (dissoc m :children)] children))))

(defn ^:export NestedMenuItem [js-props]
  (r/as-element [impl/nested-menu-item (prep js-props)]))

(defn ^:export IconMenuItem [js-props]
  (r/as-element [impl/icon-menu-item (prep js-props)]))

(defn ^:export ChevronRight [js-props]
  (r/as-element [impl/chevron-right (js->props js-props)]))

(defn ^:export ChevronDown [js-props]
  ;; React passes `expanded`; the Reagent component expects `:expanded?`.
  (let [m (js->props js-props)]
    (r/as-element
     [impl/chevron-down (cond-> m
                          (contains? m :expanded)
                          (assoc :expanded? (:expanded m)))])))
