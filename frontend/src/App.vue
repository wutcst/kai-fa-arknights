<template>
  <div id="app" tabindex="0" ref="appContainer">
    <!-- 未登录显示登录界面 -->
    <Login v-if="!isLoggedIn" @login-success="handleLoginSuccess" />

    <!-- 已登录显示游戏界面 -->
    <template v-else>
    <div class="top-bar">
      <span class="username">👤 {{ username }}</span>
      <button @click="handleLogout" class="btn-logout">退出登录</button>
    </div>
    <h1>🌍 文字冒险世界</h1>

    <div class="game-wrapper">
      <!-- 左侧：房间视图 -->
      <div class="main-area">
        <!-- 房间视图 -->
        <div class="room-wrapper">
          <RoomView
            ref="roomView"
            :roomName="currentRoomName"
            :description="longDescription"
            :exits="exits"
            @move="move"
          />
        </div>

        <!-- 底部状态栏 -->
        <div class="status-bar" :class="{ error: isError }">
          <div class="status-location">📍 {{ currentRoomName }}</div>
          <div class="status-message">{{ displayMessage || longDescription }}</div>
          <div class="status-exits" v-if="exits.length">出口：{{ exits.join('、') }}</div>
        </div>
      </div>

      <!-- 右侧：控制面板 -->
      <div class="side-panel">
        <!-- 方向控制 -->
        <div class="controls-section">
          <GameControls
            :exits="exits"
            @move="move"
          />
        </div>

        <!-- 操作按钮组 -->
        <button class="map-toggle-btn btn-look" @click="look">
          🔍 查看
        </button>
        <button class="map-toggle-btn" @click="showInventory">
          🎒 背包
        </button>
        <button class="map-toggle-btn" @click="showMap = !showMap">
          🗺️ {{ showMap ? '隐藏地图' : '显示地图' }}
        </button>
        <button class="map-toggle-btn" @click="getHelp">
          ❓ 帮助
        </button>
        <button class="map-toggle-btn btn-back" @click="goBack">
          ↩️ 返回
        </button>

        <!-- 悬浮地图 -->
        <div v-if="showMap" class="floating-map" @click="showMap = false">
          <div class="floating-map-content" @click.stop>
            <GameMap
              :rooms="rooms"
              :currentRoomId="currentRoomId"
            />
          </div>
        </div>

        <!-- 键盘提示 -->
        <div class="keyboard-hint">
          <span>方向键移动 | F房间物品 | B背包 | M地图 | H帮助 | Backspace/ESC返回</span>
        </div>
      </div>
    </div>

    <!-- 悬浮物品面板 - 房间物品 -->
    <div v-if="showRoomItems" class="floating-items-panel floating-room-items" @click.self="showRoomItems = false">
      <div class="floating-items-content">
        <div class="floating-items-header">
          <h3>🎒 房间内的物品</h3>
          <button class="btn-close" @click="showRoomItems = false">✕</button>
        </div>
        <div class="floating-item-list">
          <div v-if="items.length === 0" class="empty-msg">房间里没有物品</div>
          <div v-for="item in items" :key="item.id" class="floating-item-card">
            <div class="item-main">
              <span class="item-name">{{ item.name }}</span>
              <span class="item-desc">{{ item.description }}</span>
            </div>
            <div class="item-footer">
              <span class="item-stats">重量: {{ item.weight }} | 价值: {{ item.value }}</span>
              <button class="btn-take" @click="handleTake(item.id)">拾取</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 悬浮物品面板 - 背包 -->
    <div v-if="showInventoryPanel" class="floating-items-panel floating-inventory" @click.self="showInventoryPanel = false">
      <div class="floating-items-content">
        <div class="floating-items-header">
          <h3>🎒 我的背包</h3>
          <div class="weight-bar-wrapper">
            <div class="weight-bar">
              <div class="weight-bar-fill" :style="{ width: weightPercent + '%' }"></div>
            </div>
            <span class="weight-text">{{ playerWeight }} / {{ playerMaxWeight }}</span>
          </div>
          <div class="value-info">
            <span class="value-label">总价值:</span>
            <span class="value-amount">💰 {{ totalValue }}</span>
          </div>
          <button class="btn-close" @click="showInventoryPanel = false">✕</button>
        </div>
        <div class="floating-item-list">
          <div v-if="inventory.length === 0" class="empty-msg">背包是空的</div>
          <div v-for="item in inventory" :key="item.id" class="floating-item-card">
            <div class="item-main">
              <span class="item-name">{{ item.name }}</span>
              <span class="item-desc">{{ item.description }}</span>
            </div>
            <div class="item-footer">
              <span class="item-stats">重量: {{ item.weight }} | 价值: {{ item.value }}</span>
              <button class="btn-drop" @click="handleDrop(item.id)">丢弃</button>
            </div>
          </div>
          <button
            v-if="hasMagicCookie"
            class="btn-cookie"
            @click="handleEatCookie"
          >🍪 吃魔法饼干（+5负重）</button>
        </div>
      </div>
    </div>
    </template>
  </div>
