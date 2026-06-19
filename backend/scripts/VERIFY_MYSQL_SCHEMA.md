# MySQL Schema Verification

本项目当前只使用 `backend/sql/ability_schema.sql` 初始化数据库。该脚本会重建世界配置、能力配置和存档表，适合演示环境重新初始化。

## 初始化

Windows 本地 MySQL 建议显式指定 `127.0.0.1`：

```bash
mysql -h 127.0.0.1 -u root -p123456 < backend/sql/ability_schema.sql
```

## 结构检查

```bash
mysql -h 127.0.0.1 -u root -p123456 arknights_db < backend/scripts/verify-mysql-schema.sql
```

重点确认：

- `world_game_config` 只有一行强类型配置。
- `magic_cookie` 的 `item_category` 是 `consumable`，`usable` 是 `1`。
- `game_saves.current_room_id` 存在外键 `fk_game_saves_current_room`。
- 世界配置表之间存在外键和基础 CHECK 约束。

## 负向检查

以下 SQL 应该失败，用于确认约束生效：

```sql
INSERT INTO world_rooms (room_id, area_id, description, zh_name, room_type, floor_no, display_order)
VALUES ('bad_room', 'missing_area', 'bad', '坏房间', 'normal', 0, 999);

INSERT INTO world_items (item_id, item_name, description, weight, item_value, item_category, usable)
VALUES ('bad_item', '坏物品', 'bad', -1, 0, 'material', false);

INSERT INTO game_saves (user_id, current_room_id)
VALUES (1, 'missing_room');
```
