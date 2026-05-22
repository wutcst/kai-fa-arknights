<template>
  <div id="app" @keydown="handleKeydown" tabindex="0" ref="appContainer">
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

          <!-- 浮动功能按钮 -->
          <div class="float-buttons">
            <button @click="look" class="btn-float">🔍 查看</button>
            <button @click="goBack" class="btn-float">↩️ 返回</button>
            <button @click="getHelp" class="btn-float">❓ 帮助</button>
          </div>
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

        <!-- 地图按钮 -->
        <button class="map-toggle-btn" @click="showMap = !showMap">
          🗺️ {{ showMap ? '隐藏地图' : '显示地图' }}
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

        <!-- 物品栏 -->
        <div class="items-panel" v-if="showItems">
          <div class="items-header">
            <h3>🎒 物品</h3>
            <div class="items-tabs">
              <button
                :class="{ active: itemsViewMode === 'room' }"
                @click="itemsViewMode = 'room'"
              >房间物品</button>
              <button
                :class="{ active: itemsViewMode === 'inventory' }"
                @click="switchToInventory"
              >背包({{ playerWeight }}/{{ playerMaxWeight }})</button>
            </div>
          </div>

          <!-- 房间物品列表 -->
          <div v-if="itemsViewMode === 'room'" class="item-list">
            <div v-if="items.length === 0" class="empty-msg">房间里没有物品</div>
            <div v-for="item in items" :key="item.id" class="item-card">
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

          <!-- 背包物品列表 -->
          <div v-if="itemsViewMode === 'inventory'" class="item-list">
            <div v-if="inventory.length === 0" class="empty-msg">背包是空的</div>
            <div v-for="item in inventory" :key="item.id" class="item-card">
              <div class="item-main">
                <span class="item-name">{{ item.name }}</span>
                <span class="item-desc">{{ item.description }}</span>
              </div>
              <div class="item-footer">
                <span class="item-stats">重量: {{ item.weight }} | 价值: {{ item.value }}</span>
                <button class="btn-drop" @click="handleDrop(item.id)">丢弃</button>
              </div>
            </div>
            <!-- 吃饼干按钮 -->
            <button
              v-if="hasMagicCookie"
              class="btn-cookie"
              @click="handleEatCookie"
            >🍪 吃魔法饼干（+5负重）</button>
          </div>

          <!-- 查看物品按钮 -->
          <div class="items-actions">
            <button class="btn-items" @click="handleShowItems">📦 查看所有物品</button>
          </div>
        </div>

        <!-- 键盘提示 -->
        <div class="keyboard-hint">
          <span>方向键移动 | L查看 | M地图 | H帮助 | Backspace/ESC返回</span>
        </div>
      </div>
    </div>
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

export default {
  name: 'App',
  components: {
    RoomView,
    GameMap,
    GameControls
  },
  data() {
    return {
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
      showItems: false,  // 是否显示物品列表
      rooms: [],
      isError: false,
      showMap: false,  // 是否显示地图
      isMoving: false,  // 是否正在移动中
      itemsViewMode: 'room'  // 'room' 或 'inventory'
    };
  },
  created() {
    // 全局键盘事件监听
    window.addEventListener('keydown', (e) => {
      // 阻止方向键默认滚动页面行为
      if (['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'].includes(e.key)) {
        e.preventDefault();
      }
      if (this.isMoving) return;
      if (this.$refs.roomView) {
        this.$refs.roomView.tryMoveByKey(e);
      }
    }, true);
  },
  mounted() {
    this.fetchMap();
  },
  methods: {
    // 处理键盘事件 - 控制 RoomView 中的小人移动
    handleKeydown(event) {
      // 阻止方向键默认滚动页面行为
      if (['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'].includes(event.key)) {
        event.preventDefault();
      }
      if (this.isMoving) return;

      // Backspace 或 Esc 返回
      if (event.key === 'Backspace' || event.key === 'Escape') {
        this.goBack();
        return;
      }

      // L 键查看
      if (event.key === 'l' || event.key === 'L') {
        this.look();
        return;
      }

      // H 键帮助
      if (event.key === 'h' || event.key === 'H') {
        this.getHelp();
        return;
      }

      // M 键切换地图
      if (event.key === 'm' || event.key === 'M') {
        this.showMap = !this.showMap;
        return;
      }

      if (this.$refs.roomView) {
        this.$refs.roomView.tryMoveByKey(event);
      }
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
        this.showItems = false;
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
        this.showItems = true;  // 查看时显示物品列表
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
        this.showItems = false;
        this.isError = !response.data.success;
      } catch (error) {
        this.displayMessage = '返回错误：' + error.message;
        this.isError = true;
      }
    },
    getHelp() {
      window.open('/help.html', '_blank', 'width=600,height=500');
    },
    // 切换到背包标签页
    async switchToInventory() {
      this.itemsViewMode = 'inventory';
      // 加载最新的背包数据
      try {
        const response = await getItems();
        this.inventory = response.data.inventory || [];
        this.playerWeight = response.data.playerWeight || 0;
        this.playerMaxWeight = response.data.playerMaxWeight || 50;
      } catch (error) {
        console.error('获取背包失败:', error);
      }
    },
    // 查看物品（显示房间物品）
    async handleShowItems() {
      try {
        const response = await getItems();
        this.items = response.data.items || [];
        this.inventory = response.data.inventory || [];
        this.playerWeight = response.data.playerWeight || 0;
        this.playerMaxWeight = response.data.playerMaxWeight || 50;
        this.itemsViewMode = 'room';
        this.showItems = true;
        this.displayMessage = response.data.message;
      } catch (error) {
        this.displayMessage = '查看物品失败：' + error.message;
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
