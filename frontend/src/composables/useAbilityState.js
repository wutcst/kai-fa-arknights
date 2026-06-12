import { ref } from 'vue';
import { getUserByUsername } from '@/api/authApi';
import { getUserAbility, getAbilityConfigs, upgradeAbility, getUserStats } from '@/api/abilityApi';

export function useAbilityState(options) {
  const { appendLog, onMaxWeightUpdated } = options;

  const userGold = ref(0);
  const userAbility = ref(null);
  const abilityConfigs = ref([]);
  const abilityLevels = ref({
    maxWeightLevel: 1,
    goldBonusLevel: 1,
    moveSpeedLevel: 1
  });
  const currentMoveSpeed = ref(0.5);

  const fetchUserAbility = async (username) => {
    try {
      const userResp = await getUserByUsername(username);
      if (!userResp.data.success) {
        console.error('获取用户信息失败:', userResp.data.message);
        return;
      }
      const userId = userResp.data.id;
      const [abilityResp, configsResp] = await Promise.all([
        getUserAbility(userId),
        getAbilityConfigs()
      ]);
      userAbility.value = abilityResp.data;
      userGold.value = abilityResp.data.gold || 0;
      abilityConfigs.value = configsResp.data;
      abilityLevels.value = {
        maxWeightLevel: abilityResp.data.maxWeightLevel || 1,
        goldBonusLevel: abilityResp.data.goldBonusLevel || 1,
        moveSpeedLevel: abilityResp.data.moveSpeedLevel || 1
      };
      const speedConfig = configsResp.data.find(c => c.abilityCode === 'move_speed');
      if (speedConfig) {
        const speedLevel = abilityResp.data.moveSpeedLevel || 1;
        const baseSpeed = 0.5;
        const increment = 0.15;
        currentMoveSpeed.value = baseSpeed + (speedLevel - 1) * increment;
      }
    } catch (error) {
      console.error('获取能力信息失败:', error);
    }
  };

  const handleUpgrade = async (username, abilityCode, extraOptions = {}) => {
    try {
      const userResp = await getUserByUsername(username);
      if (!userResp.data.success) return;
      const userId = userResp.data.id;
      const response = await upgradeAbility(userId, abilityCode);
      if (response.data.success) {
        if (extraOptions.setDisplayMessage) {
          extraOptions.setDisplayMessage(response.data.message);
        }
        if (extraOptions.setIsError) {
          extraOptions.setIsError(false);
        }
        userGold.value = response.data.remainingGold;
        await fetchUserAbility(username);
        if (abilityCode === 'max_weight') {
          const statsResp = await getUserStats(userId);
          if (onMaxWeightUpdated) {
            onMaxWeightUpdated(statsResp.data.maxWeight);
          }
        }
        if (appendLog) {
          appendLog(response.data.message);
        }
      } else {
        if (extraOptions.setDisplayMessage) {
          extraOptions.setDisplayMessage(response.data.message);
        }
        if (extraOptions.setIsError) {
          extraOptions.setIsError(true);
        }
        if (appendLog) {
          appendLog(response.data.message, true);
        }
      }
    } catch (error) {
      let msg = '升级失败：' + error.message;
      if (extraOptions.setDisplayMessage) {
        extraOptions.setDisplayMessage(msg);
      }
      if (extraOptions.setIsError) {
        extraOptions.setIsError(true);
      }
      if (appendLog) {
        appendLog(msg, true);
      }
    }
  };

  const calculateUpgradeCost = (abilityCode) => {
    const config = abilityConfigs.value.find(c => c.abilityCode === abilityCode);
    if (!config || !userAbility.value) return 0;
    const level = userAbility.value[config.abilityCode + 'Level'] || 1;
    if (level >= config.maxLevel) return null;
    return Math.floor(config.baseCost * Math.pow(config.costMultiplier, level - 1));
  };

  const getAbilityValue = (abilityCode) => {
    const config = abilityConfigs.value.find(c => c.abilityCode === abilityCode);
    if (!config || !userAbility.value) return config?.baseValue || 0;
    const level = userAbility.value[config.abilityCode + 'Level'] || 1;
    return config.baseValue + config.incrementPerLevel * (level - 1);
  };

  return {
    userGold,
    userAbility,
    abilityConfigs,
    abilityLevels,
    currentMoveSpeed,
    fetchUserAbility,
    handleUpgrade,
    calculateUpgradeCost,
    getAbilityValue
  };
}
