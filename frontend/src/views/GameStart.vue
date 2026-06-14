<template>
  <div class="game-start-container">
    <div class="game-start-box">
      <div class="logo-area">
        <img class="rhodes-logo" src="@/assets/Logo_rhodesOverride.png" alt="Rhodes Island" />
      </div>
      <h1>罗德岛任务系统</h1>
      <p class="subtitle">Arknights Mission System</p>
      <p class="welcome-text">欢迎, <span class="operator-name">{{ username }}</span></p>

      <button
        class="btn-logout"
        @click="handleLogout"
      >
        <span class="btn-icon">🚪</span>
        <span>退出登录</span>
      </button>

      <div class="game-options">
        <button
          v-if="hasSave"
          class="btn-option btn-continue"
          @click="handleContinue"
          :disabled="loading"
        >
          <span class="btn-icon">📂</span>
          <span class="btn-text">继续任务</span>
          <span class="btn-desc">从上次存档继续</span>
        </button>

        <button
          class="btn-option btn-new"
          @click="handleNewGame"
          :disabled="loading"
        >
          <span class="btn-icon">🎮</span>
          <span class="btn-text">{{ hasSave ? '重新开始' : '开始任务' }}</span>
          <span class="btn-desc">{{ hasSave ? '将覆盖当前存档' : '全新任务开始' }}</span>
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
      <p>罗德岛设施探险记</p>
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
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  padding: 20px;
}

.game-start-box {
  background: rgba(10, 20, 40, 0.95);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 15px 50px rgba(0, 191, 255, 0.2), inset 0 0 60px rgba(0, 191, 255, 0.05);
  text-align: center;
  max-width: 450px;
  width: 100%;
  border: 1px solid rgba(0, 191, 255, 0.3);
}

.logo-area {
  display: flex;
  justify-content: center;
  margin-bottom: 15px;
}

.rhodes-logo {
  width: 80px;
  height: 80px;
  filter: drop-shadow(0 0 15px rgba(0, 191, 255, 0.5));
}

.game-start-box h1 {
  color: #00BFFF;
  margin-bottom: 5px;
  font-size: 26px;
  letter-spacing: 4px;
  text-shadow: 0 0 20px rgba(0, 191, 255, 0.5);
}

.subtitle {
  color: #7EC8E3;
  margin-bottom: 20px;
  font-size: 12px;
  letter-spacing: 2px;
}

.welcome-text {
  color: #7EC8E3;
  font-size: 16px;
  margin-bottom: 25px;
}

.operator-name {
  color: #00BFFF;
  font-weight: bold;
}

.btn-logout {
  background: rgba(255, 107, 107, 0.1);
  color: #FF6B6B;
  border: 1px solid rgba(255, 107, 107, 0.3);
  border-radius: 8px;
  padding: 10px 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 25px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.btn-logout:hover {
  background: rgba(255, 107, 107, 0.2);
  border-color: #FF6B6B;
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
  border: 1px solid rgba(0, 191, 255, 0.3);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(0, 191, 255, 0.05);
}

.btn-option:hover:not(:disabled) {
  transform: translateY(-3px);
  background: rgba(0, 191, 255, 0.15);
  border-color: rgba(0, 191, 255, 0.5);
  box-shadow: 0 5px 20px rgba(0, 191, 255, 0.3);
}

.btn-option:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-continue {
  border-color: rgba(78, 205, 196, 0.5);
  background: rgba(78, 205, 196, 0.1);
}

.btn-continue:hover:not(:disabled) {
  background: rgba(78, 205, 196, 0.2);
  border-color: rgba(78, 205, 196, 0.7);
  box-shadow: 0 5px 20px rgba(78, 205, 196, 0.3);
}

.btn-new {
  border-color: rgba(0, 191, 255, 0.5);
  background: rgba(0, 191, 255, 0.1);
}

.btn-new:hover:not(:disabled) {
  background: rgba(0, 191, 255, 0.2);
  border-color: rgba(0, 191, 255, 0.7);
  box-shadow: 0 5px 20px rgba(0, 191, 255, 0.3);
}

.btn-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.btn-text {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 4px;
  color: #fff;
}

.btn-desc {
  font-size: 13px;
  color: #7EC8E3;
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
  border: 4px solid rgba(0, 191, 255, 0.1);
  border-top-color: #00BFFF;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-message {
  margin-top: 15px;
  padding: 12px;
  background: rgba(255, 107, 107, 0.1);
  color: #FF6B6B;
  border-radius: 8px;
  font-size: 14px;
  border: 1px solid rgba(255, 107, 107, 0.3);
}

.footer-info {
  margin-top: 30px;
  color: rgba(126, 200, 227, 0.5);
  font-size: 14px;
}
</style>
