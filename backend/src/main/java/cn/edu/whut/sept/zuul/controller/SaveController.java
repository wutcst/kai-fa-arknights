package cn.edu.whut.sept.zuul.controller;

import cn.edu.whut.sept.zuul.model.Item;
import cn.edu.whut.sept.zuul.model.Room;
import cn.edu.whut.sept.zuul.model.User;
import cn.edu.whut.sept.zuul.repository.UserRepository;
import cn.edu.whut.sept.zuul.service.AbilityService;
import cn.edu.whut.sept.zuul.service.Game;
import cn.edu.whut.sept.zuul.service.SaveService;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 游戏存档 REST API 控制器.
 * 提供存档、读档等接口.
 */
@RestController
@RequestMapping("/api/save")
@CrossOrigin(origins = "*")
public class SaveController {

    private final Game game;
    private final SaveService saveService;
    private final UserRepository userRepository;
    private final AbilityService abilityService;

    public SaveController(Game game, SaveService saveService, UserRepository userRepository, AbilityService abilityService) {
        this.game = game;
        this.saveService = saveService;
        this.userRepository = userRepository;
        this.abilityService = abilityService;
    }

    /**
     * 检查用户是否有存档.
     */
    @GetMapping("/hasSave")
    public Map<String, Object> hasSave(@RequestParam String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return Map.of("hasSave", false, "message", "用户不存在");
        }
        boolean hasSave = saveService.hasSave(userOpt.get().getId());
        return Map.of("hasSave", hasSave);
    }

    /**
     * 保存游戏进度.
     */
    @PostMapping("/save")
    public Map<String, Object> saveGame(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        if (username == null || username.trim().isEmpty()) {
            return Map.of("success", false, "message", "用户名不能为空");
        }

        Optional<User> userOpt = userRepository.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            return Map.of("success", false, "message", "用户不存在");
        }

        Long userId = userOpt.get().getId();
        Room currentRoom = game.getCurrentRoom();
        List<Room> history = game.getRoomHistory();
        Map<String, List<Item>> roomItems = game.getAllRoomItems();

        saveService.saveGame(userId, currentRoom, game.getPlayer(), history, roomItems);

        return Map.of("success", true, "message", "游戏已保存");
    }

    /**
     * 加载游戏进度.
     */
    @PostMapping("/load")
    public Map<String, Object> loadGame(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        if (username == null || username.trim().isEmpty()) {
            return Map.of("success", false, "message", "用户名不能为空");
        }

        Optional<User> userOpt = userRepository.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            return Map.of("success", false, "message", "用户不存在");
        }

        Long userId = userOpt.get().getId();
        Map<String, Object> loadResult = saveService.loadGame(userId, game.getAllRooms());

        if (!(boolean) loadResult.get("success")) {
            return loadResult;
        }

        // 恢复游戏状态
        Room savedRoom = (Room) loadResult.get("currentRoom");
        @SuppressWarnings("unchecked")
        List<Item> inventory = (List<Item>) loadResult.get("inventory");
        int playerWeight = (int) loadResult.get("playerWeight");
        int playerMaxWeight = (int) loadResult.get("playerMaxWeight");
        @SuppressWarnings("unchecked")
        List<Room> history = (List<Room>) loadResult.get("roomHistory");
        @SuppressWarnings("unchecked")
        Map<String, List<Item>> roomItems = (Map<String, List<Item>>) loadResult.get("roomItems");

        // 设置游戏状态
        game.setCurrentRoom(savedRoom);
        game.setPlayerInventory(inventory);
        game.setMaxWeight(playerMaxWeight);
        game.setRoomHistory(history);
        game.setAllRoomItems(roomItems);  // 恢复房间物品状态

        return Map.of(
            "success", true,
            "message", "游戏已加载",
            "description", savedRoom.getZhName(),
            "longDescription", savedRoom.getShortDescription(),
            "exits", savedRoom.getExits(),
            "roomId", savedRoom.getId(),
            "items", getRoomItems(savedRoom),
            "inventory", getPlayerInventory(),
            "playerWeight", playerWeight,
            "playerMaxWeight", playerMaxWeight
        );
    }

    /**
     * 开始新游戏（删除存档并重置状态）.
     */
    @PostMapping("/newGame")
    public Map<String, Object> newGame(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        if (username == null || username.trim().isEmpty()) {
            return Map.of("success", false, "message", "用户名不能为空");
        }

        Optional<User> userOpt = userRepository.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            return Map.of("success", false, "message", "用户不存在");
        }

        Long userId = userOpt.get().getId();

        // 删除存档
        saveService.deleteSave(userId);

        // 重置游戏状态
        game.resetToStart();

        // 根据用户能力设置初始属性
        int maxWeight = abilityService.getMaxWeight(userId);
        game.setMaxWeight(maxWeight);

        Room currentRoom = game.getCurrentRoom();
        return Map.of(
            "success", true,
            "message", "新游戏已开始",
            "description", currentRoom.getZhName(),
            "longDescription", currentRoom.getShortDescription(),
            "exits", currentRoom.getExits(),
            "roomId", currentRoom.getId(),
            "items", getRoomItems(currentRoom),
            "playerMaxWeight", maxWeight
        );
    }

    /**
     * 探索结算 - 将背包物品转换为金币.
     */
    @PostMapping("/settle")
    public Map<String, Object> settleExploration(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        if (username == null || username.trim().isEmpty()) {
            return Map.of("success", false, "message", "用户名不能为空");
        }

        Optional<User> userOpt = userRepository.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            return Map.of("success", false, "message", "用户不存在");
        }

        Long userId = userOpt.get().getId();
        List<Item> inventory = new ArrayList<>(game.getPlayer().getInventory());

        Map<String, Object> settleResult = abilityService.settleExploration(userId, inventory);

        int goldEarned = (int) settleResult.get("goldEarned");
        int totalGold = (int) settleResult.get("totalGold");

        game.getPlayer().getInventory().clear();
        game.setMaxWeight(abilityService.getMaxWeight(userId));

        return Map.of(
            "success", true,
            "message", "结算成功！获得 " + goldEarned + " 金币",
            "goldEarned", goldEarned,
            "totalGold", totalGold,
            "inventory", new ArrayList<>(),
            "playerWeight", 0,
            "playerMaxWeight", abilityService.getMaxWeight(userId)
        );
    }

    private List<Map<String, Object>> getRoomItems(Room room) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (Item item : room.getItems()) {
            items.add(Map.of(
                "id", item.getId(),
                "name", item.getName(),
                "description", item.getDescription(),
                "weight", item.getWeight(),
                "value", item.getValue()
            ));
        }
        return items;
    }

    private List<Map<String, Object>> getPlayerInventory() {
        List<Map<String, Object>> items = new ArrayList<>();
        for (Item item : game.getPlayer().getInventory()) {
            items.add(Map.of(
                "id", item.getId(),
                "name", item.getName(),
                "description", item.getDescription(),
                "weight", item.getWeight(),
                "value", item.getValue()
            ));
        }
        return items;
    }
}
