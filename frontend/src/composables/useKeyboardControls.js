import { onMounted, onBeforeUnmount } from 'vue';

export function useKeyboardControls(options) {
  const {
    isLoggedIn,
    showGameStart,
    isMoving,
    playPlayerOperation,
    goBack,
    look,
    getHelp,
    handleSave,
    toggleMap,
    toggleInventoryDetail,
    closeInventoryDetail,
    isInventoryDetailOpen,
    getRoomViewRef
  } = options;

  const handleKeyDown = (e) => {
    // 阻止方向键默认滚动页面行为
    if (['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'].includes(e.key)) {
      e.preventDefault();
    }
    
    // 如果焦点在输入框中，不处理游戏快捷键
    const activeElement = document.activeElement;
    if (activeElement && (activeElement.tagName === 'INPUT' || activeElement.tagName === 'TEXTAREA')) {
      return;
    }
    
    // 只有在已登录且不在开始界面才处理游戏快捷键
    if (!isLoggedIn.value || showGameStart.value) return;

    if (isInventoryDetailOpen?.value) {
      e.preventDefault();
      if (e.key === 'b' || e.key === 'B' || e.key === 'Escape') {
        closeInventoryDetail?.();
      }
      return;
    }
    
    // 防止重复处理
    if (isMoving.value) return;
    
    // Backspace 或 Esc 返回
    if (e.key === 'Backspace' || e.key === 'Escape') {
      e.preventDefault();
      playPlayerOperation?.();
      goBack?.();
      return;
    }
    
    // F 键查看房间物品
    if (e.key === 'f' || e.key === 'F') {
      playPlayerOperation?.();
      look?.();
      return;
    }

    // B 键查看背包
    if (e.key === 'b' || e.key === 'B') {
      e.preventDefault();
      toggleInventoryDetail?.();
      return;
    }
    
    // H 键帮助
    if (e.key === 'h' || e.key === 'H') {
      playPlayerOperation?.();
      getHelp?.();
      return;
    }

    // R 键存档
    if (e.key === 'r' || e.key === 'R') {
      playPlayerOperation?.();
      handleSave?.();
      return;
    }

    // M 键切换地图
    if (e.key === 'm' || e.key === 'M') {
      playPlayerOperation?.();
      toggleMap?.();
      return;
    }
    
    // 方向键移动
    const roomView = getRoomViewRef?.();
    if (roomView && roomView.tryMoveByKey) {
      roomView.tryMoveByKey(e);
    }
  };

  const registerKeyboardControls = () => {
    window.addEventListener('keydown', handleKeyDown, true);
  };

  const unregisterKeyboardControls = () => {
    window.removeEventListener('keydown', handleKeyDown, true);
  };

  onMounted(() => {
    registerKeyboardControls();
  });

  onBeforeUnmount(() => {
    unregisterKeyboardControls();
  });

  return {
    registerKeyboardControls,
    unregisterKeyboardControls
  };
}
