<template>
  <div v-if="show" class="inventory-overlay" @click.self="$emit('close')">
    <section class="inventory-dialog" role="dialog" aria-modal="true" aria-labelledby="inventory-detail-title">
      <header class="dialog-header">
        <div>
          <span class="dialog-kicker">BAG DETAIL</span>
          <h2 id="inventory-detail-title">背包详情</h2>
        </div>
        <button class="close-button" type="button" @click="$emit('close')">B / Esc 关闭</button>
      </header>

      <div class="summary-grid">
        <article class="summary-card wide">
          <span>当前负重</span>
          <strong :class="{ danger: isOverweight }">{{ playerWeight }} / {{ playerMaxWeight }}</strong>
          <div class="weight-track" aria-hidden="true">
            <div class="weight-fill" :class="{ danger: isOverweight }" :style="{ width: weightPercent + '%' }"></div>
          </div>
        </article>
        <article class="summary-card">
          <span>总价值</span>
          <strong>{{ totalValue }}</strong>
        </article>
        <article class="summary-card">
          <span>物品数</span>
          <strong>{{ inventory.length }}</strong>
        </article>
      </div>

      <div class="item-list">
        <p v-if="!inventory.length" class="empty-state">背包为空</p>

        <template v-else>
          <article
            v-for="item in inventory"
            :key="item.id"
            class="item-card"
            :class="{ selected: item.id === selectedId }"
            role="button"
            tabindex="0"
            @click="$emit('select', item.id)"
            @keydown.enter.prevent="$emit('select', item.id)"
            @keydown.space.prevent="$emit('select', item.id)"
          >
            <div class="item-main">
              <div>
                <h3>{{ item.name }}</h3>
                <p>{{ item.description || '暂无描述' }}</p>
              </div>
              <span v-if="item.id === selectedId" class="selected-badge">已选中</span>
            </div>
            <div class="item-meta">
              <span>重量 {{ item.weight || 0 }}</span>
              <span>价值 {{ item.value || 0 }}</span>
            </div>
            <button class="drop-button" type="button" @click.stop="$emit('drop', item.id)">丢弃</button>
          </article>
        </template>
      </div>

      <footer class="dialog-actions">
        <button
          v-if="hasMagicCookie"
          class="cookie-button"
          type="button"
          @click="$emit('eat-cookie')"
        >
          使用理智增强剂
        </button>
      </footer>
    </section>
  </div>
</template>

<script>
export default {
  name: 'InventoryDetailOverlay',
  props: {
    show: {
      type: Boolean,
      default: false
    },
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
    },
    totalValue: {
      type: Number,
      default: 0
    }
  },
  emits: ['close', 'select', 'drop', 'eat-cookie'],
  computed: {
    weightPercent() {
      if (!this.playerMaxWeight) return 0;
      return Math.min(100, Math.round((this.playerWeight / this.playerMaxWeight) * 100));
    },
    isOverweight() {
      return this.playerWeight > this.playerMaxWeight;
    },
    hasMagicCookie() {
      return this.inventory.some(item => item.id === 'magic_cookie');
    }
  }
};
</script>

<style scoped>
.inventory-overlay {
  align-items: center;
  background: rgba(5, 8, 8, 0.72);
  backdrop-filter: blur(6px);
  display: flex;
  inset: 0;
  justify-content: center;
  padding: 22px;
  position: fixed;
  z-index: 1200;
}

.inventory-dialog {
  background:
    radial-gradient(circle at top left, rgba(215, 168, 77, 0.18), transparent 38%),
    linear-gradient(180deg, rgba(27, 33, 31, 0.98), rgba(12, 15, 15, 0.98));
  border: 1px solid rgba(215, 168, 77, 0.42);
  border-radius: 22px;
  box-shadow: 0 30px 90px rgba(0, 0, 0, 0.55);
  color: #f6ead2;
  max-height: min(84vh, 760px);
  max-width: 860px;
  overflow: hidden;
  padding: 20px;
  width: min(100%, 860px);
}

.dialog-header {
  align-items: flex-start;
  display: flex;
  gap: 16px;
  justify-content: space-between;
  margin-bottom: 16px;
}

