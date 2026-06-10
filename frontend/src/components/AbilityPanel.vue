<template>
  <div class="floating-items-panel floating-ability" @click.self="$emit('close')">
    <div class="floating-items-content">
      <div class="floating-items-header">
        <h3>⬆️ 能力升级</h3>
        <div class="gold-display">
          <span class="gold-label">💰 金币:</span>
          <span class="gold-amount">{{ gold }}</span>
        </div>
        <button class="btn-close" @click="$emit('close')">✕</button>
      </div>
      <div class="ability-list">
        <div v-for="config in configs" :key="config.abilityCode" class="ability-card">
          <div class="ability-info">
            <div class="ability-name">{{ config.abilityName }}</div>
            <div class="ability-desc">{{ config.description }}</div>
            <div class="ability-level-bar">
              <div class="ability-level-fill" :style="{ width: getLevelPercent(config) + '%' }"></div>
            </div>
            <div class="ability-stats">
              <span>等级: {{ getLevel(config) }} / {{ config.maxLevel }}</span>
              <span>当前值: {{ getValue(config) }}</span>
            </div>
          </div>
          <div class="ability-action">
            <template v-if="getLevel(config) >= config.maxLevel">
              <span class="max-level-tag">已满级</span>
            </template>
            <template v-else>
              <button
                class="btn-upgrade"
                @click="$emit('upgrade', config.abilityCode)"
              >
                升级 ({{ getCost(config) }}金币)
              </button>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AbilityPanel',
  props: {
    gold: {
      type: Number,
      default: 0
    },
    configs: {
      type: Array,
      default: () => []
    },
    ability: {
      type: Object,
      default: null
    }
  },
  methods: {
    getLevel(config) {
      if (!this.ability) return 1;
      const underscoreKey = config.abilityCode + 'Level';
      let level = this.ability[underscoreKey];
      if (level === undefined) {
        const camelKey = this.toCamelCase(config.abilityCode) + 'Level';
        level = this.ability[camelKey];
      }
      return level || 1;
    },
    toCamelCase(str) {
      return str.replace(/_([a-z])/g, (g) => g[1].toUpperCase());
    },
    getLevelPercent(config) {
      const level = this.getLevel(config);
      return (level / config.maxLevel) * 100;
    },
    getValue(config) {
      const level = this.getLevel(config);
      return config.baseValue + config.incrementPerLevel * (level - 1);
    },
    getCost(config) {
      const level = this.getLevel(config);
      if (level >= config.maxLevel) return null;
      return Math.floor(config.baseCost * Math.pow(config.costMultiplier, level - 1));
    }
  }
};
</script>

<style scoped>
.floating-ability {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.floating-items-content {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 15px;
  padding: 20px;
  width: 400px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
}

.floating-items-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid rgba(255, 255, 255, 0.3);
}

.floating-items-header h3 {
  margin: 0;
  color: white;
  font-size: 18px;
}

.gold-display {
  background: rgba(0, 0, 0, 0.2);
  padding: 8px 15px;
  border-radius: 20px;
  color: #ffd700;
  font-weight: bold;
}

.gold-label {
  margin-right: 5px;
}

.btn-close {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 16px;
}

.btn-close:hover {
  background: rgba(255, 255, 255, 0.3);
}

.ability-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.ability-card {
  background: white;
  border-radius: 10px;
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ability-info {
  flex: 1;
}

.ability-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.ability-desc {
  font-size: 12px;
  color: #666;
  margin-bottom: 10px;
}

.ability-level-bar {
  height: 8px;
  background: #e0e0e0;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 5px;
}

.ability-level-fill {
  height: 100%;
  background: linear-gradient(90deg, #4CAF50, #8BC34A);
  transition: width 0.3s;
}

.ability-stats {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #666;
}

.btn-upgrade {
  background: linear-gradient(135deg, #4CAF50, #8BC34A);
  border: none;
  color: white;
  padding: 10px 15px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
}

.btn-upgrade:hover {
  transform: scale(1.05);
}

.max-level-tag {
  color: #ff9800;
  font-weight: bold;
  font-size: 14px;
}
</style>
