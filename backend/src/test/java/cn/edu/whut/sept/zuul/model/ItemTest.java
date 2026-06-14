package cn.edu.whut.sept.zuul.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;

/**
 * 物品实体测试类.
 */
public class ItemTest {

    @Test
    void testConstructorWithParameters() {
        Item item = new Item("stone", "石头", "一块普通的石头", 2, 5);

        assertEquals("stone", item.getId());
        assertEquals("石头", item.getName());
        assertEquals("一块普通的石头", item.getDescription());
        assertEquals(2, item.getWeight());
        assertEquals(5, item.getValue());
    }

    @Test
    void testNoArgsConstructor() {
        Item item = new Item();
        assertNotNull(item);
    }

    @Test
    void testSettersAndGetters() {
        Item item = new Item();

        Field idField = null;
        Field nameField = null;
        Field descField = null;
        Field weightField = null;
        Field valueField = null;

        try {
            idField = Item.class.getDeclaredField("id");
            nameField = Item.class.getDeclaredField("name");
            descField = Item.class.getDeclaredField("description");
            weightField = Item.class.getDeclaredField("weight");
            valueField = Item.class.getDeclaredField("value");

            idField.setAccessible(true);
            nameField.setAccessible(true);
            descField.setAccessible(true);
            weightField.setAccessible(true);
            valueField.setAccessible(true);

            idField.set(item, "test_id");
            nameField.set(item, "测试物品");
            descField.set(item, "测试描述");
            weightField.set(item, 10);
            valueField.set(item, 100);

            assertEquals("test_id", item.getId());
            assertEquals("测试物品", item.getName());
            assertEquals("测试描述", item.getDescription());
            assertEquals(10, item.getWeight());
            assertEquals(100, item.getValue());
        } catch (Exception e) {
            fail("反射测试失败: " + e.getMessage());
        }
    }

    @Test
    void testWeightBoundary() {
        Item lightItem = new Item("feather", "羽毛", "轻如羽毛", 0, 1);
        assertEquals(0, lightItem.getWeight());

        Item heavyItem = new Item("iron", "铁块", "非常重", Integer.MAX_VALUE, 1000);
        assertEquals(Integer.MAX_VALUE, heavyItem.getWeight());
    }

    @Test
    void testValueBoundary() {
        Item freeItem = new Item("air", "空气", "免费的空气", 0, 0);
        assertEquals(0, freeItem.getValue());

        Item valuableItem = new Item("gold", "黄金", "贵重的黄金", 5, Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, valuableItem.getValue());
    }

    @Test
    void testMagicCookie() {
        Item cookie = new Item("magic_cookie", "理智增强剂", "散发神奇香气的饼干，吃了可以增加负重", 1, 0);
        assertEquals("magic_cookie", cookie.getId());
        assertEquals("理智增强剂", cookie.getName());
        assertEquals(1, cookie.getWeight());
        assertEquals(0, cookie.getValue());
    }
}