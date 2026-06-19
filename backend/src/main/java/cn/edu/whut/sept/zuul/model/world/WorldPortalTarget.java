package cn.edu.whut.sept.zuul.model.world;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "world_portal_targets",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_world_portal_target", columnNames = {"portal_room_id", "target_room_id"}),
                @UniqueConstraint(name = "uk_world_portal_target_order", columnNames = {"portal_room_id", "display_order"})
        })
public class WorldPortalTarget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "portal_room_id", nullable = false, length = 50)
    private String portalRoomId;

    @Column(name = "target_room_id", nullable = false, length = 50)
    private String targetRoomId;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    public Long getId() {
        return id;
    }

    public String getPortalRoomId() {
        return portalRoomId;
    }

    public String getTargetRoomId() {
        return targetRoomId;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }
}
