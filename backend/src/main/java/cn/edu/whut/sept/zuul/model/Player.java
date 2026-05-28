package cn.edu.whut.sept.zuul.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 玩家实体类.
 * 表示游戏中的玩家角色.
 */
public class Player {
    private String name;
    private Room currentRoom;
    private List<Item> inventory;        // 随身物品
    private int maxWeight;              // 最大负重
    private int baseMaxWeight;          // 基础最大负重

    public Player(String name) {
        this.name = name;
        this.inventory = new ArrayList<>();
        this.baseMaxWeight = 20;        // 基础负重20
        this.maxWeight = baseMaxWeight;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room room) {
        this.currentRoom = room;
    }

    /**
     * 获取随身物品列表.
     */
    public List<Item> getInventory() {
        return inventory;
    }

    /**
     * 获取当前总重量.
     */
    public int getTotalWeight() {
        int total = 0;
        for (Item item : inventory) {
            total += item.getWeight();
        }
        return total;
    }

    /**
     * 获取最大负重.
     */
    public int getMaxWeight() {
        return maxWeight;
    }

    /**
     * 增加最大负重（魔法饼干效果）.
     */
    public void increaseMaxWeight(int amount) {
        this.maxWeight += amount;
    }

    /**
     * 设置最大负重.
     */
    public void setMaxWeight(int weight) {
        this.maxWeight = weight;
    }

    /**
     * 检查是否可以添加物品.
     */
    public boolean canCarry(Item item) {
        return getTotalWeight() + item.getWeight() <= maxWeight;
    }

    /**
     * 添加物品到背包.
     */
    public boolean addItem(Item item) {
        if (canCarry(item)) {
            inventory.add(item);
            return true;
        }
        return false;
    }

    /**
     * 从背包移除物品.
     */
    public Item removeItem(String itemId) {
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.get(i).getId().equals(itemId)) {
                return inventory.remove(i);
            }
        }
        return null;
    }

    /**
     * 检查背包中是否有指定物品.
     */
    public boolean hasItem(String itemId) {
        for (Item item : inventory) {
            if (item.getId().equals(itemId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取背包中物品总价值.
     */
    public int getTotalValue() {
        int total = 0;
        for (Item item : inventory) {
            total += item.getValue();
        }
        return total;
    }
}
