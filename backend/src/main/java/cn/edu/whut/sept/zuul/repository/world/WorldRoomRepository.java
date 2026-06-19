package cn.edu.whut.sept.zuul.repository.world;

import cn.edu.whut.sept.zuul.model.world.WorldRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorldRoomRepository extends JpaRepository<WorldRoom, String> {
    List<WorldRoom> findAllByOrderByDisplayOrderAsc();
}
