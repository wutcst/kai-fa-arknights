package cn.edu.whut.sept.zuul.model.world;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "world_room_initial_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_world_room_item", columnNames = {"room_id", "item_id"}),
                @UniqueConstraint(name = "uk_world_room_item_grid", columnNames = {"room_id", "grid_row", "grid_col"})
        })
public class WorldRoomInitialItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false, length = 50)
    private String roomId;

    @Column(name = "item_id", nullable = false, length = 80)
    private String itemId;

    @Column(name = "grid_row", nullable = false)
    private Integer gridRow;

    @Column(name = "grid_col", nullable = false)
    private Integer gridCol;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    public Long getId() {
        return id;
    }

    public String getRoomId() {
        return roomId;
    }

    public String getItemId() {
        return itemId;
    }

    public Integer getGridRow() {
        return gridRow;
    }

    public Integer getGridCol() {
        return gridCol;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }
}
