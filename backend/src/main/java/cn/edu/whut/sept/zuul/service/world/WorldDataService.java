package cn.edu.whut.sept.zuul.service.world;

import cn.edu.whut.sept.zuul.model.GridPosition;
import cn.edu.whut.sept.zuul.model.Item;
import cn.edu.whut.sept.zuul.model.Room;
import cn.edu.whut.sept.zuul.model.world.WorldArea;
import cn.edu.whut.sept.zuul.model.world.WorldGameConfig;
import cn.edu.whut.sept.zuul.model.world.WorldItem;
import cn.edu.whut.sept.zuul.model.world.WorldItemEffect;
import cn.edu.whut.sept.zuul.model.world.WorldPortalTarget;
import cn.edu.whut.sept.zuul.model.world.WorldRandomSpawnCandidate;
import cn.edu.whut.sept.zuul.model.world.WorldRandomSpawnRule;
import cn.edu.whut.sept.zuul.model.world.WorldRoom;
import cn.edu.whut.sept.zuul.model.world.WorldRoomExit;
import cn.edu.whut.sept.zuul.model.world.WorldRoomInitialItem;
import cn.edu.whut.sept.zuul.repository.world.WorldAreaRepository;
import cn.edu.whut.sept.zuul.repository.world.WorldDirectionRepository;
import cn.edu.whut.sept.zuul.repository.world.WorldGameConfigRepository;
import cn.edu.whut.sept.zuul.repository.world.WorldItemEffectRepository;
import cn.edu.whut.sept.zuul.repository.world.WorldItemRepository;
import cn.edu.whut.sept.zuul.repository.world.WorldPortalTargetRepository;
import cn.edu.whut.sept.zuul.repository.world.WorldRandomSpawnCandidateRepository;
import cn.edu.whut.sept.zuul.repository.world.WorldRandomSpawnRuleRepository;
import cn.edu.whut.sept.zuul.repository.world.WorldRoomExitRepository;
import cn.edu.whut.sept.zuul.repository.world.WorldRoomInitialItemRepository;
import cn.edu.whut.sept.zuul.repository.world.WorldRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class WorldDataService {
    public static final String EFFECT_MAX_WEIGHT_BONUS = "MAX_WEIGHT_BONUS";
    private static final int GAME_CONFIG_ID = 1;
    private static final String ROOM_TYPE_PORTAL = "portal";
    private static final String ITEM_CATEGORY_CONSUMABLE = "consumable";

    private final WorldAreaRepository areaRepository;
    private final WorldRoomRepository roomRepository;
    private final WorldDirectionRepository directionRepository;
    private final WorldRoomExitRepository roomExitRepository;
    private final WorldItemRepository itemRepository;
    private final WorldItemEffectRepository itemEffectRepository;
    private final WorldRoomInitialItemRepository roomInitialItemRepository;
    private final WorldRandomSpawnRuleRepository randomSpawnRuleRepository;
    private final WorldRandomSpawnCandidateRepository randomSpawnCandidateRepository;
    private final WorldPortalTargetRepository portalTargetRepository;
    private final WorldGameConfigRepository gameConfigRepository;
    private final RandomSpawnPlanner randomSpawnPlanner;

    public WorldDataService(WorldAreaRepository areaRepository,
                            WorldRoomRepository roomRepository,
                            WorldDirectionRepository directionRepository,
                            WorldRoomExitRepository roomExitRepository,
                            WorldItemRepository itemRepository,
                            WorldItemEffectRepository itemEffectRepository,
                            WorldRoomInitialItemRepository roomInitialItemRepository,
                            WorldRandomSpawnRuleRepository randomSpawnRuleRepository,
                            WorldRandomSpawnCandidateRepository randomSpawnCandidateRepository,
                            WorldPortalTargetRepository portalTargetRepository,
                            WorldGameConfigRepository gameConfigRepository) {
        this.areaRepository = areaRepository;
        this.roomRepository = roomRepository;
        this.directionRepository = directionRepository;
        this.roomExitRepository = roomExitRepository;
        this.itemRepository = itemRepository;
        this.itemEffectRepository = itemEffectRepository;
        this.roomInitialItemRepository = roomInitialItemRepository;
        this.randomSpawnRuleRepository = randomSpawnRuleRepository;
        this.randomSpawnCandidateRepository = randomSpawnCandidateRepository;
        this.portalTargetRepository = portalTargetRepository;
        this.gameConfigRepository = gameConfigRepository;
        this.randomSpawnPlanner = new RandomSpawnPlanner();
    }

    @Transactional(readOnly = true)
    public LoadedWorld loadWorld() {
        WorldGameConfig config = loadGameConfig();

        Map<String, WorldArea> areas = indexAreas();
        Map<String, WorldItem> itemTemplates = indexItems();
        Map<String, WorldRoom> roomConfigs = indexRoomConfigs(areas);
        Map<String, Room> rooms = buildRooms(roomConfigs);
        validateRoomExists(rooms, config.getStartRoomId(), "起始房间不存在");
        validatePositive(config.getDefaultMaxWeight(), "默认负重必须大于0");
        validateGrid(config.getDefaultPlayerGridRow(), config.getDefaultPlayerGridCol(), "默认玩家格子越界");

        applyExits(rooms);
        applyInitialItems(rooms, itemTemplates);
        applyRandomItems(rooms, itemTemplates, config.getSpawnRandomSeed());

        Map<String, List<String>> portalTargets = buildPortalTargets(rooms, roomConfigs);
        Map<String, Map<String, Integer>> itemEffects = buildItemEffects(itemTemplates);
        validateMagicCookieEffect(itemTemplates, itemEffects);

        return new LoadedWorld(rooms,
                config.getStartRoomId(),
                config.getDefaultMaxWeight(),
                config.getDefaultPlayerGridRow(),
                config.getDefaultPlayerGridCol(),
                config.getSpawnRandomSeed(),
                config.getPortalRandomSeed(),
                portalTargets,
                itemEffects);
    }

    private WorldGameConfig loadGameConfig() {
        return gameConfigRepository.findById(GAME_CONFIG_ID)
                .orElseThrow(() -> new IllegalStateException("缺少世界主配置: " + GAME_CONFIG_ID));
    }

    private Map<String, WorldArea> indexAreas() {
        Map<String, WorldArea> areas = new HashMap<>();
        for (WorldArea area : areaRepository.findAll()) {
            areas.put(area.getAreaId(), area);
        }
        if (areas.isEmpty()) {
            throw new IllegalStateException("世界区域配置为空");
        }
        return areas;
    }

    private Map<String, WorldItem> indexItems() {
        Map<String, WorldItem> items = new HashMap<>();
        for (WorldItem item : itemRepository.findAll()) {
            if (item.getWeight() < 0 || item.getValue() < 0) {
                throw new IllegalStateException("物品重量和价值不能为负数: " + item.getItemId());
            }
            if (item.getItemCategory() == null || item.getItemCategory().isBlank()) {
                throw new IllegalStateException("物品分类不能为空: " + item.getItemId());
            }
            items.put(item.getItemId(), item);
        }
        if (items.isEmpty()) {
            throw new IllegalStateException("世界物品配置为空");
        }
        return items;
    }

    private Map<String, WorldRoom> indexRoomConfigs(Map<String, WorldArea> areas) {
        Map<String, WorldRoom> roomConfigs = new LinkedHashMap<>();
        List<WorldRoom> rows = roomRepository.findAllByOrderByDisplayOrderAsc();
        if (rows.isEmpty()) {
            throw new IllegalStateException("世界房间配置为空");
        }
        for (WorldRoom row : rows) {
            if (!areas.containsKey(row.getAreaId())) {
                throw new IllegalStateException("房间引用了不存在的区域: " + row.getRoomId());
            }
            roomConfigs.put(row.getRoomId(), row);
        }
        return roomConfigs;
    }

    private Map<String, Room> buildRooms(Map<String, WorldRoom> roomConfigs) {
        Map<String, Room> rooms = new LinkedHashMap<>();
        for (WorldRoom row : roomConfigs.values()) {
            rooms.put(row.getRoomId(), new Room(row.getDescription(), row.getRoomId(), row.getZhName()));
        }
        return rooms;
    }

    private void applyExits(Map<String, Room> rooms) {
        Set<String> directions = directionRepository.findAll().stream()
                .map(direction -> direction.getDirectionCode())
                .collect(java.util.stream.Collectors.toSet());
        if (directions.isEmpty()) {
            throw new IllegalStateException("世界方向配置为空");
        }

        for (WorldRoomExit exit : roomExitRepository.findAllByOrderBySourceRoomIdAscDisplayOrderAsc()) {
            Room source = validateRoomExists(rooms, exit.getSourceRoomId(), "出口源房间不存在");
            Room target = validateRoomExists(rooms, exit.getTargetRoomId(), "出口目标房间不存在");
            if (!directions.contains(exit.getDirectionCode())) {
                throw new IllegalStateException("出口方向不存在: " + exit.getDirectionCode());
            }
            source.setExit(exit.getDirectionCode(), target);
        }
    }

    private void applyInitialItems(Map<String, Room> rooms, Map<String, WorldItem> itemTemplates) {
        Set<String> roomItems = new HashSet<>();
        Set<String> roomCells = new HashSet<>();
        for (WorldRoomInitialItem row : roomInitialItemRepository.findAllByOrderByRoomIdAscDisplayOrderAsc()) {
            Room room = validateRoomExists(rooms, row.getRoomId(), "初始物品房间不存在");
            WorldItem item = validateItemExists(itemTemplates, row.getItemId());
            validateGrid(row.getGridRow(), row.getGridCol(), "初始物品格子越界: " + row.getItemId());

            String itemKey = row.getRoomId() + "|" + row.getItemId();
            String cellKey = row.getRoomId() + "|" + row.getGridRow() + "|" + row.getGridCol();
            if (!roomItems.add(itemKey)) {
                throw new IllegalStateException("同一房间初始物品重复: " + itemKey);
            }
            if (!roomCells.add(cellKey)) {
                throw new IllegalStateException("同一房间初始物品格子重复: " + cellKey);
            }

            room.addItem(toRuntimeItem(item));
            room.setItemPosition(row.getItemId(), new GridPosition(row.getGridRow(), row.getGridCol()));
        }
    }

    private void applyRandomItems(Map<String, Room> rooms, Map<String, WorldItem> itemTemplates, long spawnRandomSeed) {
        for (WorldRandomSpawnRule rule : randomSpawnRuleRepository.findByEnabledTrueOrderByDisplayOrderAsc()) {
            WorldItem item = validateItemExists(itemTemplates, rule.getItemId());
            List<WorldRandomSpawnCandidate> candidates = randomSpawnCandidateRepository
                    .findByRuleIdOrderByDisplayOrderAsc(rule.getRuleId());
            for (WorldRandomSpawnCandidate candidate : candidates) {
                Room room = validateRoomExists(rooms, candidate.getRoomId(), "随机物品候选房间不存在");
                validateGrid(candidate.getGridRow(), candidate.getGridCol(), "随机物品格子越界: " + candidate.getRoomId());
                if (room.hasItemAt(candidate.getGridRow(), candidate.getGridCol())) {
                    throw new IllegalStateException("随机物品候选格子与固定物品冲突: " + candidate.getRoomId());
                }
            }

            for (RandomSpawnPlanner.SpawnPlacement placement : randomSpawnPlanner.plan(spawnRandomSeed, rule, candidates)) {
                Room room = rooms.get(placement.getRoomId());
                if (room.getItem(placement.getItemId()) != null) {
                    throw new IllegalStateException("随机物品在同一房间重复: " + placement.getRoomId());
                }
                room.addItem(toRuntimeItem(item));
                room.setItemPosition(placement.getItemId(), placement.getPosition());
            }
        }
    }

    private Map<String, List<String>> buildPortalTargets(Map<String, Room> rooms,
                                                         Map<String, WorldRoom> roomConfigs) {
        Map<String, List<String>> portalTargets = new HashMap<>();
        for (WorldPortalTarget row : portalTargetRepository.findAllByOrderByPortalRoomIdAscDisplayOrderAsc()) {
            validateRoomExists(rooms, row.getPortalRoomId(), "传送门房间不存在");
            validateRoomExists(rooms, row.getTargetRoomId(), "传送目标房间不存在");
            WorldRoom portalRoom = roomConfigs.get(row.getPortalRoomId());
            if (portalRoom == null || !ROOM_TYPE_PORTAL.equals(portalRoom.getRoomType())) {
                throw new IllegalStateException("传送门源房间类型必须是portal: " + row.getPortalRoomId());
            }
            if (row.getPortalRoomId().equals(row.getTargetRoomId())) {
                throw new IllegalStateException("传送门不能指向自身: " + row.getPortalRoomId());
            }
            portalTargets.computeIfAbsent(row.getPortalRoomId(), key -> new ArrayList<>()).add(row.getTargetRoomId());
        }
        for (String portalRoomId : portalTargets.keySet()) {
            if (portalTargets.get(portalRoomId).isEmpty()) {
                throw new IllegalStateException("传送门缺少目标: " + portalRoomId);
            }
        }
        return portalTargets;
    }

    private Map<String, Map<String, Integer>> buildItemEffects(Map<String, WorldItem> itemTemplates) {
        Map<String, Map<String, Integer>> effects = new HashMap<>();
        for (WorldItemEffect effect : itemEffectRepository.findAll()) {
            validateItemExists(itemTemplates, effect.getItemId());
            if (effect.getEffectValue() <= 0) {
                throw new IllegalStateException("物品效果数值必须大于0: " + effect.getItemId());
            }
            effects.computeIfAbsent(effect.getItemId(), key -> new HashMap<>())
                    .put(effect.getEffectCode(), effect.getEffectValue());
        }
        return effects;
    }

    private void validateMagicCookieEffect(Map<String, WorldItem> itemTemplates,
                                           Map<String, Map<String, Integer>> itemEffects) {
        WorldItem magicCookie = validateItemExists(itemTemplates, "magic_cookie");
        if (!ITEM_CATEGORY_CONSUMABLE.equals(magicCookie.getItemCategory()) || !magicCookie.isUsable()) {
            throw new IllegalStateException("理智增强剂必须是可使用消耗品");
        }
        if (itemEffects.getOrDefault("magic_cookie", Map.of())
                .getOrDefault(EFFECT_MAX_WEIGHT_BONUS, 0) <= 0) {
            throw new IllegalStateException("理智增强剂缺少有效负重效果配置");
        }
    }

    private Room validateRoomExists(Map<String, Room> rooms, String roomId, String message) {
        Room room = rooms.get(roomId);
        if (room == null) {
            throw new IllegalStateException(message + ": " + roomId);
        }
        return room;
    }

    private WorldItem validateItemExists(Map<String, WorldItem> items, String itemId) {
        WorldItem item = items.get(itemId);
        if (item == null) {
            throw new IllegalStateException("物品模板不存在: " + itemId);
        }
        return item;
    }

    private void validateGrid(int row, int col, String message) {
        if (row < 0 || row > 8 || col < 0 || col > 8) {
            throw new IllegalStateException(message);
        }
    }

    private void validatePositive(int value, String message) {
        if (value <= 0) {
            throw new IllegalStateException(message);
        }
    }

    private Item toRuntimeItem(WorldItem item) {
        return new Item(item.getItemId(), item.getItemName(), item.getDescription(),
                item.getWeight(), item.getValue());
    }
}
