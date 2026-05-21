<template>
  <div class="game-controls">
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
  </div>
</template>

<script>
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
  gap: 8px;
  margin: 20px 0;
}

.control-row {
  display: flex;
  gap: 10px;
}

.direction-btn {
  padding: 12px 20px;
  font-size: 16px;
  border: 2px solid #4CAF50;
  background: white;
  color: #333;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  min-width: 80px;
}

.direction-btn:hover:not(:disabled) {
  background: #4CAF50;
  color: white;
}

.direction-btn:disabled {
  border-color: #ddd;
  color: #ccc;
  cursor: not-allowed;
}
</style>
