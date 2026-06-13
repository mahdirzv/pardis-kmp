/* global React, Icon, StoryArt, Pattern, Avatar, Progress, PushBar, SectionHead, StoryRow, Reveal, useNav, DATA */
/* Rivana · Rewards (trophies, word garden, characters) + Character detail */

function ScreenRewards() {
  const nav = useNav();
  const me = nav.profile;
  const earned = DATA.badges.filter((b) => b.earned);
  const mastered = DATA.words.filter((w) => w.mastery >= 1).length;
  const growing = DATA.words.length - mastered;
  const collectedChars = DATA.characters.filter((c) => c.collected);
  const days = ['M', 'T', 'W', 'T', 'F', 'S', 'S'];

  // progression model (derived, feels earned)
  const stars = 320;
  const level = 3, levelName = 'Story Keeper';
  const xpInto = 320, xpNeed = 500;
  const xp = xpInto / xpNeed;
  const nextBadge = DATA.badges.filter((b) => !b.earned).sort((a, b) => (b.progress || 0) - (a.progress || 0))[0];

  return (
    <React.Fragment>
      <ScreenBg motif="star8" color="var(--saffron-deep)" opacity={0.055} fade="top" size="150px 150px" />
      <div className="viewport" style={{ paddingBottom: 110 }}>
      <header style={{ padding: '58px var(--gutter) 6px', display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between' }}>
        <div>
          <h1 className="h1">Rewards</h1>
          <p className="fa" style={{ fontSize: 15, color: 'var(--ink-muted)', marginTop: 3 }}>جایزه‌ها و دستاوردها</p>
        </div>
        <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6, padding: '7px 13px', borderRadius: 'var(--r-pill)', background: 'var(--sun-soft)', color: 'var(--sun-deep)', fontWeight: 800, fontFamily: 'var(--font-display)', fontSize: 15 }}>
          <Icon name="starF" size={16} color="var(--sun-deep)" /> {stars}
        </span>
      </header>

      {/* ── Rank / level hero ─────────────────────────── */}
      <Reveal delay={40}>
        <section style={{ padding: '12px var(--gutter) 4px' }}>
          <div style={{ borderRadius: 'var(--r-xl)', overflow: 'hidden', position: 'relative', background: 'var(--grad-lapis, linear-gradient(135deg, #2436A1 0%, #4F2EB5 55%, #1A256E 100%))', padding: '22px 20px 20px', boxShadow: 'var(--sh-lapis)' }}>
            <Pattern motif="star8" color="#fff" opacity={0.12} fade="tr" size="120px 120px" />
            <div style={{ position: 'relative', display: 'flex', alignItems: 'center', gap: 18 }}>
              {/* level ring */}
              <div style={{ position: 'relative', width: 88, height: 88, flexShrink: 0 }}>
                <svg width="88" height="88" viewBox="0 0 88 88" style={{ transform: 'rotate(-90deg)' }}>
                  <circle cx="44" cy="44" r="39" fill="none" stroke="rgba(255,255,255,.2)" strokeWidth="6" />
                  <circle cx="44" cy="44" r="39" fill="none" stroke="var(--sun)" strokeWidth="6" strokeLinecap="round" strokeDasharray={`${xp * 245} 245`} />
                </svg>
                <div style={{ position: 'absolute', inset: 0, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', color: '#fff' }}>
                  <span style={{ fontFamily: 'var(--font-mono)', fontSize: 9, letterSpacing: '.12em', opacity: .7 }}>LVL</span>
                  <span style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: 30, lineHeight: 1 }}>{level}</span>
                </div>
              </div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <p className="eyebrow" style={{ color: 'rgba(255,255,255,.7)' }}>Your rank</p>
                <p style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: 23, color: '#fff', marginTop: 2, lineHeight: 1.05 }}>{levelName}</p>
                <div style={{ marginTop: 12 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 11, fontWeight: 700, color: 'rgba(255,255,255,.85)', marginBottom: 5, whiteSpace: 'nowrap' }}>
                    <span>{xpInto} / {xpNeed} XP</span><span>{xpNeed - xpInto} to Lvl {level + 1}</span>
                  </div>
                  <Progress value={xp} height={6} color="var(--sun)" dark />
                </div>
              </div>
            </div>
          </div>
        </section>
      </Reveal>

      {/* ── Quick stats strip ─────────────────────────── */}
      <Reveal delay={70}>
        <section style={{ padding: '14px var(--gutter) 4px' }}>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 10 }}>
            {[
              ['flameF', me.streak, 'streak', 'saffron'],
              ['feather', mastered, 'words', 'mint'],
              ['crown', collectedChars.length, 'heroes', 'lilac'],
              ['book', 6, 'stories', 'lapis'],
            ].map(([ic, n, l, t]) => (
              <div key={l} style={{ padding: '13px 6px', borderRadius: 'var(--r)', background: 'var(--surface)', border: '1px solid var(--rule)', textAlign: 'center' }}>
                <Icon name={ic} size={20} color={`var(--${t}-deep)`} />
                <p style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: 20, marginTop: 5, lineHeight: 1 }}>{n}</p>
                <p className="micro" style={{ letterSpacing: '.04em', marginTop: 3 }}>{l}</p>
              </div>
            ))}
          </div>
        </section>
      </Reveal>

      {/* ── Streak calendar ───────────────────────────── */}
      <Reveal delay={100}>
        <section style={{ padding: '14px var(--gutter) 4px' }}>
          <div style={{ borderRadius: 'var(--r-lg)', overflow: 'hidden', position: 'relative', background: 'var(--grad-saffron)', padding: '16px 18px', boxShadow: 'var(--sh-saffron)' }}>
            <Pattern motif="paisley" color="#fff" opacity={0.14} fade="tr" />
            <div style={{ position: 'relative', display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 14 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 9 }}>
                <Icon name="flameF" size={24} color="#fff" />
                <p style={{ color: '#fff', fontWeight: 800, fontFamily: 'var(--font-display)', fontSize: 16 }}>{me.streak}-night streak</p>
              </div>
              <span style={{ fontSize: 11.5, fontWeight: 700, color: 'rgba(255,255,255,.85)' }}>Keep it lit! 🔥</span>
            </div>
            <div style={{ position: 'relative', display: 'flex', justifyContent: 'space-between' }}>
              {days.map((d, i) => (
                <div key={i} style={{ textAlign: 'center', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 5 }}>
                  <span style={{ width: 32, height: 32, borderRadius: 99, display: 'inline-flex', alignItems: 'center', justifyContent: 'center', background: i < me.streak ? '#fff' : 'rgba(255,255,255,.22)', color: i < me.streak ? 'var(--saffron-deep)' : 'rgba(255,255,255,.7)' }}>
                    {i < me.streak ? <Icon name="flameF" size={16} color="var(--saffron-deep)" /> : <span style={{ fontSize: 12, fontWeight: 700 }}>{d}</span>}
                  </span>
                  <span style={{ fontSize: 10, fontWeight: 700, color: 'rgba(255,255,255,.7)' }}>{d}</span>
                </div>
              ))}
            </div>
          </div>
        </section>
      </Reveal>

      {/* ── Almost there: next badge ──────────────────── */}
      {nextBadge && (
        <Reveal delay={130}>
          <section style={{ padding: '16px var(--gutter) 4px' }}>
            <p className="micro" style={{ marginBottom: 10 }}>Almost there</p>
            <div style={{ display: 'flex', alignItems: 'center', gap: 15, padding: '16px', borderRadius: 'var(--r-lg)', background: 'var(--surface)', border: '1px solid var(--rule)', boxShadow: 'var(--sh-sm)' }}>
              <div style={{ position: 'relative', width: 60, height: 60, flexShrink: 0 }}>
                <svg width="60" height="60" viewBox="0 0 60 60" style={{ transform: 'rotate(-90deg)' }}>
                  <circle cx="30" cy="30" r="26" fill="none" stroke="var(--bg-tint)" strokeWidth="5" />
                  <circle cx="30" cy="30" r="26" fill="none" stroke={`var(--${nextBadge.tone})`} strokeWidth="5" strokeLinecap="round" strokeDasharray={`${(nextBadge.progress || 0) * 163} 163`} />
                </svg>
                <div style={{ position: 'absolute', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <Icon name={nextBadge.icon} size={26} color={`var(--${nextBadge.tone}-deep)`} />
                </div>
              </div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <p className="h3" style={{ fontSize: 16 }}>{nextBadge.label}</p>
                <p className="small" style={{ color: 'var(--ink-muted)', marginTop: 2 }}>{nextBadge.desc}</p>
                <p style={{ fontSize: 12, fontWeight: 700, color: `var(--${nextBadge.tone}-deep)`, marginTop: 6 }}>{Math.round((nextBadge.progress || 0) * 100)}% complete</p>
              </div>
            </div>
          </section>
        </Reveal>
      )}

      {/* ── Word garden ───────────────────────────────── */}
      <Reveal delay={160}>
        <section style={{ paddingTop: 22 }}>
          <SectionHead title="Word garden" fa="باغِ واژه‌ها" action="See all" onAction={() => {}} />
          <div style={{ padding: '0 var(--gutter)' }}>
            <div style={{ borderRadius: 'var(--r-lg)', border: '1px solid var(--rule)', background: 'var(--surface)', overflow: 'hidden', boxShadow: 'var(--sh-sm)' }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '14px 16px', background: 'var(--mint-tint)', borderBottom: '1px solid var(--mint-soft)' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 11 }}>
                  <span style={{ width: 38, height: 38, borderRadius: 12, background: 'var(--mint)', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}><Icon name="seed" size={20} color="#fff" /></span>
                  <div>
                    <p style={{ fontWeight: 800, fontSize: 17, color: 'var(--mint-deep)', fontFamily: 'var(--font-display)', lineHeight: 1.1 }}>{DATA.words.length} words growing</p>
                    <p style={{ fontSize: 11.5, color: 'var(--mint-deep)', opacity: .85, marginTop: 1 }}>{mastered} mastered · {growing} sprouting</p>
                  </div>
                </div>
                <button className="btn btn-sm" style={{ background: 'var(--mint)', color: '#fff' }}><Icon name="sparkle" size={15} color="#fff" /> Practice</button>
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 1, background: 'var(--rule-soft)' }}>
                {DATA.words.map((w) => (
                  <div key={w.tr} style={{ background: 'var(--surface)', padding: '15px 8px 13px', textAlign: 'center' }}>
                    <div style={{ position: 'relative', width: 46, height: 46, margin: '0 auto' }}>
                      <svg width="46" height="46" viewBox="0 0 46 46" style={{ transform: 'rotate(-90deg)' }}>
                        <circle cx="23" cy="23" r="20" fill="none" stroke="var(--bg-tint)" strokeWidth="4" />
                        <circle cx="23" cy="23" r="20" fill="none" stroke={w.mastery >= 1 ? 'var(--mint)' : 'var(--sun)'} strokeWidth="4" strokeLinecap="round" strokeDasharray={`${w.mastery * 125.6} 125.6`} />
                      </svg>
                      <span className="fa" style={{ position: 'absolute', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 17, color: 'var(--ink)', fontWeight: 600 }}>{w.fa}</span>
                      {w.mastery >= 1 && <span style={{ position: 'absolute', right: -2, bottom: -2, width: 17, height: 17, borderRadius: 99, background: 'var(--mint)', display: 'flex', alignItems: 'center', justifyContent: 'center', border: '2px solid var(--surface)' }}><Icon name="check" size={9} color="#fff" stroke={3} /></span>}
                    </div>
                    <p style={{ fontFamily: 'var(--font-mono)', fontSize: 10.5, color: 'var(--ink-muted)', marginTop: 7 }}>{w.tr}</p>
                    <p style={{ fontSize: 11.5, color: 'var(--ink-soft)', fontWeight: 600 }}>{w.en}</p>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </section>
      </Reveal>

      {/* ── Badges ────────────────────────────────────── */}
      <Reveal delay={190}>
        <section style={{ paddingTop: 24 }}>
          <SectionHead title="Badges" fa="نشان‌ها" action={`${earned.length}/${DATA.badges.length}`} />
          <div style={{ padding: '0 var(--gutter)', display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 12 }}>
            {DATA.badges.map((b) => (
              <div key={b.id} style={{ textAlign: 'center' }}>
                <div style={{ width: '100%', aspectRatio: '1', borderRadius: 20, position: 'relative', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 4, overflow: 'hidden',
                  background: b.earned ? `var(--${b.tone}-soft)` : 'var(--bg-tint)',
                  border: b.earned ? `1px solid var(--${b.tone}-soft)` : '1px dashed var(--rule-strong)' }}>
                  {b.earned && <Pattern motif="rosette" color={`var(--${b.tone}-deep)`} opacity={0.10} size="80px 80px" />}
                  {b.earned
                    ? <Icon name={b.icon} size={32} color={`var(--${b.tone}-deep)`} style={{ position: 'relative' }} />
                    : <Icon name="lock" size={24} color="var(--ink-faint)" />}
                  {!b.earned && b.progress != null && (
                    <div style={{ position: 'absolute', left: 12, right: 12, bottom: 11 }}><Progress value={b.progress} height={4} color={`var(--${b.tone})`} /></div>
                  )}
                </div>
                <p style={{ fontSize: 12, fontWeight: 700, marginTop: 8, color: b.earned ? 'var(--ink)' : 'var(--ink-muted)' }}>{b.label}</p>
              </div>
            ))}
          </div>
        </section>
      </Reveal>

      {/* ── Heroes collected ──────────────────────────── */}
      <Reveal delay={220}>
        <section style={{ paddingTop: 24 }}>
          <SectionHead title="Heroes met" fa="پهلوانانِ آشنا" action={`${collectedChars.length}/${DATA.characters.length}`} />
          <div className="shelf">
            {DATA.characters.map((c) => (
              <button key={c.id} onClick={() => nav.push('character', { id: c.id })} style={{ width: 96, background: 'none', border: 'none', cursor: 'pointer', textAlign: 'center', padding: 0 }}>
                <div style={{ width: 96, height: 120, borderRadius: 20, overflow: 'hidden', position: 'relative', boxShadow: 'var(--sh-sm)', filter: c.collected ? 'none' : 'grayscale(.75)', opacity: c.collected ? 1 : .7 }}>
                  <StoryArt kind={c.scene} />
                  <Pattern motif="rosette" color="#fff" opacity={0.14} />
                  {!c.collected && <div style={{ position: 'absolute', inset: 0, background: 'rgba(15,12,20,.4)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}><Icon name="lock" size={22} color="#fff" /></div>}
                </div>
                <p style={{ fontSize: 13, fontWeight: 700, marginTop: 8, color: c.collected ? 'var(--ink)' : 'var(--ink-muted)' }}>{c.name}</p>
                <p className="fa" style={{ fontSize: 11.5, color: 'var(--ink-muted)' }}>{c.nameFa}</p>
              </button>
            ))}
          </div>
        </section>
      </Reveal>
      <div style={{ height: 16 }} />
      </div>
    </React.Fragment>
  );
}

/* ── Character detail ────────────────────────────────────── */
function ScreenCharacter({ id }) {
  const nav = useNav();
  const c = DATA.charById[id] || DATA.characters[0];
  const inStories = DATA.stories.filter((s) => (s.chars || []).includes(c.id));

  return (
    <div className="screen-pop" style={{ position: 'absolute', inset: 0, background: 'var(--bg)' }}>
      <ScreenBg motif="vine" color={`var(--${c.tone}-deep)`} opacity={0.05} fade="bottom" size="300px 150px" />
      <div className="viewport" style={{ paddingBottom: 30 }}>
        <div style={{ position: 'relative', height: 400 }}>
          <StoryArt kind={c.scene} />
          <Pattern motif="rosette" color="#fff" opacity={0.14} fade="bl" />
          <div style={{ position: 'absolute', inset: 0, background: 'linear-gradient(180deg, rgba(15,12,20,.25) 0%, transparent 30%, transparent 55%, var(--bg) 100%)' }} />
          <div style={{ position: 'absolute', top: 44, left: 0, right: 0 }}>
            <PushBar onBack={nav.back} transparent action={<button className="iconbtn" style={{ background: 'rgba(255,255,255,.7)', backdropFilter: 'blur(8px)', border: '1px solid rgba(20,17,27,.06)' }}><Icon name="heart" size={19} /></button>} />
          </div>
          <div style={{ position: 'absolute', left: 'var(--gutter)', right: 'var(--gutter)', bottom: 20 }}>
            {c.collected
              ? <span className="tag tag-mint" style={{ marginBottom: 10 }}><Icon name="check" size={12} /> Collected</span>
              : <span className="tag" style={{ marginBottom: 10, background: 'rgba(20,17,27,.7)', color: '#fff' }}><Icon name="lock" size={11} /> Not yet met</span>}
            <h1 className="h1" style={{ fontSize: 36 }}>{c.name}</h1>
            <p className="fa" style={{ fontSize: 22, color: 'var(--ink-soft)', marginTop: 2 }}>{c.nameFa}</p>
            <p className="eyebrow" style={{ marginTop: 8 }}>{c.role}</p>
          </div>
        </div>

        <section style={{ padding: '8px var(--gutter) 4px' }}>
          <p className="lead" style={{ color: 'var(--ink)' }}>{c.bio}</p>
        </section>

        <section style={{ padding: '12px var(--gutter)' }}>
          <div style={{ display: 'flex', gap: 10 }}>
            {[['book', inStories.length, 'stories'], ['feather', c.stories, 'appearances'], ['star', c.collected ? '★' : '–', 'badge']].map(([ic, n, l]) => (
              <div key={l} style={{ flex: 1, padding: '14px 10px', borderRadius: 'var(--r)', background: 'var(--surface)', border: '1px solid var(--rule)', textAlign: 'center' }}>
                <Icon name={ic} size={20} color="var(--accent)" />
                <p style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: 20, marginTop: 4 }}>{n}</p>
                <p className="micro" style={{ letterSpacing: '.06em' }}>{l}</p>
              </div>
            ))}
          </div>
        </section>

        <section style={{ paddingTop: 12 }}>
          <p className="micro" style={{ padding: '0 var(--gutter) 8px' }}>Appears in</p>
          {inStories.map((s, i) => (
            <React.Fragment key={s.id}>
              <StoryRow story={s} onTap={() => nav.push('detail', { id: s.id })} />
              {i < inStories.length - 1 && <div style={{ height: 1, background: 'var(--rule-soft)', margin: '0 var(--gutter)' }} />}
            </React.Fragment>
          ))}
        </section>
      </div>
    </div>
  );
}

Object.assign(window, { ScreenRewards, ScreenCharacter });
