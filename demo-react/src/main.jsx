import React, { useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import CssBaseline from '@mui/material/CssBaseline';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { NestedMenu, ContextMenu } from 'jon-nested-menu';
import './styles.css';

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
      fontFamily: 'ui-monospace, monospace',
      fontSize: '0.7rem',
      padding: '2px 6px',
      borderRadius: 6,
      marginLeft: 16,
      color: 'rgba(255,255,255,0.6)',
      border: '1px solid rgba(255,255,255,0.14)',
      background: 'rgba(255,255,255,0.04)',
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
            background: badgeColor || '#a78bfa',
          }}
        >
          {badge}
        </span>
      )}
    </span>
    <span style={{ fontSize: '0.76rem', color: 'rgba(255,255,255,0.5)', marginTop: 2 }}>
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
  { label: 'Sign out', leftIcon: <Icon name="logout" />, sx: { color: '#fb7185' } },
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
  { leftIcon: <Icon name="rocket" />, renderLabel: twoLine('Pro plan', 'Everything in Team, plus SSO', 'popular', '#a78bfa') },
  { leftIcon: <Icon name="zap" />, renderLabel: twoLine('Team plan', 'Up to 20 collaborators') },
  { leftIcon: <Icon name="user" />, renderLabel: twoLine('Free', 'For personal projects') },
];

const stateItems = [
  { label: 'Active', leftIcon: <Icon name="check" /> },
  { label: 'Archived (disabled)', leftIcon: <Icon name="lock" />, disabled: true },
  {
    label: 'Reports',
    leftIcon: <Icon name="fileText" />,
    delay: 350,
    items: [{ label: 'Opens after 350ms', leftIcon: <Icon name="zap" /> }],
  },
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
  { label: 'Delete', leftIcon: <Icon name="trash" />, rightIcon: <Kbd>⌫</Kbd>, sx: { color: '#fb7185' } },
];

const PRIORITY = {
  low: { label: 'Low', color: '#34d399' },
  medium: { label: 'Medium', color: '#fbbf24' },
  high: { label: 'High', color: '#fb923c' },
  critical: { label: 'Critical', color: '#fb7185' },
};

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

const buttonProps = (label, iconName, variant = 'contained') => ({
  label,
  variant,
  disableElevation: true,
  startIcon: <Icon name={iconName} size={17} mr={false} />,
  sx: { textTransform: 'none', fontWeight: 600, borderRadius: '10px', px: '16px', py: '8px' },
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

const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: { main: '#8b5cf6' },
    background: { paper: '#171221' },
    divider: 'rgba(255,255,255,0.08)',
  },
  shape: { borderRadius: 12 },
  typography: { fontFamily: 'Inter, system-ui, sans-serif' },
});

function App() {
  const [events, setEvents] = useState([]);
  const [priority, setPriority] = useState('high');

  const track = useMemo(() => {
    const wrap = (items) =>
      items.map((item) => ({
        ...item,
        callback: (e, it) => {
          setEvents((prev) =>
            [`${new Date().toLocaleTimeString()}  ·  ${item.label ?? 'item'}`, ...prev].slice(0, 14)
          );
          item.callback?.(e, it);
        },
        items: item.items ? wrap(item.items) : undefined,
      }));
    return wrap;
  }, []);

  const priorityItems = track(
    Object.entries(PRIORITY).map(([k, { label, color }]) => ({
      label,
      value: k,
      leftIcon: <Icon name="zap" color={color} />,
      rightIcon: k === priority ? <Icon name="check" color={color} /> : undefined,
      callback: () => setPriority(k),
    }))
  );

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <div className="shell">
        <header className="hero">
          <span className="eyebrow">React · MUI</span>
          <h1>jon-nested-menu</h1>
          <p className="lead">
            Deeply nested MUI menus with icons, custom labels, keyboard navigation,
            selection state and a right-click context menu — the same component,
            consumed from plain React.
          </p>
          <div className="hero-actions">
            <NestedMenu buttonProps={buttonProps('File', 'file')} items={track(fileItems)} />
            <NestedMenu
              buttonProps={buttonProps('Account', 'user', 'outlined')}
              direction="left"
              items={track(accountItems)}
            />
          </div>
        </header>

        <section className="grid">
          <Card
            title="Icons + shortcuts"
            blurb="Left icons, sub-menus and custom right-side content."
            snippet={'{ label: "New file",\n  leftIcon: <Icon/>,\n  rightIcon: <Kbd>⌘N</Kbd> }'}
          >
            <NestedMenu buttonProps={buttonProps('Open file menu', 'file')} items={track(fileItems)} />
          </Card>

          <Card
            title="Opens to the left"
            blurb='Set direction="left" for edge-aligned layouts.'
            snippet={'<NestedMenu direction="left"\n  items={accountItems} />'}
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
            snippet={'<NestedMenu items={items}\n  value={priority} />'}
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
            <NestedMenu buttonProps={buttonProps('Browse org', 'globe')} items={track(deepItems)} />
          </Card>

          <Card
            title="Disabled + open delay"
            blurb="Disable items and delay submenu opening on hover."
            snippet={'{ label: "Archived", disabled: true }\n{ label: "Reports", delay: 350 }'}
          >
            <NestedMenu buttonProps={buttonProps('States', 'settings')} items={track(stateItems)} />
          </Card>

          <Card
            title="Custom labels (renderLabel)"
            blurb="Render any JSX as the label — titles, subtitles, badges."
            snippet={'{ renderLabel: () =>\n    <span>title + subtitle</span> }'}
          >
            <NestedMenu buttonProps={buttonProps('Choose plan', 'rocket')} items={track(renderLabelItems)} />
          </Card>
        </section>

        <section className="context-row">
          <article className="card context-card">
            <div className="card-head">
              <h2>Right-click context menu</h2>
              <p className="card-blurb">
                ContextMenu wraps any content and opens at the pointer. Delete is tinted via per-item sx.
              </p>
            </div>
            <ContextMenu items={track(contextItems)}>
              <div className="context-target">
                <div className="context-glow" />
                <span>Right-click anywhere in this canvas</span>
              </div>
            </ContextMenu>
          </article>

          <article className="card log-card">
            <div className="card-head log-head">
              <h2>Event log</h2>
              <button className="ghost-btn" type="button" onClick={() => setEvents([])}>
                Clear
              </button>
            </div>
            <ul className="log">
              {events.length === 0 ? (
                <li className="log-empty">Interact with a menu to see events…</li>
              ) : (
                events.map((e, i) => (
                  <li key={`${e}-${i}`}>
                    <span className="dot" />
                    {e}
                  </li>
                ))
              )}
            </ul>
          </article>
        </section>
      </div>
    </ThemeProvider>
  );
}

createRoot(document.getElementById('root')).render(<App />);
