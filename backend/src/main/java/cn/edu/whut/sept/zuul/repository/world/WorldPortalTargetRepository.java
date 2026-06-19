package cn.edu.whut.sept.zuul.repository.world;

import cn.edu.whut.sept.zuul.model.world.WorldPortalTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorldPortalTargetRepository extends JpaRepository<WorldPortalTarget, Long> {
    List<WorldPortalTarget> findAllByOrderByPortalRoomIdAscDisplayOrderAsc();
}
