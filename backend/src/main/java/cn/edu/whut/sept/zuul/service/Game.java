/**
 * 游戏服务类.
 * 负责管理游戏房间、玩家位置及房间间连接.
 *
 * @author  Michael Kölling and David J. Barnes
 * @version 1.0
 */
package cn.edu.whut.sept.zuul.service;

import cn.edu.whut.sept.zuul.model.Room;
import cn.edu.whut.sept.zuul.model.GridPosition;
import cn.edu.whut.sept.zuul.model.Item;
import cn.edu.whut.sept.zuul.model.Player;
import cn.edu.whut.sept.zuul.service.world.LoadedWorld;
import cn.edu.whut.sept.zuul.service.world.WorldDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 游戏核心服务.
 */
@Service
@DependsOnDatabaseInitialization
public class Game {
    private Room currentRoom;
    private Map<String, Room> rooms;
    private Map<String, List<Item>> initialRoomItems;  // 初始房间物品快照
    private Map<String, Map<String, GridPosition>> initialRoomItemPositions;
    private List<Room> roomHistory;  // 房间移动历史
    private boolean justTeleported;   // 是否刚触发传送
    private String teleportedFrom;    // 从哪个房间传送走的
    private Player player;            // 玩家对象
    private Long currentUserId;       // 当前用户ID
    private LoadedWorld loadedWorld;
    private Random portalRandom;

    @Autowired
    public Game(WorldDataService worldDataService)
    {
        initializeWorld(worldDataService.loadWorld());
    }

    Game(LoadedWorld loadedWorld) {
        initializeWorld(loadedWorld);
    }

    private void initializeWorld(LoadedWorld loadedWorld)
    {
        this.loadedWorld = loadedWorld;
        rooms = new HashMap<>();
        rooms.putAll(loadedWorld.getRooms());
        initialRoomItems = new HashMap<>();
        initialRoomItemPositions = new HashMap<>();
        roomHistory = new ArrayList<>();
        justTeleported = false;
        player = new Player("冒险者");
        player.setBaseMaxWeight(loadedWorld.getDefaultMaxWeight());
        player.resetMaxWeightToBase();
        currentRoom = rooms.get(loadedWorld.getStartRoomId());
        player.setCurrentRoom(currentRoom);
        portalRandom = new Random(loadedWorld.getPortalRandomSeed());
        saveInitialRoomItems();
    }

    /**
     * 获取所有房间映射.
     *
     * @return 房间ID到房间对象的映射
     */
    public Map<String, Room> getRooms() {
        return rooms;
    }

    /**
     * 获取玩家对象.
     *
     * @return 玩家对象
     */
    public Player getPlayer() {
        return player;
    }

    public Long getCurrentUserId() {
        return currentUserId;
    }

    public int getDefaultPlayerGridRow() {
        return loadedWorld.getDefaultPlayerGridRow();
    }

    public int getDefaultPlayerGridCol() {
        return loadedWorld.getDefaultPlayerGridCol();
    }

    public void setCurrentUserId(Long currentUserId) {
        this.currentUserId = currentUserId;
    }

    /**
     * 获取所有房间的当前物品状态.
     * 返回 Map：roomId -> items list
     */
    public Map<String, List<Item>> getAllRoomItems() {
        Map<String, List<Item>> roomItems = new HashMap<>();
        for (Map.Entry<String, Room> entry : rooms.entrySet()) {
            roomItems.put(entry.getKey(), new ArrayList<>(entry.getValue().getItems()));
        }
        return roomItems;
    }

    public Map<String, Map<String, GridPosition>> getAllRoomItemPositions() {
        Map<String, Map<String, GridPosition>> positions = new HashMap<>();
        for (Map.Entry<String, Room> entry : rooms.entrySet()) {
            positions.put(entry.getKey(), entry.getValue().getItemPositions());
        }
        return positions;
    }

    /**
     * 设置所有房间的物品状态（从存档恢复）.
     */
    public void setAllRoomItems(Map<String, List<Item>> roomItems) {
        for (Map.Entry<String, List<Item>> entry : roomItems.entrySet()) {
            Room room = rooms.get(entry.getKey());
            if (room != null) {
                room.setItems(new ArrayList<>(entry.getValue()));
            }
        }
        ensureRoomItemPositions();
    }

    public void setAllRoomItemPositions(Map<String, Map<String, GridPosition>> positions) {
        for (Map.Entry<String, Room> entry : rooms.entrySet()) {
            Map<String, GridPosition> roomPositions = positions != null ? positions.get(entry.getKey()) : null;
            entry.getValue().setItemPositions(roomPositions);
        }
        ensureRoomItemPositions();
    }

