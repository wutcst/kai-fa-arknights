package cn.edu.whut.sept.zuul.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户能力实体类.
 * 存储用户的能力等级、金币等信息.
 */
@Entity
@Table(name = "user_abilities")
public class UserAbility {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "gold", nullable = false)
    private Integer gold = 0;

    @Column(name = "max_weight_level", nullable = false)
    private Integer maxWeightLevel = 1;

    @Column(name = "gold_bonus_level", nullable = false)
    private Integer goldBonusLevel;

    @Column(name = "move_speed_level", nullable = false)
    private Integer moveSpeedLevel;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UserAbility() {
        this.gold = 0;
        this.maxWeightLevel = 1;
        this.goldBonusLevel = 1;
        this.moveSpeedLevel = 1;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public UserAbility(Long userId) {
        this();
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getGold() {
        return gold;
    }

    public void setGold(Integer gold) {
        this.gold = gold;
    }

    public Integer getMaxWeightLevel() {
        return maxWeightLevel;
    }

    public void setMaxWeightLevel(Integer maxWeightLevel) {
        this.maxWeightLevel = maxWeightLevel;
    }

    public Integer getGoldBonusLevel() {
        return goldBonusLevel;
    }

    public void setGoldBonusLevel(Integer goldBonusLevel) {
        this.goldBonusLevel = goldBonusLevel;
    }

    public Integer getMoveSpeedLevel() {
        return moveSpeedLevel;
    }

    public void setMoveSpeedLevel(Integer moveSpeedLevel) {
        this.moveSpeedLevel = moveSpeedLevel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 获取指定能力等级.
     *
     * @param abilityCode 能力代码（max_weight/gold_bonus/move_speed）
     * @return 能力等级，默认返回1
     */
    public int getAbilityLevel(String abilityCode) {
        switch (abilityCode) {
            case "max_weight":
                return maxWeightLevel;
            case "gold_bonus":
                return goldBonusLevel;
            case "move_speed":
                return moveSpeedLevel;
            default:
                return 1;
        }
    }

    /**
     * 设置指定能力等级.
     *
     * @param abilityCode 能力代码（max_weight/gold_bonus/move_speed）
     * @param level       要设置的等级
     */
    public void setAbilityLevel(String abilityCode, int level) {
        switch (abilityCode) {
            case "max_weight":
                this.maxWeightLevel = level;
                break;
            case "gold_bonus":
                this.goldBonusLevel = level;
                break;
            case "move_speed":
                this.moveSpeedLevel = level;
                break;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 增加金币.
     *
     * @param amount 增加的金币数量
     */
    public void addGold(int amount) {
        this.gold += amount;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 消耗金币.
     *
     * @param amount 消耗的金币数量
     * @return 是否成功消耗（金币不足时返回false）
     */
    public boolean spendGold(int amount) {
        if (this.gold >= amount) {
            this.gold -= amount;
            this.updatedAt = LocalDateTime.now();
            return true;
        }
        return false;
    }
}
