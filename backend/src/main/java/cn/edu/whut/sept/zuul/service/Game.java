/**
 * 游戏服务类.
 * 负责管理游戏房间、玩家位置及房间间连接.
 *
 * @author  Michael Kölling and David J. Barnes
 * @version 1.0
 */
package cn.edu.whut.sept.zuul.service;

import cn.edu.whut.sept.zuul.model.Room;
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

        currentRoom = outside;  // start game outside
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room room){
        this.currentRoom = room;
    }
}