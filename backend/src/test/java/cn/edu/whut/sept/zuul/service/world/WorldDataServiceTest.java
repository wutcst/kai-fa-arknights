package cn.edu.whut.sept.zuul.service.world;

import cn.edu.whut.sept.zuul.model.GridPosition;
import cn.edu.whut.sept.zuul.model.Item;
import cn.edu.whut.sept.zuul.model.Room;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class WorldDataServiceTest {
    @Autowired
    private WorldDataService worldDataService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void loadWorldUsesDatabaseRoomConfig() {
        LoadedWorld world = worldDataService.loadWorld();

        assertEquals("outside", world.getStartRoomId());
        assertEquals(5, world.getDefaultMaxWeight());
        assertEquals(4, world.getDefaultPlayerGridRow());
        assertEquals(4, world.getDefaultPlayerGridCol());
        assertEquals(20260620L, world.getSpawnRandomSeed());
        assertEquals(20260621L, world.getPortalRandomSeed());
        assertEquals(24, world.getRooms().size());
        assertEquals("罗德岛入口", world.getRooms().get("outside").getZhName());
        assertEquals("theater", world.getRooms().get("outside").getExit("east").getId());
    }

    @Test
    void loadWorldGeneratesStableMagicCookiesWithoutGridConflict() {
        LoadedWorld first = worldDataService.loadWorld();
        LoadedWorld second = worldDataService.loadWorld();

        assertEquals(cookieRooms(first.getRooms()), cookieRooms(second.getRooms()));
        int cookieCount = cookieRooms(first.getRooms()).size();
        assertTrue(cookieCount >= 5);
        assertTrue(cookieCount <= 10);

        for (Room room : first.getRooms().values()) {
            Set<String> occupied = new HashSet<>();
            int cookieInRoom = 0;
            for (Item item : room.getItems()) {
                GridPosition position = room.getItemPosition(item.getId());
                assertNotNull(position);
                assertTrue(occupied.add(position.getRow() + "-" + position.getCol()));
                if ("magic_cookie".equals(item.getId())) {
                    cookieInRoom++;
                }
            }
            assertTrue(cookieInRoom <= 1);
        }
    }

    @Test
    void loadWorldUsesDatabasePortalTargets() {
        LoadedWorld world = worldDataService.loadWorld();

        assertTrue(world.isPortalRoom("portal"));
        assertEquals(15, world.getPortalTargetRoomIds("portal").size());
        assertTrue(world.getPortalTargetRoomIds("portal").contains("outside"));
        assertFalse(world.getPortalTargetRoomIds("portal").contains("portal"));
    }

    @Test
    void magicCookieEffectComesFromDatabase() {
        LoadedWorld world = worldDataService.loadWorld();

        assertEquals(5, world.getItemEffectValue("magic_cookie", WorldDataService.EFFECT_MAX_WEIGHT_BONUS));
        Map<String, Object> magicCookie = jdbcTemplate.queryForMap(
                "select item_category, usable from world_items where item_id = 'magic_cookie'"
        );
        assertEquals("consumable", magicCookie.get("item_category"));
        assertEquals(Boolean.TRUE, magicCookie.get("usable"));
    }

    @Test
    void portalTargetsRequirePortalRoomType() {
        jdbcTemplate.update("update world_rooms set room_type = 'normal' where room_id = 'portal'");
        try {
            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> worldDataService.loadWorld());
            assertTrue(ex.getMessage().contains("传送门源房间类型必须是portal"));
        } finally {
            jdbcTemplate.update("update world_rooms set room_type = 'portal' where room_id = 'portal'");
        }
    }

    private Set<String> cookieRooms(Map<String, Room> rooms) {
        Set<String> result = new HashSet<>();
        for (Room room : rooms.values()) {
            if (room.getItem("magic_cookie") != null) {
                result.add(room.getId());
            }
        }
        return result;
    }
}
