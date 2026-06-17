package cn.edu.whut.sept.zuul.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;

/**
 * 玩家实体测试类.
 */
public class PlayerTest {

    private Player player;
    private Item lightItem;
    private Item mediumItem;
    private Item heavyItem;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        player = new Player("测试冒险者");
        testRoom = new Room("测试房间", "test_room");
        player.setCurrentRoom(testRoom);

        lightItem = new Item("feather", "羽毛", "轻如羽毛", 1, 5);
        mediumItem = new Item("book", "书籍", "一本厚书", 5, 50);
        heavyItem = new Item("stone", "石头", "一块石头", 10, 10);
    }

    @Test
    void testPlayerCreation() {
        assertEquals("测试冒险者", player.getName());
        assertEquals(testRoom, player.getCurrentRoom());
        assertEquals(5, player.getBaseMaxWeight());
        assertEquals(5, player.getMaxWeight());
    }

    @Test
    void testSetName() {
        player.setName("新名字");
        assertEquals("新名字", player.getName());
    }

    @Test
    void testSetCurrentRoom() {
        Room newRoom = new Room("新房间", "new_room");
        player.setCurrentRoom(newRoom);
        assertEquals(newRoom, player.getCurrentRoom());
    }

    @Test
    void testAddItemWithinWeightLimit() {
        boolean result = player.addItem(lightItem);
        assertTrue(result);
        assertTrue(player.hasItem("feather"));
        assertEquals(1, player.getTotalWeight());
    }

    @Test
    void testAddMultipleItemsWithinLimit() {
        assertTrue(player.addItem(lightItem));
        assertTrue(player.addItem(mediumItem));
        assertEquals(2, player.getInventory().size());
        assertEquals(6, player.getTotalWeight());
    }

    @Test
    void testAddItemExceedingWeightLimit() {
        player.addItem(mediumItem);
        boolean result = player.addItem(new Item("heavy", "重物", "描述", 1, 0));

        assertFalse(result);
        assertEquals(1, player.getInventory().size());
    }

    @Test
    void testAddItemExceedingWeightLimitTruly() {
        player.addItem(mediumItem);
        boolean result = player.addItem(heavyItem);

        assertFalse(result);
        assertEquals(1, player.getInventory().size());
    }

    @Test
    void testAddItemExactlyAtWeightLimit() {
        player.addItem(new Item("item1", "物品1", "描述", 4, 0));
        boolean result = player.addItem(new Item("item2", "物品2", "描述", 1, 0));

        assertTrue(result);
        assertEquals(5, player.getTotalWeight());
    }

    @Test
    void testCanCarry() {
        assertTrue(player.canCarry(lightItem));
        assertTrue(player.canCarry(mediumItem));

        player.addItem(mediumItem);
        assertFalse(player.canCarry(mediumItem));
        assertTrue(player.canCarry(lightItem));
    }

    @Test
    void testRemoveItem() {
        player.addItem(lightItem);
        player.addItem(mediumItem);

        Item removed = player.removeItem("feather");
        assertNotNull(removed);
        assertEquals("feather", removed.getId());
        assertEquals(1, player.getInventory().size());
        assertEquals(5, player.getTotalWeight());
        assertFalse(player.hasItem("feather"));
    }

    @Test
    void testRemoveNonExistentItem() {
        Item removed = player.removeItem("non_existent");
        assertNull(removed);
    }

    @Test
    void testHasItem() {
        assertFalse(player.hasItem("feather"));
        player.addItem(lightItem);
        assertTrue(player.hasItem("feather"));
    }

    @Test
    void testGetTotalWeight() {
        assertEquals(0, player.getTotalWeight());
        player.addItem(lightItem);
        assertEquals(1, player.getTotalWeight());
        player.addItem(mediumItem);
        assertEquals(6, player.getTotalWeight());
    }

    @Test
    void testGetTotalValue() {
        assertEquals(0, player.getTotalValue());
        player.addItem(lightItem);
        assertEquals(5, player.getTotalValue());
        player.addItem(mediumItem);
        assertEquals(55, player.getTotalValue());
    }

    @Test
    void testIncreaseMaxWeight() {
        assertEquals(5, player.getMaxWeight());
        player.increaseMaxWeight(10);
        assertEquals(15, player.getMaxWeight());

        player.addItem(mediumItem);
        player.addItem(lightItem);
        boolean result = player.addItem(heavyItem);
        assertFalse(result);
    }

    @Test
    void testSetMaxWeight() {
        player.setMaxWeight(50);
        assertEquals(50, player.getMaxWeight());

        for (int i = 0; i < 5; i++) {
            player.addItem(mediumItem);
        }
        assertEquals(5, player.getInventory().size());
    }

    @Test
    void testMagicCookieEffect() {
        player.addItem(new Item("magic_cookie", "理智增强剂", "增加负重", 1, 0));
        assertTrue(player.hasItem("magic_cookie"));

        int originalMaxWeight = player.getMaxWeight();
        player.increaseMaxWeight(5);
        assertEquals(originalMaxWeight + 5, player.getMaxWeight());
    }

    @Test
    void testEmptyInventory() {
        assertTrue(player.getInventory().isEmpty());
        assertEquals(0, player.getTotalWeight());
        assertEquals(0, player.getTotalValue());
    }

    @Test
    void testDuplicateItems() {
        player.addItem(lightItem);
        player.addItem(lightItem);
        assertEquals(2, player.getInventory().size());
        assertEquals(2, player.getTotalWeight());
    }
}