/**
 * 认证 API 服务层.
 */
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/auth';

// 注册
export function register(username, password) {
  return axios.post(`${API_BASE_URL}/register`, { username, password });
}

// 登录
export function login(username, password) {
  return axios.post(`${API_BASE_URL}/login`, { username, password });
}

// 修改密码
export function changePassword(username, oldPassword, newPassword) {
  return axios.post(`${API_BASE_URL}/changePassword`, { username, oldPassword, newPassword });
}

// 检查用户名是否存在
export function checkUsername(username) {
  return axios.get(`${API_BASE_URL}/checkUsername`, { params: { username } });
}

// 根据用户名获取用户信息
export function getUserByUsername(username) {
  return axios.get(`${API_BASE_URL}/user`, { params: { username } });
}
