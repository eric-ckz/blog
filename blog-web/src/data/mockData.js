const image = (name) => `/images/${name}`

export const siteStats = {
  articles: 528,
  words: '30万',
  years: 7,
  columns: 5,
}

export const categories = [
  { key: 'travel', label: '旅行游记', count: 147, description: '山川湖海，步履不停', icon: 'map' },
  { key: 'photo', label: '摄影光影', count: 37, description: '光是时间的笔', icon: 'camera' },
  { key: 'city', label: '城市漫步', count: 35, description: '街角与烟火气', icon: 'building' },
  { key: 'literature', label: '文学诗韵', count: 198, description: '文字里的避难所', icon: 'book' },
  { key: 'life', label: '生活感悟', count: 111, description: '日常里的微光', icon: 'pen' },
]

const categoryLabel = Object.fromEntries(categories.map((item) => [item.key, item.label]))

const makeArticle = (id, title, summary, category, publishedAt, options = {}) => ({
  id: String(id),
  title,
  summary,
  category,
  categoryLabel: categoryLabel[category],
  publishedAt,
  coverUrl: options.coverUrl || '',
  views: options.views ?? 12,
  readMinutes: options.readMinutes ?? 3,
  contentHtml: options.contentHtml || '',
  originalUrl: options.originalUrl || '',
})

const defaultContent = (article) => `
  <p>${article.summary}</p>
  <p>时间让许多瞬间慢慢沉淀。回头再看，那些曾经匆忙掠过的街道、光线和心情，都会在文字里获得新的意义。</p>
  <blockquote>认真记录并不是为了留住时间，而是为了在未来的某一天，仍能认出当时的自己。</blockquote>
  <p>于是把这一刻写下来，也把旅途中的风、日常里的微光和没有说完的话，一并收进这间数字书房。</p>
`