    public GridPosition getItemPosition(Room room, String itemId) {
        return room.getItemPosition(itemId);
    }

    public boolean isCellOccupied(Room room, int row, int col) {
        return room.hasItemAt(row, col);
    }

    /**
     * 获取当前房间.
     *
     * @return 当前房间对象
     */
    public Room getCurrentRoom() {
        return currentRoom;
    }

    /**
     * 设置当前房间，处理历史记录和传送逻辑.
     *
     * @param room 要设置的房间
     */
    public void setCurrentRoom(Room room) {
        // 如果进入传送房间，触发随机传送
        if (loadedWorld.isPortalRoom(room.getId())) {
            teleportedFrom = currentRoom.getZhName();  // 记录传送前的位置
            justTeleported = true;
            List<String> targetRoomIds = loadedWorld.getPortalTargetRoomIds(room.getId());
            this.currentRoom = rooms.get(targetRoomIds.get(portalRandom.nextInt(targetRoomIds.size())));
            // 传送后清空历史记录，以新位置为起点
            roomHistory.clear();
            roomHistory.add(this.currentRoom);
            player.setCurrentRoom(this.currentRoom);
        } else {
            // 普通房间移动，添加到历史记录
            if (!justTeleported) {
                roomHistory.add(currentRoom);
            }
            justTeleported = false;
            teleportedFrom = null;
            this.currentRoom = room;
            player.setCurrentRoom(room);
        }
    }

    /**
     * 是否刚刚发生了传送.
     */
    public boolean isJustTeleported() {
        return justTeleported;
    }

    /**
     * 获取传送前的位置名称.
     */
    public String getTeleportedFrom() {
        return teleportedFrom;
    }

    /**
     * 重置传送状态.
     */
    public void resetTeleported() {
        justTeleported = false;
        teleportedFrom = null;
    }

    /**
     * 返回上一个房间（逐层回退）.
     * @return 上一个房间，如果没有历史记录则返回null
     */
    public Room getBackRoom() {
        if (roomHistory.isEmpty()) {
            return null;
        }
        // 回到上一个房间
        Room backRoom = roomHistory.remove(roomHistory.size() - 1);
        this.currentRoom = backRoom;
        player.setCurrentRoom(backRoom);
        return backRoom;
    }

    /**
     * 检查是否可以回退.
     *
     * @return 是否可以回退
     */
    public boolean canGoBack() {
        return !roomHistory.isEmpty();
    }

    /**
     * Legacy method for old text-command compatibility.
     * Do not use this method in REST interaction APIs because it does not validate player grid position.
     * Use takeItemAtCell(...) instead.
     */
    public String takeItem(String itemId) {
        Item item = currentRoom.getItem(itemId);
        if (item == null) {
            return "房间里没有这个物品！";
        }

        if (!player.canCarry(item)) {
            return "物品太重了！你无法携带更多物品（当前负重：" +
                   player.getTotalWeight() + "/" + player.getMaxWeight() + "）";
        }

        currentRoom.removeItem(itemId);
        player.addItem(item);
        return "你拾取了 " + item.getName() + "（重量：" + item.getWeight() + "）";
    }

    public String takeItemAtCell(String itemId, double playerGridRow, double playerGridCol) {
        if (itemId == null || itemId.trim().isEmpty()) {
            return "物品不能为空！";
        }

        Item item = currentRoom.getItem(itemId);
        if (item == null) {
            return "当前房间没有这个物品！";
        }

        GridPosition itemPosition = currentRoom.getItemPosition(itemId);
        if (itemPosition == null) {
            return "物品位置异常，无法拾取！";
        }

        int row = normalizeGridCoordinate(playerGridRow);
        int col = normalizeGridCoordinate(playerGridCol);
        if (itemPosition.getRow() != row || itemPosition.getCol() != col) {
            return "必须站在物品所在格才能拾取！";
        }

        if (!player.canCarry(item)) {
            return "物品太重了！你无法携带更多物品（当前负重：" +
                   player.getTotalWeight() + "/" + player.getMaxWeight() + "）";
        }

        currentRoom.removeItem(itemId);
        player.addItem(item);
        return "你拾取了 " + item.getName() + "（重量：" + item.getWeight() + "）";
    }

