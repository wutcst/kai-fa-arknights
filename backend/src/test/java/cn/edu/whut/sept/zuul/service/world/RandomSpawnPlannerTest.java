package cn.edu.whut.sept.zuul.service.world;

import cn.edu.whut.sept.zuul.model.world.WorldRandomSpawnCandidate;
import cn.edu.whut.sept.zuul.model.world.WorldRandomSpawnRule;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class RandomSpawnPlannerTest {
    private final RandomSpawnPlanner planner = new RandomSpawnPlanner();

    @Test
    void sameSeedProducesSamePlacements() {
        WorldRandomSpawnRule rule = rule("cookie", "magic_cookie", 3, 3);

        List<String> first = roomIds(planner.plan(1234L, rule, candidates(6)));
        List<String> second = roomIds(planner.plan(1234L, rule, candidates(6)));

        assertEquals(first, second);
    }

    @Test
    void differentBaseSeedChangesPlacements() {
        WorldRandomSpawnRule rule = rule("cookie", "magic_cookie", 3, 3);
        List<String> first = roomIds(planner.plan(1234L, rule, candidates(6)));
        List<String> second = roomIds(planner.plan(5678L, rule, candidates(6)));

        assertNotEquals(first, second);
    }

    @Test
    void sameBaseSeedUsesRuleIdAsIndependentSalt() {
        assertNotEquals(
                planner.deriveRuleSeed(1234L, "cookie_a"),
                planner.deriveRuleSeed(1234L, "cookie_b")
        );
    }

    @Test
    void countStaysWithinConfiguredRange() {
        List<RandomSpawnPlanner.SpawnPlacement> placements =
                planner.plan(20260620L, rule("cookie", "magic_cookie", 2, 5), candidates(8));

        assertTrue(placements.size() >= 2);
        assertTrue(placements.size() <= 5);
    }

    @Test
    void insufficientCandidatesThrows() {
        WorldRandomSpawnRule rule = rule("cookie", "magic_cookie", 5, 6);

        assertThrows(IllegalStateException.class, () -> planner.plan(20260620L, rule, candidates(4)));
    }

    @Test
    void duplicateCandidateRoomThrows() {
        WorldRandomSpawnRule rule = rule("cookie", "magic_cookie", 1, 2);
        List<WorldRandomSpawnCandidate> candidates = List.of(
                candidate("cookie", "room_1", 2, 2, 1),
                candidate("cookie", "room_1", 2, 6, 2)
        );

        assertThrows(IllegalStateException.class, () -> planner.plan(20260620L, rule, candidates));
    }

    private List<String> roomIds(List<RandomSpawnPlanner.SpawnPlacement> placements) {
        return placements.stream().map(RandomSpawnPlanner.SpawnPlacement::getRoomId).collect(Collectors.toList());
    }

    private List<WorldRandomSpawnCandidate> candidates(int count) {
        return java.util.stream.IntStream.rangeClosed(1, count)
                .mapToObj(i -> candidate("cookie", "room_" + i, 2, 2, i))
                .collect(Collectors.toList());
    }

    private WorldRandomSpawnRule rule(String ruleId, String itemId, int min, int max) {
        WorldRandomSpawnRule rule = new WorldRandomSpawnRule();
        set(rule, "ruleId", ruleId);
        set(rule, "itemId", itemId);
        set(rule, "minCount", min);
        set(rule, "maxCount", max);
        set(rule, "enabled", true);
        set(rule, "displayOrder", 1);
        return rule;
    }

    private WorldRandomSpawnCandidate candidate(String ruleId, String roomId, int row, int col, int order) {
        WorldRandomSpawnCandidate candidate = new WorldRandomSpawnCandidate();
        set(candidate, "ruleId", ruleId);
        set(candidate, "roomId", roomId);
        set(candidate, "gridRow", row);
        set(candidate, "gridCol", col);
        set(candidate, "displayOrder", order);
        return candidate;
    }

    private void set(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
