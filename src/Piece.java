public class Piece {
    int x; // Pixel x-coordinate for rendering
    int y; // Pixel y-coordinate for rendering
    private int row; // Grid row index
    private int col; // Grid column index
    private int kind; // Gem type (0 to GEM_TYPES-1)
    int match; // Number of matches this gem is part of

    // Constructor initializes match to 0
    public Piece() {
        this.match = 0;
    }

    // Getters and setters with validation
    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
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

    public int getKind() {
        return kind;
    }

    public void setKind(int kind) {
        if (kind < 0 || kind >= 7) { // Assuming GEM_TYPES = 7 from Board.java
            throw new IllegalArgumentException("Invalid gem type: " + kind);
        }
        this.kind = kind;
    }
}