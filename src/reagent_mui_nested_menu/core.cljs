(ns reagent-mui-nested-menu.core
  "Nested MUI menu components for Reagent.

  Public API: `nested-menu` (dropdown), `context-menu` (right-click),
  `nested-menu-item` / `icon-menu-item` (low-level), `menu-items-from-data`
  (build menus from data) and the `chevron-right` / `chevron-down` icons.

  Uses MUI subpath imports (e.g. @mui/material/Menu) instead of the barrel
  @mui/material, keeping it compatible across MUI v5–v9."
  (:require [reagent.core :as r]
            ["@mui/material/Button$default" :as MuiButton]
            ["@mui/material/Menu$default" :as MuiMenu]
            ["@mui/material/MenuItem$default" :as MuiMenuItem]
            ["@mui/material/Typography$default" :as MuiTypography]
            ["@mui/material/SvgIcon$default" :as MuiSvgIcon]))

;; Forward declarations for the mutually-recursive menu builders.
(declare nested-menu-item icon-menu-item menu-items-from-data)

;; ---------------------------------------------------------------------------
;; Icons
;; ---------------------------------------------------------------------------
;; Rendered with MUI `SvgIcon` so they inherit theme color and size.

(defn chevron-right
  "Right-pointing chevron used as the default sub-menu indicator."
  [props]
  [:> MuiSvgIcon (merge {:font-size "small"} props)
   [:path {:d (str "M9.29 6.71c-.39.39-.39 1.02 0 1.41L13.17 12l-3.88 "
                   "3.88c-.39.39-.39 1.02 0 1.41.39.39 1.02.39 1.41 "
                   "0l4.59-4.59c.39-.39.39-1.02 0-1.41L10.7 6.7c-.38-.38"
                   "-1.02-.38-1.41.01z")}]])

(defn chevron-down
  "Down-pointing chevron used as the dropdown button end-icon. Pass
  `:expanded?` true to rotate it 180°. Rotation is inline `style`, not `sx`,
  so it works on every MUI version. Use `.jnm-caret` / `.jnm-caret-expanded`
  for optional class-based overrides."
  [{:keys [expanded?] :as props}]
  [:> MuiSvgIcon
   (-> props
       (dissoc :expanded?)
       (update :class #(cond-> ["jnm-caret"]
                         expanded? (conj "jnm-caret-expanded")
                         (seq %) (conj %)))
       (update :style #(merge {:transition "transform 0.3s ease"
                               :transform (if expanded?
                                            "rotate(180deg)"
                                            "rotate(0deg)")}
                              %)))
   [:path {:d (str "M8.12 9.29 12 13.17l3.88-3.88c.39-.39 1.02-.39 1.41 "
                   "0 .39.39.39 1.02 0 1.41l-4.59 4.59c-.39.39-1.02.39"
                   "-1.41 0L6.7 10.7a.9959.9959 0 0 1 0-1.41c.39-.38 "
                   "1.03-.39 1.42 0z")}]])

(def ^:private opposite-horizontal
  {:right "left"
   :left "right"})

;; ---------------------------------------------------------------------------
;; IconMenuItem
;; ---------------------------------------------------------------------------

(defn icon-menu-item
  "A single `MenuItem` with optional left/right icons and a label.

  Options:
  - `:label`         text label (string)
  - `:render-label`  fn returning hiccup/element, takes precedence over `:label`
  - `:left-icon`     element/hiccup rendered before the label
  - `:right-icon`    element/hiccup rendered after the label
  - `:on-click`      click handler, receives the DOM event
  - `:disabled`      boolean
  - `:selected`      boolean (highlight as selected)
  - `:style`         inline style map merged over the layout defaults
  - `:sx`            MUI `sx` map forwarded as-is (MUI v5+ only)
  - `:menu-item-props` extra props forwarded to the underlying `MenuItem`
  - `:ref`           ref callback for the underlying `li`

  The built-in layout uses an inline `:style` (not `sx`) so it renders
  identically on every supported MUI version."
  [{:keys [label render-label left-icon right-icon on-click disabled selected
           style sx menu-item-props ref class]}]
  [:> MuiMenuItem
   (cond-> {:class class
            :disabled disabled
            :selected selected
            :ref ref
            :on-click on-click
            :style (merge {:display "flex"
                           :justify-content "space-between"
                           :padding-left "4px"
                           :padding-right "4px"}
                          style)}
     sx (assoc :sx sx)
     menu-item-props (merge menu-item-props))
   [:span {:style {:display "flex" :align-items "center"}}
    left-icon
    (if render-label
      (render-label)
      [:> MuiTypography
       {:style {:padding-left "8px"
                :padding-right "8px"
                :text-align "left"}}
       label])]
   right-icon])

