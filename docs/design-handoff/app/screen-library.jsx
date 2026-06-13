/* global React, Icon, StoryArt, Pattern, Cover, SectionHead, StoryRow, Segmented, Reveal, useNav, DATA */
/* Rivana · Library (browse) */

const { useState: useStateLib } = React;

function ScreenLibrary() {
  const nav = useNav();
  const [filter, setFilter] = useStateLib('All');
  const [view, setView] = useStateLib('grid');

  const filters = ['All', 'New', 'Shahnameh Heroes', 'Creatures of Myth', 'Voyages', 'Love & Courage'];
  let list = DATA.stories;
  if (filter === 'New') list = list.filter((s) => s.new);
  else if (filter !== 'All') list = list.filter((s) => s.collection === filter);

  return (
    <React.Fragment>
      <ScreenBg motif="vine" color="var(--lapis)" opacity={0.05} fade="top" size="300px 150px" />
      <div className="viewport" style={{ paddingBottom: 110 }}>
      <header style={{ padding: '56px var(--gutter) 6px' }}>
        <div style={{ display: 'flex', alignItems: 'flex-end', justifyContent: 'space-between' }}>
          <div>
            <h1 className="h1">Library</h1>
            <p className="fa" style={{ fontSize: 15, color: 'var(--ink-muted)', marginTop: 3 }}>کتابخانه‌ی قصه‌ها</p>
          </div>
          <Segmented value={view} onChange={setView} options={[{ value: 'grid', label: '◧' }, { value: 'list', label: '☰' }]} />
        </div>
      </header>

      {/* search */}
      <div style={{ padding: '10px var(--gutter) 4px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 10, height: 48, padding: '0 16px', borderRadius: 'var(--r-pill)', background: 'var(--surface)', border: '1px solid var(--rule)', boxShadow: 'var(--sh-xs)' }}>
          <Icon name="search" size={19} color="var(--ink-muted)" />
          <span style={{ flex: 1, color: 'var(--ink-faint)', fontSize: 15 }}>Search heroes, words, voyages…</span>
          <Icon name="mic" size={18} color="var(--accent)" />
        </div>
      </div>

      {/* filter chips */}
      <div className="chiprow">
        {filters.map((f) => (
          <button key={f} className={'chip' + (f === filter ? ' is-active' : '')} onClick={() => setFilter(f)}>{f}</button>
        ))}
      </div>

      {view === 'grid' ? (
        <section style={{ padding: '6px var(--gutter) 0' }}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 18 }}>
            {list.map((s, i) => (
              <Reveal key={s.id} delay={i * 40}>
                <Cover story={s} w={163} onTap={() => nav.push('detail', { id: s.id })} showProgress motif />
              </Reveal>
            ))}
          </div>
        </section>
      ) : (
        <section style={{ paddingTop: 2 }}>
          {list.map((s, i) => (
            <React.Fragment key={s.id}>
              <StoryRow story={s} onTap={() => nav.push('detail', { id: s.id })} />
              {i < list.length - 1 && <div style={{ height: 1, background: 'var(--rule-soft)', margin: '0 var(--gutter)' }} />}
            </React.Fragment>
          ))}
        </section>
      )}

      {/* age band helper */}
      <section style={{ padding: '26px var(--gutter) 8px' }}>
        <SectionHead title="By age" fa="بر اساس سن" />
        <div style={{ display: 'flex', gap: 10 }}>
          {[{ a: '4–6', t: 'mint', l: 'Little ones' }, { a: '6–9', t: 'saffron', l: 'Readers' }, { a: '8–11', t: 'lapis', l: 'Explorers' }].map((b) => (
            <button key={b.a} style={{ flex: 1, border: 'none', cursor: 'pointer', borderRadius: 'var(--r)', padding: '16px 12px', background: `var(--${b.t}-soft)`, textAlign: 'left' }}>
              <p style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: 21, color: `var(--${b.t}-deep)` }}>{b.a}</p>
              <p style={{ fontSize: 11.5, fontWeight: 600, color: `var(--${b.t}-deep)`, opacity: .8, marginTop: 2 }}>{b.l}</p>
            </button>
          ))}
        </div>
      </section>
      </div>
    </React.Fragment>
  );
}

window.ScreenLibrary = ScreenLibrary;
