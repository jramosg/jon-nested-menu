// Post-process a `shadow-cljs release app` build into a deployable demo/dist/
// with content-hashed assets and an index.html that references them.
//
// shadow already hashes the JS (`:module-hash-names true`); this script hashes
// the hand-written CSS and rewrites index.html so every asset URL changes when
// its content changes — making `Cache-Control: immutable` safe.

import {
  readFileSync,
  writeFileSync,
  mkdirSync,
  rmSync,
  readdirSync,
  copyFileSync,
} from 'node:fs';
import { createHash } from 'node:crypto';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';

const root = join(dirname(fileURLToPath(import.meta.url)), '..');
const pub = join(root, 'public');
const dist = join(root, 'dist');
const jsSrc = join(pub, 'js');
const jsDist = join(dist, 'js');

const hash8 = (buf) =>
  createHash('sha256').update(buf).digest('hex').slice(0, 8);

// Fresh dist/.
rmSync(dist, { recursive: true, force: true });
mkdirSync(jsDist, { recursive: true });

// 1. Resolve the hashed main JS file from shadow's manifest.
const manifest = readFileSync(join(jsSrc, 'manifest.edn'), 'utf8');
const jsMatch = manifest.match(/:output-name\s+"([^"]+)"/);
if (!jsMatch) {
  throw new Error('build-dist: no :output-name found in js/manifest.edn');
}
const jsName = jsMatch[1];

// 2. Copy the built JS (already hashed) into dist/js, minus the manifest.
for (const file of readdirSync(jsSrc)) {
  if (file === 'manifest.edn') continue;
  copyFileSync(join(jsSrc, file), join(jsDist, file));
}

// 3. Hash the CSS and write it under a content-addressed name.
const cssBuf = readFileSync(join(pub, 'jon-nested-menu.css'));
const cssName = `jon-nested-menu.${hash8(cssBuf)}.css`;
writeFileSync(join(dist, cssName), cssBuf);

// 4. Rewrite index.html to point at the hashed assets.
const html = readFileSync(join(pub, 'index.html'), 'utf8')
  .replace('js/app.js', `js/${jsName}`)
  .replace('jon-nested-menu.css', cssName);
writeFileSync(join(dist, 'index.html'), html);

// 4b. Copy static, unhashed assets (favicon) referenced by index.html.
copyFileSync(join(pub, 'favicon.svg'), join(dist, 'favicon.svg'));

console.log(`build-dist: index.html -> js/${jsName}, ${cssName}`);
