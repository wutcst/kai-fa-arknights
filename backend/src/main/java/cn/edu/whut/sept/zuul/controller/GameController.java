package cn.edu.whut.sept.zuul.controller;

import cn.edu.whut.sept.zuul.model.Room;
import cn.edu.whut.sept.zuul.model.Item;
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
    private Room currentRoom;

    public GameController() {
        game = new Game();
        currentRoom = game.getCurrentRoom();
    }

    /**
     * 获取当前游戏状态.
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
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
     */
    @PostMapping("/move")
    public Map<String, Object> move(@RequestBody Map<String, String> request) {
        String direction = request.get("direction");
        Map<String, Object> result = new HashMap<>();

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
            currentRoom = nextRoom;
            result.put("success", true);
            result.put("message", "你走向了" + getZhDirection(direction));
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
     * 查看当前房间信息（look命令）.
     */
    @GetMapping("/look")
    public Map<String, Object> look() {
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
     * 获取地图数据（所有房间及连接关系）.
     */
    @GetMapping("/map")
    public Map<String, Object> getMap() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> rooms = new ArrayList<>();

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

    private String getZhLongDescription(Room room) {
        return "你在 " + room.getZhName() + "。";
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
}
