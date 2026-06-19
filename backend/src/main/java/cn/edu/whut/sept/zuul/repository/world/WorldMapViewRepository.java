package cn.edu.whut.sept.zuul.repository.world;

import cn.edu.whut.sept.zuul.model.world.WorldMapView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorldMapViewRepository extends JpaRepository<WorldMapView, String> {
    List<WorldMapView> findAllByOrderByDisplayOrderAsc();
}
