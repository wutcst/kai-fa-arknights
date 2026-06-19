import { ref } from 'vue';

export function useMessageLog() {
  const messageLog = ref([]);

  const appendLog = (text, error = false) => {
    const now = new Date();
    const time = now.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
    messageLog.value = [
      ...messageLog.value.slice(-19),
      {
        id: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
        time,
        text,
        error
      }
    ];
  };

  const clearLog = () => {
    messageLog.value = [];
  };

  return {
    messageLog,
    appendLog,
    clearLog
  };
}
