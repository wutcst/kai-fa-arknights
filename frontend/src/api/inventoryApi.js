import http from './http';

const GAME_BASE = '/game';

export function takeItem(itemId, playerGridPosition) {
  return http.post(`${GAME_BASE}/take`, {
    itemId,
    playerGridRow: playerGridPosition?.row,
    playerGridCol: playerGridPosition?.col
  });
}

export function dropItem(itemId, playerGridPosition) {
  return http.post(`${GAME_BASE}/drop`, {
    itemId,
    playerGridRow: playerGridPosition?.row,
    playerGridCol: playerGridPosition?.col
  });
}

export function getItems() {
  return http.get(`${GAME_BASE}/items`);
}

export function eatCookie() {
  return http.post(`${GAME_BASE}/eatcookie`);
}
