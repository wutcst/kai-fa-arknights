<template>
  <div class="arknights-bg">
    <!-- 背景图片层 -->
    <div class="bg-layer">
      <img
        v-if="currentBgType === 'image'"
        :src="backgrounds.images[currentBgIndex]"
        class="bg-image"
        alt=""
      />
      <video
        v-if="currentBgType === 'video'"
        ref="videoRef"
        :src="backgrounds.videos[currentVideoIndex]"
        class="bg-video"
        autoplay
        muted
        loop
        @error="onVideoError"
      ></video>
    </div>

    <!-- 悬浮格子层 -->
    <div class="bg-squares" @mousemove="onMouseMove">
      <div
        v-for="square in squares"
        :key="square.id"
        class="square-item"
        :style="square.style"
      ></div>
    </div>

    <!-- 切换背景按钮 -->
    <button class="change-bg-btn" @click="showBgSelector = true">
      <span class="change-icon">⚙</span>
    </button>

    <!-- 背景选择器 - 使用 teleport 渲染到 body -->
    <Teleport to="body">
      <transition name="fade">
        <div v-if="showBgSelector" class="bg-selector-overlay" @click="showBgSelector = false">
          <div
            class="bg-selector"
            :style="selectorStyle"
            @click.stop
          >
            <!-- 拖拽标题栏 -->
            <div class="selector-header" @mousedown="startDrag">
              <span>选择背景</span>
              <button class="close-btn" @click="showBgSelector = false">×</button>
            </div>

            <div class="selector-body">
              <div class="selector-section">
                <p>图片背景</p>
                <div class="selector-grid">
                  <div
                    v-for="(img, idx) in backgrounds.images"
                    :key="'img-' + idx"
                    class="selector-item"
                    :class="{ active: currentBgType === 'image' && currentBgIndex === idx }"
                    @click="selectImage(idx)"
                  >
                    <img :src="img" alt="" />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </transition>
    </Teleport>
  </div>
</template>

<script>
import bg1 from '@/assets/background/1.jpg';
import bg2 from '@/assets/background/2.jpg';
import bg3 from '@/assets/background/3.jpg';
import bg4 from '@/assets/background/4.jpg';
import bg5 from '@/assets/background/5.jpg';
import bg6 from '@/assets/background/6.jpg';
import bg7 from '@/assets/background/7.jpg';
import bg8 from '@/assets/background/8.jpg';

export default {
  name: 'ArknightsBackground',
  data() {
    return {
      backgrounds: {
        images: [bg1, bg2, bg3, bg4, bg5, bg6, bg7, bg8],
        videos: []
      },
      currentBgType: 'image',
      currentBgIndex: 0,
      currentVideoIndex: 0,
      showBgSelector: false,
      videoRef: null,
      // 拖拽相关
      selectorStyle: {
        position: 'fixed',
        left: '50%',
        top: '50%',
        transform: 'translate(-50%, -50%)'
      },
      isDragging: false,
      dragOffset: { x: 0, y: 0 },
      squares: [],
      squareSize: 0
    };
  },
  mounted() {
    this.initSquares();
    this.loadSavedBackground();
    window.addEventListener('resize', this.initSquares);
  },
  beforeUnmount() {
    this.saveBackgroundPreference();
    window.removeEventListener('resize', this.initSquares);
  },
  methods: {
    initSquares() {
      const size = 50; // 每个格子 50px
      const cols = Math.ceil(window.innerWidth / size);
      const rows = Math.ceil(window.innerHeight / size);
      this.squareSize = size;

      const squares = [];
      for (let row = 0; row < rows; row++) {
        for (let col = 0; col < cols; col++) {
          const index = row * cols + col;
          squares.push({
            id: index,
            col: col,
            row: row,
            style: {
              left: (col * size) + 'px',
              top: (row * size) + 'px',
              width: size + 'px',
              height: size + 'px',
              boxShadow: 'inset 0 0 0px rgba(0,0,0,0)',
              opacity: '0'
            }
          });
        }
      }
      this.squares = squares;
    },
    onMouseMove(event) {
      const mouseX = event.clientX;
      const mouseY = event.clientY;
      const size = this.squareSize;
      const maxDist = size * 2;

      const newSquares = this.squares.map((square) => {
        const centerX = square.col * size + size / 2;
        const centerY = square.row * size + size / 2;
        const dist = Math.sqrt((mouseX - centerX) ** 2 + (mouseY - centerY) ** 2);

        if (dist < maxDist) {
          const intensity = 1 - (dist / maxDist);
          return {
            ...square,
            style: {
              left: (square.col * size) + 'px',
              top: (square.row * size) + 'px',
              width: size + 'px',
              height: size + 'px',
              boxShadow: `inset 0 0 ${20 * intensity}px rgba(0,0,0,${0.5 * intensity})`,
              opacity: String(intensity)
            }
          };
        } else {
          return {
            ...square,
            style: {
              left: (square.col * size) + 'px',
              top: (square.row * size) + 'px',
              width: size + 'px',
              height: size + 'px',
              boxShadow: 'inset 0 0 0px rgba(0,0,0,0)',
              opacity: '0'
            }
          };
        }
      });
      this.squares = newSquares;
    },
    selectImage(index) {
      this.currentBgType = 'image';
      this.currentBgIndex = index;
      this.showBgSelector = false;
    },
    selectVideo(index) {
      this.currentBgType = 'video';
      this.currentVideoIndex = index;
      this.showBgSelector = false;
      this.$nextTick(() => {
        this.videoRef = this.$refs.videoRef;
      });
    },
    startDrag(event) {
      this.isDragging = true;
      this.dragOffset = {
        x: event.clientX - this.selectorStyle.left,
        y: event.clientY - this.selectorStyle.top
      };
      document.addEventListener('mousemove', this.onDrag);
      document.addEventListener('mouseup', this.stopDrag);
    },
    onDrag(event) {
      if (this.isDragging) {
        this.selectorStyle = {
          ...this.selectorStyle,
          left: event.clientX - this.dragOffset.x,
          top: event.clientY - this.dragOffset.y,
          transform: 'none'
        };
      }
    },
    stopDrag() {
      this.isDragging = false;
      document.removeEventListener('mousemove', this.onDrag);
      document.removeEventListener('mouseup', this.stopDrag);
    },
    onVideoError() {
      console.warn('视频加载失败，切换到图片背景');
      this.currentBgType = 'image';
    },
    loadSavedBackground() {
      const saved = localStorage.getItem('arknights-bg-config');
      if (saved) {
        try {
          const config = JSON.parse(saved);
          if (config.type === 'image') {
            this.currentBgIndex = config.index || 0;
          } else {
            this.currentBgType = 'video';
            this.currentVideoIndex = config.index || 0;
          }
        } catch (e) {
          console.warn('加载背景配置失败');
        }
      }
    },
    saveBackgroundPreference() {
      const config = {
        type: this.currentBgType,
        index: this.currentBgType === 'image' ? this.currentBgIndex : this.currentVideoIndex
      };
      localStorage.setItem('arknights-bg-config', JSON.stringify(config));
    }
  }
};
</script>

