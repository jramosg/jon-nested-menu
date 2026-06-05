# jon-nested-menu

Nested MUI menu components for **Reagent/ClojureScript** and **React**.

A ClojureScript port (and superset) of
[mui-nested-menu](https://github.com/steviebuilds/mui-nested-menu) by
[steviebuilds](https://github.com/steviebuilds): a dropdown that opens
arbitrarily deep nested menus, a right-click context menu, per-item icons,
custom labels, keyboard navigation and selection state.

- **Self-contained** — no external CSS required; the bits that must work
  (pointer-events, caret rotation) are applied inline.
- **Broad MUI support** — works with `@mui/material` v5, v6, v7, v8 and v9.
  Uses MUI subpath imports and plain inline `style` (not `sx`) for its own
  styling, so nothing depends on a specific MUI version.
- **Two consumers** — published to **Clojars** (Reagent) and **npm** (React).

![Example GIF](https://raw.githubusercontent.com/jramosg/jon-nested-menu/master/public/example.gif)

## Install

### ClojureScript (Clojars)

`deps.edn`:

```clojure
{:deps {io.github.jramosg/jon-nested-menu {:mvn/version "0.1.0"}
        reagent/reagent {:mvn/version "1.2.0"}}}
```

### React (npm)

```bash
npm install jon-nested-menu
```

### Peer dependencies

MUI and React are peers (you provide them). Emotion is required by MUI v5–v6
and is optional from MUI v7 onward.

```json
{
  "peerDependencies": {
    "@mui/material": ">=5 <10",
    "react": ">=17 <20",
    "react-dom": ">=17 <20",
    "@emotion/react": ">=11",
    "@emotion/styled": ">=11"
  }
}
```

## Quick start (CLJS)

```clojure
(ns my.app
  (:require [jon-nested-menu.nested-menu :refer [nested-menu]]))

[nested-menu
 {:button-props {:label "Open"}
  :items [{:label "New file" :callback (fn [_e _item] (prn "new"))}
          {:label "Export"
           :items [{:label "PDF"  :callback (fn [_e _item] (prn "pdf"))}
                   {:label "JSON" :callback (fn [_e _item] (prn "json"))}]}]}]
```

No CSS import is needed, it works out of the box.

## Components

All components live in `jon-nested-menu.nested-menu` (the older
`jon-nested-menu.js-mui` alias still works):

| Component | Purpose |
|-----------|---------|
| `nested-menu` | Dropdown button that opens a nested menu. |
| `context-menu` | Wrap content so a right-click opens a nested menu at the pointer. |
| `nested-menu-item` | A single menu item that opens a sub-menu (low-level). |
| `icon-menu-item` | A single leaf menu item with optional icons (low-level). |
| `menu-items-from-data` | Build a seq of menu elements from item data. |
| `chevron-right` / `chevron-down` | The default MUI `SvgIcon` chevrons. |

### `nested-menu` (dropdown)

```clojure
[nested-menu
 {:items        items        ;; vector of item maps (see below)
  :label        "Actions"    ;; button text (or :label inside :button-props)
  :button-props {...}         ;; props forwarded to MUI Button
  :menu-props   {...}         ;; props forwarded to the root MUI Menu
  :direction    :right        ;; :right (default) or :left
  :value        :high         ;; currently selected value (highlights leaves)
  :on-click     (fn [e] ...)}] ;; extra handler when the button is clicked
```

### `context-menu` (right-click)

```clojure
[context-menu
 {:items items}                ;; same item maps as nested-menu
 [:div {:style {:height 200}}
  "Right-click anywhere in here"]]
```

### Item maps

Every item passed to `:items` accepts:

| Key | Type | Description |
|-----|------|-------------|
| `:label` | string | Item text. |
| `:render-label` | `(fn [] hiccup)` | Custom label; takes precedence over `:label`. |
| `:left-icon` | hiccup/element | Rendered before the label. |
| `:right-icon` | hiccup/element | Rendered after the label (defaults to a chevron for sub-menus). |
| `:callback` | `(fn [event item])` | Called when a **leaf** is clicked, then the menu closes. |
| `:items` | vector | Child items — turns this entry into a sub-menu. |
| `:disabled` | boolean | Disable the item. |
| `:delay` | number | ms to hover before the sub-menu opens (default `0`). |
| `:value` | any | Selection value; highlighted when it equals the root `:value`. |
| `:sx` | map | Forwarded to the MUI `MenuItem` (MUI v5+ only). |
| `:uid` | string | Stable React key (otherwise `:label`/index is used). |

> Note: matching mui-nested-menu, clicking an item that *has children* opens
> its sub-menu and does **not** fire its `:callback`. Only leaves invoke
> `:callback`.

### Icons example

```clojure
(:require ["@mui/icons-material/ContentCopy" :default CopyIcon]
          [reagent.core :as r])

[nested-menu
 {:label "Edit"
  :items [{:label "Copy"
           :left-icon (r/create-element CopyIcon)
           :callback (fn [_e _item] (copy!))}
          {:label "Disabled" :disabled true}]}]
```

## React usage

The npm build ships real React components (the Reagent components are
reactified and their JS props are converted automatically — `buttonProps`
becomes `:button-props`, `leftIcon` becomes `:left-icon`, and so on). Pass
icons as JSX elements and `items` as plain objects.

The package is published as **dual ESM + CommonJS**, so named imports work
directly in Vite/webpack/esbuild, and `require()` still works in Node:

```jsx
import { NestedMenu, ContextMenu } from 'jon-nested-menu';

const items = [
  { label: 'New file', callback: () => console.log('new') },
  {
    label: 'Export',
    items: [
      { label: 'PDF', callback: () => console.log('pdf') },
      { label: 'JSON', disabled: true },
    ],
  },
];

<NestedMenu buttonProps={{ label: 'Open' }} items={items} direction="left" />

<ContextMenu items={items}>
  <div>Right-click here</div>
</ContextMenu>
```

Named exports: `NestedMenu`, `ContextMenu`, `NestedMenuItem`, `IconMenuItem`,
`ChevronRight`, `ChevronDown`. Props are camelCase (`buttonProps`, `menuProps`,
`leftIcon`, `rightIcon`, `renderLabel`). See `demo-react/` for a runnable
example.

## Styling

Nothing is required, the essential behaviour (sub-menu `pointer-events` and
the dropdown caret rotation) is applied as inline `style`, so the components
work with zero CSS on any MUI version.

For optional theming the following (namespaced) class hooks are emitted:

- `.jnm-menu` — every `Menu` rendered by the library (dropdown, sub-menus,
  context menu).
- `.jnm-submenu` — additionally on nested sub-menu `Menu` elements.
- `.jnm-menu-item` — each item that opens a sub-menu.
- `.jnm-caret` / `.jnm-caret-expanded` — the dropdown caret.

Per-item styling is best done with the `:sx` (MUI v5+) or `:style` keys on
the item map; class overrides of the caret rotation need `!important` because
the rotation is inline.

## Build & publish

### npm

```bash
npm run release:library   # builds dist/cjs (CommonJS) + dist/esm (ESM)
npm publish
```

### Clojars (via Slim / tools.build)

The library is published under the group `io.github.jramosg`, which Clojars
auto-verifies for the GitHub user `jramosg` (the first deploy walks you
through verification at https://clojars.org/verify/group). To publish your
own fork, change `:lib` in the `:build` alias of `deps.edn` to your own
group, e.g. `io.github.<your-user>/jon-nested-menu` or
`net.clojars.<your-user>/jon-nested-menu`.

1. Create a Clojars account, then a **deploy token** at
   https://clojars.org/tokens (use the token as the password — never your
   account password).
2. Export credentials:

   ```bash
   export CLOJARS_USERNAME=your-clojars-username
   export CLOJARS_PASSWORD=your-deploy-token
   ```

3. Build and deploy:

   ```bash
   clojure -T:build build                   # build the jar locally (optional)
   clojure -T:build deploy :snapshot true   # publish a -SNAPSHOT
   clojure -T:build deploy                   # publish a release
   ```

Bump `:version` in the `:build` alias of `deps.edn` (and `deps.edn`'s
install snippet above) for each release.

## Demos

```bash
cd demo && npm install && npm run start   # ClojureScript + Reagent
cd demo-react && npm install && npm run dev  # React + MUI
```

## Credits

This library is a ClojureScript/Reagent port of
[mui-nested-menu](https://github.com/steviebuilds/mui-nested-menu) by
[steviebuilds](https://github.com/steviebuilds). Thanks for the original
React implementation and API design.

## License

MIT.