export const articles = [
  makeArticle('150001', 'AI 时代的里程碑', '迭代了 32 个版本，烧光了所有的套餐 token，依照液态玻璃的风格，重新思考工具与人的关系。', 'life', '2026-07-25', { views: 16, readMinutes: 5 }),
  makeArticle('0', '苏州第一丝厂单向空间', '一直喜欢许知远的《十三邀》，许知远作为当代知识分子，他试图在商业和文化之间寻求某种平衡。', 'travel', '2026-07-04', {
    coverUrl: image('suzhou-library.jpg'), views: 91, readMinutes: 1,
    originalUrl: 'https://mp.weixin.qq.com/',
    contentHtml: `
      <p>一直喜欢许知远的《十三邀》，许知远作为当代知识分子，他试图在商业和文化之间寻求某种平衡。我也常常听他的播客，我觉得时代需要这样的公共知识分子。</p>
      <p>单向空间和别的书店不太一样，他们选书更偏向人文和艺术，这也是这个空间和其他商业书店不同的地方。</p>
      <img src="${image('suzhou-library.jpg')}" alt="苏州第一丝厂单向空间" />
      <p>我也试图克服这个时代浮躁的表征。这样的空间，是喧闹城市里难得的精神避难所。</p>
    `,
  }),
  makeArticle('1', '初夏的风，吹来西班牙往事', '时间如脱缰的野马，一年匆匆已过，手机照片提醒我去年今日仍走在马德里的街头。', 'travel', '2026-06-16', { coverUrl: image('madrid-summer.jpg'), views: 32, readMinutes: 4 }),
  makeArticle('2', '我在上海想念香港', '香港虽不是故乡，于我而言，却是一种乡愁。那些电车、路牌与海风，总会忽然回到眼前。', 'travel', '2026-06-07', { coverUrl: image('hong-kong-tram.jpg'), views: 25 }),
  makeArticle('3', '我的龙虾养成记', '我从不喜欢追逐热点。无论社会新闻、明星八卦，还是科技浪潮，我更在意长期而真实的改变。', 'life', '2026-05-28', { coverUrl: image('openclaw.jpg'), views: 19, readMinutes: 5 }),
  makeArticle('4', '从旺角到维港的街角', '离港前最后的十个小时，从旺角走到维港，随手拍下香港的街景与烟火气。', 'travel', '2026-05-22', { coverUrl: image('mong-kok.jpg'), views: 16 }),
  makeArticle('5', '坚尼地城夏夜的晚风', '在香港一周，几乎雨水不断。今日终于放晴，会议结束后沿着海边慢慢走。', 'travel', '2026-05-21', { coverUrl: image('journey-coast.jpg'), views: 15 }),
  makeArticle('6', '香港是一种乡愁', '香港不是故乡，于我而言，却是一种乡愁。原因大概藏在那些旧电影和年少记忆里。', 'travel', '2026-05-18', { coverUrl: image('hong-kong-tram.jpg'), views: 5 }),
  makeArticle('7', 'Run Wild（向风而野）', '初夏已来，树叶还没变成深绿。走在阳光下，仿佛生命重新舒展。', 'life', '2026-05-12', { views: 5 }),
  makeArticle('8', '小小的纪念', '我的文字终于突破一万人次阅读。作为一个完全私人的公众号，这值得被认真纪念。', 'literature', '2026-04-21', { views: 8 }),
  makeArticle('9', '黑胶唱片的优雅', '黑胶唱片是更优雅的听音乐方式，在一个午后或夜晚，把时间交给一张唱片。', 'literature', '2026-04-16', { views: 11 }),
  makeArticle('10', '奔跑着拥抱春天', '春和景明，我们怀着急切的心情奔跑着拥抱春天，迎接生命绽放的季节。', 'life', '2026-04-02', { views: 17 }),
  makeArticle('11', '苹果成立 50 周年纪念', '悠悠的过去像一片漆黑天空，我们仍能认出它，全赖思想家和艺术家的光。', 'literature', '2026-04-01', { views: 9 }),
  makeArticle('12', '岁月如诗', '我所热爱的三行诗，文字一如既往地温柔，让我们重新理解时间与生命。', 'literature', '2026-03-27', { views: 7 }),
  makeArticle('13', '生活中的美好设计', '那些看似不起眼却充满巧思的设计，常常比宏大的表达更能改善日常。', 'life', '2026-03-24', { views: 13 }),
  makeArticle('14', 'Up in the air', '连续两周跨越半个中国，在云端、机场和陌生城市之间，重新感受流动的生活。', 'life', '2026-03-18', { coverUrl: image('journey-coast.jpg'), views: 12 }),
  makeArticle('15', '理想的生活', '理想的生活并不遥远，它可能只是有时间读书、散步，以及认真吃一顿饭。', 'life', '2026-03-10', { views: 8 }),
  makeArticle('16', '焕新生活', '整理房间，也整理生活。把不再需要的东西放下，给新的日子留一点空间。', 'life', '2026-03-02', { views: 6 }),
  makeArticle('17', '被雨淋湿的新加坡假期', '热带的雨来得毫无预兆，也让旅行有了计划之外的节奏。', 'travel', '2026-02-20', { coverUrl: image('journey-coast.jpg'), views: 18 }),
  makeArticle('18', '普吉岛的海滩和落日', '海浪一次次漫过脚背，落日把整片海面染成温暖的金色。', 'travel', '2026-02-10', { coverUrl: image('journey-coast.jpg'), views: 21 }),
  makeArticle('19', '再见，吉隆坡', '从旅馆出发去机场，车窗外熟悉的街景一点点退远。', 'literature', '2026-01-30', { views: 14 }),
  makeArticle('20', '2026 年个人计划', '新的一年仍然想保持阅读、写作和旅行，也想给生活多留一些空白。', 'life', '2026-01-15', { views: 26 }),
  makeArticle('21', '偶遇：在漫游中感知艺术', '城市漫游的意义，是在没有计划的时候遇见一束光、一座展览和一段故事。', 'literature', '2026-01-09', { views: 10 }),
  makeArticle('22', '2025 年个人年度瞬间回顾', '把这一年的照片摊开，才发现许多平常日子已经拥有了自己的光。', 'travel', '2026-01-02', { coverUrl: image('madrid-summer.jpg'), views: 31 }),
  makeArticle('23', '2025 年总结：未完成的计划', '未完成并不总是失败，它也提醒我们，生活从来不是一张必须逐项勾选的清单。', 'life', '2025-12-28', { views: 29 }),
  makeArticle('68', '2024 个人年终总结', '这一年走过许多城市，也在反复书写中更清楚自己想留下什么。', 'literature', '2024-12-30', { views: 42 }),
  makeArticle('127', '2023 年个人年终总结', '当日历走到最后一页，照片和文字替我们保留了一年的纹理。', 'life', '2023-12-31', { views: 36 }),
  makeArticle('228', '重新发现城市', '熟悉的城市也值得重新步行，街角总有过去没有注意到的细节。', 'city', '2022-10-12', { coverUrl: image('hong-kong-tram.jpg'), views: 33 }),
  makeArticle('370', '一号公路的海景', '沿着海岸线一路向前，海风、悬崖和公路组成了关于自由的具体想象。', 'travel', '2021-07-04', { coverUrl: image('journey-coast.jpg'), views: 89 }),
  makeArticle('372', '追忆充满故事的维也纳', '维也纳不止有金色大厅，还有美泉宫、博物馆和写满历史的街道。', 'travel', '2021-06-28', { coverUrl: image('madrid-summer.jpg'), views: 45 }),
  makeArticle('467', '忆江南，最忆是杭州', '如果把杭州比喻为美人，浓妆淡抹总相宜。江南的城市里，我尤其偏爱杭州。', 'travel', '2020-08-01', { coverUrl: image('suzhou-library.jpg'), views: 23 }),
  makeArticle('526', '创刊号', '人生有很多个第一次，这是新的起点，记录自己的感悟和思考。', 'literature', '2020-05-18', {
    views: 22, readMinutes: 2,
    contentHtml: `
      <p>人生有很多个第一次，这是新的起点，记录自己的感悟和思考。</p>
      <blockquote>Stay Hungry, Stay Foolish.</blockquote>
      <p>求知若渴，虚怀若愚。愿我们在快速变化的世界里，仍然保有好奇、热爱与独立思考。</p>
    `,
  }),
].map((article) => ({ ...article, contentHtml: article.contentHtml || defaultContent(article) }))

