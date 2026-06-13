/* global React, ReactDOM, Nav, PhoneFrame, useTweaks, TweaksPanel, TweakSection, TweakRadio, TweakColor, TweakToggle,
   ScreenToday, ScreenLibrary, ScreenDetail, ScreenReader, ScreenFinish, ScreenBedtime, ScreenLullaby,
   ScreenRewards, ScreenCharacter, ScreenYou, ScreenParent, ScreenOnboarding, DATA */
/* Rivana · app shell + navigation */

const { useState: useAS, useEffect: useAE } = React;

const TWEAK_DEFAULTS = /*EDITMODE-BEGIN*/{
  "accent": "saffron",
  "textSize": "medium",
  "pace": "normal",
  "covers": true,
  "theme": "light"
}/*EDITMODE-END*/;

const PACE = { slow: 360, normal: 260, fast: 180 };
const TEXT = { small: 20, medium: 22, large: 25 };
const ACCENT_HEX = { saffron: '#F08A2D', lapis: '#2436A1', mint: '#2FA876', lilac: '#8B6FE6', rose: '#E1547A' };
const HEX_ACCENT = { '#F08A2D': 'saffron', '#2436A1': 'lapis', '#2FA876': 'mint', '#8B6FE6': 'lilac', '#E1547A': 'rose' };

function App() {
  const [t, setTweak] = useTweaks(TWEAK_DEFAULTS);
  const [started, setStarted] = useAS(false);
  const [tab, setTab] = useAS('today');
  const [stack, setStack] = useAS([]);            // [{screen, params}]
  const [profile, setProfile] = useAS(DATA.profiles[0]);

  const themeDark = t.theme === 'dark';
  useAE(() => { document.documentElement.dataset.accent = t.accent; }, [t.accent]);
  useAE(() => { document.documentElement.dataset.theme = t.theme; }, [t.theme]);
  window.__tw = { pace: PACE[t.pace] || 260, textSize: TEXT[t.textSize] || 22, covers: t.covers };

  const nav = {
    profile, setProfile,
    tweaks: { pace: PACE[t.pace] || 260, textSize: TEXT[t.textSize] || 22, covers: t.covers },
    push: (screen, params) => setStack((s) => [...s, { screen, params: params || {} }]),
    back: () => setStack((s) => s.slice(0, -1)),
    go: (tb) => { setStack([]); setTab(tb); },
    home: (tb) => { setStack([]); setTab(tb || 'today'); },
    theme: t.theme,
    setTheme: (v) => setTweak('theme', v),
  };

  // resolve what to render
  let content, dark = false, tabbar = true;

  if (!started) {
    content = <ScreenOnboarding />;
    tabbar = false;
  } else if (stack.length) {
    const top = stack[stack.length - 1];
    tabbar = false;
    switch (top.screen) {
      case 'detail':     content = <ScreenDetail id={top.params.id} />; break;
      case 'reader':     content = <ScreenReader id={top.params.id} />; break;
      case 'finish':     content = <ScreenFinish id={top.params.id} />; dark = true; break;
      case 'lullaby':    content = <ScreenLullaby id={top.params.id} />; dark = true; break;
      case 'character':  content = <ScreenCharacter id={top.params.id} />; break;
      case 'parent':     content = <ScreenParent />; break;
      case 'onboarding': content = <ScreenOnboarding isSwitch />; break;
      default:           content = <div />;
    }
  } else {
    switch (tab) {
      case 'today':   content = <ScreenToday />; break;
      case 'library': content = <ScreenLibrary />; break;
      case 'bedtime': content = <ScreenBedtime />; dark = true; break;
      case 'rewards': content = <ScreenRewards />; break;
      case 'you':     content = <ScreenYou />; break;
      default:        content = <ScreenToday />;
    }
  }

  // onboarding "start" hook — wrap setProfile to mark started
  const navValue = {
    ...nav,
    setProfile: (p) => { setProfile(p); setStarted(true); },
    home: (tb) => { setStack([]); setTab(tb || 'today'); setStarted(true); },
  };
  window.__nav = navValue;

  return (
    <Nav.Provider value={navValue}>
      <PhoneFrame dark={dark} statusDark={dark || themeDark} tabDark={dark || themeDark} tabbar={tabbar} tab={tab} onTab={(tb) => nav.go(tb)}>
        <div className="screen" key={(started ? '' : 'onb-') + tab + '-' + stack.map((s) => s.screen).join('/')} style={{ position: 'absolute', inset: 0 }}>
          {content}
        </div>
      </PhoneFrame>

      <TweaksPanel title="Tweaks">
        <TweakSection label="Brand" />
        <TweakColor label="Accent" value={ACCENT_HEX[t.accent]}
          options={['#F08A2D', '#2436A1', '#2FA876', '#8B6FE6', '#E1547A']}
          onChange={(hex) => setTweak('accent', HEX_ACCENT[hex] || 'saffron')} />
        <TweakSection label="Reading" />
        <TweakRadio label="Story text" value={t.textSize} options={['small', 'medium', 'large']} onChange={(v) => setTweak('textSize', v)} />
        <TweakRadio label="Narration pace" value={t.pace} options={['slow', 'normal', 'fast']} onChange={(v) => setTweak('pace', v)} />
        <TweakToggle label="Cover badges" value={t.covers} onChange={(v) => setTweak('covers', v)} />
        <TweakSection label="Appearance" />
        <TweakToggle label="Dark mode" value={themeDark} onChange={(v) => setTweak('theme', v ? 'dark' : 'light')} />
      </TweaksPanel>
    </Nav.Provider>
  );
}

ReactDOM.createRoot(document.getElementById('root')).render(<App />);
