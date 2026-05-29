package cn.edu.whut.sept.zuul.controller;

import cn.edu.whut.sept.zuul.model.Room;
import cn.edu.whut.sept.zuul.model.Item;
import cn.edu.whut.sept.zuul.model.Player;
import cn.edu.whut.sept.zuul.service.AbilityService;
import cn.edu.whut.sept.zuul.service.Game;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * 游戏 REST API 控制器.
 * 提供游戏状态、地图、移动等接口.
 */
@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameController {

    private final Game game;
    private final AbilityService abilityService;

    public GameController(Game game, AbilityService abilityService) {
        this.game = game;
        this.abilityService = abilityService;
    }

    private int getPlayerMaxWeight() {
        Long userId = game.getCurrentUserId();
        if (userId != null) {
            return abilityService.getMaxWeight(userId);
        }
        return game.getPlayer().getMaxWeight();
    }

    private Room getCurrentRoom() {
        return game.getCurrentRoom();
    }

    /**
     * 获取当前游戏状态.
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Room currentRoom = getCurrentRoom();
        Map<String, Object> result = new HashMap<>();
        result.put("description", currentRoom.getZhName());
        result.put("descriptionEn", currentRoom.getShortDescription());
        result.put("longDescription", getZhLongDescription(currentRoom));
        result.put("exits", currentRoom.getExits());
        result.put("roomId", currentRoom.getId());
        result.put("items", getRoomItems(currentRoom));
        return result;
    }

    /**
     * 移动角色到指定方向.
     *
     * @param request 包含移动方向的请求
     * @return 移动结果及当前房间状态
     */
    @PostMapping("/move")
    public Map<String, Object> move(@RequestBody Map<String, String> request) {
        String direction = request.get("direction");
        Map<String, Object> result = new HashMap<>();

        Room currentRoom = getCurrentRoom();
        Room nextRoom = currentRoom.getExit(direction);

        if (nextRoom == null) {
            result.put("success", false);
            result.put("message", "你不能往这个方向走！");
            result.put("description", currentRoom.getZhName());
            result.put("descriptionEn", currentRoom.getShortDescription());
            result.put("longDescription", getZhLongDescription(currentRoom));
            result.put("exits", currentRoom.getExits());
            result.put("roomId", currentRoom.getId());
            result.put("items", getRoomItems(currentRoom));
        } else {
            game.setCurrentRoom(nextRoom);
            currentRoom = game.getCurrentRoom();
            result.put("success", true);
            result.put("message", "你走向了" + getZhDirection(direction));
            result.put("description", currentRoom.getZhName());
            result.put("descriptionEn", currentRoom.getShortDescription());
            result.put("longDescription", getZhLongDescription(currentRoom));
            result.put("exits", currentRoom.getExits());
            result.put("roomId", currentRoom.getId());
            result.put("items", getRoomItems(currentRoom));
            // 检查是否发生了传送
            if (game.isJustTeleported()) {
                result.put("teleported", true);
                result.put("teleportedFrom", game.getTeleportedFrom());
                game.resetTeleported();  // 重置传送状态
            }
        }

        return result;
    }

    /**
     * 查看当前房间信息（look命令）.
     */
    @GetMapping("/look")
    public Map<String, Object> look() {
        Map<String, Object> result = new HashMap<>();
        Room currentRoom = getCurrentRoom();
        result.put("description", currentRoom.getZhName());
        result.put("descriptionEn", currentRoom.getShortDescription());
        result.put("longDescription", getZhLongDescription(currentRoom));
        result.put("exits", currentRoom.getExits());
        result.put("roomId", currentRoom.getId());
        result.put("items", getRoomItems(currentRoom));
        return result;
    }

    /**
     * 返回上一个房间（back命令）.
     */
    @PostMapping("/back")
    public Map<String, Object> back() {
        Map<String, Object> result = new HashMap<>();
        Room currentRoom = getCurrentRoom();

        Room backRoom = game.getBackRoom();

        if (backRoom == null) {
            result.put("success", false);
            result.put("message", "你无法再回退了，已经在起始点！");
            result.put("description", currentRoom.getZhName());
            result.put("descriptionEn", currentRoom.getShortDescription());
            result.put("longDescription", getZhLongDescription(currentRoom));
            result.put("exits", currentRoom.getExits());
            result.put("roomId", currentRoom.getId());
            result.put("items", getRoomItems(currentRoom));
        } else {
            currentRoom = backRoom;
            result.put("success", true);
            result.put("message", "你回到了上一个房间");
            result.put("description", currentRoom.getZhName());
            result.put("descriptionEn", currentRoom.getShortDescription());
            result.put("longDescription", getZhLongDescription(currentRoom));
            result.put("exits", currentRoom.getExits());
            result.put("roomId", currentRoom.getId());
            result.put("items", getRoomItems(currentRoom));
        }

        return result;
    }

    /**
     * 获取地图数据（所有房间及连接关系）.
     */
    @GetMapping("/map")
    public Map<String, Object> getMap() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> rooms = new ArrayList<>();

        Room currentRoom = getCurrentRoom();
        Map<String, Room> allRooms = game.getRooms();
        for (Map.Entry<String, Room> entry : allRooms.entrySet()) {
            Room room = entry.getValue();
            Map<String, Object> roomInfo = new HashMap<>();
            roomInfo.put("id", room.getId());
            roomInfo.put("name", room.getZhName());
            roomInfo.put("description", room.getShortDescription());
            roomInfo.put("exits", room.getExits());
            // 添加直接连通的其他房间ID
            List<String> connectedRoomIds = new ArrayList<>();
            for (String exit : room.getExits()) {
                Room neighbor = room.getExit(exit);
                if (neighbor != null) {
                    connectedRoomIds.add(neighbor.getId());
                }
            }
            roomInfo.put("connectedRooms", connectedRoomIds);
            rooms.add(roomInfo);
        }

        result.put("rooms", rooms);
        result.put("currentRoomId", currentRoom.getId());
        return result;
    }

    /**
     * 获取帮助信息.
     */
    @GetMapping("/help")
    public Map<String, Object> getHelp() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "可用的命令：点击方向按钮移动，点击帮助按钮查看");
        result.put("directions", new String[]{"north", "south", "east", "west"});
        return result;
    }

    /**
     * 拾取物品（take命令）.
     *
     * @param request 包含物品ID的请求
     * @return 拾取结果及当前状态
     */
    @PostMapping("/take")
    public Map<String, Object> take(@RequestBody Map<String, String> request) {
        String itemId = request.get("itemId");
        Map<String, Object> result = new HashMap<>();
        Room currentRoom = getCurrentRoom();

        String message = game.takeItem(itemId);
        result.put("message", message);
        result.put("description", currentRoom.getZhName());
        result.put("descriptionEn", currentRoom.getShortDescription());
        result.put("longDescription", getZhLongDescription(currentRoom));
        result.put("exits", currentRoom.getExits());
        result.put("roomId", currentRoom.getId());
        result.put("items", getRoomItems(currentRoom));
        result.put("inventory", getPlayerInventory());
        result.put("playerWeight", game.getPlayer().getTotalWeight());
        result.put("playerMaxWeight", getPlayerMaxWeight());

        // 检查是否拾取成功
        result.put("success", message.contains("拾取了"));
        return result;
    }

    /**
     * 丢弃物品（drop命令）.
     *
     * @param request 包含物品ID的请求
     * @return 丢弃结果及当前状态
     */
    @PostMapping("/drop")
    public Map<String, Object> drop(@RequestBody Map<String, String> request) {
        String itemId = request.get("itemId");
        Map<String, Object> result = new HashMap<>();
        Room currentRoom = getCurrentRoom();

        String message = game.dropItem(itemId);
        result.put("message", message);
        result.put("description", currentRoom.getZhName());
        result.put("descriptionEn", currentRoom.getShortDescription());
        result.put("longDescription", getZhLongDescription(currentRoom));
        result.put("exits", currentRoom.getExits());
        result.put("roomId", currentRoom.getId());
        result.put("items", getRoomItems(currentRoom));
        result.put("inventory", getPlayerInventory());
        result.put("playerWeight", game.getPlayer().getTotalWeight());
        result.put("playerMaxWeight", getPlayerMaxWeight());

        result.put("success", !message.contains("没有"));
        return result;
    }

    /**
     * 查看所有物品（items命令）.
     */
    @GetMapping("/items")
    public Map<String, Object> items() {
        Map<String, Object> result = new HashMap<>();
        Room currentRoom = getCurrentRoom();

        result.put("message", game.getItemsInfo());
        result.put("description", currentRoom.getZhName());
        result.put("exits", currentRoom.getExits());
        result.put("roomId", currentRoom.getId());
        result.put("items", getRoomItems(currentRoom));
        result.put("inventory", getPlayerInventory());
        result.put("playerWeight", game.getPlayer().getTotalWeight());
        result.put("playerMaxWeight", getPlayerMaxWeight());

        return result;
    }

    /**
     * 吃魔法饼干（eat cookie命令）.
     */
    @PostMapping("/eatcookie")
    public Map<String, Object> eatCookie() {
        Map<String, Object> result = new HashMap<>();
        Room currentRoom = getCurrentRoom();

        String message = game.eatCookie();
        result.put("message", message);
        result.put("description", currentRoom.getZhName());
        result.put("exits", currentRoom.getExits());
        result.put("roomId", currentRoom.getId());
        result.put("items", getRoomItems(currentRoom));
        result.put("inventory", getPlayerInventory());
        result.put("playerWeight", game.getPlayer().getTotalWeight());
        result.put("playerMaxWeight", getPlayerMaxWeight());

        result.put("success", message.contains("吃了"));
        return result;
    }

    private String getZhLongDescription(Room room) {
        return "";
    }

    private String getZhDirection(String dir) {
        switch(dir) {
            case "north": return "北方";
            case "south": return "南方";
            case "east": return "东方";
            case "west": return "西方";
            default: return dir;
        }
    }

    /**
     * 获取房间内的物品列表.
     */
    private List<Map<String, Object>> getRoomItems(Room room) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (Item item : room.getItems()) {
            Map<String, Object> itemInfo = new HashMap<>();
            itemInfo.put("id", item.getId());
            itemInfo.put("name", item.getName());
            itemInfo.put("description", item.getDescription());
            itemInfo.put("weight", item.getWeight());
            itemInfo.put("value", item.getValue());
            items.add(itemInfo);
        }
        return items;
    }

    /**
     * 获取玩家背包物品列表.
     */
    private List<Map<String, Object>> getPlayerInventory() {
        List<Map<String, Object>> items = new ArrayList<>();
        for (Item item : game.getPlayer().getInventory()) {
            Map<String, Object> itemInfo = new HashMap<>();
            itemInfo.put("id", item.getId());
            itemInfo.put("name", item.getName());
            itemInfo.put("description", item.getDescription());
            itemInfo.put("weight", item.getWeight());
            itemInfo.put("value", item.getValue());
            items.add(itemInfo);
        }
        return items;
    }
}
