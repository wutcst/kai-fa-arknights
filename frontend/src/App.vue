<template>
  <div id="app" tabindex="0" ref="appContainer">
    <!-- 未登录显示登录界面 -->
    <Login v-if="!isLoggedIn" @login-success="handleLoginSuccess" />

    <!-- 已登录但未选择游戏显示开始界面 -->
    <GameStart
      v-else-if="showGameStart"
      :username="username"
      @start-game="handleStartGame"
      @continue-game="handleContinueGame"
      @logout="handleLogout"
    />

    <!-- 已登录且已选择游戏显示游戏界面 -->
    <template v-else>
    <GameView
      ref="roomView"
      :username="username"
      :user-gold="userGold"
      :room-name="currentRoomName"
      :room-id="currentRoomId"
      :player-grid-position="playerGridPosition"
      :move-speed="currentMoveSpeed"
      :description="longDescription"
      :exits="exits"
      :items="items"
      :inventory="inventory"
      :player-weight="playerWeight"
      :player-max-weight="playerMaxWeight"
      :selected-inventory-id="selectedInventoryId"
      :messages="messageLog"
      :is-error="isError"
      :busy="isMoving"
      @move="move"
      @look="look"
      @take="handleTake"
      @drop="handleDrop"
      @eat-cookie="handleEatCookie"
      @save="handleSave"
      @load="handleLoad"
      @back="goBack"
      @toggle-map="toggleMap"
      @help="getHelp"
      @open-ability="showAbilityPanel = true; fetchUserAbility()"
      @open-inventory-detail="openInventoryDetail"
      @settle="showSettleConfirm = true"
      @select-inventory="selectedInventoryId = $event"
      @player-position-change="playerGridPosition = $event"
      @back-to-menu="handleBackToMenu"
      @logout="handleLogout"
    />

    <InventoryDetailOverlay
      :show="showInventoryDetail"
      :inventory="inventory"
      :selected-id="selectedInventoryId"
      :player-weight="playerWeight"
      :player-max-weight="playerMaxWeight"
      :total-value="inventoryTotalValue"
      @close="closeInventoryDetail"
      @select="selectedInventoryId = $event"
      @drop="handleDrop"
      @eat-cookie="handleEatCookie"
    />

    <!-- 悬浮地图 -->
    <div v-if="showMap" class="floating-map" @click="showMap = false">
      <div class="floating-map-content" @click.stop>
        <GameMap
          :rooms="rooms"
          :currentRoomId="currentRoomId"
          :current-view-type="mapCurrentViewType"
          :view-box="mapViewBox"
        />
      </div>
    </div>

    <!-- 悬浮能力面板 -->
    <AbilityPanel
      v-if="showAbilityPanel"
      :gold="userGold"
      :configs="abilityConfigs"
      :ability="userAbility"
      @close="showAbilityPanel = false"
      @upgrade="handleUpgrade"
    />

    <!-- 结算确认对话框 -->
    <ConfirmDialog
      v-if="showSettleConfirm"
      title="确认结算"
      :message="'结算后游戏将重置，您将返回主界面。\n背包中的物品将转换为金币。'"
      :goldAmount="expectedSettleGold"
      @confirm="handleSettle"
      @cancel="showSettleConfirm = false"
    />
    </template>
  </div>
</template>

<script setup>
/**
 * 游戏主应用组件.
 * 整合地图、控制和状态显示组件.
 */
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue';
import { useAuthState } from '@/composables/useAuthState';
import { useMessageLog } from '@/composables/useMessageLog';
import { useGameState } from '@/composables/useGameState';
import { useAbilityState } from '@/composables/useAbilityState';
import { useKeyboardControls } from '@/composables/useKeyboardControls';
import { useBackgroundMusic } from '@/composables/useBackgroundMusic';

import { newGame, settleExploration } from '@/api/saveApi';
import GameMap from '@/components/GameMap.vue';
import Login from '@/components/Login.vue';
import GameStart from '@/views/GameStart.vue';
import GameView from '@/views/GameView.vue';
import AbilityPanel from '@/components/AbilityPanel.vue';
import ConfirmDialog from '@/components/ConfirmDialog.vue';
import InventoryDetailOverlay from '@/components/game/InventoryDetailOverlay.vue';

