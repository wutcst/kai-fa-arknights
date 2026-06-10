<template>
  <section class="action-panel">
    <h2>行动</h2>
    <div class="move-pad">
      <button class="north-action" :disabled="!hasExit('north') || busy" @click="$emit('move', 'north')">北</button>
      <button class="west-action" :disabled="!hasExit('west') || busy" @click="$emit('move', 'west')">西</button>
      <button :disabled="busy" class="center-action" @click="$emit('look')">查看</button>
      <button class="east-action" :disabled="!hasExit('east') || busy" @click="$emit('move', 'east')">东</button>
      <button class="south-action" :disabled="!hasExit('south') || busy" @click="$emit('move', 'south')">南</button>
    </div>
    <div class="stair-actions">
      <button :disabled="activeVerticalExit !== 'up' || busy" @click="$emit('move', 'up')">上楼</button>
      <button :disabled="activeVerticalExit !== 'down' || busy" @click="$emit('move', 'down')">下楼</button>
    </div>

    <div class="action-stack">
      <button :disabled="!activeRoomItemId || busy" @click="$emit('take', activeRoomItemId)">
        {{ activeRoomItemName ? `拾取：${activeRoomItemName}` : '拾取当前位置物品' }}
      </button>
      <button :disabled="!selectedInventoryId || busy" @click="$emit('drop', selectedInventoryId)">丢弃选中物品</button>
      <button :disabled="!hasMagicCookie || busy" @click="$emit('eat-cookie')">使用魔法饼干</button>
      <button :disabled="busy" @click="$emit('save')">保存游戏</button>
      <button :disabled="busy" @click="$emit('load')">读取存档</button>
      <button :disabled="busy" @click="$emit('back')">返回上个房间</button>
      <button :disabled="busy" @click="$emit('toggle-map')">切换地图</button>
      <button :disabled="busy" @click="$emit('help')">帮助</button>
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
    inventory: {
      type: Array,
      default: () => []
    },
    activeRoomItemId: {
      type: [String, Number],
      default: ''
    },
    activeRoomItemName: {
      type: String,
      default: ''
    },
    activeVerticalExit: {
      type: String,
      default: ''
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
  grid-template-areas:
    ". north ."
    "west center east"
    ". south .";
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  margin-bottom: 14px;
}

.stair-actions {
  display: grid;
  gap: 8px;
  grid-template-columns: repeat(2, 1fr);
  margin-bottom: 14px;
}

.north-action {
  grid-area: north;
}

.west-action {
  grid-area: west;
}

.east-action {
  grid-area: east;
}

.south-action {
  grid-area: south;
}

.center-action {
  background: #7a5a32;
  grid-area: center;
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
