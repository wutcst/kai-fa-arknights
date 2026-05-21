<template>
  <div id="app">
    <h1>🌍 文字冒险世界</h1>

    <div class="game-container">
      <!-- 消息提示 -->
      <div class="message" :class="{ error: isError }">{{ message }}</div>

      <!-- 地图可视化组件 -->
      <GameMap
        :rooms="rooms"
        :currentRoomId="currentRoomId"
      />

      <!-- 状态显示组件 -->
      <GameStatus
        :currentRoomName="currentRoomName"
        :description="longDescription"
        :exits="exits"
        :items="showItems ? items : undefined"
        :message="displayMessage"
        :isError="isError"
      />

      <!-- 方向控制组件 -->
      <GameControls
        :exits="exits"
        @move="move"
      />

      <!-- 功能按钮 -->
      <div class="buttons">
        <button @click="look" class="btn-look">🔍 查看</button>
        <button @click="getHelp" class="btn-help">❓ 帮助</button>
      </div>
    </div>
  </div>
</template>

<script>
/**
 * 游戏主应用组件.
 * 整合地图、控制和状态显示组件.
 */
import { getMap, getGameStatus, move, look } from '@/api/game';
import GameMap from '@/components/GameMap.vue';
import GameControls from '@/components/GameControls.vue';
import GameStatus from '@/components/GameStatus.vue';

export default {
  name: 'App',
  components: {
    GameMap,
    GameControls,
    GameStatus
  },
  data() {
    return {
      message: '欢迎来到文字冒险世界！',
      displayMessage: '',
      currentRoomName: '',
      currentRoomId: '',
      longDescription: '',
      exits: [],
      items: [],
      showItems: false,  // 是否显示物品列表
      rooms: [],
      isError: false
    };
  },
  mounted() {
    this.fetchMap();
  },
  methods: {
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
      try {
        const response = await move(direction);
        this.displayMessage = response.data.message;
        this.currentRoomName = response.data.description;
        this.currentRoomId = response.data.roomId;
        this.longDescription = response.data.longDescription;
        this.exits = Array.from(response.data.exits);
        // 移动后清空物品列表，只有点击查看时才加载
        this.items = [];
        this.showItems = false;
        this.isError = false;
      } catch (error) {
        this.displayMessage = '移动错误：' + (error.response?.data?.message || error.message);
        this.isError = true;
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
    getHelp() {
      window.open('/help.html', '_blank', 'width=600,height=500');
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
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
}

h1 {
  color: white;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
}

.game-container {
  max-width: 800px;
  margin: 0 auto;
  background-color: rgba(255, 255, 255, 0.95);
  border-radius: 15px;
  padding: 25px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
}

.message {
  padding: 12px;
  margin-bottom: 15px;
  border-radius: 8px;
  background: #e8f5e9;
  color: #2e7d32;
}

.message.error {
  background: #ffebee;
  color: #c62828;
}

.buttons {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 15px;
}

.buttons button {
  padding: 10px 25px;
  font-size: 14px;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-look {
  background: #4CAF50;
}

.btn-look:hover {
  background: #388E3C;
}

.btn-help {
  background: #2196F3;
}

.btn-help:hover {
  background: #1976D2;
}
</style>
