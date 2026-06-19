<template>
  <div
    class="player-avatar"
    :class="{ 'facing-left': facing === 'west' }"
    :style="playerStyle"
    aria-label="玩家当前位置"
  >
    <video
      ref="playerVideo"
      class="player-video"
      :key="animation"
      :src="playerVideoSrc"
      :loop="looping"
      autoplay
      muted
      playsinline
      @ended="$emit('ended')"
    />
  </div>
</template>

<script>
import checkoutVideo from '@/assets/characters/维什戴尔-绝对主角-checkout.webm';
import moveVideo from '@/assets/characters/维什戴尔-绝对主角-Move.webm';
import operationVideo from '@/assets/characters/维什戴尔-绝对主角-Operation.webm';
import sitVideo from '@/assets/characters/维什戴尔-绝对主角-Sit.webm';
import sleepVideo from '@/assets/characters/维什戴尔-绝对主角-Sleep.webm';

const videoMap = {
  checkout: checkoutVideo,
  move: moveVideo,
  operation: operationVideo,
  sit: sitVideo,
  sleep: sleepVideo
};

export default {
  name: 'PlayerAvatar',
  props: {
    animation: {
      type: String,
      default: 'sit'
    },
    facing: {
      type: String,
      default: 'east'
    },
    playerStyle: {
      type: Object,
      default: () => ({})
    },
    looping: {
      type: Boolean,
      default: true
    }
  },
  emits: ['ended'],
  computed: {
    playerVideoSrc() {
      const src = videoMap[this.animation] || sitVideo;
      console.log('[PlayerAvatar] animation:', this.animation, 'src:', src);
      return src;
    }
  },
  watch: {
    animation() {
      this.restartVideo();
    }
  },
  mounted() {
    this.restartVideo();
  },
  methods: {
    restartVideo() {
      this.$nextTick(() => {
        const video = this.$refs.playerVideo;
        if (video) {
          video.currentTime = 0;
          video.play?.().catch(() => {});
        }
      });
    }
  }
};
</script>

<style scoped>
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
</style>
