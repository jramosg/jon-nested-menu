# React usage

The npm package wraps the Reagent components as real React components, so a
plain React app uses them with no ClojureScript on its side.

## Install

```bash
npm install reagent-mui-nested-menu
```

React, MUI and Emotion are peer dependencies you already have in a React app.

## Props

The wrapper converts your JS props for you: `buttonProps` becomes
`:button-props`, `leftIcon` becomes `:left-icon`, and so on. Pass icons as JSX
elements and `items` as plain objects. The item keys match the
[Menus from data](01-menus-from-data.md) reference, in camelCase
(`leftIcon`, `rightIcon`, `renderLabel`).

The package ships dual ESM and CommonJS, so named imports work in
Vite/webpack/esbuild and `require()` works in Node.

## Example

```jsx
import { NestedMenu, ContextMenu } from 'reagent-mui-nested-menu';

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

<NestedMenu buttonProps={{ label: 'Open' }} items={items} direction="left" />;

<ContextMenu items={items}>
  <div>Right-click here</div>
</ContextMenu>;
```

## Exports

`NestedMenu`, `ContextMenu`, `NestedMenuItem`, `IconMenuItem`,
`ChevronRight`, `ChevronDown`. A runnable example lives in `demo-react/`.
