package cn.edu.whut.sept.zuul.repository;

import cn.edu.whut.sept.zuul.model.AbilityConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AbilityConfigRepository extends JpaRepository<AbilityConfig, Integer> {
    Optional<AbilityConfig> findByAbilityCode(String abilityCode);
}
