<template>
  <div class="game-status">
    <div class="current-room">
      <h2>{{ currentRoomName }}</h2>
      <p class="description">{{ description }}</p>
    </div>

    <!-- 物品列表 -->
    <div class="items" v-if="items.length > 0">
      <h3>🎒 房间内的物品</h3>
      <div class="item-list">
        <div class="item" v-for="item in items" :key="item.id">
          <span class="item-name">{{ item.name }}</span>
          <span class="item-desc">{{ item.description }}</span>
          <span class="item-weight">重{{ item.weight }}</span>
          <span class="item-value">价值{{ item.value }}</span>
        </div>
      </div>
    </div>

    <div class="exits" v-if="exits.length > 0">
      <p>可用出口: <span v-for="(exit, index) in exits" :key="exit">
        {{ getZhDirection(exit) }}{{ index < exits.length - 1 ? ' ' : '' }}
      </span></p>
    </div>
    <div class="error" v-if="message && isError">
      {{ message }}
    </div>
    <div class="message" v-else-if="message">
      {{ message }}
    </div>
  </div>
</template>

<script>
/**
 * 游戏状态显示组件.
 * 展示当前房间信息、描述和出口.
 */
export default {
  name: 'GameStatus',
  props: {
    currentRoomName: String,
    description: String,
    exits: {
      type: Array,
      default: () => []
    },
    items: {
      type: Array,
      default: () => []
    },
    message: String,
    isError: Boolean
  },
  methods: {
    getZhDirection(dir) {
      const dirMap = {
        'north': '北方',
        'south': '南方',
        'east': '东方',
        'west': '西方'
      };
      return dirMap[dir] || dir;
    }
  }
};
</script>

<style scoped>
.game-status {
  text-align: center;
  padding: 20px;
}

.current-room h2 {
  color: #4CAF50;
  margin-bottom: 10px;
}

.description {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
}

.items {
  margin: 20px 0;
  text-align: left;
}

.items h3 {
  color: #333;
  font-size: 16px;
  margin-bottom: 10px;
}

.item-list {
  background: #f9f9f9;
  border-radius: 8px;
  padding: 10px;
}

.item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #eee;
}

.item:last-child {
  border-bottom: none;
}

.item-name {
  font-weight: bold;
  color: #4CAF50;
  min-width: 80px;
}

.item-desc {
  flex: 1;
  color: #666;
  font-size: 13px;
  text-align: left;
  padding: 0 10px;
}

.item-weight {
  color: #999;
  font-size: 12px;
  min-width: 50px;
  text-align: right;
}

.item-value {
  color: #ff9800;
  font-size: 12px;
  min-width: 60px;
  text-align: right;
  font-weight: bold;
}

.no-items {
  margin: 20px 0;
  color: #999;
  font-style: italic;
}

.exits {
  margin-top: 15px;
  color: #888;
}

.error {
  margin-top: 15px;
  color: #f44336;
  padding: 10px;
  background: #ffebee;
  border-radius: 4px;
}

.message {
  margin-top: 15px;
  color: #4CAF50;
  padding: 10px;
  background: #e8f5e9;
  border-radius: 4px;
}
</style>
