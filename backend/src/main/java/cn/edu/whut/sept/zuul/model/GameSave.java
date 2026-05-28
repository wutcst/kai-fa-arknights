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

    @Column(name = "room_history", columnDefinition = "JSON")
    private String roomHistory;  // JSON序列化

    @Column(name = "room_items", columnDefinition = "JSON")
    private String roomItems;  // 房间物品状态 JSON

    @Column(name = "saved_at")
    private LocalDateTime savedAt;

    public GameSave() {
        this.savedAt = LocalDateTime.now();
    }

    public GameSave(Long userId, String currentRoomId) {
        this.userId = userId;
        this.currentRoomId = currentRoomId;
        this.playerWeight = 0;
        this.playerMaxWeight = 20;
        this.savedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCurrentRoomId() {
        return currentRoomId;
    }

    public void setCurrentRoomId(String currentRoomId) {
        this.currentRoomId = currentRoomId;
    }

    public String getPlayerInventory() {
        return playerInventory;
    }

    public void setPlayerInventory(String playerInventory) {
        this.playerInventory = playerInventory;
    }

    public Integer getPlayerWeight() {
        return playerWeight;
    }

    public void setPlayerWeight(Integer playerWeight) {
        this.playerWeight = playerWeight;
    }

    public Integer getPlayerMaxWeight() {
        return playerMaxWeight;
    }

    public void setPlayerMaxWeight(Integer playerMaxWeight) {
        this.playerMaxWeight = playerMaxWeight;
    }

    public String getRoomHistory() {
        return roomHistory;
    }

    public void setRoomHistory(String roomHistory) {
        this.roomHistory = roomHistory;
    }

    public String getRoomItems() {
        return roomItems;
    }

    public void setRoomItems(String roomItems) {
        this.roomItems = roomItems;
    }

    public LocalDateTime getSavedAt() {
        return savedAt;
    }

    public void setSavedAt(LocalDateTime savedAt) {
        this.savedAt = savedAt;
    }
}