const roomView = ref(null);
const showAbilityPanel = ref(false);
const showSettleConfirm = ref(false);
const showInventoryDetail = ref(false);
const isInGame = ref(false); // 是否正在游戏中
const musicUnlocked = ref(false);

const { messageLog, appendLog, clearLog } = useMessageLog();

// 背景音乐控制
const { playMusic } = useBackgroundMusic();

const getCurrentMusicKey = () => {
  return isInGame.value ? 'GAME' : 'LOBBY';
};

const removeMusicUnlockListeners = () => {
  document.removeEventListener('pointerdown', handleFirstInteraction);
  document.removeEventListener('click', handleFirstInteraction);
  document.removeEventListener('keydown', handleFirstInteraction);
  window.removeEventListener('message', handleBackgroundMessageForMusic);
};

const tryPlayCurrentMusic = async () => {
  const success = await playMusic(getCurrentMusicKey());

  if (success) {
    musicUnlocked.value = true;
    removeMusicUnlockListeners();
  }

  return success;
};

const handleFirstInteraction = () => {
  tryPlayCurrentMusic();
};

const handleBackgroundMessageForMusic = (event) => {
  if (event.origin !== window.location.origin) {
    return;
  }

  const type = event.data && event.data.type;

  if (
    type === 'ARKNIGHTS_BG_PICKER_OPEN' ||
    type === 'ARKNIGHTS_BG_PICKER_CLOSE' ||
    type === 'ARKNIGHTS_LOGIN_AUDIO_UNLOCK' ||
    type === 'ARKNIGHTS_LOGIN_INTERACTION'
  ) {
    tryPlayCurrentMusic();
  }
};

const addMusicUnlockListeners = () => {
  document.addEventListener('pointerdown', handleFirstInteraction);
  document.addEventListener('click', handleFirstInteraction);
  document.addEventListener('keydown', handleFirstInteraction);
  window.addEventListener('message', handleBackgroundMessageForMusic);
};

// 监听游戏状态切换音乐
watch(isInGame, () => {
  if (musicUnlocked.value) {
    tryPlayCurrentMusic();
  }
});

const {
  isLoggedIn,
  showGameStart,
  username,
  initAuthFromStorage,
  handleLoginSuccess,
  handleLogout: baseLogout,
  handleBackToMenu: baseBackToMenu
} = useAuthState({
  onLogout: () => {
    clearLog();
    showInventoryDetail.value = false;
    if (selectedInventoryId) selectedInventoryId.value = '';
  },
  onBackToMenu: () => {
    showInventoryDetail.value = false;
    if (selectedInventoryId) selectedInventoryId.value = '';
  }
});

const getRoomViewRef = () => roomView.value;

const {
  userGold,
  userAbility,
  abilityConfigs,
  abilityLevels,
  currentMoveSpeed,
  fetchUserAbility: _fetchUserAbility,
  handleUpgrade: _handleUpgrade
} = useAbilityState({
  appendLog
});

const fetchUserAbility = () => _fetchUserAbility(username.value);

const {
  displayMessage,
  currentRoomName,
  currentRoomId,
  longDescription,
  exits,
  items,
  inventory,
  selectedInventoryId,
  playerWeight,
  playerMaxWeight,
  playerGridPosition,
  rooms,
  mapCurrentViewType,
  mapViewBox,
  isError,
  showMap,
  isMoving,
  updateGameState,
  fetchMap,
  move,
  look,
  goBack,
  showInventory,
  handleTake,
  handleDrop,
  handleEatCookie,
  handleSave: _handleSave,
  handleLoad: _handleLoad
} = useGameState({
  appendLog,
  resetRoomPosition: (direction) => roomView.value?.resetPosition(direction),
  fetchUserAbility
});

const getHelp = () => {
  window.open('/help.html', '_blank', 'width=600,height=500');
};

const playPlayerOperation = () => {
  roomView.value?.playOperation();
};

const toggleMap = async () => {
  if (!showMap.value) {
    await fetchMap(currentRoomId.value);
  }
  showMap.value = !showMap.value;
};

const closeInventoryDetail = () => {
  showInventoryDetail.value = false;
};

