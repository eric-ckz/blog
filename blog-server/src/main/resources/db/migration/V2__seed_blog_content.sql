-- 初始栏目。display_count 保留参考站的展示数字，实际导入文章数由管理端单独统计。
INSERT INTO category (id, category_key, label, description, icon, display_count, sort_order, create_time, update_time, is_delete) VALUES
(1001, 'travel', '旅行游记', '山川湖海，步履不停', 'map', 147, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1002, 'photo', '摄影光影', '光是时间的笔', 'camera', 37, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1003, 'city', '城市漫步', '街角与烟火气', 'building', 35, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1004, 'literature', '文学诗韵', '文字里的避难所', 'book', 198, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1005, 'life', '生活感悟', '日常里的微光', 'pen', 111, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 随项目导入的六张本地图片。文件会复制到 uploads/2026/07/seed 目录。
INSERT INTO media_asset (id, original_name, storage_path, public_url, mime_type, size_bytes, width, height, create_time, update_time, is_delete) VALUES
(2001, 'suzhou-library.jpg', '2026/07/seed/suzhou-library.jpg', '/uploads/2026/07/seed/suzhou-library.jpg', 'image/jpeg', 0, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2002, 'madrid-summer.jpg', '2026/07/seed/madrid-summer.jpg', '/uploads/2026/07/seed/madrid-summer.jpg', 'image/jpeg', 0, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2003, 'hong-kong-tram.jpg', '2026/07/seed/hong-kong-tram.jpg', '/uploads/2026/07/seed/hong-kong-tram.jpg', 'image/jpeg', 0, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2004, 'openclaw.jpg', '2026/07/seed/openclaw.jpg', '/uploads/2026/07/seed/openclaw.jpg', 'image/jpeg', 0, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2005, 'mong-kok.jpg', '2026/07/seed/mong-kok.jpg', '/uploads/2026/07/seed/mong-kok.jpg', 'image/jpeg', 0, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2006, 'journey-coast.jpg', '2026/07/seed/journey-coast.jpg', '/uploads/2026/07/seed/journey-coast.jpg', 'image/jpeg', 0, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 正文使用经过允许的基础 HTML。后续管理端保存时仍会通过 HtmlSanitizerService 清理。
INSERT INTO article (id, title, summary, category_id, cover_media_id, cover_url, content_html, status, published_at, view_count, read_minutes, original_url, create_time, update_time, is_delete) VALUES
(150001, 'AI 时代的里程碑', '迭代了 32 个版本，烧光了所有的套餐 token，依照液态玻璃的风格，重新思考工具与人的关系。', 1005, NULL, NULL, '<p>工具不断变化，但真正值得记录的始终是人与工具之间重新建立的关系。</p><blockquote>技术的价值，最终要回到人的体验。</blockquote>', 'PUBLISHED', '2026-07-25 09:00:00', 16, 5, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(0, '苏州第一丝厂单向空间', '一直喜欢许知远的《十三邀》，他试图在商业和文化之间寻求某种平衡。', 1001, 2001, '/uploads/2026/07/seed/suzhou-library.jpg', '<p>一直喜欢许知远的《十三邀》，也常常听他的播客。时代需要这样的公共知识分子。</p><img src="/uploads/2026/07/seed/suzhou-library.jpg" alt="苏州第一丝厂单向空间"><p>这样的空间，是喧闹城市里难得的精神避难所。</p>', 'PUBLISHED', '2026-07-04 09:00:00', 91, 1, 'https://mp.weixin.qq.com/', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(1, '初夏的风，吹来西班牙往事', '时间如脱缰的野马，一年匆匆已过，手机照片提醒我去年今日仍走在马德里的街头。', 1001, 2002, '/uploads/2026/07/seed/madrid-summer.jpg', '<p>时间让许多瞬间慢慢沉淀，回头再看，马德里的街道仍然带着初夏的风。</p>', 'PUBLISHED', '2026-06-16 09:00:00', 32, 4, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2, '我在上海想念香港', '香港虽不是故乡，于我而言，却是一种乡愁。那些电车、路牌与海风，总会忽然回到眼前。', 1001, 2003, '/uploads/2026/07/seed/hong-kong-tram.jpg', '<p>那些电车、路牌与海风，会在上海的某个雨天忽然回到眼前。</p>', 'PUBLISHED', '2026-06-07 09:00:00', 25, 3, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(3, '我的龙虾养成记', '我从不喜欢追逐热点，更在意长期而真实的改变。', 1005, 2004, '/uploads/2026/07/seed/openclaw.jpg', '<p>真正有价值的工具，不是制造更多焦虑，而是帮助人完成那些原本困难的事。</p>', 'PUBLISHED', '2026-05-28 09:00:00', 19, 5, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(4, '从旺角到维港的街角', '离港前最后的十个小时，从旺角走到维港，随手拍下香港的街景与烟火气。', 1001, 2005, '/uploads/2026/07/seed/mong-kok.jpg', '<p>离港前最后的十个小时，把时间交给步行，也交给城市的烟火气。</p>', 'PUBLISHED', '2026-05-22 09:00:00', 16, 3, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(5, '坚尼地城夏夜的晚风', '在香港一周，几乎雨水不断。今日终于放晴，会议结束后沿着海边慢慢走。', 1001, 2006, '/uploads/2026/07/seed/journey-coast.jpg', '<p>雨停后的海边有温柔的晚风，城市也终于慢了下来。</p>', 'PUBLISHED', '2026-05-21 09:00:00', 15, 3, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(6, '香港是一种乡愁', '香港不是故乡，于我而言，却是一种乡愁。', 1001, 2003, '/uploads/2026/07/seed/hong-kong-tram.jpg', '<p>原因大概藏在旧电影、霓虹灯和年少时的记忆里。</p>', 'PUBLISHED', '2026-05-18 09:00:00', 5, 3, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(7, 'Run Wild（向风而野）', '初夏已来，走在阳光下，仿佛生命重新舒展。', 1005, NULL, NULL, '<p>向风而野，在初夏的阳光里重新感受生命舒展。</p>', 'PUBLISHED', '2026-05-12 09:00:00', 5, 3, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(8, '小小的纪念', '我的文字终于突破一万人次阅读，这值得被认真纪念。', 1004, NULL, NULL, '<p>记录这个小小的时刻，也感谢每一次安静的阅读。</p>', 'PUBLISHED', '2026-04-21 09:00:00', 8, 2, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(9, '黑胶唱片的优雅', '黑胶唱片是更优雅的听音乐方式。', 1004, NULL, NULL, '<p>在一个午后或夜晚，把完整的时间交给一张唱片。</p>', 'PUBLISHED', '2026-04-16 09:00:00', 11, 3, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(10, '奔跑着拥抱春天', '春和景明，我们怀着急切的心情奔跑着拥抱春天。', 1005, NULL, NULL, '<p>迎接生命重新绽放的季节。</p>', 'PUBLISHED', '2026-04-02 09:00:00', 17, 2, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(11, '苹果成立 50 周年纪念', '悠悠的过去像一片漆黑天空，全赖思想家和艺术家的光。', 1004, NULL, NULL, '<p>设计与技术共同改变了我们理解世界的方式。</p>', 'PUBLISHED', '2026-04-01 09:00:00', 9, 4, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(12, '岁月如诗', '文字一如既往地温柔，让我们重新理解时间与生命。', 1004, NULL, NULL, '<p>诗让有限的词语拥有了更辽阔的回声。</p>', 'PUBLISHED', '2026-03-27 09:00:00', 7, 2, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(13, '生活中的美好设计', '那些看似不起眼却充满巧思的设计，常常更能改善日常。', 1005, NULL, NULL, '<p>好的设计安静地解决问题，并不急于展示自己。</p>', 'PUBLISHED', '2026-03-24 09:00:00', 13, 3, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(14, 'Up in the air', '连续两周跨越半个中国，在云端和陌生城市之间重新感受流动的生活。', 1005, 2006, '/uploads/2026/07/seed/journey-coast.jpg', '<p>机场、云层和窗外不断变化的城市，组成了一段流动的生活。</p>', 'PUBLISHED', '2026-03-18 09:00:00', 12, 3, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(15, '理想的生活', '理想的生活可能只是有时间读书、散步，以及认真吃一顿饭。', 1005, NULL, NULL, '<p>理想并不遥远，它藏在被认真对待的普通日子里。</p>', 'PUBLISHED', '2026-03-10 09:00:00', 8, 2, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(16, '焕新生活', '整理房间，也整理生活，给新的日子留一点空间。', 1005, NULL, NULL, '<p>放下不再需要的东西，空间和心情都会轻盈一些。</p>', 'PUBLISHED', '2026-03-02 09:00:00', 6, 2, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(17, '被雨淋湿的新加坡假期', '热带的雨来得毫无预兆，也让旅行有了计划之外的节奏。', 1001, 2006, '/uploads/2026/07/seed/journey-coast.jpg', '<p>计划之外的雨，让旅行获得另一种节奏。</p>', 'PUBLISHED', '2026-02-20 09:00:00', 18, 4, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(18, '普吉岛的海滩和落日', '海浪一次次漫过脚背，落日把整片海面染成温暖的金色。', 1001, 2006, '/uploads/2026/07/seed/journey-coast.jpg', '<p>海浪和落日，把时间变成一片温暖的金色。</p>', 'PUBLISHED', '2026-02-10 09:00:00', 21, 3, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(19, '再见，吉隆坡', '车窗外熟悉的街景一点点退远。', 1004, NULL, NULL, '<p>告别一座城市时，熟悉的街景会突然变得格外清晰。</p>', 'PUBLISHED', '2026-01-30 09:00:00', 14, 3, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(20, '2026 年个人计划', '新的一年仍然想保持阅读、写作和旅行。', 1005, NULL, NULL, '<p>也想给生活多留一些不被计划占满的空白。</p>', 'PUBLISHED', '2026-01-15 09:00:00', 26, 4, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(21, '偶遇：在漫游中感知艺术', '城市漫游的意义，是在没有计划的时候遇见一束光和一段故事。', 1004, NULL, NULL, '<p>偶遇让城市不再只是一张地图。</p>', 'PUBLISHED', '2026-01-09 09:00:00', 10, 3, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(22, '2025 年个人年度瞬间回顾', '把这一年的照片摊开，才发现许多平常日子已经拥有了自己的光。', 1001, 2002, '/uploads/2026/07/seed/madrid-summer.jpg', '<p>照片替我们保存了那些当时以为普通的瞬间。</p>', 'PUBLISHED', '2026-01-02 09:00:00', 31, 5, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(23, '2025 年总结：未完成的计划', '未完成并不总是失败，生活从来不是一张必须逐项勾选的清单。', 1005, NULL, NULL, '<p>允许计划没有完成，也是在学习接受真实的生活。</p>', 'PUBLISHED', '2025-12-28 09:00:00', 29, 5, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(68, '2024 个人年终总结', '这一年走过许多城市，也在书写中更清楚自己想留下什么。', 1004, NULL, NULL, '<p>回望一年，真正留下来的往往不是计划，而是感受。</p>', 'PUBLISHED', '2024-12-30 09:00:00', 42, 6, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(127, '2023 年个人年终总结', '照片和文字替我们保留了一年的纹理。', 1005, NULL, NULL, '<p>当日历走到最后一页，才看清这一年的纹理。</p>', 'PUBLISHED', '2023-12-31 09:00:00', 36, 5, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(228, '重新发现城市', '熟悉的城市也值得重新步行，街角总有过去没有注意到的细节。', 1003, 2003, '/uploads/2026/07/seed/hong-kong-tram.jpg', '<p>换一条路步行，熟悉的城市也会重新变得陌生而有趣。</p>', 'PUBLISHED', '2022-10-12 09:00:00', 33, 4, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(370, '一号公路的海景', '海风、悬崖和公路组成了关于自由的具体想象。', 1001, 2006, '/uploads/2026/07/seed/journey-coast.jpg', '<p>沿着海岸线向前，自由第一次有了具体的形状。</p>', 'PUBLISHED', '2021-07-04 09:00:00', 89, 5, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(372, '追忆充满故事的维也纳', '维也纳不止有金色大厅，还有写满历史的街道。', 1001, 2002, '/uploads/2026/07/seed/madrid-summer.jpg', '<p>博物馆、宫殿和街道，让历史在这座城市里仍然清晰可见。</p>', 'PUBLISHED', '2021-06-28 09:00:00', 45, 5, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(467, '忆江南，最忆是杭州', '江南的城市里，我尤其偏爱杭州。', 1001, 2001, '/uploads/2026/07/seed/suzhou-library.jpg', '<p>忆江南，最忆是杭州。浓妆淡抹，总有属于自己的从容。</p>', 'PUBLISHED', '2020-08-01 09:00:00', 23, 4, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(526, '创刊号', '人生有很多个第一次，这是新的起点，记录自己的感悟和思考。', 1004, NULL, NULL, '<p>人生有很多个第一次，这是新的起点。</p><blockquote>Stay Hungry, Stay Foolish.</blockquote><p>愿我们仍然保有好奇、热爱与独立思考。</p>', 'PUBLISHED', '2020-05-18 09:00:00', 22, 2, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO home_article_slot (id, slot_type, article_id, sort_order, create_time, update_time, is_delete) VALUES
(3001, 'HERO', 467, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(3002, 'HERO', 372, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(3003, 'HERO', 370, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(3011, 'FEATURED', 0, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(3012, 'FEATURED', 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(3013, 'FEATURED', 526, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(3014, 'FEATURED', 3, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO site_setting (id, setting_key, setting_value, description, create_time, update_time, is_delete) VALUES
(4001, 'stat_articles', '528', '首页展示文章数', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(4002, 'stat_words', '30万', '首页展示累计字数', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(4003, 'stat_years', '7', '首页展示写作年数', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(4004, 'stat_columns', '5', '首页展示栏目数', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(4005, 'manifesto_title', '在喧嚣的世界里，保留一间自己的书房', '首页深色宣言标题', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(4006, 'manifesto_text', '认真生活，诚实记录。愿文字与影像，让每一个普通日子都值得重读。', '首页深色宣言正文', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO about_item (id, item_type, title, description, icon, sort_order, create_time, update_time, is_delete) VALUES
(5001, 'INTRO', NULL, '2020 年 5 月 18 日，我写下公众号的第一篇《创刊号》。从那以后，7 年里写下了 528 篇文字、约 30 万字。', NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(5002, 'INTRO', NULL, '这里是我的数字书房。如果这些文字恰好也打动了你，那就再好不过了。', NULL, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(5011, 'LOVE', '文学', '读诗、读小说、读一切值得慢下来的文字。', 'book', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(5012, 'LOVE', '摄影', '相信光是时间的笔，街角和旅途的一瞬都值得被定格。', 'camera', 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(5013, 'LOVE', '旅行', '世界很大，要一篇一篇地写。', 'map-pin', 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(5014, 'LOVE', '咖啡', '许多文章，都诞生于一杯咖啡的时间。', 'coffee', 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(5021, 'TIMELINE', '2020', '写下《创刊号》，开始记录。', NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(5022, 'TIMELINE', '2021', '走过维也纳与巴黎，文字里有了远方。', NULL, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(5023, 'TIMELINE', '2023', '重新发现城市，也重新发现自己。', NULL, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(5024, 'TIMELINE', '2026', '仍在写，仍在路上。', NULL, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(5031, 'CONTACT', '微信公众号', '爱喝咖啡的文艺大叔', NULL, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
