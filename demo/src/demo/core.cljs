(ns demo.core
  (:require [reagent.core :as r]
            [reagent.dom.client :as rdomc]
            [reagent-mui-nested-menu.core :as nm]
            ["@mui/material/styles" :as styles-js]))

(defonce root* (atom nil))

(def repo-url "https://github.com/jramosg/jon-nested-menu")
(def clojars-url
  "https://clojars.org/io.github.jramosg/reagent-mui-nested-menu")
(def npm-url "https://www.npmjs.com/package/reagent-mui-nested-menu")
(def coffee-url "https://www.buymeacoffee.com/jramosg")

;; ---------------------------------------------------------------------------
;; Theme (GitHub palette, light + dark) to match jonramos.dev
;; ---------------------------------------------------------------------------

(defn- initial-theme []
  (or (.getItem js/localStorage "jnm-theme")
      (if (.. js/window
              (matchMedia "(prefers-color-scheme: dark)")
              -matches)
        "dark"
        "light")))

(defonce theme* (r/atom (initial-theme)))

(defn- set-theme! [t]
  (.setItem js/localStorage "jnm-theme" t)
  (.setAttribute (.-documentElement js/document) "data-theme" t))

(defn- mui-theme [mode]
  (styles-js/createTheme
   (clj->js
    {:palette
     (if (= mode "dark")
       {:mode "dark"
        :primary {:main "#7ee787" :contrastText "#0d1117"}
        :background {:default "#0d1117" :paper "#161b22"}
        :text {:primary "#e6edf3" :secondary "#aeb4bc"}
        :divider "rgba(240,246,252,0.1)"}
       {:mode "light"
        :primary {:main "#1a7f37" :contrastText "#ffffff"}
        :background {:default "#fafbfc" :paper "#ffffff"}
        :text {:primary "#1f2328" :secondary "#57606a"}
        :divider "rgba(31,35,40,0.1)"})
     :shape {:borderRadius 8}
     :typography
     {:fontFamily "'JetBrains Mono', ui-monospace, monospace"
      :button {:textTransform "none" :fontWeight 600 :letterSpacing "-0.01em"}}})))

;; ---------------------------------------------------------------------------
;; Custom inline SVG icons (Feather-style, inherit currentColor)
;; ---------------------------------------------------------------------------

