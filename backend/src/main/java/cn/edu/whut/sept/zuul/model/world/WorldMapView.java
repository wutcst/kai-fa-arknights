package cn.edu.whut.sept.zuul.model.world;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "world_map_views",
        uniqueConstraints = @UniqueConstraint(name = "uk_world_map_views_order", columnNames = "display_order"))
public class WorldMapView {
    @Id
    @Column(name = "view_type", length = 30)
    private String viewType;

    @Column(name = "view_name", nullable = false, length = 50)
    private String viewName;

    @Column(name = "view_box", nullable = false, length = 50)
    private String viewBox;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    public String getViewType() {
        return viewType;
    }

    public String getViewName() {
        return viewName;
    }

    public String getViewBox() {
        return viewBox;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }
}
