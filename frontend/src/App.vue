<template>
  <div id="app" tabindex="0" ref="appContainer">
    <!-- 未登录显示登录界面 -->
    <Login v-if="!isLoggedIn" @login-success="handleLoginSuccess" />

    <!-- 已登录但未选择游戏显示开始界面 -->
    <GameStart
      v-else-if="showGameStart"
      :username="username"
      @start-game="handleStartGame"
      @continue-game="handleContinueGame"
      @logout="handleLogout"
    />

    <!-- 已登录且已选择游戏显示游戏界面 -->
    <template v-else>
    <GameView
      ref="roomView"
      :username="username"
      :user-gold="userGold"
      :room-name="currentRoomName"
      :room-id="currentRoomId"
      :player-grid-position="playerGridPosition"
      :move-speed="currentMoveSpeed"
      :description="longDescription"
      :exits="exits"
      :items="items"
      :inventory="inventory"
      :player-weight="playerWeight"
      :player-max-weight="playerMaxWeight"
      :selected-inventory-id="selectedInventoryId"
      :messages="messageLog"
      :is-error="isError"
      :busy="isMoving"
      @move="move"
      @look="look"
      @take="handleTake"
      @drop="handleDrop"
      @eat-cookie="handleEatCookie"
      @save="handleSave"
      @load="handleLoad"
      @back="goBack"
      @toggle-map="showMap = !showMap"
      @help="getHelp"
      @open-ability="showAbilityPanel = true; fetchUserAbility()"
      @settle="showSettleConfirm = true"
      @select-inventory="selectedInventoryId = $event"
      @player-position-change="playerGridPosition = $event"
      @back-to-menu="handleBackToMenu"
      @logout="handleLogout"
    />

    <!-- 悬浮地图 -->
    <div v-if="showMap" class="floating-map" @click="showMap = false">
      <div class="floating-map-content" @click.stop>
        <GameMap
          :rooms="rooms"
          :currentRoomId="currentRoomId"
        />
      </div>
    </div>

    <!-- 悬浮能力面板 -->
    <AbilityPanel
      v-if="showAbilityPanel"
      :gold="userGold"
      :configs="abilityConfigs"
      :ability="userAbility"
      @close="showAbilityPanel = false"
      @upgrade="handleUpgrade"
    />

    <!-- 结算确认对话框 -->
    <ConfirmDialog
      v-if="showSettleConfirm"
      title="确认结算"
      :message="'结算后游戏将重置，您将返回主界面。\n背包中的物品将转换为金币。'"
      :goldAmount="expectedSettleGold"
      @confirm="handleSettle"
      @cancel="showSettleConfirm = false"
    />
    </template>
  </div>
</template>

<script>
/**
 * 游戏主应用组件.
 * 整合地图、控制和状态显示组件.
 */
import { getMap, getGameStatus, move, look, goBack, takeItem, dropItem, getItems, eatCookie, saveGame, loadGame, newGame, settleExploration, getUserAbility, getAbilityConfigs, upgradeAbility, getUserStats } from '@/api/game';
import { getUserByUsername } from '@/api/auth';
import GameMap from '@/components/GameMap.vue';
import Login from '@/components/Login.vue';
import GameStart from '@/views/GameStart.vue';
import GameView from '@/views/GameView.vue';
import AbilityPanel from '@/components/AbilityPanel.vue';
import ConfirmDialog from '@/components/ConfirmDialog.vue';

