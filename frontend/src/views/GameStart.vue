<template>
  <div class="game-start-container">
    <div class="game-start-box">
      <h1>🌍 文字冒险世界</h1>
      <p class="welcome-text">欢迎, {{ username }}!</p>

      <button
        class="btn-logout"
        @click="handleLogout"
      >
        🚪 退出登录
      </button>

      <div class="game-options">
        <button
          v-if="hasSave"
          class="btn-option btn-continue"
          @click="handleContinue"
          :disabled="loading"
        >
          <span class="btn-icon">📂</span>
          <span class="btn-text">继续游戏</span>
          <span class="btn-desc">从上次存档继续</span>
        </button>

        <button
          class="btn-option btn-new"
          @click="handleNewGame"
          :disabled="loading"
        >
          <span class="btn-icon">🎮</span>
          <span class="btn-text">{{ hasSave ? '重新开始' : '开始游戏' }}</span>
          <span class="btn-desc">{{ hasSave ? '将覆盖当前存档' : '全新冒险开始' }}</span>
        </button>
      </div>

      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-if="error" class="error-message">
        {{ error }}
      </div>
    </div>

    <div class="footer-info">
      <p>教学楼探险记</p>
    </div>
  </div>
</template>

<script>
import { checkSave, loadGame, saveGame, newGame } from '@/api/saveApi';

export default {
  name: 'GameStart',
  props: {
    username: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      hasSave: false,
      loading: false,
      error: ''
    };
  },
  mounted() {
    this.checkSaveStatus();
  },
  methods: {
    async checkSaveStatus() {
      try {
        const response = await checkSave(this.username);
        this.hasSave = response.data.hasSave;
      } catch (e) {
        console.error('检查存档失败:', e);
        this.hasSave = false;
      }
    },
    async handleContinue() {
      this.loading = true;
      this.error = '';
      try {
        const response = await loadGame(this.username);
        if (response.data.success) {
          this.$emit('continue-game', response.data);
        } else {
          this.error = response.data.message || '加载失败';
        }
      } catch (e) {
        this.error = '加载失败: ' + (e.response?.data?.message || e.message);
      } finally {
        this.loading = false;
      }
    },
    async handleNewGame() {
      this.loading = true;
      this.error = '';
      try {
        const response = await newGame(this.username);
        if (response.data.success) {
          await saveGame(this.username);
          this.$emit('start-game', response.data);
        } else {
          this.error = response.data.message || '开始新游戏失败';
        }
      } catch (e) {
        this.error = '开始新游戏失败: ' + (e.response?.data?.message || e.message);
      } finally {
        this.loading = false;
      }
    },
    handleLogout() {
      this.$emit('logout');
    }
  }
};
</script>

<style scoped>
.game-start-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.game-start-box {
  background: white;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 15px 50px rgba(0,0,0,0.3);
  text-align: center;
  max-width: 450px;
  width: 100%;
}

.game-start-box h1 {
  color: #4CAF50;
  margin-bottom: 10px;
  font-size: 28px;
}

.welcome-text {
  color: #666;
  font-size: 16px;
  margin-bottom: 30px;
}

.btn-logout {
  background: #f44336;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 10px 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 20px;
}

.btn-logout:hover {
  background: #d32f2f;
  transform: translateY(-2px);
}

.game-options {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.btn-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #f5f5f5;
}

.btn-option:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 5px 20px rgba(0,0,0,0.15);
}

.btn-option:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-continue {
  background: linear-gradient(135deg, #4CAF50, #8BC34A);
  color: white;
}

.btn-new {
  background: linear-gradient(135deg, #2196F3, #64B5F6);
  color: white;
}

.btn-icon {
  font-size: 36px;
  margin-bottom: 8px;
}

.btn-text {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 4px;
}

.btn-desc {
  font-size: 13px;
  opacity: 0.8;
}

.loading {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid rgba(0,0,0,0.1);
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-message {
  margin-top: 15px;
  padding: 12px;
  background: #ffebee;
  color: #c62828;
  border-radius: 8px;
  font-size: 14px;
}

.footer-info {
  margin-top: 30px;
  color: rgba(255,255,255,0.7);
  font-size: 14px;
}
</style>
