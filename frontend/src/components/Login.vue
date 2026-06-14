<template>
  <div class="auth-container">
    <div class="auth-box">
      <div class="logo-area">
        <img class="rhodes-logo" src="@/assets/Logo_rhodesOverride.png" alt="Rhodes Island" />
      </div>
      <h1>罗德岛干员系统</h1>
      <p class="subtitle">Arknights Operator System</p>

      <div class="auth-tabs">
        <button
          :class="{ active: mode === 'login' }"
          @click="switchMode('login')"
        >登录</button>
        <button
          :class="{ active: mode === 'register' }"
          @click="switchMode('register')"
        >注册</button>
        <button
          :class="{ active: mode === 'changePassword' }"
          @click="switchMode('changePassword')"
        >修改密码</button>
      </div>

      <div class="auth-form">
        <div class="form-group">
          <label>用户名</label>
          <input
            v-model="username"
            type="text"
            placeholder="请输入用户名"
            @keyup.enter="handleAuth"
          />
        </div>

        <div v-if="mode !== 'changePassword'" class="form-group">
          <label>密码</label>
          <input
            v-model="password"
            type="password"
            placeholder="请输入密码"
            @keyup.enter="handleAuth"
          />
        </div>

        <div v-if="mode === 'register'" class="form-group">
          <label>确认密码</label>
          <input
            v-model="confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            @keyup.enter="handleAuth"
          />
        </div>

        <div v-if="mode === 'changePassword'" class="form-group">
          <label>旧密码</label>
          <input
            v-model="oldPassword"
            type="password"
            placeholder="请输入旧密码"
            @keyup.enter="handleAuth"
          />
        </div>

        <div v-if="mode === 'changePassword'" class="form-group">
          <label>新密码</label>
          <input
            v-model="newPassword"
            type="password"
            placeholder="请输入新密码"
            @keyup.enter="handleAuth"
          />
        </div>

        <div v-if="mode === 'changePassword'" class="form-group">
          <label>确认新密码</label>
          <input
            v-model="confirmNewPassword"
            type="password"
            placeholder="请再次输入新密码"
            @keyup.enter="handleAuth"
          />
        </div>

        <div class="error-msg" v-if="authError">{{ authError }}</div>
        <div class="success-msg" v-if="authSuccess">{{ authSuccess }}</div>

        <button class="btn-submit" @click="handleAuth" :disabled="authLoading">
          {{ submitText }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { login, register, changePassword } from '@/api/authApi';
import {
  validateLoginForm,
  validateRegisterForm,
  validateChangePasswordForm
} from '@/utils/authValidators';

export default {
  name: 'UserLogin',
  emits: ['login-success'],
  data() {
    return {
      username: '',
      mode: 'login',
      password: '',
      confirmPassword: '',
      oldPassword: '',
      newPassword: '',
      confirmNewPassword: '',
      authError: '',
      authSuccess: '',
      authLoading: false
    };
  },
  computed: {
    submitText() {
      if (this.authLoading) return '处理中...';
      if (this.mode === 'login') return '登录';
      if (this.mode === 'register') return '注册';
      return '修改密码';
    }
  },
  methods: {
    switchMode(mode) {
      this.mode = mode;
      this.authError = '';
      this.authSuccess = '';
    },
    async handleAuth() {
      this.authError = '';
      this.authSuccess = '';

      if (this.mode === 'login') {
        await this.handleLogin();
        return;
      }

      if (this.mode === 'register') {
        await this.handleRegister();
        return;
      }

      if (this.mode === 'changePassword') {
        await this.handleChangePassword();
      }
    },
    async handleLogin() {
      const error = validateLoginForm({
        username: this.username,
        password: this.password
      });
      if (error) {
        this.authError = error;
        return;
      }

      this.authLoading = true;
      try {
        const response = await login(this.username, this.password);
        if (response.data.success) {
          this.$emit('login-success', this.username);
          this.password = '';
        } else {
          this.authError = response.data.message;
        }
      } catch (e) {
        this.authError = '登录失败：' + (e.response?.data?.message || e.message);
      } finally {
        this.authLoading = false;
      }
    },
    async handleRegister() {
      const error = validateRegisterForm({
        username: this.username,
        password: this.password,
        confirmPassword: this.confirmPassword
      });
      if (error) {
        this.authError = error;
        return;
      }

      this.authLoading = true;
      try {
        const response = await register(this.username, this.password);
        if (response.data.success) {
          this.authSuccess = '注册成功，请登录';
          this.mode = 'login';
          this.password = '';
          this.confirmPassword = '';
        } else {
          this.authError = response.data.message;
        }
      } catch (e) {
        this.authError = '注册失败：' + (e.response?.data?.message || e.message);
      } finally {
        this.authLoading = false;
      }
    },
    async handleChangePassword() {
      const error = validateChangePasswordForm({
        username: this.username,
        oldPassword: this.oldPassword,
        newPassword: this.newPassword,
        confirmNewPassword: this.confirmNewPassword
      });
      if (error) {
        this.authError = error;
        return;
      }

      this.authLoading = true;
      try {
        const response = await changePassword(this.username, this.oldPassword, this.newPassword);
        if (response.data.success) {
          this.authSuccess = '密码修改成功，请使用新密码登录';
          this.mode = 'login';
          this.password = '';
          this.oldPassword = '';
          this.newPassword = '';
          this.confirmNewPassword = '';
        } else {
          this.authError = response.data.message;
        }
      } catch (e) {
        this.authError = '修改密码失败：' + (e.response?.data?.message || e.message);
      } finally {
        this.authLoading = false;
      }
    }
  }
};
</script>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
}

