<template>
  <div class="auth-container">
    <div class="auth-box">
      <h1>🌍 文字冒险世界</h1>

      <div class="auth-tabs">
        <button
          :class="{ active: mode === 'login' }"
          @click="mode = 'login'"
        >登录</button>
        <button
          :class="{ active: mode === 'register' }"
          @click="mode = 'register'"
        >注册</button>
      </div>

      <div class="auth-form">
        <div class="form-group">
          <label>用户名</label>
          <input
            v-model="username"
            type="text"
            placeholder="请输入用户名"
            @keyup.enter="handleSubmit"
          />
        </div>

        <div class="form-group">
          <label>密码</label>
          <input
            v-model="password"
            type="password"
            placeholder="请输入密码"
            @keyup.enter="handleSubmit"
          />
        </div>

        <div v-if="mode === 'register'" class="form-group">
          <label>确认密码</label>
          <input
            v-model="confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            @keyup.enter="handleSubmit"
          />
        </div>

        <div v-if="mode === 'changePassword'" class="form-group">
          <label>旧密码</label>
          <input
            v-model="oldPassword"
            type="password"
            placeholder="请输入旧密码"
          />
        </div>

        <div v-if="mode === 'changePassword'" class="form-group">
          <label>新密码</label>
          <input
            v-model="newPassword"
            type="password"
            placeholder="请输入新密码"
          />
        </div>

        <div class="error-msg" v-if="error">{{ error }}</div>
        <div class="success-msg" v-if="success">{{ success }}</div>

        <button class="btn-submit" @click="handleSubmit" :disabled="loading">
          {{ loading ? '处理中...' : (mode === 'login' ? '登录' : mode === 'register' ? '注册' : '修改密码') }}
        </button>

        <div class="auth-links">
          <a v-if="mode === 'login'" @click="mode = 'changePassword'" href="#">修改密码</a>
          <a v-if="mode === 'changePassword'" @click="mode = 'login'" href="#">返回登录</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { login, register, changePassword } from '@/api/auth';

export default {
  name: 'LoginView',
  data() {
    return {
      mode: 'login',  // 'login', 'register', 'changePassword'
      username: '',
      password: '',
      confirmPassword: '',
      oldPassword: '',
      newPassword: '',
      error: '',
      success: '',
      loading: false
    };
  },
  methods: {
    async handleSubmit() {
      this.error = '';
      this.success = '';

      if (!this.username.trim()) {
        this.error = '用户名不能为空';
        return;
      }
      if (!this.password.trim()) {
        this.error = '密码不能为空';
        return;
      }

      if (this.mode === 'register') {
        if (this.password !== this.confirmPassword) {
          this.error = '两次密码输入不一致';
          return;
        }
        this.loading = true;
        try {
          const response = await register(this.username, this.password);
          if (response.data.success) {
            this.success = '注册成功，请登录';
            this.mode = 'login';
            this.password = '';
            this.confirmPassword = '';
          } else {
            this.error = response.data.message;
          }
        } catch (e) {
          this.error = '注册失败：' + (e.response?.data?.message || e.message);
        } finally {
          this.loading = false;
        }
      } else if (this.mode === 'login') {
        this.loading = true;
        try {
          const response = await login(this.username, this.password);
          if (response.data.success) {
            localStorage.setItem('username', this.username);
            this.$emit('login-success', this.username);
          } else {
            this.error = response.data.message;
          }
        } catch (e) {
          this.error = '登录失败：' + (e.response?.data?.message || e.message);
        } finally {
          this.loading = false;
        }
      } else if (this.mode === 'changePassword') {
        if (!this.oldPassword.trim()) {
          this.error = '旧密码不能为空';
          return;
        }
        if (!this.newPassword.trim()) {
          this.error = '新密码不能为空';
          return;
        }
        this.loading = true;
        try {
          const response = await changePassword(this.username, this.oldPassword, this.newPassword);
          if (response.data.success) {
            this.success = '密码修改成功，请重新登录';
            this.password = '';
            this.oldPassword = '';
            this.newPassword = '';
            setTimeout(() => {
              this.mode = 'login';
              this.success = '';
            }, 1500);
          } else {
            this.error = response.data.message;
          }
        } catch (e) {
          this.error = '修改失败：' + (e.response?.data?.message || e.message);
        } finally {
          this.loading = false;
        }
      }
    }
  },
  mounted() {
    // 如果已登录，直接跳转
    const savedUsername = localStorage.getItem('username');
    if (savedUsername) {
      this.$emit('login-success', savedUsername);
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.auth-box {
  background: white;
  padding: 40px;
  border-radius: 15px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.3);
  width: 100%;
  max-width: 400px;
}

.auth-box h1 {
  text-align: center;
  color: #4CAF50;
  margin-bottom: 30px;
  font-size: 24px;
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
  box-sizing: border-box;
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

.auth-links {
  margin-top: 20px;
  text-align: center;
}

.auth-links a {
  color: #2196F3;
  text-decoration: none;
  cursor: pointer;
  font-size: 14px;
}

.auth-links a:hover {
  text-decoration: underline;
}
</style>
