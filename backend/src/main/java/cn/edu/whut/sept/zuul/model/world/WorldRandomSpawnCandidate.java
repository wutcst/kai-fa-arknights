package cn.edu.whut.sept.zuul.model.world;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "world_random_spawn_candidates",
        uniqueConstraints = @UniqueConstraint(name = "uk_world_spawn_candidate_room", columnNames = {"rule_id", "room_id"}))
public class WorldRandomSpawnCandidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_id", nullable = false, length = 50)
    private String ruleId;

    @Column(name = "room_id", nullable = false, length = 50)
    private String roomId;

    @Column(name = "grid_row", nullable = false)
    private Integer gridRow;

    @Column(name = "grid_col", nullable = false)
    private Integer gridCol;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    public Long getId() {
        return id;
    }

    public String getRuleId() {
        return ruleId;
    }

    public String getRoomId() {
        return roomId;
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
