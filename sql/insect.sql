/*
 * Spotify Music - 大规模真实测试数据 (Based on provided TXT file dumps)
 * 作者：Gemini (已解决艺人与用户关联问题)
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- BCrypt 密码 '123456' 的哈希值，统一使用 sys_user.txt 中的值
SET @pwd_hash = '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK';
SET @pwd_hash_real = '$2a$10$G60wTijOntzO74XXc.6.JuR1XBxOi5hZbJ3vKiXbfQ0oRzSLt.s7u';
SET @pwd_hash_active = '$2a$10$eg6xAZllEHOL2x8jSBPUeOFhpt.DZD5vnFhmOPLbMrymmBK6AGu2q';


-- ==========================================
-- 1. 重置数据库 (TRUNCATE ALL)
-- ==========================================
TRUNCATE TABLE user_interaction;
TRUNCATE TABLE music_playlist_song;
TRUNCATE TABLE music_playlist;
TRUNCATE TABLE song_comment;
TRUNCATE TABLE user_follow; -- 假设这张表存在，解决之前的问题
TRUNCATE TABLE music_song;
TRUNCATE TABLE music_album;
TRUNCATE TABLE music_artist;
TRUNCATE TABLE sys_user_role;
-- 只删除用户数据，不删角色定义
DELETE FROM sys_user WHERE id > 0;


-- ==========================================
-- 2. 角色定义 (sys_role) - 根据 sys_role.txt
-- ==========================================
-- 假设 sys_role 表已通过 create_music.sql 预先创建，这里仅做数据校验/插入
INSERT INTO `sys_role` (`id`, `role_key`, `role_name`, `create_time`, `update_time`) VALUES 
(1, 'admin', '管理员', '2025-11-26 00:44:36', '2025-11-26 00:44:36'),
(2, 'user', '普通用户', '2025-11-26 00:44:36', '2025-11-26 00:44:36'),
(3, 'musician', '音乐人', '2025-11-26 00:44:36', '2025-11-26 00:44:36')
ON DUPLICATE KEY UPDATE role_name=VALUES(role_name);


-- ==========================================
-- 3. 初始化用户 (sys_user) - 根据 sys_user.txt
-- ==========================================
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `avatar_url`, `email`, `deleted`, `create_time`, `update_time`) VALUES
(1, 'admin', @pwd_hash, 'Admin', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', 'admin@spotify.com', 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(2, 'musician', @pwd_hash, '官方音乐人', 'https://api.dicebear.com/7.x/avataaars/svg?seed=musician_official', 'music@spotify.com', 0, '2025-11-26 00:44:36', '2025-11-26 14:26:05'),
-- 插入普通用户 (ID 10-40)
(10, 'user', @pwd_hash, '华语迷1号', 'https://api.dicebear.com/7.x/avataaars/svg?seed=mando_fan_01', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 14:26:14'),
(11, 'mando_fan_02', @pwd_hash, '周董铁粉', 'https://api.dicebear.com/7.x/avataaars/svg?seed=mando_fan_02', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(12, 'mando_fan_03', @pwd_hash, 'Eason听众', 'https://api.dicebear.com/7.x/avataaars/svg?seed=mando_fan_03', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(13, 'mando_fan_04', @pwd_hash, 'JJ Lin', 'https://api.dicebear.com/7.x/avataaars/svg?seed=mando_fan_04', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(14, 'mando_fan_05', @pwd_hash, '邓紫棋粉', 'https://api.dicebear.com/7.x/avataaars/svg?seed=mando_fan_05', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(15, 'mando_fan_06', @pwd_hash, '五月天', 'https://api.dicebear.com/7.x/avataaars/svg?seed=mando_fan_06', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(16, 'mando_fan_07', @pwd_hash, 'KTV麦霸', 'https://api.dicebear.com/7.x/avataaars/svg?seed=mando_fan_07', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(17, 'mando_fan_08', @pwd_hash, '深夜听歌', 'https://api.dicebear.com/7.x/avataaars/svg?seed=mando_fan_08', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(18, 'mando_fan_09', @pwd_hash, '老歌回放', 'https://api.dicebear.com/7.x/avataaars/svg?seed=mando_fan_09', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(19, 'mando_fan_10', @pwd_hash, '华语Top', 'https://api.dicebear.com/7.x/avataaars/svg?seed=mando_fan_10', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(20, 'west_pop_01', @pwd_hash, 'Swiftie', 'https://api.dicebear.com/7.x/avataaars/svg?seed=west_pop_01', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(21, 'west_pop_02', @pwd_hash, 'JB Fan', 'https://api.dicebear.com/7.x/avataaars/svg?seed=west_pop_02', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(22, 'west_pop_03', @pwd_hash, 'Ed Sheeran', 'https://api.dicebear.com/7.x/avataaars/svg?seed=west_pop_03', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(23, 'west_pop_04', @pwd_hash, 'Ariana', 'https://api.dicebear.com/7.x/avataaars/svg?seed=west_pop_04', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(24, 'west_pop_05', @pwd_hash, 'Billboard', 'https://api.dicebear.com/7.x/avataaars/svg?seed=west_pop_05', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(25, 'west_pop_06', @pwd_hash, 'Grammy', 'https://api.dicebear.com/7.x/avataaars/svg?seed=west_pop_06', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(26, 'west_pop_07', @pwd_hash, 'Maroon5', 'https://api.dicebear.com/7.x/avataaars/svg?seed=west_pop_07', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(27, 'west_pop_08', @pwd_hash, 'Bruno Mars', 'https://api.dicebear.com/7.x/avataaars/svg?seed=west_pop_08', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(28, 'west_pop_09', @pwd_hash, 'The Weeknd', 'https://api.dicebear.com/7.x/avataaars/svg?seed=west_pop_09', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(29, 'west_pop_10', @pwd_hash, 'Coldplay', 'https://api.dicebear.com/7.x/avataaars/svg?seed=west_pop_10', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(30, 'rocker_01', @pwd_hash, 'Linkin Park', 'https://api.dicebear.com/7.x/avataaars/svg?seed=rocker_01', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(31, 'rocker_02', @pwd_hash, 'Nirvana', 'https://api.dicebear.com/7.x/avataaars/svg?seed=rocker_02', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(32, 'hiphop_01', @pwd_hash, 'Eminem', 'https://api.dicebear.com/7.x/avataaars/svg?seed=hiphop_01', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(33, 'hiphop_02', @pwd_hash, 'Drake', 'https://api.dicebear.com/7.x/avataaars/svg?seed=hiphop_02', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(34, 'kpop_01', @pwd_hash, 'BTS Army', 'https://api.dicebear.com/7.x/avataaars/svg?seed=kpop_01', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(35, 'kpop_02', @pwd_hash, 'Blink', 'https://api.dicebear.com/7.x/avataaars/svg?seed=kpop_02', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(36, 'anime_01', @pwd_hash, 'Otaku', 'https://api.dicebear.com/7.x/avataaars/svg?seed=anime_01', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(37, 'classic_01', @pwd_hash, 'Mozart', 'https://api.dicebear.com/7.x/avataaars/svg?seed=classic_01', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(38, 'jazz_01', @pwd_hash, 'Jazz Bar', 'https://api.dicebear.com/7.x/avataaars/svg?seed=jazz_01', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(39, 'mix_01', @pwd_hash, 'Random Listener', 'https://api.dicebear.com/7.x/avataaars/svg?seed=mix_01', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(40, 'mix_02', @pwd_hash, 'Music Holic', 'https://api.dicebear.com/7.x/avataaars/svg?seed=mix_02', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:30'),
(41, 'real_musician', @pwd_hash_real, '原创歌手', 'https://api.dicebear.com/7.x/avataaars/svg?seed=real_musician', 'music@test.com', 0, '2025-11-26 03:12:35', '2025-11-26 13:09:30'),
(42, 'active_user', @pwd_hash_active, '超级活跃听众', 'https://api.dicebear.com/7.x/avataaars/svg?seed=active_user', 'new_email@test.com', 0, '2025-11-26 03:12:35', '2025-11-26 13:09:30');


-- ==========================================
-- 4. 解决艺人绑定问题: 新增 19 个音乐人账号 (ID 43-61)
-- ==========================================
-- 艺人 ID 2-20 需要绑定用户 ID 43-61
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `avatar_url`) VALUES
(43, 'musician_chen', @pwd_hash, '陈奕迅官方', 'https://api.dicebear.com/7.x/avataaars/svg?seed=43'),
(44, 'musician_lin', @pwd_hash, '林俊杰官方', 'https://api.dicebear.com/7.x/avataaars/svg?seed=44'),
(45, 'musician_deng', @pwd_hash, '邓紫棋官方', 'https://api.dicebear.com/7.x/avataaars/svg?seed=45'),
(46, 'musician_mayday', @pwd_hash, '五月天官方', 'https://api.dicebear.com/7.x/avataaars/svg?seed=46'),
(47, 'musician_taylor', @pwd_hash, 'Taylor Swift Official', 'https://api.dicebear.com/7.x/avataaars/svg?seed=47'),
(48, 'musician_edsheeran', @pwd_hash, 'Ed Sheeran Official', 'https://api.dicebear.com/7.x/avataaars/svg?seed=48'),
(49, 'musician_jb', @pwd_hash, 'Justin Bieber Official', 'https://api.dicebear.com/7.x/avataaars/svg?seed=49'),
(50, 'musician_ariana', @pwd_hash, 'Ariana Grande Official', 'https://api.dicebear.com/7.x/avataaars/svg?seed=50'),
(51, 'musician_bruno', @pwd_hash, 'Bruno Mars Official', 'https://api.dicebear.com/7.x/avataaars/svg?seed=51'),
(52, 'musician_lp', @pwd_hash, 'Linkin Park Official', 'https://api.dicebear.com/7.x/avataaars/svg?seed=52'),
(53, 'musician_coldplay', @pwd_hash, 'Coldplay Official', 'https://api.dicebear.com/7.x/avataaars/svg?seed=53'),
(54, 'musician_id', @pwd_hash, 'Imagine Dragons Official', 'https://api.dicebear.com/7.x/avataaars/svg?seed=54'),
(55, 'musician_queen', @pwd_hash, 'Queen Official', 'https://api.dicebear.com/7.x/avataaars/svg?seed=55'),
(56, 'musician_nirvana', @pwd_hash, 'Nirvana Official', 'https://api.dicebear.com/7.x/avataaars/svg?seed=56'),
(57, 'musician_bts', @pwd_hash, 'BTS Official', 'https://api.dicebear.com/7.x/avataaars/svg?seed=57'),
(58, 'musician_bp', @pwd_hash, 'BLACKPINK Official', 'https://api.dicebear.com/7.x/avataaars/svg?seed=58'),
(59, 'musician_eminem', @pwd_hash, 'Eminem Official', 'https://api.dicebear.com/7.x/avataaars/svg?seed=59'),
(60, 'musician_weeknd', @pwd_hash, 'The Weeknd Official', 'https://api.dicebear.com/7.x/avataaars/svg?seed=60'),
(61, 'musician_adele', @pwd_hash, 'Adele Official', 'https://api.dicebear.com/7.x/avataaars/svg?seed=61');


-- ==========================================
-- 5. 用户角色关联 (sys_user_role) - 根据 sys_user_role.txt + 新增音乐人角色
-- ==========================================
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1), -- admin
(2, 3), -- musician
-- (4, 3) 缺失用户，跳过
(10, 2),
(11, 2),
(12, 2),
(13, 2),
(14, 2),
(15, 2),
(16, 2),
(17, 2),
(18, 2),
(19, 2),
(20, 2),
(21, 2),
(22, 2),
(23, 2),
(24, 2),
(25, 2),
(26, 2),
(27, 2),
(28, 2),
(29, 2),
(30, 2),
(31, 2),
(32, 2),
(33, 2),
(34, 2),
(35, 2),
(36, 2),
(37, 2),
(38, 2),
(39, 2),
(40, 2),
-- 新增音乐人角色
(43, 3), 
(44, 3), 
(45, 3), 
(46, 3), 
(47, 3), 
(48, 3), 
(49, 3), 
(50, 3), 
(51, 3), 
(52, 3), 
(53, 3), 
(54, 3), 
(55, 3), 
(56, 3), 
(57, 3), 
(58, 3), 
(59, 3), 
(60, 3), 
(61, 3);


-- ==========================================
-- 6. 艺人信息 (music_artist) - 根据 music_artist.txt + 绑定用户
-- ==========================================
INSERT INTO `music_artist` (`id`, `user_id`, `name`, `bio`, `avatar_url`, `create_time`, `update_time`) VALUES
-- ID 1 绑定用户 2
(1, 2, '周杰伦', '华语流行天王', 'https://api.dicebear.com/7.x/initials/svg?seed=周杰伦', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
-- ID 2-20 绑定新用户 43-61
(2, 43, '陈奕迅', '香港著名男歌手', 'https://api.dicebear.com/7.x/initials/svg?seed=陈奕迅', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(3, 44, '林俊杰', '行走的CD', 'https://api.dicebear.com/7.x/initials/svg?seed=林俊杰', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(4, 45, '邓紫棋', '巨肺小天后', 'https://api.dicebear.com/7.x/initials/svg?seed=邓紫棋', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(5, 46, '五月天', '亚洲第一摇滚天团', 'https://api.dicebear.com/7.x/initials/svg?seed=五月天', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(6, 47, 'Taylor Swift', '美国流行天后', 'https://api.dicebear.com/7.x/initials/svg?seed=TaylorSwift', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(7, 48, 'Ed Sheeran', '英国创作才子', 'https://api.dicebear.com/7.x/initials/svg?seed=EdSheeran', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(8, 49, 'Justin Bieber', '全球偶像', 'https://api.dicebear.com/7.x/initials/svg?seed=JustinBieber', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(9, 50, 'Ariana Grande', '高音女神', 'https://api.dicebear.com/7.x/initials/svg?seed=ArianaGrande', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(10, 51, 'Bruno Mars', '火星哥', 'https://api.dicebear.com/7.x/initials/svg?seed=BrunoMars', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(11, 52, 'Linkin Park', '新金属摇滚代表', 'https://api.dicebear.com/7.x/initials/svg?seed=LinkinPark', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(12, 53, 'Coldplay', '英伦摇滚乐队', 'https://api.dicebear.com/7.x/initials/svg?seed=Coldplay', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(13, 54, 'Imagine Dragons', '梦龙乐队', 'https://api.dicebear.com/7.x/initials/svg?seed=ImagineDragons', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(14, 55, 'Queen', '传奇摇滚乐队', 'https://api.dicebear.com/7.x/initials/svg?seed=Queen', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(15, 56, 'Nirvana', 'Grunge先驱', 'https://api.dicebear.com/7.x/initials/svg?seed=Nirvana', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(16, 57, 'BTS', '防弹少年团', 'https://api.dicebear.com/7.x/initials/svg?seed=BTS', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(17, 58, 'BLACKPINK', '大势女团', 'https://api.dicebear.com/7.x/initials/svg?seed=BLACKPINK', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(18, 59, 'Eminem', '说唱之神', 'https://api.dicebear.com/7.x/initials/svg?seed=Eminem', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(19, 60, 'The Weeknd', 'R&B天王', 'https://api.dicebear.com/7.x/initials/svg?seed=TheWeeknd', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(20, 61, 'Adele', '灵魂歌姬', 'https://api.dicebear.com/7.x/initials/svg?seed=Adele', '2025-11-26 00:44:36', '2025-11-26 13:09:22');


-- ==========================================
-- 7. 专辑信息 (music_album) - 根据 music_album.txt
-- ==========================================
INSERT INTO `music_album` (`id`, `artist_id`, `title`, `cover_url`, `description`, `release_date`, `create_time`, `update_time`) VALUES
(101, 1, '周杰伦 Best Hits', 'https://placehold.co/300x300/282828/ffffff?text=周杰伦+Best+Hits', NULL, '2020-01-01', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(102, 2, '陈奕迅 Best Hits', 'https://placehold.co/300x300/282828/ffffff?text=陈奕迅+Best+Hits', NULL, '2020-01-01', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(103, 3, '林俊杰 Best Hits', 'https://placehold.co/300x300/282828/ffffff?text=林俊杰+Best+Hits', NULL, '2020-01-01', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(104, 4, '邓紫棋 Best Hits', 'https://placehold.co/300x300/282828/ffffff?text=邓紫棋+Best+Hits', NULL, '2020-01-01', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(105, 5, '五月天 Best Hits', 'https://placehold.co/300x300/282828/ffffff?text=五月天+Best+Hits', NULL, '2020-01-01', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(106, 6, 'Taylor Swift Best Hits', 'https://placehold.co/300x300/282828/ffffff?text=Taylor+Swift+Best+Hits', NULL, '2020-01-01', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(107, 7, 'Ed Sheeran Best Hits', 'https://placehold.co/300x300/282828/ffffff?text=Ed+Sheeran+Best+Hits', NULL, '2020-01-01', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(108, 8, 'Justin Bieber Best Hits', 'https://placehold.co/300x300/282828/ffffff?text=Justin+Bieber+Best+Hits', NULL, '2020-01-01', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(109, 9, 'Ariana Grande Best Hits', 'https://placehold.co/300x300/282828/ffffff?text=Ariana+Grande+Best+Hits', NULL, '2020-01-01', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(110, 10, 'Bruno Mars Best Hits', 'https://placehold.co/300x300/282828/ffffff?text=Bruno+Mars+Best+Hits', NULL, '2020-01-01', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(111, 11, 'Linkin Park Best Hits', 'https://placehold.co/300x300/282828/ffffff?text=Linkin+Park+Best+Hits', NULL, '2020-01-01', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(112, 12, 'Coldplay Best Hits', 'https://placehold.co/300x300/282828/ffffff?text=Coldplay+Best+Hits', NULL, '2020-01-01', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(113, 13, 'Imagine Dragons Best Hits', 'https://placehold.co/300x300/282828/ffffff?text=Imagine+Dragons+Best+Hits', NULL, '2020-01-01', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(114, 14, 'Queen Best Hits', 'https://placehold.co/300x300/282828/ffffff?text=Queen+Best+Hits', NULL, '2020-01-01', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(115, 15, 'Nirvana Best Hits', 'https://placehold.co/300x300/282828/ffffff?text=Nirvana+Best+Hits', NULL, '2020-01-01', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(116, 16, 'BTS Best Hits', 'https://placehold.co/300x300/282828/ffffff?text=BTS+Best+Hits', NULL, '2020-01-01', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(117, 17, 'BLACKPINK Best Hits', 'https://placehold.co/300x300/282828/ffffff?text=BLACKPINK+Best+Hits', NULL, '2020-01-01', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(118, 18, 'Eminem Best Hits', 'https://placehold.co/300x300/282828/ffffff?text=Eminem+Best+Hits', NULL, '2020-01-01', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(119, 19, 'The Weeknd Best Hits', 'https://placehold.co/300x300/282828/ffffff?text=The+Weeknd+Best+Hits', NULL, '2020-01-01', '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(120, 20, 'Adele Best Hits', 'https://placehold.co/300x300/282828/ffffff?text=Adele+Best+Hits', NULL, '2020-01-01', '2025-11-26 00:44:36', '2025-11-26 13:09:22');


-- ==========================================
-- 8. 歌曲信息 (music_song) - 根据 music_song.txt
-- 批量插入歌曲数据
-- ==========================================
INSERT INTO `music_song` (`id`, `album_id`, `artist_id`, `title`, `file_url`, `cover_url`, `duration`, `genre`, `lyrics`, `play_count`, `create_time`, `update_time`) VALUES
(1, 101, 1, '七里香', 'http://music.163.com/song/media/outer/url?id=108420.mp3', 'https://placehold.co/300x300/181818/1db954?text=七里香', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(2, 101, 1, '青花瓷', '', 'https://placehold.co/300x300/181818/1db954?text=青花瓷', 0, 'Pop', NULL, 1, '2025-11-26 00:44:36', '2025-11-26 16:08:11'),
(3, 101, 1, '稻香', '', 'https://placehold.co/300x300/181818/1db954?text=稻香', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(4, 101, 1, '夜曲', '', 'https://placehold.co/300x300/181818/1db954?text=夜曲', 0, 'Pop', NULL, 1, '2025-11-26 00:44:36', '2025-11-26 14:26:28'),
(5, 101, 1, '晴天', '', 'https://placehold.co/300x300/181818/1db954?text=晴天', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(6, 101, 1, '告白气球', '', 'https://placehold.co/300x300/181818/1db954?text=告白气球', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(7, 101, 1, '一路向北', '', 'https://placehold.co/300x300/181818/1db954?text=一路向北', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(8, 101, 1, '不能说的秘密', '', 'https://placehold.co/300x300/181818/1db954?text=不能说的秘密', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(9, 101, 1, '搁浅', '', 'https://placehold.co/300x300/181818/1db954?text=搁浅', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(10, 101, 1, '简单爱', '', 'https://placehold.co/300x300/181818/1db954?text=简单爱', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(11, 102, 2, '十年', '', 'https://placehold.co/300x300/181818/1db954?text=十年', 0, 'Ballad', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(12, 102, 2, '浮夸', '', 'https://placehold.co/300x300/181818/1db954?text=浮夸', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(13, 102, 2, 'K歌之王', '', 'https://placehold.co/300x300/181818/1db954?text=K歌之王', 0, 'Ballad', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(14, 102, 2, '好久不见', '', 'https://placehold.co/300x300/181818/1db954?text=好久不见', 0, 'Ballad', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(15, 102, 2, '红玫瑰', '', 'https://placehold.co/300x300/181818/1db954?text=红玫瑰', 0, 'Ballad', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(16, 102, 2, '富士山下', '', 'https://placehold.co/300x300/181818/1db954?text=富士山下', 0, 'Ballad', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(17, 102, 2, '单车', '', 'https://placehold.co/300x300/181818/1db954?text=单车', 0, 'Ballad', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(18, 102, 2, '不要说话', '', 'https://placehold.co/300x300/181818/1db954?text=不要说话', 0, 'Ballad', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(19, 102, 2, '陪你度过漫长岁月', '', 'https://placehold.co/300x300/181818/1db954?text=陪你度过漫长岁月', 0, 'Ballad', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(20, 102, 2, '爱情转移', '', 'https://placehold.co/300x300/181818/1db954?text=爱情转移', 0, 'Ballad', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(21, 106, 6, 'Love Story', '', 'https://placehold.co/300x300/181818/1db954?text=Love+Story', 0, 'Country', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(22, 106, 6, 'Blank Space', '', 'https://placehold.co/300x300/181818/1db954?text=Blank+Space', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(23, 106, 6, 'Shake It Off', '', 'https://placehold.co/300x300/181818/1db954?text=Shake+It+Off', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(24, 106, 6, 'Cruel Summer', '', 'https://placehold.co/300x300/181818/1db954?text=Cruel+Summer', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(25, 106, 6, 'Anti-Hero', '', 'https://placehold.co/300x300/181818/1db954?text=Anti-Hero', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(26, 106, 6, 'Style', '', 'https://placehold.co/300x300/181818/1db954?text=Style', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(27, 106, 6, 'You Belong With Me', '', 'https://placehold.co/300x300/181818/1db954?text=You+Belong+With+Me', 0, 'Country', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(28, 106, 6, 'Lover', '', 'https://placehold.co/300x300/181818/1db954?text=Lover', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(29, 106, 6, 'Bad Blood', '', 'https://placehold.co/300x300/181818/1db954?text=Bad+Blood', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(30, 106, 6, 'Look What You Made Me Do', '', 'https://placehold.co/300x300/181818/1db954?text=Look+What+You+Made+Me+Do', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(31, 111, 11, 'In The End', '', 'https://placehold.co/300x300/181818/1db954?text=In+The+End', 0, 'Rock', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(32, 111, 11, 'Numb', '', 'https://placehold.co/300x300/181818/1db954?text=Numb', 0, 'Rock', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(33, 111, 11, 'Faint', '', 'https://placehold.co/300x300/181818/1db954?text=Faint', 0, 'Rock', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(34, 111, 11, 'Crawling', '', 'https://placehold.co/300x300/181818/1db954?text=Crawling', 0, 'Rock', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(35, 111, 11, 'One Step Closer', '', 'https://placehold.co/300x300/181818/1db954?text=One+Step+Closer', 0, 'Rock', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(36, 111, 11, 'Breaking The Habit', '', 'https://placehold.co/300x300/181818/1db954?text=Breaking+The+Habit', 0, 'Rock', NULL, 1, '2025-11-26 00:44:36', '2025-11-26 16:14:38'),
(37, 111, 11, 'Somewhere I Belong', '', 'https://placehold.co/300x300/181818/1db954?text=Somewhere+I+Belong', 0, 'Rock', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(38, 111, 11, 'What I\'ve Done', '', 'https://placehold.co/300x300/181818/1db954?text=What+I\'ve+Done', 0, 'Rock', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(39, 111, 11, 'New Divide', '', 'https://placehold.co/300x300/181818/1db954?text=New+Divide', 0, 'Rock', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(40, 111, 11, 'Burn It Down', '', 'https://placehold.co/300x300/181818/1db954?text=Burn+It+Down', 0, 'Rock', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(41, 103, 3, '林俊杰 Song 10', '', 'https://placehold.co/300x300/181818/1db954?text=林俊杰+Song+10', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(42, 103, 3, '林俊杰 Song 9', '', 'https://placehold.co/300x300/181818/1db954?text=林俊杰+Song+9', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(43, 103, 3, '林俊杰 Song 8', '', 'https://placehold.co/300x300/181818/1db954?text=林俊杰+Song+8', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(44, 103, 3, '林俊杰 Song 7', '', 'https://placehold.co/300x300/181818/1db954?text=林俊杰+Song+7', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(45, 103, 3, '林俊杰 Song 6', '', 'https://placehold.co/300x300/181818/1db954?text=林俊杰+Song+6', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(46, 103, 3, '林俊杰 Song 5', '', 'https://placehold.co/300x300/181818/1db954?text=林俊杰+Song+5', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(47, 103, 3, '林俊杰 Song 4', '', 'https://placehold.co/300x300/181818/1db954?text=林俊杰+Song+4', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(48, 103, 3, '林俊杰 Song 3', '', 'https://placehold.co/300x300/181818/1db954?text=林俊杰+Song+3', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(49, 103, 3, '林俊杰 Song 2', '', 'https://placehold.co/300x300/181818/1db954?text=林俊杰+Song+2', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(50, 103, 3, '林俊杰 Song 1', '', 'https://placehold.co/300x300/181818/1db954?text=林俊杰+Song+1', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(51, 104, 4, '邓紫棋 Song 10', '', 'https://placehold.co/300x300/181818/1db954?text=邓紫棋+Song+10', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(52, 104, 4, '邓紫棋 Song 9', '', 'https://placehold.co/300x300/181818/1db954?text=邓紫棋+Song+9', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(53, 104, 4, '邓紫棋 Song 8', '', 'https://placehold.co/300x300/181818/1db954?text=邓紫棋+Song+8', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(54, 104, 4, '邓紫棋 Song 7', '', 'https://placehold.co/300x300/181818/1db954?text=邓紫棋+Song+7', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(55, 104, 4, '邓紫棋 Song 6', '', 'https://placehold.co/300x300/181818/1db954?text=邓紫棋+Song+6', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(56, 104, 4, '邓紫棋 Song 5', '', 'https://placehold.co/300x300/181818/1db954?text=邓紫棋+Song+5', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(57, 104, 4, '邓紫棋 Song 4', '', 'https://placehold.co/300x300/181818/1db954?text=邓紫棋+Song+4', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(58, 104, 4, '邓紫棋 Song 3', '', 'https://placehold.co/300x300/181818/1db954?text=邓紫棋+Song+3', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(59, 104, 4, '邓紫棋 Song 2', '', 'https://placehold.co/300x300/181818/1db954?text=邓紫棋+Song+2', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(60, 104, 4, '邓紫棋 Song 1', '', 'https://placehold.co/300x300/181818/1db954?text=邓紫棋+Song+1', 0, 'Pop', NULL, 1, '2025-11-26 00:44:36', '2025-11-26 14:25:45'),
(61, 105, 5, '五月天 Song 10', '', 'https://placehold.co/300x300/181818/1db954?text=五月天+Song+10', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(62, 105, 5, '五月天 Song 9', '', 'https://placehold.co/300x300/181818/1db954?text=五月天+Song+9', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(63, 105, 5, '五月天 Song 8', '', 'https://placehold.co/300x300/181818/1db954?text=五月天+Song+8', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(64, 105, 5, '五月天 Song 7', '', 'https://placehold.co/300x300/181818/1db954?text=五月天+Song+7', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(65, 105, 5, '五月天 Song 6', '', 'https://placehold.co/300x300/181818/1db954?text=五月天+Song+6', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(66, 105, 5, '五月天 Song 5', '', 'https://placehold.co/300x300/181818/1db954?text=五月天+Song+5', 0, 'Pop', NULL, 1, '2025-11-26 00:44:36', '2025-11-26 14:25:46'),
(67, 105, 5, '五月天 Song 4', '', 'https://placehold.co/300x300/181818/1db954?text=五月天+Song+4', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(68, 105, 5, '五月天 Song 3', '', 'https://placehold.co/300x300/181818/1db954?text=五月天+Song+3', 0, 'Pop', NULL, 1, '2025-11-26 00:44:36', '2025-11-26 15:47:09'),
(69, 105, 5, '五月天 Song 2', '', 'https://placehold.co/300x300/181818/1db954?text=五月天+Song+2', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(70, 105, 5, '五月天 Song 1', '', 'https://placehold.co/300x300/181818/1db954?text=五月天+Song+1', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(71, 107, 7, 'Ed Sheeran Song 10', '', 'https://placehold.co/300x300/181818/1db954?text=Ed+Sheeran+Song+10', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(72, 107, 7, 'Ed Sheeran Song 9', '', 'https://placehold.co/300x300/181818/1db954?text=Ed+Sheeran+Song+9', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(73, 107, 7, 'Ed Sheeran Song 8', '', 'https://placehold.co/300x300/181818/1db954?text=Ed+Sheeran+Song+8', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(74, 107, 7, 'Ed Sheeran Song 7', '', 'https://placehold.co/300x300/181818/1db954?text=Ed+Sheeran+Song+7', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(75, 107, 7, 'Ed Sheeran Song 6', '', 'https://placehold.co/300x300/181818/1db954?text=Ed+Sheeran+Song+6', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(76, 107, 7, 'Ed Sheeran Song 5', '', 'https://placehold.co/300x300/181818/1db954?text=Ed+Sheeran+Song+5', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(77, 107, 7, 'Ed Sheeran Song 4', '', 'https://placehold.co/300x300/181818/1db954?text=Ed+Sheeran+Song+4', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(78, 107, 7, 'Ed Sheeran Song 3', '', 'https://placehold.co/300x300/181818/1db954?text=Ed+Sheeran+Song+3', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(79, 107, 7, 'Ed Sheeran Song 2', '', 'https://placehold.co/300x300/181818/1db954?text=Ed+Sheeran+Song+2', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(80, 107, 7, 'Ed Sheeran Song 1', '', 'https://placehold.co/300x300/181818/1db954?text=Ed+Sheeran+Song+1', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(81, 108, 8, 'Justin Bieber Song 10', '', 'https://placehold.co/300x300/181818/1db954?text=Justin+Bieber+Song+10', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(82, 108, 8, 'Justin Bieber Song 9', '', 'https://placehold.co/300x300/181818/1db954?text=Justin+Bieber+Song+9', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(83, 108, 8, 'Justin Bieber Song 8', '', 'https://placehold.co/300x300/181818/1db954?text=Justin+Bieber+Song+8', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(84, 108, 8, 'Justin Bieber Song 7', '', 'https://placehold.co/300x300/181818/1db954?text=Justin+Bieber+Song+7', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(85, 108, 8, 'Justin Bieber Song 6', '', 'https://placehold.co/300x300/181818/1db954?text=Justin+Bieber+Song+6', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(86, 108, 8, 'Justin Bieber Song 5', '', 'https://placehold.co/300x300/181818/1db954?text=Justin+Bieber+Song+5', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(87, 108, 8, 'Justin Bieber Song 4', '', 'https://placehold.co/300x300/181818/1db954?text=Justin+Bieber+Song+4', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(88, 108, 8, 'Justin Bieber Song 3', '', 'https://placehold.co/300x300/181818/1db954?text=Justin+Bieber+Song+3', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(89, 108, 8, 'Justin Bieber Song 2', '', 'https://placehold.co/300x300/181818/1db954?text=Justin+Bieber+Song+2', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(90, 108, 8, 'Justin Bieber Song 1', '', 'https://placehold.co/300x300/181818/1db954?text=Justin+Bieber+Song+1', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(91, 109, 9, 'Ariana Grande Song 10', '', 'https://placehold.co/300x300/181818/1db954?text=Ariana+Grande+Song+10', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(92, 109, 9, 'Ariana Grande Song 9', '', 'https://placehold.co/300x300/181818/1db954?text=Ariana+Grande+Song+9', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(93, 109, 9, 'Ariana Grande Song 8', '', 'https://placehold.co/300x300/181818/1db954?text=Ariana+Grande+Song+8', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(94, 109, 9, 'Ariana Grande Song 7', '', 'https://placehold.co/300x300/181818/1db954?text=Ariana+Grande+Song+7', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(95, 109, 9, 'Ariana Grande Song 6', '', 'https://placehold.co/300x300/181818/1db954?text=Ariana+Grande+Song+6', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(96, 109, 9, 'Ariana Grande Song 5', '', 'https://placehold.co/300x300/181818/1db954?text=Ariana+Grande+Song+5', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(97, 109, 9, 'Ariana Grande Song 4', '', 'https://placehold.co/300x300/181818/1db954?text=Ariana+Grande+Song+4', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(98, 109, 9, 'Ariana Grande Song 3', '', 'https://placehold.co/300x300/181818/1db954?text=Ariana+Grande+Song+3', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(99, 109, 9, 'Ariana Grande Song 2', '', 'https://placehold.co/300x300/181818/1db954?text=Ariana+Grande+Song+2', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(100, 109, 9, 'Ariana Grande Song 1', '', 'https://placehold.co/300x300/181818/1db954?text=Ariana+Grande+Song+1', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(101, 110, 10, 'Bruno Mars Song 10', '', 'https://placehold.co/300x300/181818/1db954?text=Bruno+Mars+Song+10', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(102, 110, 10, 'Bruno Mars Song 9', '', 'https://placehold.co/300x300/181818/1db954?text=Bruno+Mars+Song+9', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(103, 110, 10, 'Bruno Mars Song 8', '', 'https://placehold.co/300x300/181818/1db954?text=Bruno+Mars+Song+8', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(104, 110, 10, 'Bruno Mars Song 7', '', 'https://placehold.co/300x300/181818/1db954?text=Bruno+Mars+Song+7', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(105, 110, 10, 'Bruno Mars Song 6', '', 'https://placehold.co/300x300/181818/1db954?text=Bruno+Mars+Song+6', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(106, 110, 10, 'Bruno Mars Song 5', '', 'https://placehold.co/300x300/181818/1db954?text=Bruno+Mars+Song+5', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(107, 110, 10, 'Bruno Mars Song 4', '', 'https://placehold.co/300x300/181818/1db954?text=Bruno+Mars+Song+4', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(108, 110, 10, 'Bruno Mars Song 3', '', 'https://placehold.co/300x300/181818/1db954?text=Bruno+Mars+Song+3', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(109, 110, 10, 'Bruno Mars Song 2', '', 'https://placehold.co/300x300/181818/1db954?text=Bruno+Mars+Song+2', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(110, 110, 10, 'Bruno Mars Song 1', '', 'https://placehold.co/300x300/181818/1db954?text=Bruno+Mars+Song+1', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(111, 112, 12, 'Coldplay Song 10', '', 'https://placehold.co/300x300/181818/1db954?text=Coldplay+Song+10', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(112, 112, 12, 'Coldplay Song 9', '', 'https://placehold.co/300x300/181818/1db954?text=Coldplay+Song+9', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(113, 112, 12, 'Coldplay Song 8', '', 'https://placehold.co/300x300/181818/1db954?text=Coldplay+Song+8', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(114, 112, 12, 'Coldplay Song 7', '', 'https://placehold.co/300x300/181818/1db954?text=Coldplay+Song+7', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(115, 112, 12, 'Coldplay Song 6', '', 'https://placehold.co/300x300/181818/1db954?text=Coldplay+Song+6', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(116, 112, 12, 'Coldplay Song 5', '', 'https://placehold.co/300x300/181818/1db954?text=Coldplay+Song+5', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(117, 112, 12, 'Coldplay Song 4', '', 'https://placehold.co/300x300/181818/1db954?text=Coldplay+Song+4', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(118, 112, 12, 'Coldplay Song 3', '', 'https://placehold.co/300x300/181818/1db954?text=Coldplay+Song+3', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(119, 112, 12, 'Coldplay Song 2', '', 'https://placehold.co/300x300/181818/1db954?text=Coldplay+Song+2', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(120, 112, 12, 'Coldplay Song 1', '', 'https://placehold.co/300x300/181818/1db954?text=Coldplay+Song+1', 0, 'Pop', NULL, 1, '2025-11-26 00:44:36', '2025-11-26 16:15:00'),
(121, 113, 13, 'Imagine Dragons Song 10', '', 'https://placehold.co/300x300/181818/1db954?text=Imagine+Dragons+Song+10', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(122, 113, 13, 'Imagine Dragons Song 9', '', 'https://placehold.co/300x300/181818/1db954?text=Imagine+Dragons+Song+9', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(123, 113, 13, 'Imagine Dragons Song 8', '', 'https://placehold.co/300x300/181818/1db954?text=Imagine+Dragons+Song+8', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(124, 113, 13, 'Imagine Dragons Song 7', '', 'https://placehold.co/300x300/181818/1db954?text=Imagine+Dragons+Song+7', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(125, 113, 13, 'Imagine Dragons Song 6', '', 'https://placehold.co/300x300/181818/1db954?text=Imagine+Dragons+Song+6', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(126, 113, 13, 'Imagine Dragons Song 5', '', 'https://placehold.co/300x300/181818/1db954?text=Imagine+Dragons+Song+5', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(127, 113, 13, 'Imagine Dragons Song 4', '', 'https://placehold.co/300x300/181818/1db954?text=Imagine+Dragons+Song+4', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(128, 113, 13, 'Imagine Dragons Song 3', '', 'https://placehold.co/300x300/181818/1db954?text=Imagine+Dragons+Song+3', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(129, 113, 13, 'Imagine Dragons Song 2', '', 'https://placehold.co/300x300/181818/1db954?text=Imagine+Dragons+Song+2', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(130, 113, 13, 'Imagine Dragons Song 1', '', 'https://placehold.co/300x300/181818/1db954?text=Imagine+Dragons+Song+1', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(131, 114, 14, 'Queen Song 10', '', 'https://placehold.co/300x300/181818/1db954?text=Queen+Song+10', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(132, 114, 14, 'Queen Song 9', '', 'https://placehold.co/300x300/181818/1db954?text=Queen+Song+9', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(133, 114, 14, 'Queen Song 8', '', 'https://placehold.co/300x300/181818/1db954?text=Queen+Song+8', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(134, 114, 14, 'Queen Song 7', '', 'https://placehold.co/300x300/181818/1db954?text=Queen+Song+7', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(135, 114, 14, 'Queen Song 6', '', 'https://placehold.co/300x300/181818/1db954?text=Queen+Song+6', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(136, 114, 14, 'Queen Song 5', '', 'https://placehold.co/300x300/181818/1db954?text=Queen+Song+5', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(137, 114, 14, 'Queen Song 4', '', 'https://placehold.co/300x300/181818/1db954?text=Queen+Song+4', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(138, 114, 14, 'Queen Song 3', '', 'https://placehold.co/300x300/181818/1db954?text=Queen+Song+3', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(139, 114, 14, 'Queen Song 2', '', 'https://placehold.co/300x300/181818/1db954?text=Queen+Song+2', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(140, 114, 14, 'Queen Song 1', '', 'https://placehold.co/300x300/181818/1db954?text=Queen+Song+1', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(141, 115, 15, 'Nirvana Song 10', '', 'https://placehold.co/300x300/181818/1db954?text=Nirvana+Song+10', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(142, 115, 15, 'Nirvana Song 9', '', 'https://placehold.co/300x300/181818/1db954?text=Nirvana+Song+9', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(143, 115, 15, 'Nirvana Song 8', '', 'https://placehold.co/300x300/181818/1db954?text=Nirvana+Song+8', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(144, 115, 15, 'Nirvana Song 7', '', 'https://placehold.co/300x300/181818/1db954?text=Nirvana+Song+7', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(145, 115, 15, 'Nirvana Song 6', '', 'https://placehold.co/300x300/181818/1db954?text=Nirvana+Song+6', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(146, 115, 15, 'Nirvana Song 5', '', 'https://placehold.co/300x300/181818/1db954?text=Nirvana+Song+5', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(147, 115, 15, 'Nirvana Song 4', '', 'https://placehold.co/300x300/181818/1db954?text=Nirvana+Song+4', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(148, 115, 15, 'Nirvana Song 3', '', 'https://placehold.co/300x300/181818/1db954?text=Nirvana+Song+3', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(149, 115, 15, 'Nirvana Song 2', '', 'https://placehold.co/300x300/181818/1db954?text=Nirvana+Song+2', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(150, 115, 15, 'Nirvana Song 1', '', 'https://placehold.co/300x300/181818/1db954?text=Nirvana+Song+1', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(151, 116, 16, 'BTS Song 10', '', 'https://placehold.co/300x300/181818/1db954?text=BTS+Song+10', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(152, 116, 16, 'BTS Song 9', '', 'https://placehold.co/300x300/181818/1db954?text=BTS+Song+9', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(153, 116, 16, 'BTS Song 8', '', 'https://placehold.co/300x300/181818/1db954?text=BTS+Song+8', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(154, 116, 16, 'BTS Song 7', '', 'https://placehold.co/300x300/181818/1db954?text=BTS+Song+7', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(155, 116, 16, 'BTS Song 6', '', 'https://placehold.co/300x300/181818/1db954?text=BTS+Song+6', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(156, 116, 16, 'BTS Song 5', '', 'https://placehold.co/300x300/181818/1db954?text=BTS+Song+5', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(157, 116, 16, 'BTS Song 4', '', 'https://placehold.co/300x300/181818/1db954?text=BTS+Song+4', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(158, 116, 16, 'BTS Song 3', '', 'https://placehold.co/300x300/181818/1db954?text=BTS+Song+3', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(159, 116, 16, 'BTS Song 2', '', 'https://placehold.co/300x300/181818/1db954?text=BTS+Song+2', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(160, 116, 16, 'BTS Song 1', '', 'https://placehold.co/300x300/181818/1db954?text=BTS+Song+1', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(161, 117, 17, 'BLACKPINK Song 10', '', 'https://placehold.co/300x300/181818/1db954?text=BLACKPINK+Song+10', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(162, 117, 17, 'BLACKPINK Song 9', '', 'https://placehold.co/300x300/181818/1db954?text=BLACKPINK+Song+9', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(163, 117, 17, 'BLACKPINK Song 8', '', 'https://placehold.co/300x300/181818/1db954?text=BLACKPINK+Song+8', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(164, 117, 17, 'BLACKPINK Song 7', '', 'https://placehold.co/300x300/181818/1db954?text=BLACKPINK+Song+7', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(165, 117, 17, 'BLACKPINK Song 6', '', 'https://placehold.co/300x300/181818/1db954?text=BLACKPINK+Song+6', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(166, 117, 17, 'BLACKPINK Song 5', '', 'https://placehold.co/300x300/181818/1db954?text=BLACKPINK+Song+5', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(167, 117, 17, 'BLACKPINK Song 4', '', 'https://placehold.co/300x300/181818/1db954?text=BLACKPINK+Song+4', 0, 'Pop', NULL, 1, '2025-11-26 00:44:36', '2025-11-26 16:15:02'),
(168, 117, 17, 'BLACKPINK Song 3', '', 'https://placehold.co/300x300/181818/1db954?text=BLACKPINK+Song+3', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(169, 117, 17, 'BLACKPINK Song 2', '', 'https://placehold.co/300x300/181818/1db954?text=BLACKPINK+Song+2', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(170, 117, 17, 'BLACKPINK Song 1', '', 'https://placehold.co/300x300/181818/1db954?text=BLACKPINK+Song+1', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(171, 118, 18, 'Eminem Song 10', '', 'https://placehold.co/300x300/181818/1db954?text=Eminem+Song+10', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(172, 118, 18, 'Eminem Song 9', '', 'https://placehold.co/300x300/181818/1db954?text=Eminem+Song+9', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(173, 118, 18, 'Eminem Song 8', '', 'https://placehold.co/300x300/181818/1db954?text=Eminem+Song+8', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(174, 118, 18, 'Eminem Song 7', '', 'https://placehold.co/300x300/181818/1db954?text=Eminem+Song+7', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(175, 118, 18, 'Eminem Song 6', '', 'https://placehold.co/300x300/181818/1db954?text=Eminem+Song+6', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(176, 118, 18, 'Eminem Song 5', '', 'https://placehold.co/300x300/181818/1db954?text=Eminem+Song+5', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(177, 118, 18, 'Eminem Song 4', '', 'https://placehold.co/300x300/181818/1db954?text=Eminem+Song+4', 0, 'Pop', NULL, 1, '2025-11-26 00:44:36', '2025-11-26 16:15:05'),
(178, 118, 18, 'Eminem Song 3', '', 'https://placehold.co/300x300/181818/1db954?text=Eminem+Song+3', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(179, 118, 18, 'Eminem Song 2', '', 'https://placehold.co/300x300/181818/1db954?text=Eminem+Song+2', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(180, 118, 18, 'Eminem Song 1', '', 'https://placehold.co/300x300/181818/1db954?text=Eminem+Song+1', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(181, 119, 19, 'The Weeknd Song 10', '', 'https://placehold.co/300x300/181818/1db954?text=The+Weeknd+Song+10', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(182, 119, 19, 'The Weeknd Song 9', '', 'https://placehold.co/300x300/181818/1db954?text=The+Weeknd+Song+9', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(183, 119, 19, 'The Weeknd Song 8', '', 'https://placehold.co/300x300/181818/1db954?text=The+Weeknd+Song+8', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(184, 119, 19, 'The Weeknd Song 7', '', 'https://placehold.co/300x300/181818/1db954?text=The+Weeknd+Song+7', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(185, 119, 19, 'The Weeknd Song 6', '', 'https://placehold.co/300x300/181818/1db954?text=The+Weeknd+Song+6', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(186, 119, 19, 'The Weeknd Song 5', '', 'https://placehold.co/300x300/181818/1db954?text=The+Weeknd+Song+5', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(187, 119, 19, 'The Weeknd Song 4', '', 'https://placehold.co/300x300/181818/1db954?text=The+Weeknd+Song+4', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(188, 119, 19, 'The Weeknd Song 3', '', 'https://placehold.co/300x300/181818/1db954?text=The+Weeknd+Song+3', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(189, 119, 19, 'The Weeknd Song 2', '', 'https://placehold.co/300x300/181818/1db954?text=The+Weeknd+Song+2', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(190, 119, 19, 'The Weeknd Song 1', '', 'https://placehold.co/300x300/181818/1db954?text=The+Weeknd+Song+1', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(191, 120, 20, 'Adele Song 10', '', 'https://placehold.co/300x300/181818/1db954?text=Adele+Song+10', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(192, 120, 20, 'Adele Song 9', '', 'https://placehold.co/300x300/181818/1db954?text=Adele+Song+9', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(193, 120, 20, 'Adele Song 8', '', 'https://placehold.co/300x300/181818/1db954?text=Adele+Song+8', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(194, 120, 20, 'Adele Song 7', '', 'https://placehold.co/300x300/181818/1db954?text=Adele+Song+7', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(195, 120, 20, 'Adele Song 6', '', 'https://placehold.co/300x300/181818/1db954?text=Adele+Song+6', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(196, 120, 20, 'Adele Song 5', '', 'https://placehold.co/300x300/181818/1db954?text=Adele+Song+5', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(197, 120, 20, 'Adele Song 4', '', 'https://placehold.co/300x300/181818/1db954?text=Adele+Song+4', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(198, 120, 20, 'Adele Song 3', '', 'https://placehold.co/300x300/181818/1db954?text=Adele+Song+3', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(199, 120, 20, 'Adele Song 2', '', 'https://placehold.co/300x300/181818/1db954?text=Adele+Song+2', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(200, 120, 20, 'Adele Song 1', '', 'https://placehold.co/300x300/181818/1db954?text=Adele+Song+1', 0, 'Pop', NULL, 0, '2025-11-26 00:44:36', '2025-11-26 13:09:22'),
(201, 1, 1, '自动化测试单曲', 'http://music.163.com/song/media/outer/url?id=123.mp3', 'https://placehold.co/300x300/181818/1db954?text=自动化测试单曲', 210, 'Jazz', NULL, 0, '2025-11-26 03:12:35', '2025-11-26 13:09:22'),
(202, 1, 1, '自动化测试单曲', 'http://music.163.com/song/media/outer/url?id=123.mp3', 'https://placehold.co/300x300/181818/1db954?text=自动化测试单曲', 210, 'Jazz', NULL, 0, '2025-11-26 03:12:57', '2025-11-26 13:09:22'),
(203, 1, 1, '自动化测试单曲', 'http://music.163.com/song/media/outer/url?id=123.mp3', 'https://placehold.co/300x300/181818/1db954?text=自动化测试单曲', 210, 'Jazz', NULL, 0, '2025-11-26 03:13:50', '2025-11-26 13:09:22');


-- ==========================================
-- 9. 其他空表 (TRUNCATE 清空/保留结构)
-- ==========================================
TRUNCATE TABLE user_interaction;
TRUNCATE TABLE music_playlist_song;
TRUNCATE TABLE music_playlist;
TRUNCATE TABLE song_comment;
TRUNCATE TABLE user_follow;


SET FOREIGN_KEY_CHECKS = 1;

-- 脚本结束