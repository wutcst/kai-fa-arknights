package cn.edu.whut.sept.zuul.repository.world;

import cn.edu.whut.sept.zuul.model.world.WorldRoomInitialItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorldRoomInitialItemRepository extends JpaRepository<WorldRoomInitialItem, Long> {
    List<WorldRoomInitialItem> findAllByOrderByRoomIdAscDisplayOrderAsc();
}
