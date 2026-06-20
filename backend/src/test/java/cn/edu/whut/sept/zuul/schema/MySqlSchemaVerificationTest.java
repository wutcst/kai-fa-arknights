package cn.edu.whut.sept.zuul.schema;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
class MySqlSchemaVerificationTest {
    @Container
    private static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("arknights_db")
            .withUsername("test")
            .withPassword("test");

    @BeforeAll
    static void initializeSchema() throws Exception {
        String schemaSql = Files.readString(
                        Path.of(System.getProperty("user.dir"), "sql", "ability_schema.sql"),
                        StandardCharsets.UTF_8)
                .replaceFirst("(?is)CREATE DATABASE IF NOT EXISTS .*?;", "")
                .replaceFirst("(?is)USE arknights_db;", "");
        try (Connection connection = DriverManager.getConnection(
                MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword())) {
            ScriptUtils.executeSqlScript(connection, new EncodedResource(
                    new ByteArrayResource(schemaSql.getBytes(StandardCharsets.UTF_8)), "UTF-8"));
        }
    }

    @Test
    void schemaContainsExpectedWorldTablesAndConstraints() throws SQLException {
        Set<String> tables = queryStringSet("""
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                """);

        assertTrue(tables.containsAll(List.of(
                "world_areas",
                "world_rooms",
                "world_directions",
                "world_map_views",
                "world_room_layouts",
                "world_room_exits",
                "world_items",
                "world_item_effects",
                "world_room_initial_items",
                "world_random_spawn_rules",
                "world_random_spawn_candidates",
                "world_portal_targets",
                "world_game_config",
                "game_saves",
                "users",
                "ability_config",
                "user_abilities"
        )));

        Set<String> constraints = queryStringSet("""
                SELECT constraint_name
                FROM information_schema.table_constraints
                WHERE table_schema = DATABASE()
                """);

        assertTrue(constraints.containsAll(List.of(
                "fk_world_rooms_area",
                "fk_world_room_exits_source",
                "fk_world_room_exits_target",
                "fk_world_room_exits_direction",
                "fk_world_room_layouts_view",
                "fk_world_room_layouts_room",
                "uk_world_map_views_order",
                "uk_world_room_layout_view_room",
                "uk_world_room_layout_view_order",
                "chk_world_room_layouts_x",
                "chk_world_room_layouts_y",
                "fk_world_initial_items_room",
                "fk_world_initial_items_item",
                "fk_world_spawn_rules_item",
                "fk_world_spawn_candidates_rule",
                "fk_world_spawn_candidates_room",
                "uk_world_spawn_candidate_cell",
                "uk_world_spawn_candidate_order",
                "fk_world_portal_targets_portal",
                "fk_world_portal_targets_target",
                "uk_world_portal_target_order",
                "chk_world_portal_targets_not_self",
                "fk_world_game_config_start_room",
                "ck_world_game_config_id",
                "fk_game_saves_user",
                "fk_game_saves_current_room",
                "fk_user_abilities_user",
                "ck_world_items_weight",
                "ck_world_items_value",
                "ck_world_game_config_grid"
        )));
    }

