import React, { useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { NestedMenu, ContextMenu } from 'reagent-mui-nested-menu';
import './styles.css';

const REPO_URL = 'https://github.com/jramosg/jon-nested-menu';
const CLOJARS_URL = 'https://clojars.org/io.github.jramosg/jon-nested-menu';
const NPM_URL = 'https://www.npmjs.com/package/jon-nested-menu';
const COFFEE_URL = 'https://www.buymeacoffee.com/jramosg';

// ---------------------------------------------------------------------------
// Theme (GitHub palette, light + dark) to match jonramos.dev
// ---------------------------------------------------------------------------

const initialTheme = () =>
  localStorage.getItem('jnm-theme') ??
  (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');

const toggled = (t) => (t === 'dark' ? 'light' : 'dark');

const muiTheme = (mode) =>
  createTheme({
    palette:
      mode === 'dark'
        ? {
            mode: 'dark',
            primary: { main: '#7ee787', contrastText: '#0d1117' },
            background: { default: '#0d1117', paper: '#161b22' },
            text: { primary: '#e6edf3', secondary: '#aeb4bc' },
            divider: 'rgba(240,246,252,0.1)',
          }
        : {
            mode: 'light',
            primary: { main: '#1a7f37', contrastText: '#ffffff' },
            background: { default: '#fafbfc', paper: '#ffffff' },
            text: { primary: '#1f2328', secondary: '#57606a' },
            divider: 'rgba(31,35,40,0.1)',
          },
    shape: { borderRadius: 8 },
    typography: {
      fontFamily: "'JetBrains Mono', ui-monospace, monospace",
      button: { textTransform: 'none', fontWeight: 600, letterSpacing: '-0.01em' },
    },
  });

// ---------------------------------------------------------------------------
// Custom inline SVG icons (Feather-style, inherit currentColor)
// ---------------------------------------------------------------------------

const ICON_PATHS = {
  file: <><path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z" /><path d="M13 2v7h7" /></>,
  folder: <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />,
  share: <><circle cx="18" cy="5" r="3" /><circle cx="6" cy="12" r="3" /><circle cx="18" cy="19" r="3" /><line x1="8.59" y1="13.51" x2="15.42" y2="17.49" /><line x1="15.41" y1="6.51" x2="8.59" y2="10.49" /></>,
  copy: <><rect x="9" y="9" width="13" height="13" rx="2" ry="2" /><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" /></>,
  mail: <><path d="M4 4h16a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z" /><polyline points="22,6 12,13 2,6" /></>,
  download: <><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" /><polyline points="7,10 12,15 17,10" /><line x1="12" y1="15" x2="12" y2="3" /></>,
  fileText: <><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" /><polyline points="14,2 14,8 20,8" /><line x1="16" y1="13" x2="8" y2="13" /><line x1="16" y1="17" x2="8" y2="17" /></>,
  code: <><polyline points="16,18 22,12 16,6" /><polyline points="8,6 2,12 8,18" /></>,
  image: <><rect x="3" y="3" width="18" height="18" rx="2" ry="2" /><circle cx="8.5" cy="8.5" r="1.5" /><polyline points="21,15 16,10 5,21" /></>,
  user: <><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" /><circle cx="12" cy="7" r="4" /></>,
  users: <><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" /><circle cx="9" cy="7" r="4" /><path d="M23 21v-2a4 4 0 0 0-3-3.87" /><path d="M16 3.13a4 4 0 0 1 0 7.75" /></>,
  shield: <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />,
  trash: <><polyline points="3,6 5,6 21,6" /><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" /></>,
  card: <><rect x="1" y="4" width="22" height="16" rx="2" ry="2" /><line x1="1" y1="10" x2="23" y2="10" /></>,
  check: <polyline points="20,6 9,17 4,12" />,
  settings: <><circle cx="12" cy="12" r="3" /><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z" /></>,
  globe: <><circle cx="12" cy="12" r="10" /><line x1="2" y1="12" x2="22" y2="12" /><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" /></>,
  lock: <><rect x="3" y="11" width="18" height="11" rx="2" ry="2" /><path d="M7 11V7a5 5 0 0 1 10 0v4" /></>,
  logout: <><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" /><polyline points="16,17 21,12 16,7" /><line x1="21" y1="12" x2="9" y2="12" /></>,
  bell: <><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" /><path d="M13.73 21a2 2 0 0 1-3.46 0" /></>,
  zap: <polygon points="13,2 3,14 12,14 11,22 21,10 12,10" />,
  scissors: <><circle cx="6" cy="6" r="3" /><circle cx="6" cy="18" r="3" /><line x1="20" y1="4" x2="8.12" y2="15.88" /><line x1="14.47" y1="14.48" x2="20" y2="20" /><line x1="8.12" y1="8.12" x2="12" y2="12" /></>,
  clipboard: <><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2" /><rect x="8" y="2" width="8" height="4" rx="1" ry="1" /></>,
  edit: <><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" /><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" /></>,
  rocket: <><path d="M4.5 16.5c-1.5 1.26-2 5-2 5s3.74-.5 5-2c.71-.84.7-2.13-.09-2.91a2.18 2.18 0 0 0-2.91-.09z" /><path d="M12 15l-3-3a22 22 0 0 1 2-3.95A12.88 12.88 0 0 1 22 2c0 2.72-.78 7.5-6 11a22.35 22.35 0 0 1-4 2z" /><path d="M9 12H4s.55-3.03 2-4c1.62-1.08 5 0 5 0" /><path d="M12 15v5s3.03-.55 4-2c1.08-1.62 0-5 0-5" /></>,
  github: <path d="M9 19c-5 1.5-5-2.5-7-3m14 5v-3.87a3.37 3.37 0 0 0-.94-2.61c3.14-.35 6.44-1.54 6.44-7A5.44 5.44 0 0 0 20 4.77 5.07 5.07 0 0 0 19.91 1S18.73.65 16 2.48a13.38 13.38 0 0 0-7 0C6.27.65 5.09 1 5.09 1A5.07 5.07 0 0 0 5 4.77a5.44 5.44 0 0 0-1.5 3.78c0 5.42 3.3 6.61 6.44 7A3.37 3.37 0 0 0 9 18.13V22" />,
  package: <><line x1="16.5" y1="9.4" x2="7.5" y2="4.21" /><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z" /><polyline points="3.27,6.96 12,12.01 20.73,6.96" /><line x1="12" y1="22.08" x2="12" y2="12" /></>,
  sun: <><circle cx="12" cy="12" r="5" /><line x1="12" y1="1" x2="12" y2="3" /><line x1="12" y1="21" x2="12" y2="23" /><line x1="4.22" y1="4.22" x2="5.64" y2="5.64" /><line x1="18.36" y1="18.36" x2="19.78" y2="19.78" /><line x1="1" y1="12" x2="3" y2="12" /><line x1="21" y1="12" x2="23" y2="12" /><line x1="4.22" y1="19.78" x2="5.64" y2="18.36" /><line x1="18.36" y1="5.64" x2="19.78" y2="4.22" /></>,
  moon: <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />,
  coffee: <><path d="M18 8h1a4 4 0 0 1 0 8h-1" /><path d="M2 8h16v9a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V8z" /><line x1="6" y1="1" x2="6" y2="4" /><line x1="10" y1="1" x2="10" y2="4" /><line x1="14" y1="1" x2="14" y2="4" /></>,
};

const Icon = ({ name, size = 18, color = 'currentColor', mr = true }) => (
  <svg
    width={size}
    height={size}
    viewBox="0 0 24 24"
    fill="none"
    stroke={color}
    strokeWidth={1.8}
    strokeLinecap="round"
    strokeLinejoin="round"
    aria-hidden
    style={{ flex: '0 0 auto', marginRight: mr ? 10 : 0 }}
  >
    {ICON_PATHS[name]}
  </svg>
);

const Kbd = ({ children }) => (
  <span
    style={{
      fontFamily: 'var(--font-mono)',
      fontSize: '0.7rem',
      padding: '2px 6px',
      borderRadius: 6,
      marginLeft: 16,
      color: 'var(--color-text-subtle)',
      border: '1px solid var(--color-border)',
      background: 'var(--color-bg-surface)',
    }}
  >
    {children}
  </span>
);

const twoLine = (title, subtitle, badge, badgeColor) => () => (
  <span style={{ display: 'flex', flexDirection: 'column', padding: '0 8px' }}>
    <span style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
      <span style={{ fontWeight: 600 }}>{title}</span>
      {badge && (
        <span
          style={{
            fontSize: '0.62rem',
            fontWeight: 700,
            letterSpacing: '0.04em',
            textTransform: 'uppercase',
            padding: '1px 7px',
            borderRadius: 999,
            color: '#0b0712',
            background: badgeColor || 'var(--color-primary)',
          }}
        >
          {badge}
        </span>
      )}
    </span>
    <span style={{ fontSize: '0.76rem', color: 'var(--color-text-subtle)', marginTop: 2 }}>
      {subtitle}
    </span>
  </span>
);

// ---------------------------------------------------------------------------
// Menu data
// ---------------------------------------------------------------------------

const fileItems = [
  { label: 'New file', leftIcon: <Icon name="file" />, rightIcon: <Kbd>⌘N</Kbd> },
  { label: 'New folder', leftIcon: <Icon name="folder" />, rightIcon: <Kbd>⌘⇧N</Kbd> },
  {
    label: 'Share',
    leftIcon: <Icon name="share" />,
    items: [
      { label: 'Copy link', leftIcon: <Icon name="copy" /> },
      { label: 'Email', leftIcon: <Icon name="mail" /> },
    ],
  },
  {
    label: 'Export',
    leftIcon: <Icon name="download" />,
    items: [
      { label: 'PDF', leftIcon: <Icon name="fileText" /> },
      { label: 'Markdown', leftIcon: <Icon name="code" /> },
      { label: 'Image', leftIcon: <Icon name="image" /> },
    ],
  },
];

const accountItems = [
  { label: 'Profile', leftIcon: <Icon name="user" /> },
  { label: 'Notifications', leftIcon: <Icon name="bell" /> },
  {
    label: 'Workspace',
    leftIcon: <Icon name="users" />,
    items: [
      { label: 'Members', leftIcon: <Icon name="users" /> },
      { label: 'Permissions', leftIcon: <Icon name="lock" /> },
      {
        label: 'Billing',
        leftIcon: <Icon name="card" />,
        items: [
          { label: 'Invoices', leftIcon: <Icon name="fileText" /> },
          { label: 'Payment methods', leftIcon: <Icon name="card" /> },
        ],
      },
    ],
  },
  { label: 'Sign out', leftIcon: <Icon name="logout" />, sx: { color: '#f78166' } },
];

const deepItems = [
  {
    label: 'Organization',
    leftIcon: <Icon name="globe" />,
    items: [
      {
        label: 'Teams',
        leftIcon: <Icon name="users" />,
        items: [
          {
            label: 'Engineering',
            leftIcon: <Icon name="code" />,
            items: [
              { label: 'Frontend' },
              { label: 'Backend' },
              { label: 'Platform' },
            ],
          },
          { label: 'Design', leftIcon: <Icon name="image" /> },
        ],
      },
      {
        label: 'Settings',
        leftIcon: <Icon name="settings" />,
        items: [
          { label: 'General' },
          { label: 'Security', leftIcon: <Icon name="shield" /> },
        ],
      },
    ],
  },
];

const renderLabelItems = [
  {
    leftIcon: <Icon name="rocket" />,
    renderLabel: twoLine('Pro plan', 'Everything in Team, plus SSO', 'popular', '#7ee787'),
  },
  { leftIcon: <Icon name="zap" />, renderLabel: twoLine('Team plan', 'Up to 20 collaborators') },
  { leftIcon: <Icon name="user" />, renderLabel: twoLine('Free', 'For personal projects') },
];

const contextItems = [
  { label: 'Copy', leftIcon: <Icon name="copy" />, rightIcon: <Kbd>⌘C</Kbd> },
  { label: 'Cut', leftIcon: <Icon name="scissors" />, rightIcon: <Kbd>⌘X</Kbd> },
  { label: 'Paste', leftIcon: <Icon name="clipboard" />, rightIcon: <Kbd>⌘V</Kbd> },
  {
    label: 'Transform',
    leftIcon: <Icon name="edit" />,
    items: [
      { label: 'Rename', leftIcon: <Icon name="edit" /> },
      { label: 'Duplicate', leftIcon: <Icon name="copy" /> },
    ],
  },
  { label: 'Delete', leftIcon: <Icon name="trash" />, sx: { color: '#f78166' }, rightIcon: <Kbd>⌫</Kbd> },
];

const PRIORITY = {
  low: { label: 'Low', color: '#3fb950' },
  medium: { label: 'Medium', color: '#d29922' },
  high: { label: 'High', color: '#db6d28' },
  critical: { label: 'Critical', color: '#f78166' },
};

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

const buttonProps = (label, iconName, variant) => ({
  label,
  variant,
  disableElevation: true,
  startIcon: <Icon name={iconName} size={17} />,
  sx: { borderRadius: '8px', px: '16px', py: '8px' },
});

const Card = ({ title, blurb, snippet, children }) => (
  <article className="card">
    <div className="card-head">
      <h2>{title}</h2>
      <p className="card-blurb">{blurb}</p>
    </div>
    <div className="card-stage">{children}</div>
    {snippet && (
      <pre className="snippet">
        <code>{snippet}</code>
      </pre>
    )}
  </article>
);

const TopLink = ({ href, label, icon }) => (
  <a className="top-link" href={href} target="_blank" rel="noopener noreferrer">
    <Icon name={icon} size={15} mr={false} />
    <span>{label}</span>
  </a>
);

const TopBar = ({ theme, onToggle }) => {
  const dark = theme === 'dark';
  return (
    <div className="topbar">
      <span className="brand">
        <span className="slash">//</span> jon-nested-menu
      </span>
      <div className="topbar-links">
        <TopLink href={REPO_URL} label="GitHub" icon="github" />
        <TopLink href={CLOJARS_URL} label="Clojars" icon="package" />
        <TopLink href={NPM_URL} label="npm" icon="package" />
        <TopLink href={COFFEE_URL} label="Coffee" icon="coffee" />
        <button className="theme-toggle" type="button" aria-label="Toggle theme" onClick={onToggle}>
          <Icon name={dark ? 'sun' : 'moon'} size={17} mr={false} />
        </button>
      </div>
    </div>
  );
};

function App() {
  const [theme, setTheme] = useState(initialTheme);
  const [events, setEvents] = useState([]);
  const [priority, setPriority] = useState('high');

  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('jnm-theme', theme);
  }, [theme]);

  const theme021 = useMemo(() => muiTheme(theme), [theme]);

  const log = (label) =>
    setEvents((prev) =>
      [...prev, `${new Date().toLocaleTimeString()}  ·  ${label || 'item'}`].slice(-14),
    );

  const track = (items) =>
    items.map((item) => ({
      ...item,
      callback: (e, it) => {
        log(item.label);
        item.callback?.(e, it);
      },
      items: item.items ? track(item.items) : undefined,
    }));

  const priorityItems = track(
    Object.entries(PRIORITY).map(([k, { label, color }]) => ({
      label,
      value: k,
      leftIcon: <Icon name="zap" color={color} />,
      rightIcon: k === priority ? <Icon name="check" color={color} /> : undefined,
      callback: () => setPriority(k),
    })),
  );

  return (
    <ThemeProvider theme={theme021}>
      <TopBar theme={theme} onToggle={() => setTheme(toggled)} />
      <div className="shell">
        <header className="hero">
          <span className="eyebrow">React · MUI · ClojureScript</span>
          <h1>
            Nested menus,
            <br />
            <span className="accent">from data.</span>
          </h1>
          <p className="lead">
            A nested MUI menu library for React and Reagent — dropdowns, a
            right-click context menu, per-item icons, custom labels, keyboard
            navigation and selection state, all from plain data.
          </p>
          <div className="hero-actions">
            <NestedMenu buttonProps={buttonProps('File', 'file', 'contained')} items={track(fileItems)} />
            <NestedMenu
              buttonProps={buttonProps('Account', 'user', 'outlined')}
              direction="left"
              items={track(accountItems)}
            />
          </div>
          <div className="install">
            <code>
              <span className="tok">npm i</span> jon-nested-menu
            </code>
            <code>
              <span className="tok">clojars</span> io.github.jramosg/jon-nested-menu
            </code>
          </div>
        </header>

        <section>
          <h2 className="section-title">Showcase</h2>
          <div className="grid">
            <Card
              title="Icons + shortcuts"
              blurb="Left icons, sub-menus and custom right-side content."
              snippet={'{ label: "New file",\n  leftIcon: <Icon/>,\n  rightIcon: <Kbd>⌘N</Kbd> }'}
            >
              <NestedMenu buttonProps={buttonProps('File menu', 'file', 'outlined')} items={track(fileItems)} />
            </Card>

            <Card
              title="Opens to the left"
              blurb='Set direction="left" for edge-aligned layouts.'
              snippet={'<NestedMenu\n  direction="left"\n  items={accountItems} />'}
            >
              <NestedMenu
                buttonProps={buttonProps('Account', 'user', 'outlined')}
                direction="left"
                items={track(accountItems)}
              />
            </Card>

            <Card
              title="Selection state"
              blurb={`Live selection — current: ${PRIORITY[priority].label}.`}
              snippet={'<NestedMenu items={items}\n             value={priority} />'}
            >
              <NestedMenu
                buttonProps={buttonProps(`Priority: ${PRIORITY[priority].label}`, 'zap', 'outlined')}
                value={priority}
                items={priorityItems}
              />
            </Card>

            <Card
              title="Deep navigation"
              blurb="Four levels deep with hover-to-open submenus."
              snippet={'{ label: "Teams",\n  items: [{ label: "Engineering",\n            items: [...] }] }'}
            >
              <NestedMenu buttonProps={buttonProps('Browse org', 'globe', 'outlined')} items={track(deepItems)} />
            </Card>

            <Card
              title="Disabled + open delay"
              blurb="Disable items and delay submenu opening on hover."
              snippet={'{ label: "Archived", disabled: true }\n{ label: "Reports", delay: 350 }'}
            >
              <NestedMenu
                buttonProps={buttonProps('States', 'settings', 'outlined')}
                items={track([
                  { label: 'Active', leftIcon: <Icon name="check" /> },
                  { label: 'Archived (disabled)', leftIcon: <Icon name="lock" />, disabled: true },
                  {
                    label: 'Reports',
                    leftIcon: <Icon name="fileText" />,
                    delay: 350,
                    items: [{ label: 'Opens after 350ms', leftIcon: <Icon name="zap" /> }],
                  },
                ])}
              />
            </Card>

            <Card
              title="Custom labels"
              blurb="Render any node as the label — titles, subtitles, badges."
              snippet={'{ renderLabel:\n  () => <span>title + subtitle</span> }'}
            >
              <NestedMenu buttonProps={buttonProps('Choose plan', 'rocket', 'outlined')} items={track(renderLabelItems)} />
            </Card>
          </div>
        </section>

        <section className="context-row">
          <article className="card">
            <div className="card-head">
              <h2>Right-click context menu</h2>
              <p className="card-blurb">
                ContextMenu wraps any content and opens at the pointer. Delete is
                tinted via per-item sx.
              </p>
            </div>
            <ContextMenu items={track(contextItems)}>
              <div className="context-target">
                <div className="context-glow" />
                <span>right-click anywhere in this canvas</span>
              </div>
            </ContextMenu>
          </article>

          <article className="card">
            <div className="card-head log-head">
              <h2>Event log</h2>
              <button className="ghost-btn" type="button" onClick={() => setEvents([])}>
                clear
              </button>
            </div>
            <ul className="log">
              {events.length === 0 ? (
                <li className="log-empty">interact with a menu to see events…</li>
              ) : (
                [...events].reverse().map((e, i) => (
                  <li key={i}>
                    <span className="dot" />
                    {e}
                  </li>
                ))
              )}
            </ul>
          </article>
        </section>
      </div>

      <footer className="footer">
        <span>jon-nested-menu</span>
        <span className="sep">·</span>
        <a href={REPO_URL} target="_blank" rel="noopener noreferrer">
          GitHub
        </a>
        <span className="sep">·</span>
        <a href={CLOJARS_URL} target="_blank" rel="noopener noreferrer">
          Clojars
        </a>
        <span className="sep">·</span>
        <a href={NPM_URL} target="_blank" rel="noopener noreferrer">
          npm
        </a>
        <span className="sep">·</span>
        <a href={COFFEE_URL} target="_blank" rel="noopener noreferrer">
          Buy me a coffee
        </a>
        <span className="sep">·</span>
        <span>built with React + MUI</span>
      </footer>
    </ThemeProvider>
  );
}

createRoot(document.getElementById('root')).render(<App />);