.auth-box {
  background: rgba(10, 20, 40, 0.95);
  padding: 40px;
  border-radius: 15px;
  box-shadow: 0 10px 40px rgba(0, 191, 255, 0.2), inset 0 0 60px rgba(0, 191, 255, 0.05);
  width: 100%;
  max-width: 400px;
  border: 1px solid rgba(0, 191, 255, 0.3);
}

.logo-area {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.rhodes-logo {
  width: 100px;
  height: 100px;
  filter: drop-shadow(0 0 15px rgba(0, 191, 255, 0.5));
}

.auth-box h1 {
  color: #00BFFF;
  margin-bottom: 5px;
  text-align: center;
  text-shadow: 0 0 20px rgba(0, 191, 255, 0.5);
  font-size: 24px;
  letter-spacing: 4px;
}

.subtitle {
  color: #7EC8E3;
  margin-bottom: 30px;
  text-align: center;
  font-size: 12px;
  letter-spacing: 2px;
}

.auth-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 25px;
}

.auth-tabs button {
  flex: 1;
  padding: 12px;
  border: 1px solid rgba(0, 191, 255, 0.3);
  background: transparent;
  color: #7EC8E3;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  transition: all 0.3s;
}

.auth-tabs button.active {
  background: rgba(0, 191, 255, 0.2);
  color: #00BFFF;
  border-color: #00BFFF;
  box-shadow: 0 0 15px rgba(0, 191, 255, 0.3);
}

.auth-tabs button:hover:not(.active) {
  background: rgba(0, 191, 255, 0.1);
  border-color: rgba(0, 191, 255, 0.5);
}

.form-group {
  margin-bottom: 20px;
  text-align: left;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #7EC8E3;
  font-weight: bold;
  font-size: 13px;
}

.form-group input {
  width: 100%;
  padding: 12px 15px;
  border: 1px solid rgba(0, 191, 255, 0.3);
  border-radius: 8px;
  font-size: 15px;
  background: rgba(0, 191, 255, 0.05);
  color: #fff;
  transition: all 0.3s;
}

.form-group input::placeholder {
  color: #5a7a8a;
}

.form-group input:focus {
  outline: none;
  border-color: #00BFFF;
  box-shadow: 0 0 15px rgba(0, 191, 255, 0.3);
  background: rgba(0, 191, 255, 0.1);
}

.error-msg {
  color: #FF6B6B;
  background: rgba(255, 107, 107, 0.1);
  padding: 10px;
  border-radius: 6px;
  margin-bottom: 15px;
  text-align: center;
  font-size: 14px;
  border: 1px solid rgba(255, 107, 107, 0.3);
}

.success-msg {
  color: #4ECDC4;
  background: rgba(78, 205, 196, 0.1);
  padding: 10px;
  border-radius: 6px;
  margin-bottom: 15px;
  text-align: center;
  font-size: 14px;
  border: 1px solid rgba(78, 205, 196, 0.3);
}

.btn-submit {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #00BFFF 0%, #0099CC 100%);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
  text-shadow: 0 1px 2px rgba(0,0,0,0.3);
  box-shadow: 0 4px 15px rgba(0, 191, 255, 0.3);
}

.btn-submit:hover:not(:disabled) {
  background: linear-gradient(135deg, #33CCFF 0%, #00BFFF 100%);
  box-shadow: 0 6px 20px rgba(0, 191, 255, 0.5);
  transform: translateY(-2px);
}

.btn-submit:disabled {
  background: linear-gradient(135deg, #4a7a8a 0%, #3a6a7a 100%);
  cursor: not-allowed;
  box-shadow: none;
}
</style>
