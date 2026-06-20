<template>
  <section class="room-grid-card">
    <div class="grid-header">
      <span class="eyebrow">TOP-DOWN ROOM</span>
      <strong>{{ roomName || '未知房间' }}</strong>
    </div>

    <div class="room-grid" :class="{ bump: isBumping }" ref="grid">
      <RoomCell
        v-for="cell in cells"
        :key="cell.key"
        :cell="cell"
      />
      <PlayerAvatar
        :animation="playerAnimation"
        :facing="playerFacing"
        :player-style="playerStyle"
        :looping="isLoopingAnimation"
        @ended="handleAnimationEnded"
      />
    </div>

    <div v-if="activeRoomItem" class="pickup-hint">
      当前格物品：<strong>{{ activeRoomItem.name }}</strong>
      <span>站到物品所在格后按“拾取当前格物品”放入背包</span>
    </div>
    <div v-if="hasVerticalExit" class="floor-hint">
      楼层出口：
      <strong v-if="hasExit('up')">右上楼梯上楼</strong>
      <strong v-if="hasExit('down')">左下楼梯下楼</strong>
      <span v-if="activeVerticalExit">当前可{{ activeVerticalExit === 'up' ? '上楼' : '下楼' }}</span>
    </div>
    <p class="room-description">{{ description || '观察周围环境，选择下一步行动。' }}</p>
  </section>
</template>

<script setup>
/* global defineProps, defineEmits, defineExpose */
import { onBeforeUnmount, onMounted, toRefs, watch } from 'vue';
import PlayerAvatar from '@/components/game/PlayerAvatar.vue';
import RoomCell from '@/components/game/RoomCell.vue';
import { useGameSounds } from '@/composables/useGameSounds';
import { usePlayerAnimation } from '@/composables/usePlayerAnimation';
import { usePlayerMovement } from '@/composables/usePlayerMovement';
import { useRoomInteraction } from '@/composables/useRoomInteraction';

const props = defineProps({
  roomName: {
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
  playerGridPosition: {
    type: Object,
    default: null
  },
  moveSpeed: {
    type: Number,
    default: 0.5
  }
});

const emit = defineEmits([
  'move',
  'active-item-change',
  'active-item-name-change',
  'active-vertical-exit-change',
  'player-position-change'
]);

const { exits, items, moveSpeed, playerGridPosition } = toRefs(props);
const { startWalkingSound, stopWalkingSound } = useGameSounds();

let movement;

const {
  playerAnimation,
  playerFacing,
  isLoopingAnimation,
  setPlayerAnimation,
  scheduleSleep,
  clearMoveEndTimer,
  scheduleSitAfterMove,
  playOperation,
  playCheckout: playCheckoutAnimation,
  handleAnimationEnded,
  updateFacing,
  cleanupAnimationTimers
} = usePlayerAnimation({
  hasActiveDirections: () => movement?.activeDirections.value.size > 0
});

movement = usePlayerMovement({
  moveSpeed,
  isNearAnyDoor: () => isNearAnyDoor(),
  exitDirectionForMovement: (deltaX, deltaY) => exitDirectionForMovement(deltaX, deltaY),
  onMoveToExit: (direction) => emit('move', direction),
  onFacingChange: updateFacing,
  onStartMoving: () => {
    clearMoveEndTimer();
    setPlayerAnimation('move');
    startWalkingSound();
  },
  onStopMoving: () => {
    stopWalkingSound();
    if (playerAnimation.value === 'move') {
      scheduleSitAfterMove();
    }
  }
});

const {
  playerX,
  playerY,
  activeDirections,
  isBumping,
  playerPosition,
  playerStyle,
  setPlayerPosition,
  tryMoveByKey,
  stopMoveByKey,
  nudge,
  resetPosition: resetMovementPosition,
  stopMovementLoop,
  cleanupMovement
} = movement;

const {
  cells,
  activeRoomItem,
  hasVerticalExit,
  activeVerticalExit,
  hasExit,
  isNearAnyDoor,
  exitDirectionForMovement
} = useRoomInteraction({
  exits,
  items,
  playerX,
  playerY
});

const resetPosition = (entryDirection) => {
  resetMovementPosition(entryDirection, () => {
    clearMoveEndTimer();
    setPlayerAnimation('sit');
  });
};

const playCheckout = () => playCheckoutAnimation({
  clearDirections: () => activeDirections.value.clear(),
  stopMovement: stopMovementLoop
});

watch(activeRoomItem, (item) => {
  emit('active-item-change', item?.id || '');
  emit('active-item-name-change', item?.name || '');
}, { immediate: true });

watch(activeVerticalExit, (direction) => {
  emit('active-vertical-exit-change', direction);
}, { immediate: true });

watch(playerPosition, (position) => {
  emit('player-position-change', position);
}, { immediate: true });

watch(playerGridPosition, (position) => {
  if (!position) return;
  setPlayerPosition(position.row, position.col);
}, { deep: true, immediate: true });

onMounted(() => {
  window.addEventListener('keyup', stopMoveByKey, true);
  scheduleSleep();
});

onBeforeUnmount(() => {
  window.removeEventListener('keyup', stopMoveByKey, true);
  stopWalkingSound();
  cleanupMovement();
  cleanupAnimationTimers();
});

defineExpose({
  tryMoveByKey,
  resetPosition,
  nudge,
  playOperation,
  playCheckout
});
</script>

<style scoped>
.room-grid-card {
  background: linear-gradient(145deg, rgba(25, 28, 32, 0.96), rgba(49, 42, 34, 0.96));
  border: 2px solid #7a5a32;
  border-radius: 18px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.35);
  color: #f7ead2;
  padding: 18px;
}

