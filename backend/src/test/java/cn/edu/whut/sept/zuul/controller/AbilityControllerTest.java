package cn.edu.whut.sept.zuul.controller;

import cn.edu.whut.sept.zuul.model.AbilityConfig;
import cn.edu.whut.sept.zuul.model.UserAbility;
import cn.edu.whut.sept.zuul.repository.AbilityConfigRepository;
import cn.edu.whut.sept.zuul.repository.UserAbilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 能力接口回归测试.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AbilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AbilityConfigRepository abilityConfigRepository;

    @Autowired
    private UserAbilityRepository userAbilityRepository;

    @BeforeEach
    void setUp() {
        userAbilityRepository.deleteAll();
        abilityConfigRepository.deleteAll();
        saveConfig("max_weight", "负重上限", 5, 3, 50, 1.8, 10);
        saveConfig("gold_bonus", "龙门币加成", 0, 5, 80, 2.0, 10);
        saveConfig("move_speed", "移动速度", 2, 1, 100, 2.2, 5);
    }

    @Test
    void testGetAllConfigsReturnsAbilityConfigs() throws Exception {
        mockMvc.perform(get("/api/ability/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.abilityCode == 'max_weight')]").exists())
                .andExpect(jsonPath("$[?(@.abilityCode == 'gold_bonus')]").exists())
                .andExpect(jsonPath("$[?(@.abilityCode == 'move_speed')]").exists());
    }

    @Test
    void testGetUserAbilityCreatesDefaultRecord() throws Exception {
        mockMvc.perform(get("/api/ability/user/{userId}", 2001L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(2001))
                .andExpect(jsonPath("$.gold").value(0))
                .andExpect(jsonPath("$.maxWeightLevel").value(1))
                .andExpect(jsonPath("$.goldBonusLevel").value(1))
                .andExpect(jsonPath("$.moveSpeedLevel").value(1));
    }

    @Test
    void testGetUserStatsUsesAbilityConfigValues() throws Exception {
        mockMvc.perform(get("/api/ability/stats/{userId}", 2002L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxWeight").value(5))
                .andExpect(jsonPath("$.goldBonusPercent").value(0))
                .andExpect(jsonPath("$.moveSpeed").value(2));
    }

    @Test
    void testUpgradeAbilityFailsWhenGoldIsNotEnough() throws Exception {
        userAbilityRepository.save(new UserAbility(2003L));

        mockMvc.perform(post("/api/ability/upgrade")
                        .param("userId", "2003")
                        .param("abilityCode", "max_weight"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("金币不足，需要 50 金币"));
    }

    @Test
    void testUpgradeAbilityConsumesGoldAndIncreasesLevel() throws Exception {
        UserAbility ability = new UserAbility(2004L);
        ability.setGold(100);
        userAbilityRepository.save(ability);

        mockMvc.perform(post("/api/ability/upgrade")
                        .param("userId", "2004")
                        .param("abilityCode", "max_weight"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.newLevel").value(2))
                .andExpect(jsonPath("$.newValue").value(8))
                .andExpect(jsonPath("$.remainingGold").value(50));
    }

    @Test
    void testSettleExplorationAddsGoldWithBonus() throws Exception {
        UserAbility ability = new UserAbility(2005L);
        ability.setGoldBonusLevel(2);
        userAbilityRepository.save(ability);

        mockMvc.perform(post("/api/ability/settle")
                        .param("userId", "2005")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"id\":\"gold\",\"name\":\"赤金\",\"description\":\"测试物品\",\"weight\":1,\"value\":100}]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baseGold").value(100))
                .andExpect(jsonPath("$.bonusPercent").value(5))
                .andExpect(jsonPath("$.bonusGold").value(5))
                .andExpect(jsonPath("$.goldEarned").value(105))
                .andExpect(jsonPath("$.totalGold").value(105))
                .andExpect(jsonPath("$.itemsSettled").value(1));
    }

    private void saveConfig(String code, String name, int baseValue, int increment, int baseCost,
                            double multiplier, int maxLevel) {
        AbilityConfig config = new AbilityConfig();
        config.setAbilityCode(code);
        config.setAbilityName(name);
        config.setBaseValue(baseValue);
        config.setIncrementPerLevel(increment);
        config.setBaseCost(baseCost);
        config.setCostMultiplier(BigDecimal.valueOf(multiplier));
        config.setMaxLevel(maxLevel);
        config.setDescription(name);
        abilityConfigRepository.save(config);
    }
}
