package cn.edu.whut.sept.zuul.service;

import cn.edu.whut.sept.zuul.model.AbilityConfig;
import cn.edu.whut.sept.zuul.model.Item;
import cn.edu.whut.sept.zuul.model.UserAbility;
import cn.edu.whut.sept.zuul.repository.AbilityConfigRepository;
import cn.edu.whut.sept.zuul.repository.UserAbilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 能力服务层.
 * 处理用户能力、升级、金币结算等业务逻辑.
 */
@Service
public class AbilityService {

    @Autowired
    private UserAbilityRepository userAbilityRepository;

    @Autowired
    private AbilityConfigRepository abilityConfigRepository;

    /**
     * 获取或创建用户能力记录.
     *
     * @param userId 用户ID
     * @return 用户能力对象
     */
    public UserAbility getOrCreateUserAbility(Long userId) {
        Optional<UserAbility> existing = userAbilityRepository.findByUserId(userId);
        if (existing.isPresent()) {
            return existing.get();
        }
        UserAbility newAbility = new UserAbility(userId);
        return userAbilityRepository.save(newAbility);
    }

    /**
     * 获取用户能力信息.
     *
     * @param userId 用户ID
     * @return 用户能力对象
     */
    public UserAbility getUserAbility(Long userId) {
        return getOrCreateUserAbility(userId);
    }

    /**
     * 获取所有能力配置.
     *
     * @return 能力配置列表
     */
    public List<AbilityConfig> getAllAbilityConfigs() {
        return abilityConfigRepository.findAll();
    }

    /**
     * 根据能力代码获取能力配置.
     *
     * @param abilityCode 能力代码
     * @return 能力配置对象
     */
    public AbilityConfig getAbilityConfig(String abilityCode) {
        return abilityConfigRepository.findByAbilityCode(abilityCode).orElse(null);
    }

    /**
     * 结算探索奖励.
     * 根据背包物品计算金币奖励，并应用金币加成百分比.
     *
     * @param userId    用户ID
     * @param inventory 背包物品列表
     * @return 结算结果信息
     */
    @Transactional
    public Map<String, Object> settleExploration(Long userId, List<Item> inventory) {
        Map<String, Object> result = new HashMap<>();

        UserAbility userAbility = getOrCreateUserAbility(userId);

        int totalValue = 0;
        for (Item item : inventory) {
            totalValue += item.getValue();
        }

        int goldBonusPercent = getGoldBonusPercent(userId);
        int bonusGold = (int) (totalValue * goldBonusPercent / 100.0);
        int totalEarned = totalValue + bonusGold;

        userAbility.addGold(totalEarned);
        userAbilityRepository.save(userAbility);

        result.put("goldEarned", totalEarned);
        result.put("baseGold", totalValue);
        result.put("bonusGold", bonusGold);
        result.put("bonusPercent", goldBonusPercent);
        result.put("totalGold", userAbility.getGold());
        result.put("itemsSettled", inventory.size());

        return result;
    }

    /**
     * 升级能力.
     * 消耗金币提升指定能力的等级.
     *
     * @param userId      用户ID
     * @param abilityCode 能力代码
     * @return 升级结果信息
     */
    @Transactional
    public Map<String, Object> upgradeAbility(Long userId, String abilityCode) {
        Map<String, Object> result = new HashMap<>();

        UserAbility userAbility = getOrCreateUserAbility(userId);
        AbilityConfig config = getAbilityConfig(abilityCode);

        if (config == null) {
            result.put("success", false);
            result.put("message", "无效的能力类型");
            return result;
        }

        int currentLevel = userAbility.getAbilityLevel(abilityCode);
        if (currentLevel >= config.getMaxLevel()) {
            result.put("success", false);
            result.put("message", "已达到最大等级");
            return result;
        }

        int cost = config.calculateCost(currentLevel);
        if (!userAbility.spendGold(cost)) {
            result.put("success", false);
            result.put("message", "金币不足，需要 " + cost + " 金币");
            return result;
        }

        int newLevel = currentLevel + 1;
        userAbility.setAbilityLevel(abilityCode, newLevel);
        userAbilityRepository.saveAndFlush(userAbility);

        int newValue = config.calculateValue(newLevel);

        result.put("success", true);
        result.put("message", abilityCode + " 升级成功！");
        result.put("newLevel", newLevel);
        result.put("newValue", newValue);
        result.put("cost", cost);
        result.put("remainingGold", userAbility.getGold());

        return result;
    }

    /**
     * 获取用户最大负重.
     *
     * @param userId 用户ID
     * @return 最大负重值
     */
    public int getMaxWeight(Long userId) {
        UserAbility ua = getOrCreateUserAbility(userId);
        AbilityConfig config = getAbilityConfig("max_weight");
        if (config == null) return 20;
        return config.calculateValue(ua.getMaxWeightLevel());
    }

    /**
     * 获取用户金币加成百分比.
     *
     * @param userId 用户ID
     * @return 金币加成百分比
     */
    public int getGoldBonusPercent(Long userId) {
        UserAbility ua = getOrCreateUserAbility(userId);
        AbilityConfig config = getAbilityConfig("gold_bonus");
        if (config == null) return 0;
        return config.calculateValue(ua.getGoldBonusLevel());
    }

    /**
     * 获取用户移动速度.
     *
     * @param userId 用户ID
     * @return 移动速度值
     */
    public int getMoveSpeed(Long userId) {
        UserAbility ua = getOrCreateUserAbility(userId);
        AbilityConfig config = getAbilityConfig("move_speed");
        if (config == null) return 5;
        return config.calculateValue(ua.getMoveSpeedLevel());
    }
}
