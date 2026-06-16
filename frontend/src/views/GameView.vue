<template>
  <main class="game-view">
    <header class="game-topbar">
      <div>
        <span class="game-kicker">ZUUL EXPEDITION</span>
        <h1>俯视角房间探索</h1>
      </div>
      <div class="top-actions">
        <span class="gold-pill"><img src="@/assets/items/Lungmen_Dollars.png" class="gold-icon" alt="金币">{{ userGold }}</span>
        <button @click="playCheckoutThen('back-to-menu')">返回主界面</button>
        <button class="logout" @click="playCheckoutThen('logout')">退出登录</button>
      </div>
    </header>

    <GameStatusBar
      :room-name="roomName"
      :floor-label="floorLabel"
      :username="username"
      :player-weight="playerWeight"
      :player-max-weight="playerMaxWeight"
      :items="items"
      :is-error="isError"
    />

    <div class="game-layout">
      <section class="main-play-area">
        <RoomGrid
          ref="roomGrid"
          :room-name="roomName"
          :description="description"
          :exits="exits"
          :items="items"
          :player-grid-position="playerGridPosition"
          :move-speed="moveSpeed"
          @move="$emit('move', $event)"
          @active-item-change="activeRoomItemId = $event"
          @active-item-name-change="activeRoomItemName = $event"
          @active-vertical-exit-change="activeVerticalExit = $event"
          @player-position-change="$emit('player-position-change', $event)"
        />

        <InventoryHotbar
          class="bottom-hotbar"
          :inventory="inventory"
          :selected-id="selectedInventoryId"
          @select="$emit('select-inventory', $event)"
        />
      </section>

      <aside class="right-rail">
        <ActionPanel
          :exits="exits"
          :inventory="inventory"
          :active-room-item-id="activeRoomItemId"
          :active-room-item-name="activeRoomItemName"
          :active-vertical-exit="activeVerticalExit"
          :selected-inventory-id="selectedInventoryId"
          :busy="busy"
          @move="handleActionMove"
          @look="playOperationThen('look')"
          @take="playOperationThen('take', $event)"
          @drop="playOperationThen('drop', $event)"
          @eat-cookie="playOperationThen('eat-cookie')"
          @save="playOperationThen('save')"
          @load="playOperationThen('load')"
          @back="playOperationThen('back')"
          @toggle-map="playOperationThen('toggle-map')"
          @help="playOperationThen('help')"
          @open-ability="playOperationThen('open-ability')"
          @settle="$emit('settle')"
        />

        <InventoryPanel
          :inventory="inventory"
          :selected-id="selectedInventoryId"
          :player-weight="playerWeight"
          :player-max-weight="playerMaxWeight"
          @select="$emit('select-inventory', $event)"
        />
      </aside>
    </div>

    <MessageLog :messages="messages" />
  </main>
</template>

<script>
import RoomGrid from '@/components/game/RoomGrid.vue';
import GameStatusBar from '@/components/game/GameStatusBar.vue';
import ActionPanel from '@/components/game/ActionPanel.vue';
import InventoryPanel from '@/components/game/InventoryPanel.vue';
import InventoryHotbar from '@/components/game/InventoryHotbar.vue';
import MessageLog from '@/components/game/MessageLog.vue';

