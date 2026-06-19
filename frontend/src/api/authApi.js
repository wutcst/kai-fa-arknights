import http from './http';

const AUTH_BASE = '/auth';

export function register(username, password) {
  return http.post(`${AUTH_BASE}/register`, { username, password });
}

export function login(username, password) {
  return http.post(`${AUTH_BASE}/login`, { username, password });
}

export function changePassword(username, oldPassword, newPassword) {
  return http.post(`${AUTH_BASE}/changePassword`, {
    username,
    oldPassword,
    newPassword
  });
}

export function checkUsername(username) {
  return http.get(`${AUTH_BASE}/checkUsername`, { params: { username } });
}

export function getUserByUsername(username) {
  return http.get(`${AUTH_BASE}/user`, { params: { username } });
}
