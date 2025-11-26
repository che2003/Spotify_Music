/*
 * 最终完整的建表脚本：Spotify Music Recommendation System (MySQL)
 * 根据用户提供的 TXT 文件内容校对，仅包含所有 10 张表结构定义 (DDL)。
 * 注意：所有 CREATE TABLE 语句均基于原始 create_music.sql 结构，并确保列名与 TXT 文件匹配。
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

-- 用户表 (sys_user) - 字段匹配 sys_user.txt
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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 角色表 (sys_role) - 字段匹配 sys_role.txt
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `role_key` VARCHAR(20) NOT NULL COMMENT '角色字符: admin, user, musician',
  `role_name` VARCHAR(20) NOT NULL COMMENT '角色名称',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_key` (`role_key`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='角色定义表';

-- 用户-角色关联表 (sys_user_role) - 字段匹配 sys_user_role.txt
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 用户关注表 (user_follow) - 字段匹配 user_follow.txt (假设有 id, user_id, followed_user_id, create_time)
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

-- 歌手/音乐人信息表 (music_artist) - 字段匹配 music_artist.txt
DROP TABLE IF EXISTS `music_artist`;
CREATE TABLE `music_artist` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT DEFAULT NULL COMMENT '关联的用户ID',
  `name` VARCHAR(100) NOT NULL COMMENT '艺名/乐队名',
  `bio` TEXT COMMENT '简介',
  `avatar_url` VARCHAR(255) DEFAULT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='歌手表';

-- 专辑表 (music_album) - 字段匹配 music_album.txt
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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='专辑表';

-- 歌曲表 (music_song) - 字段匹配 music_song.txt
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
  `lyrics` TEXT,
  `play_count` BIGINT DEFAULT 0 COMMENT '总播放量',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_title` (`title`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='歌曲表';


-- ----------------------------
-- 4. 歌单与交互模块
-- ----------------------------

-- 歌单表 (music_playlist) - 字段匹配 music_playlist.txt
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
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='用户歌单表';

-- 歌单-歌曲关联表 (music_playlist_song) - 字段匹配 music_playlist_song.txt
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

-- 用户交互行为表 (user_interaction) - 字段匹配 user_interaction.txt
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

-- 评论表 (song_comment) - 字段匹配 song_comment.txt
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


-- ----------------------------
-- 5. 插入必要的数据 (仅插入角色定义)
-- ----------------------------

-- 插入角色定义 (ROLE_ADMIN, ROLE_USER, ROLE_MUSICIAN) - 确保 ID 和 Key 存在
INSERT INTO `sys_role` (`id`, `role_key`, `role_name`, `create_time`, `update_time`) VALUES 
(1, 'admin', '管理员', '2025-11-26 00:44:36', '2025-11-26 00:44:36'),
(2, 'user', '普通用户', '2025-11-26 00:44:36', '2025-11-26 00:44:36'),
(3, 'musician', '音乐人', '2025-11-26 00:44:36', '2025-11-26 00:44:36')
ON DUPLICATE KEY UPDATE role_name=VALUES(role_name);

SET FOREIGN_KEY_CHECKS = 1;

-- 脚本结束