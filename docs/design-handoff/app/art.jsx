/* global React, Icon */
/* Rivana · art + chrome primitives */

/* ── Persian pattern overlay ─────────────────────────────── */
function Pattern({ motif = 'paisley', color = '#14111B', opacity = 0.06, fade, size, style }) {
  const fadeBg =
    fade === 'top'    ? 'linear-gradient(180deg, black 0%, transparent 62%)' :
    fade === 'bottom' ? 'linear-gradient(0deg, black 0%, transparent 62%)' :
    fade === 'tr'     ? 'radial-gradient(circle at 100% 0%, black 0%, transparent 72%)' :
    fade === 'bl'     ? 'radial-gradient(circle at 0% 100%, black 0%, transparent 72%)' :
    fade === 'edges'  ? 'radial-gradient(ellipse at center, transparent 28%, black 78%)' : null;
  const motifSrc = (typeof window !== 'undefined' && window.__resources && window.__resources['pat_' + motif])
    || `app/patterns/${motif}.svg`;
  const url = `url('${motifSrc}')`;
  const maskImage = fadeBg ? `${url}, ${fadeBg}` : url;
  const mSize = size || (motif === 'vine' ? '240px 120px' : '132px 132px');
  return (
    <div aria-hidden="true" style={{
      position: 'absolute', inset: 0, pointerEvents: 'none', zIndex: 0,
      background: color, opacity,
      WebkitMaskImage: maskImage, maskImage,
      WebkitMaskRepeat: fadeBg ? 'repeat, no-repeat' : 'repeat',
      maskRepeat: fadeBg ? 'repeat, no-repeat' : 'repeat',
      WebkitMaskSize: fadeBg ? `${mSize}, 100% 100%` : mSize,
      maskSize: fadeBg ? `${mSize}, 100% 100%` : mSize,
      WebkitMaskComposite: 'source-in', maskComposite: 'intersect',
      ...style,
    }} />
  );
}

/* ── Story scene illustrations (CSS) ─────────────────────── */
function StoryArt({ kind, style }) {
  switch (kind) {
    case 'night':
      return <div className="scene scene-night" style={style}><div className="mt-back" /><div className="moon" /><div className="mt" /></div>;
    case 'dawn':
      return (
        <div className="scene scene-dawn" style={style}>
          <div className="sun" />
          <div className="cy" style={{ left: '6%',  width: '14%', height: '50%' }} />
          <div className="cy" style={{ left: '22%', width: '14%', height: '64%' }} />
          <div className="cy" style={{ left: '62%', width: '14%', height: '54%' }} />
          <div className="cy" style={{ right: '6%', width: '14%', height: '60%' }} />
        </div>
      );
    case 'vase':
      return (
        <div className="scene scene-vase" style={style}>
          <div className="stem" style={{ bottom: '46%', height: '30%' }} />
          <div className="leaf" style={{ left: '40%', bottom: '70%', transform: 'rotate(-30deg)' }} />
          <div className="leaf" style={{ left: '52%', bottom: '76%', transform: 'rotate(20deg)' }} />
          <div className="leaf" style={{ left: '46%', bottom: '64%', transform: 'rotate(-10deg)' }} />
          <div className="vase" />
        </div>
      );
    case 'hills':
      return (
        <div className="scene scene-hills" style={style}>
          <div className="h2" /><div className="h1" />
          <svg className="bird" viewBox="0 0 40 12"><path d="M0 8 Q 8 0, 12 8 T 24 8 T 40 8" stroke="#14111B" strokeWidth="1.4" fill="none" strokeLinecap="round" /></svg>
        </div>
      );
    case 'sea':
      return <div className="scene scene-sea" style={style}><div className="sail" /><div className="mast" /><div className="boat" /></div>;
    case 'flame':
      return <div className="scene scene-flame" style={style}><div className="fire" /></div>;
    case 'lullaby':
      return <div className="scene scene-lull" style={style}><div className="stars" /><div className="moon" /></div>;
    default:
      return <div className="scene scene-dawn" style={style} />;
  }
}

