package cn.edu.whut.sept.zuul.service;

import cn.edu.whut.sept.zuul.model.GameSave;
import cn.edu.whut.sept.zuul.model.GridPosition;
import cn.edu.whut.sept.zuul.model.Item;
import cn.edu.whut.sept.zuul.model.Player;
import cn.edu.whut.sept.zuul.model.Room;
import cn.edu.whut.sept.zuul.repository.GameSaveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 存档服务回归测试.
 */
@ExtendWith(MockitoExtension.class)
class SaveServiceTest {

    @Mock
    private GameSaveRepository gameSaveRepository;

    private SaveService saveService;
    private Game game;

    @BeforeEach
    void setUp() {
        saveService = new SaveService(gameSaveRepository);
        game = new Game();
    }

    @Test
    void testSaveAndLoadKeepsContinuousPlayerGridPosition() {
        Long userId = 1001L;
        GameSave saved = saveAndCapture(userId, 3.456, 5.789);
        when(gameSaveRepository.findByUserId(userId)).thenReturn(Optional.of(saved));

        Map<String, Object> loaded = saveService.loadGame(userId, game.getAllRooms());

        assertTrue((boolean) loaded.get("success"));
        assertEquals(3.456, (double) loaded.get("playerGridRow"), 0.001);
        assertEquals(5.789, (double) loaded.get("playerGridCol"), 0.001);
    }

    @Test
    void testLoadSupportsLegacyIntegerGridPosition() {
        Long userId = 1002L;
        GameSave save = new GameSave(userId, "outside");
        save.setPlayerGridRow(4);
        save.setPlayerGridCol(5);
        when(gameSaveRepository.findByUserId(userId)).thenReturn(Optional.of(save));

        Map<String, Object> loaded = saveService.loadGame(userId, game.getAllRooms());

        assertTrue((boolean) loaded.get("success"));
        assertEquals(4.0, (double) loaded.get("playerGridRow"), 0.001);
        assertEquals(5.0, (double) loaded.get("playerGridCol"), 0.001);
    }

    @Test
    void testSaveAndLoadKeepsInventoryWeightAndRoomHistory() {
        Long userId = 1003L;
        Player player = game.getPlayer();
        Item testItem = new Item("test_item", "测试物品", "用于存档测试", 2, 30);
        player.addItem(testItem);
        player.setMaxWeight(17);

        Room theater = game.getRooms().get("theater");
        List<Room> history = List.of(game.getRooms().get("outside"));

        GameSave saved = saveAndCapture(
                userId,
                theater,
                player,
                history,
                game.getAllRoomItems(),
                game.getAllRoomItemPositions(),
                4.0,
                4.0
        );
        when(gameSaveRepository.findByUserId(userId)).thenReturn(Optional.of(saved));

        Map<String, Object> loaded = saveService.loadGame(userId, game.getAllRooms());

        @SuppressWarnings("unchecked")
        List<Item> inventory = (List<Item>) loaded.get("inventory");
        @SuppressWarnings("unchecked")
        List<Room> loadedHistory = (List<Room>) loaded.get("roomHistory");

        assertTrue((boolean) loaded.get("success"));
        assertEquals("theater", ((Room) loaded.get("currentRoom")).getId());
        assertEquals(2, loaded.get("playerWeight"));
        assertEquals(17, loaded.get("playerMaxWeight"));
        assertEquals(1, inventory.size());
        assertEquals("test_item", inventory.get(0).getId());
        assertEquals(1, loadedHistory.size());
        assertEquals("outside", loadedHistory.get(0).getId());
    }

    @Test
    void testSaveAndLoadKeepsRoomItemPositions() {
        Long userId = 1004L;
        Room outside = game.getRooms().get("outside");
        outside.setItemPosition("orirock", new GridPosition(6, 7));

        GameSave saved = saveAndCapture(
                userId,
                outside,
                game.getPlayer(),
                game.getRoomHistory(),
                game.getAllRoomItems(),
                game.getAllRoomItemPositions(),
                4.0,
                4.0
        );
        when(gameSaveRepository.findByUserId(userId)).thenReturn(Optional.of(saved));

        Map<String, Object> loaded = saveService.loadGame(userId, game.getAllRooms());

        @SuppressWarnings("unchecked")
        Map<String, Map<String, GridPosition>> loadedPositions =
                (Map<String, Map<String, GridPosition>>) loaded.get("roomItemPositions");

        assertTrue((boolean) loaded.get("success"));
        assertTrue(loadedPositions.containsKey("outside"));
        assertEquals(6, loadedPositions.get("outside").get("orirock").getRow());
        assertEquals(7, loadedPositions.get("outside").get("orirock").getCol());
    }

    @Test
    void testLoadFailsWhenCurrentRoomIsMissing() {
        Long userId = 1005L;
        when(gameSaveRepository.findByUserId(userId))
                .thenReturn(Optional.of(new GameSave(userId, "missing_room")));

        Map<String, Object> loaded = saveService.loadGame(userId, game.getAllRooms());

        assertFalse((boolean) loaded.get("success"));
        assertEquals("存档数据损坏，无法加载", loaded.get("message"));
    }

    private GameSave saveAndCapture(Long userId, double playerGridRow, double playerGridCol) {
        return saveAndCapture(
                userId,
                game.getCurrentRoom(),
                game.getPlayer(),
                game.getRoomHistory(),
                game.getAllRoomItems(),
                game.getAllRoomItemPositions(),
                playerGridRow,
                playerGridCol
        );
    }

    private GameSave saveAndCapture(Long userId, Room currentRoom, Player player, List<Room> history,
                                    Map<String, List<Item>> roomItems,
                                    Map<String, Map<String, GridPosition>> roomItemPositions,
                                    double playerGridRow, double playerGridCol) {
        when(gameSaveRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(gameSaveRepository.save(any(GameSave.class))).thenAnswer(invocation -> invocation.getArgument(0));

        saveService.saveGame(
                userId,
                currentRoom,
                player,
                new ArrayList<>(history),
                roomItems,
                roomItemPositions,
                playerGridRow,
                playerGridCol
        );

        ArgumentCaptor<GameSave> captor = ArgumentCaptor.forClass(GameSave.class);
        org.mockito.Mockito.verify(gameSaveRepository).save(captor.capture());
        return captor.getValue();
    }
}
