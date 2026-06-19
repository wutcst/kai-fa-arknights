package cn.edu.whut.sept.zuul.repository.world;

import cn.edu.whut.sept.zuul.model.world.WorldArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorldAreaRepository extends JpaRepository<WorldArea, String> {
}
