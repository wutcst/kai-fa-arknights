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
  background: rgba(5, 8, 8, 0.72);
  backdrop-filter: blur(8px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.floating-items-content {
  background: linear-gradient(135deg, rgba(15, 12, 5, 0.95) 0%, rgba(25, 18, 8, 0.95) 100%);
  border-radius: 12px;
  padding: 20px;
  width: 420px;
  max-height: 85vh;
  overflow-y: auto;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5), inset 0 1px 0 rgba(246, 234, 210, 0.1);
  border: 1px solid rgba(215, 168, 77, 0.5);
}

.floating-items-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(215, 168, 77, 0.3);
}

.floating-items-header h3 {
  margin: 0;
  color: #f7d67b;
  font-size: 18px;
  letter-spacing: 1px;
}

.gold-display {
  background: rgba(0, 0, 0, 0.4);
  padding: 6px 14px;
  border-radius: 20px;
  color: #f7d67b;
  font-weight: bold;
  border: 1px solid rgba(247, 214, 123, 0.4);
  display: flex;
  align-items: center;
  gap: 8px;
}

.gold-icon {
  width: 18px;
  height: 18px;
  vertical-align: middle;
}

.btn-close {
  background: rgba(246, 234, 210, 0.08);
  border: 1px solid rgba(215, 168, 77, 0.4);
  color: #f6ead2;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.2s;
}

.btn-close:hover {
  background: rgba(215, 168, 77, 0.2);
  border-color: #f7d67b;
}

.ability-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ability-card {
  background: rgba(0, 0, 0, 0.35);
  border-radius: 10px;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid rgba(215, 168, 77, 0.25);
  transition: border-color 0.2s;
}

.ability-card:hover {
  border-color: rgba(215, 168, 77, 0.5);
}

.ability-info {
  flex: 1;
}

.ability-name {
  font-size: 15px;
  font-weight: bold;
  color: #f6ead2;
  margin-bottom: 6px;
}

.ability-desc {
  font-size: 12px;
  color: #a89b82;
  margin-bottom: 10px;
  line-height: 1.4;
}

.ability-level-bar {
  height: 8px;
  background: rgba(0, 0, 0, 0.4);
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 6px;
  border: 1px solid rgba(215, 168, 77, 0.2);
}

.ability-level-fill {
  height: 100%;
  background: linear-gradient(90deg, #c7953d, #f7d67b);
  transition: width 0.3s;
  box-shadow: 0 0 6px rgba(247, 214, 123, 0.4);
}

.ability-stats {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #c7953d;
}

.btn-upgrade {
  background: linear-gradient(135deg, #c7953d 0%, #f7d67b 50%, #c7953d 100%);
  border: none;
  color: #1a1510;
  padding: 10px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  box-shadow: 0 2px 8px rgba(199, 149, 61, 0.3);
  transition: all 0.2s;
}

.btn-upgrade:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(247, 214, 123, 0.4);
}

.max-level-tag {
  color: #c7953d;
  font-weight: bold;
  font-size: 14px;
}
</style>
