package cn.edu.whut.sept.zuul.model.world;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "world_areas")
public class WorldArea {
    @Id
    @Column(name = "area_id", length = 50)
    private String areaId;

    @Column(name = "area_name", nullable = false, length = 50)
    private String areaName;

    @Column(name = "description", length = 200)
    private String description;

    public String getAreaId() {
        return areaId;
    }

    public String getAreaName() {
        return areaName;
    }

    public String getDescription() {
        return description;
    }
}
