/* global React, Icon, StoryArt, Pattern, Avatar, Progress, PushBar, Reveal, useNav, DATA */
/* Rivana · Story detail */

function ScreenDetail({ id }) {
  const nav = useNav();
  const s = DATA.storyById[id] || DATA.stories[0];
  const chars = (s.chars || []).map((c) => DATA.charById[c]).filter(Boolean);
  const resume = s.progress > 0;

  const chapters = [
    { n: 1, t: 'The Hills of Persia', m: 3, done: resume },
    { n: 2, t: 'A Horse of Fire', m: 4, done: false, active: resume },
    { n: 3, t: 'The Young Warrior', m: 2, done: false, locked: false },
    { n: 4, t: 'Under the Stars', m: 2, done: false, locked: true },
  ];

  return (
    <div className="screen-pop" style={{ position: 'absolute', inset: 0, background: 'var(--bg)' }}>
      <ScreenBg motif="paisley" color={`var(--${s.tone}-deep)`} opacity={0.045} fade="bottom" size="220px 220px" />
      <div className="viewport" style={{ paddingBottom: 116 }}>
        {/* hero */}
        <div style={{ position: 'relative', height: 396 }}>
          <StoryArt kind={s.scene} />
          <Pattern motif="paisley" color="#fff" opacity={0.12} fade="bl" />
          <div style={{ position: 'absolute', inset: 0, background: 'linear-gradient(180deg, rgba(15,12,20,.28) 0%, transparent 24%, transparent 52%, var(--bg) 100%)' }} />
          <div style={{ position: 'absolute', top: 44, left: 0, right: 0 }}>
            <PushBar onBack={nav.back} transparent action={<button className="iconbtn" style={{ background: 'rgba(255,255,255,.7)', backdropFilter: 'blur(8px)', border: '1px solid rgba(20,17,27,.06)' }}><Icon name="heart" size={19} color="var(--ink)" /></button>} />
          </div>
          <div style={{ position: 'absolute', left: 'var(--gutter)', right: 'var(--gutter)', bottom: 18 }}>
            <span className={`tag tag-${s.tone}`} style={{ marginBottom: 10 }}><Icon name="compass" size={12} /> {s.collection}</span>
            <h1 className="h1" style={{ fontSize: 32 }}>{s.title}</h1>
            <p className="fa" style={{ fontSize: 18, color: 'var(--ink-soft)', marginTop: 4 }}>{s.titleFa}</p>
          </div>
        </div>

        {/* meta chips */}
        <div style={{ display: 'flex', gap: 8, padding: '4px var(--gutter) 0', flexWrap: 'wrap' }}>
          {[['clock', `${s.minutes} min`], ['book', `${s.pages} pages`], ['user', `Age ${s.age}`], ['feather', `${s.vocab.length} words`]].map(([ic, lb]) => (
            <span key={lb} className="tag" style={{ background: 'var(--surface)', border: '1px solid var(--rule)', color: 'var(--ink-soft)', padding: '7px 11px' }}>
              <Icon name={ic} size={13} color="var(--ink-muted)" /> {lb}
            </span>
          ))}
        </div>

        {/* synopsis */}
        <Reveal delay={60}>
          <section style={{ padding: '18px var(--gutter) 4px' }}>
            <p className="lead" style={{ color: 'var(--ink)' }}>{s.blurb}</p>
            <p className="fa" style={{ fontSize: 15, color: 'var(--ink-muted)', marginTop: 10 }}>{s.blurbFa}</p>
          </section>
        </Reveal>

        {/* narrator + audio note */}
        <section style={{ padding: '10px var(--gutter)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 11, padding: '12px 14px', borderRadius: 'var(--r)', background: 'var(--accent-tint)', border: '1px solid var(--accent-soft)' }}>
            <span style={{ width: 40, height: 40, borderRadius: 99, background: 'var(--accent)', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}><Icon name="mic" size={18} color="#fff" /></span>
            <div style={{ flex: 1, lineHeight: 1.3 }}>
              <p style={{ fontSize: 13.5, fontWeight: 700, color: 'var(--accent-deep)' }}>Narrated in English & Persian</p>
              <p style={{ fontSize: 12, color: 'var(--accent-deep)', opacity: .8 }}>Word-by-word read-along · tap any Persian word</p>
            </div>
            <Icon name="translate" size={20} color="var(--accent-deep)" />
          </div>
        </section>

        {/* characters */}
        <section style={{ paddingTop: 14 }}>
          <p className="micro" style={{ padding: '0 var(--gutter) 10px' }}>Who you'll meet</p>
          <div className="shelf" style={{ gap: 16 }}>
            {chars.map((c) => (
              <button key={c.id} onClick={() => nav.push('character', { id: c.id })} style={{ background: 'none', border: 'none', cursor: 'pointer', width: 76, textAlign: 'center' }}>
                <div style={{ width: 76, height: 76, borderRadius: 22, overflow: 'hidden', position: 'relative', boxShadow: 'var(--sh-sm)', margin: '0 auto' }}>
                  <StoryArt kind={c.scene} />
                  <Pattern motif="rosette" color="#fff" opacity={0.14} />
                </div>
                <p style={{ fontSize: 13, fontWeight: 700, marginTop: 7 }}>{c.name}</p>
                <p className="fa" style={{ fontSize: 11.5, color: 'var(--ink-muted)' }}>{c.nameFa}</p>
              </button>
            ))}
          </div>
        </section>

        {/* what you'll learn */}
        <section style={{ padding: '20px var(--gutter) 4px' }}>
          <p className="micro" style={{ marginBottom: 10 }}>Words you'll learn</p>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
            {s.vocab.map((w) => (
              <span key={w} className="tag tag-lapis" style={{ padding: '8px 13px', fontSize: 13, fontFamily: 'var(--font-mono)' }}>{w}</span>
            ))}
          </div>
        </section>

        {/* chapters */}
        <section style={{ padding: '18px var(--gutter) 8px' }}>
          <p className="micro" style={{ marginBottom: 8 }}>Chapters</p>
          <div style={{ borderRadius: 'var(--r-lg)', border: '1px solid var(--rule)', overflow: 'hidden', background: 'var(--surface)' }}>
            {chapters.map((ch, i) => (
              <button key={ch.n} onClick={() => !ch.locked && nav.push('reader', { id: s.id })} style={{ width: '100%', display: 'flex', alignItems: 'center', gap: 13, padding: '14px 15px', border: 'none', borderTop: i ? '1px solid var(--rule-soft)' : 'none', background: ch.active ? 'var(--accent-tint)' : 'transparent', cursor: ch.locked ? 'default' : 'pointer', textAlign: 'left', opacity: ch.locked ? .5 : 1 }}>
                <span style={{ width: 34, height: 34, borderRadius: 11, flexShrink: 0, display: 'inline-flex', alignItems: 'center', justifyContent: 'center', background: ch.done ? 'var(--mint)' : ch.active ? 'var(--accent)' : 'var(--bg-tint)', color: ch.done || ch.active ? '#fff' : 'var(--ink-muted)' }}>
                  {ch.done ? <Icon name="check" size={17} color="#fff" /> : ch.locked ? <Icon name="lock" size={15} /> : <span style={{ fontWeight: 800, fontFamily: 'var(--font-display)' }}>{ch.n}</span>}
                </span>
                <div style={{ flex: 1 }}>
                  <p style={{ fontWeight: 700, fontSize: 15 }}>{ch.t}</p>
                  <p className="micro" style={{ letterSpacing: '.06em', marginTop: 1 }}>{ch.m} min{ch.active ? ' · continue' : ch.done ? ' · done' : ''}</p>
                </div>
                {!ch.locked && <Icon name={ch.active ? 'play' : 'chevR'} size={ch.active ? 16 : 18} color={ch.active ? 'var(--accent)' : 'var(--ink-faint)'} />}
              </button>
            ))}
          </div>
        </section>
      </div>

      {/* sticky CTA bar */}
      <div style={{ position: 'absolute', left: 0, right: 0, bottom: 0, padding: '14px var(--gutter) calc(env(safe-area-inset-bottom,0px) + 18px)', display: 'flex', gap: 10, background: 'linear-gradient(180deg, transparent, var(--bg) 28%)' }}>
        <button className="btn btn-accent btn-lg" style={{ flex: 1 }} onClick={() => nav.push('reader', { id: s.id })}>
          <Icon name="play" size={18} color="#fff" /> {resume ? 'Continue reading' : 'Start reading'}
        </button>
        <button className="btn btn-ghost btn-lg" style={{ width: 56, padding: 0 }} onClick={() => nav.push('reader', { id: s.id })} aria-label="Listen">
          <Icon name="headphone" size={22} />
        </button>
      </div>
    </div>
  );
}

window.ScreenDetail = ScreenDetail;
