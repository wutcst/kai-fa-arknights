<template>
  <section class="inventory-hotbar" aria-label="底部物品栏">
    <header class="weight-row">
      <div class="weight-track" aria-hidden="true">
        <div class="weight-fill" :class="{ overweight: isOverweight }" :style="{ width: weightPercent + '%' }"></div>
      </div>
      <span class="weight-value" :class="{ overweight: isOverweight }">
        {{ playerWeight }} / {{ playerMaxWeight }}
      </span>
    </header>

    <p v-if="!inventory.length" class="empty-hint">暂无携带物品</p>

    <div v-else class="hotbar-items">
      <button
        v-for="item in inventory"
        :key="item.id"
        class="hotbar-item"
        :class="{ selected: item.id === selectedId }"
        type="button"
        @click="$emit('select', item.id)"
      >
        <span class="item-name">{{ item.name }}</span>
        <span class="item-meta">
          重 {{ item.weight || 0 }}
          <template v-if="item.value !== undefined"> / 值 {{ item.value }}</template>
        </span>
      </button>
    </div>
  </section>
</template>

<script>
export default {
  name: 'InventoryHotbar',
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
  },
  emits: ['select']
};
</script>

<style scoped>
.inventory-hotbar {
  background:
    linear-gradient(180deg, rgba(25, 31, 30, 0.9), rgba(10, 12, 12, 0.94)),
    radial-gradient(circle at top, rgba(215, 168, 77, 0.16), transparent 58%);
  border: 1px solid rgba(215, 168, 77, 0.34);
  border-radius: 18px;
  box-shadow: 0 18px 36px rgba(0, 0, 0, 0.34);
  color: #f6ead2;
  padding: 12px;
}

.weight-row {
  align-items: center;
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.weight-value {
  border: 1px solid rgba(247, 214, 123, 0.3);
  border-radius: 999px;
  color: #f7d67b;
  font-size: 13px;
  font-weight: 800;
  padding: 6px 10px;
  white-space: nowrap;
}

.weight-value.overweight {
  border-color: rgba(225, 104, 79, 0.7);
  color: #ff9b82;
}

.weight-track {
  background: rgba(255, 255, 255, 0.07);
  border-radius: 999px;
  flex: 1;
  height: 7px;
  overflow: hidden;
}

.weight-fill {
  background: linear-gradient(90deg, #58b985, #d7a84d);
  height: 100%;
  transition: width 0.2s ease;
}

.weight-fill.overweight {
  background: linear-gradient(90deg, #e1684f, #ffb088);
}

.empty-hint {
  color: #b9aa90;
  font-size: 14px;
  margin: 0;
  text-align: center;
}

.hotbar-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 320px;
  overflow-y: auto;
  padding: 2px 4px 4px;
  scrollbar-color: rgba(247, 214, 123, 0.36) rgba(255, 255, 255, 0.06);
  scrollbar-width: thin;
}

.hotbar-item {
  align-items: flex-start;
  background: rgba(255, 255, 255, 0.055);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 14px;
  color: #f6ead2;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 10px 12px;
  text-align: left;
  transition: border-color 0.18s ease, background 0.18s ease, transform 0.18s ease;
}

.hotbar-item:hover {
  background: rgba(247, 214, 123, 0.1);
  border-color: rgba(247, 214, 123, 0.55);
  transform: translateY(-1px);
}

.hotbar-item.selected {
  background: linear-gradient(180deg, rgba(247, 214, 123, 0.2), rgba(143, 102, 45, 0.16));
  border-color: #f7d67b;
  box-shadow: inset 0 0 0 1px rgba(247, 214, 123, 0.22), 0 0 18px rgba(247, 214, 123, 0.16);
}

.item-name {
  font-size: 14px;
  font-weight: 800;
  line-height: 1.2;
}

.item-meta {
  color: #c9b796;
  font-size: 12px;
  line-height: 1.2;
}

@media (max-width: 640px) {
  .inventory-hotbar {
    border-radius: 14px;
    padding: 10px;
  }

  .hotbar-item {
    flex-basis: 104px;
    min-height: 60px;
  }

  .hotbar-items {
    justify-content: flex-start;
  }
}
</style>
