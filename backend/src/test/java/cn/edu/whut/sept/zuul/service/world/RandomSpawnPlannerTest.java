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
        WorldRandomSpawnRule rule = rule("cookie", "magic_cookie", 3, 3, 1234L);

        List<String> first = roomIds(planner.plan(rule, candidates(6)));
        List<String> second = roomIds(planner.plan(rule, candidates(6)));

        assertEquals(first, second);
    }

    @Test
    void differentSeedChangesPlacements() {
        List<String> first = roomIds(planner.plan(rule("cookie", "magic_cookie", 3, 3, 1234L), candidates(6)));
        List<String> second = roomIds(planner.plan(rule("cookie", "magic_cookie", 3, 3, 5678L), candidates(6)));

        assertNotEquals(first, second);
    }

    @Test
    void countStaysWithinConfiguredRange() {
        List<RandomSpawnPlanner.SpawnPlacement> placements =
                planner.plan(rule("cookie", "magic_cookie", 2, 5, 20240612L), candidates(8));

        assertTrue(placements.size() >= 2);
        assertTrue(placements.size() <= 5);
    }

    @Test
    void insufficientCandidatesThrows() {
        WorldRandomSpawnRule rule = rule("cookie", "magic_cookie", 5, 6, 20240612L);

        assertThrows(IllegalStateException.class, () -> planner.plan(rule, candidates(4)));
    }

    @Test
    void duplicateCandidateRoomThrows() {
        WorldRandomSpawnRule rule = rule("cookie", "magic_cookie", 1, 2, 20240612L);
        List<WorldRandomSpawnCandidate> candidates = List.of(
                candidate("cookie", "room_1", 2, 2, 1),
                candidate("cookie", "room_1", 2, 6, 2)
        );

        assertThrows(IllegalStateException.class, () -> planner.plan(rule, candidates));
    }

    private List<String> roomIds(List<RandomSpawnPlanner.SpawnPlacement> placements) {
        return placements.stream().map(RandomSpawnPlanner.SpawnPlacement::getRoomId).collect(Collectors.toList());
    }

    private List<WorldRandomSpawnCandidate> candidates(int count) {
        return java.util.stream.IntStream.rangeClosed(1, count)
                .mapToObj(i -> candidate("cookie", "room_" + i, 2, 2, i))
                .collect(Collectors.toList());
    }

    private WorldRandomSpawnRule rule(String ruleId, String itemId, int min, int max, long seed) {
        WorldRandomSpawnRule rule = new WorldRandomSpawnRule();
        set(rule, "ruleId", ruleId);
        set(rule, "itemId", itemId);
        set(rule, "minCount", min);
        set(rule, "maxCount", max);
        set(rule, "randomSeed", seed);
        set(rule, "enabled", true);
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