export default {
  name: 'App',
  components: {
    GameMap,
    Login,
    GameStart,
    GameView,
    AbilityPanel,
    ConfirmDialog
  },
  data() {
    return {
      // 登录状态
      isLoggedIn: false,
      showGameStart: true,
      username: '',
      // 游戏状态
      message: '欢迎来到文字冒险世界！',
      displayMessage: '',
      currentRoomName: '',
      currentRoomId: '',
      longDescription: '',
      exits: [],
      items: [],           // 房间物品
      inventory: [],       // 玩家背包
      selectedInventoryId: '',
      messageLog: [],
      playerWeight: 0,
      playerMaxWeight: 20,
      playerGridPosition: { row: 4, col: 4 },
      showAbilityPanel: false,  // 是否显示能力面板
      showSettleConfirm: false,  // 是否显示结算确认对话框
      rooms: [],
      isError: false,
      showMap: false,  // 是否显示地图
      isMoving: false,  // 是否正在移动中
      // 金币和能力
      userGold: 0,
      userAbility: null,
      abilityConfigs: [],
      abilityLevels: {
        maxWeightLevel: 1,
        goldBonusLevel: 1,
        moveSpeedLevel: 1
      },
      currentMoveSpeed: 0.5  // 当前移动速度
    };
  },
  created() {
    // 全局键盘事件监听
    window.addEventListener('keydown', (e) => {
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
      if (!this.isLoggedIn || this.showGameStart) return;
      
      // 防止重复处理
      if (this.isMoving) return;
      
      // Backspace 或 Esc 返回
      if (e.key === 'Backspace' || e.key === 'Escape') {
        e.preventDefault();
        this.playPlayerOperation();
        this.goBack();
        return;
      }
      
      // F 键查看房间物品
      if (e.key === 'f' || e.key === 'F') {
        this.playPlayerOperation();
        this.look();
        return;
      }

      // B 键查看背包
      if (e.key === 'b' || e.key === 'B') {
        this.playPlayerOperation();
        this.showInventory();
        return;
      }
      
      // H 键帮助
      if (e.key === 'h' || e.key === 'H') {
        this.playPlayerOperation();
        this.getHelp();
        return;
      }

      // R 键存档
      if (e.key === 'r' || e.key === 'R') {
        this.playPlayerOperation();
        this.handleSave();
        return;
      }

      // M 键切换地图
      if (e.key === 'm' || e.key === 'M') {
        this.playPlayerOperation();
        this.showMap = !this.showMap;
        return;
      }
      
      // 方向键移动
      if (this.$refs.roomView) {
        this.$refs.roomView.tryMoveByKey(e);
      }
    }, true);
  },
  mounted() {
    // 检查登录状态
    const savedUsername = localStorage.getItem('username');
    if (savedUsername) {
      this.username = savedUsername;
      this.isLoggedIn = true;
      this.showGameStart = true;  // 显示游戏开始界面
    }
  },
  methods: {
    // 登录成功处理
    handleLoginSuccess(username) {
      this.username = username;
      this.isLoggedIn = true;
      this.showGameStart = true;  // 显示游戏开始界面
      localStorage.setItem('username', username);
    },
    // 开始新游戏
    handleStartGame(gameData) {
      this.showGameStart = false;
      this.updateGameState(gameData);
      this.appendLog('开始新的探索');
      this.fetchMap();
      this.fetchUserAbility();
    },
    // 继续游戏
    handleContinueGame(gameData) {
      this.showGameStart = false;
      this.updateGameState(gameData);
      this.appendLog('读取已有存档，继续探索');
      this.fetchMap(gameData.roomId);  // 传入保存的房间ID
      this.fetchUserAbility();
    },
    // 更新游戏状态
    updateGameState(gameData) {
      this.currentRoomName = gameData.description || '';
      this.currentRoomId = gameData.roomId || '';
      this.longDescription = gameData.longDescription || '';
      this.exits = Array.from(gameData.exits || []);
      // 房间物品必须通过“查看”显现，避免进入房间时提前暴露。
      this.items = [];
      this.inventory = gameData.inventory || [];
      this.syncSelectedInventory();
      this.playerWeight = gameData.playerWeight || 0;
      this.playerMaxWeight = gameData.playerMaxWeight || 20;
      this.playerGridPosition = {
        row: gameData.playerGridRow ?? 4,
        col: gameData.playerGridCol ?? 4
      };
      this.displayMessage = '';
      this.isError = false;
    },
    // 返回主界面
    handleBackToMenu() {
      this.showGameStart = true;
      this.selectedInventoryId = '';
    },
    // 退出登录
    handleLogout() {
      this.isLoggedIn = false;
      this.showGameStart = true;
      this.username = '';
      this.selectedInventoryId = '';
      this.messageLog = [];
      localStorage.removeItem('username');
    },
    appendLog(text, error = false) {
      const now = new Date();
      const time = now.toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      });
      this.messageLog = [
        ...this.messageLog.slice(-19),
        {
          id: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
          time,
          text,
          error
        }
      ];
    },
    syncSelectedInventory() {
      if (!this.selectedInventoryId) return;
      const exists = this.inventory.some(item => item.id === this.selectedInventoryId);
      if (!exists) {
        this.selectedInventoryId = '';
      }
    },
    playPlayerOperation() {
      this.$refs.roomView?.playOperation();
    },
    async fetchMap(savedRoomId = null) {
      try {
        const response = await getMap();
        this.rooms = response.data.rooms;
        // 如果传入了保存的房间ID（继续游戏时），使用它；否则使用后端返回的
        this.currentRoomId = savedRoomId || response.data.currentRoomId;
        // 只有在新游戏时才调用fetchStatus，继续游戏时已有完整状态
        if (!savedRoomId) {
          this.fetchStatus();
        }
      } catch (error) {
        this.message = '错误：无法连接到服务器，请确保后端正在运行！';
        this.isError = true;
        this.appendLog(this.message, true);
      }
    },
    async fetchStatus() {
      try {
        const response = await getGameStatus();
        this.currentRoomName = response.data.description;
        this.currentRoomId = response.data.roomId;
        this.longDescription = response.data.longDescription;
        this.exits = Array.from(response.data.exits);
        // 不加载物品列表，只有点击查看时才加载
        this.items = [];
        this.displayMessage = '';
        this.isError = false;
      } catch (error) {
        this.displayMessage = '错误：' + error.message;
        this.isError = true;
        this.appendLog(this.displayMessage, true);
      }
    },
    async move(direction) {
      this.isMoving = true;
      try {
        const response = await move(direction);
        // 检查是否发生了传送
        if (response.data.teleported) {
          this.displayMessage = `⚠️ 你被传送门传送到了 ${response.data.description}！`;
        } else {
          this.displayMessage = response.data.message;
        }
        this.currentRoomName = response.data.description;
        this.currentRoomId = response.data.roomId;
        this.longDescription = response.data.longDescription;
        this.exits = Array.from(response.data.exits);
        // 移动后清空物品列表，只有点击查看时才加载
        this.items = [];
        this.isError = response.data.teleported;
        this.appendLog(this.displayMessage || `移动到 ${this.currentRoomName}`, this.isError);
      } catch (error) {
        this.displayMessage = '移动错误：' + (error.response?.data?.message || error.message);
        this.isError = true;
        this.appendLog(this.displayMessage, true);
      } finally {
        this.isMoving = false;
        // 移动完成后重置小人位置（带延迟以配合动画）
        setTimeout(() => {
          if (this.$refs.roomView) {
            this.$refs.roomView.resetPosition(direction);
          }
        }, 50);
      }
    },
    async look() {
      try {
        const response = await look();
        this.currentRoomName = response.data.description;
        this.currentRoomId = response.data.roomId;
        this.longDescription = response.data.longDescription;
        this.exits = Array.from(response.data.exits);
        this.items = response.data.items || [];
        this.displayMessage = '';
        this.isError = false;
        this.appendLog(this.items.length ? `查看房间，发现 ${this.items.length} 个物品` : '查看房间，暂未发现物品');
      } catch (error) {
        this.displayMessage = '查看错误：' + error.message;
        this.isError = true;
        this.appendLog(this.displayMessage, true);
      }
    },
    async goBack() {
      try {
        const response = await goBack();
        this.displayMessage = response.data.message;
        this.currentRoomName = response.data.description;
        this.currentRoomId = response.data.roomId;
        this.longDescription = response.data.longDescription;
        this.exits = Array.from(response.data.exits);
        this.items = [];
        this.isError = !response.data.success;
        this.appendLog(this.displayMessage || '返回上个房间', this.isError);
      } catch (error) {
        this.displayMessage = '返回错误：' + error.message;
        this.isError = true;
        this.appendLog(this.displayMessage, true);
      }
    },
    getHelp() {
      window.open('/help.html', '_blank', 'width=600,height=500');
    },
    // 查看背包
    async showInventory() {
      try {
        const response = await getItems();
        this.inventory = response.data.inventory || [];
        this.playerWeight = response.data.playerWeight || 0;
        this.playerMaxWeight = response.data.playerMaxWeight || 50;
        this.syncSelectedInventory();
        this.displayMessage = response.data.message;
        this.appendLog(this.displayMessage || `查看背包，共 ${this.inventory.length} 个物品`);
      } catch (error) {
        this.displayMessage = '查看背包失败：' + error.message;
        this.isError = true;
        this.appendLog(this.displayMessage, true);
      }
    },
    // 拾取物品
    async handleTake(itemId) {
      try {
        const response = await takeItem(itemId);
        this.displayMessage = response.data.message;
        this.items = response.data.items || [];
        this.inventory = response.data.inventory || [];
        this.playerWeight = response.data.playerWeight || 0;
        this.playerMaxWeight = response.data.playerMaxWeight || 50;
        this.isError = !response.data.success;
        this.syncSelectedInventory();
        this.appendLog(this.displayMessage || '拾取物品', this.isError);
      } catch (error) {
        this.displayMessage = '拾取失败：' + error.message;
        this.isError = true;
        this.appendLog(this.displayMessage, true);
      }
    },
    // 丢弃物品
    async handleDrop(itemId) {
      try {
        const response = await dropItem(itemId);
        this.displayMessage = response.data.message;
        this.items = response.data.items || [];
        this.inventory = response.data.inventory || [];
        this.playerWeight = response.data.playerWeight || 0;
        this.playerMaxWeight = response.data.playerMaxWeight || 50;
        this.isError = !response.data.success;
        this.syncSelectedInventory();
        this.appendLog(this.displayMessage || '丢弃物品', this.isError);
      } catch (error) {
        this.displayMessage = '丢弃失败：' + error.message;
        this.isError = true;
        this.appendLog(this.displayMessage, true);
      }
    },
    // 吃魔法饼干
    async handleEatCookie() {
      try {
        const response = await eatCookie();
        this.displayMessage = response.data.message;
        this.inventory = response.data.inventory || [];
        this.playerWeight = response.data.playerWeight || 0;
        this.playerMaxWeight = response.data.playerMaxWeight || 50;
        this.isError = !response.data.success;
        this.syncSelectedInventory();
        this.appendLog(this.displayMessage || '使用魔法饼干', this.isError);
      } catch (error) {
        this.displayMessage = '吃饼干失败：' + error.message;
        this.isError = true;
        this.appendLog(this.displayMessage, true);
      }
    },
    // 手动保存游戏进度
    async handleSave() {
      try {
        await saveGame(this.username, this.playerGridPosition);
        this.displayMessage = '💾 游戏已保存';
        this.isError = false;
        this.appendLog(this.displayMessage);
      } catch (e) {
        this.displayMessage = '保存失败';
        this.isError = true;
        this.appendLog(this.displayMessage, true);
      }
    },
    async handleLoad() {
      try {
        const response = await loadGame(this.username);
        this.displayMessage = response.data.message || '已读取存档';
        this.isError = !response.data.success;
        if (!response.data.success) {
          this.appendLog(this.displayMessage, true);
          return;
        }
        this.updateGameState(response.data);
        this.appendLog('读取存档成功');
        await this.fetchMap(response.data.roomId);
        await this.fetchUserAbility();
      } catch (error) {
        this.displayMessage = '读取存档失败：' + error.message;
        this.isError = true;
        this.appendLog(this.displayMessage, true);
      }
    },
    // 获取用户金币和能力信息
    async fetchUserAbility() {
      try {
        const userResp = await getUserByUsername(this.username);
        if (!userResp.data.success) {
          console.error('获取用户信息失败:', userResp.data.message);
          return;
        }
        const userId = userResp.data.id;
        console.log('获取到用户ID:', userId);
        const [abilityResp, configsResp] = await Promise.all([
          getUserAbility(userId),
          getAbilityConfigs()
        ]);
        console.log('能力响应:', abilityResp.data);
        this.userAbility = abilityResp.data;
        this.userGold = abilityResp.data.gold || 0;
        this.abilityConfigs = configsResp.data;
        this.abilityLevels = {
          maxWeightLevel: abilityResp.data.maxWeightLevel || 1,
          goldBonusLevel: abilityResp.data.goldBonusLevel || 1,
          moveSpeedLevel: abilityResp.data.moveSpeedLevel || 1
        };
        // 计算移动速度
        const speedConfig = configsResp.data.find(c => c.abilityCode === 'move_speed');
        if (speedConfig) {
          const speedLevel = abilityResp.data.moveSpeedLevel || 1;
          const baseSpeed = 0.5;
const increment = 0.15;
          this.currentMoveSpeed = baseSpeed + (speedLevel - 1) * increment;
        }
        console.log('用户金币:', this.userGold);
      } catch (error) {
        console.error('获取能力信息失败:', error);
      }
    },
    // 探索结算
    async handleSettle() {
      this.showSettleConfirm = false;
      await this.$refs.roomView?.playCheckout();
      try {
        const response = await settleExploration(this.username);
        if (response.data.success) {
          this.displayMessage = response.data.message;
          this.userGold = response.data.totalGold;
          this.inventory = [];
          this.selectedInventoryId = '';
          this.playerWeight = 0;
          this.playerMaxWeight = response.data.playerMaxWeight;
          this.isError = false;
          await this.fetchUserAbility();
          await newGame(this.username);
          this.showGameStart = true;
          this.appendLog(this.displayMessage);
        } else {
          this.displayMessage = response.data.message;
          this.isError = true;
          this.appendLog(this.displayMessage, true);
        }
      } catch (error) {
        this.displayMessage = '结算失败：' + error.message;
        this.isError = true;
        this.appendLog(this.displayMessage, true);
      }
    },
    // 升级能力
    async handleUpgrade(abilityCode) {
      try {
        const userResp = await getUserByUsername(this.username);
        if (!userResp.data.success) return;
        const userId = userResp.data.id;
        const response = await upgradeAbility(userId, abilityCode);
        if (response.data.success) {
          this.displayMessage = response.data.message;
          this.userGold = response.data.remainingGold;
          await this.fetchUserAbility();
          if (abilityCode === 'max_weight') {
            const statsResp = await getUserStats(userId);
            console.log('升级后 maxWeight:', statsResp.data.maxWeight);
            this.playerMaxWeight = statsResp.data.maxWeight;
            console.log('this.playerMaxWeight 已更新为:', this.playerMaxWeight);
          }
          this.isError = false;
          this.appendLog(this.displayMessage);
        } else {
          this.displayMessage = response.data.message;
          this.isError = true;
          this.appendLog(this.displayMessage, true);
        }
      } catch (error) {
        this.displayMessage = '升级失败：' + error.message;
        this.isError = true;
        this.appendLog(this.displayMessage, true);
      }
    },
    // 计算升级所需金币
    calculateUpgradeCost(abilityCode) {
      const config = this.abilityConfigs.find(c => c.abilityCode === abilityCode);
      if (!config || !this.userAbility) return 0;
      const level = this.userAbility[config.abilityCode + 'Level'] || 1;
      if (level >= config.maxLevel) return null;
      return Math.floor(config.baseCost * Math.pow(config.costMultiplier, level - 1));
    },
    // 获取能力当前值
    getAbilityValue(abilityCode) {
      const config = this.abilityConfigs.find(c => c.abilityCode === abilityCode);
      if (!config || !this.userAbility) return config?.baseValue || 0;
      const level = this.userAbility[config.abilityCode + 'Level'] || 1;
      return config.baseValue + config.incrementPerLevel * (level - 1);
    }
  },
  computed: {
    inventoryTotalValue() {
      return this.inventory.reduce((sum, item) => sum + (item.value || 0), 0);
    },
    // 预期结算收益（含金币加成）
    expectedSettleGold() {
      const baseGold = this.inventoryTotalValue;
      const bonusLevel = this.abilityLevels?.goldBonusLevel || 1;
      const bonusPercent = (bonusLevel - 1) * 10;
      const bonus = Math.floor(baseGold * bonusPercent / 100);
      return baseGold + bonus;
    }
  }
};
</script>

