package cn.edu.whut.sept.zuul.repository.world;

import cn.edu.whut.sept.zuul.model.world.WorldRandomSpawnCandidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorldRandomSpawnCandidateRepository extends JpaRepository<WorldRandomSpawnCandidate, Long> {
    List<WorldRandomSpawnCandidate> findByRuleIdOrderByDisplayOrderAsc(String ruleId);
}
