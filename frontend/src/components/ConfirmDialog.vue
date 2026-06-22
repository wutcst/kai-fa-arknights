<template>
  <div class="confirm-dialog-overlay" @click.self="$emit('cancel')">
    <div class="confirm-dialog">
      <div class="confirm-header">
        <div class="header-accent"></div>
        <h3>{{ title }}</h3>
        <button class="btn-close" @click="$emit('cancel')">✕</button>
      </div>
      <div class="confirm-body">
        <p class="confirm-message">{{ message }}</p>
        <div v-if="goldAmount !== null" class="gold-info">
          <span class="gold-label">将获得金币</span>
          <span class="gold-amount">
            <img src="@/assets/items/Lungmen_Dollars.png" class="gold-icon" alt="金币" />
            {{ goldAmount }}
          </span>
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
  background: rgba(0, 0, 0, 0.75);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.confirm-dialog {
  background: linear-gradient(180deg, rgba(35, 30, 24, 0.98), rgba(18, 14, 12, 0.99)),
    radial-gradient(circle at top, rgba(215, 168, 77, 0.12), transparent 60%);
  border: 1px solid rgba(215, 168, 77, 0.4);
  border-radius: 16px;
  width: 400px;
  max-width: 90vw;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5), inset 0 1px 0 rgba(247, 214, 123, 0.15);
}

.confirm-header {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 20px;
  background: linear-gradient(180deg, rgba(50, 42, 32, 0.9), rgba(30, 25, 20, 0.9));
  border-bottom: 1px solid rgba(215, 168, 77, 0.25);
}

.header-accent {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, transparent, #d7a84d, transparent);
}

.confirm-header h3 {
  margin: 0;
  font-size: 18px;
  color: #f7ead2;
  letter-spacing: 1px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.5);
}

.btn-close {
  background: rgba(247, 214, 123, 0.1);
  border: 1px solid rgba(247, 214, 123, 0.3);
  color: #f7ead2;
  width: 30px;
  height: 30px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-close:hover {
  background: rgba(247, 214, 123, 0.25);
  border-color: rgba(247, 214, 123, 0.5);
}

.confirm-body {
  padding: 28px 24px;
}

.confirm-message {
  margin: 0 0 24px 0;
  font-size: 15px;
  color: #c9b896;
  line-height: 1.7;
  text-align: center;
}

.gold-info {
  background: linear-gradient(135deg, rgba(50, 40, 25, 0.9), rgba(35, 28, 18, 0.9));
  border: 1px solid rgba(215, 168, 77, 0.5);
  border-radius: 12px;
  padding: 18px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.3);
}

.gold-label {
  color: #a89878;
  font-size: 14px;
  letter-spacing: 1px;
}

.gold-amount {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #f7d67b;
  font-size: 22px;
  font-weight: 800;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.4);
}

.gold-icon {
  width: 24px;
  height: 24px;
}

.confirm-footer {
  display: flex;
  gap: 12px;
  padding: 18px 24px;
  background: rgba(15, 12, 10, 0.6);
  border-top: 1px solid rgba(215, 168, 77, 0.15);
}

.btn-cancel,
.btn-confirm {
  flex: 1;
  padding: 14px 16px;
  border: 1px solid rgba(247, 214, 123, 0.35);
  border-radius: 10px;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
  letter-spacing: 0.5px;
}

.btn-cancel {
  background: rgba(60, 50, 40, 0.6);
  color: #b0a090;
}

.btn-cancel:hover {
  background: rgba(80, 65, 50, 0.7);
  color: #d0c0b0;
  transform: translateY(-1px);
}

.btn-confirm {
  background: linear-gradient(180deg, rgba(100, 75, 45, 0.9), rgba(70, 55, 35, 0.9));
  color: #f7ead2;
  border-color: rgba(215, 168, 77, 0.5);
  box-shadow: inset 0 1px 0 rgba(247, 214, 123, 0.2);
}

.btn-confirm:hover {
  background: linear-gradient(180deg, rgba(120, 90, 55, 0.95), rgba(85, 65, 42, 0.95));
  border-color: rgba(247, 214, 123, 0.7);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(215, 168, 77, 0.2), inset 0 1px 0 rgba(247, 214, 123, 0.3);
}
</style>
