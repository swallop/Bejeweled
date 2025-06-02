public class Piece {
    int x; // Pixel x-coordinate for rendering
    int y; // Pixel y-coordinate for rendering
    private int row; // Grid row index
    private int col; // Grid column index
    private int kind; // Gem type (0 to GEM_TYPES-1)
    int match; // Number of matches this gem is part of

    // Constructor initializes match to 0 and kind to -1 (invalid)
    public Piece() {
        this.match = 0;
        this.kind = -1; // Initialize to invalid kind
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
        if (row < 0) {
            throw new IllegalArgumentException("Row cannot be negative: " + row);
        }
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        if (col < 0) {
            throw new IllegalArgumentException("Column cannot be negative: " + col);
        }
        this.col = col;
    }

    public int getKind() {
        return kind;
    }

    public void setKind(int kind) {
        if (kind < -1 || kind >= 7) { // Allow -1 for invalid/border pieces
            throw new IllegalArgumentException("Invalid gem type: " + kind);
        }
        this.kind = kind;
    }
}