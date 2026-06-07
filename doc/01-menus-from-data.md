# Menus from data

You build every menu from plain ClojureScript data. A vector of item maps
becomes the menu; nesting an item's `:items` makes a sub-menu.

## Components

All of these live in the single public namespace
`reagent-mui-nested-menu.core`. The API reference (signatures and docstrings)
sits in the namespace list on the left.

| Component | Purpose |
|-----------|---------|
| `nested-menu` | Dropdown button that opens a nested menu. |
| `context-menu` | Wrap content so a right-click opens a nested menu at the pointer. |
| `nested-menu-item` | A menu item that opens a sub-menu (low-level). |
| `icon-menu-item` | A leaf menu item with optional icons (low-level). |
| `menu-items-from-data` | Turn item data into a seq of menu elements. |
| `chevron-right` / `chevron-down` | The default MUI `SvgIcon` chevrons. |

## Dropdown

```clojure
[nested-menu
 {:items        items          ;; vector of item maps (see below)
  :label        "Actions"      ;; button text (or :label inside :button-props)
  :button-props {...}          ;; props forwarded to MUI Button
  :menu-props   {...}          ;; props forwarded to the root MUI Menu
  :direction    :right         ;; :right (default) or :left
  :value        :high          ;; currently selected value (highlights leaves)
  :on-click     (fn [e] ...)}] ;; extra handler when the button is clicked
```

## Context menu

Right-click anywhere inside the wrapped content to open the menu at the
pointer.

```clojure
[context-menu
 {:items items}                ;; same item maps as the dropdown
 [:div {:style {:height 200}}
  "Right-click anywhere in here"]]
```

## Item maps

Each item map accepts:

| Key | Type | Description |
|-----|------|-------------|
| `:label` | string | Item text. |
| `:render-label` | `(fn [] hiccup)` | Custom label; takes precedence over `:label`. |
| `:left-icon` | hiccup/element | Rendered before the label. |
| `:right-icon` | hiccup/element | Rendered after the label (defaults to a chevron for sub-menus). |
| `:callback` | `(fn [event item])` | Runs when a leaf is clicked, then the menu closes. |
| `:items` | vector | Child items that turn this entry into a sub-menu. |
| `:disabled` | boolean | Disable the item. |
| `:delay` | number | ms to hover before the sub-menu opens (default `0`). |
| `:value` | any | Selection value; highlighted when it equals the root `:value`. |
| `:sx` | map | Forwarded to the MUI `MenuItem` (MUI v5+ only). |
| `:uid` | string | Stable React key (otherwise `:label`/index is used). |

Clicking an item that has `:items` opens its sub-menu and skips `:callback`.
Only leaf items run `:callback`.

## Icons

Pass any element as `:left-icon` or `:right-icon`. MUI icon components work
through `r/create-element`:

```clojure
(:require ["@mui/icons-material/ContentCopy$default" :as CopyIcon]
          [reagent.core :as r])

[nested-menu
 {:label "Edit"
  :items [{:label "Copy"
           :left-icon (r/create-element CopyIcon)
           :callback (fn [_e _item] (copy!))}
          {:label "Disabled" :disabled true}]}]
```

## Custom labels and selection

`:render-label` returns hiccup, so an item can show a title with a subtitle or
a badge. Pair root `:value` with per-item `:value` to highlight the current
choice:

```clojure
[nested-menu
 {:value @priority
  :items (for [k [:low :high]]
           {:label (name k)
            :value k
            :callback (fn [_ _] (reset! priority k))})}]
```

Next: [React usage](02-react-usage.md) and [Styling](03-styling.md).
