<template>
  <div class="game-controls">
    <div class="control-row">
      <button
        class="direction-btn up"
        @click="$emit('move', 'up')"
        :disabled="!hasExit('up')"
      >
        ↑ 上楼
      </button>
    </div>
    <div class="control-row">
      <button
        class="direction-btn"
        @click="$emit('move', 'north')"
        :disabled="!hasExit('north')"
      >
        ↑ 北
      </button>
    </div>
    <div class="control-row">
      <button
        class="direction-btn"
        @click="$emit('move', 'west')"
        :disabled="!hasExit('west')"
      >
        ← 西
      </button>
      <button
        class="direction-btn"
        @click="$emit('move', 'east')"
        :disabled="!hasExit('east')"
      >
        东 →
      </button>
    </div>
    <div class="control-row">
      <button
        class="direction-btn"
        @click="$emit('move', 'south')"
        :disabled="!hasExit('south')"
      >
        ↓ 南
      </button>
    </div>
    <div class="control-row">
      <button
        class="direction-btn down"
        @click="$emit('move', 'down')"
        :disabled="!hasExit('down')"
      >
        ↓ 下楼
      </button>
    </div>
  </div>
</template>

<script>
/**
 * 游戏方向控制组件.
 * 提供移动按钮.
 */
export default {
  name: 'GameControls',
  props: {
    exits: {
      type: Array,
      default: () => []
    }
  },
  methods: {
    hasExit(direction) {
      return this.exits.includes(direction);
    }
  }
};
</script>

<style scoped>
.game-controls {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  background: rgba(44, 62, 80, 0.9);
  padding: 12px 15px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.4);
}

.control-row {
  display: flex;
  gap: 8px;
}

.direction-btn {
  padding: 10px 16px;
  font-size: 14px;
  border: 2px solid #2ecc71;
  background: rgba(255,255,255,0.1);
  color: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  min-width: 65px;
  font-weight: bold;
}

.direction-btn:hover:not(:disabled) {
  background: #2ecc71;
  color: white;
  transform: scale(1.05);
}

.direction-btn:disabled {
  border-color: #555;
  color: #666;
  cursor: not-allowed;
  background: rgba(255,255,255,0.05);
}
</style>
