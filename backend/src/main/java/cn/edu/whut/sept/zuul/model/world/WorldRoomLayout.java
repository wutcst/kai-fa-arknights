package cn.edu.whut.sept.zuul.model.world;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "world_room_layouts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_world_room_layout_view_room", columnNames = {"view_type", "room_id"}),
                @UniqueConstraint(name = "uk_world_room_layout_view_order", columnNames = {"view_type", "display_order"})
        })
public class WorldRoomLayout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "view_type", nullable = false, length = 30)
    private String viewType;

    @Column(name = "room_id", nullable = false, length = 50)
    private String roomId;

    @Column(name = "x", nullable = false)
    private Integer x;

    @Column(name = "y", nullable = false)
    private Integer y;

    @Column(name = "primary_view", nullable = false)
    private Boolean primaryView;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    public Long getId() {
        return id;
    }

    public String getViewType() {
        return viewType;
    }

    public String getRoomId() {
        return roomId;
    }

    public Integer getX() {
        return x;
    }

    public Integer getY() {
        return y;
    }

    public Boolean getPrimaryView() {
        return primaryView;
    }

    public boolean isPrimaryView() {
        return Boolean.TRUE.equals(primaryView);
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }
}