(def ^:private icon-paths
  {:file      [[:path {:d "M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z"}]
               [:path {:d "M13 2v7h7"}]]
   :folder    [[:path {:d "M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"}]]
   :share     [[:circle {:cx 18 :cy 5 :r 3}] [:circle {:cx 6 :cy 12 :r 3}]
               [:circle {:cx 18 :cy 19 :r 3}]
               [:line {:x1 8.59 :y1 13.51 :x2 15.42 :y2 17.49}]
               [:line {:x1 15.41 :y1 6.51 :x2 8.59 :y2 10.49}]]
   :copy      [[:rect {:x 9 :y 9 :width 13 :height 13 :rx 2 :ry 2}]
               [:path {:d "M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"}]]
   :mail      [[:path {:d "M4 4h16a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z"}]
               [:polyline {:points "22,6 12,13 2,6"}]]
   :download  [[:path {:d "M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"}]
               [:polyline {:points "7,10 12,15 17,10"}]
               [:line {:x1 12 :y1 15 :x2 12 :y2 3}]]
   :file-text [[:path {:d "M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"}]
               [:polyline {:points "14,2 14,8 20,8"}]
               [:line {:x1 16 :y1 13 :x2 8 :y2 13}]
               [:line {:x1 16 :y1 17 :x2 8 :y2 17}]]
   :code      [[:polyline {:points "16,18 22,12 16,6"}]
               [:polyline {:points "8,6 2,12 8,18"}]]
   :image     [[:rect {:x 3 :y 3 :width 18 :height 18 :rx 2 :ry 2}]
               [:circle {:cx 8.5 :cy 8.5 :r 1.5}]
               [:polyline {:points "21,15 16,10 5,21"}]]
   :user      [[:path {:d "M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"}]
               [:circle {:cx 12 :cy 7 :r 4}]]
   :users     [[:path {:d "M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"}]
               [:circle {:cx 9 :cy 7 :r 4}]
               [:path {:d "M23 21v-2a4 4 0 0 0-3-3.87"}]
               [:path {:d "M16 3.13a4 4 0 0 1 0 7.75"}]]
   :shield    [[:path {:d "M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"}]]
   :trash     [[:polyline {:points "3,6 5,6 21,6"}]
               [:path {:d "M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"}]]
   :card      [[:rect {:x 1 :y 4 :width 22 :height 16 :rx 2 :ry 2}]
               [:line {:x1 1 :y1 10 :x2 23 :y2 10}]]
   :check     [[:polyline {:points "20,6 9,17 4,12"}]]
   :settings  [[:circle {:cx 12 :cy 12 :r 3}]
               [:path {:d "M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"}]]
   :globe     [[:circle {:cx 12 :cy 12 :r 10}]
               [:line {:x1 2 :y1 12 :x2 22 :y2 12}]
               [:path {:d "M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"}]]
   :lock      [[:rect {:x 3 :y 11 :width 18 :height 11 :rx 2 :ry 2}]
               [:path {:d "M7 11V7a5 5 0 0 1 10 0v4"}]]
   :logout    [[:path {:d "M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"}]
               [:polyline {:points "16,17 21,12 16,7"}]
               [:line {:x1 21 :y1 12 :x2 9 :y2 12}]]
   :bell      [[:path {:d "M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"}]
               [:path {:d "M13.73 21a2 2 0 0 1-3.46 0"}]]
   :zap       [[:polygon {:points "13,2 3,14 12,14 11,22 21,10 12,10"}]]
   :scissors  [[:circle {:cx 6 :cy 6 :r 3}] [:circle {:cx 6 :cy 18 :r 3}]
               [:line {:x1 20 :y1 4 :x2 8.12 :y2 15.88}]
               [:line {:x1 14.47 :y1 14.48 :x2 20 :y2 20}]
               [:line {:x1 8.12 :y1 8.12 :x2 12 :y2 12}]]
   :clipboard [[:path {:d "M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"}]
               [:rect {:x 8 :y 2 :width 8 :height 4 :rx 1 :ry 1}]]
   :edit      [[:path {:d "M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"}]
               [:path {:d "M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"}]]
   :rocket    [[:path {:d "M4.5 16.5c-1.5 1.26-2 5-2 5s3.74-.5 5-2c.71-.84.7-2.13-.09-2.91a2.18 2.18 0 0 0-2.91-.09z"}]
               [:path {:d "M12 15l-3-3a22 22 0 0 1 2-3.95A12.88 12.88 0 0 1 22 2c0 2.72-.78 7.5-6 11a22.35 22.35 0 0 1-4 2z"}]
               [:path {:d "M9 12H4s.55-3.03 2-4c1.62-1.08 5 0 5 0"}]
               [:path {:d "M12 15v5s3.03-.55 4-2c1.08-1.62 0-5 0-5"}]]
   :github    [[:path {:d "M9 19c-5 1.5-5-2.5-7-3m14 5v-3.87a3.37 3.37 0 0 0-.94-2.61c3.14-.35 6.44-1.54 6.44-7A5.44 5.44 0 0 0 20 4.77 5.07 5.07 0 0 0 19.91 1S18.73.65 16 2.48a13.38 13.38 0 0 0-7 0C6.27.65 5.09 1 5.09 1A5.07 5.07 0 0 0 5 4.77a5.44 5.44 0 0 0-1.5 3.78c0 5.42 3.3 6.61 6.44 7A3.37 3.37 0 0 0 9 18.13V22"}]]
   :package   [[:line {:x1 16.5 :y1 9.4 :x2 7.5 :y2 4.21}]
               [:path {:d "M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"}]
               [:polyline {:points "3.27,6.96 12,12.01 20.73,6.96"}]
               [:line {:x1 12 :y1 22.08 :x2 12 :y2 12}]]
   :sun       [[:circle {:cx 12 :cy 12 :r 5}]
               [:line {:x1 12 :y1 1 :x2 12 :y2 3}]
               [:line {:x1 12 :y1 21 :x2 12 :y2 23}]
               [:line {:x1 4.22 :y1 4.22 :x2 5.64 :y2 5.64}]
               [:line {:x1 18.36 :y1 18.36 :x2 19.78 :y2 19.78}]
               [:line {:x1 1 :y1 12 :x2 3 :y2 12}]
               [:line {:x1 21 :y1 12 :x2 23 :y2 12}]
               [:line {:x1 4.22 :y1 19.78 :x2 5.64 :y2 18.36}]
               [:line {:x1 18.36 :y1 5.64 :x2 19.78 :y2 4.22}]]
   :moon      [[:path {:d "M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"}]]
   :coffee    [[:path {:d "M18 8h1a4 4 0 0 1 0 8h-1"}]
               [:path {:d "M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z"}]
               [:line {:x1 6 :y1 1 :x2 6 :y2 4}]
               [:line {:x1 10 :y1 1 :x2 10 :y2 4}]
               [:line {:x1 14 :y1 1 :x2 14 :y2 4}]]})

