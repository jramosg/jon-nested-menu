# MUI Nested Menu Example

This project showcases a nested menu using Material-UI components in ClojureScript with [Reagent Material-UI](https://github.com/arttuka/reagent-material-ui).

## Usage

To implement the nested menu, use the following code:

```clojure
[nested-menu {:button-props {:label "Open"}
              :items [{:label "Menu item 1"
                       :callback #(prn "clicked 1")}
                      {:label "Menu item 2"
                       :callback #(prn "clicked 2")
                       :items [{:label "Menu item 2.1"
                                :callback #(prn "clicked 3")
                                :items [{:label "Menu item 2.1.1"
                                         :callback #(prn "clicked 2.1.1")}]}
                               {:label "Menu item 2.2"
                                :callback #(prn "clicked 2.2")}]}]}]
```

## Parameters

- :button-props: Map of properties for the button that triggers the menu.
  - :label: (String) The label displayed on the button.
  - other [button props](https://v5.mui.com/material-ui/api/button/)

- :items: Vector of menu item maps.
  - :label: (String) Display label for the menu item.
  - :callback: (Function) Function executed on click.
  - :items: (Optional) Nested vector of submenu items, which can contain the same structure.


## css Styles

Add the following CSS to style the nested menu:

```css
:root {
  --rotate-transition: 0.3s ease;
}

.nested-menu {
  pointer-events: none;
}

.nested-menu .nested-menu-item {
  pointer-events: auto;
}

.expand-group {
  transform: rotate(0deg);
}

.expand-group.MuiSvgIcon-root {
  transition: var(--rotate-transition);
}

.expand-group.expanded {
  transform: rotate(180deg);
}
```

# License
MIT License.

# Contributing
Fork the repository and submit pull requests for enhancements!
