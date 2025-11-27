/*
 * Spotify Music - 精简且结构化的测试数据 (与 music_load 目录联动)
 * 包含基础 RBAC、核心艺人/专辑/歌曲、歌单与播放队列数据。
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
USE `spotify_music`;

-- 1. 清理现有数据
TRUNCATE TABLE user_interaction;
TRUNCATE TABLE music_playlist_song;
TRUNCATE TABLE music_playlist;
TRUNCATE TABLE playlist_tag_rel;
TRUNCATE TABLE playlist_tag;
TRUNCATE TABLE song_comment;
TRUNCATE TABLE user_follow;
TRUNCATE TABLE play_history;
TRUNCATE TABLE play_queue_item;
TRUNCATE TABLE play_queue;
TRUNCATE TABLE music_song_genre;
TRUNCATE TABLE music_genre;
TRUNCATE TABLE user_artist_like;
TRUNCATE TABLE user_album_like;
TRUNCATE TABLE sys_banner;
TRUNCATE TABLE music_song;
TRUNCATE TABLE music_album;
TRUNCATE TABLE music_artist;
TRUNCATE TABLE sys_user_role;
TRUNCATE TABLE sys_user;

-- 2. 基础用户与角色
SET @pwd_hash = '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK';
SET @asset_prefix = 'music_load/music_information';

INSERT INTO `sys_role` (`id`, `role_key`, `role_name`) VALUES
(1, 'admin', '管理员'),
(2, 'user', '普通用户'),
(3, 'musician', '音乐人');

INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `email`) VALUES
(1, 'admin_root', @pwd_hash, '系统管理员', 'admin@spotify.com'),
(2, 'editor_amy', @pwd_hash, '内容运营', 'editor@spotify.com'),
(3, 'asoul_official', @pwd_hash, 'A-SOUL 官方', 'asoul@spotify.com'),
(4, 'radwimps_band', @pwd_hash, 'RADWIMPS', 'radwimps@spotify.com'),
(5, 'demo_listener', @pwd_hash, '示例听众', 'listener@spotify.com'),
(6, 'cantonese_fan', @pwd_hash, '粤语收藏家', 'fan@spotify.com');

INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 3),
(5, 2),
(6, 2);

INSERT INTO `user_follow` (`user_id`, `followed_user_id`) VALUES
(5, 3),
(5, 4),
(6, 7);

-- 3. 艺人、专辑、歌曲
INSERT INTO `music_artist` (`id`, `user_id`, `name`, `bio`, `avatar_url`, `total_fans`, `total_plays`) VALUES
(1, 3, 'A-SOUL', '虚拟偶像女团，以轻快流行曲风著称。', CONCAT(@asset_prefix, '/所有封面(Covers)/A-SOUL - Quiet.jpg'), 520000, 18000000),
(2, NULL, 'Gracie Abrams', '美国创作型歌手，代表作《21》。', CONCAT(@asset_prefix, '/所有封面(Covers)/Gracie Abrams - 21.jpg'), 150000, 12000000),
(3, NULL, 'Justin Bieber', '加拿大小天王，合作金曲《Peaches》。', CONCAT(@asset_prefix, '/所有封面(Covers)/Justin Bieber&Daniel Caesar&GIVĒON - Peaches (Explicit).jpg'), 3000000, 250000000),
(4, 4, 'RADWIMPS', '日本摇滚乐队，电影配乐常客。', CONCAT(@asset_prefix, '/所有封面(Covers)/RADWIMPS (ラッドウィンプス) - 愛にできることはまだあるかい (爱能做到的还有什么).jpg'), 2200000, 320000000),
(5, NULL, 'Rosa Walton & Hallie Coggins', '《赛博朋克2077》原声曲《I Really Want to Stay at Your House》。', CONCAT(@asset_prefix, '/所有封面(Covers)/Rosa Walton&Hallie Coggins - I Really Want to Stay at Your House.jpg'), 800000, 88000000),
(6, NULL, 'The Kid LAROI', '澳洲创作歌手，《STAY》主唱。', CONCAT(@asset_prefix, '/所有封面(Covers)/The Kid LAROI&Justin Bieber - STAY (Explicit).jpg'), 1200000, 140000000),
(7, NULL, 'Twins', '香港流行女子二人组合。', CONCAT(@asset_prefix, '/所有封面(Covers)/Twins - 下一站天后.jpg'), 900000, 75000000),
(8, NULL, '刘若英', '华语流行女歌手，温暖治愈。', NULL, 600000, 64000000),
(9, NULL, '卢巧音', '香港流行女歌手。', CONCAT(@asset_prefix, '/所有封面(Covers)/卢巧音 - 好心分手.jpg'), 300000, 30000000);

INSERT INTO `music_album` (`id`, `artist_id`, `title`, `cover_url`, `release_date`) VALUES
(1, 1, 'Quiet (Single)', CONCAT(@asset_prefix, '/所有封面(Covers)/A-SOUL - Quiet.jpg'), '2021-12-10'),
(2, 2, 'This Is What It Feels Like', CONCAT(@asset_prefix, '/所有封面(Covers)/Gracie Abrams - 21.jpg'), '2021-11-12'),
(3, 3, 'Justice', CONCAT(@asset_prefix, '/所有封面(Covers)/Justin Bieber&Daniel Caesar&GIVĒON - Peaches (Explicit).jpg'), '2021-03-19'),
(4, 6, 'F*CK LOVE 3: OVER YOU', CONCAT(@asset_prefix, '/所有封面(Covers)/The Kid LAROI&Justin Bieber - STAY (Explicit).jpg'), '2021-07-09'),
(5, 4, 'Weathering With You OST', CONCAT(@asset_prefix, '/所有封面(Covers)/RADWIMPS (ラッドウィンプス) - 愛にできることはまだあるかい (爱能做到的还有什么).jpg'), '2019-07-19'),
(6, 5, 'Cyberpunk 2077: Radio, Vol.2', CONCAT(@asset_prefix, '/所有封面(Covers)/Rosa Walton&Hallie Coggins - I Really Want to Stay at Your House.jpg'), '2020-12-10'),
(7, 4, 'Weathering With You OST (Movie Edit)', CONCAT(@asset_prefix, '/所有封面(Covers)/三浦透子 (みうら とうこ)&RADWIMPS (ラッドウィンプス) - グランドエスケープ (逃离地面) (Movie edit).jpg'), '2019-07-19'),
(8, 7, '下一站天后', CONCAT(@asset_prefix, '/所有封面(Covers)/Twins - 下一站天后.jpg'), '2003-06-13'),
(9, 9, '好心分手 (Single)', CONCAT(@asset_prefix, '/所有封面(Covers)/卢巧音 - 好心分手.jpg'), '2004-01-01');

INSERT INTO `music_song` (`id`, `album_id`, `artist_id`, `title`, `file_url`, `cover_url`, `duration`, `genre`, `lyrics`, `description`, `play_count`) VALUES
(1, 1, 1, 'Quiet', CONCAT(@asset_prefix, '/所有歌曲(MP3)/A-SOUL - Quiet.mp3'), CONCAT(@asset_prefix, '/所有封面(Covers)/A-SOUL - Quiet.jpg'), 215, 'Pop', '[00:00.00] Quiet - A-SOUL\n[00:10.00] 和声轻轻进入，旋律渐暖。', 'A-SOUL 代表作，轻快电子流行搭配温暖人声。', 180000),
(2, 2, 2, '21', CONCAT(@asset_prefix, '/所有歌曲(MP3)/Gracie Abrams - 21.mp3'), CONCAT(@asset_prefix, '/所有封面(Covers)/Gracie Abrams - 21.jpg'), 188, 'Indie Pop', '[00:00.00] 21 - Gracie Abrams\n[00:12.00] 回忆青春的呢喃。', '柔和木吉他铺底的 bedroom pop 作品。', 92000),
(3, 3, 3, 'Peaches (Explicit)', CONCAT(@asset_prefix, '/所有歌曲(MP3)/Justin Bieber&Daniel Caesar&GIVĒON - Peaches (Explicit).mp3'), CONCAT(@asset_prefix, '/所有封面(Covers)/Justin Bieber&Daniel Caesar&GIVĒON - Peaches (Explicit).jpg'), 198, 'R&B', '[00:00.00] Peaches\n[00:15.00] I get my peaches out in Georgia...', 'Justin Bieber 联合 Daniel Caesar、GIVĒON 的 R&B 热单。', 250000),
(4, 4, 6, 'STAY (Explicit)', CONCAT(@asset_prefix, '/所有歌曲(MP3)/The Kid LAROI&Justin Bieber - STAY (Explicit).mp3'), CONCAT(@asset_prefix, '/所有封面(Covers)/The Kid LAROI&Justin Bieber - STAY (Explicit).jpg'), 141, 'Pop', '[00:00.00] STAY\n[00:08.00] 活力四射的合成器开场。', '高能流行单曲，双人对唱刻画不安与挽留。', 310000),
(5, 5, 4, '愛にできることはまだあるかい', CONCAT(@asset_prefix, '/所有歌曲(MP3)/RADWIMPS (ラッドウィンプス) - 愛にできることはまだあるかい (爱能做到的还有什么).mp3'), CONCAT(@asset_prefix, '/所有封面(Covers)/RADWIMPS (ラッドウィンプス) - 愛にできることはまだあるかい (爱能做到的还有什么).jpg'), 394, 'Soundtrack', '[00:00.00] 愛にできることはまだあるかい\n[00:25.00] 钢琴与弦乐缓慢铺陈。', '《天气之子》主题曲，细腻描绘少年心境。', 175000),
(6, 6, 5, 'I Really Want to Stay at Your House', CONCAT(@asset_prefix, '/所有歌曲(MP3)/Rosa Walton&Hallie Coggins - I Really Want to Stay at Your House.mp3'), CONCAT(@asset_prefix, '/所有封面(Covers)/Rosa Walton&Hallie Coggins - I Really Want to Stay at Your House.jpg'), 265, 'Synth Pop', '[00:00.00] I really want to stay at your house...\n[00:30.00] 合成器气泡声进入。', '《赛博朋克2077》情感高潮时刻的插曲。', 460000),
(7, 7, 4, 'グランドエスケープ (Movie edit)', CONCAT(@asset_prefix, '/所有歌曲(MP3)/三浦透子 (みうら とうこ)&RADWIMPS (ラッドウィンプス) - グランドエスケープ (逃离地面) (Movie edit).mp3'), CONCAT(@asset_prefix, '/所有封面(Covers)/三浦透子 (みうら とうこ)&RADWIMPS (ラッドウィンプス) - グランドエスケープ (逃离地面) (Movie edit).jpg'), 293, 'Soundtrack', '[00:00.00] グランドエスケープ\n[00:18.00] 清澈女声与弦乐齐鸣。', '电影高潮段落的插曲，具有强烈的冒险感。', 132000),
(8, 8, 7, '下一站天后', CONCAT(@asset_prefix, '/所有歌曲(MP3)/Twins - 下一站天后.mp3'), CONCAT(@asset_prefix, '/所有封面(Covers)/Twins - 下一站天后.jpg'), 246, 'C-Pop', '[00:00.00] 下一站天后\n[00:12.00] 粤语流行的轻快旋律。', 'Twins 经典励志单曲，鼓励坚持舞台梦想。', 85000),
(9, 8, 8, '后来', CONCAT(@asset_prefix, '/所有歌曲(MP3)/刘若英 - 后来.mp3'), NULL, 280, 'C-Pop', '[00:00.00] 后来\n[00:20.00] 钢琴与弦乐交织。', '刘若英治愈情歌，描绘青春回忆。', 215000),
(10, 9, 9, '好心分手', CONCAT(@asset_prefix, '/所有歌曲(MP3)/卢巧音 - 好心分手.mp3'), CONCAT(@asset_prefix, '/所有封面(Covers)/卢巧音 - 好心分手.jpg'), 260, 'C-Pop', '[00:00.00] 好心分手\n[00:18.00] 粤语流行与摇滚混合的氛围。', '卢巧音代表作，洒脱面对感情分离。', 99000);

INSERT INTO `music_genre` (`id`, `name`) VALUES
(1, 'Pop'),
(2, 'Indie Pop'),
(3, 'R&B'),
(4, 'Soundtrack'),
(5, 'Synth Pop'),
(6, 'C-Pop');

INSERT INTO `music_song_genre` (`song_id`, `genre_id`) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 1),
(5, 4),
(6, 5),
(7, 4),
(8, 6),
(9, 6),
(10, 6);

-- 4. 歌单与互动数据
INSERT INTO `music_playlist` (`id`, `creator_id`, `title`, `description`, `visibility`, `is_collaborative`, `status`, `category`, `mood`, `background_color`, `follower_count`, `like_count`, `play_count`, `share_count`, `last_modified_by`)
VALUES
(1, 2, '数字专辑试听集', '来自 music_load 目录的示例歌曲，验证播放与封面路径。', 'public', 1, 1, 'Pop', 'Chill', '#CCE8FF', 320, 76, 12400, 12, 2),
(2, 5, '粤语回忆', '怀旧粤语作品合集', 'public', 0, 1, 'Cantonese', 'Nostalgic', '#FFDDEE', 58, 20, 3300, 3, 5);

INSERT INTO `playlist_tag` (`id`, `name`) VALUES
(1, '流行'),
(2, '协作'),
(3, '影视原声'),
(4, '粤语'),
(5, '励志');

INSERT INTO `playlist_tag_rel` (`playlist_id`, `tag_id`) VALUES
(1, 1),
(1, 2),
(1, 3),
(2, 4),
(2, 5);

INSERT INTO `music_playlist_song` (`playlist_id`, `song_id`, `position`, `added_by`, `status`, `version`) VALUES
(1, 1, 10, 2, 1, 1),
(1, 3, 20, 2, 1, 1),
(1, 6, 30, 2, 1, 1),
(2, 8, 10, 5, 1, 1),
(2, 10, 20, 5, 1, 1);

INSERT INTO `play_history` (`user_id`, `song_id`, `play_time`) VALUES
(5, 1, NOW()),
(5, 6, NOW() - INTERVAL 2 MINUTE),
(6, 8, NOW() - INTERVAL 10 MINUTE),
(6, 10, NOW() - INTERVAL 20 MINUTE);

INSERT INTO `play_queue` (`id`, `user_id`, `device_id`, `source_type`, `source_id`, `is_shuffle`, `repeat_mode`, `current_index`, `current_position_ms`, `version`)
VALUES
(1, 5, 'ios-demo-001', 'playlist', 1, 0, 'all', 0, 0, 1);

INSERT INTO `play_queue_item` (`queue_id`, `song_id`, `position`, `is_played`, `inserted_by`) VALUES
(1, 1, 10, 1, 5),
(1, 4, 20, 0, 5);

INSERT INTO `sys_banner` (`id`, `title`, `image_url`, `target_url`, `sort_order`, `is_enabled`) VALUES
(1, '年度播放榜单', CONCAT(@asset_prefix, '/所有封面(Covers)/A-SOUL - Quiet.jpg'), 'https://example.com/charts', 10, 1),
(2, '影视原声推荐', CONCAT(@asset_prefix, '/所有封面(Covers)/三浦透子 (みうら とうこ)&RADWIMPS (ラッドウィンプス) - グランドエスケープ (逃离地面) (Movie edit).jpg'), 'https://example.com/ost', 8, 1);

SET FOREIGN_KEY_CHECKS = 1;

