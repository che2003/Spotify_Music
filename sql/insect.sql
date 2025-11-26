/*
 * Spotify Music - 大规模真实测试数据 (For NCF Training)
 * Author: Gemini
 * Data: 35 Users, 20 Artists, 200 Songs, ~1000 Interactions
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ==========================================
-- 1. 重置数据库 (TRUNCATE ALL)
-- ==========================================
TRUNCATE TABLE user_interaction;
TRUNCATE TABLE music_playlist_song;
TRUNCATE TABLE music_playlist;
TRUNCATE TABLE song_comment;
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

-- 2.2 普通用户组 A: 华语流行爱好者 (ID 10-19)
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
(19, 'mando_fan_10', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', '华语Top', 'https://api.dicebear.com/7.x/avataaars/svg?seed=19');

-- 2.3 普通用户组 B: 欧美流行爱好者 (ID 20-29)
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `avatar_url`) VALUES
(20, 'west_pop_01', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'Swiftie', 'https://api.dicebear.com/7.x/avataaars/svg?seed=20'),
(21, 'west_pop_02', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'JB Fan', 'https://api.dicebear.com/7.x/avataaars/svg?seed=21'),
(22, 'west_pop_03', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'Ed Sheeran', 'https://api.dicebear.com/7.x/avataaars/svg?seed=22'),
(23, 'west_pop_04', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'Ariana', 'https://api.dicebear.com/7.x/avataaars/svg?seed=23'),
(24, 'west_pop_05', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'Billboard', 'https://api.dicebear.com/7.x/avataaars/svg?seed=24'),
(25, 'west_pop_06', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'Grammy', 'https://api.dicebear.com/7.x/avataaars/svg?seed=25'),
(26, 'west_pop_07', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'Maroon5', 'https://api.dicebear.com/7.x/avataaars/svg?seed=26'),
(27, 'west_pop_08', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'Bruno Mars', 'https://api.dicebear.com/7.x/avataaars/svg?seed=27'),
(28, 'west_pop_09', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'The Weeknd', 'https://api.dicebear.com/7.x/avataaars/svg?seed=28'),
(29, 'west_pop_10', '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK', 'Coldplay', 'https://api.dicebear.com/7.x/avataaars/svg?seed=29');

-- 2.4 普通用户组 C: 摇滚/嘻哈/Kpop 混合 (ID 30-40)
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `avatar_url`) VALUES
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

-- 给所有生成的测试用户赋予 ROLE_USER (2)
INSERT INTO `sys_user_role` (user_id, role_id)
SELECT id, 2 FROM sys_user WHERE id >= 10;


-- ==========================================
-- 3. 初始化艺人 (20个)
-- ==========================================
INSERT INTO `music_artist` (`id`, `name`, `bio`, `avatar_url`, `user_id`) VALUES
-- 华语 (1-5)
(1, '周杰伦', '华语流行天王', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg', 2),
(2, '陈奕迅', '香港著名男歌手', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', NULL),
(3, '林俊杰', '行走的CD', 'https://p1.music.126.net/j1_s_yG9Ld1H_1_1_1_1_1==/109951165625561763.jpg', NULL),
(4, '邓紫棋', '巨肺小天后', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', NULL),
(5, '五月天', '亚洲第一摇滚天团', 'https://p1.music.126.net/K6c1X5n9j4x6j7x8x9x0xA==/109951165625561763.jpg', NULL),
-- 欧美流行 (6-10)
(6, 'Taylor Swift', '美国流行天后', 'https://p2.music.126.net/6y-UleORITEDbvrOLV0Q8A==/5639395138885805.jpg', NULL),
(7, 'Ed Sheeran', '英国创作才子', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg', NULL),
(8, 'Justin Bieber', '全球偶像', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', NULL),
(9, 'Ariana Grande', '高音女神', 'https://p1.music.126.net/K6c1X5n9j4x6j7x8x9x0xA==/109951165625561763.jpg', NULL),
(10, 'Bruno Mars', '火星哥', 'https://p2.music.126.net/6y-UleORITEDbvrOLV0Q8A==/5639395138885805.jpg', NULL),
-- 摇滚/另类 (11-15)
(11, 'Linkin Park', '新金属摇滚代表', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg', NULL),
(12, 'Coldplay', '英伦摇滚乐队', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', NULL),
(13, 'Imagine Dragons', '梦龙乐队', 'https://p1.music.126.net/K6c1X5n9j4x6j7x8x9x0xA==/109951165625561763.jpg', NULL),
(14, 'Queen', '传奇摇滚乐队', 'https://p2.music.126.net/6y-UleORITEDbvrOLV0Q8A==/5639395138885805.jpg', NULL),
(15, 'Nirvana', 'Grunge先驱', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg', NULL),
-- 其他 (16-20)
(16, 'BTS', '防弹少年团', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', NULL),
(17, 'BLACKPINK', '大势女团', 'https://p1.music.126.net/K6c1X5n9j4x6j7x8x9x0xA==/109951165625561763.jpg', NULL),
(18, 'Eminem', '说唱之神', 'https://p2.music.126.net/6y-UleORITEDbvrOLV0Q8A==/5639395138885805.jpg', NULL),
(19, 'The Weeknd', 'R&B天王', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg', NULL),
(20, 'Adele', '灵魂歌姬', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg', NULL);


-- ==========================================
-- 4. 初始化歌曲 (200首) - 每个艺人10首
-- 为了简洁，专辑 ID 统一为 100+ArtistID
-- ==========================================

-- 4.1 插入专辑 Placeholder
INSERT INTO `music_album` (`id`, `artist_id`, `title`, `cover_url`, `release_date`) 
SELECT id + 100, id, CONCAT(name, ' Best Hits'), avatar_url, '2020-01-01' FROM music_artist;

-- 4.2 插入歌曲 (使用存储过程或直接批量插入，这里用批量插入简化)
-- Artist 1: 周杰伦
INSERT INTO `music_song` (`album_id`, `artist_id`, `title`, `genre`, `file_url`, `cover_url`) VALUES
(101, 1, '七里香', 'Pop', 'http://music.163.com/song/media/outer/url?id=108420.mp3', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg'),
(101, 1, '青花瓷', 'Pop', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg'),
(101, 1, '稻香', 'Pop', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg'),
(101, 1, '夜曲', 'Pop', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg'),
(101, 1, '晴天', 'Pop', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg'),
(101, 1, '告白气球', 'Pop', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg'),
(101, 1, '一路向北', 'Pop', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg'),
(101, 1, '不能说的秘密', 'Pop', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg'),
(101, 1, '搁浅', 'Pop', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg'),
(101, 1, '简单爱', 'Pop', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg');

-- Artist 2: 陈奕迅
INSERT INTO `music_song` (`album_id`, `artist_id`, `title`, `genre`, `file_url`, `cover_url`) VALUES
(102, 2, '十年', 'Ballad', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg'),
(102, 2, '浮夸', 'Pop', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg'),
(102, 2, 'K歌之王', 'Ballad', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg'),
(102, 2, '好久不见', 'Ballad', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg'),
(102, 2, '红玫瑰', 'Ballad', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg'),
(102, 2, '富士山下', 'Ballad', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg'),
(102, 2, '单车', 'Ballad', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg'),
(102, 2, '不要说话', 'Ballad', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg'),
(102, 2, '陪你度过漫长岁月', 'Ballad', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg'),
(102, 2, '爱情转移', 'Ballad', '', 'https://p2.music.126.net/1gNCwmADglq6d8Zz0g4jEQ==/109951165550297684.jpg');

-- Artist 6: Taylor Swift
INSERT INTO `music_song` (`album_id`, `artist_id`, `title`, `genre`, `file_url`, `cover_url`) VALUES
(106, 6, 'Love Story', 'Country', '', 'https://p1.music.126.net/K6c1X5n9j4x6j7x8x9x0xA==/109951165625561763.jpg'),
(106, 6, 'Blank Space', 'Pop', '', 'https://p1.music.126.net/K6c1X5n9j4x6j7x8x9x0xA==/109951165625561763.jpg'),
(106, 6, 'Shake It Off', 'Pop', '', 'https://p1.music.126.net/K6c1X5n9j4x6j7x8x9x0xA==/109951165625561763.jpg'),
(106, 6, 'Cruel Summer', 'Pop', '', 'https://p1.music.126.net/K6c1X5n9j4x6j7x8x9x0xA==/109951165625561763.jpg'),
(106, 6, 'Anti-Hero', 'Pop', '', 'https://p1.music.126.net/K6c1X5n9j4x6j7x8x9x0xA==/109951165625561763.jpg'),
(106, 6, 'Style', 'Pop', '', 'https://p1.music.126.net/K6c1X5n9j4x6j7x8x9x0xA==/109951165625561763.jpg'),
(106, 6, 'You Belong With Me', 'Country', '', 'https://p1.music.126.net/K6c1X5n9j4x6j7x8x9x0xA==/109951165625561763.jpg'),
(106, 6, 'Lover', 'Pop', '', 'https://p1.music.126.net/K6c1X5n9j4x6j7x8x9x0xA==/109951165625561763.jpg'),
(106, 6, 'Bad Blood', 'Pop', '', 'https://p1.music.126.net/K6c1X5n9j4x6j7x8x9x0xA==/109951165625561763.jpg'),
(106, 6, 'Look What You Made Me Do', 'Pop', '', 'https://p1.music.126.net/K6c1X5n9j4x6j7x8x9x0xA==/109951165625561763.jpg');

-- Artist 11: Linkin Park
INSERT INTO `music_song` (`album_id`, `artist_id`, `title`, `genre`, `file_url`, `cover_url`) VALUES
(111, 11, 'In The End', 'Rock', '', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg'),
(111, 11, 'Numb', 'Rock', '', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg'),
(111, 11, 'Faint', 'Rock', '', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg'),
(111, 11, 'Crawling', 'Rock', '', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg'),
(111, 11, 'One Step Closer', 'Rock', '', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg'),
(111, 11, 'Breaking The Habit', 'Rock', '', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg'),
(111, 11, 'Somewhere I Belong', 'Rock', '', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg'),
(111, 11, 'What I\'ve Done', 'Rock', '', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg'),
(111, 11, 'New Divide', 'Rock', '', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg'),
(111, 11, 'Burn It Down', 'Rock', '', 'https://p1.music.126.net/r7u0Hn27Vj1FpL0e6x9m0A==/109951165625561763.jpg');

-- 填充更多歌曲 (简单生成：Artist 3-5, 7-10, 12-20 的歌曲)
INSERT INTO `music_song` (`album_id`, `artist_id`, `title`, `genre`, `file_url`, `cover_url`)
SELECT 
    a.id + 100, 
    a.id, 
    CONCAT(a.name, ' Song ', n.num), 
    'Pop', 
    '', 
    a.avatar_url
FROM music_artist a
JOIN (
    SELECT 1 AS num UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5
    UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
) n
WHERE a.id NOT IN (1, 2, 6, 11); -- 跳过上面已经手动插入的艺人

-- ==========================================
-- 7. 批量生成交互数据 (Simulation)
-- ==========================================
-- 逻辑：
-- Group A (10-19) 喜欢 Artist 1-5 (Mandopop)
-- Group B (20-29) 喜欢 Artist 6-10 (Western Pop)
-- Group C (30-40) 喜欢 Artist 11-20 (Rock/Mix)

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

SET FOREIGN_KEY_CHECKS = 1;

-- 脚本结束