    /**
     * Legacy method for old text-command compatibility.
     * Do not use this method in REST interaction APIs because dropped items need grid positions.
     * Use dropItemAtCell(...) instead.
     */
    public String dropItem(String itemId) {
        if (itemId.equals("all")) {
            if (player.getInventory().isEmpty()) {
                return "你身上没有任何物品！";
            }
            if (countFreeItemPositions(currentRoom) < player.getInventory().size()) {
                return "当前房间没有可放置的位置！";
            }
            int count = 0;
            for (Item item : new ArrayList<>(player.getInventory())) {
                GridPosition position = findFreeItemPosition(currentRoom);
                currentRoom.addItem(item);
                currentRoom.setItemPosition(item.getId(), position);
                count++;
            }
            player.getInventory().clear();
            return "你丢弃了所有物品（" + count + "件）";
        }

        if (!player.hasItem(itemId)) {
            return "你身上没有这个物品！";
        }
        GridPosition position = findFreeItemPosition(currentRoom);
        if (position == null) {
            return "当前房间没有可放置的位置！";
        }

        Item item = player.removeItem(itemId);
        currentRoom.addItem(item);
        currentRoom.setItemPosition(item.getId(), position);
        return "你丢弃了 " + item.getName();
    }

    public String dropItemAtCell(String itemId, double playerGridRow, double playerGridCol) {
        if (itemId == null || itemId.trim().isEmpty()) {
            return "物品不能为空！";
        }
        if ("all".equals(itemId)) {
            return "当前模式不支持一次丢弃全部物品，请逐个丢弃。";
        }

        int row = normalizeGridCoordinate(playerGridRow);
        int col = normalizeGridCoordinate(playerGridCol);
        if (!player.hasItem(itemId)) {
            return "你身上没有这个物品！";
        }

        if (currentRoom.hasItemAt(row, col)) {
            return "当前格已有物品，不能丢弃！";
        }

        Item item = player.removeItem(itemId);
        currentRoom.addItem(item);
        currentRoom.setItemPosition(item.getId(), new GridPosition(row, col));
        return "你丢弃了 " + item.getName();
    }

    /**
     * 吃理智增强剂.
     * @return 结果信息
     */
    public String eatCookie() {
        Item cookie = player.removeItem("magic_cookie");
        if (cookie == null) {
            return "你身上没有理智增强剂！";
        }
        int bonus = loadedWorld.getItemEffectValue("magic_cookie", WorldDataService.EFFECT_MAX_WEIGHT_BONUS);
        player.increaseMaxWeight(bonus);
        return "你吃了理智增强剂！负重上限增加了" + bonus + "点（当前负重上限：" +
               player.getMaxWeight() + "）";
    }

    /**
     * 获取物品信息（房间和背包）.
     * @return 物品信息字符串
     */
    public String getItemsInfo() {
        StringBuilder sb = new StringBuilder();

        // 房间物品
        List<Item> roomItems = currentRoom.getItems();
        int roomWeight = currentRoom.getTotalWeight();
        int roomValue = currentRoom.getTotalValue();

        sb.append("【房间物品】" + currentRoom.getZhName() + "\n");
        if (roomItems.isEmpty()) {
            sb.append("  没有物品\n");
        } else {
            for (Item item : roomItems) {
                sb.append("  - " + item.getName() + ": " + item.getDescription() +
                         "（重量:" + item.getWeight() + " 价值:" + item.getValue() + "）\n");
            }
        }
        sb.append("  总重量: " + roomWeight + " | 总价值: " + roomValue + "\n\n");

        // 背包物品
        List<Item> inventory = player.getInventory();
        int invWeight = player.getTotalWeight();
        int invValue = player.getTotalValue();

        sb.append("【随身物品】" + player.getName() + "\n");
        sb.append("  负重: " + invWeight + "/" + player.getMaxWeight() + "\n");
        if (inventory.isEmpty()) {
            sb.append("  没有物品\n");
        } else {
            for (Item item : inventory) {
                sb.append("  - " + item.getName() + ": " + item.getDescription() +
                         "（重量:" + item.getWeight() + " 价值:" + item.getValue() + "）\n");
            }
        }
        sb.append("  总重量: " + invWeight + " | 总价值: " + invValue);

        return sb.toString();
    }

    /**
     * 获取房间移动历史.
     *
     * @return 房间历史列表的副本
     */
    public List<Room> getRoomHistory() {
        return new ArrayList<>(roomHistory);
    }

    /**
     * 设置房间移动历史.
     *
     * @param history 房间历史列表
     */
    public void setRoomHistory(List<Room> history) {
        this.roomHistory.clear();
        this.roomHistory.addAll(history);
    }

