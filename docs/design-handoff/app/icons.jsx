/* global React */
/* Rivana · icons — rendered from the real Lucide set when available,
   with a hand-drawn fallback for offline / unmapped names. */
const LU_NAME = {
  home: 'House', book: 'BookOpen', moon: 'Moon', trophy: 'Trophy', user: 'User',
  play: 'Play', pause: 'Pause', prev: 'SkipBack', next: 'SkipForward',
  arrow: 'ArrowRight', chevR: 'ChevronRight', chevL: 'ChevronLeft', chevD: 'ChevronDown',
  close: 'X', search: 'Search', mic: 'Mic', globe: 'Globe',
  heart: 'Heart', heartF: 'Heart', star: 'Star', starF: 'Star',
  sparkle: 'Sparkles', shield: 'Shield', check: 'Check', checkC: 'CircleCheck',
  wave: 'AudioLines', note: 'Music', flame: 'Flame', flameF: 'Flame',
  volume: 'Volume2', timer: 'Timer', clock: 'Clock', plus: 'Plus', lock: 'Lock',
  gear: 'Settings', chart: 'ChartColumnIncreasing', translate: 'Languages',
  compass: 'Compass', feather: 'Feather', crown: 'Crown', bookmark: 'Bookmark',
  headphone: 'Headphones', grid: 'LayoutGrid', list: 'List', bell: 'Bell',
  sun: 'Sun', seed: 'Sprout', map: 'Map',
};
const LU_FILLED = { play: 1, pause: 1, prev: 1, next: 1, heartF: 1, starF: 1, flameF: 1 };

