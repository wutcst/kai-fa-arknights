/* eslint-disable */
import { ref } from 'vue';

// 音乐文件路径
const MUSIC = {
  LOBBY: require('@/assets/music/塞壬唱片-MSR _ z1on _ ReStudio - 栖所架构.mp3'),
  GAME: require('@/assets/music/塞壬唱片-MSR _ Mong Tong - 岁识气象.mp3')
};

// 音乐播放器状态
const currentMusic = ref(null);
const isPlaying = ref(false);
const musicVolume = ref(0.3); // 默认音量 30%
let audioElement = null;

// 初始化音频元素
const initAudio = () => {
  if (!audioElement) {
    audioElement = new Audio();
    audioElement.loop = true;
    audioElement.volume = musicVolume.value;
  }
};

// 播放指定音乐
const playMusic = async (musicKey) => {
  initAudio();
  const musicPath = MUSIC[musicKey];
  if (!musicPath) {
    return false;
  }
  if (audioElement.src !== musicPath) {
    audioElement.src = musicPath;
    audioElement.load();
  }

  try {
    await audioElement.play();
    isPlaying.value = true;
    currentMusic.value = musicKey;
    console.log('正在播放:', musicKey === 'LOBBY' ? '栖所架构' : '岁识气象');
    return true;
  } catch (err) {
    isPlaying.value = false;
    console.warn('音乐播放失败:', err);
    return false;
  }
};

// 停止音乐
const stopMusic = () => {
  if (audioElement) {
    audioElement.pause();
    audioElement.currentTime = 0;
    isPlaying.value = false;
  }
};

// 暂停音乐
const pauseMusic = () => {
  if (audioElement) {
    audioElement.pause();
    isPlaying.value = false;
  }
};

// 继续播放
const resumeMusic = () => {
  if (audioElement) {
    audioElement.play().then(() => {
      isPlaying.value = true;
    });
  }
};

// 设置音量
const setVolume = (volume) => {
  musicVolume.value = Math.max(0, Math.min(1, volume));
  if (audioElement) {
    audioElement.volume = musicVolume.value;
  }
};

// 获取当前音乐状态
const getMusicState = () => ({
  currentMusic: currentMusic.value,
  isPlaying: isPlaying.value,
  volume: musicVolume.value
});

// 清理音频资源
const cleanup = () => {
  if (audioElement) {
    audioElement.pause();
    audioElement.src = '';
    audioElement = null;
  }
};

export function useBackgroundMusic() {
  return {
    playMusic,
    stopMusic,
    pauseMusic,
    resumeMusic,
    setVolume,
    getMusicState,
    cleanup,
    currentMusic,
    isPlaying,
    musicVolume
  };
}
