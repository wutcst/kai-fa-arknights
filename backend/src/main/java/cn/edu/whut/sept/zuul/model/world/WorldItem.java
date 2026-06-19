package cn.edu.whut.sept.zuul.model.world;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "world_items")
public class WorldItem {
    @Id
    @Column(name = "item_id", length = 80)
    private String itemId;

    @Column(name = "item_name", nullable = false, length = 80)
    private String itemName;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "weight", nullable = false)
    private Integer weight;

    @Column(name = "item_value", nullable = false)
    private Integer value;

    @Column(name = "item_category", nullable = false, length = 30)
    private String itemCategory;

    @Column(name = "usable", nullable = false)
    private Boolean usable;

    public String getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getDescription() {
        return description;
    }

    public Integer getWeight() {
        return weight;
    }

    public Integer getValue() {
        return value;
    }

    public String getItemCategory() {
        return itemCategory;
    }

    public Boolean getUsable() {
        return usable;
    }

    public boolean isUsable() {
        return Boolean.TRUE.equals(usable);
    }
}
