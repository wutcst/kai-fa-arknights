-- MySQL 数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS arknights_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE arknights_db;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(20) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入测试用户（密码都是 123456）
INSERT INTO users (username, password) VALUES
    ('player1', '123456'),
    ('player2', '123456'),
    ('admin', 'admin123')
ON DUPLICATE KEY UPDATE username = username;

-- 创建游戏存档表
CREATE TABLE IF NOT EXISTS game_saves (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    current_room_id VARCHAR(50) NOT NULL,
    player_inventory JSON,           -- 背包物品序列化
    player_weight INT DEFAULT 0,
    player_max_weight INT DEFAULT 20,
    room_history JSON,               -- 移动历史序列化
    room_items JSON,                 -- 房间物品状态序列化
    saved_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
