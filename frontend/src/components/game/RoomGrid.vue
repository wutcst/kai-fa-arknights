<template>
  <section class="room-grid-card">
    <div class="grid-header">
      <span class="eyebrow">TOP-DOWN ROOM</span>
      <strong>{{ roomName || '未知房间' }}</strong>
    </div>

    <div class="room-grid" :class="{ bump: isBumping }" ref="grid">
      <div
        v-for="cell in cells"
        :key="cell.key"
        class="room-cell"
        :class="cell.classes"
      >
        <span v-if="cell.label" class="cell-label">{{ cell.label }}</span>
      </div>
      <div
        class="player-avatar"
        :class="{ 'facing-left': playerFacing === 'west' }"
        :style="playerStyle"
        aria-label="玩家当前位置"
      >
        <video
          ref="playerVideo"
          class="player-video"
          :key="playerAnimation"
          :src="playerVideoSrc"
          :loop="isLoopingAnimation"
          autoplay
          muted
          playsinline
          @ended="handleAnimationEnded"
        />
      </div>
    </div>

    <div v-if="activeRoomItem" class="pickup-hint">
      可拾取：<strong>{{ activeRoomItem.name }}</strong>
      <span>靠近物品后按“拾取附近物品”放入背包</span>
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

<script>
import checkoutVideo from '@/assets/characters/维什戴尔-绝对主角-checkout.webm';
import moveVideo from '@/assets/characters/维什戴尔-绝对主角-Move.webm';
import operationVideo from '@/assets/characters/维什戴尔-绝对主角-Operation.webm';
import sitVideo from '@/assets/characters/维什戴尔-绝对主角-Sit.webm';
import sleepVideo from '@/assets/characters/维什戴尔-绝对主角-Sleep.webm';

const GRID_SIZE = 9;
const CENTER = 4;
const MIN_POSITION = 0.5;
const MAX_POSITION = GRID_SIZE - 1.5;
const INTERACT_DISTANCE = 0.78;
const BASE_STEP_PER_FRAME = 0.032;
const BUTTON_NUDGE_FRAMES = 12;
const SLEEP_DELAY_MS = 8000;
const OPERATION_FALLBACK_MS = 1800;
const CHECKOUT_FALLBACK_MS = 3500;