const openInventoryDetail = async () => {
  await showInventory();
  showInventoryDetail.value = true;
};

const toggleInventoryDetail = async () => {
  if (showInventoryDetail.value) {
    closeInventoryDetail();
    return;
  }
  await openInventoryDetail();
};

const handleSave = () => {
  _handleSave(username.value);
};

const handleLoad = () => {
  _handleLoad(username.value);
};

const handleUpgrade = (abilityCode) => {
  _handleUpgrade(username.value, abilityCode, {
    setDisplayMessage: (msg) => { displayMessage.value = msg; },
    setIsError: (val) => { isError.value = val; }
  });
};

useKeyboardControls({
  isLoggedIn,
  showGameStart,
  isMoving,
  playPlayerOperation,
  goBack,
  look,
  getHelp,
  handleSave,
  toggleMap,
  toggleInventoryDetail,
  closeInventoryDetail,
  isInventoryDetailOpen: showInventoryDetail,
  getRoomViewRef
});

onMounted(() => {
  initAuthFromStorage();
  addMusicUnlockListeners();
  tryPlayCurrentMusic();
});

onBeforeUnmount(() => {
  removeMusicUnlockListeners();
});

const handleStartGame = (gameData) => {
  showGameStart.value = false;
  isInGame.value = true;
  updateGameState(gameData);
  appendLog('开始新的探索');
  fetchMap();
  fetchUserAbility();
};

const handleContinueGame = (gameData) => {
  showGameStart.value = false;
  isInGame.value = true;
  updateGameState(gameData);
  appendLog('读取已有存档，继续探索');
  fetchMap(gameData.roomId);
  fetchUserAbility();
};

const handleLogout = () => {
  closeInventoryDetail();
  isInGame.value = false;
  baseLogout();
};

const handleBackToMenu = () => {
  closeInventoryDetail();
  isInGame.value = false;
  baseBackToMenu();
};

const inventoryTotalValue = computed(() => {
  return inventory.value.reduce((sum, item) => sum + (item.value || 0), 0);
});

const expectedSettleGold = computed(() => {
  const baseGold = inventoryTotalValue.value;
  const bonusLevel = abilityLevels.value?.goldBonusLevel || 1;
  const bonusPercent = (bonusLevel - 1) * 10;
  const bonus = Math.floor(baseGold * bonusPercent / 100);
  return baseGold + bonus;
});

const handleSettle = async () => {
  showSettleConfirm.value = false;
  await roomView.value?.playCheckout();
  try {
    const response = await settleExploration(username.value);
    if (response.data.success) {
      displayMessage.value = response.data.message;
      userGold.value = response.data.totalGold;
      inventory.value = [];
      selectedInventoryId.value = '';
      closeInventoryDetail();
      playerWeight.value = 0;
      playerMaxWeight.value = response.data.playerMaxWeight;
      isError.value = false;
      await fetchUserAbility();
      await newGame(username.value);
      isInGame.value = false;
      showGameStart.value = true;
      appendLog(displayMessage.value);
    } else {
      displayMessage.value = response.data.message;
      isError.value = true;
      appendLog(displayMessage.value, true);
    }
  } catch (error) {
    displayMessage.value = '结算失败：' + error.message;
    isError.value = true;
    appendLog(displayMessage.value, true);
  }
};
</script>

<style>
* {
  box-sizing: border-box;
}

#app {
  font-family: 'Microsoft YaHei', 'Segoe UI', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  text-align: center;
  color: #2c3e50;
  padding: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  outline: none;
}

h1 {
  color: white;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
  margin: 0;
  padding: 8px;
  font-size: 22px;
}

/* 顶部栏 */
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 20px;
  background: rgba(0,0,0,0.2);
}

.username {
  color: white;
  font-weight: bold;
  font-size: 16px;
}

.btn-menu {
  padding: 8px 16px;
  background: rgba(156, 39, 176, 0.8);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  transition: all 0.2s;
  margin-right: 10px;
}

.btn-menu:hover {
  background: #7B1FA2;
  transform: scale(1.05);
}

.btn-logout {
  padding: 8px 16px;
  background: rgba(231, 76, 60, 0.8);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  transition: all 0.2s;
}

.btn-logout:hover {
  background: #e74c3c;
  transform: scale(1.05);
}

