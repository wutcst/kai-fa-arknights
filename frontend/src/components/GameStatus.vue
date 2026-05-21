<template>
  <div class="game-status">
    <div class="current-room">
      <h2>{{ currentRoomName }}</h2>
      <p class="description">{{ description }}</p>
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
