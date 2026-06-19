package cn.edu.whut.sept.zuul.service.world;

import cn.edu.whut.sept.zuul.model.Room;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LoadedWorld {
    private final Map<String, Room> rooms;
    private final String startRoomId;
    private final int defaultMaxWeight;
    private final int defaultPlayerGridRow;
    private final int defaultPlayerGridCol;
    private final long spawnRandomSeed;
    private final long portalRandomSeed;
    private final Map<String, List<String>> portalTargets;
    private final Map<String, Map<String, Integer>> itemEffects;

    public LoadedWorld(Map<String, Room> rooms,
                       String startRoomId,
                       int defaultMaxWeight,
                       int defaultPlayerGridRow,
                       int defaultPlayerGridCol,
                       long spawnRandomSeed,
                       long portalRandomSeed,
                       Map<String, List<String>> portalTargets,
                       Map<String, Map<String, Integer>> itemEffects) {
        this.rooms = rooms;
        this.startRoomId = startRoomId;
        this.defaultMaxWeight = defaultMaxWeight;
        this.defaultPlayerGridRow = defaultPlayerGridRow;
        this.defaultPlayerGridCol = defaultPlayerGridCol;
        this.spawnRandomSeed = spawnRandomSeed;
        this.portalRandomSeed = portalRandomSeed;
        this.portalTargets = portalTargets;
        this.itemEffects = itemEffects;
    }

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public String getStartRoomId() {
        return startRoomId;
    }

    public int getDefaultMaxWeight() {
        return defaultMaxWeight;
    }

    public int getDefaultPlayerGridRow() {
        return defaultPlayerGridRow;
    }

    public int getDefaultPlayerGridCol() {
        return defaultPlayerGridCol;
    }

    public long getSpawnRandomSeed() {
        return spawnRandomSeed;
    }

    public long getPortalRandomSeed() {
        return portalRandomSeed;
    }

    public Map<String, List<String>> getPortalTargets() {
        return Collections.unmodifiableMap(portalTargets);
    }

    public boolean isPortalRoom(String roomId) {
        return portalTargets.containsKey(roomId);
    }

    public List<String> getPortalTargetRoomIds(String roomId) {
        return portalTargets.getOrDefault(roomId, Collections.emptyList());
    }

    public int getItemEffectValue(String itemId, String effectCode) {
        return itemEffects.getOrDefault(itemId, Collections.emptyMap()).getOrDefault(effectCode, 0);
    }
}
