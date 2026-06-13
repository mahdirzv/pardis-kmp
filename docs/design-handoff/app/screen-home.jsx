/* global React, Icon, StoryArt, Pattern, Avatar, Progress, Cover, SectionHead, StoryRow, Reveal, useNav, DATA, Waveform */
/* Rivana · Today (home hub) */

function ScreenToday() {
  const nav = useNav();
  const me = nav.profile;
  const cont = DATA.storyById.rostam;
  const tonight = DATA.lullabies[1];
  const fresh = DATA.stories.filter((s) => s.new).concat(DATA.stories.filter((s) => !s.new && s.id !== 'rostam'));
  const word = { fa: 'دلیر', tr: 'delir', en: 'brave', ex: 'A delir heart fears nothing.' };

  return (
    <React.Fragment>
      <ScreenBg motif="paisley" color="var(--saffron-deep)" opacity={0.05} fade="top" size="240px 240px" />
      <div className="viewport" style={{ paddingBottom: 110 }}>
      {/* greeting */}
      <header style={{ padding: '58px var(--gutter) 8px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div>
          <p className="eyebrow">{greeting()} · {today()}</p>
          <h1 className="h1" style={{ marginTop: 5 }}>Salâm, {me.name}</h1>
          <p className="fa" style={{ fontSize: 15, color: 'var(--ink-muted)', marginTop: 3 }}>سلام، روزت پر از قصه</p>
        </div>
        <button className="iconbtn" onClick={() => nav.go('you')} style={{ width: 46, height: 46, padding: 0, border: 'none', boxShadow: 'var(--sh-sm)' }}>
          <Avatar name={me.name} tone={me.tone} size="lg" style={{ width: 46, height: 46, fontSize: 19 }} />
        </button>
      </header>

      {/* streak strip */}
      <Reveal delay={40}>
        <div style={{ margin: '12px var(--gutter) 4px', display: 'flex', gap: 10 }}>
          <div style={{ flex: 1, display: 'flex', alignItems: 'center', gap: 10, padding: '11px 14px', borderRadius: 'var(--r)', background: 'var(--saffron-tint)', border: '1px solid var(--saffron-soft)' }}>
            <Icon name="flameF" size={22} color="var(--saffron)" />
            <div style={{ lineHeight: 1.1 }}>
              <p style={{ fontWeight: 800, fontSize: 17, color: 'var(--saffron-deep)', fontFamily: 'var(--font-display)' }}>{me.streak} nights</p>
              <p className="micro" style={{ letterSpacing: '.08em', color: 'var(--saffron-deep)', opacity: .75 }}>reading streak</p>
            </div>
          </div>
          <div style={{ flex: 1, display: 'flex', alignItems: 'center', gap: 10, padding: '11px 14px', borderRadius: 'var(--r)', background: 'var(--lapis-tint)', border: '1px solid var(--lapis-soft)' }}>
            <Icon name="feather" size={22} color="var(--lapis)" />
            <div style={{ lineHeight: 1.1 }}>
              <p style={{ fontWeight: 800, fontSize: 17, color: 'var(--lapis-deep)', fontFamily: 'var(--font-display)' }}>{DATA.words.length} words</p>
              <p className="micro" style={{ letterSpacing: '.08em', color: 'var(--lapis-deep)', opacity: .75 }}>collected</p>
            </div>
          </div>
        </div>
      </Reveal>

      {/* CONTINUE hero */}
      <Reveal delay={90}>
        <section style={{ padding: '14px var(--gutter) 6px' }}>
          <p className="micro" style={{ marginBottom: 9 }}>Continue reading</p>
          <button onClick={() => nav.push('reader', { id: cont.id })} style={{ display: 'block', width: '100%', textAlign: 'left', border: 'none', padding: 0, cursor: 'pointer', borderRadius: 'var(--r-xl)', overflow: 'hidden', position: 'relative', boxShadow: 'var(--sh-md)' }}>
            <div style={{ position: 'relative', height: 230 }}>
              <StoryArt kind={cont.scene} />
              <Pattern motif="paisley" color="#fff" opacity={0.12} fade="bl" />
              <div style={{ position: 'absolute', inset: 0, background: 'linear-gradient(180deg, transparent 30%, rgba(15,12,20,.72) 100%)' }} />
              <div style={{ position: 'absolute', left: 18, right: 18, bottom: 16, color: '#fff' }}>
                <span className={`tag`} style={{ background: 'rgba(255,255,255,.22)', color: '#fff', backdropFilter: 'blur(6px)', fontSize: 11 }}>{cont.collection}</span>
                <h2 className="h2" style={{ color: '#fff', marginTop: 9, fontSize: 25 }}>{cont.title}</h2>
                <p className="fa" style={{ color: 'rgba(255,255,255,.7)', fontSize: 13.5, marginTop: 2 }}>{cont.titleFa}</p>
                <div style={{ display: 'flex', alignItems: 'center', gap: 11, marginTop: 13 }}>
                  <span style={{ width: 50, height: 50, borderRadius: 99, background: 'var(--accent)', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', boxShadow: 'var(--sh-accent)', flexShrink: 0 }}>
                    <Icon name="play" size={20} color="#fff" />
                  </span>
                  <div style={{ flex: 1 }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6, fontSize: 12, fontWeight: 600, color: 'rgba(255,255,255,.85)' }}>
                      <span>Page 6 of {cont.pages}</span><span>{Math.round(cont.progress * 100)}%</span>
                    </div>
                    <Progress value={cont.progress} height={5} color="#fff" />
                  </div>
                </div>
              </div>
            </div>
          </button>
        </section>
      </Reveal>

      {/* Tonight's bedtime */}
      <Reveal delay={140}>
        <section style={{ padding: '16px var(--gutter) 6px' }}>
          <button onClick={() => nav.push('lullaby', { id: tonight.id })} style={{ width: '100%', textAlign: 'left', border: 'none', cursor: 'pointer', borderRadius: 'var(--r-lg)', overflow: 'hidden', position: 'relative', padding: 16, display: 'flex', alignItems: 'center', gap: 14, background: 'var(--grad-night)', boxShadow: 'var(--sh)' }}>
            <Pattern motif="rosette" color="#fff" opacity={0.10} />
            <div style={{ width: 64, height: 64, borderRadius: 16, overflow: 'hidden', flexShrink: 0, position: 'relative', boxShadow: '0 8px 18px -6px rgba(0,0,0,.5)' }}>
              <StoryArt kind="lullaby" />
            </div>
            <div style={{ flex: 1, position: 'relative' }}>
              <p className="micro" style={{ color: 'rgba(255,255,255,.6)', marginBottom: 3 }}>Tonight's bedtime</p>
              <p className="h3" style={{ color: '#fff', fontSize: 17 }}>{tonight.title}</p>
              <p style={{ color: 'rgba(255,255,255,.6)', fontSize: 12.5, marginTop: 2 }}>{tonight.minutes} min · sleep timer ready</p>
            </div>
            <span style={{ width: 44, height: 44, borderRadius: 99, background: 'rgba(255,255,255,.16)', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', position: 'relative', flexShrink: 0 }}>
              <Icon name="moon" size={20} color="#fff" />
            </span>
          </button>
        </section>
      </Reveal>

      {/* New this week shelf */}
      <Reveal delay={180}>
        <section style={{ paddingTop: 22 }}>
          <SectionHead title="New this week" fa="تازه‌ها" action="See all" onAction={() => nav.go('library')} />
          <div className="shelf">
            {fresh.map((s) => <Cover key={s.id} story={s} w={150} onTap={() => nav.push('detail', { id: s.id })} />)}
          </div>
        </section>
      </Reveal>

      {/* Word of the day */}
      <Reveal delay={210}>
        <section style={{ padding: '24px var(--gutter) 6px' }}>
          <div style={{ borderRadius: 'var(--r-lg)', overflow: 'hidden', position: 'relative', background: 'var(--lilac-soft)', border: '1px solid rgba(139,111,230,.18)', padding: '18px 18px 16px' }}>
            <Pattern motif="vine" color="var(--lilac-deep)" opacity={0.10} fade="tr" />
            <div style={{ position: 'relative', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <p className="micro" style={{ color: 'var(--lilac-deep)' }}>Word of the day</p>
              <Icon name="volume" size={18} color="var(--lilac-deep)" />
            </div>
            <p className="fa" style={{ fontSize: 40, color: 'var(--lilac-deep)', margin: '8px 0 2px', fontWeight: 700, position: 'relative' }}>{word.fa}</p>
            <p style={{ fontFamily: 'var(--font-mono)', fontSize: 13, color: 'var(--lilac-deep)', position: 'relative' }}>{word.tr} — “{word.en}”</p>
            <p className="body" style={{ marginTop: 8, fontStyle: 'italic', position: 'relative', color: 'var(--ink-soft)' }}>{word.ex}</p>
          </div>
        </section>
      </Reveal>

      {/* Collections */}
      <Reveal delay={240}>
        <section style={{ paddingTop: 22 }}>
          <SectionHead title="Explore collections" fa="مجموعه‌ها" />
          <div className="shelf">
            {DATA.collections.map((c) => (
              <button key={c.id} onClick={() => nav.go('library')} style={{ width: 168, flexShrink: 0, border: 'none', cursor: 'pointer', padding: 0, borderRadius: 'var(--r-lg)', overflow: 'hidden', position: 'relative', textAlign: 'left', boxShadow: 'var(--sh-sm)' }}>
                <div style={{ height: 110, position: 'relative' }}>
                  <StoryArt kind={c.scene} />
                  <Pattern motif="paisley" color="#fff" opacity={0.12} fade="bottom" size="100px 100px" />
                  <div style={{ position: 'absolute', inset: 0, background: 'linear-gradient(180deg, transparent 40%, rgba(15,12,20,.6))' }} />
                  <div style={{ position: 'absolute', left: 13, right: 13, bottom: 11, color: '#fff' }}>
                    <p className="h3" style={{ color: '#fff', fontSize: 16 }}>{c.name}</p>
                    <p style={{ fontSize: 11.5, color: 'rgba(255,255,255,.75)', marginTop: 1 }}>{c.count} stories</p>
                  </div>
                </div>
              </button>
            ))}
          </div>
        </section>
      </Reveal>

      <p className="fa" style={{ textAlign: 'center', color: 'var(--ink-faint)', fontSize: 13, padding: '26px 0 6px' }}>پایانِ امروز · فردا قصه‌ای تازه</p>
      </div>
    </React.Fragment>
  );
}

function greeting() {
  const h = new Date().getHours();
  return h < 12 ? 'Good morning' : h < 18 ? 'Good afternoon' : 'Good evening';
}
function today() {
  return new Date().toLocaleDateString('en-US', { weekday: 'long' });
}

window.ScreenToday = ScreenToday;
