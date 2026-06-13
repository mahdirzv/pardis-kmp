/* global React, Icon, StoryArt, Pattern, Avatar, Progress, Waveform, SectionHead, Reveal, useNav, DATA */
/* Rivana · Bedtime tab + Lullaby player (dark) */

const { useState: useBS, useEffect: useBE } = React;

function ScreenBedtime() {
  const nav = useNav();
  const feat = DATA.lullabies[0];
  const rest = DATA.lullabies.slice(1);
  return (
    <React.Fragment>
      <ScreenBg motif="star8" color="#fff" opacity={0.055} fade="top" size="150px 150px" height={520} />
      <div className="viewport" style={{ paddingBottom: 110 }}>
      <Pattern motif="rosette" color="#fff" opacity={0.035} fade="top" style={{ height: 460 }} />
      <header style={{ padding: '58px var(--gutter) 6px', position: 'relative' }}>
        <p className="eyebrow" style={{ color: 'rgba(255,233,210,.75)' }}>{new Date().getHours() >= 18 ? 'Sweet dreams' : 'For tonight'}</p>
        <h1 className="h1" style={{ color: '#fff', marginTop: 5 }}>Bedtime</h1>
        <p className="fa" style={{ fontSize: 15, color: 'rgba(255,255,255,.5)', marginTop: 3 }}>وقتِ خواب · لای‌لای</p>
      </header>

      {/* featured */}
      <Reveal delay={50}>
        <section style={{ padding: '12px var(--gutter) 4px' }}>
          <button onClick={() => nav.push('lullaby', { id: feat.id })} style={{ width: '100%', border: 'none', cursor: 'pointer', padding: 0, borderRadius: 'var(--r-xl)', overflow: 'hidden', position: 'relative', boxShadow: '0 24px 50px -20px rgba(0,0,0,.6)', textAlign: 'left' }}>
            <div style={{ height: 250, position: 'relative' }}>
              <StoryArt kind="lullaby" />
              <Pattern motif="paisley" color="#fff" opacity={0.12} fade="bl" />
              <div style={{ position: 'absolute', inset: 0, background: 'linear-gradient(180deg, transparent 40%, rgba(15,12,30,.7))' }} />
              <div style={{ position: 'absolute', left: 18, right: 18, bottom: 16, color: '#fff' }}>
                <span className="tag" style={{ background: 'rgba(255,255,255,.18)', color: '#fff', backdropFilter: 'blur(6px)', fontSize: 11 }}><Icon name="note" size={12} /> Lullaby of the night</span>
                <h2 className="h2" style={{ color: '#fff', marginTop: 9 }}>{feat.title}</h2>
                <p className="fa" style={{ color: 'rgba(255,255,255,.65)', fontSize: 14, marginTop: 2 }}>{feat.titleFa}</p>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginTop: 12 }}>
                  <span style={{ width: 46, height: 46, borderRadius: 99, background: '#fff', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}><Icon name="play" size={19} color="var(--lapis-deep)" /></span>
                  <span style={{ fontSize: 13, color: 'rgba(255,255,255,.8)' }}>{feat.minutes} min · {feat.origin}</span>
                </div>
              </div>
            </div>
          </button>
        </section>
      </Reveal>

      {/* wind-down ritual */}
      <Reveal delay={100}>
        <section style={{ padding: '18px var(--gutter) 4px' }}>
          <div style={{ display: 'flex', gap: 11, padding: '14px 16px', borderRadius: 'var(--r-lg)', background: 'rgba(255,255,255,.06)', border: '1px solid rgba(255,255,255,.10)', alignItems: 'center' }}>
            <span style={{ width: 46, height: 46, borderRadius: 14, background: 'rgba(244,181,58,.18)', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}><Icon name="timer" size={22} color="var(--sun)" /></span>
            <div style={{ flex: 1 }}>
              <p style={{ color: '#fff', fontWeight: 700, fontSize: 15 }}>Wind-down routine</p>
              <p style={{ color: 'rgba(255,255,255,.55)', fontSize: 12.5, marginTop: 1 }}>One short story, then a lullaby. ~25 min.</p>
            </div>
            <button className="btn btn-sm" style={{ background: 'rgba(255,255,255,.14)', color: '#fff' }}>Start</button>
          </div>
        </section>
      </Reveal>

      {/* list */}
      <section style={{ paddingTop: 22 }}>
        <SectionHead title="Lullaby shelf" fa="قفسه‌ی لای‌لایی" dark />
        <div style={{ padding: '0 var(--gutter)' }}>
          {rest.map((l, i) => (
            <button key={l.id} onClick={() => nav.push('lullaby', { id: l.id })} style={{ width: '100%', display: 'flex', gap: 13, alignItems: 'center', background: 'none', border: 'none', borderTop: i ? '1px solid rgba(255,255,255,.08)' : 'none', padding: '12px 0', cursor: 'pointer', textAlign: 'left' }}>
              <div style={{ width: 56, height: 56, borderRadius: 14, overflow: 'hidden', flexShrink: 0, position: 'relative' }}><StoryArt kind={l.scene} /></div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <p style={{ color: '#fff', fontWeight: 700, fontSize: 15.5 }}>{l.title}</p>
                <p className="fa" style={{ color: 'rgba(255,255,255,.5)', fontSize: 12.5, marginTop: 1 }}>{l.titleFa}</p>
                <p className="micro" style={{ color: 'rgba(255,255,255,.4)', letterSpacing: '.08em', marginTop: 4 }}>{l.minutes} min · {l.plays} plays</p>
              </div>
              <span style={{ width: 40, height: 40, borderRadius: 99, background: 'rgba(255,255,255,.12)', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}><Icon name="play" size={16} color="#fff" /></span>
            </button>
          ))}
        </div>
      </section>
      <p className="fa" style={{ textAlign: 'center', color: 'rgba(255,255,255,.3)', fontSize: 13, padding: '26px 0 6px' }}>شب بخیر · خواب‌های خوش</p>
      </div>
    </React.Fragment>
  );
}

/* ── Lullaby player ──────────────────────────────────────── */
function ScreenLullaby({ id }) {
  const nav = useNav();
  const l = DATA.lullabies.find((x) => x.id === id) || DATA.lullabies[0];
  const [playing, setPlaying] = useBS(true);
  const [prog, setProg] = useBS(0.28);
  const [timer, setTimer] = useBS(30);
  const [showTimer, setShowTimer] = useBS(false);
  const [amb, setAmb] = useBS(true);

  useBE(() => {
    if (!playing) return;
    const t = setInterval(() => setProg((p) => (p >= 1 ? 0 : p + 0.0015)), 60);
    return () => clearInterval(t);
  }, [playing]);

  const total = l.minutes * 60;
  const fmt = (s) => `${Math.floor(s / 60)}:${String(Math.floor(s % 60)).padStart(2, '0')}`;

  return (
    <div style={{ position: 'absolute', inset: 0, background: 'linear-gradient(180deg, #1A256E 0%, #0F1330 60%, #0A0E22 100%)', display: 'flex', flexDirection: 'column' }}>
      <Pattern motif="rosette" color="#fff" opacity={0.06} />
      {/* top */}
      <div style={{ paddingTop: 50, position: 'relative', zIndex: 5 }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '6px var(--gutter) 0' }}>
          <button className="iconbtn ghost-dark" onClick={nav.back}><Icon name="chevD" size={20} color="#fff" /></button>
          <p className="micro" style={{ color: 'rgba(255,255,255,.6)' }}>Now playing</p>
          <button className="iconbtn ghost-dark"><Icon name="heart" size={18} color="#fff" /></button>
        </div>
      </div>

      {/* album art */}
      <div style={{ position: 'relative', zIndex: 2, padding: '26px var(--gutter) 0', display: 'flex', justifyContent: 'center' }}>
        <div style={{ width: 280, height: 280, borderRadius: 36, overflow: 'hidden', position: 'relative', boxShadow: '0 40px 80px -24px rgba(0,0,0,.7), 0 0 0 1px rgba(255,255,255,.08)' }}>
          <StoryArt kind={l.scene} />
          <Pattern motif="paisley" color="#fff" opacity={0.12} fade="edges" />
          <div className="moonglow" style={{ position: 'absolute', inset: 0, animation: playing ? 'breathe 5s ease-in-out infinite' : 'none' }} />
        </div>
      </div>

      <div style={{ flex: 1 }} />

      {/* meta + controls */}
      <div style={{ position: 'relative', zIndex: 3, padding: '0 var(--gutter) calc(env(safe-area-inset-bottom,0px) + 22px)' }}>
        <div style={{ textAlign: 'center' }}>
          <h2 className="h2" style={{ color: '#fff' }}>{l.title}</h2>
          <p className="fa" style={{ color: 'rgba(255,255,255,.6)', fontSize: 16, marginTop: 4 }}>{l.titleFa}</p>
          <p style={{ color: 'rgba(255,255,255,.4)', fontSize: 12.5, marginTop: 6, fontFamily: 'var(--font-mono)', letterSpacing: '.06em' }}>{l.origin}</p>
        </div>

        {/* waveform progress */}
        <div style={{ margin: '22px 0 6px', color: '#fff' }}>
          <Waveform playing={playing} progress={prog} color="#fff" height={38} bars={42} />
        </div>
        <div style={{ display: 'flex', justifyContent: 'space-between', color: 'rgba(255,255,255,.5)', fontFamily: 'var(--font-mono)', fontSize: 11 }}>
          <span>{fmt(prog * total)}</span><span>-{fmt(total - prog * total)}</span>
        </div>

        {/* transport */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 26, margin: '16px 0 6px' }}>
          <button className="iconbtn ghost-dark" style={{ width: 50, height: 50 }}><Icon name="prev" size={22} color="#fff" /></button>
          <button onClick={() => setPlaying((p) => !p)} style={{ width: 80, height: 80, borderRadius: 99, border: 'none', cursor: 'pointer', background: '#fff', color: 'var(--lapis-deep)', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', boxShadow: '0 16px 36px -10px rgba(255,255,255,.4)' }}>
            <Icon name={playing ? 'pause' : 'play'} size={32} color="var(--lapis-deep)" />
          </button>
          <button className="iconbtn ghost-dark" style={{ width: 50, height: 50 }}><Icon name="next" size={22} color="#fff" /></button>
        </div>

        {/* bottom row: timer + ambient */}
        <div style={{ display: 'flex', gap: 10, marginTop: 14 }}>
          <button onClick={() => setShowTimer(true)} style={{ flex: 1, height: 46, borderRadius: 99, border: '1px solid rgba(255,255,255,.16)', background: 'rgba(255,255,255,.08)', color: '#fff', cursor: 'pointer', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: 8, fontWeight: 700, fontSize: 13.5 }}>
            <Icon name="timer" size={18} color="var(--sun)" /> {timer ? `Sleep in ${timer}m` : 'Sleep timer'}
          </button>
          <button onClick={() => setAmb((a) => !a)} style={{ flex: 1, height: 46, borderRadius: 99, border: '1px solid rgba(255,255,255,.16)', background: amb ? 'rgba(244,181,58,.18)' : 'rgba(255,255,255,.08)', color: '#fff', cursor: 'pointer', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: 8, fontWeight: 700, fontSize: 13.5 }}>
            <Icon name="wave" size={18} color={amb ? 'var(--sun)' : 'rgba(255,255,255,.7)'} /> Rain {amb ? 'on' : 'off'}
          </button>
        </div>
      </div>

      {showTimer && (
        <div className="sheet-back" onClick={() => setShowTimer(false)}>
          <div className="sheet" onClick={(e) => e.stopPropagation()} style={{ background: '#171B3A' }}>
            <div className="sheet-grip" style={{ background: 'rgba(255,255,255,.25)' }} />
            <p className="h3" style={{ color: '#fff', textAlign: 'center', padding: '8px 0 4px' }}>Sleep timer</p>
            <p className="fa" style={{ color: 'rgba(255,255,255,.5)', textAlign: 'center', fontSize: 13 }}>زمان‌سنجِ خواب</p>
            <div style={{ padding: '16px var(--gutter) 8px', display: 'flex', flexDirection: 'column', gap: 8 }}>
              {[15, 30, 45, 0].map((m) => (
                <button key={m} onClick={() => { setTimer(m); setShowTimer(false); }} style={{ height: 52, borderRadius: 16, border: '1px solid rgba(255,255,255,.12)', background: timer === m ? 'rgba(255,255,255,.14)' : 'transparent', color: '#fff', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 18px', fontWeight: 700, fontSize: 15 }}>
                  <span>{m ? `${m} minutes` : 'End of lullaby'}</span>
                  {timer === m && <Icon name="check" size={20} color="var(--sun)" />}
                </button>
              ))}
            </div>
          </div>
        </div>
      )}
      <style>{`
        .moonglow{background:radial-gradient(circle at 50% 40%, rgba(255,255,255,.22), transparent 60%);}
        @keyframes breathe{0%,100%{opacity:.5}50%{opacity:1}}
      `}</style>
    </div>
  );
}

Object.assign(window, { ScreenBedtime, ScreenLullaby });
