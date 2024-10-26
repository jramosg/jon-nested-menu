(ns jon-nested-menu.core
  (:require
   [reagent.dom :as rdom]
   [jon-nested-menu.views :as views]
   [jon-nested-menu.config :as config]
   ["@mui/material/styles" :as styles-js]
   [reagent-mui.util :as mui-util]
   [reagent-mui.styles :as styles]
   [reagent-mui.material.container :refer [container]]
   [reagent-mui.material.css-baseline :refer [css-baseline]]))


(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(def example-items [{:label "Menu item 1"
                     :callback #(prn "clicked 1")}
                    {:label "Menu item 2"
                     :callback #(prn "clicked 2")
                     :items [{:label "Menu item 2.1"
                              :callback #(prn "clicked 3")
                              :items [{:label "Menu item 2.1.1"
                                       :callback #(prn "clicked 2.1.1")}]}
                             {:label "Menu item 2.2"
                              :callback #(prn "clicked 2.2")}]}])

(defn ^:dev/after-load mount-root []
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render
     [styles/experimental-css-vars-provider
      {:theme (styles-js/experimental_extendTheme #js{:cssVarPrefix "jon"})}
      [css-baseline]
      [container {:class "toolbar"}
       [views/nested-menu {:button-props {:label "Open menu right side"}
                           :items example-items}]
       [views/nested-menu {:button-props {:label "Open menu left side"}
                           :direction :left
                           :items example-items}]]]
     root-el)))

(defn init []
  (dev-setup)
  (mount-root))
