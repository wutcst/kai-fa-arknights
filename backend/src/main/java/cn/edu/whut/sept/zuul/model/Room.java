package cn.edu.whut.sept.zuul.model;

import java.util.Set;
import java.util.HashMap;

public class Room
{
    private String description;
    private String id;
    private String zhName;
    private HashMap<String, Room> exits;        // stores exits of this room.

    public Room(String description, String id)
    {
        this.description = description;
        this.id = id;
        this.zhName = getZhNameById(id);
        exits = new HashMap<>();
    }

    private String getZhNameById(String id) {
        switch(id) {
            case "outside": return "校门口";
            case "theater": return "教学楼";
            case "pub": return "校园酒吧";
            case "lab": return "计算机实验室";
            case "office": return "办公室";
            default: return id;
        }
    }

    public String getId() { return id; }
    public String getZhName() { return zhName; }

    public void setExit(String direction, Room neighbor)
    {
        exits.put(direction, neighbor);
    }

    public String getShortDescription()
    {
        return description;
    }

    public String getLongDescription()
    {
        return "You are " + description + ".\n" + getExitString();
    }

    private String getExitString()
    {
        String returnString = "Exits:";
        Set<String> keys = exits.keySet();
        for(String exit : keys) {
            returnString += " " + exit;
        }
        return returnString;
    }

    public Room getExit(String direction)
    {
        return exits.get(direction);
    }

    public Set<String> getExits() {
        return exits.keySet();
    }
}


