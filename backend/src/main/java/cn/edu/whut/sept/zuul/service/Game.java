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
    private Map<String, Map<String, GridPosition>> initialRoomItemPositions;
    private List<Room> roomHistory;  // 房间移动历史
    private boolean justTeleported;   // 是否刚触发传送
    private String teleportedFrom;    // 从哪个房间传送走的
    private Player player;            // 玩家对象
    private Long currentUserId;       // 当前用户ID

    public Game()
    {
        rooms = new HashMap<>();
        initialRoomItems = new HashMap<>();
        initialRoomItemPositions = new HashMap<>();
        roomHistory = new ArrayList<>();
        justTeleported = false;
        player = new Player("冒险者");
        createRooms();
        initializeRoomItemPositions();
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

        // 训练设施内部房间
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

        // 传送房间只连接到罗德岛入口
        portal.setExit("south", outside);

        // 训练设施内部连接
        // 一楼：south进入内部
        theater.setExit("south", theaterLobby);  // 从外部进入训练设施内部
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

        // 设施三层
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

        // 训练设施内部房间
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
        outside.addItem(new Item("orirock", "源岩", "最基础的岩石原料，广泛用于初级加工与制造，能从几乎所有岩层中采集到。", 2, 5));
        outside.addItem(new Item("orirock_cube", "固源岩", "将源岩粉碎后重组而成的坚固立方体，基建制造与干员初期精英化的常用素材。", 1, 1));

        theater.addItem(new Item("orirock_concentration", "提纯源岩", "经过多道工序提纯的高密度源岩，硬度极高，是高级精英化与专精的基石材料。", 3, 10));
        theater.addItem(new Item("device", "装置", "功能完好的通用型机械装置，是制造全新装置和合成各类精密仪器的中间产物。", 5, 50));

        pub.addItem(new Item("loxic_kohl", "扭转醇", "具有特殊旋光性的醇类化合物，是合成白马醇等多种关键药物与工业品的前置原料。", 2, 15));
        pub.addItem(new Item("white_horse_kohl", "白马醇", "由扭转醇精制而成的纯白醇类，性质极其稳定，高级术师与治疗干员技能专精的消耗品。", 1, 8));

        lab.addItem(new Item("integrated_device", "全新装置", "刚从生产线下来的精密装置，性能处于最佳状态，为精英化二阶段和关键技能专精所必需。", 3, 200));
        lab.addItem(new Item("crystalline_component", "晶体元件", "从晶体矿物上切割下的基础电子元件，是构建晶体电路等复杂系统的起点。", 1, 80));
        lab.addItem(new Item("crystalline_circuit", "晶体电路", "集成了多个晶体元件的高密度电路模块，运算性能强大，用于高级技能专精与模组数据块制造。", 8, 5000));

        office.addItem(new Item("module_data_block", "模组数据块", "记录着干员个性化作战分析与适配方案的数据块，用于解锁和升级专属模组系统。", 1, 100));
        office.addItem(new Item("rma70_12", "RMA70-12", "源石技艺与现代工业结合的半成品，编号70-12，稀有度高，是多种高端电子元件的基板。", 1, 50));

        library.addItem(new Item("sintered_core", "烧结核凝晶", "在超高压高温下烧结的能量核心，内部蕴含恐怖能量，为重装与医疗干员的终极专属材料。", 3, 80));
        library.addItem(new Item("bipolar_nanoflake", "双极纳米片", "带有正负电荷的纳米级薄片，是源石技艺放大器的核心，术师与辅助干员的顶级专精需求。", 4, 120));

        gym.addItem(new Item("oriron", "异铁", "在天然磁场中生成的奇异铁矿石，采集后可用于熔炼异铁组，是基础工业原料之一。", 2, 100));
        gym.addItem(new Item("oriron_shard", "异铁碎片", "开采异铁矿时产生的碎片，可合成完整异铁，常用于初期武器与装备的强化。", 1, 10));

        cafeteria.addItem(new Item("sugar", "糖", "便携式高能代糖补给，不仅是干员作战时的能量来源，也是制造糖组的基本材料。", 2, 15));
        cafeteria.addItem(new Item("sugar_pack", "糖组", "将糖压缩包装后的能量块，便于大量储存与运输，是中期精英化和技能升级的常见需求。", 2, 20));

        garden.addItem(new Item("polyketon", "酮凝集", "有机聚合物形成的凝胶状物质，可作为粘合剂与绝缘层，是制造站首批可生产的材料之一。", 1, 5));

        bookstore.addItem(new Item("sugar_lump", "糖聚块", "高度提纯并聚合的糖晶体，蕴含惊人能量，仅供顶尖技能的专精与模组升级使用。", 1, 25));
        bookstore.addItem(new Item("aketon", "酮凝集组", "酮凝集经过压缩和固化处理后的块状物，绝缘与隔源性能优异，极受术师干员青睐。", 1, 15));

        dormitory.addItem(new Item("polyester", "聚酸酯", "常见的合成树脂原料，轻便且易于塑形，是制造聚酸酯组和部分家具零件的基础素材。", 1, 20));
        dormitory.addItem(new Item("polyester_pack", "聚酸酯组", "多份聚酸酯的标准化封装包，便于运输与管理，满足干员中期精英化的大量消耗。", 2, 40));

        // 训练设施内部物品
        theaterLobby.addItem(new Item("oriron_cluster", "异铁组", "由数块异铁组合而成的标准加工单元，广泛用于重装干员的精英化与防御装备制造。", 1, 0));
        theaterLobby.addItem(new Item("keton_colloid", "酮阵列", "在特殊条件下令酮凝集组规整排列形成的胶体阵列，结构极度稳定，用于尖端的源石技艺强化。", 1, 0));

        theaterClassroom101.addItem(new Item("grindstone", "研磨石", "表面密布研磨颗粒的工具石，能将粗加工部件打磨至微米级精度，泛用性极高。", 10, 50));
        theaterClassroom101.addItem(new Item("grindstone_pentahydrate", "五水研磨石", "含有五个结晶水的特殊研磨石，研磨精度进一步提升，是生产双极纳米片等顶级材料的关键。", 1, 5));

        theaterClassroom102.addItem(new Item("rma70_24", "RMA70-24", "RMA70-12的深度加工型，内部回路更为复杂，专为精英化二阶段及精密仪器制造而设计。", 1, 20));
        theaterClassroom102.addItem(new Item("incandescent_alloy", "炽合金", "能在极高温度下保持稳定的合金，是制作武器隔热层与源石蚀刻回路的重要材料。", 1, 10));

        theaterStairway1f.addItem(new Item("damaged_device", "破损装置", "在冲突中受损的机械装置，虽然无法直接使用，但拆解后仍能回收若干标准零件。", 5, 100));

        theaterClassroom201.addItem(new Item("oriron_block", "异铁块", "将异铁组熔炼锻压成的超合金块，坚不可摧，是重装与部分近卫干员专精的顶级材料。", 3, 80));
        theaterClassroom201.addItem(new Item("compound_cutting_fluid", "化合切削液", "用于精密加工的特种化学液，能显著提升材料切割精度，是维多利亚篇章后出现的新素材。", 2, 0));

        theaterClassroom202.addItem(new Item("incandescent_alloy_block", "炽合金块", "炽合金的锻压块，耐热极限更为出色，近卫与狙击干员高阶专精的必备消耗品。", 4, 120));

        theaterOffice.addItem(new Item("refined_solvent", "精炼溶剂", "经过多重蒸馏的超纯溶剂，能溶解绝大多数顽固原料，是制造聚合凝胶和聚合剂的必需品。", 1, 25));
        theaterOffice.addItem(new Item("semi_synthetic_solvent", "半自然溶剂", "天然提取物与合成溶剂的混合物，调和了效能与成本，是精炼溶剂的前置半成品。", 1, 0));

        theaterStairway2f.addItem(new Item("cutting_fluid_solution", "切削原液", "未经稀释的高浓度切削液，切割能力极强，但直接使用风险大，须调配成化合切削液。", 2, 50));

        theaterClassroom301.addItem(new Item("polyester_lump", "聚酸酯块", "由聚酸酯组高压聚合而成的硬质块体，强度远超普通酯类，用于精英化二阶段的防具制作。", 3, 80));
        theaterClassroom301.addItem(new Item("d32_steel", "D32钢", "代号D32的特种合金，完美平衡了物理强度与源石传导率，是近卫与先锋的终极专精材料。", 4, 100));

        theaterClassroom302.addItem(new Item("crystalline_electronic_unit", "晶体电子单元", "将晶体电路与高密度能源整合的微型单元，狙击与特种干员达成顶尖战力的必要材料。", 3, 0));

        theaterLab.addItem(new Item("polymerized_gel", "聚合凝胶", "通过高分子聚合而成的特殊凝胶，拥有惊人的吸附与缓冲能力，广泛应用于医疗与防护插板。", 8, 2000));
        theaterLab.addItem(new Item("polymerization_agent", "聚合剂", "能催化聚合反应的高效药剂，是制造双极纳米片和D32钢等终极素材的核心触媒。", 1, 30));
        theaterLab.addItem(new Item("rma70_12", "RMA70-12", "源石技艺与现代工业结合的半成品，编号70-12，稀有度高，是多种高端电子元件的基板。", 2, 50));

        theaterStairway3f.addItem(new Item("bipolar_nanoflake", "双极纳米片", "带有正负电荷的纳米级薄片，是源石技艺放大器的核心，术师与辅助干员的顶级专精需求。", 1, 0));

        // 随机在多个房间添加理智增强剂
        Random random = new Random();
        Room[] cookieRooms = {outside, pub, lab, library, gym, cafeteria, garden, bookstore,
                             theaterLobby, theaterClassroom101, theaterClassroom102,
                             theaterClassroom201, theaterClassroom202, theaterOffice,
                             theaterClassroom301, theaterClassroom302, theaterLab};
        int cookieCount = random.nextInt(6) + 5;  // 5-10块理智增强剂
        for (int i = 0; i < cookieCount; i++) {
            Room r = cookieRooms[random.nextInt(cookieRooms.length)];
            r.addItem(new Item("magic_cookie", "理智增强剂", "罗德岛开发的特殊药剂，注射可以增加负重", 1, 0));
        }

        currentRoom = outside;  // start game outside
        player.setCurrentRoom(currentRoom);
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
        if (room.getId().equals("portal")) {
            teleportedFrom = currentRoom.getZhName();  // 记录传送前的位置
            justTeleported = true;
            // 随机传送到其他房间（除了传送房间本身和训练设施内部）
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
        player.increaseMaxWeight(5);
        return "你吃了理智增强剂！负重上限增加了5点（当前负重上限：" +
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
