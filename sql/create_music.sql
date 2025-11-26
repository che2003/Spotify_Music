/*
 * 最终完整的建表脚本：Spotify Music Recommendation System (MySQL)
 * 包含所有核心表结构、以及 RBAC 测试所需的数据。
 * * ========================================
 * 【更新说明】:
 * 1. 扩展 music_song 支持歌词。
 * 2. 扩展 music_artist 支持数据统计 (total_fans, total_plays)。
 * 3. 新增 6 张表：play_history, music_genre, music_song_genre, 
 * user_artist_like, user_album_like, sys_banner。
 * ========================================
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 创建数据库
-- ----------------------------
CREATE DATABASE IF NOT EXISTS `spotify_music` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `spotify_music`;

-- ----------------------------
-- 2. 核心用户与权限模块
-- ----------------------------

-- 用户表 (sys_user)
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '加密后的密码',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除(0:正常,1:删除)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 角色表 (sys_role)
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_key` VARCHAR(20) NOT NULL COMMENT '角色字符: admin, user, musician',
  `role_name` VARCHAR(20) NOT NULL COMMENT '角色名称',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_key` (`role_key`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COMMENT='角色定义表';

-- 用户-角色关联表 (sys_user_role)
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 用户关注表 (user_follow) - 支持粉丝关注艺人/用户 (B2)
DROP TABLE IF EXISTS `user_follow`;
CREATE TABLE `user_follow` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '关注方用户ID',
  `followed_user_id` BIGINT NOT NULL COMMENT '被关注的用户ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_follow` (`user_id`,`followed_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注关系表';


-- ----------------------------
-- 3. 音乐核心资源模块
-- ----------------------------

-- 歌手/音乐人信息表 (music_artist) - 【已修改】新增统计字段 (C1)
DROP TABLE IF EXISTS `music_artist`;
CREATE TABLE `music_artist` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT DEFAULT NULL COMMENT '关联的用户ID',
  `name` VARCHAR(100) NOT NULL COMMENT '艺名/乐队名',
  `bio` TEXT COMMENT '简介',
  `avatar_url` VARCHAR(255) DEFAULT NULL,
  `total_fans` INT DEFAULT 0 COMMENT '粉丝总数', /* 新增字段 (C1) */
  `total_plays` BIGINT DEFAULT 0 COMMENT '所有作品总播放量', /* 新增字段 (C1) */
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COMMENT='歌手表';

-- 专辑表 (music_album)
DROP TABLE IF EXISTS `music_album`;
CREATE TABLE `music_album` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `artist_id` BIGINT NOT NULL COMMENT '关联歌手ID',
  `title` VARCHAR(100) NOT NULL COMMENT '专辑名',
  `cover_url` VARCHAR(255) DEFAULT NULL COMMENT '封面图URL',
  `description` TEXT COMMENT '专辑介绍',
  `release_date` DATE DEFAULT NULL COMMENT '发行日期',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='专辑表';

-- 歌曲表 (music_song) - 【已修改】更新歌词字段注释 (A1)
DROP TABLE IF EXISTS `music_song`;
CREATE TABLE `music_song` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `album_id` BIGINT NOT NULL COMMENT '所属专辑ID',
  `artist_id` BIGINT NOT NULL COMMENT '所属歌手ID',
  `title` VARCHAR(100) NOT NULL COMMENT '歌名',
  `file_url` VARCHAR(255) NOT NULL COMMENT '音乐文件链接',
  `cover_url` VARCHAR(255) DEFAULT NULL,
  `duration` INT DEFAULT 0 COMMENT '时长(秒)',
  `genre` VARCHAR(50) DEFAULT NULL,
  `lyrics` TEXT COMMENT '歌词内容', /* 更新注释 (A1) */
  `play_count` BIGINT DEFAULT 0 COMMENT '总播放量',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_title` (`title`)
) ENGINE=InnoDB AUTO_INCREMENT=201 DEFAULT CHARSET=utf8mb4 COMMENT='歌曲表';

-- 【新增】音乐流派字典表 (music_genre) - (A4)
DROP TABLE IF EXISTS `music_genre`;
CREATE TABLE `music_genre` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL UNIQUE COMMENT '流派名称',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='音乐流派字典表';

-- 【新增】歌曲-流派关联表 (music_song_genre) - (A4)
DROP TABLE IF EXISTS `music_song_genre`;
CREATE TABLE `music_song_genre` (
  `song_id` BIGINT NOT NULL COMMENT '歌曲ID',
  `genre_id` INT NOT NULL COMMENT '流派ID',
  PRIMARY KEY (`song_id`, `genre_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌曲-流派关联表(多对多)';

-- ----------------------------
-- 4. 歌单与交互模块
-- ----------------------------

-- 歌单表 (music_playlist)
DROP TABLE IF EXISTS `music_playlist`;
CREATE TABLE `music_playlist` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `creator_id` BIGINT NOT NULL COMMENT '创建者ID',
  `title` VARCHAR(100) NOT NULL COMMENT '歌单名',
  `cover_url` VARCHAR(255) DEFAULT NULL,
  `description` VARCHAR(500) DEFAULT NULL,
  `is_public` TINYINT(1) DEFAULT 1 COMMENT '是否公开',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COMMENT='用户歌单表';

