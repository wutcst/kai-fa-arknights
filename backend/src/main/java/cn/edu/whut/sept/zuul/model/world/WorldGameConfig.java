package cn.edu.whut.sept.zuul.model.world;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "world_game_config")
public class WorldGameConfig {
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "start_room_id", nullable = false, length = 50)
    private String startRoomId;

    @Column(name = "default_max_weight", nullable = false)
    private Integer defaultMaxWeight;

    @Column(name = "default_player_grid_row", nullable = false)
    private Integer defaultPlayerGridRow;

    @Column(name = "default_player_grid_col", nullable = false)
    private Integer defaultPlayerGridCol;

    @Column(name = "spawn_random_seed", nullable = false)
    private Long spawnRandomSeed;

    @Column(name = "portal_random_seed", nullable = false)
    private Long portalRandomSeed;

    public Integer getId() {
        return id;
    }

    public String getStartRoomId() {
        return startRoomId;
    }

    public Integer getDefaultMaxWeight() {
        return defaultMaxWeight;
    }

    public Integer getDefaultPlayerGridRow() {
        return defaultPlayerGridRow;
    }

    public Integer getDefaultPlayerGridCol() {
        return defaultPlayerGridCol;
    }

    public Long getSpawnRandomSeed() {
        return spawnRandomSeed;
    }

    public Long getPortalRandomSeed() {
        return portalRandomSeed;
    }
}
