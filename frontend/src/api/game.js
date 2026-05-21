import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/game';

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
