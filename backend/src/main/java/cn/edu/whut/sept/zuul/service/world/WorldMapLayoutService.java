package cn.edu.whut.sept.zuul.service.world;

import cn.edu.whut.sept.zuul.model.world.WorldMapView;
import cn.edu.whut.sept.zuul.model.world.WorldRoom;
import cn.edu.whut.sept.zuul.model.world.WorldRoomLayout;
import cn.edu.whut.sept.zuul.repository.world.WorldMapViewRepository;
import cn.edu.whut.sept.zuul.repository.world.WorldRoomLayoutRepository;
import cn.edu.whut.sept.zuul.repository.world.WorldRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WorldMapLayoutService {
    private final WorldMapViewRepository mapViewRepository;
    private final WorldRoomLayoutRepository roomLayoutRepository;
    private final WorldRoomRepository roomRepository;

    public WorldMapLayoutService(WorldMapViewRepository mapViewRepository,
                                 WorldRoomLayoutRepository roomLayoutRepository,
                                 WorldRoomRepository roomRepository) {
        this.mapViewRepository = mapViewRepository;
        this.roomLayoutRepository = roomLayoutRepository;
        this.roomRepository = roomRepository;
    }

    @Transactional(readOnly = true)
    public MapLayoutSnapshot loadLayoutSnapshot(String currentRoomId) {
        LayoutConfig config = loadAndValidate();
        String currentViewType = resolveViewType(currentRoomId, config.roomLayoutsByRoomId);
        WorldMapView view = config.viewsByType.get(currentViewType);
        if (view == null) {
            throw new IllegalStateException("当前地图视图不存在: " + currentViewType);
        }
        return new MapLayoutSnapshot(currentViewType, view.getViewBox(), config.mapViews, config.roomLayoutsByRoomId);
    }

    private String resolveViewType(String currentRoomId, Map<String, List<RoomLayoutInfo>> roomLayoutsByRoomId) {
        List<RoomLayoutInfo> layouts = roomLayoutsByRoomId.get(currentRoomId);
        if (layouts == null || layouts.isEmpty()) {
            throw new IllegalStateException("当前房间缺少地图布局: " + currentRoomId);
        }
        return layouts.stream()
                .filter(RoomLayoutInfo::isPrimaryView)
                .findFirst()
                .orElse(layouts.get(0))
                .getViewType();
    }

    private LayoutConfig loadAndValidate() {
        List<WorldMapView> views = mapViewRepository.findAllByOrderByDisplayOrderAsc();
        if (views.isEmpty()) {
            throw new IllegalStateException("地图视图配置为空");
        }

        Map<String, WorldMapView> viewsByType = new LinkedHashMap<>();
        List<MapViewInfo> mapViews = new ArrayList<>();
        for (WorldMapView view : views) {
            if (view.getViewType() == null || view.getViewType().isBlank()) {
                throw new IllegalStateException("地图视图类型不能为空");
            }
            if (view.getViewBox() == null || view.getViewBox().isBlank()) {
                throw new IllegalStateException("地图视图 viewBox 不能为空: " + view.getViewType());
            }
            viewsByType.put(view.getViewType(), view);
            mapViews.add(new MapViewInfo(view.getViewType(), view.getViewName(), view.getViewBox()));
        }

        Set<String> roomIds = roomRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(WorldRoom::getRoomId)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        if (roomIds.isEmpty()) {
            throw new IllegalStateException("世界房间配置为空");
        }

        Map<String, List<WorldRoomLayout>> rawLayoutsByRoomId = new LinkedHashMap<>();
        for (WorldRoomLayout layout : roomLayoutRepository.findAllByOrderByViewTypeAscDisplayOrderAsc()) {
            validateLayout(layout, viewsByType.keySet(), roomIds);
            rawLayoutsByRoomId.computeIfAbsent(layout.getRoomId(), ignored -> new ArrayList<>()).add(layout);
        }

        Map<String, List<RoomLayoutInfo>> roomLayoutsByRoomId = new LinkedHashMap<>();
        for (String roomId : roomIds) {
            List<WorldRoomLayout> layouts = rawLayoutsByRoomId.get(roomId);
            if (layouts == null || layouts.isEmpty()) {
                throw new IllegalStateException("房间缺少地图布局: " + roomId);
            }
            long primaryCount = layouts.stream().filter(WorldRoomLayout::isPrimaryView).count();
            if (primaryCount > 1) {
                throw new IllegalStateException("房间存在多个主地图布局: " + roomId);
            }
            if (layouts.size() > 1 && primaryCount != 1) {
                throw new IllegalStateException("多视图房间必须指定一个主地图布局: " + roomId);
            }

            List<RoomLayoutInfo> infos = layouts.stream()
                    .map(row -> new RoomLayoutInfo(row.getViewType(), row.getX(), row.getY(), row.isPrimaryView()))
                    .toList();
            roomLayoutsByRoomId.put(roomId, infos);
        }

        return new LayoutConfig(viewsByType, mapViews, roomLayoutsByRoomId);
    }

    private void validateLayout(WorldRoomLayout layout, Set<String> viewTypes, Set<String> roomIds) {
        if (!viewTypes.contains(layout.getViewType())) {
            throw new IllegalStateException("地图布局引用了不存在的视图: " + layout.getViewType());
        }
        if (!roomIds.contains(layout.getRoomId())) {
            throw new IllegalStateException("地图布局引用了不存在的房间: " + layout.getRoomId());
        }
        if (layout.getX() == null || layout.getX() < 0 || layout.getY() == null || layout.getY() < 0) {
            throw new IllegalStateException("地图布局坐标不能为负数: " + layout.getRoomId());
        }
    }

    private static class LayoutConfig {
        private final Map<String, WorldMapView> viewsByType;
        private final List<MapViewInfo> mapViews;
        private final Map<String, List<RoomLayoutInfo>> roomLayoutsByRoomId;

        private LayoutConfig(Map<String, WorldMapView> viewsByType,
                             List<MapViewInfo> mapViews,
                             Map<String, List<RoomLayoutInfo>> roomLayoutsByRoomId) {
            this.viewsByType = viewsByType;
            this.mapViews = mapViews;
            this.roomLayoutsByRoomId = roomLayoutsByRoomId;
        }
    }
}
