<template>
  <div v-if="show" class="floating-items-panel floating-inventory" @click.self="$emit('close')">
    <div class="floating-items-content">
      <div class="floating-items-header">
        <h3>🎒 我的背包</h3>
        <div class="weight-bar-wrapper">
          <div class="weight-bar">
            <div class="weight-bar-fill" :style="{ width: weightPercent + '%' }"></div>
          </div>
          <span class="weight-text">{{ playerWeight }} / {{ playerMaxWeight }}</span>
        </div>
        <div class="value-info">
          <span class="value-label">总价值:</span>
          <span class="value-amount">💰 {{ totalValue }}</span>
        </div>
        <button class="btn-close" @click="$emit('close')">✕</button>
      </div>
      <div class="floating-item-list">
        <div v-if="inventory.length === 0" class="empty-msg">背包是空的</div>
        <div v-for="item in inventory" :key="item.id" class="floating-item-card">
          <div class="item-main">
            <span class="item-name">{{ item.name }}</span>
            <span class="item-desc">{{ item.description }}</span>
          </div>
          <div class="item-footer">
            <span class="item-stats">重量: {{ item.weight }} | 价值: {{ item.value }}</span>
            <button class="btn-drop" @click="$emit('drop', item.id)">丢弃</button>
          </div>
        </div>
        <button
          v-if="hasMagicCookie"
          class="btn-cookie"
          @click="$emit('eat-cookie')"
        >🍪 吃理智增强剂（+5负重）</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'InventoryPanel',
  props: {
    show: {
      type: Boolean,
      default: false
    },
    inventory: {
      type: Array,
      default: () => []
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
  computed: {
    weightPercent() {
      if (this.playerMaxWeight === 0) return 0;
      return Math.min(100, (this.playerWeight / this.playerMaxWeight) * 100);
    },
    hasMagicCookie() {
      return this.inventory.some(item => item.id === 'magic_cookie');
    }
  }
};
</script>
