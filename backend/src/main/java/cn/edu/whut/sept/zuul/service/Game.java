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

/**
 * 游戏核心服务.
 */
public class Game
{
    private Room currentRoom;
    private Map<String, Room> rooms;

    public Game()
    {
        rooms = new HashMap<>();
        createRooms();
    }

    public Map<String, Room> getRooms() {
        return rooms;
    }

    private void createRooms()
    {
        Room outside, theater, pub, lab, office;

        // create the rooms
        outside = new Room("outside the main entrance of the university", "outside");
        theater = new Room("in a lecture theater", "theater");
        pub = new Room("in the campus pub", "pub");
        lab = new Room("in a computing lab", "lab");
        office = new Room("in the computing admin office", "office");

        // initialise room exits
        outside.setExit("east", theater);
        outside.setExit("south", lab);
        outside.setExit("west", pub);

        theater.setExit("west", outside);

        pub.setExit("east", outside);

        lab.setExit("north", outside);
        lab.setExit("east", office);

        office.setExit("west", lab);

        // save all rooms
        rooms.put("outside", outside);
        rooms.put("theater", theater);
        rooms.put("pub", pub);
        rooms.put("lab", lab);
        rooms.put("office", office);

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

    public void setCurrentRoom(Room room){
        this.currentRoom = room;
    }
}