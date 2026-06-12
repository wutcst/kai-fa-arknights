<template>
  <div class="auth-container">
    <div class="auth-box">
      <h1>🌍 文字冒险世界</h1>

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
}

.auth-box {
  background: white;
  padding: 40px;
  border-radius: 15px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  width: 100%;
  max-width: 400px;
}

.auth-box h1 {
  color: #4CAF50;
  margin-bottom: 30px;
  text-align: center;
}

.auth-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 25px;
}

.auth-tabs button {
  flex: 1;
  padding: 12px;
  border: none;
  background: #e0e0e0;
  color: #666;
  border-radius: 8px;
  cursor: pointer;
  font-size: 15px;
  font-weight: bold;
  transition: all 0.2s;
}

.auth-tabs button.active {
  background: #4CAF50;
  color: white;
}

.auth-tabs button:hover:not(.active) {
  background: #bdbdbd;
}

.form-group {
  margin-bottom: 20px;
  text-align: left;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #333;
  font-weight: bold;
}

.form-group input {
  width: 100%;
  padding: 12px 15px;
  border: 2px solid #ddd;
  border-radius: 8px;
  font-size: 15px;
  transition: border-color 0.2s;
}

.form-group input:focus {
  outline: none;
  border-color: #4CAF50;
}

.error-msg {
  color: #e74c3c;
  background: #fde8e8;
  padding: 10px;
  border-radius: 6px;
  margin-bottom: 15px;
  text-align: center;
  font-size: 14px;
}

.success-msg {
  color: #27ae60;
  background: #e8f8f0;
  padding: 10px;
  border-radius: 6px;
  margin-bottom: 15px;
  text-align: center;
  font-size: 14px;
}

.btn-submit {
  width: 100%;
  padding: 14px;
  background: #4CAF50;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-submit:hover:not(:disabled) {
  background: #388E3C;
}

.btn-submit:disabled {
  background: #a5d6a7;
  cursor: not-allowed;
}
</style>