;; ---------------------------------------------------------------------------
;; NestedMenuItem
;; ---------------------------------------------------------------------------

(defn- submenu-focused?
  "True when the document's active element is a direct child of the open
  sub-menu container."
  [container-ref menu-container-ref]
  (let [active (some-> @container-ref .-ownerDocument .-activeElement)]
    (boolean
     (when (and active @menu-container-ref)
       (some #(= % active)
             (array-seq (.-children @menu-container-ref)))))))

(defn nested-menu-item
  "A `MenuItem` that opens a nested `Menu` on hover/focus. Mirrors the
  behaviour of mui-nested-menu's `NestedMenuItem`, including keyboard
  navigation (ArrowRight enters the sub-menu, ArrowLeft returns to the
  parent) and a configurable open `:delay`.

  In addition to every `icon-menu-item` option it accepts:
  - `:items`             child item maps (rendered recursively)
  - `:parent-menu-open?` whether the containing menu is open
  - `:close!`            zero-arg fn closing the whole menu tree (root)
  - `:direction`         `:right` (default) or `:left`
  - `:delay`             ms to wait on hover before opening (default 0)
  - `:selected-value`    currently selected value (for highlighting leaves)
  - `:menu-props`        extra props forwarded to the sub-menu `Menu`
  - `:container-props`   extra props forwarded to the wrapper `div`"
  [{:keys [label render-label left-icon right-icon items disabled delay value
           direction selected-value parent-menu-open? close! menu-props
           container-props sx]
    :or {delay 0 direction :right}}]
  (r/with-let [open?* (r/atom false)
               item-ref (atom nil)
               container-ref (atom nil)
               menu-container-ref (atom nil)
               timeout* (atom nil)]
    (let [has-children? (boolean (seq items))
          open? (and parent-menu-open? @open?*)
          handle-mouse-enter
          (fn [e]
            (reset! timeout*
                    (js/setTimeout
                     (fn []
                       (when-not disabled (reset! open?* true))
                       (when-let [f (:on-mouse-enter container-props)]
                         (f e)))
                     delay)))
          handle-mouse-leave
          (fn [e]
            (some-> @timeout* js/clearTimeout)
            (reset! open?* false)
            (when-let [f (:on-mouse-leave container-props)]
              (f e)))
          handle-focus
          (fn [e]
            (when (and (= (.-target e) @container-ref) (not disabled))
              (reset! open?* true))
            (when-let [f (:on-focus container-props)]
              (f e)))
          handle-key-down
          (fn [e]
            (let [k (.-key e)]
              (when-not (= k "Escape")
                (when (submenu-focused? container-ref menu-container-ref)
                  (.stopPropagation e))
                (let [active (some-> @container-ref
                                     .-ownerDocument
                                     .-activeElement)]
                  (when (and (= k "ArrowLeft")
                             (submenu-focused? container-ref
                                               menu-container-ref))
                    (some-> @container-ref .focus))
                  (when (and (= k "ArrowRight")
                             (= (.-target e) @container-ref)
                             (= (.-target e) active))
                    (some-> @menu-container-ref
                            .-children
                            (aget 0)
                            (.focus)))))))]
      [:div
       (merge (dissoc container-props :on-mouse-enter :on-mouse-leave :on-focus)
              {:ref #(reset! container-ref %)
               :tab-index (when-not disabled -1)
               :on-focus handle-focus
               :on-mouse-enter handle-mouse-enter
               :on-mouse-leave handle-mouse-leave
               :on-key-down handle-key-down})
       [icon-menu-item
        {:class "jnm-menu-item"
         :ref #(reset! item-ref %)
         :label label
         :render-label render-label
         :left-icon left-icon
         :right-icon (or right-icon
                         (when has-children? [chevron-right]))
         :disabled disabled
         :selected (boolean (and value (= value selected-value)))
         :sx sx}]
       [:> MuiMenu
        (merge {:class ["jnm-menu" "jnm-submenu"]
                :style {:pointer-events "none"}
                :anchor-el @item-ref
                :open open?
                :anchor-origin {:vertical "top"
                                :horizontal (name direction)}
                :transform-origin
                {:vertical "top"
                 :horizontal (opposite-horizontal direction)}
                :auto-focus false
                :disable-auto-focus true
                :disable-enforce-focus true
                :on-close #(reset! open?* false)}
               menu-props)
        [:div {:ref #(reset! menu-container-ref %)
               :style {:pointer-events "auto"}}
         (menu-items-from-data
          {:items items
           :parent-menu-open? @open?*
           :close! close!
           :direction direction
           :selected-value selected-value})]]])))

;; ---------------------------------------------------------------------------
;; Building menus from data
;; ---------------------------------------------------------------------------

(defn menu-items-from-data
  "Turn a vector of item maps into a seq of `nested-menu-item` /
  `icon-menu-item` elements.

  Each item map supports: `:label`, `:render-label`, `:left-icon`,
  `:right-icon`, `:callback`, `:items`, `:disabled`, `:sx`, `:delay`,
  `:value` and `:uid`. Items with non-empty `:items` become sub-menus;
  leaves invoke `:callback` (called with `[event item]`) and then `:close!`."
  [{:keys [items parent-menu-open? close! direction selected-value]
    :or {direction :right}}]
  (doall
   (map-indexed
    (fn [i {:keys [items callback value] :as item}]
      (let [k (or (:uid item) (:label item) i)]
        (if (seq items)
          ^{:key k}
          [nested-menu-item
           (merge item
                  {:parent-menu-open? parent-menu-open?
                   :close! close!
                   :direction direction
                   :selected-value selected-value})]
          ^{:key k}
          [icon-menu-item
           (merge (select-keys item [:label :render-label :left-icon
                                     :right-icon :disabled :sx])
                  {:selected (boolean (and value (= value selected-value)))
                   :on-click (fn [e]
                               (when callback (callback e item))
                               (when close! (close!)))})])))
    items)))

;; ---------------------------------------------------------------------------
;; NestedDropdown
;; ---------------------------------------------------------------------------

(defn nested-menu
  "Dropdown button that opens a (possibly nested) menu.

  Options:
  - `:items`        vector of item maps (see `menu-items-from-data`)
  - `:label`        button label (falls back to `(:label button-props)`)
  - `:button-props` props forwarded to the MUI `Button`
  - `:menu-props`   props forwarded to the root MUI `Menu`
  - `:direction`    `:right` (default) or `:left` opening direction
  - `:value`        currently selected value (highlights matching leaves)
  - `:on-click`     extra handler invoked when the button is clicked"
  [{:keys [items button-props menu-props direction label on-click]
    :or {direction :right}
    selected-value :value}]
  (r/with-let [anchor-el (r/atom nil)]
    (let [open? (boolean @anchor-el)
          close! #(reset! anchor-el nil)]
      [:<>
       [:> MuiButton
        (merge {:id "select-tag-button"
                :aria-haspopup true
                :color "primary"
                :variant "outlined"
                :end-icon (r/as-element [chevron-down {:expanded? open?}])
                :on-click (fn [e]
                            (reset! anchor-el (.-currentTarget e))
                            (when on-click (on-click e)))}
               (dissoc button-props :label))
        (or label (:label button-props) "Menu")]
       [:> MuiMenu
        (merge {:class "jnm-menu"
                :open open?
                :anchor-el @anchor-el
                :on-close close!}
               menu-props)
        (menu-items-from-data
         {:items items
          :parent-menu-open? open?
          :close! close!
          :direction direction
          :selected-value selected-value})]])))

;; ---------------------------------------------------------------------------
;; ContextMenu
;; ---------------------------------------------------------------------------

(defn context-menu
  "Wrap `children` so that right-clicking inside them opens a nested menu
  anchored at the pointer position.

  Options:
  - `:items`      vector of item maps (see `menu-items-from-data`)
  - `:menu-props` extra props forwarded to the MUI `Menu`
  - `:direction`  `:right` (default) or `:left`"
  [{:keys [items menu-props direction] :or {direction :right}} & children]
  (r/with-let [position* (r/atom nil)
               mouse-down* (r/atom nil)
               wrapper-ref (atom nil)]
    (let [close! #(reset! position* nil)
          handle-mouse-down
          (fn [e]
            (when @position* (reset! position* nil))
            (when (= 2 (.-button e))
              (let [b (.getBoundingClientRect @wrapper-ref)
                    x (.-clientX e)
                    y (.-clientY e)]
                (when-not (or (< x (.-left b)) (> x (.-right b))
                              (< y (.-top b)) (> y (.-bottom b)))
                  (reset! mouse-down* {:left x :top y})))))
          handle-mouse-up
          (fn [e]
            (let [top (.-clientY e)
                  left (.-clientX e)]
              (when (and @mouse-down*
                         (= (:top @mouse-down*) top)
                         (= (:left @mouse-down*) left))
                (reset! position* {:left left :top top}))))]
      (into
       [:div {:ref #(reset! wrapper-ref %)
              :on-context-menu #(.preventDefault %)
              :on-mouse-down handle-mouse-down
              :on-mouse-up handle-mouse-up}
        (when @position*
          [:> MuiMenu
           (merge {:class "jnm-menu"
                   :open true
                   :on-close close!
                   :on-context-menu #(.preventDefault %)
                   :anchor-reference "anchorPosition"
                   :anchor-position @position*}
                  menu-props)
           (menu-items-from-data
            {:items items
             :parent-menu-open? true
             :close! close!
             :direction direction})])]
       (vec children)))))