    @Test
    void seedDataMatchesRuntimeExpectations() throws SQLException {
        assertEquals(1, queryInt("SELECT COUNT(*) FROM world_game_config"));
        assertEquals(1, queryInt("SELECT COUNT(*) FROM world_game_config WHERE id = 1 AND start_room_id = 'outside'"));
        assertEquals(1, queryInt("""
                SELECT COUNT(*)
                FROM world_items
                WHERE item_id = 'magic_cookie'
                  AND item_category = 'consumable'
                  AND usable = TRUE
                """));
        assertEquals(24, queryInt("SELECT COUNT(*) FROM world_rooms"));
        assertEquals(47, queryInt("SELECT COUNT(*) FROM world_items"));
        assertEquals(2, queryInt("SELECT COUNT(*) FROM world_map_views"));
        assertEquals(25, queryInt("SELECT COUNT(*) FROM world_room_layouts"));
        assertEquals(15, queryInt("SELECT COUNT(*) FROM world_portal_targets WHERE portal_room_id = 'portal'"));
        assertEquals(3, queryInt("SELECT COUNT(*) FROM ability_config"));
        assertEquals(1, queryInt("""
                SELECT COUNT(*)
                FROM ability_config
                WHERE ability_code = 'max_weight'
                  AND base_value = 5
                  AND increment_per_level = 3
                  AND base_cost = 50
                  AND max_level = 10
                """));
        assertEquals(1, queryInt("""
                SELECT COUNT(*)
                FROM ability_config
                WHERE ability_code = 'gold_bonus'
                  AND base_value = 0
                  AND increment_per_level = 5
                  AND base_cost = 80
                  AND max_level = 10
                """));
        assertEquals(1, queryInt("""
                SELECT COUNT(*)
                FROM ability_config
                WHERE ability_code = 'move_speed'
                  AND base_value = 2
                  AND increment_per_level = 1
                  AND base_cost = 100
                  AND max_level = 5
                """));
    }

    @Test
    void mapLayoutsCoverEveryRoomAndHaveSinglePrimaryView() throws SQLException {
        assertEquals(0, queryInt("""
                SELECT COUNT(*)
                FROM world_rooms r
                LEFT JOIN world_room_layouts l ON l.room_id = r.room_id
                WHERE l.room_id IS NULL
                """));
        assertEquals(0, queryInt("""
                SELECT COUNT(*)
                FROM (
                    SELECT room_id
                    FROM world_room_layouts
                    GROUP BY room_id
                    HAVING SUM(primary_view = TRUE) <> 1
                ) invalid_layouts
                """));
    }

    @Test
    void databaseRejectsInvalidWorldConfiguration() {
        assertStatementFails("""
                INSERT INTO world_rooms (room_id, area_id, description, zh_name, room_type, floor_no, display_order)
                VALUES ('bad_room', 'missing_area', 'bad', '坏房间', 'normal', 0, 999)
                """);
        assertStatementFails("""
                INSERT INTO world_items (item_id, item_name, description, weight, item_value, item_category, usable)
                VALUES ('bad_item', '坏物品', 'bad', -1, 0, 'material', false)
                """);
        assertStatementFails("INSERT INTO game_saves (user_id, current_room_id) VALUES (999999, 'outside')");
        assertStatementFails("INSERT INTO game_saves (user_id, current_room_id) VALUES (1, 'missing_room')");
        assertStatementFails("INSERT INTO user_abilities (user_id) VALUES (999999)");
        assertStatementFails("""
                INSERT INTO world_game_config (
                    id,
                    start_room_id,
                    default_max_weight,
                    default_player_grid_row,
                    default_player_grid_col,
                    spawn_random_seed,
                    portal_random_seed
                ) VALUES (
                    2,
                    'outside',
                    5,
                    4,
                    4,
                    1,
                    1
                )
                """);
        assertStatementFails("""
                INSERT INTO world_portal_targets (portal_room_id, target_room_id, display_order)
                VALUES ('portal', 'portal', 999)
                """);
        assertStatementFails("""
                INSERT INTO world_random_spawn_candidates (rule_id, room_id, grid_row, grid_col, display_order)
                VALUES ('magic_cookie_spawn', 'office', 0, 0, 1)
                """);
    }

    private static int queryInt(String sql) throws SQLException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             var resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private static Set<String> queryStringSet(String sql) throws SQLException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             var resultSet = statement.executeQuery(sql)) {
            Set<String> values = new java.util.HashSet<>();
            while (resultSet.next()) {
                values.add(resultSet.getString(1));
            }
            return values;
        }
    }

    private static void assertStatementFails(String sql) {
        assertThrows(SQLException.class, () -> {
            try (Connection connection = getConnection();
                 Statement statement = connection.createStatement()) {
                statement.execute(sql);
            }
        });
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(MYSQL.getJdbcUrl(), MYSQL.getUsername(), MYSQL.getPassword());
    }
}
