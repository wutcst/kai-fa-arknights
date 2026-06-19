package cn.edu.whut.sept.zuul.model.world;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "world_random_spawn_rules")
public class WorldRandomSpawnRule {
    @Id
    @Column(name = "rule_id", length = 50)
    private String ruleId;

    @Column(name = "item_id", nullable = false, length = 80)
    private String itemId;

    @Column(name = "min_count", nullable = false)
    private Integer minCount;

    @Column(name = "max_count", nullable = false)
    private Integer maxCount;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    public String getRuleId() {
        return ruleId;
    }

    public String getItemId() {
        return itemId;
    }

    public Integer getMinCount() {
        return minCount;
    }

    public Integer getMaxCount() {
        return maxCount;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }
}
