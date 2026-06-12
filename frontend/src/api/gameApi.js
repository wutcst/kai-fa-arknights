import http from './http';

const GAME_BASE = '/game';

export function getGameStatus() {
  return http.get(`${GAME_BASE}/status`);
}

export function getMap() {
  return http.get(`${GAME_BASE}/map`);
}

export function move(direction) {
  return http.post(`${GAME_BASE}/move`, { direction });
}

export function getHelp() {
  return http.get(`${GAME_BASE}/help`);
}

export function look() {
  return http.get(`${GAME_BASE}/look`);
}

export function goBack() {
  return http.post(`${GAME_BASE}/back`);
}
