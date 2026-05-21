package cn.edu.whut.sept.zuul.model;

import java.util.Set;
import java.util.HashMap;
import java.util.Map;

public class RoomDTO {
    private String name;
    private String description;
    private String zhName;
    private Map<String, String> exits;

    public RoomDTO(String name, String description, String zhName, Set<String> exits) {
        this.name = name;
        this.description = description;
        this.zhName = zhName;
        this.exits = new java.util.HashMap<>();
        for (String exit : exits) {
            this.exits.put(exit, exit);
        }
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getZhName() { return zhName; }
    public Map<String, String> getExits() { return exits; }
}
