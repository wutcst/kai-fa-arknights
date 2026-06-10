package cn.edu.whut.sept.zuul.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 能力配置实体类.
 * 定义游戏中各种能力的配置参数.
 */
@Entity
@Table(name = "ability_config")
public class AbilityConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ability_code", nullable = false, unique = true, length = 30)
    private String abilityCode;

    @Column(name = "ability_name", nullable = false, length = 50)
    private String abilityName;

    @Column(name = "base_value", nullable = false)
    private Integer baseValue;

    @Column(name = "increment_per_level", nullable = false)
    private Integer incrementPerLevel;

    @Column(name = "base_cost", nullable = false)
    private Integer baseCost;

    @Column(name = "cost_multiplier", nullable = false, precision = 3, scale = 1)
    private BigDecimal costMultiplier;

    @Column(name = "max_level", nullable = false)
    private Integer maxLevel;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public AbilityConfig() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAbilityCode() {
        return abilityCode;
    }

    public void setAbilityCode(String abilityCode) {
        this.abilityCode = abilityCode;
    }

    public String getAbilityName() {
        return abilityName;
    }

    public void setAbilityName(String abilityName) {
        this.abilityName = abilityName;
    }

    public Integer getBaseValue() {
        return baseValue;
    }

    public void setBaseValue(Integer baseValue) {
        this.baseValue = baseValue;
    }

    public Integer getIncrementPerLevel() {
        return incrementPerLevel;
    }

    public void setIncrementPerLevel(Integer incrementPerLevel) {
        this.incrementPerLevel = incrementPerLevel;
    }

    public Integer getBaseCost() {
        return baseCost;
    }

    public void setBaseCost(Integer baseCost) {
        this.baseCost = baseCost;
    }

    public BigDecimal getCostMultiplier() {
        return costMultiplier;
    }

    public void setCostMultiplier(BigDecimal costMultiplier) {
        this.costMultiplier = costMultiplier;
    }

    public Integer getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(Integer maxLevel) {
        this.maxLevel = maxLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 计算指定等级的能力升级费用.
     *
     * @param currentLevel 当前等级
     * @return 升级到下一级需要的金币
     */
    public int calculateCost(int currentLevel) {
        return (int) (baseCost * Math.pow(costMultiplier.doubleValue(), currentLevel - 1));
    }

    /**
     * 计算指定等级的能力效果值.
     *
     * @param level 等级
     * @return 该等级的能力效果值
     */
    public int calculateValue(int level) {
        return baseValue + incrementPerLevel * (level - 1);
    }
}