(defn icon
  ([k] (icon k nil))
  ([k {:keys [size color mr] :or {size 18 mr true}}]
   (into [:svg {:width size :height size :viewBox "0 0 24 24" :fill "none"
                :stroke (or color "currentColor") :stroke-width 1.8
                :stroke-linecap "round" :stroke-linejoin "round"
                :aria-hidden true
                :style {:flex "0 0 auto" :margin-right (if mr "10px" 0)}}]
         (icon-paths k))))

(defn- kbd [s]
  [:span {:style {:font-family "var(--font-mono)"
                  :font-size "0.7rem"
                  :padding "2px 6px"
                  :border-radius "6px"
                  :margin-left "16px"
                  :color "var(--color-text-subtle)"
                  :border "1px solid var(--color-border)"
                  :background "var(--color-bg-surface)"}}
   s])

(defn- two-line [title subtitle badge badge-color]
  (fn []
    [:span {:style {:display "flex" :flex-direction "column"
                    :padding-left "8px" :padding-right "8px"}}
     [:span {:style {:display "flex" :align-items "center" :gap "8px"}}
      [:span {:style {:font-weight 600}} title]
      (when badge
        [:span {:style {:font-size "0.62rem" :font-weight 700
                        :letter-spacing "0.04em" :text-transform "uppercase"
                        :padding "1px 7px" :border-radius "999px"
                        :color "#0b0712"
                        :background (or badge-color "var(--color-primary)")}}
         badge])]
     [:span {:style {:font-size "0.76rem" :color "var(--color-text-subtle)"
                     :margin-top "2px"}}
      subtitle]]))

(defn- cb [s] (fn [_e _item] (js/console.log s)))

;; ---------------------------------------------------------------------------
;; Menu data
;; ---------------------------------------------------------------------------

(def file-items
  [{:label "New file" :left-icon (icon :file)
    :right-icon (kbd "⌘N") :callback (cb "new-file")}
   {:label "New folder" :left-icon (icon :folder)
    :right-icon (kbd "⌘⇧N") :callback (cb "new-folder")}
   {:label "Share" :left-icon (icon :share)
    :items [{:label "Copy link" :left-icon (icon :copy)
             :callback (cb "copy-link")}
            {:label "Email" :left-icon (icon :mail) :callback (cb "email")}]}
   {:label "Export" :left-icon (icon :download)
    :items [{:label "PDF" :left-icon (icon :file-text) :callback (cb "pdf")}
            {:label "Markdown" :left-icon (icon :code) :callback (cb "md")}
            {:label "Image" :left-icon (icon :image) :callback (cb "image")}]}])

