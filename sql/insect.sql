/*
 * Spotify Music - 大规模真实测试数据 (For NCF Training)
 * Author: Gemini
 * Data: 35 Users, 20 Artists, 200 Songs, ~1000 Interactions
 *
 * ==========================================
 * 【更新说明】:
 * 1. 增加了对所有新增表的 TRUNCATE 操作 (A2, A4, B1, D3)。
 * 2. 扩展了 music_artist 的 INSERT 语句，包含统计字段 (C1)。
 * 3. 增加了 music_genre, music_song_genre 的测试数据 (A4)。
 * 4. 增加了 play_history (A2), user_artist_like, user_album_like (B1), sys_banner (D3) 的测试数据。
 * 5. 确保 music_song 包含 lyrics 字段的测试数据 (A1)。
 * ==========================================
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ==========================================
-- 1. 重置数据库 (TRUNCATE ALL)
-- ==========================================
TRUNCATE TABLE user_interaction;
TRUNCATE TABLE music_playlist_song;
TRUNCATE TABLE music_playlist;
TRUNCATE TABLE playlist_tag_rel;      /* 新增 (歌单标签) */
TRUNCATE TABLE playlist_tag;          /* 新增 (歌单标签) */
TRUNCATE TABLE song_comment;
TRUNCATE TABLE user_follow;
TRUNCATE TABLE play_history;          /* 新增 (A2) */
TRUNCATE TABLE play_queue_item;       /* 新增 播放队列 */
TRUNCATE TABLE play_queue;            /* 新增 播放队列 */
TRUNCATE TABLE music_genre;           /* 新增 (A4) */
TRUNCATE TABLE music_song_genre;      /* 新增 (A4) */
TRUNCATE TABLE user_artist_like;      /* 新增 (B1) */
TRUNCATE TABLE user_album_like;       /* 新增 (B1) */
TRUNCATE TABLE sys_banner;            /* 新增 (D3) */
TRUNCATE TABLE music_song;
TRUNCATE TABLE music_album;
TRUNCATE TABLE music_artist;
TRUNCATE TABLE sys_user_role;
DELETE FROM sys_user WHERE id > 0;
TRUNCATE TABLE sys_user;

-- ==========================================
-- 2. 初始化用户 (35个用户)
-- 密码统一为: 123456 ($2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK)
-- ==========================================

-- 2.1 管理员与音乐人
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `email`) VALUES 
(1, 'admin', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'Admin', 'admin@spotify.com'),
(2, 'musician_official', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', '官方音乐人', 'music@spotify.com');

INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1), (2, 3);
INSERT INTO `user_follow` (`user_id`, `followed_user_id`) VALUES (1, 2);

