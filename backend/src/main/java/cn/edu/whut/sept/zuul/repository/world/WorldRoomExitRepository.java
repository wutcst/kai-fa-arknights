package cn.edu.whut.sept.zuul.repository.world;

import cn.edu.whut.sept.zuul.model.world.WorldRoomExit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorldRoomExitRepository extends JpaRepository<WorldRoomExit, Long> {
    List<WorldRoomExit> findAllByOrderBySourceRoomIdAscDisplayOrderAsc();
}
