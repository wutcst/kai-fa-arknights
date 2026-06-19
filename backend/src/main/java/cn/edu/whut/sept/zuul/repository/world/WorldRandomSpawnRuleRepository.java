package cn.edu.whut.sept.zuul.repository.world;

import cn.edu.whut.sept.zuul.model.world.WorldRandomSpawnRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorldRandomSpawnRuleRepository extends JpaRepository<WorldRandomSpawnRule, String> {
    List<WorldRandomSpawnRule> findByEnabledTrueOrderByRuleIdAsc();
}