.dialog-kicker {
  color: #d7a84d;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 2px;
}

h2,
h3,
p {
  margin: 0;
}

h2 {
  font-size: 26px;
  margin-top: 4px;
}

.close-button,
.cookie-button,
.drop-button {
  border: 1px solid rgba(247, 214, 123, 0.42);
  border-radius: 999px;
  background: rgba(18, 22, 22, 0.78);
  color: #f7ead2;
  cursor: pointer;
  font-weight: 800;
  padding: 10px 14px;
  transition: border-color 0.18s ease, filter 0.18s ease, transform 0.18s ease;
}

.close-button:hover,
.cookie-button:hover,
.drop-button:hover {
  border-color: rgba(247, 214, 123, 0.72);
  filter: brightness(1.08);
  transform: translateY(-1px);
}

.summary-grid {
  display: grid;
  gap: 12px;
  grid-template-columns: 2fr 1fr 1fr;
  margin-bottom: 16px;
}

.summary-card {
  background: rgba(255, 255, 255, 0.055);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  display: grid;
  gap: 8px;
  padding: 14px;
  text-align: left;
}

.summary-card span {
  color: #b9aa90;
  font-size: 12px;
}

.summary-card strong {
  color: #f7d67b;
  font-size: 20px;
}

.summary-card strong.danger {
  color: #ff9b82;
}

.weight-track {
  background: rgba(255, 255, 255, 0.07);
  border-radius: 999px;
  height: 8px;
  overflow: hidden;
}

.weight-fill {
  background: linear-gradient(90deg, #58b985, #d7a84d);
  height: 100%;
  transition: width 0.2s ease;
}

.weight-fill.danger {
  background: linear-gradient(90deg, #e1684f, #ffb088);
}

.item-list {
  display: grid;
  gap: 10px;
  max-height: min(46vh, 390px);
  overflow-y: auto;
  padding-right: 6px;
}

.empty-state {
  background: rgba(255, 255, 255, 0.045);
  border: 1px dashed rgba(215, 168, 77, 0.34);
  border-radius: 16px;
  color: #b9aa90;
  padding: 24px;
  text-align: center;
}

.item-card {
  background: rgba(255, 255, 255, 0.055);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  color: #f6ead2;
  cursor: pointer;
  display: grid;
  gap: 12px;
  padding: 14px;
  text-align: left;
  transition: border-color 0.18s ease, background 0.18s ease, transform 0.18s ease;
}

.item-card:hover {
  background: rgba(247, 214, 123, 0.1);
  border-color: rgba(247, 214, 123, 0.5);
  transform: translateY(-1px);
}

.item-card.selected {
  background: linear-gradient(180deg, rgba(247, 214, 123, 0.18), rgba(143, 102, 45, 0.16));
  border-color: #f7d67b;
  box-shadow: inset 0 0 0 1px rgba(247, 214, 123, 0.2);
}

.item-main {
  align-items: flex-start;
  display: flex;
  gap: 12px;
  justify-content: space-between;
}

h3 {
  font-size: 18px;
}

.item-main p {
  color: #c9b796;
  line-height: 1.5;
  margin-top: 6px;
}

.selected-badge {
  border: 1px solid rgba(247, 214, 123, 0.44);
  border-radius: 999px;
  color: #f7d67b;
  flex: 0 0 auto;
  font-size: 12px;
  font-weight: 800;
  padding: 5px 8px;
}

.item-meta {
  color: #d7c7aa;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 13px;
}

.drop-button {
  border-color: rgba(225, 104, 79, 0.5);
  justify-self: end;
  padding: 8px 12px;
}

.cookie-button {
  background: linear-gradient(135deg, rgba(88, 185, 133, 0.24), rgba(215, 168, 77, 0.18));
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

@media (max-width: 640px) {
  .inventory-overlay {
    align-items: stretch;
    padding: 12px;
  }

  .inventory-dialog {
    border-radius: 18px;
    max-height: 92vh;
    padding: 16px;
  }

  .dialog-header,
  .item-main {
    flex-direction: column;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }

  .close-button {
    width: 100%;
  }
}
</style>
