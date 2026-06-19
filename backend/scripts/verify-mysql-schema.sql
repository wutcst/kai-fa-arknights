USE arknights_db;

SELECT table_name
FROM information_schema.tables
WHERE table_schema = DATABASE()
  AND table_name IN (
      'world_areas',
      'world_rooms',
      'world_directions',
      'world_map_views',
      'world_room_layouts',
      'world_room_exits',
      'world_items',
      'world_item_effects',
      'world_room_initial_items',
      'world_random_spawn_rules',
      'world_random_spawn_candidates',
      'world_portal_targets',
      'world_game_config',
      'game_saves'
  )
ORDER BY table_name;

SELECT constraint_name, table_name, constraint_type
FROM information_schema.table_constraints
WHERE table_schema = DATABASE()
  AND constraint_name IN (
      'fk_world_rooms_area',
      'fk_world_room_exits_source',
      'fk_world_room_exits_target',
      'fk_world_room_exits_direction',
      'fk_world_room_layouts_view',
      'fk_world_room_layouts_room',
      'uk_world_map_views_order',
      'uk_world_room_layout_view_room',
      'uk_world_room_layout_view_order',
      'chk_world_room_layouts_x',
      'chk_world_room_layouts_y',
      'fk_world_initial_items_room',
      'fk_world_initial_items_item',
      'fk_world_spawn_rules_item',
      'fk_world_spawn_candidates_rule',
      'fk_world_spawn_candidates_room',
      'uk_world_spawn_candidate_cell',
      'uk_world_spawn_candidate_order',
      'fk_world_portal_targets_portal',
      'fk_world_portal_targets_target',
      'uk_world_portal_target_order',
      'chk_world_portal_targets_not_self',
      'fk_world_game_config_start_room',
      'ck_world_game_config_id',
      'fk_game_saves_current_room',
      'ck_world_items_weight',
      'ck_world_items_value',
      'ck_world_game_config_grid'
  )
ORDER BY table_name, constraint_name;

SELECT * FROM world_game_config;

SELECT item_id, item_category, usable
FROM world_items
WHERE item_id = 'magic_cookie';

SELECT COUNT(*) AS room_count FROM world_rooms;
SELECT COUNT(*) AS item_count FROM world_items;
SELECT COUNT(*) AS map_view_count FROM world_map_views;
SELECT COUNT(*) AS room_layout_count FROM world_room_layouts;
SELECT COUNT(*) AS portal_target_count FROM world_portal_targets WHERE portal_room_id = 'portal';

SELECT view_type, view_box
FROM world_map_views
ORDER BY display_order;

SELECT r.room_id
FROM world_rooms r
LEFT JOIN world_room_layouts l ON l.room_id = r.room_id
WHERE l.room_id IS NULL;

SELECT room_id, COUNT(*) AS layout_count, SUM(primary_view = TRUE) AS primary_layout_count
FROM world_room_layouts
GROUP BY room_id
HAVING COUNT(*) > 1;