(def account-items
  [{:label "Profile" :left-icon (icon :user) :callback (cb "profile")}
   {:label "Notifications" :left-icon (icon :bell) :callback (cb "notify")}
   {:label "Workspace" :left-icon (icon :users)
    :items [{:label "Members" :left-icon (icon :users)
             :callback (cb "members")}
            {:label "Permissions" :left-icon (icon :lock)
             :callback (cb "perms")}
            {:label "Billing" :left-icon (icon :card)
             :items [{:label "Invoices" :left-icon (icon :file-text)
                      :callback (cb "invoices")}
                     {:label "Payment methods" :left-icon (icon :card)
                      :callback (cb "payment")}]}]}
   {:label "Sign out" :left-icon (icon :logout)
    :sx {:color "#f78166"} :callback (cb "sign-out")}])

(def deep-items
  [{:label "Organization" :left-icon (icon :globe)
    :items [{:label "Teams" :left-icon (icon :users)
             :items [{:label "Engineering" :left-icon (icon :code)
                      :items [{:label "Frontend" :callback (cb "fe")}
                              {:label "Backend" :callback (cb "be")}
                              {:label "Platform" :callback (cb "platform")}]}
                     {:label "Design" :left-icon (icon :image)
                      :callback (cb "design")}]}
            {:label "Settings" :left-icon (icon :settings)
             :items [{:label "General" :callback (cb "general")}
                     {:label "Security" :left-icon (icon :shield)
                      :callback (cb "security")}]}]}])

(def render-label-items
  [{:left-icon (icon :rocket)
    :render-label (two-line "Pro plan" "Everything in Team, plus SSO"
                            "popular" "#7ee787")
    :callback (cb "pro")}
   {:left-icon (icon :zap)
    :render-label (two-line "Team plan" "Up to 20 collaborators" nil nil)
    :callback (cb "team")}
   {:left-icon (icon :user)
    :render-label (two-line "Free" "For personal projects" nil nil)
    :callback (cb "free")}])

(def context-items
  [{:label "Copy" :left-icon (icon :copy) :right-icon (kbd "⌘C")
    :callback (cb "ctx-copy")}
   {:label "Cut" :left-icon (icon :scissors) :right-icon (kbd "⌘X")
    :callback (cb "ctx-cut")}
   {:label "Paste" :left-icon (icon :clipboard) :right-icon (kbd "⌘V")
    :callback (cb "ctx-paste")}
   {:label "Transform" :left-icon (icon :edit)
    :items [{:label "Rename" :left-icon (icon :edit) :callback (cb "rename")}
            {:label "Duplicate" :left-icon (icon :copy)
             :callback (cb "duplicate")}]}
   {:label "Delete" :left-icon (icon :trash) :sx {:color "#f78166"}
    :right-icon (kbd "⌫") :callback (cb "ctx-delete")}])

(def priority-meta
  {:low      {:label "Low"      :color "#3fb950"}
   :medium   {:label "Medium"   :color "#d29922"}
   :high     {:label "High"     :color "#db6d28"}
   :critical {:label "Critical" :color "#f78166"}})

;; ---------------------------------------------------------------------------
;; UI
;; ---------------------------------------------------------------------------

(defn- button-props [label icon-key variant]
  {:label label
   :variant variant
   :disable-elevation true
   :start-icon (r/as-element (icon icon-key {:size 17}))
   :sx {:border-radius "8px" :px "16px" :py "8px"}})

(defn- card [{:keys [title blurb snippet]} & body]
  (into [:article {:class "card"}
         [:div {:class "card-head"}
          [:h2 title]
          [:p {:class "card-blurb"} blurb]]
         (into [:div {:class "card-stage"}] body)]
        (when snippet
          [[:pre {:class "snippet"} [:code snippet]]])))

