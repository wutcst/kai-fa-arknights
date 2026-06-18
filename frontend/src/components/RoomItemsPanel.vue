<template>
  <div v-if="show" class="floating-items-panel floating-room-items" @click.self="$emit('close')">
    <div class="floating-items-content">
      <div class="floating-items-header">
        <h3>🎒 房间内的物品</h3>
        <button class="btn-close" @click="$emit('close')">✕</button>
      </div>
      <div class="floating-item-list">
        <div v-if="items.length === 0" class="empty-msg">房间里没有物品</div>
        <div v-for="item in items" :key="item.id" class="floating-item-card">
          <img v-if="getItemImage(item)" :src="getItemImage(item)" class="item-icon" />
          <div class="item-main">
            <span class="item-name">{{ item.name }}</span>
            <span class="item-desc">{{ item.description }}</span>
          </div>
          <div class="item-footer">
            <span class="item-stats">重量: {{ item.weight }} | 价值: {{ item.value }}</span>
            <button class="btn-take" @click="$emit('take', item.id)">拾取</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import apSupplyImg from '@/assets/items/ap_supply.png';

export default {
  name: 'RoomItemsPanel',
  props: {
    show: {
      type: Boolean,
      default: false
    },
    items: {
      type: Array,
      default: () => []
    }
  },
  methods: {
    getItemImage(item) {
      const imageMap = {
        magic_cookie: apSupplyImg
      };
      return imageMap[item.id] || null;
    }
  }
};
</script>

<style scoped>
.item-icon {
  width: 32px;
  height: 32px;
  object-fit: contain;
  flex-shrink: 0;
}
</style>
