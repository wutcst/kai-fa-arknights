package cn.edu.whut.sept.zuul.service.world;

import java.util.List;
import java.util.Map;

public class MapLayoutSnapshot {
    private final String currentViewType;
    private final String viewBox;
    private final List<MapViewInfo> mapViews;
    private final Map<String, List<RoomLayoutInfo>> roomLayoutsByRoomId;

    public MapLayoutSnapshot(String currentViewType,
                             String viewBox,
                             List<MapViewInfo> mapViews,
                             Map<String, List<RoomLayoutInfo>> roomLayoutsByRoomId) {
        this.currentViewType = currentViewType;
        this.viewBox = viewBox;
        this.mapViews = mapViews;
        this.roomLayoutsByRoomId = roomLayoutsByRoomId;
    }

    public String getCurrentViewType() {
        return currentViewType;
    }

    public String getViewBox() {
        return viewBox;
    }

    public List<MapViewInfo> getMapViews() {
        return mapViews;
    }

    public Map<String, List<RoomLayoutInfo>> getRoomLayoutsByRoomId() {
        return roomLayoutsByRoomId;
    }
}
