/* global React, Icon, StoryArt, Pattern, Avatar, Progress, useNav, DATA */
/* Rivana · Reader — signature read-along experience */

const { useState: useRS, useEffect: useRE, useRef: useRR } = React;

function ScreenReader({ id }) {
  const nav = useNav();
  const pace = (nav.tweaks && nav.tweaks.pace) || 260;
  const fontSize = (nav.tweaks && nav.tweaks.textSize) || 22;
  const story = DATA.storyById[id] || DATA.storyById.rostam;
  const pages = story.readerPages || DATA.storyById.rostam.readerPages;

  const [pageIdx, setPageIdx] = useRS(0);
  const [playing, setPlaying] = useRS(false);
  const [spoken, setSpoken] = useRS(0);
  const [lang, setLang] = useRS('both');     // 'both' | 'en' | 'fa'
  const [word, setWord] = useRS(null);
  const [autoplay] = useRS(true);
  const sx = useRR(0);

  const page = pages[pageIdx];
  const words = page.en.split(/(\s+)/);                 // keep spaces
  const wordTokens = words.map((w, i) => ({ w, i, isWord: /\S/.test(w) }));
  const realWordIdx = wordTokens.filter((t) => t.isWord).map((t) => t.i);
  const totalSpoken = realWordIdx.length;
  const last = pageIdx === pages.length - 1;

  // narration ticker
  useRE(() => {
    if (!playing) return;
    if (spoken >= totalSpoken) {
      const t = setTimeout(() => {
        setPlaying(false);
        if (!last && autoplay) { setPageIdx((p) => p + 1); }
        else if (last) { nav.push('finish', { id: story.id }); }
      }, 650);
      return () => clearTimeout(t);
    }
    const t = setTimeout(() => setSpoken((s) => s + 1), pace);
    return () => clearTimeout(t);
  }, [playing, spoken, totalSpoken, last]);

  useRE(() => { setSpoken(0); }, [pageIdx]);

  const go = (d) => {
    setPlaying(false);
    setPageIdx((p) => Math.max(0, Math.min(pages.length - 1, p + d)));
  };

  // map a token index to spoken-state class
  const spokenWordSet = realWordIdx.slice(0, spoken);
  const activeTokenIdx = realWordIdx[spoken] ;
  const wordClass = (t) => {
    if (!t.isWord) return '';
    if (spokenWordSet.includes(t.i)) return 'kw spoken';
    if (t.i === activeTokenIdx && playing) return 'kw active';
    return playing || spoken > 0 ? 'kw unspoken' : 'kw spoken';
  };

  return (
    <div style={{ position: 'absolute', inset: 0, background: 'var(--bg)', display: 'flex', flexDirection: 'column' }}>
      {/* top bar */}
      <div style={{ paddingTop: 50, position: 'relative', zIndex: 10 }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '4px var(--gutter) 0', gap: 12 }}>
          <button className="iconbtn" onClick={nav.back}><Icon name="chevD" size={20} stroke={2.2} /></button>
          <div style={{ textAlign: 'center', lineHeight: 1.15, flex: 1, minWidth: 0, padding: '0 8px' }}>
            <p style={{ fontSize: 13, fontWeight: 800, fontFamily: 'var(--font-display)', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{story.title}</p>
            <p className="micro" style={{ letterSpacing: '.1em', marginTop: 2 }}>Page {pageIdx + 1} of {pages.length}</p>
          </div>
          <button className="iconbtn"><Icon name="bookmark" size={19} /></button>
        </div>
        {/* page dots */}
        <div style={{ display: 'flex', gap: 5, justifyContent: 'center', padding: '12px 0 4px' }}>
          {pages.map((_, i) => (
            <span key={i} onClick={() => go(i - pageIdx)} style={{ height: 5, width: i === pageIdx ? 22 : 5, borderRadius: 99, background: i === pageIdx ? 'var(--accent)' : i < pageIdx ? 'var(--accent-soft)' : 'var(--rule)', transition: 'all .3s var(--ease)', cursor: 'pointer' }} />
          ))}
        </div>
      </div>

      {/* page body */}
      <div className="viewport" key={pageIdx} style={{ position: 'relative', flex: 1, animation: 'pageturn .42s var(--ease) both' }}
        onPointerDown={(e) => { sx.current = e.clientX; }}
        onPointerUp={(e) => { const dx = e.clientX - sx.current; if (dx < -45) go(1); else if (dx > 45) go(-1); }}>
        {/* illustration */}
        <div style={{ margin: '8px var(--gutter) 0', height: 290, borderRadius: 'var(--r-xl)', overflow: 'hidden', position: 'relative', boxShadow: 'var(--sh)' }}>
          <StoryArt kind={page.art} />
          <Pattern motif="paisley" color="#fff" opacity={0.10} fade="bl" />
          <span style={{ position: 'absolute', top: 12, left: 12, padding: '4px 10px', borderRadius: 99, background: 'rgba(15,12,20,.4)', backdropFilter: 'blur(6px)', color: '#fff', fontSize: 11, fontWeight: 700, fontFamily: 'var(--font-mono)', letterSpacing: '.08em' }}>
            {pageIdx + 1} / {pages.length}
          </span>
        </div>

        {/* prose */}
        <div style={{ padding: '22px var(--gutter) 30px' }}>
          {(lang === 'both' || lang === 'en') && (
            <p style={{ fontFamily: 'var(--font-display)', fontWeight: 500, fontSize: fontSize, lineHeight: 1.5, letterSpacing: '-0.01em', color: 'var(--ink)' }}>
              {wordTokens.map((t) => <span key={t.i} className={wordClass(t)}>{t.w}</span>)}
            </p>
          )}
          {(lang === 'both' || lang === 'fa') && (
            <p className="fa" style={{ fontSize: lang === 'fa' ? 26 : 20, lineHeight: 2, color: 'var(--ink-soft)', marginTop: lang === 'both' ? 18 : 0, paddingTop: lang === 'both' ? 18 : 0, borderTop: lang === 'both' ? '1px solid var(--rule)' : 'none' }}>
              {renderFa(page, (g) => setWord(g))}
            </p>
          )}
          <p className="small" style={{ marginTop: 16, color: 'var(--ink-faint)' }}>
            <Icon name="translate" size={13} color="var(--ink-faint)" style={{ verticalAlign: '-2px', marginInlineEnd: 5 }} />
            Tap a <span style={{ color: 'var(--lapis-deep)', fontWeight: 700, borderBottom: '1.5px dotted var(--lapis)' }}>dotted</span> word to learn it
          </p>
        </div>
        <style>{`@keyframes pageturn{from{opacity:0;transform:translateX(22px)}to{opacity:1;transform:none}}`}</style>
      </div>

      {/* page nav via dots + dock controls + swipe */}

      {/* narration dock */}
      <div style={{ position: 'relative', zIndex: 10, padding: '12px var(--gutter) calc(env(safe-area-inset-bottom,0px) + 18px)', background: 'var(--surface)', borderTop: '1px solid var(--rule)', boxShadow: '0 -10px 30px -18px rgba(20,17,27,.2)', borderRadius: '24px 24px 0 0' }}>
        {/* scrubber */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 12 }}>
          <span style={{ fontFamily: 'var(--font-mono)', fontSize: 11, color: 'var(--ink-muted)' }}>0:{String(Math.min(59, spoken * 4)).padStart(2, '0')}</span>
          <Progress value={totalSpoken ? spoken / totalSpoken : 0} height={5} />
          <span style={{ fontFamily: 'var(--font-mono)', fontSize: 11, color: 'var(--ink-muted)' }}>0:{String(totalSpoken * 4).padStart(2, '0')}</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Segmented value={lang} onChange={setLang} options={[{ value: 'en', label: 'EN' }, { value: 'both', label: 'Both' }, { value: 'fa', label: 'فا' }]} />
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <button className="iconbtn" onClick={() => go(-1)} disabled={pageIdx === 0} style={{ opacity: pageIdx === 0 ? .4 : 1 }}><Icon name="prev" size={18} /></button>
            <button onClick={() => { if (spoken >= totalSpoken) setSpoken(0); setPlaying((p) => !p); }} style={{ width: 58, height: 58, borderRadius: 99, border: 'none', cursor: 'pointer', background: 'var(--accent)', color: '#fff', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', boxShadow: 'var(--sh-accent)' }}>
              <Icon name={playing ? 'pause' : 'play'} size={24} color="#fff" />
            </button>
            <button className="iconbtn" onClick={() => go(1)} disabled={last} style={{ opacity: last ? .4 : 1 }}><Icon name="next" size={18} /></button>
          </div>
        </div>
      </div>

      {word && <WordCard g={word} story={story} onClose={() => setWord(null)} />}
    </div>
  );
}

/* render the Farsi line, wrapping glossary words as tappable */
function renderFa(page, onTap) {
  let text = page.fa;
  const gloss = page.glossary || [];
  // build a regex of glossary fa words
  const parts = [];
  let remaining = text;
  let guard = 0;
  while (remaining.length && guard++ < 200) {
    let hitIdx = -1, hitG = null;
    gloss.forEach((g) => {
      const idx = remaining.indexOf(g.fa);
      if (idx !== -1 && (hitIdx === -1 || idx < hitIdx)) { hitIdx = idx; hitG = g; }
    });
    if (hitIdx === -1) { parts.push(remaining); break; }
    if (hitIdx > 0) parts.push(remaining.slice(0, hitIdx));
    parts.push(<span key={parts.length} className="fa-word" onClick={() => onTap(hitG)}>{hitG.fa}</span>);
    remaining = remaining.slice(hitIdx + hitG.fa.length);
  }
  return parts;
}

function WordCard({ g, story, onClose }) {
  const [added, setAdded] = useRS(false);
  return (
    <div className="sheet-back" onClick={onClose}>
      <div className="sheet" onClick={(e) => e.stopPropagation()} style={{ padding: '10px 0 calc(env(safe-area-inset-bottom,0px) + 22px)' }}>
        <div className="sheet-grip" />
        <div style={{ padding: '14px var(--gutter) 0' }}>
          <div style={{ borderRadius: 'var(--r-lg)', overflow: 'hidden', position: 'relative', background: 'var(--lapis-tint)', border: '1px solid var(--lapis-soft)', padding: '22px 20px', textAlign: 'center' }}>
            <Pattern motif="paisley" color="var(--lapis)" opacity={0.08} fade="edges" />
            <p className="fa" style={{ fontSize: 52, fontWeight: 700, color: 'var(--lapis-deep)', position: 'relative', lineHeight: 1.2 }}>{g.fa}</p>
            <p style={{ fontFamily: 'var(--font-mono)', fontSize: 15, color: 'var(--lapis)', position: 'relative', marginTop: 2 }}>{g.tr}</p>
          </div>
          <p className="h2" style={{ textAlign: 'center', marginTop: 16 }}>“{g.en}”</p>
          <p className="small" style={{ textAlign: 'center', color: 'var(--ink-muted)', marginTop: 4 }}>from <b style={{ color: 'var(--ink-soft)' }}>{story.title}</b></p>
          <div style={{ display: 'flex', gap: 10, marginTop: 18 }}>
            <button className="btn btn-soft btn-lg" style={{ flex: 1 }}><Icon name="volume" size={19} color="var(--accent-deep)" /> Hear it</button>
            <button className="btn btn-lg" onClick={() => setAdded(true)} style={{ flex: 1, background: added ? 'var(--mint)' : 'var(--ink)', color: '#fff' }}>
              <Icon name={added ? 'check' : 'plus'} size={18} color="#fff" /> {added ? 'In your garden' : 'Add to garden'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

function navArrow(side, hidden) {
  return {
    position: 'absolute', top: '38%', [side]: 6, zIndex: 8,
    width: 40, height: 40, borderRadius: 99, border: 'none',
    background: 'rgba(255,255,255,.7)', backdropFilter: 'blur(8px)',
    boxShadow: 'var(--sh-sm)', display: hidden ? 'none' : 'inline-flex',
    alignItems: 'center', justifyContent: 'center', cursor: 'pointer',
  };
}

/* ── Chapter complete celebration ────────────────────────── */
function ScreenFinish({ id }) {
  const nav = useNav();
  const story = DATA.storyById[id] || DATA.storyById.rostam;
  const pages = story.readerPages || DATA.storyById.rostam.readerPages;
  const hero = DATA.charById[(story.chars || [])[0]] || DATA.charById.rostam;

  // dedupe the words this chapter taught
  const seen = new Set();
  const words = [];
  for (const p of pages) for (const g of (p.glossary || [])) {
    if (!seen.has(g.fa)) { seen.add(g.fa); words.push(g); }
  }
  const garden = words.slice(0, 6);

  const idx = DATA.stories.findIndex((s) => s.id === story.id);
  const next = DATA.stories[(idx + 1) % DATA.stories.length];

  // one-shot confetti burst on entry
  const confetti = React.useMemo(() => Array.from({ length: 26 }).map((_, i) => ({
    left: (i * 38 + 6) % 100,
    delay: (i % 9) * 90,
    dur: 2.2 + (i % 5) * 0.42,
    color: ['#F4B53A', '#F08A2D', '#8B6FE6', '#34B57F', '#fff'][i % 5],
    kind: i % 3, size: 6 + (i % 4) * 2, rot: (i * 57) % 360, drift: ((i % 5) - 2) * 26,
  })), []);

  const Stat = ({ icon, n, label, color, d }) => (
    <div style={{ flex: 1, padding: '15px 6px 13px', borderRadius: 20, background: 'rgba(255,255,255,.09)', border: '1px solid rgba(255,255,255,.14)', animation: 'fin-rise .6s var(--ease) both', animationDelay: d }}>
      <div style={{ width: 38, height: 38, margin: '0 auto', borderRadius: 12, background: 'rgba(255,255,255,.10)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <Icon name={icon} size={21} color={color} />
      </div>
      <p style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: 23, color: '#fff', marginTop: 8, letterSpacing: '-.02em' }}>{n}</p>
      <p style={{ fontSize: 10.5, color: 'rgba(255,255,255,.6)', fontWeight: 600, marginTop: 1 }}>{label}</p>
    </div>
  );

  return (
    <div style={{ position: 'absolute', inset: 0, background: 'var(--grad-night)', display: 'flex', flexDirection: 'column' }}>
      <Pattern motif="nightsky" color="#fff" opacity={0.07} fade="top" style={{ height: 540 }} />
      <div aria-hidden="true" style={{ position: 'absolute', top: -40, left: '50%', transform: 'translateX(-50%)', width: 460, height: 460, borderRadius: '50%', background: 'radial-gradient(circle, rgba(244,181,58,.20) 0%, rgba(139,111,230,.10) 38%, transparent 66%)', pointerEvents: 'none' }} />

      {/* confetti */}
      <div aria-hidden="true" style={{ position: 'absolute', inset: 0, overflow: 'hidden', pointerEvents: 'none', zIndex: 1 }}>
        {confetti.map((c, i) => (
          <span key={i} style={{ position: 'absolute', left: `${c.left}%`, top: '-6%', color: c.color, ['--drift']: `${c.drift}px`, animation: `fin-fall ${c.dur}s cubic-bezier(.3,.4,.5,1) ${c.delay}ms forwards` }}>
            {c.kind === 0
              ? <Icon name="sparkle" size={c.size + 6} />
              : <span style={{ display: 'block', width: c.size, height: c.size * (c.kind === 1 ? 0.5 : 1), borderRadius: c.kind === 2 ? '50%' : 2, background: 'currentColor', transform: `rotate(${c.rot}deg)` }} />}
          </span>
        ))}
      </div>

      <div className="viewport" style={{ position: 'relative', zIndex: 2, paddingTop: 56, display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center' }}>

        {/* hero seal medallion */}
        <div style={{ position: 'relative', width: 188, height: 188, display: 'flex', alignItems: 'center', justifyContent: 'center', marginTop: 8, animation: 'fin-seal .7s var(--ease-back) both' }}>
          <div style={{ position: 'absolute', width: 188, height: 188, borderRadius: '50%', border: '2px dashed rgba(244,181,58,.45)', animation: 'fin-spin 26s linear infinite' }} />
          <div style={{ position: 'absolute', width: 162, height: 162, borderRadius: '50%', border: '1px solid rgba(255,255,255,.16)' }} />
          <div style={{ width: 142, height: 142, borderRadius: '50%', overflow: 'hidden', position: 'relative', boxShadow: '0 18px 40px -14px rgba(0,0,0,.6), 0 0 0 5px rgba(255,255,255,.08), inset 0 0 0 1px rgba(255,255,255,.18)' }}>
            <StoryArt kind={story.scene} />
            <Pattern motif="rosette" color="#fff" opacity={0.18} fade="edges" />
            <div style={{ position: 'absolute', inset: 0, background: 'radial-gradient(circle at 50% 22%, transparent 30%, rgba(15,12,40,.45) 100%)' }} />
          </div>
          {/* gold seal */}
          <div style={{ position: 'absolute', bottom: 14, width: 50, height: 50, borderRadius: '50%', background: 'linear-gradient(150deg, #F8C75A, #E58A1E)', display: 'flex', alignItems: 'center', justifyContent: 'center', boxShadow: '0 8px 18px -4px rgba(229,138,30,.6), inset 0 1px 1px rgba(255,255,255,.5)', border: '2px solid rgba(255,255,255,.85)', animation: 'fin-pop .5s var(--ease-back) .35s both' }}>
            <Icon name="check" size={26} color="#fff" stroke={2.6} />
          </div>
        </div>

        {/* stars earned */}
        <div style={{ display: 'flex', gap: 6, alignItems: 'flex-end', marginTop: 18 }}>
          {[0, 1, 2].map((s) => (
            <span key={s} style={{ animation: 'fin-pop .45s var(--ease-back) both', animationDelay: `${0.5 + s * 0.12}s`, transform: s === 1 ? 'translateY(-6px)' : 'none' }}>
              <Icon name="starF" size={s === 1 ? 40 : 32} color="#F4B53A" style={{ filter: 'drop-shadow(0 3px 8px rgba(244,181,58,.5))' }} />
            </span>
          ))}
        </div>

        <p className="eyebrow" style={{ color: 'rgba(255,233,210,.85)', marginTop: 20, animation: 'fin-rise .6s var(--ease) .5s both' }}>Chapter complete · {story.collection}</p>
        <h1 className="h1" style={{ color: '#fff', fontSize: 33, marginTop: 7, padding: '0 24px', animation: 'fin-rise .6s var(--ease) .56s both' }}>Âfarin, you did it!</h1>
        <p className="fa" style={{ color: 'rgba(255,255,255,.72)', fontSize: 17, marginTop: 7, animation: 'fin-rise .6s var(--ease) .62s both' }}>آفرین! یک قصه‌ی دیگر تمام شد</p>
        <p style={{ color: 'rgba(255,255,255,.5)', fontSize: 13.5, marginTop: 8, animation: 'fin-rise .6s var(--ease) .66s both' }}>You finished <b style={{ color: 'rgba(255,255,255,.82)', fontWeight: 700 }}>{story.title}</b></p>

        {/* stats */}
        <div style={{ display: 'flex', gap: 10, marginTop: 24, width: '100%', padding: '0 var(--gutter)' }}>
          <Stat icon="flameF" n="+1" label="night streak" color="#F08A2D" d=".70s" />
          <Stat icon="feather" n={`+${words.length}`} label="new words" color="#8B6FE6" d=".78s" />
          <Stat icon="starF" n="+20" label="stars" color="#F4B53A" d=".86s" />
        </div>

        {/* character cameo */}
        <div style={{ width: '100%', padding: '14px var(--gutter) 0', animation: 'fin-rise .6s var(--ease) .92s both' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 13, textAlign: 'left', padding: '13px 15px', borderRadius: 'var(--r-lg)', background: 'rgba(255,255,255,.07)', border: '1px solid rgba(255,255,255,.13)' }}>
            <Avatar name={hero.name} tone={hero.tone} size="lg" ring />
            <div style={{ flex: 1, minWidth: 0 }}>
              <p style={{ color: '#fff', fontWeight: 700, fontSize: 15 }}>{hero.name} is proud of you <span className="fa" style={{ fontWeight: 500, color: 'rgba(255,255,255,.55)', fontSize: 13, marginInlineStart: 4 }}>{hero.nameFa}</span></p>
              <p style={{ color: 'rgba(255,255,255,.6)', fontSize: 12.5, marginTop: 2 }}>“You stayed to the very last page — that is the heart of a true hero.”</p>
            </div>
          </div>
        </div>

        {/* words to garden */}
        <div style={{ width: '100%', padding: '20px var(--gutter) 0', animation: 'fin-rise .6s var(--ease) 1s both' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 7, marginBottom: 11 }}>
            <Icon name="seed" size={16} color="#34B57F" />
            <p className="micro" style={{ color: 'rgba(255,255,255,.6)' }}>New words in your garden</p>
          </div>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
            {garden.map((g, i) => (
              <span key={i} className="fa" style={{ display: 'inline-flex', alignItems: 'center', padding: '8px 13px', borderRadius: 99, background: 'rgba(255,255,255,.10)', border: '1px solid rgba(255,255,255,.12)', color: '#fff', fontSize: 16, animation: 'fin-pop .4s var(--ease-back) both', animationDelay: `${1.04 + i * 0.05}s` }}>
                {g.fa}<span style={{ fontFamily: 'var(--font-mono)', fontSize: 10.5, opacity: .55, marginInlineStart: 8, direction: 'ltr', display: 'inline-block' }}>{g.tr}</span>
              </span>
            ))}
          </div>
        </div>

        <div style={{ height: 28 }} />
      </div>

      {/* CTAs */}
      <div style={{ position: 'relative', zIndex: 2, padding: '12px var(--gutter) calc(env(safe-area-inset-bottom,0px) + 18px)', display: 'flex', gap: 10, background: 'linear-gradient(180deg, transparent, rgba(15,12,40,.55) 40%)' }}>
        <button className="btn btn-lg" style={{ flex: 1, background: '#fff', color: 'var(--lapis-deep)' }} onClick={() => nav.push('detail', { id: next.id })}><Icon name="next" size={18} color="var(--lapis-deep)" /> Next story</button>
        <button className="btn btn-lg btn-ghost" style={{ width: 116, color: '#fff', boxShadow: 'inset 0 0 0 1.5px rgba(255,255,255,.3)' }} onClick={() => nav.home('today')}>Done</button>
      </div>

      <style>{`
        @keyframes fin-pop { from { opacity: 0; transform: scale(.5); } to { opacity: 1; transform: none; } }
        @keyframes fin-seal { from { opacity: 0; transform: scale(.7) rotate(-8deg); } to { opacity: 1; transform: none; } }
        @keyframes fin-rise { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: none; } }
        @keyframes fin-spin { to { transform: rotate(360deg); } }
        @keyframes fin-fall { 0% { opacity: 0; transform: translate(0,0) rotate(0); } 12% { opacity: 1; } 100% { opacity: 0; transform: translate(var(--drift,0), 118vh) rotate(540deg); } }
        @media (prefers-reduced-motion: reduce) {
          [style*="fin-fall"], [style*="fin-spin"] { animation: none !important; }
        }
      `}</style>
    </div>
  );
}

Object.assign(window, { ScreenReader, ScreenFinish, WordCard });
