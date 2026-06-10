package cn.edu.whut.sept.zuul.model;

import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 房间实体类.
 * 表示游戏中的一个地点.
 */
public class Room
{
    private String description;
    private String id;
    private String zhName;
    private HashMap<String, Room> exits;        // 房间出口
    private List<Item> items;                   // 房间内的物品

    public Room(String description, String id)
    {
        this.description = description;
        this.id = id;
        this.zhName = getZhNameById(id);
        exits = new HashMap<>();
        items = new ArrayList<>();
    }

    /**
     * 向房间添加物品.
     */
    public void addItem(Item item) {
        items.add(item);
    }

    /**
     * 从房间移除物品.
     */
    public void removeItem(String itemId) {
        items.removeIf(item -> item.getId().equals(itemId));
    }

    /**
     * 获取房间内的物品列表.
     */
    public List<Item> getItems() {
        return items;
    }

    /**
     * 根据物品ID获取物品.
     */
    public Item getItem(String itemId) {
        for (Item item : items) {
            if (item.getId().equals(itemId)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 获取房间内物品总重量.
     */
    public int getTotalWeight() {
        int total = 0;
        for (Item item : items) {
            total += item.getWeight();
        }
        return total;
    }

    /**
     * 获取房间内物品总价值.
     */
    public int getTotalValue() {
        int total = 0;
        for (Item item : items) {
            total += item.getValue();
        }
        return total;
    }

    /**
     * 根据房间ID获取中文名称.
     */
    private String getZhNameById(String id) {
        switch(id) {
            case "outside": return "校门口";
            case "theater": return "教学楼";
            case "pub": return "校园酒吧";
            case "lab": return "计算机实验室";
            case "office": return "办公室";
            case "portal": return "传送门";
            case "library": return "图书馆";
            case "cafeteria": return "食堂";
            case "gym": return "体育馆";
            case "garden": return "花园";
            case "dormitory": return "宿舍楼";
            case "bookstore": return "书店";
            case "theater_lobby": return "教学楼大厅";
            case "theater_classroom_101": return "101教室";
            case "theater_classroom_102": return "102教室";
            case "theater_stairway_1f": return "一楼楼梯间";
            case "theater_classroom_201": return "201教室";
            case "theater_classroom_202": return "202教室";
            case "theater_office": return "教师办公室";
            case "theater_stairway_2f": return "二楼楼梯间";
            case "theater_classroom_301": return "301教室";
            case "theater_classroom_302": return "302教室";
            case "theater_lab": return "计算机实验室";
            case "theater_stairway_3f": return "三楼楼梯间";
            default: return id;
        }
    }

    /**
     * 设置房间物品（用于从存档恢复）.
     *
     * @param items 物品列表
     */
    public void setItems(List<Item> items) {
        this.items = items;
    }

    /**
     * 获取房间ID.
     */
    public String getId() { return id; }

    /**
     * 获取房间中文名称.
     */
    public String getZhName() { return zhName; }

    /**
     * 设置房间出口.
     *
     * @param direction 方向
     * @param neighbor  相连的房间
     */
    public void setExit(String direction, Room neighbor)
    {
        exits.put(direction, neighbor);
    }

    /**
     * 获取房间短描述（英文）.
     */
    public String getShortDescription()
    {
        return description;
    }

    /**
     * 获取房间长描述.
     */
    public String getLongDescription()
    {
        return "You are " + description + ".\n" + getExitString();
    }

    /**
     * 获取出口方向字符串.
     */
    private String getExitString()
    {
        String returnString = "Exits:";
        Set<String> keys = exits.keySet();
        for(String exit : keys) {
            returnString += " " + exit;
        }
        return returnString;
    }

    /**
     * 获取指定方向的出口房间.
     *
     * @param direction 方向
     * @return 出口房间，如果不存在则返回null
     */
    public Room getExit(String direction)
    {
        return exits.get(direction);
    }

    /**
     * 获取所有出口方向.
     */
    public Set<String> getExits() {
        return exits.keySet();
    }
}
