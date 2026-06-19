package cn.edu.whut.sept.zuul.model.world;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "world_directions")
public class WorldDirection {
    @Id
    @Column(name = "direction_code", length = 20)
    private String directionCode;

    @Column(name = "direction_name", nullable = false, length = 20)
    private String directionName;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    public String getDirectionCode() {
        return directionCode;
    }

    public String getDirectionName() {
        return directionName;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }
}
