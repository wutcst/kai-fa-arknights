<template>
  <div class="confirm-dialog-overlay" @click.self="$emit('cancel')">
    <div class="confirm-dialog">
      <div class="confirm-header">
        <h3>{{ title }}</h3>
        <button class="btn-close" @click="$emit('cancel')">✕</button>
      </div>
      <div class="confirm-body">
        <p class="confirm-message">{{ message }}</p>
        <div v-if="goldAmount !== null" class="gold-info">
          <span class="gold-label">将获得金币:</span>
          <span class="gold-amount">💰 {{ goldAmount }}</span>
        </div>
      </div>
      <div class="confirm-footer">
        <button class="btn-cancel" @click="$emit('cancel')">取消</button>
        <button class="btn-confirm" @click="$emit('confirm')">确认结算</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ConfirmDialog',
  props: {
    title: {
      type: String,
      default: '确认结算'
    },
    message: {
      type: String,
      default: '确定要结束当前探索吗？'
    },
    goldAmount: {
      type: Number,
      default: null
    }
  }
};
</script>

<style scoped>
.confirm-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.confirm-dialog {
  background: white;
  border-radius: 15px;
  width: 380px;
  max-width: 90vw;
  overflow: hidden;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
}

.confirm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.confirm-header h3 {
  margin: 0;
  font-size: 18px;
}

.btn-close {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 14px;
}

.btn-close:hover {
  background: rgba(255, 255, 255, 0.3);
}

.confirm-body {
  padding: 25px 20px;
}

.confirm-message {
  margin: 0 0 20px 0;
  font-size: 15px;
  color: #333;
  line-height: 1.6;
}

.gold-info {
  background: linear-gradient(135deg, #fff9e6, #fff3cc);
  border: 2px solid #ffd700;
  border-radius: 10px;
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.gold-label {
  color: #666;
  font-size: 14px;
}

.gold-amount {
  color: #ff9800;
  font-size: 20px;
  font-weight: bold;
}

.confirm-footer {
  display: flex;
  gap: 10px;
  padding: 15px 20px;
  background: #f9f9f9;
  border-top: 1px solid #eee;
}

.btn-cancel,
.btn-confirm {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel {
  background: #e0e0e0;
  color: #666;
}

.btn-cancel:hover {
  background: #d0d0d0;
}

.btn-confirm {
  background: linear-gradient(135deg, #9b59b6, #8e44ad);
  color: white;
}

.btn-confirm:hover {
  transform: scale(1.02);
}
</style>
