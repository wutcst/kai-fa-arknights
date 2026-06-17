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
    player_inventory JSON,
    player_weight INT DEFAULT 0,
    player_max_weight INT DEFAULT 5,
    player_grid_row INT DEFAULT 4,
    player_grid_col INT DEFAULT 4,
    room_history JSON,
    room_items JSON,
    room_item_positions JSON,
    saved_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 创建能力配置表 - 定义各种能力及其升级成本
CREATE TABLE IF NOT EXISTS ability_config (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ability_code VARCHAR(30) NOT NULL UNIQUE,
    ability_name VARCHAR(50) NOT NULL,
    base_value INT NOT NULL DEFAULT 0,
    increment_per_level INT NOT NULL DEFAULT 1,
    base_cost INT NOT NULL DEFAULT 10,
    cost_multiplier DECIMAL(3,1) NOT NULL DEFAULT 1.5,
    max_level INT NOT NULL DEFAULT 10,
    description VARCHAR(200),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入默认能力配置
INSERT INTO ability_config (ability_code, ability_name, base_value, increment_per_level, base_cost, cost_multiplier, max_level, description) VALUES
('max_weight', '负重上限', 5, 3, 50, 1.8, 10, '背包最大负重，每级增加3'),
('gold_bonus', '龙门币加成', 0, 5, 80, 2.0, 10, '结算时额外获得龙门币百分比，每级增加5%'),
('move_speed', '移动速度', 10, 2, 100, 2.2, 5, '房间内移动速度，每级增加2')
ON DUPLICATE KEY UPDATE ability_name = ability_name;

-- 创建用户能力表 - 存储用户各能力等级和金币
CREATE TABLE IF NOT EXISTS user_abilities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    gold INT NOT NULL DEFAULT 0,
    max_weight_level INT NOT NULL DEFAULT 1,
    gold_bonus_level INT NOT NULL DEFAULT 1,
    move_speed_level INT NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
