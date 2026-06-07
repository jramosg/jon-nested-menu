# Development

Build, test, publish and run-the-demos notes for maintainers. Library usage
lives in the [README](README.md).

## Tests

Unit tests cover the data-driven menu builder (no DOM needed) and run on Node
via shadow-cljs:

```bash
npm test   # npx shadow-cljs compile test && node target/node-tests.js
```

## Build

```bash
npm run release:library   # builds dist/cjs (CommonJS) + dist/esm (ESM)
```

## Publish to npm

```bash
npm run release:library
npm publish
```

## Publish to Clojars (Slim / tools.build)

The library is published under the group `io.github.jramosg`, which Clojars
auto-verifies for the GitHub user `jramosg`. Clojars walks you through group
verification on first deploy (https://clojars.org/verify/group). For your own
fork, change `:lib` in the `:build` alias of `deps.edn` to your group, e.g.
`io.github.<your-user>/reagent-mui-nested-menu` or
`net.clojars.<your-user>/reagent-mui-nested-menu`.

1. Create a Clojars account, then a deploy token at
   https://clojars.org/tokens (use the token as the password, not your account
   password).
2. Export credentials in the same shell as the deploy:

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

Bump `:version` in the `:build` alias of `deps.edn` (and the install snippet in
the README) for each release.

## Demos

```bash
cd demo && npm install && npm run start      # ClojureScript + Reagent
cd demo-react && npm install && npm run dev   # React + MUI
```

The CLJS demo release (`cd demo && npm run build`) emits content-hashed assets
into `demo/dist/` via `scripts/build-dist.mjs`, which rewrites `index.html` to
point at the hashed filenames. The `deploy-vps` workflow ships `demo/dist/` and
`demo-react/dist/`.
