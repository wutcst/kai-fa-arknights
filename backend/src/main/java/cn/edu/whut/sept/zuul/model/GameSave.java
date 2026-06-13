package cn.edu.whut.sept.zuul.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 游戏存档实体类.
 * 存储用户的游戏进度.
 */
@Entity
@Table(name = "game_saves")
public class GameSave {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "current_room_id", nullable = false)
    private String currentRoomId;

    @Column(name = "player_inventory", columnDefinition = "JSON")
    private String playerInventory;  // JSON序列化

    @Column(name = "player_weight")
    private Integer playerWeight = 0;

    @Column(name = "player_max_weight")
    private Integer playerMaxWeight = 20;

    @Column(name = "player_grid_row")
    private Integer playerGridRow = 4;

    @Column(name = "player_grid_col")
    private Integer playerGridCol = 4;

    @Column(name = "room_history", columnDefinition = "JSON")
    private String roomHistory;  // JSON序列化

    @Column(name = "room_items", columnDefinition = "JSON")
    private String roomItems;  // 房间物品状态 JSON

    @Column(name = "room_item_positions", columnDefinition = "JSON")
    private String roomItemPositions;  // 房间物品坐标 JSON

    @Column(name = "saved_at")
    private LocalDateTime savedAt;

    public GameSave() {
        this.savedAt = LocalDateTime.now();
    }

    /**
     * 构造函数.
     *
     * @param userId         用户ID
     * @param currentRoomId 当前房间ID
     */
    public GameSave(Long userId, String currentRoomId) {
        this.userId = userId;
        this.currentRoomId = currentRoomId;
        this.playerWeight = 0;
        this.playerMaxWeight = 20;
        this.savedAt = LocalDateTime.now();
    }

    /** 获取存档ID. */
    public Long getId() { return id; }

    /** 设置存档ID. */
    public void setId(Long id) { this.id = id; }

    /** 获取用户ID. */
    public Long getUserId() { return userId; }

    /** 设置用户ID. */
    public void setUserId(Long userId) { this.userId = userId; }

    /** 获取当前房间ID. */
    public String getCurrentRoomId() { return currentRoomId; }

    /** 设置当前房间ID. */
    public void setCurrentRoomId(String currentRoomId) { this.currentRoomId = currentRoomId; }

    /** 获取玩家背包（JSON格式）. */
    public String getPlayerInventory() { return playerInventory; }

    /** 设置玩家背包（JSON格式）. */
    public void setPlayerInventory(String playerInventory) { this.playerInventory = playerInventory; }

    /** 获取玩家当前重量. */
    public Integer getPlayerWeight() { return playerWeight; }

    /** 设置玩家当前重量. */
    public void setPlayerWeight(Integer playerWeight) { this.playerWeight = playerWeight; }

    /** 获取玩家最大负重. */
    public Integer getPlayerMaxWeight() { return playerMaxWeight; }

    /** 设置玩家最大负重. */
    public void setPlayerMaxWeight(Integer playerMaxWeight) { this.playerMaxWeight = playerMaxWeight; }

    /** 获取玩家网格行. */
    public Integer getPlayerGridRow() { return playerGridRow; }

    /** 设置玩家网格行. */
    public void setPlayerGridRow(Integer playerGridRow) { this.playerGridRow = playerGridRow; }

    /** 获取玩家网格列. */
    public Integer getPlayerGridCol() { return playerGridCol; }

    /** 设置玩家网格列. */
    public void setPlayerGridCol(Integer playerGridCol) { this.playerGridCol = playerGridCol; }

    /** 获取房间历史（JSON格式）. */
    public String getRoomHistory() { return roomHistory; }

    /** 设置房间历史（JSON格式）. */
    public void setRoomHistory(String roomHistory) { this.roomHistory = roomHistory; }

    /** 获取房间物品状态（JSON格式）. */
    public String getRoomItems() { return roomItems; }

    /** 设置房间物品状态（JSON格式）. */
    public void setRoomItems(String roomItems) { this.roomItems = roomItems; }

    /** 获取房间物品坐标（JSON格式）. */
    public String getRoomItemPositions() { return roomItemPositions; }

    /** 设置房间物品坐标（JSON格式）. */
    public void setRoomItemPositions(String roomItemPositions) { this.roomItemPositions = roomItemPositions; }

    /** 获取存档时间. */
    public LocalDateTime getSavedAt() { return savedAt; }

    /** 设置存档时间. */
    public void setSavedAt(LocalDateTime savedAt) { this.savedAt = savedAt; }
}
