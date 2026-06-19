package cn.edu.whut.sept.zuul.model.world;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "world_rooms")
public class WorldRoom {
    @Id
    @Column(name = "room_id", length = 50)
    private String roomId;

    @Column(name = "area_id", nullable = false, length = 50)
    private String areaId;

    @Column(name = "description", nullable = false, length = 200)
    private String description;

    @Column(name = "zh_name", nullable = false, length = 50)
    private String zhName;

    @Column(name = "room_type", nullable = false, length = 30)
    private String roomType;

    @Column(name = "floor_no", nullable = false)
    private Integer floorNo;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    public String getRoomId() {
        return roomId;
    }

    public String getAreaId() {
        return areaId;
    }

    public String getDescription() {
        return description;
    }

    public String getZhName() {
        return zhName;
    }

    public String getRoomType() {
        return roomType;
    }

    public Integer getFloorNo() {
        return floorNo;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }
}
