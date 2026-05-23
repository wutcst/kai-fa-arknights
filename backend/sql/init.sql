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
