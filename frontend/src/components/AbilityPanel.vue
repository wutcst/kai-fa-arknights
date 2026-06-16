<template>
  <div class="floating-items-panel floating-ability" @click.self="$emit('close')">
    <div class="floating-items-content">
      <div class="floating-items-header">
        <h3>⬆️ 能力升级</h3>
        <div class="gold-display">
          <img src="@/assets/items/Lungmen_Dollars.png" class="gold-icon" alt="龙门币">
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
                升级 ({{ getCost(config) }}龙门币)
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
  background: rgba(10, 20, 40, 0.9);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.floating-items-content {
  background: linear-gradient(135deg, #1a2a4a 0%, #0f1f3a 100%);
  border-radius: 15px;
  padding: 20px;
  width: 400px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 10px 40px rgba(0, 191, 255, 0.2);
  border: 1px solid rgba(0, 191, 255, 0.3);
}

.floating-items-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid rgba(0, 191, 255, 0.3);
}

.floating-items-header h3 {
  margin: 0;
  color: #00BFFF;
  font-size: 18px;
  text-shadow: 0 0 10px rgba(0, 191, 255, 0.5);
}

.gold-display {
  background: rgba(0, 0, 0, 0.3);
  padding: 6px 12px;
  border-radius: 20px;
  color: #ffd700;
  font-weight: bold;
  border: 1px solid rgba(255, 215, 0, 0.3);
  display: flex;
  align-items: center;
  gap: 6px;
}

.gold-icon {
  width: 18px;
  height: 18px;
  vertical-align: middle;
}

.btn-close {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(0, 191, 255, 0.3);
  color: #00BFFF;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 16px;
}

.btn-close:hover {
  background: rgba(0, 191, 255, 0.2);
}

.ability-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.ability-card {
  background: rgba(0, 191, 255, 0.1);
  border-radius: 10px;
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid rgba(0, 191, 255, 0.2);
}

.ability-info {
  flex: 1;
}

.ability-name {
  font-size: 16px;
  font-weight: bold;
  color: #e0efff;
  margin-bottom: 5px;
}

.ability-desc {
  font-size: 12px;
  color: #7EC8E3;
  margin-bottom: 10px;
}

.ability-level-bar {
  height: 8px;
  background: rgba(0, 191, 255, 0.2);
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 5px;
}

.ability-level-fill {
  height: 100%;
  background: linear-gradient(90deg, #00BFFF, #4ECDC4);
  transition: width 0.3s;
  box-shadow: 0 0 8px rgba(0, 191, 255, 0.5);
}

.ability-stats {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #7EC8E3;
}

.btn-upgrade {
  background: linear-gradient(135deg, #00BFFF, #4ECDC4);
  border: none;
  color: white;
  padding: 10px 15px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  box-shadow: 0 2px 10px rgba(0, 191, 255, 0.3);
}

.btn-upgrade:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 15px rgba(0, 191, 255, 0.5);
}

.max-level-tag {
  color: #4ECDC4;
  font-weight: bold;
  font-size: 14px;
  text-shadow: 0 0 5px rgba(78, 205, 196, 0.5);
}
</style>
