package cn.edu.whut.sept.zuul.service;

import cn.edu.whut.sept.zuul.model.Room;
import cn.edu.whut.sept.zuul.model.Item;
import cn.edu.whut.sept.zuul.model.Player;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

/**
 * 游戏核心服务测试类.
 */
public class GameTest {

    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
    }

    @Test
    void testGameInitialization() {
        Room currentRoom = game.getCurrentRoom();
        assertNotNull(currentRoom);
        assertEquals("outside", currentRoom.getId());
    }

    @Test
    void testRoomsCreated() {
        Map<String, Room> rooms = game.getRooms();
        assertNotNull(rooms);
        assertFalse(rooms.isEmpty());

        assertTrue(rooms.containsKey("outside"));
        assertTrue(rooms.containsKey("theater"));
        assertTrue(rooms.containsKey("lab"));
        assertTrue(rooms.containsKey("pub"));
        assertTrue(rooms.containsKey("library"));
        assertTrue(rooms.containsKey("gym"));
        assertTrue(rooms.containsKey("cafeteria"));
        assertTrue(rooms.containsKey("garden"));
        assertTrue(rooms.containsKey("bookstore"));
        assertTrue(rooms.containsKey("dormitory"));
        assertTrue(rooms.containsKey("portal"));
    }

    @Test
    void testTheaterRoomsCreated() {
        Map<String, Room> rooms = game.getRooms();

        assertTrue(rooms.containsKey("theater_lobby"));
        assertTrue(rooms.containsKey("theater_classroom_101"));
        assertTrue(rooms.containsKey("theater_classroom_102"));
        assertTrue(rooms.containsKey("theater_stairway_1f"));
        assertTrue(rooms.containsKey("theater_classroom_201"));
        assertTrue(rooms.containsKey("theater_classroom_202"));
        assertTrue(rooms.containsKey("theater_office"));
        assertTrue(rooms.containsKey("theater_stairway_2f"));
        assertTrue(rooms.containsKey("theater_classroom_301"));
        assertTrue(rooms.containsKey("theater_classroom_302"));
        assertTrue(rooms.containsKey("theater_lab"));
        assertTrue(rooms.containsKey("theater_stairway_3f"));
    }

    @Test
    void testPlayerCreated() {
        Player player = game.getPlayer();
        assertNotNull(player);
        assertEquals("冒险者", player.getName());
    }

    @Test
    void testRoomExits() {
        Room outside = game.getRooms().get("outside");

        assertNotNull(outside.getExit("east"));
        assertNotNull(outside.getExit("south"));
        assertNotNull(outside.getExit("west"));
        assertNotNull(outside.getExit("north"));
    }

    @Test
    void testRoomItemsExist() {
        Map<String, List<Item>> allItems = game.getAllRoomItems();
        assertNotNull(allItems);
        assertFalse(allItems.isEmpty());
    }

    @Test
    void testOutsideRoomHasItems() {
        Room outside = game.getRooms().get("outside");
        List<Item> items = outside.getItems();
        assertFalse(items.isEmpty());
    }

    @Test
    void testPortalRoomExists() {
        Room portal = game.getRooms().get("portal");
        assertNotNull(portal);
        assertEquals("传送门", portal.getZhName());
    }

    @Test
    void testGoRoomMovement() {
        Room theater = game.getRooms().get("theater");
        game.setCurrentRoom(theater);

        assertEquals(theater, game.getCurrentRoom());
        assertEquals("theater", game.getCurrentRoom().getId());
    }

    @Test
    void testBackRoomWithoutHistory() {
        Room initialRoom = game.getCurrentRoom();
        Room backRoom = game.getBackRoom();

        if (backRoom == null) {
            assertEquals(initialRoom, game.getCurrentRoom());
        } else {
            assertNotEquals(initialRoom, backRoom);
        }
    }

    @Test
    void testBackRoomWithHistory() {
        Room theater = game.getRooms().get("theater");
        game.setCurrentRoom(theater);

        Room lab = game.getRooms().get("lab");
        game.setCurrentRoom(lab);

        Room backRoom = game.getBackRoom();
        assertNotNull(backRoom);
        assertEquals("theater", backRoom.getId());
    }

    @Test
    void testCanGoBack() {
        assertFalse(game.canGoBack());

        Room theater = game.getRooms().get("theater");
        game.setCurrentRoom(theater);

        Room lab = game.getRooms().get("lab");
        game.setCurrentRoom(lab);

        assertTrue(game.canGoBack());
    }

    @Test
    void testTakeItemFromRoom() {
        Room outside = game.getRooms().get("outside");
        Item stone = outside.getItem("stone");

        if (stone != null) {
            String result = game.takeItem("stone");
            assertNotNull(result);

            assertFalse(outside.getItems().contains(stone));
            assertTrue(game.getPlayer().hasItem("stone"));
        }
    }

    @Test
    void testTakeNonExistentItem() {
        String result = game.takeItem("non_existent_item");
        assertNotNull(result);
        assertTrue(result.contains("没有这个物品"));
    }

    @Test
    void testTakeItemTooHeavy() {
        Player player = game.getPlayer();
        player.setMaxWeight(1);

        Room outside = game.getRooms().get("outside");
        Item book = outside.getItem("book");

        if (book != null && book.getWeight() > 1) {
            String result = game.takeItem("book");
            assertNotNull(result);
            assertTrue(result.contains("太重了") || result.contains("无法携带"));
        }
    }

    @Test
    void testDropItem() {
        Room outside = game.getRooms().get("outside");
        Item stone = outside.getItem("stone");

        if (stone != null) {
            game.takeItem("stone");
            assertTrue(game.getPlayer().hasItem("stone"));

            String result = game.dropItem("stone");
            assertNotNull(result);

            assertFalse(game.getPlayer().hasItem("stone"));
            assertTrue(outside.getItems().contains(stone));
        }
    }

    @Test
    void testDropAllItems() {
        Room outside = game.getRooms().get("outside");
        Item stone = outside.getItem("stone");

        if (stone != null) {
            game.takeItem("stone");

            String result = game.dropItem("all");
            assertNotNull(result);

            assertTrue(game.getPlayer().getInventory().isEmpty());
        }
    }

    @Test
    void testDropNonExistentItem() {
        String result = game.dropItem("non_existent_item");
        assertNotNull(result);
        assertTrue(result.contains("没有这个物品"));
    }

    @Test
    void testEatCookie() {
        Room outside = game.getRooms().get("outside");
        Item cookie = outside.getItem("magic_cookie");

        if (cookie != null) {
            game.takeItem("magic_cookie");
            assertTrue(game.getPlayer().hasItem("magic_cookie"));

            int originalMaxWeight = game.getPlayer().getMaxWeight();
            String result = game.eatCookie();

            assertFalse(game.getPlayer().hasItem("magic_cookie"));
            assertEquals(originalMaxWeight + 5, game.getPlayer().getMaxWeight());
        }
    }

    @Test
    void testEatCookieWhenNotHave() {
        String result = game.eatCookie();
        assertNotNull(result);
        assertTrue(result.contains("没有魔法饼干"));
    }

    @Test
    void testTeleportFromPortal() {
        Room portal = game.getRooms().get("portal");
        Room beforeTeleport = game.getCurrentRoom();

        game.setCurrentRoom(portal);

        if (game.isJustTeleported()) {
            assertNotEquals(portal, game.getCurrentRoom());
            assertNotEquals(beforeTeleport, game.getCurrentRoom());
            assertNotNull(game.getTeleportedFrom());
        }
    }

    @Test
    void testRoomHistoryTracking() {
        game.setCurrentRoom(game.getRooms().get("theater"));
        assertEquals(1, game.getRoomHistory().size());

        game.setCurrentRoom(game.getRooms().get("lab"));
        assertEquals(2, game.getRoomHistory().size());
    }

    @Test
    void testRoomHistoryClearedAfterTeleport() {
        game.setCurrentRoom(game.getRooms().get("theater"));
        game.setCurrentRoom(game.getRooms().get("lab"));
        assertFalse(game.getRoomHistory().isEmpty());

        game.setCurrentRoom(game.getRooms().get("portal"));
        assertTrue(game.isJustTeleported());
        assertNotEquals("portal", game.getCurrentRoom().getId());
        assertEquals(1, game.getRoomHistory().size());
        assertEquals(game.getCurrentRoom(), game.getRoomHistory().get(0));
    }

    @Test
    void testItemsInfo() {
        String itemsInfo = game.getItemsInfo();
        assertNotNull(itemsInfo);
        assertTrue(itemsInfo.contains("房间物品") || itemsInfo.contains("随身物品"));
    }

    @Test
    void testResetToStart() {
        Room theater = game.getRooms().get("theater");
        game.setCurrentRoom(theater);

        game.getPlayer().setMaxWeight(100);

        game.resetToStart();

        assertEquals("outside", game.getCurrentRoom().getId());
        assertEquals(20, game.getPlayer().getMaxWeight());
        assertTrue(game.getPlayer().getInventory().isEmpty());
    }

    @Test
    void testSetPlayerInventory() {
        Player player = game.getPlayer();
        player.getInventory().clear();

        Item item = new Item("test", "测试物品", "测试", 1, 10);
        player.addItem(item);

        assertEquals(1, player.getInventory().size());

        game.setPlayerInventory(java.util.Collections.emptyList());
        assertTrue(player.getInventory().isEmpty());
    }

    @Test
    void testSetMaxWeight() {
        Player player = game.getPlayer();
        assertEquals(20, player.getMaxWeight());

        game.setMaxWeight(50);
        assertEquals(50, player.getMaxWeight());
    }

    @Test
    void testCurrentUserId() {
        assertNull(game.getCurrentUserId());

        game.setCurrentUserId(123L);
        assertEquals(Long.valueOf(123L), game.getCurrentUserId());
    }

    @Test
    void testGetAllRooms() {
        Map<String, Room> allRooms = game.getAllRooms();
        assertNotNull(allRooms);
        assertEquals(game.getRooms().size(), allRooms.size());
    }

    @Test
    void testMultipleBackOperations() {
        game.setCurrentRoom(game.getRooms().get("theater"));
        game.setCurrentRoom(game.getRooms().get("lab"));
        game.setCurrentRoom(game.getRooms().get("pub"));

        assertEquals(3, game.getRoomHistory().size());

        game.getBackRoom();
        assertEquals("lab", game.getCurrentRoom().getId());

        game.getBackRoom();
        assertEquals("theater", game.getCurrentRoom().getId());

        game.getBackRoom();
        if (game.getRoomHistory().isEmpty()) {
            assertEquals("outside", game.getCurrentRoom().getId());
        }
    }
}