.grid-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 16px;
  margin-bottom: 14px;
}

.eyebrow {
  color: #d7a84d;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  letter-spacing: 2px;
}

.grid-header strong {
  font-size: 22px;
}

.room-grid {
  display: grid;
  grid-template-columns: repeat(9, minmax(28px, 1fr));
  grid-template-rows: repeat(9, minmax(0, 1fr));
  gap: 4px;
  aspect-ratio: 1;
  background: #151719;
  border: 6px solid #2a1e16;
  border-radius: 14px;
  padding: 8px;
  position: relative;
  transition: transform 0.18s ease;
}

.room-grid.bump {
  transform: scale(0.995);
}

.room-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  aspect-ratio: 1;
  border-radius: 6px;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
  transition: transform 0.16s ease, filter 0.16s ease, background 0.16s ease;
}

.floor {
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.04) 25%, transparent 25%),
    #3b342b;
  border: 1px solid rgba(255, 255, 255, 0.05);
}

.player-cell {
  box-shadow:
    inset 0 0 0 2px rgba(247, 214, 123, 0.5),
    0 0 18px rgba(247, 214, 123, 0.28);
  filter: brightness(1.12);
}

.wall {
  background: linear-gradient(135deg, #171717, #453321);
  border: 1px solid #7a5a32;
  box-shadow: inset 0 0 0 1px rgba(0, 0, 0, 0.45);
}

.door {
  background: linear-gradient(180deg, #c68b2f, #5b3516);
  border: 1px solid #ffd280;
  color: #fff3cf;
  font-weight: 800;
}

.item {
  background: radial-gradient(circle, #f1d27a 0 28%, #634520 29% 70%, #2a1e16 71%);
  border: 1px solid #d7a84d;
}

.stair {
  background:
    repeating-linear-gradient(135deg, rgba(255, 255, 255, 0.16) 0 4px, transparent 4px 8px),
    linear-gradient(180deg, #315d4a, #182b24);
  border: 1px solid #8fe2b4;
  color: #dff5e8;
  font-weight: 900;
}

.near-active {
  box-shadow:
    0 0 0 3px rgba(247, 214, 123, 0.22),
    0 0 28px rgba(247, 214, 123, 0.68);
  filter: brightness(1.18);
  transform: scale(1.04);
}

.stair.near-active {
  box-shadow:
    0 0 0 3px rgba(143, 226, 180, 0.22),
    0 0 28px rgba(143, 226, 180, 0.62);
}

.room-description {
  margin: 14px 0 0;
  color: #d9c8aa;
  line-height: 1.6;
  text-align: left;
}

.pickup-hint {
  align-items: center;
  background: rgba(247, 214, 123, 0.12);
  border: 1px solid rgba(247, 214, 123, 0.42);
  border-radius: 12px;
  color: #f7ead2;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
  padding: 10px 12px;
}

.floor-hint {
  align-items: center;
  background: rgba(88, 185, 133, 0.12);
  border: 1px solid rgba(88, 185, 133, 0.38);
  border-radius: 12px;
  color: #dff5e8;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
  padding: 10px 12px;
}

.floor-hint strong {
  color: #8fe2b4;
}

.pickup-hint strong {
  color: #ffe08a;
}

.pickup-hint span {
  color: #cdbb9e;
  font-size: 13px;
}

@media (max-width: 760px) {
  .room-grid-card {
    padding: 12px;
  }

  .grid-header {
    align-items: flex-start;
    flex-direction: column;
    gap: 4px;
  }
}
</style>
