package cn.edu.whut.sept.zuul.model.world;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "world_item_effects",
        uniqueConstraints = @UniqueConstraint(name = "uk_world_item_effect", columnNames = {"item_id", "effect_code"}))
public class WorldItemEffect {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_id", nullable = false, length = 80)
    private String itemId;

    @Column(name = "effect_code", nullable = false, length = 50)
    private String effectCode;

    @Column(name = "effect_value", nullable = false)
    private Integer effectValue;

    public Long getId() {
        return id;
    }

    public String getItemId() {
        return itemId;
    }

    public String getEffectCode() {
        return effectCode;
    }

    public Integer getEffectValue() {
        return effectValue;
    }
}
