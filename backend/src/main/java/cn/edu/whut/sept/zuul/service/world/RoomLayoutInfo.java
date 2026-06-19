package cn.edu.whut.sept.zuul.service.world;

public class RoomLayoutInfo {
    private final String viewType;
    private final int x;
    private final int y;
    private final boolean primaryView;

    public RoomLayoutInfo(String viewType, int x, int y, boolean primaryView) {
        this.viewType = viewType;
        this.x = x;
        this.y = y;
        this.primaryView = primaryView;
    }

    public String getViewType() {
        return viewType;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean isPrimaryView() {
        return primaryView;
    }
}
