import http from './http';

const GAME_BASE = '/game';

export function takeItem(itemId) {
  return http.post(`${GAME_BASE}/take`, { itemId });
}

export function dropItem(itemId) {
  return http.post(`${GAME_BASE}/drop`, { itemId });
}

export function getItems() {
  return http.get(`${GAME_BASE}/items`);
}

export function eatCookie() {
  return http.post(`${GAME_BASE}/eatcookie`);
}
