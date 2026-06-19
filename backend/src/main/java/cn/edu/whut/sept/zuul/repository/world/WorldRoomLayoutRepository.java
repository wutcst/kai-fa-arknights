package cn.edu.whut.sept.zuul.repository.world;

import cn.edu.whut.sept.zuul.model.world.WorldRoomLayout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorldRoomLayoutRepository extends JpaRepository<WorldRoomLayout, Long> {
    List<WorldRoomLayout> findAllByOrderByViewTypeAscDisplayOrderAsc();

    List<WorldRoomLayout> findByViewTypeOrderByDisplayOrderAsc(String viewType);

    List<WorldRoomLayout> findByRoomId(String roomId);
}