export default {
  name: 'GameView',
  components: {
    RoomGrid,
    GameStatusBar,
    ActionPanel,
    InventoryPanel,
    InventoryHotbar,
    MessageLog
  },
  props: {
    username: {
      type: String,
      default: ''
    },
    userGold: {
      type: Number,
      default: 0
    },
    roomName: {
      type: String,
      default: ''
    },
    roomId: {
      type: String,
      default: ''
    },
    description: {
      type: String,
      default: ''
    },
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
    playerWeight: {
      type: Number,
      default: 0
    },
    playerMaxWeight: {
      type: Number,
      default: 20
    },
    selectedInventoryId: {
      type: [String, Number],
      default: ''
    },
    playerGridPosition: {
      type: Object,
      default: null
    },
    moveSpeed: {
      type: Number,
      default: 0.5
    },
    messages: {
      type: Array,
      default: () => []
    },
    isError: {
      type: Boolean,
      default: false
    },
    busy: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      activeRoomItemId: '',
      activeRoomItemName: '',
      activeVerticalExit: ''
    };
  },
  computed: {
    floorLabel() {
      const floorByRoomId = {
        theater_lobby: '设施一层',
        theater_classroom_101: '设施一层',
        theater_classroom_102: '设施一层',
        theater_stairway_1f: '设施一层',
        theater_classroom_201: '设施二层',
        theater_classroom_202: '设施二层',
        theater_office: '设施二层',
        theater_stairway_2f: '设施二层',
        theater_classroom_301: '设施三层',
        theater_classroom_302: '设施三层',
        theater_lab: '设施三层',
        theater_stairway_3f: '设施三层'
      };
      if (floorByRoomId[this.roomId]) {
        return floorByRoomId[this.roomId];
      }

      const source = `${this.roomName}`.toLowerCase();
      if (source.includes('101') || source.includes('102') || source.includes('一楼') || source.includes('一层')) {
        return '设施一层';
      }
      if (source.includes('201') || source.includes('202') || source.includes('二楼') || source.includes('二层')) {
        return '设施二层';
      }
      if (source.includes('301') || source.includes('302') || source.includes('三楼') || source.includes('三层')) {
        return '设施三层';
      }
      return '设施外围';
    }
  },
  methods: {
    handleActionMove(direction) {
      if (direction === 'up' || direction === 'down') {
        this.$refs.roomGrid?.playOperation();
        this.$emit('move', direction);
        return;
      }
      this.$refs.roomGrid?.nudge(direction);
    },
    playOperationThen(eventName, payload) {
      this.$refs.roomGrid?.playOperation();
      if (payload === undefined) {
        this.$emit(eventName);
        return;
      }
      this.$emit(eventName, payload);
    },
    async playCheckoutThen(eventName) {
      await this.$refs.roomGrid?.playCheckout();
      this.$emit(eventName);
    },
    tryMoveByKey(event) {
      this.$refs.roomGrid?.tryMoveByKey(event);
    },
    resetPosition(entryDirection) {
      this.$refs.roomGrid?.resetPosition(entryDirection);
    },
    playOperation() {
      this.$refs.roomGrid?.playOperation();
    },
    playCheckout() {
      return this.$refs.roomGrid?.playCheckout();
    }
  }
};
</script>

<style scoped>
.game-view {
  min-height: 100vh;
  padding: 18px;
  background:
    radial-gradient(circle at top left, rgba(215, 168, 77, 0.22), transparent 36%),
    radial-gradient(circle at bottom right, rgba(38, 117, 91, 0.28), transparent 34%),
    linear-gradient(135deg, #111614 0%, #241c18 54%, #101415 100%);
  color: #f7ead2;
  text-align: left;
}

.game-topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
  margin: 0 auto 14px;
  max-width: 1480px;
}

.game-kicker {
  color: #d7a84d;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  letter-spacing: 2px;
}

h1 {
  color: #f7ead2;
  font-size: clamp(26px, 4vw, 44px);
  letter-spacing: 1px;
  margin: 4px 0 0;
  padding: 0;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.55);
}

.top-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
}

.gold-icon {
  width: 20px;
  height: 20px;
  vertical-align: middle;
  margin-right: 6px;
}

.gold-pill,
button {
  border: 1px solid rgba(247, 214, 123, 0.45);
  border-radius: 999px;
  background: rgba(18, 22, 22, 0.78);
  color: #f7ead2;
  font-weight: 700;
  padding: 10px 14px;
}

button {
  cursor: pointer;
  transition: transform 0.14s ease, filter 0.14s ease;
}

button:hover {
  filter: brightness(1.1);
  transform: translateY(-1px);
}

.logout {
  border-color: rgba(225, 104, 79, 0.6);
}

.game-layout {
  display: grid;
  grid-template-columns: minmax(360px, 1fr) 360px;
  gap: 16px;
  margin: 16px auto;
  max-width: 1480px;
}

.main-play-area {
  display: grid;
  gap: 14px;
  min-width: 0;
}

.bottom-hotbar {
  justify-self: center;
  width: min(100%, 760px);
}

.right-rail {
  display: grid;
  align-content: start;
  gap: 16px;
}

.game-view > .status-shell,
.game-view > .message-log {
  margin-left: auto;
  margin-right: auto;
  max-width: 1480px;
}

@media (max-width: 1050px) {
  .game-layout {
    grid-template-columns: 1fr;
  }

  .right-rail {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 700px) {
  .game-view {
    padding: 12px;
  }

  .game-topbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .top-actions {
    justify-content: flex-start;
  }

  .right-rail {
    grid-template-columns: 1fr;
  }
}
</style>
