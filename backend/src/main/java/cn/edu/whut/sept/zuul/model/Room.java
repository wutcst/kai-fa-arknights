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
        this.description = description;
        this.id = id;
        this.zhName = getZhNameById(id);
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
     * 根据房间ID获取中文名称.
     */
    private String getZhNameById(String id) {
        switch(id) {
            case "outside": return "罗德岛入口";
            case "theater": return "训练设施";
            case "pub": return "公开交易所";
            case "lab": return "加工站";
            case "office": return "精英干员办公室";
            case "portal": return "机密传送门";
            case "library": return "机密档案室";
            case "cafeteria": return "物资补给区";
            case "gym": return "体能训练场";
            case "garden": return "户外休闲区";
            case "bookstore": return "资源回收站";
            case "dormitory": return "干员宿舍区";
            case "theater_lobby": return "设施接待大厅";
            case "theater_classroom_101": return "基础训练室A";
            case "theater_classroom_102": return "基础训练室B";
            case "theater_stairway_1f": return "设施东侧通道";
            case "theater_classroom_201": return "进阶训练室A";
            case "theater_classroom_202": return "进阶训练室B";
            case "theater_office": return "人事档案室";
            case "theater_stairway_2f": return "设施中央通道";
            case "theater_classroom_301": return "精英训练室A";
            case "theater_classroom_302": return "精英训练室B";
            case "theater_lab": return "制造站";
            case "theater_stairway_3f": return "设施顶层通道";
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

