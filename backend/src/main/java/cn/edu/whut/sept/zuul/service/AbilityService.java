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

@Service
public class AbilityService {

    @Autowired
    private UserAbilityRepository userAbilityRepository;

    @Autowired
    private AbilityConfigRepository abilityConfigRepository;

    public UserAbility getOrCreateUserAbility(Long userId) {
        Optional<UserAbility> existing = userAbilityRepository.findByUserId(userId);
        if (existing.isPresent()) {
            return existing.get();
        }
        UserAbility newAbility = new UserAbility(userId);
        return userAbilityRepository.save(newAbility);
    }

    public UserAbility getUserAbility(Long userId) {
        return getOrCreateUserAbility(userId);
    }

    public List<AbilityConfig> getAllAbilityConfigs() {
        return abilityConfigRepository.findAll();
    }

    public AbilityConfig getAbilityConfig(String abilityCode) {
        return abilityConfigRepository.findByAbilityCode(abilityCode).orElse(null);
    }

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
        userAbilityRepository.save(userAbility);

        int newValue = config.calculateValue(newLevel);

        result.put("success", true);
        result.put("message", abilityCode + " 升级成功！");
        result.put("newLevel", newLevel);
        result.put("newValue", newValue);
        result.put("cost", cost);
        result.put("remainingGold", userAbility.getGold());

        return result;
    }

    public int getMaxWeight(Long userId) {
        UserAbility ua = getOrCreateUserAbility(userId);
        AbilityConfig config = getAbilityConfig("max_weight");
        if (config == null) return 20;
        return config.calculateValue(ua.getMaxWeightLevel());
    }

    public int getGoldBonusPercent(Long userId) {
        UserAbility ua = getOrCreateUserAbility(userId);
        AbilityConfig config = getAbilityConfig("gold_bonus");
        if (config == null) return 0;
        return config.calculateValue(ua.getGoldBonusLevel());
    }

    public int getMoveSpeed(Long userId) {
        UserAbility ua = getOrCreateUserAbility(userId);
        AbilityConfig config = getAbilityConfig("move_speed");
        if (config == null) return 5;
        return config.calculateValue(ua.getMoveSpeedLevel());
    }
}