    /**
     * 设置玩家背包物品.
     *
     * @param items 物品列表
     */
    public void setPlayerInventory(List<Item> items) {
        this.player.getInventory().clear();
        this.player.getInventory().addAll(items);
    }

    /**
     * 设置玩家最大负重.
     *
     * @param weight 最大负重值
     */
    public void setMaxWeight(int weight) {
        this.player.increaseMaxWeight(weight - this.player.getMaxWeight());
    }

    /**
     * 保存初始房间物品快照.
     * 用于游戏重置时恢复房间物品.
     */
    private void saveInitialRoomItems() {
        for (Map.Entry<String, Room> entry : rooms.entrySet()) {
            List<Item> itemsCopy = new ArrayList<>();
            for (Item item : entry.getValue().getItems()) {
                itemsCopy.add(new Item(item.getId(), item.getName(), item.getDescription(),
                                      item.getWeight(), item.getValue()));
            }
            initialRoomItems.put(entry.getKey(), itemsCopy);
            initialRoomItemPositions.put(entry.getKey(), entry.getValue().getItemPositions());
        }
    }

    private void initializeRoomItemPositions() {
        for (Room room : rooms.values()) {
            assignMissingItemPositions(room);
        }
    }

    public void ensureRoomItemPositions() {
        for (Room room : rooms.values()) {
            assignMissingItemPositions(room);
        }
    }

    private void assignMissingItemPositions(Room room) {
        int[][] candidates = {
            {2, 2}, {2, 6}, {6, 2}, {6, 6}, {5, 3}, {3, 5}
        };
        Map<String, GridPosition> positions = room.getItemPositions();
        List<String> used = new ArrayList<>();
        for (GridPosition position : positions.values()) {
            used.add(position.getRow() + "-" + position.getCol());
        }

        int candidateIndex = 0;
        for (Item item : room.getItems()) {
            if (room.getItemPosition(item.getId()) != null) {
                continue;
            }
            while (candidateIndex < candidates.length) {
                int row = candidates[candidateIndex][0];
                int col = candidates[candidateIndex][1];
                candidateIndex++;
                String key = row + "-" + col;
                if (!used.contains(key)) {
                    room.setItemPosition(item.getId(), new GridPosition(row, col));
                    used.add(key);
                    break;
                }
            }
        }
    }

    private int countFreeItemPositions(Room room) {
        int count = 0;
        int[][] candidates = getItemPositionCandidates();
        for (int[] candidate : candidates) {
            if (!room.hasItemAt(candidate[0], candidate[1])) {
                count++;
            }
        }
        return count;
    }

    private GridPosition findFreeItemPosition(Room room) {
        int[][] candidates = getItemPositionCandidates();
        for (int[] candidate : candidates) {
            int row = candidate[0];
            int col = candidate[1];
            if (!room.hasItemAt(row, col)) {
                return new GridPosition(row, col);
            }
        }
        return null;
    }

    private int[][] getItemPositionCandidates() {
        return new int[][] {
            {2, 2}, {2, 6}, {6, 2}, {6, 6}, {5, 3}, {3, 5}
        };
    }

    private int normalizeGridCoordinate(double coordinate) {
        return Math.max(0, Math.min(8, (int) Math.round(coordinate)));
    }

    /**
     * 重置游戏到初始状态.
     */
    public void resetToStart() {
        this.currentRoom = rooms.get(loadedWorld.getStartRoomId());
        this.player.setCurrentRoom(this.currentRoom);
        this.roomHistory.clear();
        this.player.getInventory().clear();
        this.player.setBaseMaxWeight(loadedWorld.getDefaultMaxWeight());
        this.player.resetMaxWeightToBase();
        this.justTeleported = false;
        this.teleportedFrom = null;
        this.portalRandom = new Random(loadedWorld.getPortalRandomSeed());
        // 恢复房间物品到初始状态
        restoreRoomItems();
    }

    /**
     * 恢复房间物品到初始状态.
     */
    private void restoreRoomItems() {
        for (Map.Entry<String, List<Item>> entry : initialRoomItems.entrySet()) {
            Room room = rooms.get(entry.getKey());
            if (room != null) {
                room.getItems().clear();
                for (Item item : entry.getValue()) {
                    room.addItem(new Item(item.getId(), item.getName(), item.getDescription(),
                                         item.getWeight(), item.getValue()));
                }
                room.setItemPositions(initialRoomItemPositions.get(entry.getKey()));
            }
        }
        ensureRoomItemPositions();
    }

    /**
     * 获取所有房间的Map.
     */
    public Map<String, Room> getAllRooms() {
        return this.rooms;
    }
}
