<template>
  <div class="room-view" tabindex="0" ref="roomView">
    <div class="room-container">

      <!-- 第一人称视角：房间四面墙 -->
      <div class="room-scene">
        <div class="wall wall-top" :class="{ 'has-exit': hasExit('north') }">
          <span v-if="hasExit('north')" class="exit-marker">⬆️ 出口</span>
          <span v-else class="wall-text">北墙</span>
        </div>

        <div class="wall-row">
          <div class="wall wall-left" :class="{ 'has-exit': hasExit('west') }">
            <span v-if="hasExit('west')" class="exit-marker">⬅️ 出口</span>
            <span v-else class="wall-text">西墙</span>
          </div>

          <div class="room-center" ref="roomCenter">
            <p class="room-desc">{{ description }}</p>
            <div
              class="player-icon"
              :style="playerStyle"
              :class="{ transitioning: isAnimating }"
              @transitionend="onMoveComplete"
            >🧑</div>
          </div>

          <div class="wall wall-right" :class="{ 'has-exit': hasExit('east') }">
            <span v-if="hasExit('east')" class="exit-marker">➡️ 出口</span>
            <span v-else class="wall-text">东墙</span>
          </div>
        </div>

        <div class="wall wall-bottom" :class="{ 'has-exit': hasExit('south') }">
          <span v-if="hasExit('south')" class="exit-marker">⬇️ 出口</span>
          <span v-else class="wall-text">南墙</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
/**
 * 第一人称视角房间视图组件.
 * 展示房间四面墙壁及出口信息，玩家可通过WASD控制小人移动.
 */
export default {
  name: 'RoomView',
  props: {
    roomName: String,
    description: String,
    exits: {
      type: Array,
      default: () => []
    },
    canMove: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      playerX: 50,  // 百分比位置
      playerY: 50,
      isAnimating: false,
      isMoving: false,
      moveDirection: null,
      pendingDirection: null  // 待处理的移动方向
    };
  },
  computed: {
    playerStyle() {
      return {
        left: this.playerX + '%',
        top: this.playerY + '%',
        transition: this.isAnimating ? 'all 0.3s ease' : 'none'
      };
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.$refs.roomView.focus();
    });
  },
  methods: {
    // 供外部调用的键盘处理方法
    tryMoveByKey(event) {
      if (this.isMoving || !this.canMove) return;

      const key = event.key;
      let direction = null;
      let dx = 0, dy = 0;

      if (key === 'ArrowUp') {
        direction = 'north';
        dy = -20;
      } else if (key === 'ArrowDown') {
        direction = 'south';
        dy = 20;
      } else if (key === 'ArrowLeft') {
        direction = 'west';
        dx = -20;
      } else if (key === 'ArrowRight') {
        direction = 'east';
        dx = 20;
      }

      if (direction) {
        this.tryMove(direction, dx, dy);
      }
    },
    hasExit(direction) {
      return this.exits.includes(direction);
    },
    handleKeydown(event) {
      if (this.isMoving || !this.canMove) return;

      const key = event.key;
      let direction = null;
      let dx = 0, dy = 0;

      if (key === 'ArrowUp') {
        direction = 'north';
        dy = -20;
      } else if (key === 'ArrowDown') {
        direction = 'south';
        dy = 20;
      } else if (key === 'ArrowLeft') {
        direction = 'west';
        dx = -20;
      } else if (key === 'ArrowRight') {
        direction = 'east';
        dx = 20;
      }

      if (direction) {
        this.tryMove(direction, dx, dy);
      }
    },
    tryMove(direction, dx, dy) {
      if (this.isAnimating) return;

      const newX = this.playerX + dx;
      const newY = this.playerY + dy;

      // 检查是否到达边界
      if (newX < 10 || newX > 90 || newY < 10 || newY > 90) {
        // 到达边界，检查是否有出口
        if (this.hasExit(direction)) {
          // 先移动到边界位置（带动画）
          this.isAnimating = true;
          this.isMoving = true;
          this.moveDirection = direction;
          this.playerX = newX;
          this.playerY = newY;
          // 动画完成后触发房间切换
          this.pendingDirection = direction;
        } else {
          // 无出口，碰撞效果
          this.bounceBack();
        }
      } else {
        // 正常移动（带动画）
        this.isAnimating = true;
        this.playerX = newX;
        this.playerY = newY;
      }
    },
    bounceBack() {
      // 简单的碰撞反馈
      const btn = this.$refs.roomCenter;
      if (btn) {
        btn.style.animation = 'none';
        btn.offsetHeight; // 触发重绘
        btn.style.animation = 'bounce 0.3s';
      }
    },
    onMoveComplete() {
      if (this.pendingDirection) {
        // 有待处理的移动方向，触发房间切换
        const direction = this.pendingDirection;
        this.pendingDirection = null;
        this.$emit('move', direction);
      }
      this.isAnimating = false;
    },
    onMoveStart() {
      this.isAnimating = true;
    },
    resetPosition() {
      this.playerX = 50;
      this.playerY = 50;
      this.isMoving = false;
      this.isAnimating = false;
    }
  }
};
</script>

<style scoped>
.room-view {
  padding: 0;
  outline: none;
  width: 100%;
  height: 100%;
}

.room-container {
  background: #1a252f;
  border-radius: 12px;
  overflow: hidden;
  width: 100%;
  height: 100%;
}

.room-scene {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: linear-gradient(180deg, #1a252f 0%, #2c3e50 100%);
  position: relative;
}

.wall-row {
  display: flex;
  flex: 1;
}

.wall {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #2c3e50 0%, #1a252f 100%);
  border: 1px solid #34495e;
}

.wall-top, .wall-bottom {
  height: 50px;
  width: 100%;
}

.wall-left, .wall-right {
  width: 50px;
  flex-shrink: 0;
}

.wall-left {
  border-right: none;
}

.wall-right {
  border-left: none;
}

.room-center {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(26, 37, 47, 0.8);
  margin: 3px;
  position: relative;
}

.room-desc {
  color: #bdc3c7;
  font-size: 14px;
  text-align: center;
  padding: 8px;
  margin: 0;
}

.player-icon {
  position: absolute;
  font-size: 36px;
  transform: translate(-50%, -50%);
  cursor: default;
  z-index: 10;
}

.wall-text {
  color: #7f8c8d;
  font-size: 14px;
}

.wall.has-exit {
  background: linear-gradient(135deg, #27ae60 0%, #1e8449 100%);
  border-color: #2ecc71;
}

.exit-marker {
  color: white;
  font-weight: bold;
  font-size: 16px;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

@keyframes bounce {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}
</style>
