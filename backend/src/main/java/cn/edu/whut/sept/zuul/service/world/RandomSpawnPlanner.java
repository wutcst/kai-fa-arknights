package cn.edu.whut.sept.zuul.service.world;

import cn.edu.whut.sept.zuul.model.GridPosition;
import cn.edu.whut.sept.zuul.model.world.WorldRandomSpawnCandidate;
import cn.edu.whut.sept.zuul.model.world.WorldRandomSpawnRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class RandomSpawnPlanner {
    public List<SpawnPlacement> plan(long baseSeed, WorldRandomSpawnRule rule,
                                     List<WorldRandomSpawnCandidate> candidates) {
        if (rule.getMinCount() < 0 || rule.getMaxCount() < rule.getMinCount()) {
            throw new IllegalStateException("随机物品规则数量配置非法: " + rule.getRuleId());
        }
        if (candidates.size() < rule.getMaxCount()) {
            throw new IllegalStateException("随机物品候选房间不足: " + rule.getRuleId());
        }

        Set<String> roomIds = new HashSet<>();
        for (WorldRandomSpawnCandidate candidate : candidates) {
            if (!roomIds.add(candidate.getRoomId())) {
                throw new IllegalStateException("随机物品候选房间重复: " + candidate.getRoomId());
            }
        }

        Random random = new Random(deriveRuleSeed(baseSeed, rule.getRuleId()));
        int count = rule.getMinCount() + random.nextInt(rule.getMaxCount() - rule.getMinCount() + 1);
        List<WorldRandomSpawnCandidate> shuffled = new ArrayList<>(candidates);
        Collections.shuffle(shuffled, random);

        List<SpawnPlacement> placements = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            WorldRandomSpawnCandidate candidate = shuffled.get(i);
            placements.add(new SpawnPlacement(
                    candidate.getRoomId(),
                    rule.getItemId(),
                    new GridPosition(candidate.getGridRow(), candidate.getGridCol())
            ));
        }
        placements.sort(Comparator.comparing(SpawnPlacement::getRoomId));
        return placements;
    }

    long deriveRuleSeed(long baseSeed, String ruleId) {
        return Objects.hash(baseSeed, ruleId);
    }

    public static class SpawnPlacement {
        private final String roomId;
        private final String itemId;
        private final GridPosition position;

        public SpawnPlacement(String roomId, String itemId, GridPosition position) {
            this.roomId = roomId;
            this.itemId = itemId;
            this.position = position;
        }

        public String getRoomId() {
            return roomId;
        }

        public String getItemId() {
            return itemId;
        }

        public GridPosition getPosition() {
            return position;
        }
    }
}
