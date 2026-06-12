<template>
  <section class="status-shell" :class="{ danger: isOverweight || isError }">
    <div>
      <span class="status-label">ROOM</span>
      <strong>{{ roomName || '未知房间' }}</strong>
    </div>
    <div>
      <span class="status-label">FLOOR</span>
      <strong>{{ floorLabel }}</strong>
    </div>
    <div>
      <span class="status-label">LOAD</span>
      <strong>{{ playerWeight }} / {{ playerMaxWeight }}</strong>
    </div>
    <div>
      <span class="status-label">PLAYER</span>
      <strong>{{ username || '未登录' }}</strong>
    </div>
    <div class="status-hint">
      {{ hintText }}
    </div>
  </section>
</template>

<script>
export default {
  name: 'GameStatusBar',
  props: {
    roomName: {
      type: String,
      default: ''
    },
    floorLabel: {
      type: String,
      default: '室外区域'
    },
    username: {
      type: String,
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
    items: {
      type: Array,
      default: () => []
    },
    isError: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    isOverweight() {
      return this.playerWeight > this.playerMaxWeight;
    },
    hintText() {
      if (!this.username) return '请先登录后开始探索';
      if (this.isOverweight) return '当前负重超过上限，建议丢弃物品';
      if (this.isError) return '上一条操作未成功，查看日志确认原因';
      if (!this.items.length) return '当前未发现可拾取物品，可先查看房间';
      return `发现 ${this.items.length} 个可拾取物品`;
    }
  }
};
</script>

<style scoped>
.status-shell {
  display: grid;
  grid-template-columns: 1.1fr 0.8fr 0.7fr 0.9fr 1.6fr;
  gap: 12px;
  align-items: center;
  background: rgba(13, 18, 19, 0.86);
  border: 1px solid rgba(215, 168, 77, 0.42);
  border-radius: 16px;
  color: #f5ead2;
  padding: 14px 16px;
}

.status-shell.danger {
  border-color: #e1684f;
  box-shadow: 0 0 0 1px rgba(225, 104, 79, 0.25);
}

.status-label {
  display: block;
  color: #c7953d;
  font-family: 'Courier New', monospace;
  font-size: 11px;
  letter-spacing: 1.5px;
  margin-bottom: 3px;
}

strong {
  font-size: 15px;
}

.status-hint {
  color: #d4c3a5;
  font-size: 14px;
  text-align: left;
}

@media (max-width: 900px) {
  .status-shell {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 560px) {
  .status-shell {
    grid-template-columns: 1fr;
  }
}
</style>