</template>

<script>
/**
 * 游戏主应用组件.
 * 整合地图、控制和状态显示组件.
 */
import { getMap, getGameStatus, move, look, goBack, takeItem, dropItem, getItems, eatCookie } from '@/api/game';
import RoomView from '@/components/RoomView.vue';
import GameMap from '@/components/GameMap.vue';
import GameControls from '@/components/GameControls.vue';
import Login from '@/components/Login.vue';

export default {
  name: 'App',
  components: {
    RoomView,
    GameMap,
    GameControls,
    Login
  },
  data() {
    return {
      // 登录状态
      isLoggedIn: false,
      username: '',
      // 游戏状态
      message: '欢迎来到文字冒险世界！',
      displayMessage: '',
      currentRoomName: '',
      currentRoomId: '',
      longDescription: '',
      exits: [],
      items: [],           // 房间物品
      inventory: [],       // 玩家背包
      playerWeight: 0,
      playerMaxWeight: 20,
      showRoomItems: false,  // 是否显示房间物品面板
      showInventoryPanel: false,  // 是否显示背包面板
      rooms: [],
      isError: false,
      showMap: false,  // 是否显示地图
      isMoving: false  // 是否正在移动中
    };
  },
  created() {
    // 全局键盘事件监听
    window.addEventListener('keydown', (e) => {
      // 阻止方向键默认滚动页面行为
      if (['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'].includes(e.key)) {
        e.preventDefault();
      }
      
      // 如果焦点在输入框中，不处理游戏快捷键
      const activeElement = document.activeElement;
      if (activeElement && (activeElement.tagName === 'INPUT' || activeElement.tagName === 'TEXTAREA')) {
        return;
      }
      
      // 只有在已登录状态下才处理游戏快捷键
      if (!this.isLoggedIn) return;
      
      // 防止重复处理
      if (this.isMoving) return;
      
      // Backspace 或 Esc 返回
      if (e.key === 'Backspace' || e.key === 'Escape') {
        e.preventDefault();
        this.goBack();
        return;
      }
      
      // F 键查看房间物品
      if (e.key === 'f' || e.key === 'F') {
        this.look();
        return;
      }

      // B 键查看背包
      if (e.key === 'b' || e.key === 'B') {
        this.showInventory();
        return;
      }
      
      // H 键帮助
      if (e.key === 'h' || e.key === 'H') {
        this.getHelp();
        return;
      }
      
      // M 键切换地图
      if (e.key === 'm' || e.key === 'M') {
        this.showMap = !this.showMap;
        return;
      }
      
      // 方向键移动
      if (this.$refs.roomView) {
        this.$refs.roomView.tryMoveByKey(e);
      }
    }, true);
  },
  mounted() {
    // 检查登录状态
    const savedUsername = localStorage.getItem('username');
    if (savedUsername) {
      this.username = savedUsername;
      this.isLoggedIn = true;
      this.fetchMap();
    }
  },
  methods: {
    // 登录成功处理
    handleLoginSuccess(username) {
      this.username = username;
      this.isLoggedIn = true;
      this.fetchMap();
    },
    // 退出登录
    handleLogout() {
      this.isLoggedIn = false;
      this.username = '';
      localStorage.removeItem('username');
    },
    async fetchMap() {
      try {
        const response = await getMap();
        this.rooms = response.data.rooms;
        this.currentRoomId = response.data.currentRoomId;
        this.fetchStatus();
      } catch (error) {
        this.message = '错误：无法连接到服务器，请确保后端正在运行！';
        this.isError = true;
      }
    },
    async fetchStatus() {
      try {
        const response = await getGameStatus();
        this.currentRoomName = response.data.description;
        this.currentRoomId = response.data.roomId;
        this.longDescription = response.data.longDescription;
        this.exits = Array.from(response.data.exits);
        // 不加载物品列表，只有点击查看时才加载
        this.items = [];
        this.displayMessage = '';
        this.isError = false;
      } catch (error) {
        this.displayMessage = '错误：' + error.message;
        this.isError = true;
      }
    },
    async move(direction) {
      this.isMoving = true;
      try {
        const response = await move(direction);
        // 检查是否发生了传送
        if (response.data.teleported) {
          this.displayMessage = `⚠️ 你被传送门传送到了 ${response.data.description}！`;
        } else {
          this.displayMessage = response.data.message;
        }
        this.currentRoomName = response.data.description;
        this.currentRoomId = response.data.roomId;
        this.longDescription = response.data.longDescription;
        this.exits = Array.from(response.data.exits);
        // 移动后清空物品列表，只有点击查看时才加载
        this.items = [];
        this.showRoomItems = false;
        this.showInventoryPanel = false;
        this.isError = response.data.teleported;
      } catch (error) {
        this.displayMessage = '移动错误：' + (error.response?.data?.message || error.message);
        this.isError = true;
      } finally {
        this.isMoving = false;
        // 移动完成后重置小人位置（带延迟以配合动画）
        setTimeout(() => {
          if (this.$refs.roomView) {
            this.$refs.roomView.resetPosition();
          }
        }, 50);
      }
    },
    async look() {
      try {
        const response = await look();
        this.currentRoomName = response.data.description;
        this.currentRoomId = response.data.roomId;
        this.longDescription = response.data.longDescription;
        this.exits = Array.from(response.data.exits);
        this.items = response.data.items || [];
        this.showRoomItems = true;  // 查看时显示房间物品面板
        this.showInventoryPanel = false;
        this.displayMessage = '';
        this.isError = false;
      } catch (error) {
        this.displayMessage = '查看错误：' + error.message;
        this.isError = true;
      }
    },
    async goBack() {
      try {
        const response = await goBack();
        this.displayMessage = response.data.message;
        this.currentRoomName = response.data.description;
        this.currentRoomId = response.data.roomId;
        this.longDescription = response.data.longDescription;
        this.exits = Array.from(response.data.exits);
        this.items = [];
        this.showRoomItems = false;
        this.showInventoryPanel = false;
        this.isError = !response.data.success;
      } catch (error) {
        this.displayMessage = '返回错误：' + error.message;
        this.isError = true;
      }
    },
    getHelp() {
      window.open('/help.html', '_blank', 'width=600,height=500');
    },
    // 查看背包
    async showInventory() {
      try {
        const response = await getItems();
        this.inventory = response.data.inventory || [];
        this.playerWeight = response.data.playerWeight || 0;
        this.playerMaxWeight = response.data.playerMaxWeight || 50;
        this.showInventoryPanel = true;  // 显示背包悬浮面板
        this.showRoomItems = false;
        this.displayMessage = response.data.message;
      } catch (error) {
        this.displayMessage = '查看背包失败：' + error.message;
        this.isError = true;
      }
    },
    // 拾取物品
    async handleTake(itemId) {
      try {
        const response = await takeItem(itemId);
        this.displayMessage = response.data.message;
        this.items = response.data.items || [];
        this.inventory = response.data.inventory || [];
        this.playerWeight = response.data.playerWeight || 0;
        this.playerMaxWeight = response.data.playerMaxWeight || 50;
        this.isError = !response.data.success;
      } catch (error) {
        this.displayMessage = '拾取失败：' + error.message;
        this.isError = true;
      }
    },
    // 丢弃物品
    async handleDrop(itemId) {
      try {
        const response = await dropItem(itemId);
        this.displayMessage = response.data.message;
        this.items = response.data.items || [];
        this.inventory = response.data.inventory || [];
        this.playerWeight = response.data.playerWeight || 0;
        this.playerMaxWeight = response.data.playerMaxWeight || 50;
        this.isError = !response.data.success;
      } catch (error) {
        this.displayMessage = '丢弃失败：' + error.message;
        this.isError = true;
      }
    },
    // 吃魔法饼干
    async handleEatCookie() {
      try {
        const response = await eatCookie();
        this.displayMessage = response.data.message;
        this.inventory = response.data.inventory || [];
        this.playerWeight = response.data.playerWeight || 0;
        this.playerMaxWeight = response.data.playerMaxWeight || 50;
        this.isError = !response.data.success;
      } catch (error) {
        this.displayMessage = '吃饼干失败：' + error.message;
        this.isError = true;
      }
    }
  },
  computed: {
    // 检查背包中是否有魔法饼干
    hasMagicCookie() {
      return this.inventory.some(item => item.id === 'magic_cookie');
    },
    // 负重百分比
    weightPercent() {
      if (this.playerMaxWeight === 0) return 0;
      return Math.min(100, (this.playerWeight / this.playerMaxWeight) * 100);
    },
    // 背包物品总价值
    totalValue() {
      return this.inventory.reduce((sum, item) => sum + (item.value || 0), 0);
    }
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
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.floating-map-content {
  background: white;
  border-radius: 10px;
  padding: 20px;
  width: 95vw;
  height: 90vh;
  max-width: 1300px;
  max-height: 900px;
  overflow: auto;
}
</style>
