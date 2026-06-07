# Styling

You need no CSS. Sub-menu `pointer-events` and the dropdown caret rotation
ship as inline `style`, so the components render correctly with zero CSS on
any MUI version.

## Per-item styling

Style a single item with `:sx` (MUI v5+) or `:style` on its item map:

```clojure
{:label "Delete"
 :left-icon (icon :trash)
 :sx {:color "#f78166"}}
```

## Class hooks

For theme-wide rules, target these namespaced classes:

| Class | Element |
|-------|---------|
| `.jnm-menu` | every `Menu` the library renders |
| `.jnm-submenu` | nested sub-menu `Menu` elements |
| `.jnm-menu-item` | an item that opens a sub-menu |
| `.jnm-caret`, `.jnm-caret-expanded` | the dropdown caret |

Overriding the caret rotation through a class needs `!important`, because the
rotation is inline.