<style scoped>
.arknights-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 0;
  overflow: hidden;
  background: transparent !important;
  border: none !important;
  outline: none !important;
}

.bg-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.bg-image,
.bg-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 悬浮格子层 */
.bg-squares {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 2;
}

.square-item {
  position: absolute;
  background: transparent;
}

.change-bg-btn {
  position: fixed;
  top: 20px;
  right: 20px;
  width: 40px;
  height: 40px;
  background: rgba(0, 0, 0, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  cursor: pointer;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.change-bg-btn:hover {
  background: rgba(0, 0, 0, 0.7);
  border-color: rgba(255, 255, 255, 0.5);
}

.change-icon {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.8);
}

.bg-selector-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.8);
  z-index: 10000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.bg-selector {
  background: rgba(20, 20, 30, 0.98);
  border: 1px solid rgba(255, 230, 100, 0.4);
  border-radius: 12px;
  min-width: 400px;
  max-width: 90vw;
  max-height: 80vh;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5), 0 0 30px rgba(255, 230, 100, 0.1);
}

.selector-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: linear-gradient(135deg, rgba(255, 230, 100, 0.15) 0%, rgba(255, 200, 50, 0.08) 100%);
  border-bottom: 1px solid rgba(255, 230, 100, 0.2);
  cursor: move;
  user-select: none;
}

.selector-header span {
  color: #ffe63d;
  font-size: 16px;
  font-weight: bold;
  font-family: 'Spinnaker', sans-serif;
}

.close-btn {
  width: 28px;
  height: 28px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  color: rgba(255, 255, 255, 0.8);
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.close-btn:hover {
  background: rgba(255, 100, 100, 0.3);
  border-color: rgba(255, 100, 100, 0.5);
  color: #fff;
}

.selector-body {
  padding: 20px;
  max-height: 60vh;
  overflow-y: auto;
}

.selector-section {
  margin-bottom: 20px;
}

.selector-section p {
  color: rgba(255, 255, 255, 0.7);
  margin: 0 0 10px 0;
  font-size: 14px;
}

.selector-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}

.selector-item {
  aspect-ratio: 16/9;
  border: 2px solid transparent;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
}

.selector-item:hover {
  border-color: rgba(255, 230, 100, 0.5);
}

.selector-item.active {
  border-color: #ffe63d;
  box-shadow: 0 0 10px rgba(255, 230, 100, 0.5);
}

.selector-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
