/* Rivana · content model — Persian myth storybook
   Brand voice: "Persia", not "Iran" (user-facing). */

window.DATA = (function () {
  /* ── Reader pages for the hero story (Rostam & Sohrab) ──
     Each page: scene art, English prose, a Farsi line, and a
     glossary of tappable words that appear in the Farsi line. */
  const rostamPages = [
    {
      art: 'dawn',
      en: "Long ago, in the green hills of Persia, lived the mightiest hero the world had ever known. His name was Rostam.",
      fa: "زمانی دور، در تپه‌های سبز پارس، بزرگ‌ترین پهلوانِ جهان زندگی می‌کرد. نامِ او رُستم بود.",
      glossary: [
        { fa: 'پهلوان', tr: 'pahlavân', en: 'champion / hero' },
        { fa: 'پارس', tr: 'Pârs', en: 'Persia' },
      ],
    },
    {
      art: 'hills',
      en: "Rostam had a horse of fire and shadow named Rakhsh. Together they crossed deserts, rivers, and seven great trials.",
      fa: "رُستم اسبی از آتش و سایه داشت به نامِ رخش. آن‌ها با هم از بیابان‌ها، رودها و هفت‌خوانِ بزرگ گذشتند.",
      glossary: [
        { fa: 'اسب', tr: 'asb', en: 'horse' },
        { fa: 'آتش', tr: 'âtash', en: 'fire' },
        { fa: 'هفت‌خوان', tr: 'haft-khân', en: 'the seven trials' },
      ],
    },
    {
      art: 'vase',
      en: "Far away, a brave young warrior was growing up. Her heart was as bold as a lion's. She was searching for the father she had never met.",
      fa: "دور از آن‌جا، جنگجویی جوان و دلیر بزرگ می‌شد. دلِ او مانندِ شیر بی‌باک بود. او به دنبالِ پدری می‌گشت که هرگز ندیده بود.",
      glossary: [
        { fa: 'جنگجو', tr: 'jangju', en: 'warrior' },
        { fa: 'دلیر', tr: 'delir', en: 'brave' },
        { fa: 'شیر', tr: 'shir', en: 'lion' },
      ],
    },
    {
      art: 'night',
      en: "Under a sky full of stars, the two heroes would meet. Neither knew the other's name. The bravest story of Persia was about to begin.",
      fa: "زیرِ آسمانی پُر از ستاره، آن دو پهلوان به هم می‌رسیدند. هیچ‌کدام نامِ دیگری را نمی‌دانست. دلیرانه‌ترین داستانِ پارس می‌خواست آغاز شود.",
      glossary: [
        { fa: 'آسمان', tr: 'âsemân', en: 'sky' },
        { fa: 'ستاره', tr: 'setâre', en: 'star' },
        { fa: 'داستان', tr: 'dâstân', en: 'story' },
      ],
    },
  ];

  const stories = [
    {
      id: 'rostam', title: 'Rostam & the Young Warrior', titleFa: 'رستم و جنگجوی جوان',
      scene: 'dawn', tone: 'saffron', collection: 'Shahnameh Heroes', age: '6–9',
      minutes: 11, pages: 18, level: 'Folio I', new: false, progress: 0.34,
      blurb: "Persia's greatest champion meets a fearless young warrior beneath a field of stars — without knowing they share the same blood.",
      blurbFa: 'بزرگ‌ترین پهلوانِ پارس با جنگجویی جوان و بی‌باک روبه‌رو می‌شود.',
      chars: ['rostam', 'tahmineh', 'rakhsh'],
      vocab: ['pahlavân', 'asb', 'shir', 'setâre'],
      readerPages: rostamPages,
    },
    {
      id: 'kaveh', title: 'Kaveh & the Banner of Dawn', titleFa: 'کاوه و درفشِ کاویانی',
      scene: 'flame', tone: 'rose', collection: 'Shahnameh Heroes', age: '7–10',
      minutes: 13, pages: 20, level: 'Folio I', new: true, progress: 0,
      blurb: 'A humble blacksmith raises his leather apron as a flag and frees Persia from the serpent-king Zahak.',
      blurbFa: 'آهنگری ساده پیش‌بندِ خود را چون پرچم بالا می‌برد.',
      chars: ['kaveh', 'zahak'],
      vocab: ['âhangar', 'derafsh', 'âzâdi'],
    },
    {
      id: 'simurgh', title: 'The Simurgh & the Lost Child', titleFa: 'سیمرغ و کودکِ گمشده',
      scene: 'hills', tone: 'lilac', collection: 'Creatures of Myth', age: '4–7',
      minutes: 8, pages: 14, level: 'Folio II', new: true, progress: 0,
      blurb: 'A baby left on a mountain is raised by the wisest, kindest bird in the world — the feather-bright Simurgh.',
      blurbFa: 'کودکی که بر کوه رها شده، توسطِ داناترین پرنده پرورش می‌یابد.',
      chars: ['simurgh', 'zal'],
      vocab: ['parande', 'kuh', 'par'],
    },
    {
      id: 'bijan', title: 'Bijan & Manijeh', titleFa: 'بیژن و منیژه',
      scene: 'vase', tone: 'lapis', collection: 'Love & Courage', age: '8–11',
      minutes: 14, pages: 22, level: 'Folio II', new: false, progress: 0,
      blurb: 'A knight is trapped in a deep pit, and only the cleverness of a princess can set him free.',
      blurbFa: 'دختری باهوش، دلاوری را از چاهی ژرف می‌رهاند.',
      chars: ['bijan', 'manijeh'],
      vocab: ['châh', 'kelid', 'doost'],
    },
    {
      id: 'sindbad', title: 'Sindbad of the Seven Seas', titleFa: 'سندباد و هفت دریا',
      scene: 'sea', tone: 'mint', collection: 'Voyages', age: '5–8',
      minutes: 10, pages: 16, level: 'Folio II', new: false, progress: 0,
      blurb: 'Set sail with a curious traveller across roaring waves toward islands no map has ever named.',
      blurbFa: 'با مسافری کنجکاو بر موج‌های دریا سفر کن.',
      chars: ['sindbad'],
      vocab: ['daryâ', 'kashti', 'safar'],
    },
    {
      id: 'anahita', title: 'Anahita, Keeper of Waters', titleFa: 'آناهیتا، نگهبانِ آب‌ها',
      scene: 'night', tone: 'lapis', collection: 'Creatures of Myth', age: '6–9',
      minutes: 9, pages: 15, level: 'Folio I', new: false, progress: 0,
      blurb: 'The shining guardian of rivers and rain teaches a drought-struck village the secret of giving.',
      blurbFa: 'نگهبانِ درخشانِ رودها رازِ بخشش را می‌آموزد.',
      chars: ['anahita'],
      vocab: ['âb', 'bârân', 'rud'],
    },
  ];

  const characters = [
    { id: 'rostam', name: 'Rostam', nameFa: 'رستم', tone: 'saffron', scene: 'dawn', role: 'Champion of Persia', collected: true, stories: 3,
      bio: 'The mightiest of all Persian heroes — strong as a mountain, loyal to his king, and rider of the great horse Rakhsh.' },
    { id: 'simurgh', name: 'Simurgh', nameFa: 'سیمرغ', tone: 'lilac', scene: 'hills', role: 'The Wise Bird', collected: true, stories: 2,
      bio: 'A vast, kind bird whose feathers glow like dawn. She heals the wounded and raises lost children on Mount Damavand.' },
    { id: 'anahita', name: 'Anahita', nameFa: 'آناهیتا', tone: 'lapis', scene: 'night', role: 'Keeper of Waters', collected: true, stories: 1,
      bio: 'The radiant guardian of rivers, rain, and wisdom — bringer of life to every field in Persia.' },
    { id: 'kaveh', name: 'Kaveh', nameFa: 'کاوه', tone: 'rose', scene: 'flame', role: 'The Blacksmith', collected: true, stories: 1,
      bio: 'An ordinary blacksmith whose courage sparked a revolt and whose apron became the banner of a free Persia.' },
    { id: 'zahak', name: 'Zahak', nameFa: 'ضحاک', tone: 'rose', scene: 'flame', role: 'The Serpent King', collected: false, stories: 1,
      bio: 'A shadowed king with serpents at his shoulders — the villain Kaveh and Fereydun rise to defeat.' },
    { id: 'bijan', name: 'Bijan', nameFa: 'بیژن', tone: 'lapis', scene: 'vase', role: 'The Brave Knight', collected: false, stories: 1,
      bio: 'A bold young knight of Persia whose daring leads him into danger — and into a legendary love.' },
    { id: 'manijeh', name: 'Manijeh', nameFa: 'منیژه', tone: 'lilac', scene: 'vase', role: 'The Clever Princess', collected: false, stories: 1,
      bio: 'A princess whose loyalty and cleverness free a trapped knight against all odds.' },
    { id: 'sindbad', name: 'Sindbad', nameFa: 'سندباد', tone: 'mint', scene: 'sea', role: 'The Sea Traveller', collected: true, stories: 1,
      bio: 'A curious voyager who sails toward every horizon and returns with tales of wonder.' },
    { id: 'zal', name: 'Zal', nameFa: 'زال', tone: 'sun', scene: 'hills', role: 'Child of the Simurgh', collected: false, stories: 1,
      bio: 'Born with snow-white hair and raised by the Simurgh, Zal grows into a wise and gentle prince.' },
    { id: 'rakhsh', name: 'Rakhsh', nameFa: 'رخش', tone: 'saffron', scene: 'dawn', role: "Rostam's Horse", collected: false, stories: 2,
      bio: 'A horse of fire and shadow, the only steed strong enough to carry Rostam into battle.' },
    { id: 'tahmineh', name: 'Tahmineh', nameFa: 'تهمینه', tone: 'rose', scene: 'night', role: 'The Princess', collected: false, stories: 1,
      bio: 'A princess of Samangan, brave and clear-eyed, whose story is woven into the legend of Rostam.' },
  ];

  const lullabies = [
    { id: 'l1', title: 'Moon Over Damavand', titleFa: 'ماه بر فرازِ دماوند', minutes: 18, origin: 'Traditional · Mazandaran', scene: 'lullaby', tone: 'lilac', plays: '2.1k' },
    { id: 'l2', title: 'Laay Laay, Little Star', titleFa: 'لای‌لای، ستاره‌ی کوچک', minutes: 12, origin: 'Folk lullaby', scene: 'night', tone: 'lapis', plays: '4.8k' },
    { id: 'l3', title: 'The Sleepy River', titleFa: 'رودِ خواب‌آلود', minutes: 22, origin: 'Original · Rivana', scene: 'lullaby', tone: 'mint', plays: '1.3k' },
    { id: 'l4', title: 'Garden of Dreams', titleFa: 'باغِ رؤیاها', minutes: 15, origin: 'Traditional · Shiraz', scene: 'night', tone: 'saffron', plays: '3.6k' },
  ];

  const badges = [
    { id: 'b1', label: 'First Voyage', desc: 'Finished your first story', icon: 'compass', tone: 'mint', earned: true },
    { id: 'b2', label: '7-Night Streak', desc: 'Read 7 nights in a row', icon: 'flame', tone: 'saffron', earned: true },
    { id: 'b3', label: 'Word Collector', desc: 'Learned 25 Persian words', icon: 'feather', tone: 'lapis', earned: true },
    { id: 'b4', label: 'Hero of Persia', desc: 'Met 5 Shahnameh heroes', icon: 'crown', tone: 'lilac', earned: false, progress: 0.6 },
    { id: 'b5', label: 'Night Owl', desc: 'Listen to 10 lullabies', icon: 'moon', tone: 'lilac', earned: false, progress: 0.4 },
    { id: 'b6', label: 'Storyteller', desc: 'Read aloud 3 times', icon: 'mic', tone: 'rose', earned: false, progress: 0.33 },
  ];

  /* vocabulary "garden" — words collected, with mastery 0..1 */
  const words = [
    { fa: 'پهلوان', tr: 'pahlavân', en: 'champion', story: 'Rostam', mastery: 1 },
    { fa: 'شیر', tr: 'shir', en: 'lion', story: 'Rostam', mastery: 1 },
    { fa: 'ستاره', tr: 'setâre', en: 'star', story: 'Rostam', mastery: 0.66 },
    { fa: 'آتش', tr: 'âtash', en: 'fire', story: 'Kaveh', mastery: 0.66 },
    { fa: 'پرنده', tr: 'parande', en: 'bird', story: 'Simurgh', mastery: 1 },
    { fa: 'دریا', tr: 'daryâ', en: 'sea', story: 'Sindbad', mastery: 0.33 },
    { fa: 'آب', tr: 'âb', en: 'water', story: 'Anahita', mastery: 1 },
    { fa: 'کوه', tr: 'kuh', en: 'mountain', story: 'Simurgh', mastery: 0.33 },
    { fa: 'آسمان', tr: 'âsemân', en: 'sky', story: 'Rostam', mastery: 0.66 },
  ];

  const profiles = [
    { id: 'roya', name: 'Roya', tone: 'saffron', age: 7, streak: 7, kid: true },
    { id: 'darius', name: 'Darius', tone: 'lapis', age: 9, streak: 3, kid: true },
    { id: 'mina', name: 'Mina', tone: 'lilac', age: 5, streak: 0, kid: true },
  ];

  const collections = [
    { id: 'c1', name: 'Shahnameh Heroes', nameFa: 'پهلوانانِ شاهنامه', count: 8, tone: 'saffron', scene: 'dawn' },
    { id: 'c2', name: 'Creatures of Myth', nameFa: 'موجوداتِ افسانه', count: 6, tone: 'lilac', scene: 'hills' },
    { id: 'c3', name: 'Voyages', nameFa: 'سفرها', count: 5, tone: 'mint', scene: 'sea' },
    { id: 'c4', name: 'Love & Courage', nameFa: 'عشق و دلیری', count: 4, tone: 'lapis', scene: 'vase' },
  ];

  const byId = (arr) => Object.fromEntries(arr.map((x) => [x.id, x]));

  return {
    stories, characters, lullabies, badges, words, profiles, collections,
    storyById: byId(stories), charById: byId(characters),
  };
})();
