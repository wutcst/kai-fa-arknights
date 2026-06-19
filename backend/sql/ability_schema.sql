-- MySQL 数据库初始化脚本
-- 注意：本脚本会重建世界配置、能力配置和存档表，适合课程演示环境重新初始化。
CREATE DATABASE IF NOT EXISTS arknights_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE arknights_db;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS game_saves;
DROP TABLE IF EXISTS user_abilities;
DROP TABLE IF EXISTS ability_config;
DROP TABLE IF EXISTS world_portal_targets;
DROP TABLE IF EXISTS world_random_spawn_candidates;
DROP TABLE IF EXISTS world_random_spawn_rules;
DROP TABLE IF EXISTS world_room_initial_items;
DROP TABLE IF EXISTS world_item_effects;
DROP TABLE IF EXISTS world_room_exits;
DROP TABLE IF EXISTS world_items;
DROP TABLE IF EXISTS world_rooms;
DROP TABLE IF EXISTS world_directions;
DROP TABLE IF EXISTS world_areas;
DROP TABLE IF EXISTS world_game_config;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(20) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO users (username, password) VALUES
    ('player1', '123456'),
    ('player2', '123456'),
    ('admin', 'admin123')
ON DUPLICATE KEY UPDATE username = username;

CREATE TABLE world_areas (
    area_id VARCHAR(50) PRIMARY KEY,
    area_name VARCHAR(50) NOT NULL,
    description VARCHAR(200)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE world_rooms (
    room_id VARCHAR(50) PRIMARY KEY,
    area_id VARCHAR(50) NOT NULL,
    description VARCHAR(200) NOT NULL,
    zh_name VARCHAR(50) NOT NULL,
    room_type VARCHAR(30) NOT NULL DEFAULT 'normal',
    floor_no INT NOT NULL DEFAULT 0,
    display_order INT NOT NULL,
    CONSTRAINT fk_world_rooms_area FOREIGN KEY (area_id) REFERENCES world_areas(area_id),
    CONSTRAINT ck_world_rooms_type CHECK (room_type IN ('normal', 'entrance', 'facility', 'portal')),
    INDEX idx_world_rooms_area (area_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE world_directions (
    direction_code VARCHAR(20) PRIMARY KEY,
    direction_name VARCHAR(20) NOT NULL,
    display_order INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE world_room_exits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    source_room_id VARCHAR(50) NOT NULL,
    direction_code VARCHAR(20) NOT NULL,
    target_room_id VARCHAR(50) NOT NULL,
    display_order INT NOT NULL,
    UNIQUE KEY uk_world_room_exit (source_room_id, direction_code),
    CONSTRAINT fk_world_room_exits_source FOREIGN KEY (source_room_id) REFERENCES world_rooms(room_id),
    CONSTRAINT fk_world_room_exits_target FOREIGN KEY (target_room_id) REFERENCES world_rooms(room_id),
    CONSTRAINT fk_world_room_exits_direction FOREIGN KEY (direction_code) REFERENCES world_directions(direction_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE world_items (
    item_id VARCHAR(80) PRIMARY KEY,
    item_name VARCHAR(80) NOT NULL,
    description VARCHAR(500) NOT NULL,
    weight INT NOT NULL,
    item_value INT NOT NULL,
    item_category VARCHAR(30) NOT NULL,
    usable BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT ck_world_items_weight CHECK (weight >= 0),
    CONSTRAINT ck_world_items_value CHECK (item_value >= 0),
    CONSTRAINT ck_world_items_category CHECK (item_category IN ('material', 'currency', 'consumable'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE world_item_effects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id VARCHAR(80) NOT NULL,
    effect_code VARCHAR(50) NOT NULL,
    effect_value INT NOT NULL,
    UNIQUE KEY uk_world_item_effect (item_id, effect_code),
    CONSTRAINT fk_world_item_effects_item FOREIGN KEY (item_id) REFERENCES world_items(item_id),
    CONSTRAINT ck_world_item_effect_value CHECK (effect_value > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE world_room_initial_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id VARCHAR(50) NOT NULL,
    item_id VARCHAR(80) NOT NULL,
    grid_row INT NOT NULL,
    grid_col INT NOT NULL,
    display_order INT NOT NULL,
    UNIQUE KEY uk_world_room_item (room_id, item_id),
    UNIQUE KEY uk_world_room_item_grid (room_id, grid_row, grid_col),
    CONSTRAINT fk_world_initial_items_room FOREIGN KEY (room_id) REFERENCES world_rooms(room_id),
    CONSTRAINT fk_world_initial_items_item FOREIGN KEY (item_id) REFERENCES world_items(item_id),
    CONSTRAINT ck_world_initial_items_grid CHECK (grid_row BETWEEN 0 AND 8 AND grid_col BETWEEN 0 AND 8)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE world_random_spawn_rules (
    rule_id VARCHAR(50) PRIMARY KEY,
    item_id VARCHAR(80) NOT NULL,
    min_count INT NOT NULL,
    max_count INT NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INT NOT NULL,
    CONSTRAINT fk_world_spawn_rules_item FOREIGN KEY (item_id) REFERENCES world_items(item_id),
    CONSTRAINT ck_world_spawn_rule_count CHECK (min_count >= 0 AND max_count >= min_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE world_random_spawn_candidates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_id VARCHAR(50) NOT NULL,
    room_id VARCHAR(50) NOT NULL,
    grid_row INT NOT NULL,
    grid_col INT NOT NULL,
    display_order INT NOT NULL,
    UNIQUE KEY uk_world_spawn_candidate_room (rule_id, room_id),
    CONSTRAINT fk_world_spawn_candidates_rule FOREIGN KEY (rule_id) REFERENCES world_random_spawn_rules(rule_id),
    CONSTRAINT fk_world_spawn_candidates_room FOREIGN KEY (room_id) REFERENCES world_rooms(room_id),
    CONSTRAINT ck_world_spawn_candidates_grid CHECK (grid_row BETWEEN 0 AND 8 AND grid_col BETWEEN 0 AND 8)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE world_portal_targets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    portal_room_id VARCHAR(50) NOT NULL,
    target_room_id VARCHAR(50) NOT NULL,
    display_order INT NOT NULL,
    UNIQUE KEY uk_world_portal_target (portal_room_id, target_room_id),
    CONSTRAINT fk_world_portal_targets_portal FOREIGN KEY (portal_room_id) REFERENCES world_rooms(room_id),
    CONSTRAINT fk_world_portal_targets_target FOREIGN KEY (target_room_id) REFERENCES world_rooms(room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE world_game_config (
    id INT PRIMARY KEY,
    start_room_id VARCHAR(50) NOT NULL,
    default_max_weight INT NOT NULL,
    default_player_grid_row INT NOT NULL,
    default_player_grid_col INT NOT NULL,
    spawn_random_seed BIGINT NOT NULL,
    portal_random_seed BIGINT NOT NULL,
    CONSTRAINT fk_world_game_config_start_room FOREIGN KEY (start_room_id) REFERENCES world_rooms(room_id),
    CONSTRAINT ck_world_game_config_weight CHECK (default_max_weight > 0),
    CONSTRAINT ck_world_game_config_grid CHECK (default_player_grid_row BETWEEN 0 AND 8 AND default_player_grid_col BETWEEN 0 AND 8)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE game_saves (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    current_room_id VARCHAR(50) NOT NULL,
    player_inventory JSON,
    player_weight INT DEFAULT 0,
    player_max_weight INT DEFAULT 5,
    player_grid_row INT DEFAULT 4,
    player_grid_col INT DEFAULT 4,
    room_history JSON,
    room_items JSON,
    room_item_positions JSON,
    saved_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_id (user_id),
    CONSTRAINT fk_game_saves_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_game_saves_current_room FOREIGN KEY (current_room_id) REFERENCES world_rooms(room_id),
    CONSTRAINT ck_game_saves_weight CHECK (player_weight >= 0 AND player_max_weight > 0),
    CONSTRAINT ck_game_saves_grid CHECK (player_grid_row BETWEEN 0 AND 8000 AND player_grid_col BETWEEN 0 AND 8000)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ability_config (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ability_code VARCHAR(30) NOT NULL UNIQUE,
    ability_name VARCHAR(50) NOT NULL,
    base_value INT NOT NULL DEFAULT 0,
    increment_per_level INT NOT NULL DEFAULT 1,
    base_cost INT NOT NULL DEFAULT 10,
    cost_multiplier DECIMAL(3,1) NOT NULL DEFAULT 1.5,
    max_level INT NOT NULL DEFAULT 10,
    description VARCHAR(200),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO ability_config (ability_code, ability_name, base_value, increment_per_level, base_cost, cost_multiplier, max_level, description) VALUES
('max_weight', '负重上限', 5, 3, 50, 1.8, 10, '背包最大负重，每级增加3'),
('gold_bonus', '龙门币加成', 0, 5, 80, 2.0, 10, '结算时额外获得龙门币百分比，每级增加5%'),
('move_speed', '移动速度', 2, 1, 100, 2.2, 5, '房间内移动速度，每级增加1');

CREATE TABLE user_abilities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    gold INT NOT NULL DEFAULT 0,
    max_weight_level INT NOT NULL DEFAULT 1,
    gold_bonus_level INT NOT NULL DEFAULT 1,
    move_speed_level INT NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_abilities_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO world_areas (area_id, area_name, description) VALUES
('main_campus', '罗德岛主区', '主探索区域'),
('training_facility', '训练设施内部', '训练设施多层区域');

INSERT INTO world_directions (direction_code, direction_name, display_order) VALUES
('north', '北', 1), ('south', '南', 2), ('east', '东', 3), ('west', '西', 4), ('up', '上楼', 5), ('down', '下楼', 6);

INSERT INTO world_rooms (room_id, area_id, description, zh_name, room_type, floor_no, display_order) VALUES
('outside', 'main_campus', 'outside the main entrance of the university', '罗德岛入口', 'normal', 0, 1),
('theater', 'main_campus', 'in a lecture theater', '训练设施', 'entrance', 0, 2),
('pub', 'main_campus', 'in the campus pub', '公开交易所', 'normal', 0, 3),
('lab', 'main_campus', 'in a computing lab', '加工站', 'normal', 0, 4),
('office', 'main_campus', 'in the computing admin office', '精英干员办公室', 'normal', 0, 5),
('portal', 'main_campus', 'in a mysterious portal room', '机密传送门', 'portal', 0, 6),
('library', 'main_campus', 'in the university library', '机密档案室', 'normal', 0, 7),
('gym', 'main_campus', 'in the campus gym', '体能训练场', 'normal', 0, 8),
('cafeteria', 'main_campus', 'in the campus cafeteria', '物资补给区', 'normal', 0, 9),
('garden', 'main_campus', 'in the campus garden', '户外休闲区', 'normal', 0, 10),
('bookstore', 'main_campus', 'in the campus bookstore', '资源回收站', 'normal', 0, 11),
('dormitory', 'main_campus', 'in the student dormitory', '干员宿舍区', 'normal', 0, 12),
('theater_lobby', 'training_facility', 'in the theater lobby', '设施接待大厅', 'facility', 1, 13),
('theater_classroom_101', 'training_facility', 'in classroom 101', '基础训练室A', 'facility', 1, 14),
('theater_classroom_102', 'training_facility', 'in classroom 102', '基础训练室B', 'facility', 1, 15),
('theater_stairway_1f', 'training_facility', 'in the 1st floor stairway', '设施东侧通道', 'facility', 1, 16),
('theater_classroom_201', 'training_facility', 'in classroom 201', '进阶训练室A', 'facility', 2, 17),
('theater_classroom_202', 'training_facility', 'in classroom 202', '进阶训练室B', 'facility', 2, 18),
('theater_office', 'training_facility', 'in the teacher office', '人事档案室', 'facility', 2, 19),
('theater_stairway_2f', 'training_facility', 'in the 2nd floor stairway', '设施中央通道', 'facility', 2, 20),
('theater_classroom_301', 'training_facility', 'in classroom 301', '精英训练室A', 'facility', 3, 21),
('theater_classroom_302', 'training_facility', 'in classroom 302', '精英训练室B', 'facility', 3, 22),
('theater_lab', 'training_facility', 'in the computer lab', '制造站', 'facility', 3, 23),
('theater_stairway_3f', 'training_facility', 'in the 3rd floor stairway', '设施顶层通道', 'facility', 3, 24);

INSERT INTO world_game_config (id, start_room_id, default_max_weight, default_player_grid_row, default_player_grid_col, spawn_random_seed, portal_random_seed) VALUES
(1, 'outside', 5, 4, 4, 20260620, 20260621);

INSERT INTO world_room_exits (source_room_id, direction_code, target_room_id, display_order) VALUES
('outside','east','theater',1),('outside','south','lab',2),('outside','west','pub',3),('outside','north','portal',4),
('theater','west','outside',1),('theater','north','library',2),('library','south','theater',1),
('pub','east','outside',1),('pub','south','gym',2),('gym','north','pub',1),('gym','south','cafeteria',2),('cafeteria','north','gym',1),
('lab','north','outside',1),('lab','east','office',2),('lab','south','garden',3),('office','west','lab',1),
('garden','north','lab',1),('garden','west','bookstore',2),('garden','south','dormitory',3),('bookstore','east','garden',1),('dormitory','north','garden',1),
('portal','south','outside',1),('theater','south','theater_lobby',3),('theater_lobby','north','theater',1),('theater_lobby','west','theater_classroom_101',2),('theater_lobby','east','theater_classroom_102',3),('theater_lobby','up','theater_stairway_1f',4),
('theater_classroom_101','east','theater_lobby',1),('theater_classroom_102','west','theater_lobby',1),('theater_stairway_1f','down','theater_lobby',1),('theater_stairway_1f','up','theater_stairway_2f',2),
('theater_stairway_2f','down','theater_stairway_1f',1),('theater_stairway_2f','up','theater_stairway_3f',2),('theater_stairway_2f','west','theater_classroom_201',3),('theater_stairway_2f','east','theater_classroom_202',4),('theater_stairway_2f','south','theater_office',5),
('theater_classroom_201','east','theater_stairway_2f',1),('theater_classroom_202','west','theater_stairway_2f',1),('theater_office','north','theater_stairway_2f',1),
('theater_stairway_3f','down','theater_stairway_2f',1),('theater_stairway_3f','west','theater_classroom_301',2),('theater_stairway_3f','east','theater_classroom_302',3),('theater_stairway_3f','south','theater_lab',4),
('theater_classroom_301','east','theater_stairway_3f',1),('theater_classroom_302','west','theater_stairway_3f',1),('theater_lab','north','theater_stairway_3f',1);

INSERT INTO world_items (item_id, item_name, description, weight, item_value, item_category, usable) VALUES
('orirock','源岩','最基础的岩石原料，广泛用于初级加工与制造，能从几乎所有岩层中采集到。',1,5,'material',false),
('orirock_cube','固源岩','将源岩粉碎后重组而成的坚固立方体，基建制造与干员初期精英化的常用素材。',1,10,'material',false),
('orirock_concentration','提纯源岩','经过多道工序提纯的高密度源岩，硬度极高，是高级精英化与专精的基石材料。',2,50,'material',false),
('device','装置','功能完好的通用型机械装置，是制造全新装置和合成各类精密仪器的中间产物。',3,60,'material',false),
('loxic_kohl','扭转醇','具有特殊旋光性的醇类化合物，是合成白马醇等多种关键药物与工业品的前置原料。',1,15,'material',false),
('white_horse_kohl','白马醇','由扭转醇精制而成的纯白醇类，性质极其稳定，高级术师与治疗干员技能专精的消耗品。',2,80,'material',false),
('integrated_device','全新装置','刚从生产线下来的精密装置，性能处于最佳状态，为精英化二阶段和关键技能专精所必需。',3,200,'material',false),
('crystalline_component','晶体元件','从晶体矿物上切割下的基础电子元件，是构建晶体电路等复杂系统的起点。',2,100,'material',false),
('crystalline_circuit','晶体电路','集成了多个晶体元件的高密度电路模块，运算性能强大，用于高级技能专精与模组数据块制造。',4,500,'material',false),
('rma70_12','RMA70-12','源石技艺与现代工业结合的半成品，编号70-12，稀有度高，是多种高端电子元件的基板。',2,120,'material',false),
('oriron','异铁','在天然磁场中生成的奇异铁矿石，采集后可用于熔炼异铁组，是基础工业原料之一。',1,20,'material',false),
('oriron_shard','异铁碎片','开采异铁矿时产生的碎片，可合成完整异铁，常用于初期武器与装备的强化。',1,8,'material',false),
('sugar','糖','便携式高能代糖补给，不仅是干员作战时的能量来源，也是制造糖组的基本材料。',1,12,'material',false),
('sugar_pack','糖组','将糖压缩包装后的能量块，便于大量储存与运输，是中期精英化和技能升级的常见需求。',2,40,'material',false),
('polyketon','酮凝集','有机聚合物形成的凝胶状物质，可作为粘合剂与绝缘层，是制造站首批可生产的材料之一。',1,5,'material',false),
('sugar_lump','糖聚块','高度提纯并聚合的糖晶体，蕴含惊人能量，仅供顶尖技能的专精与模组升级使用。',2,100,'material',false),
('aketon','酮凝集组','酮凝集经过压缩和固化处理后的块状物，绝缘与隔源性能优异，极受术师干员青睐。',2,50,'material',false),
('polyester','聚酸酯','常见的合成树脂原料，轻便且易于塑形，是制造聚酸酯组和部分家具零件的基础素材。',1,15,'material',false),
('polyester_pack','聚酸酯组','多份聚酸酯的标准化封装包，便于运输与管理，满足干员中期精英化的大量消耗。',2,45,'material',false),
('oriron_cluster','异铁组','由数块异铁组合而成的标准加工单元，广泛用于重装干员的精英化与防御装备制造。',2,80,'material',false),
('keton_colloid','酮阵列','在特殊条件下令酮凝集组规整排列形成的胶体阵列，结构极度稳定，用于尖端的源石技艺强化。',3,200,'material',false),
('grindstone','研磨石','表面密布研磨颗粒的工具石，能将粗加工部件打磨至微米级精度，泛用性极高。',2,60,'material',false),
('grindstone_pentahydrate','五水研磨石','含有五个结晶水的特殊研磨石，研磨精度进一步提升，是生产双极纳米片等顶级材料的关键。',3,250,'material',false),
('rma70_24','RMA70-24','RMA70-12的深度加工型，内部回路更为复杂，专为精英化二阶段及精密仪器制造而设计。',3,300,'material',false),
('incandescent_alloy','炽合金','能在极高温度下保持稳定的合金，是制作武器隔热层与源石蚀刻回路的重要材料。',2,150,'material',false),
('damaged_device','破损装置','在冲突中受损的机械装置，虽然无法直接使用，但拆解后仍能回收若干标准零件。',3,30,'material',false),
('oriron_block','异铁块','将异铁组熔炼锻压成的超合金块，坚不可摧，是重装与部分近卫干员专精的顶级材料。',4,400,'material',false),
('compound_cutting_fluid','化合切削液','用于精密加工的特种化学液，能显著提升材料切割精度，是维多利亚篇章后出现的新素材。',3,180,'material',false),
('incandescent_alloy_block','炽合金块','炽合金的锻压块，耐热极限更为出色，近卫与狙击干员高阶专精的必备消耗品。',4,450,'material',false),
('refined_solvent','精炼溶剂','经过多重蒸馏的超纯溶剂，能溶解绝大多数顽固原料，是制造聚合凝胶和聚合剂的必需品。',2,120,'material',false),
('semi_synthetic_solvent','半自然溶剂','天然提取物与合成溶剂的混合物，调和了效能与成本，是精炼溶剂的前置半成品。',1,40,'material',false),
('cutting_fluid_solution','切削原液','未经稀释的高浓度切削液，切割能力极强，但直接使用风险大，须调配成化合切削液。',2,60,'material',false),
('polyester_lump','聚酸酯块','由聚酸酯组高压聚合而成的硬质块体，强度远超普通酯类，用于精英化二阶段的防具制作。',3,200,'material',false),
('gel','凝胶','具有良好的生物相容性的透明凝胶，常用于伤口处理与精密仪器润滑。',3,150,'material',false),
('gold','赤金','高纯度的黄金，在罗德岛作为通用货币流通，也可用于高级装备的镀层处理。',2,100,'currency',false),
('polymerized_gel','聚合凝胶','通过高分子聚合而成的特殊凝胶，拥有惊人的吸附与缓冲能力，广泛应用于医疗与防护插板。',5,1500,'material',false),
('diketone','双酮','含有两个酮基的有机化合物，是多种药物合成的关键中间体。',1,50,'material',false),
('trans_salt_group','转质盐组','经过特殊工艺处理的盐类化合物，常用于源石技艺的媒介转换。',2,100,'material',false),
('ring_preform','环烃预制体','未完成环化反应的烃类化合物，是合成高级材料的中间体。',1,60,'material',false),
('trans_salt_block','转质盐聚块','高密度的转质盐结晶块，用于重装干员的高阶精英化。',4,350,'material',false),
('modified_device','改量装置','经过改装调整的测量设备，可用于精密仪器的校准工作。',2,80,'material',false),
('ester_raw','酯原料','生产聚酯类材料的基础原料，通过加工可制成多种工业用品。',1,35,'material',false),
('carbon','碳','最基础的碳元素材料，可用于多种合成反应的原料。',1,10,'material',false),
('carbon_fiber','碳素','高强度碳纤维材料，是制造轻质高强度装备的关键原料。',2,90,'material',false),
('orirock_cluster','固源岩组','多块固源岩的组合包装，便于大批量运输和使用。',2,45,'material',false),
('light_manganese','轻锰矿','含有锰元素的轻质矿物，是冶炼特种合金的原料之一。',1,55,'material',false),
('magic_cookie','理智增强剂','罗德岛开发的特殊药剂，注射可以增加负重',1,0,'consumable',true);

INSERT INTO world_item_effects (item_id, effect_code, effect_value) VALUES
('magic_cookie','MAX_WEIGHT_BONUS',5);

INSERT INTO world_room_initial_items (room_id, item_id, grid_row, grid_col, display_order) VALUES
('outside','orirock',2,2,1),('outside','orirock_cube',2,6,2),
('theater','orirock_concentration',2,2,1),('theater','device',2,6,2),
('pub','loxic_kohl',2,2,1),('pub','white_horse_kohl',2,6,2),
('lab','integrated_device',2,2,1),('lab','crystalline_component',2,6,2),('lab','crystalline_circuit',6,2,3),
('office','rma70_12',2,2,1),('gym','oriron',2,2,1),('gym','oriron_shard',2,6,2),
('cafeteria','sugar',2,2,1),('cafeteria','sugar_pack',2,6,2),('garden','polyketon',2,2,1),
('bookstore','sugar_lump',2,2,1),('bookstore','aketon',2,6,2),('dormitory','polyester',2,2,1),('dormitory','polyester_pack',2,6,2),
('theater_lobby','oriron_cluster',2,2,1),('theater_lobby','keton_colloid',2,6,2),
('theater_classroom_101','grindstone',2,2,1),('theater_classroom_101','grindstone_pentahydrate',2,6,2),('theater_classroom_101','orirock_cluster',6,2,3),
('theater_classroom_102','rma70_24',2,2,1),('theater_classroom_102','incandescent_alloy',2,6,2),('theater_classroom_102','light_manganese',6,2,3),
('theater_stairway_1f','damaged_device',2,2,1),('theater_stairway_1f','modified_device',2,6,2),
('theater_classroom_201','oriron_block',2,2,1),('theater_classroom_201','compound_cutting_fluid',2,6,2),('theater_classroom_201','ester_raw',6,2,3),
('theater_classroom_202','incandescent_alloy_block',2,2,1),('theater_classroom_202','carbon',2,6,2),('theater_classroom_202','carbon_fiber',6,2,3),
('theater_office','refined_solvent',2,2,1),('theater_office','semi_synthetic_solvent',2,6,2),('theater_office','trans_salt_group',6,2,3),('theater_office','ring_preform',6,6,4),
('theater_stairway_2f','cutting_fluid_solution',2,2,1),('theater_stairway_2f','trans_salt_block',2,6,2),
('theater_classroom_301','polyester_lump',2,2,1),('theater_classroom_301','gel',2,6,2),
('theater_classroom_302','gold',2,2,1),
('theater_lab','polymerized_gel',2,2,1),('theater_lab','diketone',2,6,2);

INSERT INTO world_random_spawn_rules (rule_id, item_id, min_count, max_count, enabled, display_order) VALUES
('magic_cookie_spawn','magic_cookie',5,10,true,1);

INSERT INTO world_random_spawn_candidates (rule_id, room_id, grid_row, grid_col, display_order) VALUES
('magic_cookie_spawn','outside',6,2,1),('magic_cookie_spawn','pub',6,2,2),('magic_cookie_spawn','lab',6,6,3),('magic_cookie_spawn','library',2,2,4),
('magic_cookie_spawn','gym',6,2,5),('magic_cookie_spawn','cafeteria',6,2,6),('magic_cookie_spawn','garden',2,6,7),('magic_cookie_spawn','bookstore',6,2,8),
('magic_cookie_spawn','theater_lobby',6,2,9),('magic_cookie_spawn','theater_classroom_101',6,6,10),('magic_cookie_spawn','theater_classroom_102',6,6,11),
('magic_cookie_spawn','theater_classroom_201',6,6,12),('magic_cookie_spawn','theater_classroom_202',6,6,13),('magic_cookie_spawn','theater_office',5,3,14),
('magic_cookie_spawn','theater_classroom_301',6,2,15),('magic_cookie_spawn','theater_classroom_302',2,6,16),('magic_cookie_spawn','theater_lab',6,2,17);

INSERT INTO world_portal_targets (portal_room_id, target_room_id, display_order) VALUES
('portal','outside',1),('portal','theater',2),('portal','pub',3),('portal','lab',4),('portal','office',5),('portal','library',6),('portal','gym',7),('portal','cafeteria',8),('portal','garden',9),('portal','bookstore',10),('portal','dormitory',11),('portal','theater_lobby',12),('portal','theater_classroom_101',13),('portal','theater_classroom_102',14),('portal','theater_stairway_1f',15);
