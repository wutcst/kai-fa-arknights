package cn.edu.whut.sept.zuul.service;

import cn.edu.whut.sept.zuul.model.GameSave;
import cn.edu.whut.sept.zuul.model.Item;
import cn.edu.whut.sept.zuul.model.Player;
import cn.edu.whut.sept.zuul.model.Room;
import cn.edu.whut.sept.zuul.repository.GameSaveRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 存档服务层.
 * 处理游戏存档的保存和加载.
 */
@Service
@Transactional
public class SaveService {
    private static final int GRID_POSITION_SCALE = 1000;

    private final GameSaveRepository gameSaveRepository;
    private final ObjectMapper objectMapper;

    public SaveService(GameSaveRepository gameSaveRepository) {
        this.gameSaveRepository = gameSaveRepository;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 检查用户是否有存档.
     *
     * @param userId 用户ID
     * @return 是否有存档
     */
    public boolean hasSave(Long userId) {
        return gameSaveRepository.existsByUserId(userId);
    }

    /**
     * 获取用户的存档信息.
     *
     * @param userId 用户ID
     * @return 存档信息（如果存在）
     */
    public Optional<GameSave> getSave(Long userId) {
        return gameSaveRepository.findByUserId(userId);
    }

    /**
     * 保存游戏进度.
     *
     * @param userId      用户ID
     * @param currentRoom 当前房间
     * @param player      玩家对象
     * @param roomHistory 房间移动历史
     * @param roomItems   各房间物品状态
     * @return 保存的存档对象
     */
    public GameSave saveGame(Long userId, Room currentRoom, Player player, List<Room> roomHistory,
                             Map<String, List<Item>> roomItems, double playerGridRow, double playerGridCol) {
        GameSave save = gameSaveRepository.findByUserId(userId)
                .orElse(new GameSave());

        save.setUserId(userId);
        save.setCurrentRoomId(currentRoom.getId());
        save.setPlayerWeight(player.getTotalWeight());
        save.setPlayerMaxWeight(player.getMaxWeight());
        save.setPlayerGridRow(encodeGridCoordinate(playerGridRow));
        save.setPlayerGridCol(encodeGridCoordinate(playerGridCol));
        save.setSavedAt(LocalDateTime.now());

        // 序列化背包物品
        try {
            String inventoryJson = objectMapper.writeValueAsString(player.getInventory());
            save.setPlayerInventory(inventoryJson);
        } catch (JsonProcessingException e) {
            save.setPlayerInventory("[]");
        }

        // 序列化房间历史
        try {
            List<String> historyIds = new ArrayList<>();
            for (Room room : roomHistory) {
                historyIds.add(room.getId());
            }
            String historyJson = objectMapper.writeValueAsString(historyIds);
            save.setRoomHistory(historyJson);
        } catch (JsonProcessingException e) {
            save.setRoomHistory("[]");
        }

        // 序列化房间物品状态
        try {
            String roomItemsJson = objectMapper.writeValueAsString(roomItems);
            save.setRoomItems(roomItemsJson);
        } catch (JsonProcessingException e) {
            save.setRoomItems("{}");
        }

        return gameSaveRepository.save(save);
    }

    /**
     * 删除存档.
     *
     * @param userId 用户ID
     */
    public void deleteSave(Long userId) {
        gameSaveRepository.deleteByUserId(userId);
    }

    /**
     * 从存档加载游戏状态.
     *
     * @param userId   用户ID
     * @param allRooms 所有房间的映射
     * @return 包含加载状态的Map
     */
    public Map<String, Object> loadGame(Long userId, Map<String, Room> allRooms) {
        Optional<GameSave> saveOpt = gameSaveRepository.findByUserId(userId);

        if (saveOpt.isEmpty()) {
            return Map.of("success", false, "message", "没有找到存档");
        }

        GameSave save = saveOpt.get();

        // 获取存档的房间
        Room savedRoom = allRooms.get(save.getCurrentRoomId());
        if (savedRoom == null) {
            return Map.of("success", false, "message", "存档数据损坏，无法加载");
        }

        // 反序列化背包物品
        List<Item> inventory = new ArrayList<>();
        try {
            if (save.getPlayerInventory() != null && !save.getPlayerInventory().isEmpty()) {
                inventory = objectMapper.readValue(save.getPlayerInventory(), new TypeReference<List<Item>>() {});
            }
        } catch (JsonProcessingException e) {
            inventory = new ArrayList<>();
        }

        // 反序列化房间历史
        List<Room> history = new ArrayList<>();
        try {
            if (save.getRoomHistory() != null && !save.getRoomHistory().isEmpty()) {
                List<String> historyIds = objectMapper.readValue(save.getRoomHistory(), new TypeReference<List<String>>() {});
                for (String roomId : historyIds) {
                    Room room = allRooms.get(roomId);
                    if (room != null) {
                        history.add(room);
                    }
                }
            }
        } catch (JsonProcessingException e) {
            history = new ArrayList<>();
        }

        // 反序列化房间物品状态
        Map<String, List<Item>> roomItems = new HashMap<>();
        try {
            if (save.getRoomItems() != null && !save.getRoomItems().isEmpty()) {
                roomItems = objectMapper.readValue(save.getRoomItems(), new TypeReference<Map<String, List<Item>>>() {});
            }
        } catch (JsonProcessingException e) {
            roomItems = new HashMap<>();
        }

        return Map.of(
            "success", true,
            "currentRoom", savedRoom,
            "inventory", inventory,
            "playerWeight", save.getPlayerWeight(),
            "playerMaxWeight", save.getPlayerMaxWeight(),
            "playerGridRow", decodeGridCoordinate(save.getPlayerGridRow()),
            "playerGridCol", decodeGridCoordinate(save.getPlayerGridCol()),
            "roomHistory", history,
            "roomItems", roomItems,
            "savedAt", save.getSavedAt()
        );
    }

    private int encodeGridCoordinate(double coordinate) {
        return (int) Math.round(coordinate * GRID_POSITION_SCALE);
    }

    private double decodeGridCoordinate(Integer storedCoordinate) {
        if (storedCoordinate == null) {
            return 4.0;
        }
        // 旧存档保存的是 0..8 整数格子；新存档保存的是放大后的连续坐标。
        if (storedCoordinate >= 0 && storedCoordinate <= 8) {
            return storedCoordinate.doubleValue();
        }
        return storedCoordinate.doubleValue() / GRID_POSITION_SCALE;
    }
}
