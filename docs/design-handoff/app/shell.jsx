/* global React, Icon, StatusBar, StoryArt, Pattern, Avatar, Progress */
/* Rivana · phone shell + shared presentational components */

const { useState, useEffect, useRef, createContext, useContext } = React;

/* ── Navigation context ──────────────────────────────────── */
const Nav = createContext(null);
const useNav = () => useContext(Nav);

/* ── Phone frame ─────────────────────────────────────────── */
function PhoneFrame({ dark, statusDark, children, tabbar, tab, onTab, tabDark }) {
  const sd = statusDark != null ? statusDark : dark;
  return (
    <div className="phone">
      <div className="phone-screen" style={{ backgroundColor: dark ? '#0F1330' : 'var(--bg)' }}>
        <div className="island" />
        <StatusBar dark={sd} />
        {children}
        {tabbar && <TabBar dark={tabDark != null ? tabDark : dark} active={tab} onTab={onTab} />}
        <div className="home-ind" style={{ background: sd ? 'rgba(255,255,255,.6)' : 'rgba(20,17,27,.28)' }} />
      </div>
    </div>
  );
}

const TABS = [
  { id: 'today', label: 'Today', lu: 'House', icon: 'home' },
  { id: 'library', label: 'Library', lu: 'Library', icon: 'book' },
  { id: 'bedtime', label: 'Bedtime', lu: 'MoonStar', icon: 'moon' },
  { id: 'rewards', label: 'Rewards', lu: 'Trophy', icon: 'trophy' },
  { id: 'you', label: 'You', lu: 'UserRound', icon: 'user' },
];

/* Inline a real Lucide icon from the UMD runtime data (exportable SVG, no font). */
function LucideIcon({ name, size = 24, color = 'currentColor', stroke = 2, fill = 'none' }) {
  const L = typeof window !== 'undefined' && window.lucide;
  const node = L && L.icons && L.icons[name];
  if (!node) return null;
  const kids = node[2] || [];
  return React.createElement('svg', {
    width: size, height: size, viewBox: '0 0 24 24', fill,
    stroke: color, strokeWidth: stroke, strokeLinecap: 'round', strokeLinejoin: 'round',
    style: { display: 'block' },
  }, kids.map((c, i) => React.createElement(c[0], Object.assign({ key: i }, c[1]))));
}

function TabBar({ active, onTab, dark }) {
  const useLu = typeof window !== 'undefined' && window.lucide && window.lucide.icons;
  return (
    <nav className={'tabbar' + (dark ? ' is-dark' : '')}>
      {TABS.map((t) => {
        const on = t.id === active;
        const col = on ? (dark ? 'var(--accent-deep)' : 'var(--accent-deep)') : (dark ? 'rgba(255,255,255,.52)' : 'var(--ink-muted)');
        return (
          <button key={t.id} className={'tab' + (on ? ' is-active' : '')} onClick={() => onTab(t.id)}
            style={{ color: col }}>
            <span className="ti">
              {useLu
                ? <LucideIcon name={t.lu} size={23} color={col} stroke={on ? 2.3 : 1.8} />
                : <Icon name={t.icon} size={22} stroke={on ? 2.3 : 1.8} color={col} />}
            </span>
            <span>{t.label}</span>
          </button>
        );
      })}
    </nav>
  );
}

/* ── Reveal-on-mount (staggered entrance) ────────────────── */
function Reveal({ delay = 0, children, style }) {
  return (
    <div style={{ animation: `rv .5s ${delay}ms var(--ease) both`, ...style }}>
      {children}
      <style>{`@keyframes rv{from{opacity:0;transform:translateY(12px)}to{opacity:1;transform:none}}`}</style>
    </div>
  );
}

/* ── Section header ──────────────────────────────────────── */
function SectionHead({ title, fa, action, onAction, dark, style }) {
  return (
    <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between', padding: '0 var(--gutter)', marginBottom: 13, ...style }}>
      <div>
        <h2 className="h3" style={{ color: dark ? '#fff' : 'var(--ink)' }}>{title}</h2>
        {fa && <p className="fa" style={{ fontSize: 13, color: dark ? 'rgba(255,255,255,.5)' : 'var(--ink-muted)', marginTop: 2 }}>{fa}</p>}
      </div>
      {action && (
        <button onClick={onAction} style={{ background: onAction ? 'none' : 'var(--bg-tint)', border: 'none', cursor: onAction ? 'pointer' : 'default', display: 'flex', alignItems: 'center', gap: 3,
          fontFamily: 'var(--font-body)', fontSize: 13, fontWeight: 700, color: dark ? 'rgba(255,255,255,.7)' : 'var(--accent-deep)',
          borderRadius: onAction ? 0 : 'var(--r-pill)', padding: onAction ? 0 : '4px 11px' }}>
          {action} {onAction && <Icon name="chevR" size={15} stroke={2.4} />}
        </button>
      )}
    </div>
  );
}