(defn- top-link [href label icon-key]
  [:a {:class "top-link" :href href :target "_blank" :rel "noopener"}
   (icon icon-key {:size 15 :mr false})
   [:span label]])

(defn- topbar []
  (let [dark? (= @theme* "dark")]
    [:div {:class "topbar"}
     [:span {:class "brand"}
      [:span {:class "slash"} "//"] " reagent-mui-nested-menu"]
     [:div {:class "topbar-links"}
      [top-link repo-url "GitHub" :github]
      [top-link clojars-url "Clojars" :package]
      [top-link npm-url "npm" :package]
      [top-link coffee-url "Coffee" :coffee]
      [:button {:class "theme-toggle"
                :type "button"
                :aria-label "Toggle theme"
                :on-click (fn []
                            (let [t (if dark? "light" "dark")]
                              (set-theme! t)
                              (reset! theme* t)))}
       (icon (if dark? :sun :moon) {:size 17 :mr false})]]]))

(defn- showcase-page []
  (r/with-let [events* (r/atom [])
               priority* (r/atom :high)]
    (letfn [(track [items]
              (mapv (fn [{:keys [label callback items] :as item}]
                      (assoc item
                             :callback
                             (fn [e it]
                               (swap! events*
                                      #(->> (str (.toLocaleTimeString (js/Date.))
                                                 "  ·  " (or label "item"))
                                            (conj %)
                                            (take-last 14)
                                            vec))
                               (when callback (callback e it)))
                             :items (when (seq items) (track items))))
                    items))
            (priority-items []
              (track
               (for [k [:low :medium :high :critical]
                     :let [{:keys [label color]} (priority-meta k)]]
                 {:label label :value k
                  :left-icon (icon :zap {:color color})
                  :right-icon (when (= k @priority*)
                                (icon :check {:color color}))
                  :callback (fn [_ _] (reset! priority* k))})))]
      [:<>
       [topbar]
       [:div {:class "shell"}
        [:header {:class "hero"}
         [:span {:class "eyebrow"} "ClojureScript · Reagent · MUI"]
         [:h1 "Nested menus," [:br] [:span {:class "accent"} "from data."]]
         [:p {:class "lead"}
          "A nested MUI menu library for Reagent and React. Describe the menu "
          "as plain ClojureScript data and render it as a dropdown or a "
          "right-click context menu, with icons and keyboard navigation."]
         [:div {:class "hero-actions"}
          [nm/nested-menu
           {:button-props (button-props "File" :file "contained")
            :items (track file-items)}]
          [nm/nested-menu
           {:button-props (button-props "Account" :user "outlined")
            :direction :left
            :items (track account-items)}]]
         [:div {:class "install"}
          [:code [:span {:class "tok"} "clojars"]
           " io.github.jramosg/reagent-mui-nested-menu"]
          [:code [:span {:class "tok"} "npm i"]
           " reagent-mui-nested-menu"]]]

        [:section
         [:h2 {:class "section-title"} "Showcase"]
         [:div {:class "grid"}
          [card {:title "Icons + shortcuts"
                 :blurb "Left icons, sub-menus and custom right-side content."
                 :snippet "{:label \"New file\"\n :left-icon (icon :file)\n :right-icon (kbd \"⌘N\")}"}
           [nm/nested-menu {:button-props (button-props "File menu" :file
                                                        "outlined")
                            :items (track file-items)}]]

          [card {:title "Opens to the left"
                 :blurb "Set :direction :left for edge-aligned layouts."
                 :snippet "[nested-menu\n {:direction :left\n  :items account-items}]"}
           [nm/nested-menu {:button-props (button-props "Account" :user
                                                        "outlined")
                            :direction :left
                            :items (track account-items)}]]

          [card {:title "Selection state"
                 :blurb (str "Click an item to set it. Current: "
                             (:label (priority-meta @priority*)) ".")
                 :snippet "[nested-menu {:items items\n              :value @priority*}]"}
           [nm/nested-menu
            {:button-props (button-props
                            (str "Priority: "
                                 (:label (priority-meta @priority*)))
                            :zap "outlined")
             :value @priority*
             :items (priority-items)}]]

          [card {:title "Deep navigation"
                 :blurb "Four levels deep with hover-to-open submenus."
                 :snippet "{:label \"Teams\"\n :items [{:label \"Engineering\"\n          :items [...]}]}"}
           [nm/nested-menu {:button-props (button-props "Browse org" :globe
                                                        "outlined")
                            :items (track deep-items)}]]

          [card {:title "Disabled + open delay"
                 :blurb "Disable items and delay submenu opening on hover."
                 :snippet "{:label \"Archived\" :disabled true}\n{:label \"Reports\" :delay 350}"}
           [nm/nested-menu
            {:button-props (button-props "States" :settings "outlined")
             :items (track
                     [{:label "Active" :left-icon (icon :check)
                       :callback (cb "active")}
                      {:label "Archived (disabled)" :left-icon (icon :lock)
                       :disabled true}
                      {:label "Reports" :left-icon (icon :file-text)
                       :delay 350
                       :items [{:label "Opens after 350ms"
                                :left-icon (icon :zap)
                                :callback (cb "delayed")}]}])}]]

          [card {:title "Custom labels"
                 :blurb "Give an item :render-label to draw a custom title and subtitle."
                 :snippet "{:render-label\n (fn [] [:span ...title+subtitle...])}"}
           [nm/nested-menu {:button-props (button-props "Choose plan" :rocket
                                                        "outlined")
                            :items (track render-label-items)}]]]]

        [:section {:class "context-row"}
         [:article {:class "card"}
          [:div {:class "card-head"}
           [:h2 "Right-click context menu"]
           [:p {:class "card-blurb"}
            "context-menu wraps any content and opens at the pointer. "
            "Per-item :sx paints the Delete row red."]]
          [nm/context-menu {:items (track context-items)}
           [:div {:class "context-target"}
            [:div {:class "context-glow"}]
            [:span "right-click anywhere in this canvas"]]]]

         [:article {:class "card"}
          [:div {:class "card-head log-head"}
           [:h2 "Event log"]
           [:button {:class "ghost-btn" :type "button"
                     :on-click #(reset! events* [])}
            "clear"]]
          [:ul {:class "log"}
           (if (seq @events*)
             (doall
              (for [[i e] (map-indexed vector (rseq @events*))]
                ^{:key i} [:li [:span {:class "dot"}] e]))
             [:li {:class "log-empty"} "interact with a menu to see events…"])]]]]

       [:footer {:class "footer"}
        [:span "reagent-mui-nested-menu"]
        [:span {:class "sep"} "·"]
        [:a {:href repo-url :target "_blank" :rel "noopener"} "GitHub"]
        [:span {:class "sep"} "·"]
        [:a {:href clojars-url :target "_blank" :rel "noopener"} "Clojars"]
        [:span {:class "sep"} "·"]
        [:a {:href npm-url :target "_blank" :rel "noopener"} "npm"]
        [:span {:class "sep"} "·"]
        [:a {:href coffee-url :target "_blank" :rel "noopener"}
         "Buy me a coffee"]
        [:span {:class "sep"} "·"]
        [:span "built with Reagent + MUI"]]])))

(defn- app []
  [:> (.-ThemeProvider styles-js) {:theme (mui-theme @theme*)}
   [showcase-page]])

(defn ^:dev/after-load mount-root []
  (let [el (.getElementById js/document "app")]
    (when (nil? @root*)
      (reset! root* (rdomc/create-root el)))
    (rdomc/render @root* [app])))

(defn init []
  (set-theme! @theme*)
  (mount-root))
