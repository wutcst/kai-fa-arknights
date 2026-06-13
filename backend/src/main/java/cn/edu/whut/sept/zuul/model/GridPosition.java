package cn.edu.whut.sept.zuul.model;

/**
 * 房间网格坐标.
 */
public class GridPosition {
    private int row;
    private int col;

    public GridPosition() {
    }

    public GridPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }
}
