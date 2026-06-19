package cn.edu.whut.sept.zuul.service.world;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class WorldMapLayoutServiceTest {
    @Autowired
    private WorldMapLayoutService worldMapLayoutService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void loadExternalViewSnapshot() {
        MapLayoutSnapshot snapshot = worldMapLayoutService.loadLayoutSnapshot("outside");

        assertEquals("external", snapshot.getCurrentViewType());
        assertEquals("0 0 1100 1050", snapshot.getViewBox());
        assertTrue(snapshot.getMapViews().stream().anyMatch(view -> "external".equals(view.getViewType())));
        assertTrue(snapshot.getRoomLayoutsByRoomId().containsKey("outside"));
    }

    @Test
    void resolveInternalPrimaryView() {
        MapLayoutSnapshot snapshot = worldMapLayoutService.loadLayoutSnapshot("theater_lobby");

        assertEquals("internal", snapshot.getCurrentViewType());
        assertEquals("100 30 700 750", snapshot.getViewBox());
    }

    @Test
    void theaterHasDualLayoutsAndExternalPrimary() {
        MapLayoutSnapshot snapshot = worldMapLayoutService.loadLayoutSnapshot("theater");
        List<RoomLayoutInfo> theaterLayouts = snapshot.getRoomLayoutsByRoomId().get("theater");

        assertEquals("external", snapshot.getCurrentViewType());
        assertEquals(2, theaterLayouts.size());
        assertEquals(1, theaterLayouts.stream().filter(RoomLayoutInfo::isPrimaryView).count());
        assertTrue(theaterLayouts.stream().anyMatch(layout -> "internal".equals(layout.getViewType())));
        assertTrue(theaterLayouts.stream().anyMatch(layout -> "external".equals(layout.getViewType())));
    }

    @Test
    void allRoomsHaveAtLeastOneLayout() {
        MapLayoutSnapshot snapshot = worldMapLayoutService.loadLayoutSnapshot("outside");
        Integer roomCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM world_rooms", Integer.class);

        assertEquals(roomCount, snapshot.getRoomLayoutsByRoomId().size());
        for (Map.Entry<String, List<RoomLayoutInfo>> entry : snapshot.getRoomLayoutsByRoomId().entrySet()) {
            assertTrue(entry.getValue().size() >= 1, "missing layout for " + entry.getKey());
        }
    }

    @Test
    void missingRoomLayoutFailsFast() {
        jdbcTemplate.update("DELETE FROM world_room_layouts WHERE room_id = 'dormitory'");
        try {
            assertThrows(IllegalStateException.class, () -> worldMapLayoutService.loadLayoutSnapshot("outside"));
        } finally {
            jdbcTemplate.update("""
                    INSERT INTO world_room_layouts (view_type, room_id, x, y, primary_view, display_order)
                    VALUES ('external','dormitory',500,800,true,12)
                    """);
        }
    }

    @Test
    void multiplePrimaryLayoutsFailFast() {
        jdbcTemplate.update("""
                UPDATE world_room_layouts
                SET primary_view = true
                WHERE room_id = 'theater' AND view_type = 'internal'
                """);
        try {
            assertThrows(IllegalStateException.class, () -> worldMapLayoutService.loadLayoutSnapshot("theater"));
        } finally {
            jdbcTemplate.update("""
                    UPDATE world_room_layouts
                    SET primary_view = false
                    WHERE room_id = 'theater' AND view_type = 'internal'
                    """);
        }
    }
}
