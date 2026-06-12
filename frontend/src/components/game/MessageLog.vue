<template>
  <section class="message-log">
    <div class="panel-title">
      <h2>日志</h2>
      <span>最近 {{ messages.length }} 条</span>
    </div>
    <div class="log-list" ref="logList">
      <p v-for="entry in messages" :key="entry.id" :class="{ error: entry.error }">
        <span>{{ entry.time }}</span>
        {{ entry.text }}
      </p>
      <p v-if="!messages.length" class="empty-text">暂无操作记录</p>
    </div>
  </section>
</template>

<script>
export default {
  name: 'MessageLog',
  props: {
    messages: {
      type: Array,
      default: () => []
    }
  },
  watch: {
    messages: {
      deep: true,
      handler() {
        this.$nextTick(() => {
          const list = this.$refs.logList;
          if (list) {
            list.scrollTop = list.scrollHeight;
          }
        });
      }
    }
  }
};
</script>

<style scoped>
.message-log {
  background: rgba(18, 22, 22, 0.9);
  border: 1px solid rgba(215, 168, 77, 0.32);
  border-radius: 16px;
  color: #f6ead2;
  padding: 16px;
}

.panel-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

h2 {
  margin: 0;
}

.panel-title span {
  color: #b9aa90;
  font-size: 12px;
}

.log-list {
  display: grid;
  gap: 8px;
  max-height: 180px;
  overflow-y: auto;
  padding-right: 4px;
  text-align: left;
}

p {
  background: rgba(255, 255, 255, 0.04);
  border-left: 3px solid #58b985;
  border-radius: 8px;
  line-height: 1.5;
  margin: 0;
  padding: 8px 10px;
}

p.error {
  border-left-color: #e1684f;
}

p span {
  color: #d7a84d;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  margin-right: 6px;
}

.empty-text {
  border-left-color: #7a5a32;
  color: #b9aa90;
}
</style>
