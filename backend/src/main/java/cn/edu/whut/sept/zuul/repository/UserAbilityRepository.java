package cn.edu.whut.sept.zuul.repository;

import cn.edu.whut.sept.zuul.model.UserAbility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户能力数据访问层.
 */
@Repository
public interface UserAbilityRepository extends JpaRepository<UserAbility, Long> {
    
    /**
     * 根据用户ID查找用户能力.
     *
     * @param userId 用户ID
     * @return 用户能力对象
     */
    Optional<UserAbility> findByUserId(Long userId);
}
