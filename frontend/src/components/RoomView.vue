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
    },
    moveSpeed: {
      type: Number,
      default: 0.5
    }
  },
  data() {
    return {
      playerX: 50,  // 百分比位置
      playerY: 50,
      isAnimating: false,
      isMoving: false,
      moveDirection: null,
      pendingDirection: null,  // 待处理的移动方向
      // 连续移动相关
      keysPressed: new Set(),
      animationFrameId: null,
      isContinuousMove: false
    };
  },
  computed: {
    effectiveMoveSpeed() {
      return this.moveSpeed || 0.5;
    },
    playerStyle() {
      return {
        left: this.playerX + '%',
        top: this.playerY + '%',
        transition: this.isAnimating ? 'all 0.15s ease-out' : 'none'
      };
    }
  },
  mounted() {
    this.$nextTick(() => {
      this.$refs.roomView.focus();
    });
    // 添加全局按键监听
    window.addEventListener('keydown', this.handleKeyDown);
    window.addEventListener('keyup', this.handleKeyUp);
  },
  beforeUnmount() {
    window.removeEventListener('keydown', this.handleKeyDown);
    window.removeEventListener('keyup', this.handleKeyUp);
    if (this.animationFrameId) {
      cancelAnimationFrame(this.animationFrameId);
    }
  },
  methods: {
    // 供外部调用的键盘处理方法
    tryMoveByKey(event) {
      if (this.isMoving && !this.isContinuousMove) return;
      if (!this.canMove) return;

      const key = event.key;
      const lowerKey = key.toLowerCase();

      // 方向键 或 WASD
      if (['arrowup', 'w'].includes(lowerKey) ||
          ['arrowdown', 's'].includes(lowerKey) ||
          ['arrowleft', 'a'].includes(lowerKey) ||
          ['arrowright', 'd'].includes(lowerKey)) {
        event.preventDefault();
        this.keysPressed.add(lowerKey);
        if (!this.animationFrameId) {
          this.startContinuousMove();
        }
      }
    },

    // 处理按键松开
    handleKeyUp(event) {
      const key = event.key.toLowerCase();
      if (['arrowup', 'w', 'arrowdown', 's', 'arrowleft', 'a', 'arrowright', 'd'].includes(key)) {
        this.keysPressed.delete(key);
        if (this.keysPressed.size === 0) {
          this.stopContinuousMove();
        }
      }
    },

    // 处理按键按下（内部使用）
    handleKeyDown(event) {
      // 只处理 RoomView 获得焦点时
      if (document.activeElement !== this.$refs.roomView) return;
      this.tryMoveByKey(event);
    },

    // 开始连续移动
    startContinuousMove() {
      this.isContinuousMove = true;
      this.updateMovement();
    },

    // 停止连续移动
    stopContinuousMove() {
      this.isContinuousMove = false;
      if (this.animationFrameId) {
        cancelAnimationFrame(this.animationFrameId);
        this.animationFrameId = null;
      }
    },

    // 更新移动（动画循环）
    updateMovement() {
      if (!this.isContinuousMove || this.keysPressed.size === 0) {
        this.stopContinuousMove();
        return;
      }

      let dx = 0, dy = 0;

      // 计算各方向增量
      if (this.keysPressed.has('arrowup') || this.keysPressed.has('w')) dy -= this.effectiveMoveSpeed;
      if (this.keysPressed.has('arrowdown') || this.keysPressed.has('s')) dy += this.effectiveMoveSpeed;
      if (this.keysPressed.has('arrowleft') || this.keysPressed.has('a')) dx -= this.effectiveMoveSpeed;
      if (this.keysPressed.has('arrowright') || this.keysPressed.has('d')) dx += this.effectiveMoveSpeed;

      // 对角线移动归一化
      if (dx !== 0 && dy !== 0) {
        const factor = 1 / Math.sqrt(2);
        dx *= factor;
        dy *= factor;
      }

      const newX = this.playerX + dx;
      const newY = this.playerY + dy;

      // 检查边界
      const atBoundary = this.checkBoundaryAndMove(newX, newY, dx, dy);
      if (atBoundary) {
        return; // 边界触发了房间切换，停止移动循环
      }

      this.animationFrameId = requestAnimationFrame(() => this.updateMovement());
    },

    // 检查边界并移动
    checkBoundaryAndMove(newX, newY, dx, dy) {
      // 检测8个方向的边界
      let direction = null;

      if (newX < 10) direction = 'west';
      else if (newX > 90) direction = 'east';

      if (newY < 10) direction = 'north';
      else if (newY > 90) direction = 'south';

      // 对角线边界检测
      if (dx !== 0 && dy !== 0) {
        if (newX < 10 && newY < 10) direction = this.getDiagonalDirection('west', 'north');
        else if (newX < 10 && newY > 90) direction = this.getDiagonalDirection('west', 'south');
        else if (newX > 90 && newY < 10) direction = this.getDiagonalDirection('east', 'north');
        else if (newX > 90 && newY > 90) direction = this.getDiagonalDirection('east', 'south');
      }

      if (direction) {
        if (this.hasExit(direction)) {
          // 移动到边界位置
          this.playerX = Math.max(5, Math.min(95, newX));
          this.playerY = Math.max(5, Math.min(95, newY));
          // 触发房间切换
          this.triggerRoomMove(direction);
          return true;
        } else {
          // 无出口，碰撞
          this.bounceBack();
          return false;
        }
      }

      // 正常移动
      this.playerX = newX;
      this.playerY = newY;
      return false;
    },

    // 获取对角线方向
    getDiagonalDirection(dir1, dir2) {
      // 根据对角线组合返回优先方向
      if (this.hasExit(dir1)) return dir1;
      if (this.hasExit(dir2)) return dir2;
      return dir1;
    },

    // 触发房间切换移动
    triggerRoomMove(direction) {
      this.isMoving = true;
      this.stopContinuousMove();
      // 延迟发送移动事件，让玩家看到移动到边界的效果
      setTimeout(() => {
        this.$emit('move', direction);
      }, 150);
    },

    hasExit(direction) {
      return this.exits.includes(direction);
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
    resetPosition() {
      this.playerX = 50;
      this.playerY = 50;
      this.isMoving = false;
      this.isAnimating = false;
      this.stopContinuousMove();
      this.keysPressed.clear();
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
