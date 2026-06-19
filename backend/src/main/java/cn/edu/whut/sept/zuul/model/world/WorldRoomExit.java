package cn.edu.whut.sept.zuul.model.world;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "world_room_exits",
        uniqueConstraints = @UniqueConstraint(name = "uk_world_room_exit", columnNames = {"source_room_id", "direction_code"}))
public class WorldRoomExit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_room_id", nullable = false, length = 50)
    private String sourceRoomId;

    @Column(name = "direction_code", nullable = false, length = 20)
    private String directionCode;

    @Column(name = "target_room_id", nullable = false, length = 50)
    private String targetRoomId;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    public Long getId() {
        return id;
    }

    public String getSourceRoomId() {
        return sourceRoomId;
    }

    public String getDirectionCode() {
        return directionCode;
    }

    public String getTargetRoomId() {
        return targetRoomId;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }
}
