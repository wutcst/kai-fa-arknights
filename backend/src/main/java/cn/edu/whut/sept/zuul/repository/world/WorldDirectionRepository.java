package cn.edu.whut.sept.zuul.repository.world;

import cn.edu.whut.sept.zuul.model.world.WorldDirection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorldDirectionRepository extends JpaRepository<WorldDirection, String> {
}
