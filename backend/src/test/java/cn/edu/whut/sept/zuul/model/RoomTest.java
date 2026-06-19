package cn.edu.whut.sept.zuul.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.ArrayList;

/**
 * 房间实体测试类.
 */
public class RoomTest {

    private Room outside;
    private Room theater;
    private Room lab;
    private Item stone;
    private Item book;

    @BeforeEach
    void setUp() {
        outside = new Room("outside the main entrance of the university", "outside");
        theater = new Room("in a lecture theater", "theater");
        lab = new Room("in a computing lab", "lab");

        stone = new Item("stone", "石头", "一块普通的石头", 2, 5);
        book = new Item("book", "书籍", "一本计算机教材", 5, 50);
    }

    @Test
    void testRoomCreation() {
        assertEquals("outside the main entrance of the university", outside.getShortDescription());
        assertEquals("outside", outside.getId());
        assertEquals("outside", outside.getZhName());
    }

    @Test
    void testRoomChineseNameComesFromConstructor() {
        Room portal = new Room("in a portal", "portal", "机密传送门");

        assertEquals("机密传送门", portal.getZhName());
    }

    @Test
    void testRoomChineseNameFallbackUsesIdOnly() {
        Room unknown = new Room("unknown room", "unknown_id");
        assertEquals("unknown_id", unknown.getZhName());
    }

    @Test
    void testSetExit() {
        outside.setExit("east", theater);
        outside.setExit("south", lab);

        Room eastRoom = outside.getExit("east");
        Room southRoom = outside.getExit("south");

        assertEquals(theater, eastRoom);
        assertEquals(lab, southRoom);
    }

    @Test
    void testGetExitNull() {
        Room result = outside.getExit("north");
        assertNull(result);
    }

    @Test
    void testGetExits() {
        outside.setExit("east", theater);
        outside.setExit("south", lab);
        outside.setExit("west", null);

        assertTrue(outside.getExits().contains("east"));
        assertTrue(outside.getExits().contains("south"));
        assertTrue(outside.getExits().contains("west"));
    }

    @Test
    void testGetLongDescription() {
        outside.setExit("east", theater);
        String longDesc = outside.getLongDescription();

        assertTrue(longDesc.contains("outside the main entrance of the university"));
        assertTrue(longDesc.contains("Exits:"));
        assertTrue(longDesc.contains("east"));
    }

    @Test
    void testAddItem() {
        outside.addItem(stone);
        outside.addItem(book);

        List<Item> items = outside.getItems();
        assertEquals(2, items.size());
    }

    @Test
    void testRemoveItem() {
        outside.addItem(stone);
        outside.addItem(book);

        outside.removeItem("stone");

        List<Item> items = outside.getItems();
        assertEquals(1, items.size());
        assertFalse(items.contains(stone));
    }

    @Test
    void testRemoveNonExistentItem() {
        outside.addItem(stone);
        outside.removeItem("non_existent");
        assertEquals(1, outside.getItems().size());
    }

    @Test
    void testGetItem() {
        outside.addItem(stone);
        outside.addItem(book);

        Item found = outside.getItem("book");
        assertNotNull(found);
        assertEquals("book", found.getId());

        Item notFound = outside.getItem("non_existent");
        assertNull(notFound);
    }

    @Test
    void testGetTotalWeight() {
        assertEquals(0, outside.getTotalWeight());

        outside.addItem(stone);
        assertEquals(2, outside.getTotalWeight());

        outside.addItem(book);
        assertEquals(7, outside.getTotalWeight());
    }

    @Test
    void testGetTotalValue() {
        assertEquals(0, outside.getTotalValue());

        outside.addItem(stone);
        assertEquals(5, outside.getTotalValue());

        outside.addItem(book);
        assertEquals(55, outside.getTotalValue());
    }

    @Test
    void testSetItems() {
        List<Item> items = new ArrayList<>();
        items.add(stone);
        items.add(book);

        outside.setItems(items);

        assertEquals(2, outside.getItems().size());
    }

    @Test
    void testEmptyRoom() {
        assertTrue(outside.getItems().isEmpty());
        assertEquals(0, outside.getTotalWeight());
        assertEquals(0, outside.getTotalValue());
    }

    @Test
    void testMultipleExits() {
        Room room1 = new Room("room1", "room1");
        Room room2 = new Room("room2", "room2");
        Room room3 = new Room("room3", "room3");
        Room room4 = new Room("room4", "room4");

        outside.setExit("north", room1);
        outside.setExit("east", room2);
        outside.setExit("south", room3);
        outside.setExit("west", room4);

        assertEquals(4, outside.getExits().size());
    }

    @Test
    void testDuplicateItems() {
        outside.addItem(stone);
        outside.addItem(stone);
        assertEquals(2, outside.getItems().size());
        assertEquals(4, outside.getTotalWeight());
    }

    @Test
    void testClassroomNames() {
        Room classroom101 = new Room("classroom 101", "theater_classroom_101", "基础训练室A");
        Room stairway = new Room("stairway", "theater_stairway_1f", "设施东侧通道");
        Room office = new Room("teacher office", "theater_office", "人事档案室");

        assertEquals("基础训练室A", classroom101.getZhName());
        assertEquals("设施东侧通道", stairway.getZhName());
        assertEquals("人事档案室", office.getZhName());
    }
}