const findArticles = (ids) => ids.map((id) => articles.find((article) => article.id === String(id))).filter(Boolean)

export const homeData = {
  stats: siteStats,
  heroSlides: findArticles(['467', '372', '370']),
  featured: findArticles(['0', '1', '526', '3']),
  recent: findArticles(['150001', '0', '1', '2', '3', '4']),
  categories,
}

export const aboutData = {
  intro: [
    '2020 年 5 月 18 日，我写下了公众号的第一篇《创刊号》，引用 Steve Jobs 的那句「Stay Hungry, Stay Foolish」作为开端。从那以后，7 年里一共写下了 528 篇文字、约 30 万字——有对生活的感悟，有快门里的光影，也有旅途中的风景与心情。',
    '这里是我的数字书房。如果这些文字恰好也打动了你，那就再好不过了。',
  ],
  loves: [
    { title: '文学', icon: 'book', description: '读诗、读小说、读一切值得慢下来的文字。辛波斯卡、博尔赫斯、木心、余光中，都是这里的常客。' },
    { title: '摄影', icon: 'camera', description: '相信光是时间的笔。街角的光影、旅途的黄昏、咖啡馆窗前的一瞬，都值得被定格。' },
    { title: '旅行', icon: 'map-pin', description: '从江南古镇到维也纳与巴黎，从加州一号公路到巴厘岛。世界很大，要一篇一篇地写。' },
    { title: '咖啡', icon: 'coffee', description: '爱喝咖啡这件事，写在了名字里。许多文章，都诞生于一杯咖啡的时间。' },
  ],
  timeline: [
    { year: '2020', description: '写下《创刊号》，开始记录。' },
    { year: '2021', description: '走过维也纳与巴黎，文字里有了远方。' },
    { year: '2023', description: '重新发现上海，也重新发现自己。' },
    { year: '2026', description: '仍在写，仍在路上。' },
  ],
  contact: { label: '微信公众号', value: '爱喝咖啡的文艺大叔' },
}