-- 歌单-歌曲关联表 (music_playlist_song)
DROP TABLE IF EXISTS `music_playlist_song`;
CREATE TABLE `music_playlist_song` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `playlist_id` BIGINT NOT NULL,
  `song_id` BIGINT NOT NULL,
  `sort_order` INT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_playlist_song` (`playlist_id`, `song_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌单歌曲关联表';

-- 用户交互行为表 (user_interaction)
DROP TABLE IF EXISTS `user_interaction`;
CREATE TABLE `user_interaction` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `song_id` BIGINT NOT NULL,
  `type` TINYINT NOT NULL COMMENT '交互类型: 1=播放, 3=喜欢, 4=跳过',
  `rating` DECIMAL(2,1) DEFAULT NULL COMMENT '评分(1.0-5.0)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user_song` (`user_id`, `song_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户交互日志(NCF数据源)';

-- 评论表 (song_comment)
DROP TABLE IF EXISTS `song_comment`;
CREATE TABLE `song_comment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `song_id` BIGINT NOT NULL,
  `content` VARCHAR(500) NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌曲评论表';

-- 【新增】用户播放历史记录表 (play_history) - (A2)
DROP TABLE IF EXISTS `play_history`;
CREATE TABLE `play_history` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `song_id` BIGINT NOT NULL COMMENT '歌曲ID',
  `play_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '播放时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_time` (`user_id`, `play_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户播放历史记录表';

-- 【新增】用户收藏艺人关系表 (user_artist_like) - (B1)
DROP TABLE IF EXISTS `user_artist_like`;
CREATE TABLE `user_artist_like` (
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `artist_id` BIGINT NOT NULL COMMENT '艺人ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`, `artist_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏艺人关系表';

-- 【新增】用户收藏专辑关系表 (user_album_like) - (B1)
DROP TABLE IF EXISTS `user_album_like`;
CREATE TABLE `user_album_like` (
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `album_id` BIGINT NOT NULL COMMENT '专辑ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`, `album_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏专辑关系表';


-- ----------------------------
-- 5. 运营配置模块
-- ----------------------------

-- 【新增】系统 Banner/公告配置表 (sys_banner) - (D3)
DROP TABLE IF EXISTS `sys_banner`;
CREATE TABLE `sys_banner` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(255) NOT NULL COMMENT '标题',
  `image_url` VARCHAR(255) NOT NULL COMMENT '图片URL',
  `target_url` VARCHAR(255) DEFAULT NULL COMMENT '点击跳转链接',
  `sort_order` INT DEFAULT 0 COMMENT '排序权重',
  `is_enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统Banner/公告配置表';


-- ----------------------------
-- 6. 插入必要的数据 (RBAC & 测试数据)
-- ----------------------------

-- BCrypt 密码 '123456' 的哈希值 (用于登录测试)
SET @pwd_hash = '$2a$10$9F0Tvh4TgxYGjXt9NqlfAO1Dm89pwHbG36cGBydXoTVKOuKCHqwxK';

-- 插入角色定义
INSERT INTO `sys_role` (`id`, `role_key`, `role_name`) VALUES 
(1, 'admin', '管理员'),
(2, 'user', '普通用户'),
(3, 'musician', '音乐人');

-- 插入三个测试用户 (ID 1, 3, 4 对应之前的测试)
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `email`) VALUES 
(1, 'music_lover_001', @pwd_hash, '管理员账号', 'admin@spotify.com'),
(3, 'jay_chou', @pwd_hash, '周杰伦', 'jay@spotify.com'),
(4, 'poor_user', @pwd_hash, '普通用户', 'poor@spotify.com');


-- 赋予角色 (RBAC 测试所需)
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1), -- ID 1 (music_lover_001) 是 Admin
(3, 3), -- ID 3 (jay_chou) 是 Musician
(4, 2); -- ID 4 (poor_user) 是 User


-- 插入艺人 (与用户关联) - 【已修改】新增 total_fans 和 total_plays 字段 (C1)
INSERT INTO `music_artist` (`id`, `user_id`, `name`, `bio`, `total_fans`, `total_plays`) VALUES 
(1, 3, 'J.Chou Official', 'Musician User Profile (User ID 3)', 12000, 500000), 
(2, 1, 'Admin Artist', 'Used for deletion test (User ID 1)', 10, 100);

-- 插入专辑 (方便关联歌曲)
INSERT INTO `music_album` (`id`, `artist_id`, `title`) VALUES 
(1, 1, '测试专辑 001');

-- 插入歌曲 (用于测试权限和列表) - 【已修改】新增 lyrics 歌词 (A1)
INSERT INTO `music_song` (`id`, `album_id`, `artist_id`, `title`, `file_url`, `cover_url`, `genre`, `lyrics`, `play_count`) VALUES 
(100, 1, 1, '音乐人的作品', 'http://xxx.mp3', 'https://p1.music.126.net/cover1.jpg', 'Pop', '[00:00.00] 歌词测试', 10000), -- Musician's own song
(200, 1, 2, '别人的作品', 'http://xxx.mp3', 'https://p1.music.126.net/cover2.jpg', 'Rock', '[00:00.00] 歌词测试', 50); -- Other's song (Admin's)


SET FOREIGN_KEY_CHECKS = 1;

-- 脚本结束