/**
 * 统一 HTTP 客户端。
 * Vue CLI 环境下使用 VUE_APP_API_BASE_URL 配置后端 API 根路径。
 */
import axios from 'axios';

const API_BASE_URL = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080/api';

const http = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000
});

export default http;
export { API_BASE_URL };
