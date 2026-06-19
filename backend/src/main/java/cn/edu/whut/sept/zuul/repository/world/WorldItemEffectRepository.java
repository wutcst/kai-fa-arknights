package cn.edu.whut.sept.zuul.repository.world;

import cn.edu.whut.sept.zuul.model.world.WorldItemEffect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorldItemEffectRepository extends JpaRepository<WorldItemEffect, Long> {
    Optional<WorldItemEffect> findByItemIdAndEffectCode(String itemId, String effectCode);
}
