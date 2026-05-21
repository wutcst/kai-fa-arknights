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
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 游戏核心服务.
 */
public class Game
{
    private Room currentRoom;
    private Map<String, Room> rooms;
    private List<Room> roomHistory;  // 房间移动历史
    private boolean justTeleported;   // 是否刚触发传送
    private String teleportedFrom;    // 从哪个房间传送走的

    public Game()
    {
        rooms = new HashMap<>();
        roomHistory = new ArrayList<>();
        justTeleported = false;
        createRooms();
    }

    public Map<String, Room> getRooms() {
        return rooms;
    }

    private void createRooms()
    {
        Room outside, theater, pub, lab, office, portal;

        // create the rooms
        outside = new Room("outside the main entrance of the university", "outside");
        theater = new Room("in a lecture theater", "theater");
        pub = new Room("in the campus pub", "pub");
        lab = new Room("in a computing lab", "lab");
        office = new Room("in the computing admin office", "office");
        portal = new Room("in a mysterious portal room", "portal");

        // initialise room exits
        outside.setExit("east", theater);
        outside.setExit("south", lab);
        outside.setExit("west", pub);
        outside.setExit("north", portal);

        theater.setExit("west", outside);

        pub.setExit("east", outside);

        lab.setExit("north", outside);
        lab.setExit("east", office);

        office.setExit("west", lab);

        // 传送房间只连接到校门口
        portal.setExit("south", outside);

        // save all rooms
        rooms.put("outside", outside);
        rooms.put("theater", theater);
        rooms.put("pub", pub);
        rooms.put("lab", lab);
        rooms.put("office", office);
        rooms.put("portal", portal);

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

        currentRoom = outside;  // start game outside
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
            // 随机传送到其他房间（除了传送房间本身）
            Room[] targetRooms = {rooms.get("outside"), rooms.get("theater"),
                                  rooms.get("pub"), rooms.get("lab"), rooms.get("office")};
            Random random = new Random();
            this.currentRoom = targetRooms[random.nextInt(targetRooms.length)];
            // 传送后不清除历史记录，而是以新位置为起点继续记录
        } else {
            // 普通房间移动，添加到历史记录
            if (!justTeleported) {
                roomHistory.add(currentRoom);
            }
            justTeleported = false;
            teleportedFrom = null;
            this.currentRoom = room;
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
        return backRoom;
    }

    /**
     * 检查是否可以回退.
     */
    public boolean canGoBack() {
        return !roomHistory.isEmpty();
    }
}