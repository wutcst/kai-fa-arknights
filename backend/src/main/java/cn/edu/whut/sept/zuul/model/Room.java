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
    private Map<String, GridPosition> itemPositions; // 物品在房间网格中的位置

    public Room(String description, String id)
    {
        this(description, id, id);
    }

    public Room(String description, String id, String zhName)
    {
        this.description = description;
        this.id = id;
        this.zhName = zhName;
        exits = new HashMap<>();
        items = new ArrayList<>();
        itemPositions = new HashMap<>();
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
        itemPositions.remove(itemId);
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

    public void setItemPosition(String itemId, GridPosition position) {
        itemPositions.put(itemId, position);
    }

    public GridPosition getItemPosition(String itemId) {
        return itemPositions.get(itemId);
    }

    public Map<String, GridPosition> getItemPositions() {
        return new HashMap<>(itemPositions);
    }

    public void setItemPositions(Map<String, GridPosition> positions) {
        itemPositions = new HashMap<>();
        if (positions != null) {
            itemPositions.putAll(positions);
        }
    }

    public boolean hasItemAt(int row, int col) {
        return getItemAt(row, col) != null;
    }

    public Item getItemAt(int row, int col) {
        for (Item item : items) {
            GridPosition position = itemPositions.get(item.getId());
            if (position != null && position.getRow() == row && position.getCol() == col) {
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