.gold-display {
  background: rgba(255, 215, 0, 0.3);
  padding: 5px 15px;
  border-radius: 20px;
  color: #ffd700;
  font-weight: bold;
  font-size: 14px;
}

.btn-settle {
  background: linear-gradient(135deg, #9b59b6, #8e44ad);
}

.btn-settle:hover {
  background: linear-gradient(135deg, #8e44ad, #9b59b6);
}

.btn-ability {
  background: linear-gradient(135deg, #f39c12, #e67e22);
}

.btn-ability:hover {
  background: linear-gradient(135deg, #e67e22, #f39c12);
}

/* 整体布局 */
.game-wrapper {
  display: flex;
  gap: 15px;
  padding: 0 15px 15px;
  max-width: 1600px;
  margin: 0 auto;
  height: calc(100vh - 60px);
}

/* 左侧主区域 */
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
}

/* 房间视图包装 */
.room-wrapper {
  flex: 1;
  position: relative;
  min-height: 0;
}

/* 浮动功能按钮 */
.float-buttons {
  position: absolute;
  top: 15px;
  right: 15px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 20;
}

.btn-float {
  padding: 10px 18px;
  font-size: 14px;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: bold;
  box-shadow: 0 3px 10px rgba(0,0,0,0.3);
}

.btn-float:nth-child(1) { background: #4CAF50; }
.btn-float:nth-child(2) { background: #9C27B0; }
.btn-float:nth-child(3) { background: #2196F3; }
.btn-float:hover { transform: scale(1.05); opacity: 0.9; }

/* 底部状态栏 */
.status-bar {
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  border-radius: 12px;
  padding: 15px 20px;
  color: white;
  text-align: left;
  box-shadow: 0 4px 15px rgba(0,0,0,0.3);
}

.status-bar.error {
  background: linear-gradient(135deg, #c0392b 0%, #e74c3c 100%);
}

.status-location {
  font-size: 22px;
  font-weight: bold;
  margin-bottom: 8px;
}

.status-message {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 6px;
  line-height: 1.5;
}

.status-exits {
  font-size: 14px;
  opacity: 0.7;
}

/* 右侧信息面板 */
.side-panel {
  width: 280px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex-shrink: 0;
}

/* 控制区域 */
.controls-section {
  background: rgba(44, 62, 80, 0.9);
  border-radius: 12px;
  padding: 10px;
}

/* 物品面板 */
.items-panel {
  background: rgba(255,255,255,0.95);
  border-radius: 12px;
  padding: 18px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  text-align: left;
  flex: 1;
  overflow-y: auto;
  min-height: 200px;
}

.items-header {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 15px;
}

.items-panel h3 {
  margin: 0;
  font-size: 18px;
  color: #2c3e50;
}

.items-tabs {
  display: flex;
  gap: 8px;
}

.items-tabs button {
  flex: 1;
  padding: 8px 12px;
  font-size: 13px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  background: #e0e0e0;
  color: #666;
  transition: all 0.2s;
}

.items-tabs button.active {
  background: #4CAF50;
  color: white;
  font-weight: bold;
}

.items-tabs button:hover:not(.active) {
  background: #bdbdbd;
}

.item-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.empty-msg {
  text-align: center;
  color: #999;
  padding: 25px;
  font-size: 14px;
}

.item-card {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 12px;
}

.item-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-name {
  font-weight: bold;
  font-size: 15px;
  color: #2c3e50;
}

.item-desc {
  font-size: 13px;
  color: #7f8c8d;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.item-stats {
  font-size: 13px;
  color: #e74c3c;
  font-weight: bold;
}

.btn-take, .btn-drop {
  padding: 6px 14px;
  font-size: 13px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.2s;
}

.btn-take {
  background: #4CAF50;
  color: white;
}

.btn-take:hover {
  background: #388E3C;
}

.btn-drop {
  background: #ff9800;
  color: white;
}

.btn-drop:hover {
  background: #f57c00;
}

.items-actions {
  margin-top: 15px;
  padding-top: 12px;
  border-top: 1px solid #eee;
}

.btn-items {
  width: 100%;
  padding: 10px;
  font-size: 14px;
  background: #2196F3;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
}

.btn-items:hover {
  background: #1976D2;
}

.btn-cookie {
  width: 100%;
  padding: 12px;
  font-size: 14px;
  background: linear-gradient(135deg, #FF6B6B, #FF8E53);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  margin-top: 12px;
}

.btn-cookie:hover {
  background: linear-gradient(135deg, #FF5252, #FF7043);
}

/* 键盘提示 */
.keyboard-hint {
  margin-top: auto;
  padding: 10px;
  color: rgba(255,255,255,0.8);
  font-size: 12px;
  text-align: center;
}

/* 悬浮物品面板 */
.floating-items-panel {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.floating-items-content {
  background: white;
  border-radius: 16px;
  padding: 24px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 10px 40px rgba(0,0,0,0.3);
}

.floating-items-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #eee;
}

.floating-items-header h3 {
  margin: 0;
  font-size: 22px;
  color: #333;
}

.btn-close {
  background: #ff5252;
  color: white;
  border: none;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
  transition: all 0.2s;
}

.btn-close:hover {
  background: #ff1744;
  transform: scale(1.1);
}

.floating-item-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.floating-item-card {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #e0e0e0;
}

.floating-item-card .item-main {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.floating-item-card .item-name {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.floating-item-card .item-desc {
  font-size: 14px;
  color: #666;
}

.floating-item-card .item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e0e0e0;
}

.floating-item-card .item-stats {
  font-size: 13px;
  color: #888;
}

.floating-item-card .btn-take,
.floating-item-card .btn-drop {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  transition: all 0.2s;
}

.floating-item-card .btn-take {
  background: #4CAF50;
  color: white;
}

.floating-item-card .btn-take:hover {
  background: #45a049;
}

.floating-item-card .btn-drop {
  background: #ff9800;
  color: white;
}

.floating-item-card .btn-drop:hover {
  background: #fb8c00;
}

.btn-cookie {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #ff6b6b, #ff8e53);
  color: white;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 16px;
  font-weight: bold;
  margin-top: 15px;
  transition: all 0.2s;
}

.btn-cookie:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(255,107,107,0.4);
}

.weight-bar-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}

.weight-bar {
  flex: 1;
  height: 10px;
  background: #e0e0e0;
  border-radius: 5px;
  overflow: hidden;
}

.weight-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #4CAF50, #8BC34A);
  border-radius: 5px;
  transition: width 0.3s ease;
}

.weight-text {
  font-size: 14px;
  color: #666;
  font-weight: bold;
  white-space: nowrap;
}

.value-info {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
}

.value-label {
  font-size: 13px;
  color: #888;
}

.value-amount {
  font-size: 16px;
  font-weight: bold;
  color: #FF9800;
}

.empty-msg {
  text-align: center;
  padding: 30px;
  color: #999;
  font-size: 16px;
}

/* 地图按钮 */
.map-toggle-btn {
  padding: 10px 18px;
  background: #607D8B;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  box-shadow: 0 3px 10px rgba(0,0,0,0.2);
}

.map-toggle-btn:hover {
  background: #455A64;
}

.map-toggle-btn.btn-back {
  background: #9C27B0;
}

.map-toggle-btn.btn-back:hover {
  background: #7B1FA2;
}

.map-toggle-btn.btn-save {
  background: #FF9800;
}

.map-toggle-btn.btn-save:hover {
  background: #F57C00;
}

.map-toggle-btn.btn-look {
  background: #4CAF50;
}

.map-toggle-btn.btn-look:hover {
  background: #45a049;
}

/* 悬浮地图 */
.floating-map {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(5, 8, 8, 0.72);
  backdrop-filter: blur(6px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.floating-map-content {
  background:
    radial-gradient(circle at top left, rgba(215, 168, 77, 0.18), transparent 38%),
    linear-gradient(180deg, rgba(27, 33, 31, 0.98), rgba(12, 15, 15, 0.98));
  border: 1px solid rgba(215, 168, 77, 0.42);
  border-radius: 22px;
  box-shadow: 0 30px 90px rgba(0, 0, 0, 0.55);
  padding: 20px;
  width: 95vw;
  height: 90vh;
  max-width: 1300px;
  max-height: 900px;
  overflow: auto;
}
</style>
