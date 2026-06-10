/**
 * 游戏 API 服务层.
 * 封装与后端交互的接口.
 */
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/game';
const SAVE_API_BASE_URL = 'http://localhost:8080/api/save';

// 获取游戏状态
export function getGameStatus() {
  return axios.get(`${API_BASE_URL}/status`);
}

// 获取地图数据
export function getMap() {
  return axios.get(`${API_BASE_URL}/map`);
}

// 移动角色
export function move(direction) {
  return axios.post(`${API_BASE_URL}/move`, { direction });
}

// 获取帮助信息
export function getHelp() {
  return axios.get(`${API_BASE_URL}/help`);
}

// 查看当前房间信息（look命令）
export function look() {
  return axios.get(`${API_BASE_URL}/look`);
}

// 返回上一个房间（back命令）
export function goBack() {
  return axios.post(`${API_BASE_URL}/back`);
}

// 拾取物品
export function takeItem(itemId) {
  return axios.post(`${API_BASE_URL}/take`, { itemId });
}

// 丢弃物品
export function dropItem(itemId) {
  return axios.post(`${API_BASE_URL}/drop`, { itemId });
}

// 查看所有物品
export function getItems() {
  return axios.get(`${API_BASE_URL}/items`);
}

// 吃魔法饼干
export function eatCookie() {
  return axios.post(`${API_BASE_URL}/eatcookie`);
}

// 检查存档是否存在
export function checkSave(username) {
  return axios.get(`${SAVE_API_BASE_URL}/hasSave`, { params: { username } });
}

// 保存游戏
export function saveGame(username) {
  return axios.post(`${SAVE_API_BASE_URL}/save`, { username });
}

// 加载游戏
export function loadGame(username) {
  return axios.post(`${SAVE_API_BASE_URL}/load`, { username });
}

// 开始新游戏
export function newGame(username) {
  return axios.post(`${SAVE_API_BASE_URL}/newGame`, { username });
}

// 探索结算
export function settleExploration(username) {
  return axios.post(`${SAVE_API_BASE_URL}/settle`, { username });
}

// 能力相关API
const ABILITY_API_BASE_URL = 'http://localhost:8080/api/ability';

// 获取用户能力信息
export function getUserAbility(userId) {
  return axios.get(`${ABILITY_API_BASE_URL}/user/${userId}`);
}

// 获取所有能力配置
export function getAbilityConfigs() {
  return axios.get(`${ABILITY_API_BASE_URL}/config`);
}

// 升级能力
export function upgradeAbility(userId, abilityCode) {
  return axios.post(`${ABILITY_API_BASE_URL}/upgrade`, null, {
    params: { userId, abilityCode }
  });
}

// 获取用户属性数值
export function getUserStats(userId) {
  return axios.get(`${ABILITY_API_BASE_URL}/stats/${userId}`);
}