<style>
* {
  box-sizing: border-box;
}

#app {
  font-family: 'Microsoft YaHei', 'Segoe UI', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  text-align: center;
  color: #2c3e50;
  padding: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  outline: none;
}

h1 {
  color: white;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
  margin: 0;
  padding: 8px;
  font-size: 22px;
}

/* 顶部栏 */
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 20px;
  background: rgba(0,0,0,0.2);
}

.username {
  color: white;
  font-weight: bold;
  font-size: 16px;
}

.btn-menu {
  padding: 8px 16px;
  background: rgba(156, 39, 176, 0.8);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  transition: all 0.2s;
  margin-right: 10px;
}

.btn-menu:hover {
  background: #7B1FA2;
  transform: scale(1.05);
}

.btn-logout {
  padding: 8px 16px;
  background: rgba(231, 76, 60, 0.8);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  transition: all 0.2s;
}

.btn-logout:hover {
  background: #e74c3c;
  transform: scale(1.05);
}

.gold-display {
  background: rgba(255, 215, 0, 0.3);
  padding: 5px 15px;
  border-radius: 20px;
  color: #ffd700;
  font-weight: bold;
  font-size: 14px;
}

.btn-settle {
  background: linear-gradient(135deg, #9b59b6, #8e44ad);
}

.btn-settle:hover {
  background: linear-gradient(135deg, #8e44ad, #9b59b6);
}

.btn-ability {
  background: linear-gradient(135deg, #f39c12, #e67e22);
}

.btn-ability:hover {
  background: linear-gradient(135deg, #e67e22, #f39c12);
}

/* 整体布局 */
.game-wrapper {
  display: flex;
  gap: 15px;
  padding: 0 15px 15px;
  max-width: 1600px;
  margin: 0 auto;
  height: calc(100vh - 60px);
}

/* 左侧主区域 */
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
}

/* 房间视图包装 */
.room-wrapper {
  flex: 1;
  position: relative;
  min-height: 0;
}

/* 浮动功能按钮 */
.float-buttons {
  position: absolute;
  top: 15px;
  right: 15px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 20;
}

.btn-float {
  padding: 10px 18px;
  font-size: 14px;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: bold;
  box-shadow: 0 3px 10px rgba(0,0,0,0.3);
}

.btn-float:nth-child(1) { background: #4CAF50; }
.btn-float:nth-child(2) { background: #9C27B0; }
.btn-float:nth-child(3) { background: #2196F3; }
.btn-float:hover { transform: scale(1.05); opacity: 0.9; }

/* 底部状态栏 */
.status-bar {
  background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
  border-radius: 12px;
  padding: 15px 20px;
  color: white;
  text-align: left;
  box-shadow: 0 4px 15px rgba(0,0,0,0.3);
}

.status-bar.error {
  background: linear-gradient(135deg, #c0392b 0%, #e74c3c 100%);
}

.status-location {
  font-size: 22px;
  font-weight: bold;
  margin-bottom: 8px;
}

.status-message {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 6px;
  line-height: 1.5;
}

.status-exits {
  font-size: 14px;
  opacity: 0.7;
}

/* 右侧信息面板 */
.side-panel {
  width: 280px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  flex-shrink: 0;
}

/* 控制区域 */
.controls-section {
  background: rgba(44, 62, 80, 0.9);
  border-radius: 12px;
  padding: 10px;
}

/* 物品面板 */
.items-panel {
  background: rgba(255,255,255,0.95);
  border-radius: 12px;
  padding: 18px;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  text-align: left;
  flex: 1;
  overflow-y: auto;
  min-height: 200px;
}

.items-header {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 15px;
}

.items-panel h3 {
  margin: 0;
  font-size: 18px;
  color: #2c3e50;
}

.items-tabs {
  display: flex;
  gap: 8px;
}

.items-tabs button {
  flex: 1;
  padding: 8px 12px;
  font-size: 13px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  background: #e0e0e0;
  color: #666;
  transition: all 0.2s;
}

.items-tabs button.active {
  background: #4CAF50;
  color: white;
  font-weight: bold;
}

.items-tabs button:hover:not(.active) {
  background: #bdbdbd;
}

.item-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.empty-msg {
  text-align: center;
  color: #999;
  padding: 25px;
  font-size: 14px;
}

.item-card {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 12px;
}

.item-main {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-name {
  font-weight: bold;
  font-size: 15px;
  color: #2c3e50;
}

.item-desc {
  font-size: 13px;
  color: #7f8c8d;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.item-stats {
  font-size: 13px;
  color: #e74c3c;
  font-weight: bold;
}

.btn-take, .btn-drop {
  padding: 6px 14px;
  font-size: 13px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.2s;
}

.btn-take {
  background: #4CAF50;
  color: white;
}

.btn-take:hover {
  background: #388E3C;
}

.btn-drop {
  background: #ff9800;
  color: white;
}

.btn-drop:hover {
  background: #f57c00;
}

.items-actions {
  margin-top: 15px;
  padding-top: 12px;
  border-top: 1px solid #eee;
}

.btn-items {
  width: 100%;
  padding: 10px;
  font-size: 14px;
  background: #2196F3;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
}

.btn-items:hover {
  background: #1976D2;
}

.btn-cookie {
  width: 100%;
  padding: 12px;
  font-size: 14px;
  background: linear-gradient(135deg, #FF6B6B, #FF8E53);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  margin-top: 12px;
}

.btn-cookie:hover {
  background: linear-gradient(135deg, #FF5252, #FF7043);
}

/* 键盘提示 */
.keyboard-hint {
  margin-top: auto;
  padding: 10px;
  color: rgba(255,255,255,0.8);
  font-size: 12px;
  text-align: center;
}

/* 悬浮物品面板 */
.floating-items-panel {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.floating-items-content {
  background: white;
  border-radius: 16px;
  padding: 24px;
  width: 90%;
  max-width: 500px;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 10px 40px rgba(0,0,0,0.3);
}

.floating-items-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #eee;
}

.floating-items-header h3 {
  margin: 0;
  font-size: 22px;
  color: #333;
}

.btn-close {
  background: #ff5252;
  color: white;
  border: none;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
  transition: all 0.2s;
}

.btn-close:hover {
  background: #ff1744;
  transform: scale(1.1);
}

.floating-item-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.floating-item-card {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 16px;
  border: 1px solid #e0e0e0;
}

.floating-item-card .item-main {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.floating-item-card .item-name {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.floating-item-card .item-desc {
  font-size: 14px;
  color: #666;
}

.floating-item-card .item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e0e0e0;
}

.floating-item-card .item-stats {
  font-size: 13px;
  color: #888;
}

.floating-item-card .btn-take,
.floating-item-card .btn-drop {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  transition: all 0.2s;
}

.floating-item-card .btn-take {
  background: #4CAF50;
  color: white;
}

.floating-item-card .btn-take:hover {
  background: #45a049;
}

.floating-item-card .btn-drop {
  background: #ff9800;
  color: white;
}

.floating-item-card .btn-drop:hover {
  background: #fb8c00;
}

.btn-cookie {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #ff6b6b, #ff8e53);
  color: white;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  font-size: 16px;
  font-weight: bold;
  margin-top: 15px;
  transition: all 0.2s;
}

.btn-cookie:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(255,107,107,0.4);
}

.weight-bar-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}

.weight-bar {
  flex: 1;
  height: 10px;
  background: #e0e0e0;
  border-radius: 5px;
  overflow: hidden;
}

.weight-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #4CAF50, #8BC34A);
  border-radius: 5px;
  transition: width 0.3s ease;
}

.weight-text {
  font-size: 14px;
  color: #666;
  font-weight: bold;
  white-space: nowrap;
}

.value-info {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
}

.value-label {
  font-size: 13px;
  color: #888;
}

.value-amount {
  font-size: 16px;
  font-weight: bold;
  color: #FF9800;
}

.empty-msg {
  text-align: center;
  padding: 30px;
  color: #999;
  font-size: 16px;
}

/* 地图按钮 */
.map-toggle-btn {
  padding: 10px 18px;
  background: #607D8B;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  box-shadow: 0 3px 10px rgba(0,0,0,0.2);
}

.map-toggle-btn:hover {
  background: #455A64;
}

.map-toggle-btn.btn-back {
  background: #9C27B0;
}

.map-toggle-btn.btn-back:hover {
  background: #7B1FA2;
}

.map-toggle-btn.btn-save {
  background: #FF9800;
}

.map-toggle-btn.btn-save:hover {
  background: #F57C00;
}

.map-toggle-btn.btn-look {
  background: #4CAF50;
}

.map-toggle-btn.btn-look:hover {
  background: #45a049;
}

/* 悬浮地图 */
.floating-map {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.floating-map-content {
  background: white;
  border-radius: 10px;
  padding: 20px;
  width: 95vw;
  height: 90vh;
  max-width: 1300px;
  max-height: 900px;
  overflow: auto;
}
</style>