/* ── Avatar (coloured initial) ───────────────────────────── */
const TONE = {
  lapis: 'var(--lapis)', saffron: 'var(--saffron)', mint: 'var(--mint)',
  lilac: 'var(--lilac)', sun: 'var(--sun)', rose: 'var(--rose)',
};
function Avatar({ name, tone = 'lapis', size = 'md', ring, style }) {
  const initial = String(name || 'R').trim().charAt(0).toUpperCase();
  const cls = 'avatar' + (size === 'sm' ? ' avatar-sm' : size === 'lg' ? ' avatar-lg' : size === 'xl' ? ' avatar-xl' : '');
  return <span className={cls} style={{ background: TONE[tone] || TONE.lapis, boxShadow: ring ? `0 0 0 3px var(--surface), 0 0 0 5px ${TONE[tone]}` : undefined, ...style }}>{initial}</span>;
}

/* ── Progress ────────────────────────────────────────────── */
function Progress({ value = 0.4, color, dark = false, height = 6, style }) {
  return (
    <div className={'progress' + (dark ? ' on-dark' : '')} style={{ height, ...style }}>
      <span style={{ width: `${Math.max(0, Math.min(1, value)) * 100}%`, background: color || 'var(--accent)' }} />
    </div>
  );
}

/* ── Waveform (decorative, animated when playing) ────────── */
function Waveform({ playing = false, color = 'currentColor', bars = 38, height = 34, progress = 0.5 }) {
  const seed = [5,9,16,11,20,28,18,12,24,30,22,14,8,18,26,33,24,15,10,21,29,19,13,7,17,25,31,23,16,9,14,22,28,20,12,18,11,6];
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 2.5, height, width: '100%' }}>
      {Array.from({ length: bars }).map((_, i) => {
        const h = (seed[i % seed.length] / 33) * height;
        const passed = i / bars < progress;
        return <span key={i} style={{
          flex: 1, height: Math.max(3, h), borderRadius: 99,
          background: color, opacity: passed ? 1 : 0.34,
          animation: playing ? `wv 1s ${(i % 7) * 0.09}s ease-in-out infinite alternate` : 'none',
          transformOrigin: 'center',
        }} />;
      })}
      <style>{`@keyframes wv { from { transform: scaleY(.5); } to { transform: scaleY(1.18); } }`}</style>
    </div>
  );
}

/* ── iOS status bar (tintable) ───────────────────────────── */
function StatusBar({ dark = false, time = '9:41' }) {
  const c = dark ? '#fff' : '#14111B';
  return (
    <div className="statusbar" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '17px 34px 0' }}>
      <span style={{ fontFamily: '-apple-system, system-ui', fontWeight: 600, fontSize: 16, color: c, letterSpacing: '.02em' }}>{time}</span>
      <span style={{ display: 'flex', alignItems: 'center', gap: 7 }}>
        <svg width="18" height="12" viewBox="0 0 19 12"><rect x="0" y="7.5" width="3.2" height="4.5" rx=".7" fill={c}/><rect x="4.8" y="5" width="3.2" height="7" rx=".7" fill={c}/><rect x="9.6" y="2.5" width="3.2" height="9.5" rx=".7" fill={c}/><rect x="14.4" y="0" width="3.2" height="12" rx=".7" fill={c}/></svg>
        <svg width="16" height="12" viewBox="0 0 17 12"><path d="M8.5 3.2C10.8 3.2 12.9 4.1 14.4 5.6L15.5 4.5C13.7 2.7 11.2 1.5 8.5 1.5 5.8 1.5 3.3 2.7 1.5 4.5L2.6 5.6C4.1 4.1 6.2 3.2 8.5 3.2Z" fill={c}/><path d="M8.5 6.8C9.9 6.8 11.1 7.3 12 8.2L13.1 7.1C11.8 5.9 10.2 5.1 8.5 5.1 6.8 5.1 5.2 5.9 3.9 7.1L5 8.2C5.9 7.3 7.1 6.8 8.5 6.8Z" fill={c}/><circle cx="8.5" cy="10.5" r="1.5" fill={c}/></svg>
        <svg width="26" height="13" viewBox="0 0 27 13"><rect x=".5" y=".5" width="23" height="12" rx="3.5" stroke={c} strokeOpacity=".35" fill="none"/><rect x="2" y="2" width="19" height="9" rx="2" fill={c}/><path d="M25 4.5v4c.8-.3 1.5-1.3 1.5-2s-.7-1.7-1.5-2z" fill={c} fillOpacity=".4"/></svg>
      </span>
    </div>
  );
}

function FaWord({ children, onTap }) {
  return <span className="fa-word" onClick={onTap}>{children}</span>;
}

Object.assign(window, { Pattern, StoryArt, Avatar, Progress, Waveform, StatusBar, FaWord, TONE });
