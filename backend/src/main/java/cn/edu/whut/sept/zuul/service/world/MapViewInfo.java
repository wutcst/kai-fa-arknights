package cn.edu.whut.sept.zuul.service.world;

public class MapViewInfo {
    private final String viewType;
    private final String viewName;
    private final String viewBox;

    public MapViewInfo(String viewType, String viewName, String viewBox) {
        this.viewType = viewType;
        this.viewName = viewName;
        this.viewBox = viewBox;
    }

    public String getViewType() {
        return viewType;
    }

    public String getViewName() {
        return viewName;
    }

    public String getViewBox() {
        return viewBox;
    }
}
