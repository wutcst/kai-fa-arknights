package cn.edu.whut.sept.zuul.repository;

import cn.edu.whut.sept.zuul.model.UserAbility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAbilityRepository extends JpaRepository<UserAbility, Long> {
    Optional<UserAbility> findByUserId(Long userId);
}
