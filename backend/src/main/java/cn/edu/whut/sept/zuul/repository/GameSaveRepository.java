package cn.edu.whut.sept.zuul.repository;

import cn.edu.whut.sept.zuul.model.GameSave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

/**
 * 游戏存档数据访问层.
 */
@Repository
public interface GameSaveRepository extends JpaRepository<GameSave, Long> {

    /**
     * 根据用户ID查找存档.
     */
    Optional<GameSave> findByUserId(Long userId);

    /**
     * 检查用户是否有存档.
     */
    boolean existsByUserId(Long userId);

    /**
     * 删除用户的存档.
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM GameSave g WHERE g.userId = :userId")
    void deleteByUserId(Long userId);
}
