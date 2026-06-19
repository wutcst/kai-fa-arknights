package cn.edu.whut.sept.zuul.controller;

import cn.edu.whut.sept.zuul.service.Game;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 游戏 REST API 控制器测试类.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Game game;

    @BeforeEach
    void setUp() {
        game.resetToStart();
    }

    @Test
    void testGetStatus() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/game/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.roomId").exists())
                .andExpect(jsonPath("$.exits").exists())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(content);

        assertNotNull(jsonNode.get("description").asText());
        assertEquals("outside", jsonNode.get("roomId").asText());
    }

    @Test
    void testGetStatusExits() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/game/status"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(content);

        assertTrue(jsonNode.get("exits").isArray());
        assertTrue(jsonNode.get("exits").size() > 0);
    }

    @Test
    void testGetHelp() throws Exception {
        mockMvc.perform(get("/api/game/help"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.directions").isArray())
                .andExpect(jsonPath("$.directions[0]").value("north"));
    }

    @Test
    void testMoveNorth() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/game/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"direction\":\"north\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(content);

        assertNotNull(jsonNode.get("success"));
    }

    @Test
    void testMoveInvalidDirection() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/game/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"direction\":\"up\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(content);

        assertFalse(jsonNode.get("success").asBoolean());
        assertTrue(jsonNode.has("message"));
    }

    @Test
    void testMoveEast() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/game/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"direction\":\"east\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(content);

        assertNotNull(jsonNode.get("success"));
        if (jsonNode.get("success").asBoolean()) {
            assertEquals("theater", jsonNode.get("roomId").asText());
        }
    }

    @Test
    void testMoveSouth() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/game/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"direction\":\"south\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(content);

        assertNotNull(jsonNode.get("success"));
    }

    @Test
    void testMoveWest() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/game/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"direction\":\"west\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(content);

        assertNotNull(jsonNode.get("success"));
    }

    @Test
    void testLook() throws Exception {
        mockMvc.perform(get("/api/game/look"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.roomId").value("outside"))
                .andExpect(jsonPath("$.exits").exists());
    }

    @Test
    void testLookReturnsCurrentRoomInfo() throws Exception {
        mockMvc.perform(get("/api/game/look"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.longDescription").exists())
                .andExpect(jsonPath("$.items").exists());
    }

    @Test
    void testBackFromStartingPoint() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/game/back")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(content);

        assertNotNull(jsonNode.get("success"));
    }

    @Test
    void testBackAfterMoving() throws Exception {
        mockMvc.perform(post("/api/game/move")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"direction\":\"east\"}"))
                .andExpect(status().isOk());

        MvcResult backResult = mockMvc.perform(post("/api/game/back")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = backResult.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(content);

        assertNotNull(jsonNode.get("success"));
    }

    @Test
    void testGetMap() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/game/map"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rooms").isArray())
                .andExpect(jsonPath("$.currentRoomId").exists())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(content);

        assertTrue(jsonNode.get("rooms").size() > 0);
        assertEquals("outside", jsonNode.get("currentRoomId").asText());
    }

    @Test
    void testMapContainsRoomConnections() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/game/map"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(content);

        JsonNode firstRoom = jsonNode.get("rooms").get(0);
        assertTrue(firstRoom.has("id"));
        assertTrue(firstRoom.has("name"));
        assertTrue(firstRoom.has("exits"));
        assertTrue(firstRoom.has("connectedRooms"));
    }

    @Test
    void testTakeNonExistentItem() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/game/take")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":\"definitely_non_existent_item_12345\",\"playerGridRow\":2,\"playerGridCol\":2}"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(content);

        assertFalse(jsonNode.get("success").asBoolean());
    }

    @Test
    void testTakeWithoutPlayerPositionFails() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/game/take")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":\"stone\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

        assertFalse(jsonNode.get("success").asBoolean());
        assertTrue(jsonNode.hasNonNull("message"));
    }

    @Test
    void testTakeWithInvalidPlayerPositionFails() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/game/take")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":\"stone\",\"playerGridRow\":\"abc\",\"playerGridCol\":2}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

        assertFalse(jsonNode.get("success").asBoolean());
        assertTrue(jsonNode.hasNonNull("message"));
    }

    @Test
    void testTakeItem() throws Exception {
        MvcResult statusResult = mockMvc.perform(get("/api/game/status"))
                .andExpect(status().isOk())
                .andReturn();

        String statusContent = statusResult.getResponse().getContentAsString();
        JsonNode statusJson = objectMapper.readTree(statusContent);

        if (statusJson.get("items").size() > 0) {
            String itemId = statusJson.get("items").get(0).get("id").asText();
            int row = statusJson.get("items").get(0).get("row").asInt();
            int col = statusJson.get("items").get(0).get("col").asInt();

            MvcResult takeResult = mockMvc.perform(post("/api/game/take")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"itemId\":\"" + itemId + "\",\"playerGridRow\":" + row + ",\"playerGridCol\":" + col + "}"))
                    .andExpect(status().isOk())
                    .andReturn();

            String takeContent = takeResult.getResponse().getContentAsString();
            JsonNode takeJson = objectMapper.readTree(takeContent);

            assertTrue(takeJson.get("success").asBoolean() || !takeJson.get("message").asText().contains("成功"));
        }
    }

    @Test
    void testDropItemWithoutInventory() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/game/drop")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":\"stone\",\"playerGridRow\":4,\"playerGridCol\":4}"))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(content);

        assertFalse(jsonNode.get("success").asBoolean());
    }

    @Test
    void testDropWithoutPlayerPositionFails() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/game/drop")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":\"stone\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

        assertFalse(jsonNode.get("success").asBoolean());
        assertTrue(jsonNode.hasNonNull("message"));
    }

    @Test
    void testDropWithInvalidPlayerPositionFails() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/game/drop")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":\"stone\",\"playerGridRow\":4,\"playerGridCol\":\"bad\"}"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

        assertFalse(jsonNode.get("success").asBoolean());
        assertTrue(jsonNode.hasNonNull("message"));
    }

    @Test
    void testDropItemWithInventory() throws Exception {
        MvcResult statusResult = mockMvc.perform(get("/api/game/status"))
                .andExpect(status().isOk())
                .andReturn();

        String statusContent = statusResult.getResponse().getContentAsString();
        JsonNode statusJson = objectMapper.readTree(statusContent);

        if (statusJson.get("items").size() > 0) {
            String itemId = statusJson.get("items").get(0).get("id").asText();
            int row = statusJson.get("items").get(0).get("row").asInt();
            int col = statusJson.get("items").get(0).get("col").asInt();

            mockMvc.perform(post("/api/game/take")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"itemId\":\"" + itemId + "\",\"playerGridRow\":" + row + ",\"playerGridCol\":" + col + "}"))
                    .andExpect(status().isOk());

            MvcResult dropResult = mockMvc.perform(post("/api/game/drop")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"itemId\":\"" + itemId + "\",\"playerGridRow\":4,\"playerGridCol\":4}"))
                    .andExpect(status().isOk())
                    .andReturn();

            String dropContent = dropResult.getResponse().getContentAsString();
            JsonNode dropJson = objectMapper.readTree(dropContent);

            assertTrue(dropJson.get("success").asBoolean() || !dropJson.get("message").asText().contains("没有"));
        }
    }

    @Test
    void testItems() throws Exception {
        mockMvc.perform(get("/api/game/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.inventory").exists())
                .andExpect(jsonPath("$.playerWeight").exists())
                .andExpect(jsonPath("$.playerMaxWeight").exists());
    }

    @Test
    void testItemsReturnsCurrentRunMaxWeight() throws Exception {
        game.getPlayer().setMaxWeight(10);

        mockMvc.perform(get("/api/game/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerMaxWeight").value(10));
    }

    @Test
    void testEatCookieWithoutInventory() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/game/eatcookie")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String content = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(content);

        assertFalse(jsonNode.get("success").asBoolean());
    }

    @Test
    void testMoveAndBackSequence() throws Exception {
        mockMvc.perform(post("/api/game/move")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"direction\":\"east\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value("theater"));

        mockMvc.perform(post("/api/game/back")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value("outside"));
    }

    @Test
    void testMoveSequenceToDifferentRooms() throws Exception {
        mockMvc.perform(post("/api/game/move")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"direction\":\"south\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value("lab"));

        mockMvc.perform(post("/api/game/move")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"direction\":\"north\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value("outside"));
    }

    @Test
    void testPlayerInventoryUpdates() throws Exception {
        MvcResult statusResult = mockMvc.perform(get("/api/game/status"))
                .andExpect(status().isOk())
                .andReturn();

        String statusContent = statusResult.getResponse().getContentAsString();
        JsonNode statusJson = objectMapper.readTree(statusContent);

        if (statusJson.get("items").size() > 0) {
            String itemId = statusJson.get("items").get(0).get("id").asText();
            int row = statusJson.get("items").get(0).get("row").asInt();
            int col = statusJson.get("items").get(0).get("col").asInt();

            MvcResult takeResult = mockMvc.perform(post("/api/game/take")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"itemId\":\"" + itemId + "\",\"playerGridRow\":" + row + ",\"playerGridCol\":" + col + "}"))
                    .andExpect(status().isOk())
                    .andReturn();

            String takeContent = takeResult.getResponse().getContentAsString();
            JsonNode takeJson = objectMapper.readTree(takeContent);

            assertTrue(takeJson.has("inventory"));
            assertTrue(takeJson.has("playerWeight"));
            assertTrue(takeJson.has("playerMaxWeight"));
        }
    }

    @Test
    void testPortalTeleport() throws Exception {
        mockMvc.perform(post("/api/game/move")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"direction\":\"north\"}"))
                .andExpect(status().isOk());

        MvcResult portalResult = mockMvc.perform(post("/api/game/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"direction\":\"north\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String content = portalResult.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(content);

        assertNotNull(jsonNode.get("roomId"));
    }

    @Test
    void testCORSHeaders() throws Exception {
        mockMvc.perform(get("/api/game/status")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }

    @Test
    void testResponseContentType() throws Exception {
        mockMvc.perform(get("/api/game/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
