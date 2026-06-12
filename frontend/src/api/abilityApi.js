import http from './http';

const ABILITY_BASE = '/ability';

export function getUserAbility(userId) {
  return http.get(`${ABILITY_BASE}/user/${userId}`);
}

export function getAbilityConfigs() {
  return http.get(`${ABILITY_BASE}/config`);
}

export function upgradeAbility(userId, abilityCode) {
  return http.post(`${ABILITY_BASE}/upgrade`, null, {
    params: { userId, abilityCode }
  });
}

export function getUserStats(userId) {
  return http.get(`${ABILITY_BASE}/stats/${userId}`);
}
