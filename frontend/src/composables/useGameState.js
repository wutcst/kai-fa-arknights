import { ref } from 'vue';
import { getMap, getGameStatus, move as apiMove, look as apiLook, goBack as apiGoBack, takeItem, dropItem, getItems, eatCookie as apiEatCookie, saveGame, loadGame } from '@/api/game';

export function useGameState(options) {
  const { appendLog, resetRoomPosition, fetchUserAbility } = options;

  const message = ref('欢迎来到文字冒险世界！');
  const displayMessage = ref('');
  const currentRoomName = ref('');
  const currentRoomId = ref('');
  const longDescription = ref('');
  const exits = ref([]);
  const items = ref([]);
  const inventory = ref([]);
  const selectedInventoryId = ref('');
  const playerWeight = ref(0);
  const playerMaxWeight = ref(20);
  const playerGridPosition = ref({ row: 4, col: 4 });
  const rooms = ref([]);
  const isError = ref(false);
  const showMap = ref(false);
  const isMoving = ref(false);

  const updateGameState = (gameData) => {
    currentRoomName.value = gameData.description || '';
    currentRoomId.value = gameData.roomId || '';
    longDescription.value = gameData.longDescription || '';
    exits.value = Array.from(gameData.exits || []);
    items.value = [];
    inventory.value = gameData.inventory || [];
    syncSelectedInventory();
    playerWeight.value = gameData.playerWeight || 0;
    playerMaxWeight.value = gameData.playerMaxWeight || 20;
    playerGridPosition.value = {
      row: gameData.playerGridRow ?? 4,
      col: gameData.playerGridCol ?? 4
    };
    displayMessage.value = '';
    isError.value = false;
  };

  const syncSelectedInventory = () => {
    if (!selectedInventoryId.value) return;
    const exists = inventory.value.some(item => item.id === selectedInventoryId.value);
    if (!exists) {
      selectedInventoryId.value = '';
    }
  };

  const fetchStatus = async () => {
    try {
      const response = await getGameStatus();
      currentRoomName.value = response.data.description;
      currentRoomId.value = response.data.roomId;
      longDescription.value = response.data.longDescription;
      exits.value = Array.from(response.data.exits);
      items.value = [];
      displayMessage.value = '';
      isError.value = false;
    } catch (error) {
      displayMessage.value = '错误：' + error.message;
      isError.value = true;
      appendLog(displayMessage.value, true);
    }
  };

  const fetchMap = async (savedRoomId = null) => {
    try {
      const response = await getMap();
      rooms.value = response.data.rooms;
      currentRoomId.value = savedRoomId || response.data.currentRoomId;
      if (!savedRoomId) {
        fetchStatus();
      }
    } catch (error) {
      message.value = '错误：无法连接到服务器，请确保后端正在运行！';
      isError.value = true;
      appendLog(message.value, true);
    }
  };

  const move = async (direction) => {
    isMoving.value = true;
    try {
      const response = await apiMove(direction);
      if (response.data.teleported) {
        displayMessage.value = `⚠️ 你被传送门传送到了 ${response.data.description}！`;
      } else {
        displayMessage.value = response.data.message;
      }
      currentRoomName.value = response.data.description;
      currentRoomId.value = response.data.roomId;
      longDescription.value = response.data.longDescription;
      exits.value = Array.from(response.data.exits);
      items.value = [];
      isError.value = response.data.teleported;
      appendLog(displayMessage.value || `移动到 ${currentRoomName.value}`, isError.value);
    } catch (error) {
      displayMessage.value = '移动错误：' + (error.response?.data?.message || error.message);
      isError.value = true;
      appendLog(displayMessage.value, true);
    } finally {
      isMoving.value = false;
      setTimeout(() => {
        resetRoomPosition?.(direction);
      }, 50);
    }
  };

  const look = async () => {
    try {
      const response = await apiLook();
      currentRoomName.value = response.data.description;
      currentRoomId.value = response.data.roomId;
      longDescription.value = response.data.longDescription;
      exits.value = Array.from(response.data.exits);
      items.value = response.data.items || [];
      displayMessage.value = '';
      isError.value = false;
      appendLog(items.value.length ? `查看房间，发现 ${items.value.length} 个物品` : '查看房间，暂未发现物品');
    } catch (error) {
      displayMessage.value = '查看错误：' + error.message;
      isError.value = true;
      appendLog(displayMessage.value, true);
    }
  };

  const goBack = async () => {
    try {
      const response = await apiGoBack();
      displayMessage.value = response.data.message;
      currentRoomName.value = response.data.description;
      currentRoomId.value = response.data.roomId;
      longDescription.value = response.data.longDescription;
      exits.value = Array.from(response.data.exits);
      items.value = [];
      isError.value = !response.data.success;
      appendLog(displayMessage.value || '返回上个房间', isError.value);
    } catch (error) {
      displayMessage.value = '返回错误：' + error.message;
      isError.value = true;
      appendLog(displayMessage.value, true);
    }
  };

  const showInventory = async () => {
    try {
      const response = await getItems();
      inventory.value = response.data.inventory || [];
      playerWeight.value = response.data.playerWeight || 0;
      playerMaxWeight.value = response.data.playerMaxWeight || 50;
      syncSelectedInventory();
      displayMessage.value = response.data.message;
      appendLog(displayMessage.value || `查看背包，共 ${inventory.value.length} 个物品`);
    } catch (error) {
      displayMessage.value = '查看背包失败：' + error.message;
      isError.value = true;
      appendLog(displayMessage.value, true);
    }
  };

  const handleTake = async (itemId) => {
    try {
      const response = await takeItem(itemId);
      displayMessage.value = response.data.message;
      items.value = response.data.items || [];
      inventory.value = response.data.inventory || [];
      playerWeight.value = response.data.playerWeight || 0;
      playerMaxWeight.value = response.data.playerMaxWeight || 50;
      isError.value = !response.data.success;
      syncSelectedInventory();
      appendLog(displayMessage.value || '拾取物品', isError.value);
    } catch (error) {
      displayMessage.value = '拾取失败：' + error.message;
      isError.value = true;
      appendLog(displayMessage.value, true);
    }
  };

  const handleDrop = async (itemId) => {
    try {
      const response = await dropItem(itemId);
      displayMessage.value = response.data.message;
      items.value = response.data.items || [];
      inventory.value = response.data.inventory || [];
      playerWeight.value = response.data.playerWeight || 0;
      playerMaxWeight.value = response.data.playerMaxWeight || 50;
      isError.value = !response.data.success;
      syncSelectedInventory();
      appendLog(displayMessage.value || '丢弃物品', isError.value);
    } catch (error) {
      displayMessage.value = '丢弃失败：' + error.message;
      isError.value = true;
      appendLog(displayMessage.value, true);
    }
  };

  const handleEatCookie = async () => {
    try {
      const response = await apiEatCookie();
      displayMessage.value = response.data.message;
      inventory.value = response.data.inventory || [];
      playerWeight.value = response.data.playerWeight || 0;
      playerMaxWeight.value = response.data.playerMaxWeight || 50;
      isError.value = !response.data.success;
      syncSelectedInventory();
      appendLog(displayMessage.value || '使用魔法饼干', isError.value);
    } catch (error) {
      displayMessage.value = '吃饼干失败：' + error.message;
      isError.value = true;
      appendLog(displayMessage.value, true);
    }
  };

  const handleSave = async (username) => {
    try {
      await saveGame(username, playerGridPosition.value);
      displayMessage.value = '💾 游戏已保存';
      isError.value = false;
      appendLog(displayMessage.value);
    } catch (e) {
      displayMessage.value = '保存失败';
      isError.value = true;
      appendLog(displayMessage.value, true);
    }
  };

  const handleLoad = async (username, afterLoadCallback) => {
    try {
      const response = await loadGame(username);
      displayMessage.value = response.data.message || '已读取存档';
      isError.value = !response.data.success;
      if (!response.data.success) {
        appendLog(displayMessage.value, true);
        return;
      }
      updateGameState(response.data);
      appendLog('读取存档成功');
      await fetchMap(response.data.roomId);
      if (fetchUserAbility) {
        await fetchUserAbility();
      }
      if (afterLoadCallback) {
        afterLoadCallback();
      }
    } catch (error) {
      displayMessage.value = '读取存档失败：' + error.message;
      isError.value = true;
      appendLog(displayMessage.value, true);
    }
  };

  return {
    message,
    displayMessage,
    currentRoomName,
    currentRoomId,
    longDescription,
    exits,
    items,
    inventory,
    selectedInventoryId,
    playerWeight,
    playerMaxWeight,
    playerGridPosition,
    rooms,
    isError,
    showMap,
    isMoving,
    updateGameState,
    syncSelectedInventory,
    fetchMap,
    fetchStatus,
    move,
    look,
    goBack,
    showInventory,
    handleTake,
    handleDrop,
    handleEatCookie,
    handleSave,
    handleLoad
  };
}