function Icon({ name, size = 22, stroke = 1.9, color, style }) {
  const L = typeof window !== 'undefined' && window.lucide;
  const lname = LU_NAME[name];
  if (L && L.icons && lname && L.icons[lname]) {
    const filled = LU_FILLED[name];
    const kids = (L.icons[lname][2]) || [];
    return React.createElement('svg', {
      width: size, height: size, viewBox: '0 0 24 24', style,
      fill: filled ? (color || 'currentColor') : 'none',
      stroke: color || 'currentColor', strokeWidth: filled ? 1.4 : stroke,
      strokeLinecap: 'round', strokeLinejoin: 'round',
    }, kids.map((c, i) => React.createElement(c[0], Object.assign({ key: i }, c[1]))));
  }
  const p = {
    width: size, height: size, viewBox: '0 0 24 24', style,
    fill: 'none', stroke: color || 'currentColor', strokeWidth: stroke,
    strokeLinecap: 'round', strokeLinejoin: 'round',
  };
  switch (name) {
    case 'home':    return <svg {...p}><path d="M11.35 3.44 3.5 9.9A2 2 0 0 0 3 11.4v8.1A1.5 1.5 0 0 0 4.5 21H9v-5.5a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1V21h4.5A1.5 1.5 0 0 0 21 19.5v-8.1a2 2 0 0 0-.7-1.5l-7.85-6.46a1 1 0 0 0-1.1 0z"/></svg>;
    case 'homeF':   return <svg {...p} stroke="none" fill={color || 'currentColor'}><path d="M11.3 2.9 3.5 9.4A2 2 0 0 0 3 10.9V20a1.5 1.5 0 0 0 1.5 1.5H9V15.5a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v6h4.5A1.5 1.5 0 0 0 21 20v-9.1a2 2 0 0 0-.7-1.5l-7.8-6.5a1 1 0 0 0-1.3 0z"/></svg>;
    case 'book':    return <svg {...p}><path d="M5 5.5A2.5 2.5 0 0 1 7.5 3H18a1 1 0 0 1 1 1v13.5a1 1 0 0 1-1 1H7.5A2.5 2.5 0 0 0 5 21z"/><path d="M5 5.5A2.5 2.5 0 0 0 7.5 8H19"/></svg>;
    case 'bookF':   return <svg {...p} stroke="none" fill={color || 'currentColor'}><path d="M6.5 3A2.5 2.5 0 0 0 4 5.5v14A2.5 2.5 0 0 0 6.5 22H19a1 1 0 0 0 1-1V4a1 1 0 0 0-1-1H6.5zM18 18.5H6.5a1 1 0 0 0 0 2H18v-2z" opacity=".35"/><path d="M6.5 3A2.5 2.5 0 0 0 4 5.5v12.2A3.5 3.5 0 0 1 6.5 17H19a1 1 0 0 0 1-1V4a1 1 0 0 0-1-1H6.5z"/></svg>;
    case 'moon':    return <svg {...p}><path d="M20.5 14.2A8.5 8.5 0 1 1 10.4 3.6 7 7 0 0 0 20.5 14.2z"/></svg>;
    case 'moonF':   return <svg {...p} stroke="none" fill={color || 'currentColor'}><path d="M20.5 14.2A8.5 8.5 0 1 1 10.4 3.6a.6.6 0 0 1 .73.86 7 7 0 0 0 9.07 8.99.6.6 0 0 1 .3.75z"/></svg>;
    case 'trophy':  return <svg {...p}><path d="M6 5a1 1 0 0 1 1-1h10a1 1 0 0 1 1 1v3.5a6 6 0 0 1-12 0z"/><path d="M6 6H4.6a2 2 0 0 0 0 4H6.8M18 6h1.4a2 2 0 0 1 0 4H17.2M12 14.4V17M9.2 20.5h5.6a1 1 0 0 0-1-1.5h-3.6a1 1 0 0 0-1 1.5z"/></svg>;
    case 'trophyF': return <svg {...p} stroke="none" fill={color || 'currentColor'}><path d="M17 4H7a1 1 0 0 0-1 1H4.5a2.5 2.5 0 0 0 0 5H6.3A6 6 0 0 0 11 13.9V17h-1a1 1 0 0 0-1 1H8a1 1 0 0 0 0 2h8a1 1 0 0 0 0-2h-1a1 1 0 0 0-1-1h-1v-3.1A6 6 0 0 0 17.7 10h1.8a2.5 2.5 0 0 0 0-5H18a1 1 0 0 0-1-1zM6 6v2.4A1.5 1.5 0 0 1 4.9 6H6zm12 2.4V6h1.1A1.5 1.5 0 0 1 18 8.4z"/></svg>;
    case 'user':    return <svg {...p}><circle cx="12" cy="7.8" r="4.1"/><path d="M5 20.2C5 16.8 8.1 14 12 14s7 2.8 7 6.2a.8.8 0 0 1-.8.8H5.8a.8.8 0 0 1-.8-.8z"/></svg>;
    case 'userF':   return <svg {...p} stroke="none" fill={color || 'currentColor'}><circle cx="12" cy="7.5" r="4.2"/><path d="M12 13.4c-4.2 0-7.5 2.9-7.5 6.1A1.5 1.5 0 0 0 6 21h12a1.5 1.5 0 0 0 1.5-1.5c0-3.2-3.3-6.1-7.5-6.1z"/></svg>;
    case 'play':    return <svg {...p} fill={color || 'currentColor'} stroke="none"><path d="M7 4.5v15a1 1 0 0 0 1.52.86l12-7.5a1 1 0 0 0 0-1.72l-12-7.5A1 1 0 0 0 7 4.5z"/></svg>;
    case 'pause':   return <svg {...p} fill={color || 'currentColor'} stroke="none"><rect x="6" y="4" width="4.4" height="16" rx="1.4"/><rect x="13.6" y="4" width="4.4" height="16" rx="1.4"/></svg>;
    case 'prev':    return <svg {...p} fill={color || 'currentColor'} stroke="none"><path d="M6 5v14a1 1 0 0 0 2 0v-5l9.5 5.8A1 1 0 0 0 19 19V5a1 1 0 0 0-1.5-.85L8 10V5a1 1 0 0 0-2 0z"/></svg>;
    case 'next':    return <svg {...p} fill={color || 'currentColor'} stroke="none"><path d="M18 5v14a1 1 0 0 1-2 0v-5L6.5 19.8A1 1 0 0 1 5 19V5a1 1 0 0 1 1.5-.85L16 10V5a1 1 0 0 1 2 0z"/></svg>;
    case 'arrow':   return <svg {...p}><path d="M5 12h14M13 5l7 7-7 7"/></svg>;
    case 'chevR':   return <svg {...p}><path d="M9 5l7 7-7 7"/></svg>;
    case 'chevL':   return <svg {...p}><path d="M15 5l-7 7 7 7"/></svg>;
    case 'chevD':   return <svg {...p}><path d="M5 9l7 7 7-7"/></svg>;
    case 'close':   return <svg {...p}><path d="M6 6l12 12M18 6 6 18"/></svg>;
    case 'search':  return <svg {...p}><circle cx="11" cy="11" r="7"/><path d="m20 20-3.2-3.2"/></svg>;
    case 'mic':     return <svg {...p}><rect x="9" y="2" width="6" height="12" rx="3"/><path d="M19 11a7 7 0 0 1-14 0M12 18v4"/></svg>;
    case 'globe':   return <svg {...p}><circle cx="12" cy="12" r="9"/><path d="M3 12h18M12 3a14 14 0 0 1 0 18M12 3a14 14 0 0 0 0 18"/></svg>;
    case 'heart':   return <svg {...p}><path d="M12 21s-7-4.5-7-10a4 4 0 0 1 7-2.6A4 4 0 0 1 19 11c0 5.5-7 10-7 10z"/></svg>;
    case 'heartF':  return <svg {...p} fill={color || 'currentColor'}><path d="M12 21s-7-4.5-7-10a4 4 0 0 1 7-2.6A4 4 0 0 1 19 11c0 5.5-7 10-7 10z"/></svg>;
    case 'star':    return <svg {...p}><path d="m12 3 2.6 5.6 6 .7-4.4 4.1 1.2 6L12 16.8 6.6 19.5l1.2-6L3.4 9.3l6-.7z"/></svg>;
    case 'starF':   return <svg {...p} fill={color || 'currentColor'}><path d="m12 3 2.6 5.6 6 .7-4.4 4.1 1.2 6L12 16.8 6.6 19.5l1.2-6L3.4 9.3l6-.7z"/></svg>;
    case 'sparkle': return <svg {...p}><path d="M12 3l1.8 5.4L19 10l-5.2 1.6L12 17l-1.8-5.4L5 10l5.2-1.6L12 3z"/><path d="M19 4l.7 2 2 .7-2 .7-.7 2-.7-2-2-.7 2-.7.7-2z"/></svg>;
    case 'shield':  return <svg {...p}><path d="M12 2l8 4v6c0 5-3.5 8.5-8 10-4.5-1.5-8-5-8-10V6z"/></svg>;
    case 'check':   return <svg {...p}><polyline points="4 12 10 18 20 6"/></svg>;
    case 'checkC':  return <svg {...p}><circle cx="12" cy="12" r="9"/><polyline points="8 12 11 15 16 9"/></svg>;
    case 'wave':    return <svg {...p}><path d="M2 12c2-3.5 4-3.5 6 0s4 3.5 6 0 4-3.5 6 0"/></svg>;
    case 'note':    return <svg {...p}><path d="M9 18V6l11-2v12"/><circle cx="6" cy="18" r="2.6"/><circle cx="17" cy="16" r="2.6"/></svg>;
    case 'flame':   return <svg {...p}><path d="M12 3c2.5 4.5 5.5 5.5 5.5 9.5a5.5 5.5 0 0 1-11 0c0-2 1-3.2 2-4.2-.4 2.2.7 3.2 3 3.2 0-3.2-2.2-5.4 .5-8.5z"/></svg>;
    case 'flameF':  return <svg {...p} fill={color || 'currentColor'} stroke="none"><path d="M12 2c2.6 5 6 6 6 10.2A6 6 0 0 1 6 12.2c0-2.2 1-3.6 2.2-4.7-.4 2.4.8 3.5 3.3 3.5 0-3.5-2.4-5.9.5-9z"/></svg>;
    case 'volume':  return <svg {...p}><path d="M11 5 6 9H3v6h3l5 4z"/><path d="M16 8a5 5 0 0 1 0 8"/><path d="M19 5a9 9 0 0 1 0 14"/></svg>;
    case 'timer':   return <svg {...p}><circle cx="12" cy="13" r="8"/><path d="M12 13V9M9 2h6"/></svg>;
    case 'clock':   return <svg {...p}><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3.5 2"/></svg>;
    case 'plus':    return <svg {...p}><path d="M12 5v14M5 12h14"/></svg>;
    case 'lock':    return <svg {...p}><rect x="4.5" y="11" width="15" height="9.5" rx="2.5"/><path d="M8 11V7.5a4 4 0 0 1 8 0V11"/></svg>;
    case 'gear':    return <svg {...p}><circle cx="12" cy="12" r="3"/><path d="M19.5 12a7.5 7.5 0 0 0-.12-1.3l2-1.6-2-3.4-2.4 1a7 7 0 0 0-2.2-1.3L14.4 2H9.6L9.2 4.4a7 7 0 0 0-2.2 1.3l-2.4-1-2 3.4 2 1.6A7.5 7.5 0 0 0 4.5 12c0 .44.04.87.12 1.3l-2 1.6 2 3.4 2.4-1a7 7 0 0 0 2.2 1.3l.4 2.4h4.8l.4-2.4a7 7 0 0 0 2.2-1.3l2.4 1 2-3.4-2-1.6c.08-.43.12-.86.12-1.3z"/></svg>;
    case 'chart':   return <svg {...p}><path d="M4 20h16"/><rect x="5" y="11" width="3.2" height="6" rx="1"/><rect x="10.4" y="7" width="3.2" height="10" rx="1"/><rect x="15.8" y="13" width="3.2" height="4" rx="1"/></svg>;
    case 'translate': return <svg {...p}><path d="M3 5h12"/><path d="M9 3v2c0 4-2 8-6 10"/><path d="M5 9c0 3 4 5 8 5"/><path d="M22 22l-5-11-5 11"/><path d="M14 18h6"/></svg>;
    case 'compass': return <svg {...p}><circle cx="12" cy="12" r="9"/><path d="M15.5 8.5 13 13l-4.5 2.5L11 11z"/></svg>;
    case 'feather': return <svg {...p}><path d="M20 4c-7 0-12 4-13 11l-3 3M8 16h8M11 11l5-5"/></svg>;
    case 'crown':   return <svg {...p}><path d="M3 8l3.5 3L12 5l5.5 6L21 8l-1.5 10h-15z"/></svg>;
    case 'bookmark':return <svg {...p}><path d="M6 4h12a1 1 0 0 1 1 1v15l-7-4-7 4V5a1 1 0 0 1 1-1z"/></svg>;
    case 'headphone': return <svg {...p}><path d="M4 13v-1a8 8 0 0 1 16 0v1"/><rect x="3" y="13" width="4" height="7" rx="1.6"/><rect x="17" y="13" width="4" height="7" rx="1.6"/></svg>;
    case 'grid':    return <svg {...p}><rect x="4" y="4" width="7" height="7" rx="2"/><rect x="13" y="4" width="7" height="7" rx="2"/><rect x="4" y="13" width="7" height="7" rx="2"/><rect x="13" y="13" width="7" height="7" rx="2"/></svg>;
    case 'list':    return <svg {...p}><path d="M8 6h12M8 12h12M8 18h12M4 6h.01M4 12h.01M4 18h.01"/></svg>;
    case 'bell':    return <svg {...p}><path d="M18 9a6 6 0 0 0-12 0c0 6-3 7-3 7h18s-3-1-3-7M10.5 20a2 2 0 0 0 3 0"/></svg>;
    case 'sun':     return <svg {...p}><circle cx="12" cy="12" r="4.5"/><path d="M12 2v2.5M12 19.5V22M2 12h2.5M19.5 12H22M4.9 4.9l1.8 1.8M17.3 17.3l1.8 1.8M19.1 4.9l-1.8 1.8M6.7 17.3l-1.8 1.8"/></svg>;
    case 'seed':    return <svg {...p}><path d="M12 21c-4 0-7-3-7-7 4 0 7 3 7 7zM12 21c4 0 7-3 7-7-4 0-7 3-7 7zM12 21V9M12 9c0-3 2-5 4-6-1 3 0 5-4 6z"/></svg>;
    case 'map':     return <svg {...p}><path d="M9 4 3 6.5v13L9 17l6 2.5 6-2.5v-13L15 6.5 9 4zM9 4v13M15 6.5v13"/></svg>;
    default:        return <svg {...p}><circle cx="12" cy="12" r="9"/></svg>;
  }
}

window.Icon = Icon;
