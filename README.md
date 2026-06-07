# reagent-mui-nested-menu

[![Clojars Project](https://img.shields.io/clojars/v/io.github.jramosg/reagent-mui-nested-menu.svg)](https://clojars.org/io.github.jramosg/reagent-mui-nested-menu)
[![npm](https://img.shields.io/npm/v/reagent-mui-nested-menu.svg)](https://www.npmjs.com/package/reagent-mui-nested-menu)
[![cljdoc](https://cljdoc.org/badge/io.github.jramosg/reagent-mui-nested-menu)](https://cljdoc.org/d/io.github.jramosg/reagent-mui-nested-menu)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

Nested MUI menus for **Reagent/ClojureScript** and **React**: a dropdown, a
right-click context menu, per-item icons, custom labels and keyboard
navigation. A ClojureScript port of
[mui-nested-menu](https://github.com/steviebuilds/mui-nested-menu) by
[steviebuilds](https://github.com/steviebuilds).

- **No CSS to import.** Pointer-events and the caret rotation ship inline.
- **MUI v5 to v9.** Subpath imports and inline `style` keep it off any
  version-specific API.
- **Clojars and npm.** One source builds the Reagent jar and the React package.

![Example GIF](https://raw.githubusercontent.com/jramosg/jon-nested-menu/master/public/example.gif)

## Install

### ClojureScript (Clojars)

`deps.edn`:

```clojure
{:deps {io.github.jramosg/reagent-mui-nested-menu {:mvn/version "0.0.1"}
        reagent/reagent {:mvn/version "2.0.0"}}}
```

### React (npm)

```bash
npm install reagent-mui-nested-menu
```

### Peer dependencies

MUI and React are peers (you provide them). MUI v5 and v6 need Emotion;
v7 and later make it optional.

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
  (:require [reagent-mui-nested-menu.core :refer [nested-menu]]))

[nested-menu
 {:button-props {:label "Open"}
  :items [{:label "New file" :callback (fn [_e _item] (prn "new"))}
          {:label "Export"
           :items [{:label "PDF"  :callback (fn [_e _item] (prn "pdf"))}
                   {:label "JSON" :callback (fn [_e _item] (prn "json"))}]}]}]
```

No CSS import needed.

## Documentation

The full guide and the API reference live on
[cljdoc](https://cljdoc.org/d/io.github.jramosg/reagent-mui-nested-menu):

- [Menus from data](doc/01-menus-from-data.md): components, item maps, nesting,
  icons, selection and the context menu.
- [React usage](doc/02-react-usage.md): the npm package and JSX examples.
- [Styling](doc/03-styling.md): class hooks and per-item styles.

Public API: the single namespace `reagent-mui-nested-menu.core`.

## Development

Build, test, publish and demo instructions live in
[DEVELOPMENT.md](https://github.com/jramosg/jon-nested-menu/blob/master/DEVELOPMENT.md).

## Credits

Port of [mui-nested-menu](https://github.com/steviebuilds/mui-nested-menu)
by [steviebuilds](https://github.com/steviebuilds).

## License

MIT.
