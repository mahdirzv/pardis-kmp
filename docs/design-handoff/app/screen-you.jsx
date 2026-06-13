/* global React, Icon, StoryArt, Pattern, Avatar, Progress, PushBar, SectionHead, Segmented, Reveal, useNav, DATA */
/* Rivana · You (profile) + Parent area + Onboarding */

const { useState: useYS } = React;

function ScreenYou() {
  const nav = useNav();
  const me = nav.profile;
  const Row = ({ icon, tone = 'lapis', label, detail, onClick, last }) => (
    <button onClick={onClick} style={{ width: '100%', display: 'flex', alignItems: 'center', gap: 13, padding: '13px 16px', background: 'none', border: 'none', borderTop: last ? 'none' : 'none', cursor: 'pointer', textAlign: 'left' }}>
      <span style={{ width: 34, height: 34, borderRadius: 10, background: `var(--${tone}-soft)`, display: 'inline-flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}><Icon name={icon} size={18} color={`var(--${tone}-deep)`} /></span>
      <span style={{ flex: 1, fontWeight: 600, fontSize: 15 }}>{label}</span>
      {detail && <span className="small" style={{ color: 'var(--ink-muted)' }}>{detail}</span>}
      <Icon name="chevR" size={17} color="var(--ink-faint)" />
    </button>
  );
  const Group = ({ children, label }) => (
    <section style={{ padding: '0 var(--gutter)', marginTop: 18 }}>
      {label && <p className="micro" style={{ marginBottom: 8 }}>{label}</p>}
      <div style={{ borderRadius: 'var(--r-lg)', border: '1px solid var(--rule)', background: 'var(--surface)', overflow: 'hidden' }}>
        {React.Children.map(children, (ch, i) => (
          <React.Fragment>{i ? <div style={{ height: 1, background: 'var(--rule-soft)', marginLeft: 63 }} /> : null}{ch}</React.Fragment>
        ))}
      </div>
    </section>
  );

  return (
    <React.Fragment>
      <ScreenBg motif="rosette" color="var(--lapis)" opacity={0.05} fade="top" size="180px 180px" />
      <div className="viewport" style={{ paddingBottom: 110 }}>
      <header style={{ padding: '58px var(--gutter) 6px' }}>
        <h1 className="h1">You</h1>
      </header>

      {/* profile card */}
      <Reveal delay={40}>
        <section style={{ padding: '8px var(--gutter) 0' }}>
          <div style={{ borderRadius: 'var(--r-xl)', overflow: 'hidden', position: 'relative', background: 'var(--grad-dawn)', border: '1px solid var(--rule)', padding: '22px 20px' }}>
            <Pattern motif="paisley" color="var(--lapis)" opacity={0.07} fade="tr" />
            <div style={{ position: 'relative', display: 'flex', alignItems: 'center', gap: 16 }}>
              <Avatar name={me.name} tone={me.tone} size="xl" ring />
              <div style={{ flex: 1 }}>
                <p className="h2">{me.name}</p>
                <p className="small" style={{ color: 'var(--ink-soft)', marginTop: 2 }}>Age {me.age} · {me.streak}-night streak</p>
                <button onClick={() => nav.push('onboarding', { switch: true })} className="btn btn-sm" style={{ marginTop: 10, background: 'var(--surface)', boxShadow: 'var(--sh-xs)' }}><Icon name="user" size={15} /> Switch reader</button>
              </div>
            </div>
          </div>
        </section>
      </Reveal>

      <section style={{ padding: '0 var(--gutter)', marginTop: 18 }}>
        <p className="micro" style={{ marginBottom: 8 }}>Appearance</p>
        <div style={{ borderRadius: 'var(--r-lg)', border: '1px solid var(--rule)', background: 'var(--surface)', overflow: 'hidden' }}>
          <button onClick={() => nav.setTheme(nav.theme === 'dark' ? 'light' : 'dark')} style={{ width: '100%', display: 'flex', alignItems: 'center', gap: 13, padding: '13px 16px', background: 'none', border: 'none', cursor: 'pointer', textAlign: 'left' }}>
            <span style={{ width: 34, height: 34, borderRadius: 10, background: 'var(--lilac-soft)', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}><Icon name={nav.theme === 'dark' ? 'moon' : 'sun'} size={18} color="var(--lilac-deep)" /></span>
            <span style={{ flex: 1, fontWeight: 600, fontSize: 15 }}>Dark mode</span>
            <span style={{ width: 46, height: 28, borderRadius: 99, background: nav.theme === 'dark' ? 'var(--accent)' : 'var(--rule-strong)', position: 'relative', transition: 'background .2s', flexShrink: 0 }}>
              <span style={{ position: 'absolute', top: 3, left: nav.theme === 'dark' ? 21 : 3, width: 22, height: 22, borderRadius: 99, background: '#fff', boxShadow: 'var(--sh-xs)', transition: 'left .2s var(--ease)' }} />
            </span>
          </button>
        </div>
      </section>

      <Group label="Reading">
        <Row icon="translate" tone="lapis" label="Story language" detail="English & فارسی" />
        <Row icon="volume" tone="saffron" label="Narration speed" detail="Normal" />
        <Row icon="book" tone="mint" label="Downloads" detail="3 stories" />
      </Group>

      <Group label="Family">
        <Row icon="shield" tone="lapis" label="Parents' corner" detail="Locked" onClick={() => nav.push('parent', {})} />
        <Row icon="bell" tone="saffron" label="Bedtime reminder" detail="8:00 PM" />
        <Row icon="star" tone="lilac" label="Rivana Plus" detail="Active" />
      </Group>

      <Group label="About">
        <Row icon="gear" tone="lapis" label="Settings" />
        <Row icon="heart" tone="rose" label="Rate Rivana" />
      </Group>

      <p className="fa" style={{ textAlign: 'center', color: 'var(--ink-faint)', fontSize: 13, padding: '24px 0 6px' }}>ریوانا · قصه‌های پارسی برای کودکان</p>
      <p className="micro" style={{ textAlign: 'center', letterSpacing: '.12em' }}>Rivana · v1.0</p>
      </div>
    </React.Fragment>
  );
}

/* ── Parent area ─────────────────────────────────────────── */
function ScreenParent() {
  const nav = useNav();
  const [limit, setLimit] = useYS('30');
  const stats = [['clock', '2h 14m', 'read this week', 'saffron'], ['book', '6', 'stories', 'lapis'], ['feather', '14', 'new words', 'mint'], ['flameF', '7', 'night streak', 'rose']];
  return (
    <div className="screen-pop" style={{ position: 'absolute', inset: 0, background: 'var(--bg)' }}>
      <ScreenBg motif="star8" color="var(--lapis)" opacity={0.045} fade="top" size="150px 150px" />
      <div className="viewport" style={{ paddingBottom: 30 }}>
        <div style={{ paddingTop: 46 }}><PushBar onBack={nav.back} title="Parents' corner" /></div>
        <p className="fa" style={{ textAlign: 'center', fontSize: 13, color: 'var(--ink-muted)', marginTop: -2 }}>گوشه‌ی والدین</p>

        {/* week summary */}
        <section style={{ padding: '16px var(--gutter) 4px' }}>
          <p className="micro" style={{ marginBottom: 10 }}>This week</p>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            {stats.map(([ic, n, l, t]) => (
              <div key={l} style={{ padding: '16px', borderRadius: 'var(--r-lg)', background: 'var(--surface)', border: '1px solid var(--rule)', display: 'flex', alignItems: 'center', gap: 12 }}>
                <span style={{ width: 42, height: 42, borderRadius: 12, background: `var(--${t}-soft)`, display: 'inline-flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}><Icon name={ic} size={20} color={`var(--${t}-deep)`} /></span>
                <div>
                  <p style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: 22, lineHeight: 1 }}>{n}</p>
                  <p className="micro" style={{ letterSpacing: '.04em', marginTop: 3 }}>{l}</p>
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* children */}
        <section style={{ padding: '16px var(--gutter) 4px' }}>
          <p className="micro" style={{ marginBottom: 10 }}>Children</p>
          <div style={{ borderRadius: 'var(--r-lg)', border: '1px solid var(--rule)', background: 'var(--surface)', overflow: 'hidden' }}>
            {DATA.profiles.map((p, i) => (
              <div key={p.id} style={{ display: 'flex', alignItems: 'center', gap: 13, padding: '14px 16px', borderTop: i ? '1px solid var(--rule-soft)' : 'none' }}>
                <Avatar name={p.name} tone={p.tone} size="lg" />
                <div style={{ flex: 1 }}>
                  <p style={{ fontWeight: 700, fontSize: 15.5 }}>{p.name} <span className="small" style={{ color: 'var(--ink-muted)', fontWeight: 500 }}>· age {p.age}</span></p>
                  <div style={{ marginTop: 7, maxWidth: 150 }}><Progress value={[0.7, 0.4, 0.2][i]} height={5} color={`var(--${p.tone})`} /></div>
                </div>
                <span className="tag" style={{ background: 'var(--bg-tint)' }}>{p.streak}🔥</span>
              </div>
            ))}
          </div>
        </section>

        {/* controls */}
        <section style={{ padding: '16px var(--gutter) 4px' }}>
          <p className="micro" style={{ marginBottom: 10 }}>Daily reading limit</p>
          <Segmented value={limit} onChange={setLimit} options={[{ value: '20', label: '20m' }, { value: '30', label: '30m' }, { value: '60', label: '60m' }, { value: 'off', label: 'Off' }]} />
        </section>

        <section style={{ padding: '18px var(--gutter) 4px' }}>
          <div style={{ borderRadius: 'var(--r-lg)', border: '1px solid var(--rule)', background: 'var(--surface)', overflow: 'hidden' }}>
            {[['moon', 'Bedtime cutoff', '9:00 PM', true], ['shield', 'Kid-safe mode', 'Always on', true], ['globe', 'Show transliteration', 'On', false]].map(([ic, l, d, on], i) => (
              <div key={l} style={{ display: 'flex', alignItems: 'center', gap: 13, padding: '13px 16px', borderTop: i ? '1px solid var(--rule-soft)' : 'none' }}>
                <span style={{ width: 34, height: 34, borderRadius: 10, background: 'var(--lapis-soft)', display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}><Icon name={ic} size={18} color="var(--lapis-deep)" /></span>
                <div style={{ flex: 1 }}>
                  <p style={{ fontWeight: 600, fontSize: 14.5 }}>{l}</p>
                  <p className="small" style={{ color: 'var(--ink-muted)' }}>{d}</p>
                </div>
                <span style={{ width: 46, height: 28, borderRadius: 99, background: on ? 'var(--mint)' : 'var(--rule-strong, var(--rule))', position: 'relative', transition: 'background .2s', flexShrink: 0 }}>
                  <span style={{ position: 'absolute', top: 3, left: on ? 21 : 3, width: 22, height: 22, borderRadius: 99, background: '#fff', boxShadow: 'var(--sh-xs)', transition: 'left .2s var(--ease)' }} />
                </span>
              </div>
            ))}
          </div>
        </section>

        <section style={{ padding: '18px var(--gutter)' }}>
          <div style={{ borderRadius: 'var(--r-lg)', overflow: 'hidden', position: 'relative', background: 'var(--lapis)', padding: '18px 18px', boxShadow: 'var(--sh-lapis)' }}>
            <Pattern motif="paisley" color="#fff" opacity={0.14} fade="tr" />
            <div style={{ position: 'relative', display: 'flex', alignItems: 'center', gap: 14 }}>
              <Icon name="star" size={30} color="#fff" />
              <div style={{ flex: 1 }}>
                <p style={{ color: '#fff', fontWeight: 800, fontSize: 16, fontFamily: 'var(--font-display)' }}>Rivana Plus</p>
                <p style={{ color: 'rgba(255,255,255,.75)', fontSize: 12.5, marginTop: 1 }}>Unlimited stories · all lullabies · offline</p>
              </div>
              <button className="btn btn-sm" style={{ background: '#fff', color: 'var(--lapis-deep)' }}>Manage</button>
            </div>
          </div>
        </section>
      </div>
    </div>
  );
}

/* ── Onboarding / profile picker ─────────────────────────── */
function ScreenOnboarding({ isSwitch }) {
  const nav = useNav();
  return (
    <div style={{ position: 'absolute', inset: 0, background: 'var(--bg)', display: 'flex', flexDirection: 'column' }}>
      <Pattern motif="paisley" color="var(--lapis)" opacity={0.05} fade="top" style={{ height: 420 }} />
      <div className="viewport" style={{ position: 'relative', display: 'flex', flexDirection: 'column' }}>
        <div style={{ paddingTop: 64, textAlign: 'center', padding: '64px var(--gutter) 0' }}>
          {isSwitch && <div style={{ textAlign: 'left', marginBottom: 4 }}><button className="iconbtn" onClick={nav.back}><Icon name="chevL" size={20} /></button></div>}
          <div style={{ display: 'inline-flex', alignItems: 'baseline', gap: 9, marginTop: isSwitch ? 4 : 18 }}>
            <span style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: 30, letterSpacing: '-0.03em' }}>Rivana</span>
            <span className="fa" style={{ fontSize: 19, color: 'var(--ink-muted)' }}>ریوانا</span>
          </div>
          <h1 className="h1" style={{ fontSize: 30, marginTop: 26 }}>Who's reading tonight?</h1>
          <p className="fa" style={{ fontSize: 16, color: 'var(--ink-muted)', marginTop: 6 }}>امشب کی قصه می‌خواند؟</p>
        </div>

        <div style={{ padding: '34px var(--gutter) 0', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
          {DATA.profiles.map((p, i) => (
            <Reveal key={p.id} delay={i * 70}>
              <button onClick={() => { nav.setProfile(p); nav.home('today'); }} style={{ width: '100%', border: 'none', cursor: 'pointer', borderRadius: 'var(--r-xl)', padding: '24px 16px', background: 'var(--surface)', boxShadow: 'var(--sh-sm)', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 12 }}>
                <Avatar name={p.name} tone={p.tone} style={{ width: 80, height: 80, fontSize: 34 }} />
                <div style={{ textAlign: 'center' }}>
                  <p className="h3" style={{ fontSize: 18 }}>{p.name}</p>
                  <p className="micro" style={{ letterSpacing: '.06em', marginTop: 2 }}>Age {p.age}</p>
                </div>
              </button>
            </Reveal>
          ))}
          <Reveal delay={DATA.profiles.length * 70}>
            <button style={{ width: '100%', minHeight: 168, border: '2px dashed var(--rule-strong, var(--rule))', cursor: 'pointer', borderRadius: 'var(--r-xl)', background: 'transparent', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 10, color: 'var(--ink-muted)' }}>
              <span style={{ width: 56, height: 56, borderRadius: 99, background: 'var(--bg-tint)', display: 'inline-flex', alignItems: 'center', justifyContent: 'center' }}><Icon name="plus" size={26} color="var(--ink-soft)" /></span>
              <span style={{ fontWeight: 700, fontSize: 14 }}>Add child</span>
            </button>
          </Reveal>
        </div>

        <div style={{ flex: 1, minHeight: 24 }} />
        <div style={{ padding: '20px var(--gutter) calc(env(safe-area-inset-bottom,0px) + 22px)', textAlign: 'center' }}>
          <button onClick={() => nav.push('parent', {})} style={{ background: 'none', border: 'none', cursor: 'pointer', display: 'inline-flex', alignItems: 'center', gap: 7, color: 'var(--ink-soft)', fontWeight: 700, fontSize: 14 }}>
            <Icon name="shield" size={17} color="var(--lapis)" /> I'm a parent
          </button>
        </div>
      </div>
    </div>
  );
}

Object.assign(window, { ScreenYou, ScreenParent, ScreenOnboarding });