/* ── Story cover (portrait) ──────────────────────────────── */
function Cover({ story, w = 150, onTap, showProgress, light, motif = true }) {
  const ar = 1.32;
  const badges = window.__tw ? window.__tw.covers : true;
  return (
    <button onClick={onTap} style={{ width: w, background: 'none', border: 'none', padding: 0, cursor: 'pointer', textAlign: 'left', display: 'block' }}>
      <div style={{ width: w, height: w * ar, borderRadius: 'var(--r-md)', overflow: 'hidden', position: 'relative', boxShadow: 'var(--sh)', border: '1px solid rgba(20,17,27,.06)' }}>
        <StoryArt kind={story.scene} />
        {motif && <Pattern motif="paisley" color="#fff" opacity={0.10} fade="bottom" size="120px 120px" />}
        {badges && story.new && (
          <span className="tag tag-saffron" style={{ position: 'absolute', top: 9, left: 9, background: 'var(--saffron)', color: '#fff', fontSize: 10.5, padding: '3px 8px' }}>NEW</span>
        )}
        {badges && (
        <span style={{ position: 'absolute', top: 9, right: 9 }}>
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: 4, padding: '3px 8px', borderRadius: 99, background: 'rgba(15,12,20,.42)', backdropFilter: 'blur(6px)', color: '#fff', fontSize: 10.5, fontWeight: 700 }}>
            <Icon name="clock" size={11} stroke={2.2} color="#fff" /> {story.minutes}m
          </span>
        </span>
        )}
        {showProgress != null && story.progress > 0 && (
          <div style={{ position: 'absolute', left: 9, right: 9, bottom: 9, padding: '6px 9px', borderRadius: 12, background: 'rgba(15,12,20,.5)', backdropFilter: 'blur(8px)', display: 'flex', alignItems: 'center', gap: 8 }}>
            <Icon name="play" size={11} color="#fff" />
            <Progress value={story.progress} height={4} color="#fff" />
          </div>
        )}
      </div>
      <p className="h3" style={{ fontSize: 15.5, marginTop: 9, color: light ? '#fff' : 'var(--ink)', lineHeight: 1.16 }}>{story.title}</p>
      <p className="fa" style={{ fontSize: 12.5, color: light ? 'rgba(255,255,255,.55)' : 'var(--ink-muted)', marginTop: 2 }}>{story.titleFa}</p>
    </button>
  );
}

/* ── Horizontal story list row ───────────────────────────── */
function StoryRow({ story, onTap, right }) {
  return (
    <button onClick={onTap} style={{ display: 'flex', gap: 13, alignItems: 'center', width: '100%', background: 'none', border: 'none', padding: '8px var(--gutter)', cursor: 'pointer', textAlign: 'left' }}>
      <div style={{ width: 60, height: 78, borderRadius: 14, overflow: 'hidden', flexShrink: 0, position: 'relative', boxShadow: 'var(--sh-sm)' }}>
        <StoryArt kind={story.scene} />
      </div>
      <div style={{ flex: 1, minWidth: 0 }}>
        <p className="h3" style={{ fontSize: 16 }}>{story.title}</p>
        <p className="fa" style={{ fontSize: 12.5, color: 'var(--ink-muted)', margin: '1px 0 5px' }}>{story.titleFa}</p>
        <div style={{ display: 'flex', gap: 7, alignItems: 'center' }}>
          <span className={`tag tag-${story.tone}`} style={{ fontSize: 10.5, padding: '3px 8px' }}>{story.collection}</span>
          <span className="micro" style={{ letterSpacing: '.08em' }}>{story.age} · {story.minutes}m</span>
        </div>
      </div>
      {right || <Icon name="chevR" size={18} color="var(--ink-faint)" />}
    </button>
  );
}

/* ── Pill segmented control ──────────────────────────────── */
function Segmented({ options, value, onChange, dark }) {
  return (
    <div style={{ display: 'inline-flex', padding: 3, borderRadius: 99, background: dark ? 'rgba(255,255,255,.10)' : 'var(--bg-tint)', gap: 2 }}>
      {options.map((o) => {
        const on = o.value === value;
        return (
          <button key={o.value} onClick={() => onChange(o.value)} style={{
            border: 'none', cursor: 'pointer', height: 32, padding: '0 14px', borderRadius: 99,
            fontFamily: 'var(--font-body)', fontSize: 13, fontWeight: 700,
            background: on ? (dark ? '#fff' : 'var(--surface)') : 'transparent',
            color: on ? (dark ? 'var(--lapis-deep)' : 'var(--ink)') : (dark ? 'rgba(255,255,255,.6)' : 'var(--ink-muted)'),
            boxShadow: on ? 'var(--sh-xs)' : 'none', transition: 'all .16s var(--ease)',
          }}>{o.label}</button>
        );
      })}
    </div>
  );
}

/* ── Top bar for pushed screens (back / title / action) ──── */
function PushBar({ onBack, title, action, dark, transparent }) {
  const c = dark ? '#fff' : 'var(--ink)';
  return (
    <div style={{ position: 'relative', zIndex: 30, display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '8px var(--gutter)', gap: 10 }}>
      <button className="iconbtn" onClick={onBack} style={transparent || dark ? { background: dark ? 'rgba(255,255,255,.14)' : 'rgba(255,255,255,.7)', backdropFilter: 'blur(8px)', border: dark ? '1px solid rgba(255,255,255,.2)' : '1px solid rgba(20,17,27,.06)', color: c } : null}>
        <Icon name="chevL" size={21} stroke={2.2} color={c} />
      </button>
      {title && <span className="h3" style={{ fontSize: 16, color: c, flex: 1, textAlign: 'center' }}>{title}</span>}
      {action || <span style={{ width: 42 }} />}
    </div>
  );
}

/* ── Full-screen motif background (pinned behind a screen's scroll) ── */
function ScreenBg({ motif = 'paisley', color = 'var(--ink)', opacity = 0.05, fade = 'top', size, height }) {
  return <Pattern motif={motif} color={color} opacity={opacity} fade={fade} size={size}
    style={{ zIndex: 0, height: height || '100%' }} />;
}

Object.assign(window, { Nav, useNav, PhoneFrame, TabBar, TABS, Reveal, SectionHead, Cover, StoryRow, Segmented, PushBar, ScreenBg });
