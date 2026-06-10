<template>
  <section class="action-panel">
    <h2>行动</h2>
    <div class="move-pad">
      <button :disabled="!hasExit('north') || busy" @click="$emit('move', 'north')">北</button>
      <button :disabled="!hasExit('west') || busy" @click="$emit('move', 'west')">西</button>
      <button :disabled="busy" class="center-action" @click="$emit('look')">查看</button>
      <button :disabled="!hasExit('east') || busy" @click="$emit('move', 'east')">东</button>
      <button :disabled="!hasExit('south') || busy" @click="$emit('move', 'south')">南</button>
    </div>

    <div class="action-stack">
      <button :disabled="!items.length || busy" @click="$emit('take', firstRoomItemId)">拾取房间物品</button>
      <button :disabled="!selectedInventoryId || busy" @click="$emit('drop', selectedInventoryId)">丢弃选中物品</button>
      <button :disabled="!hasMagicCookie || busy" @click="$emit('eat-cookie')">使用魔法饼干</button>
      <button :disabled="busy" @click="$emit('save')">保存游戏</button>
      <button :disabled="busy" @click="$emit('load')">读取存档</button>
      <button :disabled="busy" @click="$emit('back')">返回上个房间</button>
      <button :disabled="busy" @click="$emit('toggle-map')">切换地图</button>
      <button :disabled="busy" @click="$emit('open-ability')">升级能力</button>
      <button :disabled="busy" class="settle" @click="$emit('settle')">结算探索</button>
    </div>
  </section>
</template>

<script>
export default {
  name: 'ActionPanel',
  props: {
    exits: {
      type: Array,
      default: () => []
    },
    items: {
      type: Array,
      default: () => []
    },
    inventory: {
      type: Array,
      default: () => []
    },
    selectedInventoryId: {
      type: [String, Number],
      default: ''
    },
    busy: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    firstRoomItemId() {
      return this.items[0]?.id || '';
    },
    hasMagicCookie() {
      return this.inventory.some(item => item.id === 'magic_cookie');
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
.action-panel {
  background: rgba(18, 22, 22, 0.9);
  border: 1px solid rgba(215, 168, 77, 0.32);
  border-radius: 16px;
  color: #f6ead2;
  padding: 16px;
}

h2 {
  margin: 0 0 14px;
  text-align: left;
}

.move-pad {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 14px;
}

.move-pad button:first-child,
.move-pad button:last-child {
  grid-column: 2;
}

.center-action {
  background: #7a5a32;
}

.action-stack {
  display: grid;
  gap: 8px;
}

button {
  border: 1px solid rgba(247, 214, 123, 0.45);
  border-radius: 10px;
  background: linear-gradient(180deg, #2f3d35, #17211d);
  color: #f7ead2;
  cursor: pointer;
  font-weight: 700;
  min-height: 40px;
  transition: transform 0.14s ease, border-color 0.14s ease, filter 0.14s ease;
}

button:hover:not(:disabled) {
  border-color: #f7d67b;
  filter: brightness(1.1);
  transform: translateY(-1px);
}

button:active:not(:disabled) {
  transform: translateY(1px);
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.38;
}

.settle {
  background: linear-gradient(180deg, #6f3c2f, #2d1411);
}
</style>
