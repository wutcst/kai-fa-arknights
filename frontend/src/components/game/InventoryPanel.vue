<template>
  <section class="inventory-card">
    <div class="panel-title">
      <h2>背包</h2>
      <span>{{ playerWeight }} / {{ playerMaxWeight }}</span>
    </div>

    <div class="weight-track">
      <div class="weight-fill" :class="{ overweight: isOverweight }" :style="{ width: weightPercent + '%' }"></div>
    </div>

    <div class="inventory-list">
      <button
        v-for="item in inventory"
        :key="item.id"
        class="inventory-item"
        :class="{ selected: item.id === selectedId }"
        @click="$emit('select', item.id)"
      >
        <span class="item-name">{{ item.name }}</span>
        <span class="item-meta">重 {{ item.weight || 0 }} / 值 {{ item.value || 0 }}</span>
      </button>
      <p v-if="!inventory.length" class="empty-text">背包为空</p>
    </div>
  </section>
</template>

<script>
export default {
  name: 'GameInventoryPanel',
  props: {
    inventory: {
      type: Array,
      default: () => []
    },
    selectedId: {
      type: [String, Number],
      default: ''
    },
    playerWeight: {
      type: Number,
      default: 0
    },
    playerMaxWeight: {
      type: Number,
      default: 20
    }
  },
  computed: {
    weightPercent() {
      if (!this.playerMaxWeight) return 0;
      return Math.min(100, Math.round((this.playerWeight / this.playerMaxWeight) * 100));
    },
    isOverweight() {
      return this.playerWeight > this.playerMaxWeight;
    }
  }
};
</script>

<style scoped>
.inventory-card {
  background: rgba(18, 22, 22, 0.9);
  border: 1px solid rgba(215, 168, 77, 0.32);
  border-radius: 16px;
  color: #f6ead2;
  padding: 16px;
}

.panel-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

h2 {
  margin: 0;
}

.weight-track {
  height: 8px;
  background: #1b1f1e;
  border-radius: 999px;
  margin: 12px 0;
  overflow: hidden;
}

.weight-fill {
  height: 100%;
  background: linear-gradient(90deg, #58b985, #d7a84d);
  transition: width 0.2s ease;
}

.weight-fill.overweight {
  background: #e1684f;
}

.inventory-list {
  display: grid;
  gap: 8px;
  max-height: 240px;
  overflow-y: auto;
}

.inventory-item {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.04);
  color: #f6ead2;
  cursor: pointer;
  padding: 10px;
  text-align: left;
}

.inventory-item.selected {
  border-color: #f7d67b;
  background: rgba(247, 214, 123, 0.14);
}

.item-name {
  font-weight: 700;
}

.item-meta {
  color: #c9b796;
  font-size: 12px;
  white-space: nowrap;
}

.empty-text {
  color: #b9aa90;
  margin: 8px 0 0;
}
</style>
