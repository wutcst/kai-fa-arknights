import http from './http';

const SAVE_BASE = '/save';

export function checkSave(username) {
  return http.get(`${SAVE_BASE}/hasSave`, { params: { username } });
}

export function saveGame(username, playerGridPosition = null) {
  return http.post(`${SAVE_BASE}/save`, {
    username,
    playerGridRow: playerGridPosition?.row,
    playerGridCol: playerGridPosition?.col
  });
}

export function loadGame(username) {
  return http.post(`${SAVE_BASE}/load`, { username });
}

export function newGame(username) {
  return http.post(`${SAVE_BASE}/newGame`, { username });
}

export function settleExploration(username) {
  return http.post(`${SAVE_BASE}/settle`, { username });
}