export default {
  name: 'RoomGrid',
  props: {
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
  },
  data() {
    return {
      playerX: CENTER,
      playerY: CENTER,
      isBumping: false,
      bumpTimer: null,
      activeDirections: new Set(),
      animationFrame: null,
      lastFrameTime: 0,
      playerAnimation: 'sit',
      idleTimer: null,
      operationTimer: null,
      checkoutTimer: null,
      checkoutResolver: null,
      playerFacing: 'east'
    };
  },
  computed: {
    playerVideos() {
      return {
        checkout: checkoutVideo,
        move: moveVideo,
        operation: operationVideo,
        sit: sitVideo,
        sleep: sleepVideo
      };
    },
    playerVideoSrc() {
      return this.playerVideos[this.playerAnimation] || sitVideo;
    },
    isLoopingAnimation() {
      return !['operation', 'checkout'].includes(this.playerAnimation);
    },
    itemPositions() {
      return [
        [2, 2],
        [6, 2],
        [2, 6],
        [6, 6],
        [3, 5],
        [5, 3]
      ];
    },
    stairPositions() {
      return {
        up: [7, 3],
        down: [1, 5]
      };
    },
    visibleItems() {
      return this.items.slice(0, 6);
    },
    activeRoomItem() {
      const itemIndex = this.itemPositions.findIndex(([itemX, itemY]) => (
        this.distanceTo(itemX, itemY) <= INTERACT_DISTANCE
      ));
      return itemIndex >= 0 ? this.visibleItems[itemIndex] : null;
    },
    hasVerticalExit() {
      return this.hasExit('up') || this.hasExit('down');
    },
    activeVerticalExit() {
      return ['up', 'down'].find((direction) => {
        const [stairX, stairY] = this.stairPositions[direction];
        return this.hasExit(direction) && this.distanceTo(stairX, stairY) <= INTERACT_DISTANCE;
      }) || '';
    },
    playerPosition() {
      return {
        row: Number(this.playerY.toFixed(3)),
        col: Number(this.playerX.toFixed(3))
      };
    },
    playerStyle() {
      return {
        left: `${((this.playerX + 0.5) / GRID_SIZE) * 100}%`,
        top: `${((this.playerY - 0.6) / GRID_SIZE) * 100}%`
      };
    },
    playerCell() {
      return {
        row: this.coordinateToCell(this.playerY),
        col: this.coordinateToCell(this.playerX)
      };
    },
    cells() {
      return Array.from({ length: GRID_SIZE * GRID_SIZE }, (_, index) => {
        const row = Math.floor(index / GRID_SIZE);
        const col = index % GRID_SIZE;
        const classes = [];
        let label = '';

        if (row === 0 || row === GRID_SIZE - 1 || col === 0 || col === GRID_SIZE - 1) {
          classes.push('wall');
        } else {
          classes.push('floor');
        }

        if (row === this.playerCell.row && col === this.playerCell.col) {
          classes.push('player-cell');
        }

        const direction = this.exitAt(row, col);
        if (direction) {
          classes.push('door', `door-${direction}`);
          if (this.isNearDoor(direction)) {
            classes.push('near-active');
          }
          label = this.exitLabel(direction);
        }

        const itemIndex = this.itemPositions.findIndex(([itemCol, itemRow]) => itemCol === col && itemRow === row);
        if (itemIndex >= 0 && this.visibleItems[itemIndex] && !label) {
          classes.push('item');
          if (this.activeRoomItem?.id === this.visibleItems[itemIndex].id) {
            classes.push('near-active');
          }
          label = this.itemLabel(this.visibleItems[itemIndex]);
        }

        const stairDirection = this.stairAt(row, col);
        if (stairDirection && !label) {
          classes.push('stair', `stair-${stairDirection}`);
          if (this.activeVerticalExit === stairDirection) {
            classes.push('near-active');
          }
          label = stairDirection === 'up' ? '⇧' : '⇩';
        }

        return {
          key: `${row}-${col}`,
          classes,
          label
        };
      });
    }
  },
  watch: {
    activeRoomItem: {
      immediate: true,
      handler(item) {
        this.$emit('active-item-change', item?.id || '');
        this.$emit('active-item-name-change', item?.name || '');
      }
    },
    activeVerticalExit: {
      immediate: true,
      handler(direction) {
        this.$emit('active-vertical-exit-change', direction);
      }
    },
    playerPosition: {
      immediate: true,
      handler(position) {
        this.$emit('player-position-change', position);
      }
    },
    playerGridPosition: {
      deep: true,
      immediate: true,
      handler(position) {
        if (!position) return;
        const nextY = this.clampPosition(position.row);
        const nextX = this.clampPosition(position.col);
        if (Math.abs(nextY - this.playerY) > 0.001 || Math.abs(nextX - this.playerX) > 0.001) {
          this.playerY = nextY;
          this.playerX = nextX;
        }
      }
    }
  },
  methods: {
    coordinateToCell(position) {
      if (position <= MIN_POSITION) return 0;
      if (position >= MAX_POSITION) return GRID_SIZE - 1;
      return Math.round(position);
    },
    clampPosition(value) {
      const number = Number(value);
      if (!Number.isFinite(number)) return CENTER;
      return Math.max(MIN_POSITION, Math.min(MAX_POSITION, number));
    },
    hasExit(direction) {
      return this.exits.includes(direction);
    },
    exitAt(row, col) {
      if (row === 0 && col === CENTER && this.hasExit('north')) return 'north';
      if (row === GRID_SIZE - 1 && col === CENTER && this.hasExit('south')) return 'south';
      if (row === CENTER && col === 0 && this.hasExit('west')) return 'west';
      if (row === CENTER && col === GRID_SIZE - 1 && this.hasExit('east')) return 'east';
      return '';
    },
    exitLabel(direction) {
      const labels = {
        north: 'N',
        south: 'S',
        west: 'W',
        east: 'E'
      };
      return labels[direction] || '';
    },
    stairAt(row, col) {
      return ['up', 'down'].find((direction) => {
        const [stairCol, stairRow] = this.stairPositions[direction];
        return this.hasExit(direction) && stairRow === row && stairCol === col;
      }) || '';
    },
    itemLabel(item) {
      if (item.id === 'magic_cookie') return '🍪';
      if ((item.name || '').includes('钥匙')) return '⚿';
      if ((item.name || '').includes('金币')) return '◎';
      return '✦';
    },
    distanceTo(targetX, targetY) {
      return Math.hypot(this.playerX - targetX, this.playerY - targetY);
    },
    isNearDoor(direction) {
      const doorPositions = {
        north: [CENTER, MIN_POSITION],
        south: [CENTER, MAX_POSITION],
        west: [MIN_POSITION, CENTER],
        east: [MAX_POSITION, CENTER]
      };
      const [doorX, doorY] = doorPositions[direction] || [CENTER, CENTER];
      return this.hasExit(direction) && this.distanceTo(doorX, doorY) <= INTERACT_DISTANCE;
    },
    bumpWall() {
      if (this.bumpTimer) {
        clearTimeout(this.bumpTimer);
      }
      this.isBumping = true;
      this.bumpTimer = setTimeout(() => {
        this.isBumping = false;
      }, 140);
    },
    normalizedVector() {
      let x = 0;
      let y = 0;
      if (this.activeDirections.has('north')) y -= 1;
      if (this.activeDirections.has('south')) y += 1;
      if (this.activeDirections.has('west')) x -= 1;
      if (this.activeDirections.has('east')) x += 1;
      this.updateFacing(x);
      const length = Math.hypot(x, y);
      if (!length) return { x: 0, y: 0 };
      return { x: x / length, y: y / length };
    },
    movementStep(deltaMs) {
      const speed = Math.max(0.2, Number(this.moveSpeed) || 0.5);
      return BASE_STEP_PER_FRAME * speed * (deltaMs / 16.67);
    },
    startMovementLoop() {
      if (this.animationFrame) return;
      this.setPlayerAnimation('move');
      this.lastFrameTime = performance.now();
      this.animationFrame = requestAnimationFrame(this.updateMovement);
    },
    stopMovementLoop() {
      if (this.animationFrame) {
        cancelAnimationFrame(this.animationFrame);
        this.animationFrame = null;
      }
      this.lastFrameTime = 0;
      if (!this.activeDirections.size && this.playerAnimation === 'move') {
        this.setPlayerAnimation('sit');
      }
    },
    updateMovement(timestamp) {
      const deltaMs = Math.min(40, timestamp - this.lastFrameTime || 16.67);
      this.lastFrameTime = timestamp;

      if (!this.activeDirections.size) {
        this.stopMovementLoop();
        return;
      }

      const vector = this.normalizedVector();
      this.applyMovement(vector.x * this.movementStep(deltaMs), vector.y * this.movementStep(deltaMs));
      this.animationFrame = requestAnimationFrame(this.updateMovement);
    },
    applyMovement(deltaX, deltaY) {
      if (!deltaX && !deltaY) return;
      this.updateFacing(deltaX);

      const nextX = this.clampPosition(this.playerX + deltaX);
      const nextY = this.clampPosition(this.playerY + deltaY);
      const blockedX = nextX === this.playerX && deltaX !== 0;
      const blockedY = nextY === this.playerY && deltaY !== 0;

      this.playerX = nextX;
      this.playerY = nextY;

      const exitDirection = this.exitDirectionForMovement(deltaX, deltaY);
      if (exitDirection) {
        this.activeDirections.clear();
        this.stopMovementLoop();
        this.$emit('move', exitDirection);
        return;
      }

      if ((blockedX || blockedY) && !this.isNearAnyDoor()) {
        this.bumpWall();
      }
    },
    exitDirectionForMovement(deltaX, deltaY) {
      if (deltaY < 0 && this.isStandingOnDoor('north')) {
        return 'north';
      }
      if (deltaY > 0 && this.isStandingOnDoor('south')) {
        return 'south';
      }
      if (deltaX < 0 && this.isStandingOnDoor('west')) {
        return 'west';
      }
      if (deltaX > 0 && this.isStandingOnDoor('east')) {
        return 'east';
      }
      return '';
    },
    isStandingOnDoor(direction) {
      if (!this.hasExit(direction)) return false;
      const doorCells = {
        north: { row: 0, col: CENTER },
        south: { row: GRID_SIZE - 1, col: CENTER },
        west: { row: CENTER, col: 0 },
        east: { row: CENTER, col: GRID_SIZE - 1 }
      };
      const doorCell = doorCells[direction];
      return this.playerCell.row === doorCell.row && this.playerCell.col === doorCell.col;
    },
    isNearAnyDoor() {
      return ['north', 'south', 'west', 'east'].some(direction => this.isNearDoor(direction));
    },
    directionFromKey(key) {
      const keyMap = {
        ArrowUp: 'north',
        w: 'north',
        W: 'north',
        ArrowDown: 'south',
        s: 'south',
        S: 'south',
        ArrowLeft: 'west',
        a: 'west',
        A: 'west',
        ArrowRight: 'east',
        d: 'east',
        D: 'east'
      };
      return keyMap[key] || '';
    },
    tryMoveByKey(event) {
      const direction = this.directionFromKey(event.key);
      if (!direction) return;
      event.preventDefault();
      this.activeDirections.add(direction);
      this.startMovementLoop();
    },
    stopMoveByKey(event) {
      const direction = this.directionFromKey(event.key);
      if (!direction) return;
      this.activeDirections.delete(direction);
    },
    nudge(direction, frames = BUTTON_NUDGE_FRAMES) {
      const vectors = {
        north: [0, -1],
        south: [0, 1],
        west: [-1, 0],
        east: [1, 0]
      };
      const [x, y] = vectors[direction] || [0, 0];
      if (!x && !y) return;
      this.updateFacing(x);

      let remaining = frames;
      const tick = () => {
        this.setPlayerAnimation('move');
        this.applyMovement(x * this.movementStep(16.67), y * this.movementStep(16.67));
        remaining -= 1;
        if (remaining > 0) {
          requestAnimationFrame(tick);
        } else if (!this.activeDirections.size && this.playerAnimation === 'move') {
          this.setPlayerAnimation('sit');
        }
      };
      requestAnimationFrame(tick);
    },
    resetPosition(entryDirection) {
      const upEntry = this.stairPositions.down;
      const downEntry = this.stairPositions.up;
      const entryPositions = {
        north: [CENTER, GRID_SIZE - 2],
        south: [CENTER, 1],
        west: [GRID_SIZE - 2, CENTER],
        east: [1, CENTER],
        up: [upEntry[0], upEntry[1]],
        down: [downEntry[0], downEntry[1]]
      };
      const [x, y] = entryPositions[entryDirection] || [CENTER, CENTER];
      this.activeDirections.clear();
      this.stopMovementLoop();
      this.playerX = this.clampPosition(x);
      this.playerY = this.clampPosition(y);
      this.setPlayerAnimation('sit');
      this.bumpWall();
    },
    playOperation() {
      if (this.playerAnimation === 'checkout') return;
      if (this.operationTimer) {
        clearTimeout(this.operationTimer);
      }
      this.setPlayerAnimation('operation');
      this.operationTimer = setTimeout(() => {
        if (this.playerAnimation === 'operation') {
          this.setPlayerAnimation('sit');
        }
      }, OPERATION_FALLBACK_MS);
    },
    playCheckout() {
      this.activeDirections.clear();
      this.stopMovementLoop();
      this.clearIdleTimer();
      if (this.operationTimer) {
        clearTimeout(this.operationTimer);
        this.operationTimer = null;
      }
      this.setPlayerAnimation('checkout');
      return new Promise((resolve) => {
        this.checkoutResolver = resolve;
        this.checkoutTimer = setTimeout(() => {
          this.finishCheckout();
        }, CHECKOUT_FALLBACK_MS);
      });
    },
    setPlayerAnimation(animation) {
      if (this.playerAnimation === 'checkout' && animation !== 'checkout') return;
      this.playerAnimation = animation;
      this.clearIdleTimer();
      if (animation === 'sit') {
        this.scheduleSleep();
      }
      this.$nextTick(() => {
        const video = this.$refs.playerVideo;
        if (video) {
          video.currentTime = 0;
          video.play?.().catch(() => {});
        }
      });
    },
    scheduleSleep() {
      this.clearIdleTimer();
      this.idleTimer = setTimeout(() => {
        if (this.playerAnimation === 'sit' && !this.activeDirections.size) {
          this.playerAnimation = 'sleep';
        }
      }, SLEEP_DELAY_MS);
    },
    clearIdleTimer() {
      if (this.idleTimer) {
        clearTimeout(this.idleTimer);
        this.idleTimer = null;
      }
    },
    handleAnimationEnded() {
      if (this.playerAnimation === 'operation') {
        if (this.operationTimer) {
          clearTimeout(this.operationTimer);
          this.operationTimer = null;
        }
        this.setPlayerAnimation('sit');
      }
      if (this.playerAnimation === 'checkout') {
        this.finishCheckout();
      }
    },
    finishCheckout() {
      if (this.checkoutTimer) {
        clearTimeout(this.checkoutTimer);
        this.checkoutTimer = null;
      }
      const resolve = this.checkoutResolver;
      this.checkoutResolver = null;
      if (resolve) {
        resolve();
      }
    },
    updateFacing(deltaX) {
      if (deltaX < 0) {
        this.playerFacing = 'west';
      }
      if (deltaX > 0) {
        this.playerFacing = 'east';
      }
    }
  },
  mounted() {
    window.addEventListener('keyup', this.stopMoveByKey, true);
    this.scheduleSleep();
  },
  beforeUnmount() {
    window.removeEventListener('keyup', this.stopMoveByKey, true);
    if (this.bumpTimer) {
      clearTimeout(this.bumpTimer);
    }
    if (this.operationTimer) {
      clearTimeout(this.operationTimer);
    }
    if (this.checkoutTimer) {
      clearTimeout(this.checkoutTimer);
    }
    this.clearIdleTimer();
    this.stopMovementLoop();
  }
};
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

.player-avatar {
  align-items: center;
  display: flex;
  height: calc((100% - 64px) / 9 * 4.05);
  justify-content: center;
  min-height: 108px;
  min-width: 108px;
  pointer-events: none;
  position: absolute;
  transform: translate(-50%, -50%);
  transform-origin: center center;
  width: calc((100% - 64px) / 9 * 4.05);
  z-index: 3;
}

.player-avatar.facing-left {
  transform: translate(-50%, -50%) scaleX(-1);
}

.player-video {
  display: block;
  height: 100%;
  object-fit: contain;
  width: 100%;
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

.cell-label {
  font-size: clamp(12px, 2vw, 20px);
  line-height: 1;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.8);
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