-- 2.2-2.4 插入普通用户组 (ID 10-40)
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `avatar_url`) VALUES
(10, 'mando_fan_01', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', '华语迷1号', 'https://api.dicebear.com/7.x/avataaars/svg?seed=10'),
(11, 'mando_fan_02', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', '周董铁粉', 'https://api.dicebear.com/7.x/avataaars/svg?seed=11'),
(12, 'mando_fan_03', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'Eason听众', 'https://api.dicebear.com/7.x/avataaars/svg?seed=12'),
(13, 'mando_fan_04', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'JJ Lin', 'https://api.dicebear.com/7.x/avataaars/svg?seed=13'),
(14, 'mando_fan_05', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', '邓紫棋粉', 'https://api.dicebear.com/7.x/avataaars/svg?seed=14'),
(15, 'mando_fan_06', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', '五月天', 'https://api.dicebear.com/7.x/avataaars/svg?seed=15'),
(16, 'mando_fan_07', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'KTV麦霸', 'https://api.dicebear.com/7.x/avataaars/svg?seed=16'),
(17, 'mando_fan_08', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', '深夜听歌', 'https://api.dicebear.com/7.x/avataaars/svg?seed=17'),
(18, 'mando_fan_09', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', '老歌回放', 'https://api.dicebear.com/7.x/avataaars/svg?seed=18'),
(19, 'mando_fan_10', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', '华语Top', 'https://api.dicebear.com/7.x/avataaars/svg?seed=19'),
(20, 'west_pop_01', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'Swiftie', 'https://api.dicebear.com/7.x/avataaars/svg?seed=20'),
(21, 'west_pop_02', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'JB Fan', 'https://api.dicebear.com/7.x/avataaars/svg?seed=21'),
(22, 'west_pop_03', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'Ed Sheeran', 'https://api.dicebear.com/7.x/avataaars/svg?seed=22'),
(23, 'west_pop_04', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'Ariana', 'https://api.dicebear.com/7.x/avataaars/svg?seed=23'),
(24, 'west_pop_05', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'Billboard', 'https://api.dicebear.com/7.x/avataaars/svg?seed=24'),
(25, 'west_pop_06', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'Grammy', 'https://api.dicebear.com/7.x/avataaars/svg?seed=25'),
(26, 'west_pop_07', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'Maroon5', 'https://api.dicebear.com/7.x/avataaars/svg?seed=26'),
(27, 'west_pop_08', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'Bruno Mars', 'https://api.dicebear.com/7.x/avataaars/svg?seed=27'),
(28, 'west_pop_09', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'The Weeknd', 'https://api.dicebear.com/7.x/avataaars/svg?seed=28'),
(29, 'west_pop_10', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'Coldplay', 'https://api.dicebear.com/7.x/avataaars/svg?seed=29'),
(30, 'rocker_01', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'Linkin Park', 'https://api.dicebear.com/7.x/avataaars/svg?seed=30'),
(31, 'rocker_02', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'Nirvana', 'https://api.dicebear.com/7.x/avataaars/svg?seed=31'),
(32, 'hiphop_01', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'Eminem', 'https://api.dicebear.com/7.x/avataaars/svg?seed=32'),
(33, 'hiphop_02', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'Drake', 'https://api.dicebear.com/7.x/avataaars/svg?seed=33'),
(34, 'kpop_01', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'BTS Army', 'https://api.dicebear.com/7.x/avataaars/svg?seed=34'),
(35, 'kpop_02', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'Blink', 'https://api.dicebear.com/7.x/avataaars/svg?seed=35'),
(36, 'anime_01', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'Otaku', 'https://api.dicebear.com/7.x/avataaars/svg?seed=36'),
(37, 'classic_01', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'Mozart', 'https://api.dicebear.com/7.x/avataaars/svg?seed=37'),
(38, 'jazz_01', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'Jazz Bar', 'https://api.dicebear.com/7.x/avataaars/svg?seed=38'),
(39, 'mix_01', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'Random Listener', 'https://api.dicebear.com/7.x/avataaars/svg?seed=39'),
(40, 'mix_02', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'Music Holic', 'https://api.dicebear.com/7.x/avataaars/svg?seed=40');

INSERT INTO `sys_user_role` (user_id, role_id)
SELECT id, 2 FROM sys_user WHERE id >= 10;


-- ==========================================
-- 3. 初始化艺人 (20个) - 【已更新】新增 total_fans 和 total_plays (C1)
-- ==========================================
INSERT INTO `music_artist` (`id`, `name`, `bio`, `avatar_url`, `user_id`, `total_fans`, `total_plays`) VALUES
(1, '周杰伦', '华语流行天王', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg', 2, 800000, 150000000),
(2, '陈奕迅', '香港著名男歌手', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', NULL, 650000, 120000000),
(3, '林俊杰', '行走的CD', 'https://p1.music.126.net/j1_s_yG9Ld1H_1_1_1_1_1==/109951165625561763.jpg', NULL, 500000, 90000000),
(4, '邓紫棋', '巨肺小天后', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', NULL, 300000, 60000000),
(5, '五月天', '亚洲第一摇滚天团', 'https://p1.music.126.net/K6c1X5n9j4x6j7x8x9x0xA==/109951165625561763.jpg', NULL, 450000, 80000000),
(6, 'Taylor Swift', '美国流行天后', 'https://p2.music.126.net/6y-UleORITEDbvrOLV0Q8A==/5639395138885805.jpg', NULL, 900000, 200000000),
(7, 'Ed Sheeran', '英国创作才子', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg', NULL, 400000, 75000000),
(8, 'Justin Bieber', '全球偶像', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', NULL, 350000, 70000000),
(9, 'Ariana Grande', '高音女神', 'https://p1.music.126.net/K6c1X5n9j4x6j7x8x9x0xA==/109951165625561763.jpg', NULL, 300000, 65000000),
(10, 'Bruno Mars', '火星哥', 'https://p2.music.126.net/6y-UleORITEDbvrOLV0Q8A==/5639395138885805.jpg', NULL, 250000, 55000000),
(11, 'Linkin Park', '新金属摇滚代表', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg', NULL, 200000, 50000000),
(12, 'Coldplay', '英伦摇滚乐队', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', NULL, 180000, 45000000),
(13, 'Imagine Dragons', '梦龙乐队', 'https://p1.music.126.net/K6c1X5n9j4x6j7x8x9x0xA==/109951165625561763.jpg', NULL, 150000, 40000000),
(14, 'Queen', '传奇摇滚乐队', 'https://p2.music.126.net/6y-UleORITEDbvrOLV0Q8A==/5639395138885805.jpg', NULL, 120000, 35000000),
(15, 'Nirvana', 'Grunge先驱', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg', NULL, 100000, 30000000),
(16, 'BTS', '防弹少年团', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', NULL, 500000, 100000000),
(17, 'BLACKPINK', '大势女团', 'https://p1.music.126.net/K6c1X5n9j4x6j7x8x9x0xA==/109951165625561763.jpg', NULL, 400000, 80000000),
(18, 'Eminem', '说唱之神', 'https://p2.music.126.net/6y-UleORITEDbvrOLV0Q8A==/5639395138885805.jpg', NULL, 350000, 70000000),
(19, 'The Weeknd', 'R&B天王', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg', NULL, 300000, 60000000),
(20, 'Adele', '灵魂歌姬', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', NULL, 280000, 55000000);


-- ==========================================
-- 4. 初始化歌曲 (200首) - 【已更新】增加 lyrics 字段 (A1)
-- ==========================================

-- 确保历史环境已经包含歌曲简介列，避免插入时出现 Unknown column 'description'
ALTER TABLE `music_song`
    ADD COLUMN IF NOT EXISTS `description` TEXT COMMENT '歌曲简介/创作故事' AFTER `lyrics`;

-- 4.1 插入专辑 Placeholder
INSERT INTO `music_album` (`id`, `artist_id`, `title`, `cover_url`, `release_date`)
SELECT id + 100, id, CONCAT(name, ' Best Hits'), avatar_url, '2020-01-01' FROM music_artist;

-- 4.2 插入歌曲 (部分手动，确保 lyrics 数据)
-- Artist 1: 周杰伦
INSERT INTO `music_song` (`album_id`, `artist_id`, `title`, `genre`, `file_url`, `cover_url`, `lyrics`, `description`) VALUES
(101, 1, '七里香', 'Pop', 'http://music.163.com/song/media/outer/url?id=108420.mp3', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', '[00:00.00] 七里香 - 周杰伦\n[00:05.00] 七里香歌词测试', '周杰伦抒情代表作，记录夏日青春的味道。'),
(101, 1, '青花瓷', 'Pop', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', '[00:00.00] 青花瓷 - 周杰伦\n[00:05.00] 青花瓷歌词测试', '新中式风格融合流行旋律的示例简介。'),
(101, 1, '稻香', 'Pop', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', '[00:00.00] 稻香 - 周杰伦\n[00:05.00] 稻香歌词测试', '怀旧童年与田园气息的灵感故事。'),
(101, 1, '夜曲', 'Pop', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', '[00:00.00] 夜曲 - 周杰伦\n[00:05.00] 夜曲歌词测试', '暗黑华丽的钢琴夜曲，展现戏剧张力。'),
(101, 1, '晴天', 'Pop', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', '[00:00.00] 晴天 - 周杰伦\n[00:05.00] 晴天歌词测试', '校园恋爱的轻快记忆，送给每一个晴朗午后。'),
(101, 1, '告白气球', 'Pop', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', '[00:00.00] 告白气球 - 周杰伦\n[00:05.00] 告白气球歌词测试', '甜蜜小品，讲述勇敢告白的瞬间。'),
(101, 1, '一路向北', 'Pop', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', '[00:00.00] 一路向北 - 周杰伦\n[00:05.00] 一路向北歌词测试', '电影感公路故事，记录离别的背影。'),
(101, 1, '不能说的秘密', 'Pop', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', '[00:00.00] 不能说的秘密 - 周杰伦\n[00:05.00] 不能说的秘密歌词测试', '同名电影主题曲，穿越时空的钢琴旋律。'),
(101, 1, '搁浅', 'Pop', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', '[00:00.00] 搁浅 - 周杰伦\n[00:05.00] 搁浅歌词测试', '失恋心情的独白，略带摇滚质感。'),
(101, 1, '简单爱', 'Pop', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', '[00:00.00] 简单爱 - 周杰伦\n[00:05.00] 简单爱歌词测试', '直白告白的青春恋曲示例说明。');

-- Artist 2: 陈奕迅
INSERT INTO `music_song` (`album_id`, `artist_id`, `title`, `genre`, `file_url`, `cover_url`, `lyrics`, `description`) VALUES
(102, 2, '十年', 'Ballad', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', '[00:00.00] 十年 - 陈奕迅\n[00:05.00] 十年歌词测试', '回望十年人生的感慨，用于情感回忆的简介。'),
(102, 2, '浮夸', 'Pop', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', '[00:00.00] 浮夸 - 陈奕迅\n[00:05.00] 浮夸歌词测试', '戏剧张力十足的演绎，展示情绪起伏。'),
(102, 2, 'K歌之王', 'Ballad', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', '[00:00.00] K歌之王 - 陈奕迅\n[00:05.00] K歌之王歌词测试', '关于陪伴与夜生活的叙事小品。'),
(102, 2, '好久不见', 'Ballad', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', '[00:00.00] 好久不见 - 陈奕迅\n[00:05.00] 好久不见歌词测试', '久别重逢的问候，抚平城市孤独。'),
(102, 2, '红玫瑰', 'Ballad', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', '[00:00.00] 红玫瑰 - 陈奕迅\n[00:05.00] 红玫瑰歌词测试', '红白玫瑰意象下的爱情抉择。'),
(102, 2, '富士山下', 'Ballad', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', '[00:00.00] 富士山下 - 陈奕迅\n[00:05.00] 富士山下歌词测试', '在旅行途中思考爱情走向的故事。'),
(102, 2, '单车', 'Ballad', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', '[00:00.00] 单车 - 陈奕迅\n[00:05.00] 单车歌词测试', '写给父亲与少年的陪伴记忆。'),
(102, 2, '不要说话', 'Ballad', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', '[00:00.00] 不要说话 - 陈奕迅\n[00:05.00] 不要说话歌词测试', '用沉默表达深爱的慢板歌曲简介。'),
(102, 2, '陪你度过漫长岁月', 'Ballad', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', '[00:00.00] 陪你度过漫长岁月 - 陈奕迅\n[00:05.00] 陪你度过漫长岁月歌词测试', '陪伴是最长情的告白，温暖治愈。'),
(102, 2, '爱情转移', 'Ballad', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', '[00:00.00] 爱情转移 - 陈奕迅\n[00:05.00] 爱情转移歌词测试', '城市爱情流动中的自我疗愈。');

-- Artist 6: Taylor Swift
INSERT INTO `music_song` (`album_id`, `artist_id`, `title`, `genre`, `file_url`, `cover_url`, `lyrics`, `description`) VALUES
(106, 6, 'Love Story', 'Country', '', 'https://p1.music.126.net/K6c1X5n9j4x6j7x8x9x0xA==/109951165625561763.jpg', '[00:00.00] Love Story - Taylor Swift', '乡村公主时代的经典桥段，改编罗密欧与朱丽叶。'),
(106, 6, 'Blank Space', 'Pop', '', 'https://p1.music.126.net/K6c1X5n9j4x6j7x8x9x0xA==/109951165625561763.jpg', '[00:00.00] Blank Space - Taylor Swift', '自黑风格的流行舞曲，调侃外界标签。'),
(106, 6, 'Shake It Off', 'Pop', '', 'https://p1.music.126.net/K6c1X5n9j4x6j7x8x9x0xA==/109951165625561763.jpg', '[00:00.00] Shake It Off - Taylor Swift', '摆脱质疑、尽情舞动的宣言。'),
(106, 6, 'Cruel Summer', 'Pop', '', 'https://p1.music.126.net/K6c1X5n9j4x6j7x8x9x0xA==/109951165625561763.jpg', '[00:00.00] Cruel Summer - Taylor Swift', '炙热夏日的爱恋与秘密。'),
(106, 6, 'Anti-Hero', 'Pop', '', 'https://p1.music.126.net/K6c1X5n9j4x6j7x8x9x0xA==/109951165625561763.jpg', '[00:00.00] Anti-Hero - Taylor Swift', '自我审视与和解的午夜独白。'),
(106, 6, 'Style', 'Pop', '', 'https://p1.music.126.net/K6c1X5n9j4x6j7x8x9x0xA==/109951165625561763.jpg', '[00:00.00] Style - Taylor Swift', '复古霓虹与永不过时的爱情故事。'),
(106, 6, 'You Belong With Me', 'Country', '', 'https://p1.music.126.net/K6c1X5n9j4x6j7x8x9x0xA==/109951165625561763.jpg', '[00:00.00] You Belong With Me - Taylor Swift', '邻家女孩视角的青春小甜歌。'),
(106, 6, 'Lover', 'Pop', '', 'https://p1.music.126.net/K6c1X5n9j4x6j7x8x9x0xA==/109951165625561763.jpg', '[00:00.00] Lover - Taylor Swift', '复古华尔兹节奏下的婚礼誓言。'),
(106, 6, 'Bad Blood', 'Pop', '', 'https://p1.music.126.net/K6c1X5n9j4x6j7x8x9x0xA==/109951165625561763.jpg', '[00:00.00] Bad Blood - Taylor Swift', '友谊破裂后的决绝宣言。'),
(106, 6, 'Look What You Made Me Do', 'Pop', '', 'https://p1.music.126.net/K6c1X5n9j4x6j7x8x9x0xA==/109951165625561763.jpg', '[00:00.00] Look What You Made Me Do - Taylor Swift', '黑暗合成器风的复仇故事。');

-- Artist 11: Linkin Park
INSERT INTO `music_song` (`album_id`, `artist_id`, `title`, `genre`, `file_url`, `cover_url`, `lyrics`, `description`) VALUES
(111, 11, 'In The End', 'Rock', '', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg', '[00:00.00] In The End - Linkin Park', '新金属代表作，探讨无力感。'),
(111, 11, 'Numb', 'Rock', '', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg', '[00:00.00] Numb - Linkin Park', '写给代沟与自我挣扎的呐喊。'),
(111, 11, 'Faint', 'Rock', '', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg', '[00:00.00] Faint - Linkin Park', '快节奏的愤怒释放。'),
(111, 11, 'Crawling', 'Rock', '', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg', '[00:00.00] Crawling - Linkin Park', '探讨焦虑与自我怀疑。'),
(111, 11, 'One Step Closer', 'Rock', '', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg', '[00:00.00] One Step Closer - Linkin Park', '怒吼式摇滚入门曲。'),
(111, 11, 'Breaking The Habit', 'Rock', '', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg', '[00:00.00] Breaking The Habit - Linkin Park', '讲述戒断坏习惯的决心。'),
(111, 11, 'Somewhere I Belong', 'Rock', '', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg', '[00:00.00] Somewhere I Belong - Linkin Park', '寻找归属的旅程。'),
(111, 11, 'What I\'ve Done', 'Rock', '', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg', '[00:00.00] What I\'ve Done - Linkin Park', '对过去错误的忏悔。'),
(111, 11, 'New Divide', 'Rock', '', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg', '[00:00.00] New Divide - Linkin Park', '电影主题曲风格的科幻史诗。'),
(111, 11, 'Burn It Down', 'Rock', '', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg', '[00:00.00] Burn It Down - Linkin Park', '电子摇滚融合的能量炸点。');

-- 填充更多歌曲 (简单生成：Artist 3-5, 7-10, 12-20 的歌曲)
INSERT INTO `music_song` (`album_id`, `artist_id`, `title`, `genre`, `file_url`, `cover_url`, `lyrics`, `description`)
SELECT
    a.id + 100,
    a.id,
    CONCAT(a.name, ' Song ', n.num),
    CASE
        WHEN a.id BETWEEN 3 AND 5 THEN 'Pop'
        WHEN a.id BETWEEN 7 AND 10 THEN 'Pop'
        WHEN a.id BETWEEN 12 AND 15 THEN 'Rock'
        WHEN a.id BETWEEN 16 AND 17 THEN 'K-Pop'
        WHEN a.id = 18 THEN 'Hip-Hop'
        ELSE 'Mix'
    END,
    '',
    a.avatar_url,
    CONCAT('[00:00.00] ', a.name, ' Song ', n.num, ' 歌词') /* 新增歌词内容 */,
    CONCAT('关于 ', a.name, ' Song ', n.num, ' 的创作故事')
FROM music_artist a
JOIN (
    SELECT 1 AS num UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
) n
WHERE a.id NOT IN (1, 2, 6, 11);


-- ==========================================
-- 5. 初始化流派数据 (music_genre) - 【新增】(A4)
-- ==========================================
INSERT INTO `music_genre` (`id`, `name`) VALUES
(1, 'Pop'), (2, 'Ballad'), (3, 'Rock'), (4, 'Hip-Hop'), 
(5, 'K-Pop'), (6, 'Country'), (7, 'Electronic'), (8, 'Jazz');

-- ==========================================
-- 6. 关联歌曲与流派 (music_song_genre) - 【新增】(A4)
-- ==========================================
-- 关联 Pop
INSERT INTO `music_song_genre` (`song_id`, `genre_id`)
SELECT id, 1 FROM music_song WHERE genre = 'Pop';

-- 关联 Ballad
INSERT INTO `music_song_genre` (`song_id`, `genre_id`)
SELECT id, 2 FROM music_song WHERE genre = 'Ballad';

-- 关联 Rock
INSERT INTO `music_song_genre` (`song_id`, `genre_id`)
SELECT id, 3 FROM music_song WHERE genre = 'Rock';

-- 关联 K-Pop
INSERT INTO `music_song_genre` (`song_id`, `genre_id`)
SELECT id, 5 FROM music_song WHERE genre = 'K-Pop';

-- 关联 Hip-Hop
INSERT INTO `music_song_genre` (`song_id`, `genre_id`)
SELECT id, 4 FROM music_song WHERE genre = 'Hip-Hop';


-- ==========================================
-- 7. 运营数据 (sys_banner) - 【新增】(D3)
-- ==========================================
INSERT INTO `sys_banner` (`id`, `title`, `image_url`, `target_url`, `sort_order`) VALUES
(1, 'NCF智能推荐', 'http://banner/recom.jpg', '/discover/recommend', 10),
(2, 'Taylor Swift新专', 'http://banner/ts.jpg', '/artist/6', 20),
(3, '每周新歌榜', 'http://banner/weekly.jpg', '/charts/weekly', 30);


-- ==========================================
-- 8. 批量生成交互数据 (Simulation)
-- ==========================================
-- Group A Likes Mandopop (Rating 4-5)
INSERT INTO `user_interaction` (`user_id`, `song_id`, `type`, `rating`)
SELECT 
    u.id, 
    s.id, 
    3, -- Like
    4.0 + (RAND() * 1.0) -- Rating 4.0 - 5.0
FROM sys_user u
CROSS JOIN music_song s
WHERE u.id BETWEEN 10 AND 19
AND s.artist_id BETWEEN 1 AND 5
AND RAND() < 0.3; -- 30% 的概率喜欢

-- Group B Likes Western Pop (Rating 4-5)
INSERT INTO `user_interaction` (`user_id`, `song_id`, `type`, `rating`)
SELECT 
    u.id, 
    s.id, 
    3, 
    4.0 + (RAND() * 1.0)
FROM sys_user u
CROSS JOIN music_song s
WHERE u.id BETWEEN 20 AND 29
AND s.artist_id BETWEEN 6 AND 10
AND RAND() < 0.3;

-- Group C Likes Rock/Mix (Rating 4-5)
INSERT INTO `user_interaction` (`user_id`, `song_id`, `type`, `rating`)
SELECT 
    u.id, 
    s.id, 
    3, 
    4.0 + (RAND() * 1.0)
FROM sys_user u
CROSS JOIN music_song s
WHERE u.id BETWEEN 30 AND 40
AND s.artist_id BETWEEN 11 AND 20
AND RAND() < 0.3;

-- 添加一些噪音数据 (Rating 1-2) - 用户偶尔听到不喜欢的歌
INSERT INTO `user_interaction` (`user_id`, `song_id`, `type`, `rating`)
SELECT 
    u.id, 
    s.id, 
    4, -- Skip
    1.0 + (RAND() * 1.0) -- Rating 1.0 - 2.0
FROM sys_user u
CROSS JOIN music_song s
WHERE u.id BETWEEN 10 AND 19 -- Mandopop fans dislike Rock
AND s.artist_id = 11 -- Linkin Park
AND RAND() < 0.1;


-- ==========================================
-- 9. 补充歌单与播放队列数据 (playlist & queue)
-- ==========================================
-- 歌单数据（含协作、可见性与统计字段）
INSERT INTO `music_playlist` (`id`, `creator_id`, `title`, `description`, `visibility`, `is_collaborative`, `status`, `category`, `mood`, `background_color`, `follower_count`, `like_count`, `play_count`, `share_count`, `last_played_at`, `last_modified_by`)
VALUES
(1, 1, '管理员协作精选', '管理员和音乐人共同维护的热门歌单', 'public', 1, 1, 'Pop', 'Happy', '#FFEEDD', 280, 90, 10200, 25, NOW() - INTERVAL 1 DAY, 2),
(2, 10, '华语慢歌', '周杰伦、陈奕迅精选', 'public', 0, 1, 'Ballad', 'Calm', '#DDF0FF', 120, 45, 3600, 12, NOW() - INTERVAL 2 DAY, 10),
(3, 20, '通勤摇滚', 'Linkin Park 励志摇滚合集', 'link', 0, 1, 'Rock', 'Energetic', '#CCDDEE', 60, 18, 1800, 6, NOW() - INTERVAL 3 DAY, 20);

-- 歌单标签
INSERT INTO `playlist_tag` (`id`, `name`) VALUES
(1, '流行'),
(2, '协作'),
(3, '华语'),
(4, '摇滚'),
(5, '通勤');

INSERT INTO `playlist_tag_rel` (`playlist_id`, `tag_id`) VALUES
(1, 1), (1, 2),
(2, 1), (2, 3),
(3, 4), (3, 5);

-- 歌单-歌曲关联，使用 position 支持插队
INSERT INTO `music_playlist_song` (`playlist_id`, `song_id`, `position`, `added_by`, `status`, `version`)
SELECT 1, id, 10, 1, 1, 1 FROM music_song WHERE title = '七里香' LIMIT 1;
INSERT INTO `music_playlist_song` (`playlist_id`, `song_id`, `position`, `added_by`, `status`, `version`)
SELECT 1, id, 20, 2, 1, 1 FROM music_song WHERE title = 'Love Story' LIMIT 1;
INSERT INTO `music_playlist_song` (`playlist_id`, `song_id`, `position`, `added_by`, `status`, `version`)
SELECT 1, id, 30, 2, 1, 1 FROM music_song WHERE title = 'Numb' LIMIT 1;
INSERT INTO `music_playlist_song` (`playlist_id`, `song_id`, `position`, `added_by`, `status`, `version`)
SELECT 2, id, 10, 10, 1, 1 FROM music_song WHERE title = '稻香' LIMIT 1;
INSERT INTO `music_playlist_song` (`playlist_id`, `song_id`, `position`, `added_by`, `status`, `version`)
SELECT 2, id, 20, 10, 1, 1 FROM music_song WHERE title = '好久不见' LIMIT 1;
INSERT INTO `music_playlist_song` (`playlist_id`, `song_id`, `position`, `added_by`, `status`, `version`)
SELECT 3, id, 10, 20, 1, 1 FROM music_song WHERE title = 'In The End' LIMIT 1;
INSERT INTO `music_playlist_song` (`playlist_id`, `song_id`, `position`, `added_by`, `status`, `version`)
SELECT 3, id, 20, 20, 1, 1 FROM music_song WHERE title = 'What I\'ve Done' LIMIT 1;

-- 播放队列示例
INSERT INTO `play_queue` (`id`, `user_id`, `device_id`, `source_type`, `source_id`, `is_shuffle`, `repeat_mode`, `current_index`, `current_position_ms`, `version`)
VALUES
(1, 10, 'ios-001', 'playlist', 2, 0, 'all', 0, 0, 1),
(2, 20, 'web-001', 'playlist', 3, 1, 'off', 1, 45200, 1);

INSERT INTO `play_queue_item` (`queue_id`, `song_id`, `position`, `is_played`, `inserted_by`)
SELECT 1, id, 10, 1, 10 FROM music_song WHERE title = '稻香' LIMIT 1;
INSERT INTO `play_queue_item` (`queue_id`, `song_id`, `position`, `is_played`, `inserted_by`)
SELECT 1, id, 20, 0, 10 FROM music_song WHERE title = '好久不见' LIMIT 1;
INSERT INTO `play_queue_item` (`queue_id`, `song_id`, `position`, `is_played`, `inserted_by`)
SELECT 2, id, 10, 1, 20 FROM music_song WHERE title = 'In The End' LIMIT 1;
INSERT INTO `play_queue_item` (`queue_id`, `song_id`, `position`, `is_played`, `inserted_by`)
SELECT 2, id, 20, 0, 20 FROM music_song WHERE title = 'What I\'ve Done' LIMIT 1;


-- ==========================================
-- 10. 补充收藏关系数据 (user_artist_like, user_album_like) - 【新增】(B1)
-- ==========================================
-- User 10 (华语迷) 收藏 周杰伦 (Artist 1) 和他的专辑 (Album 101)
INSERT INTO `user_artist_like` (`user_id`, `artist_id`) VALUES
(10, 1), (11, 1), (12, 2);
INSERT INTO `user_album_like` (`user_id`, `album_id`) VALUES 
(10, 101), (11, 101), (12, 102);

-- User 20 (Swiftie) 收藏 Taylor Swift (Artist 6) 和她的专辑 (Album 106)
INSERT INTO `user_artist_like` (`user_id`, `artist_id`) VALUES 
(20, 6), (21, 6);
INSERT INTO `user_album_like` (`user_id`, `album_id`) VALUES 
(20, 106), (21, 106);

-- User 30 (Rocker) 收藏 Linkin Park (Artist 11)
INSERT INTO `user_artist_like` (`user_id`, `artist_id`) VALUES (30, 11);
INSERT INTO `user_album_like` (`user_id`, `album_id`) VALUES (30, 111);


-- ==========================================
-- 11. 补充播放历史数据 (play_history) - 【新增】(A2)
-- ==========================================
-- User 10 最近播放记录 (Song 101: 七里香, Song 102: 青花瓷)
INSERT INTO `play_history` (`user_id`, `song_id`, `play_time`) VALUES
(10, 101, NOW() - INTERVAL 10 MINUTE),
(10, 102, NOW() - INTERVAL 20 MINUTE),
(10, 101, NOW() - INTERVAL 30 MINUTE),
-- User 20 最近播放记录
(20, 106, NOW() - INTERVAL 5 MINUTE),
(20, 107, NOW() - INTERVAL 15 MINUTE);


SET FOREIGN_KEY_CHECKS = 1;

-- 脚本结束