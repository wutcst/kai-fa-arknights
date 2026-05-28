/**
 * 游戏服务类.
 * 负责管理游戏房间、玩家位置及房间间连接.
 *
 * @author  Michael Kölling and David J. Barnes
 * @version 1.0
 */
package cn.edu.whut.sept.zuul.service;

import cn.edu.whut.sept.zuul.model.Room;
import cn.edu.whut.sept.zuul.model.Item;
import cn.edu.whut.sept.zuul.model.Player;
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
public class Game {
    private Room currentRoom;
    private Map<String, Room> rooms;
    private Map<String, List<Item>> initialRoomItems;  // 初始房间物品快照
    private List<Room> roomHistory;  // 房间移动历史
    private boolean justTeleported;   // 是否刚触发传送
    private String teleportedFrom;    // 从哪个房间传送走的
    private Player player;            // 玩家对象

    public Game()
    {
        rooms = new HashMap<>();
        initialRoomItems = new HashMap<>();
        roomHistory = new ArrayList<>();
        justTeleported = false;
        player = new Player("冒险者");
        createRooms();
        saveInitialRoomItems();
    }

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public Player getPlayer() {
        return player;
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
    }

    private void createRooms()
    {
        Room outside, theater, pub, lab, office, portal;
        Room library, gym, cafeteria, garden, bookstore, dormitory;
        Room theaterLobby, theaterClassroom101, theaterClassroom102, theaterStairway1f;
        Room theaterClassroom201, theaterClassroom202, theaterOffice, theaterStairway2f;
        Room theaterClassroom301, theaterClassroom302, theaterLab, theaterStairway3f;

        // create the rooms
        outside = new Room("outside the main entrance of the university", "outside");
        theater = new Room("in a lecture theater", "theater");
        pub = new Room("in the campus pub", "pub");
        lab = new Room("in a computing lab", "lab");
        office = new Room("in the computing admin office", "office");
        portal = new Room("in a mysterious portal room", "portal");
        library = new Room("in the university library", "library");
        gym = new Room("in the campus gym", "gym");
        cafeteria = new Room("in the campus cafeteria", "cafeteria");
        garden = new Room("in the campus garden", "garden");
        bookstore = new Room("in the campus bookstore", "bookstore");
        dormitory = new Room("in the student dormitory", "dormitory");

        // 教学楼内部房间
        theaterLobby = new Room("in the theater lobby", "theater_lobby");
        theaterClassroom101 = new Room("in classroom 101", "theater_classroom_101");
        theaterClassroom102 = new Room("in classroom 102", "theater_classroom_102");
        theaterStairway1f = new Room("in the 1st floor stairway", "theater_stairway_1f");

        theaterClassroom201 = new Room("in classroom 201", "theater_classroom_201");
        theaterClassroom202 = new Room("in classroom 202", "theater_classroom_202");
        theaterOffice = new Room("in the teacher office", "theater_office");
        theaterStairway2f = new Room("in the 2nd floor stairway", "theater_stairway_2f");

        theaterClassroom301 = new Room("in classroom 301", "theater_classroom_301");
        theaterClassroom302 = new Room("in classroom 302", "theater_classroom_302");
        theaterLab = new Room("in the computer lab", "theater_lab");
        theaterStairway3f = new Room("in the 3rd floor stairway", "theater_stairway_3f");

        // initialise room exits
        outside.setExit("east", theater);
        outside.setExit("south", lab);
        outside.setExit("west", pub);
        outside.setExit("north", portal);

        theater.setExit("west", outside);
        theater.setExit("north", library);

        library.setExit("south", theater);

        pub.setExit("east", outside);
        pub.setExit("south", gym);

        gym.setExit("north", pub);
        gym.setExit("south", cafeteria);

        cafeteria.setExit("north", gym);

        lab.setExit("north", outside);
        lab.setExit("east", office);
        lab.setExit("south", garden);

        office.setExit("west", lab);

        garden.setExit("north", lab);
        garden.setExit("west", bookstore);
        garden.setExit("south", dormitory);

        bookstore.setExit("east", garden);

        dormitory.setExit("north", garden);

        // 传送房间只连接到校门口
        portal.setExit("south", outside);

        // 教学楼内部连接
        // 一楼：south进入内部
        theater.setExit("south", theaterLobby);  // 从外部进入教学楼内部
        theaterLobby.setExit("north", theater);  // 回到外部
        theaterLobby.setExit("west", theaterClassroom101);
        theaterLobby.setExit("east", theaterClassroom102);
        theaterLobby.setExit("up", theaterStairway1f);
        theaterClassroom101.setExit("east", theaterLobby);
        theaterClassroom102.setExit("west", theaterLobby);
        theaterStairway1f.setExit("down", theaterLobby);
        theaterStairway1f.setExit("up", theaterStairway2f);

        // 二楼
        theaterStairway2f.setExit("down", theaterStairway1f);
        theaterStairway2f.setExit("up", theaterStairway3f);
        theaterStairway2f.setExit("west", theaterClassroom201);
        theaterStairway2f.setExit("east", theaterClassroom202);
        theaterStairway2f.setExit("south", theaterOffice);
        theaterClassroom201.setExit("east", theaterStairway2f);
        theaterClassroom202.setExit("west", theaterStairway2f);
        theaterOffice.setExit("north", theaterStairway2f);

        // 三楼
        theaterStairway3f.setExit("down", theaterStairway2f);
        theaterStairway3f.setExit("west", theaterClassroom301);
        theaterStairway3f.setExit("east", theaterClassroom302);
        theaterStairway3f.setExit("south", theaterLab);
        theaterClassroom301.setExit("east", theaterStairway3f);
        theaterClassroom302.setExit("west", theaterStairway3f);
        theaterLab.setExit("north", theaterStairway3f);

        // save all rooms
        rooms.put("outside", outside);
        rooms.put("theater", theater);
        rooms.put("pub", pub);
        rooms.put("lab", lab);
        rooms.put("office", office);
        rooms.put("portal", portal);
        rooms.put("library", library);
        rooms.put("gym", gym);
        rooms.put("cafeteria", cafeteria);
        rooms.put("garden", garden);
        rooms.put("bookstore", bookstore);
        rooms.put("dormitory", dormitory);

        // 教学楼内部房间
        rooms.put("theater_lobby", theaterLobby);
        rooms.put("theater_classroom_101", theaterClassroom101);
        rooms.put("theater_classroom_102", theaterClassroom102);
        rooms.put("theater_stairway_1f", theaterStairway1f);
        rooms.put("theater_classroom_201", theaterClassroom201);
        rooms.put("theater_classroom_202", theaterClassroom202);
        rooms.put("theater_office", theaterOffice);
        rooms.put("theater_stairway_2f", theaterStairway2f);
        rooms.put("theater_classroom_301", theaterClassroom301);
        rooms.put("theater_classroom_302", theaterClassroom302);
        rooms.put("theater_lab", theaterLab);
        rooms.put("theater_stairway_3f", theaterStairway3f);

        // 添加物品到各个房间
        outside.addItem(new Item("stone", "石头", "一块普通的石头", 2, 5));
        outside.addItem(new Item("leaf", "树叶", "一片绿色的树叶", 1, 1));

        theater.addItem(new Item("chalk", "粉笔", "盒装粉笔", 3, 10));
        theater.addItem(new Item("book", "教材", "一本计算机教材", 5, 50));

        pub.addItem(new Item("beer", "啤酒", "一杯冰镇啤酒", 2, 15));
        pub.addItem(new Item("snack", "小吃", "一袋薯片", 1, 8));

        lab.addItem(new Item("keyboard", "键盘", "机械键盘", 3, 200));
        lab.addItem(new Item("mouse", "鼠标", "无线鼠标", 1, 80));
        lab.addItem(new Item("laptop", "笔记本电脑", "联想ThinkPad", 8, 5000));

        office.addItem(new Item("paper", "文件", "一份重要文件", 1, 100));
        office.addItem(new Item("pen", "钢笔", "黑色钢笔", 1, 50));

        library.addItem(new Item("novel", "小说", "一本经典文学作品", 3, 80));
        library.addItem(new Item("dictionary", "词典", "英汉双语词典", 4, 120));

        gym.addItem(new Item("ball", "篮球", "一个标准篮球", 2, 100));
        gym.addItem(new Item("towel", "毛巾", "一条运动毛巾", 1, 10));

        cafeteria.addItem(new Item("rice", "米饭", "一份米饭套餐", 2, 15));
        cafeteria.addItem(new Item("noodles", "面条", "一碗牛肉面", 2, 20));

        garden.addItem(new Item("flower", "花朵", "一朵美丽的花", 1, 5));

        bookstore.addItem(new Item("magazine", "杂志", "最新一期科技杂志", 1, 25));
        bookstore.addItem(new Item("notebook", "笔记本", "精美笔记本", 1, 15));

        dormitory.addItem(new Item("pillow", "枕头", "柔软的枕头", 1, 20));
        dormitory.addItem(new Item("blanket", "毯子", "温暖的毯子", 2, 40));

        // 教学楼内部物品
        theaterLobby.addItem(new Item("brochure", "宣传册", "校园宣传册", 1, 0));
        theaterLobby.addItem(new Item("floor_map", "楼层地图", "教学楼楼层分布图", 1, 0));

        theaterClassroom101.addItem(new Item("desk", "课桌", "木制课桌", 10, 50));
        theaterClassroom101.addItem(new Item("eraser", "黑板擦", "粉尘黑板擦", 1, 5));

        theaterClassroom102.addItem(new Item("projector_remote", "投影仪遥控", "投影仪遥控器", 1, 20));
        theaterClassroom102.addItem(new Item("chalk_box", "粉笔盒", "一盒彩色粉笔", 1, 10));

        theaterStairway1f.addItem(new Item("fire_extinguisher", "灭火器", "干粉灭火器", 5, 100));

        theaterClassroom201.addItem(new Item("globe", "地球仪", "教学用地球仪", 3, 80));
        theaterClassroom201.addItem(new Item("student_work", "学生作品", "优秀学生美术作品", 2, 0));

        theaterClassroom202.addItem(new Item("model", "模型", "建筑结构模型", 4, 120));

        theaterOffice.addItem(new Item("coffee", "咖啡", "一杯热咖啡", 1, 25));
        theaterOffice.addItem(new Item("certificate", "证书", "优秀教师证书", 1, 0));

        theaterStairway2f.addItem(new Item("first_aid_kit", "急救箱", "基础急救用品", 2, 50));

        theaterClassroom301.addItem(new Item("globe_2", "地球仪", "教学用地球仪", 3, 80));
        theaterClassroom301.addItem(new Item("atlas", "地图集", "世界地图集", 4, 100));

        theaterClassroom302.addItem(new Item("painting", "油画", "风景油画", 3, 0));

        theaterLab.addItem(new Item("computer", "电脑", "教学用电脑", 8, 2000));
        theaterLab.addItem(new Item("usb_drive", "U盘", "16G优盘", 1, 30));
        theaterLab.addItem(new Item("lab_coat", "实验服", "白色实验服", 2, 50));

        theaterStairway3f.addItem(new Item("safety_sign", "安全标识", "消防通道指示牌", 1, 0));

        // 随机在多个房间添加魔法饼干
        Random random = new Random();
        Room[] cookieRooms = {outside, pub, lab, library, gym, cafeteria, garden, bookstore,
                             theaterLobby, theaterClassroom101, theaterClassroom102,
                             theaterClassroom201, theaterClassroom202, theaterOffice,
                             theaterClassroom301, theaterClassroom302, theaterLab};
        int cookieCount = random.nextInt(6) + 5;  // 5-10块魔法饼干
        for (int i = 0; i < cookieCount; i++) {
            Room r = cookieRooms[random.nextInt(cookieRooms.length)];
            r.addItem(new Item("magic_cookie", "魔法饼干", "散发神奇香气的饼干，吃了可以增加负重", 1, 0));
        }

        currentRoom = outside;  // start game outside
        player.setCurrentRoom(currentRoom);
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    /**
     * 设置当前房间，处理历史记录和传送逻辑.
     */
    public void setCurrentRoom(Room room) {
        // 如果进入传送房间，触发随机传送
        if (room.getId().equals("portal")) {
            teleportedFrom = currentRoom.getZhName();  // 记录传送前的位置
            justTeleported = true;
            // 随机传送到其他房间（除了传送房间本身和教学楼内部）
            Room[] targetRooms = {
                rooms.get("outside"), rooms.get("theater"),
                rooms.get("pub"), rooms.get("lab"), rooms.get("office"),
                rooms.get("library"), rooms.get("gym"), rooms.get("cafeteria"),
                rooms.get("garden"), rooms.get("bookstore"), rooms.get("dormitory"),
                rooms.get("theater_lobby"), rooms.get("theater_classroom_101"),
                rooms.get("theater_classroom_102"), rooms.get("theater_stairway_1f")
            };
            Random random = new Random();
            this.currentRoom = targetRooms[random.nextInt(targetRooms.length)];
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
     */
    public boolean canGoBack() {
        return !roomHistory.isEmpty();
    }

    /**
     * 拾取物品.
     * @param itemId 物品ID
     * @return 结果信息
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

    /**
     * 丢弃物品.
     * @param itemId 物品ID，如果为"all"则丢弃所有物品
     * @return 结果信息
     */
    public String dropItem(String itemId) {
        if (itemId.equals("all")) {
            if (player.getInventory().isEmpty()) {
                return "你身上没有任何物品！";
            }
            int count = 0;
            for (Item item : new ArrayList<>(player.getInventory())) {
                currentRoom.addItem(item);
                count++;
            }
            player.getInventory().clear();
            return "你丢弃了所有物品（" + count + "件）";
        }

        Item item = player.removeItem(itemId);
        if (item == null) {
            return "你身上没有这个物品！";
        }
        currentRoom.addItem(item);
        return "你丢弃了 " + item.getName();
    }

    /**
     * 吃魔法饼干.
     * @return 结果信息
     */
    public String eatCookie() {
        Item cookie = player.removeItem("magic_cookie");
        if (cookie == null) {
            return "你身上没有魔法饼干！";
        }
        player.increaseMaxWeight(5);
        return "你吃了魔法饼干！负重上限增加了5点（当前负重上限：" +
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
     */
    public List<Room> getRoomHistory() {
        return new ArrayList<>(roomHistory);
    }

    /**
     * 设置房间移动历史.
     */
    public void setRoomHistory(List<Room> history) {
        this.roomHistory.clear();
        this.roomHistory.addAll(history);
    }

    /**
     * 设置玩家背包物品.
     */
    public void setPlayerInventory(List<Item> items) {
        this.player.getInventory().clear();
        this.player.getInventory().addAll(items);
    }

    /**
     * 设置玩家最大负重.
     */
    public void setMaxWeight(int weight) {
        this.player.increaseMaxWeight(weight - this.player.getMaxWeight());
    }

    /**
     * 保存初始房间物品快照.
     */
    private void saveInitialRoomItems() {
        for (Map.Entry<String, Room> entry : rooms.entrySet()) {
            List<Item> itemsCopy = new ArrayList<>();
            for (Item item : entry.getValue().getItems()) {
                itemsCopy.add(new Item(item.getId(), item.getName(), item.getDescription(),
                                      item.getWeight(), item.getValue()));
            }
            initialRoomItems.put(entry.getKey(), itemsCopy);
        }
    }

    public void resetToStart() {
        this.currentRoom = rooms.get("outside");
        this.roomHistory.clear();
        this.player.getInventory().clear();
        this.player.setMaxWeight(20);
        this.justTeleported = false;
        this.teleportedFrom = null;
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
            }
        }
    }

    /**
     * 获取所有房间的Map.
     */
    public Map<String, Room> getAllRooms() {
        return this.rooms;
    }
}