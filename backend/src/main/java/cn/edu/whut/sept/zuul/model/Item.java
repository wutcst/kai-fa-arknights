package cn.edu.whut.sept.zuul.model;

/**
 * 物品实体类.
 * 表示游戏中的物品.
 */
public class Item {
    private String id;
    private String name;
    private String description;
    private int weight;
    private int value;

    public Item() {
        // 无参构造函数，供Jackson反序列化使用
    }

    public Item(String id, String name, String description, int weight, int value) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.weight = weight;
        this.value = value;
    }

    /**
     * 获取物品ID.
     */
    public String getId() { return id; }

    /**
     * 获取物品名称.
     */
    public String getName() { return name; }

    /**
     * 获取物品描述.
     */
    public String getDescription() { return description; }

    /**
     * 获取物品重量.
     */
    public int getWeight() { return weight; }

    /**
     * 获取物品价值.
     */
    public int getValue() { return value; }
}
