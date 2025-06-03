import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.Random;

// Manages the game board, including gem grid, swaps, matches, and animations
public class Board {
    private static final int SIZE = 8;
    private static final int TILE_SIZE = 54;
    private static final int OFFSET_X = 65;
    private static final int OFFSET_Y = 60;
    private static final int GEM_TYPES = 7;
    private static final int INVALID_KIND = -1;

    private final Piece[][] grid;
    private Assets assets;
    private int click = 0;
    private int x0, y0, x, y;
    private boolean isSwap = false;
    private boolean isMoving;
    private Random rand;

    // Constructor initializes the grid and assets
    public Board(Assets assets) {
        this.assets = assets;
        this.grid = new Piece[SIZE + 2][SIZE + 2];
        this.rand = new Random();
        initGrid();
    }

    // Initializes the grid with pieces, setting borders to invalid
    private void initGrid() {
        for (int i = 0; i < SIZE + 2; i++) {
            for (int j = 0; j < SIZE + 2; j++) {
                grid[i][j] = new Piece();
            }
        }

        // Initialize playable area with random gems, avoiding initial matches
        for (int i = 1; i <= SIZE; i++) {
            for (int j = 1; j <= SIZE; j++) {
                int gemType;
                do {
                    gemType = rand.nextInt(GEM_TYPES);
                } while (CreateInitialMatch(i, j, gemType));
                grid[i][j].setKind(gemType);
                grid[i][j].setRow(i);
                grid[i][j].setCol(j);
                grid[i][j].setX(j * TILE_SIZE);
                grid[i][j].setY(i * TILE_SIZE);
            }
        }
    }

    // Check if placing a gem would create an initial match
    //Cancel the matched gem at the first start
    private boolean CreateInitialMatch(int row, int col, int gemType) {
        // Check horizontal
        int horizontalCount = 1;
        if (col > 1 && grid[row][col-1].getKind() == gemType) {
            horizontalCount++;
            if (col > 2 && grid[row][col-2].getKind() == gemType) {
                horizontalCount++;
            }
        }

        // Check vertical
        int verticalCount = 1;
        if (row > 1 && grid[row-1][col].getKind() == gemType) {
            verticalCount++;
            if (row > 2 && grid[row-2][col].getKind() == gemType) {
                verticalCount++;
            }
        }
        return horizontalCount >= 3 || verticalCount >= 3;
    }

    // Handles mouse input for selecting and swapping pieces
    public void handleMouseInput(MouseEvent mouse) {
        int posX = mouse.getX() - OFFSET_X;
        int posY = mouse.getY() - OFFSET_Y;

        if (mouse.getButton() == MouseEvent.BUTTON1 && !isSwap && !isMoving) {
            click++;
            if (click == 1) {
                x0 = posX / TILE_SIZE + 1;
                y0 = posY / TILE_SIZE + 1;
                if (!isValidPosition(x0, y0)) {
                    click = 0; // Reset if click is outside playable area
                }
            } else if (click == 2) {
                x = posX / TILE_SIZE + 1;
                y = posY / TILE_SIZE + 1;
                if (isValidPosition(x, y) && isAdjacentMove(x0, y0, x, y)) {
                    swap(grid[y0][x0], grid[y][x]);
                    isSwap = true;
                }
                click = 0;
            }
        }
    }

    // Checks if a position is within the playable grid
    private boolean isValidPosition(int x, int y) {
        return x >= 1 && x <= SIZE && y >= 1 && y <= SIZE;
    }

    // Checks if the move is to an adjacent cell (not diagonal)
    private boolean isAdjacentMove(int x1, int y1, int x2, int y2) {
        return Math.abs(x2 - x1) + Math.abs(y2 - y1) == 1;
    }

    // Swaps two pieces, updating their positions and coordinates
    private void swap(Piece p1, Piece p2) {
        int tempRow = p1.getRow(), tempCol = p1.getCol();
        p1.setRow(p2.getRow());
        p1.setCol(p2.getCol());
        p2.setRow(tempRow);
        p2.setCol(tempCol);

        grid[p1.getRow()][p1.getCol()] = p1;
        grid[p2.getRow()][p2.getCol()] = p2;
    }

    // Updates the game state (matches and animations)
    public int update() {
        findMatches();
        animateMovement();
        return processMatches();
    }

    // Finds matches of three or more gems in a row or column
    private void findMatches() {
        // Reset matches
        for (int i = 1; i <= SIZE; i++) {
            for (int j = 1; j <= SIZE; j++) {
                grid[i][j].match = 0;
            }
        }

        if (isSwap) {
            // Check rows and columns of swapped gems (y0, x0) and (y, x)
            checkRow(y0);
            checkRow(y);
            checkColumn(x0);
            checkColumn(x);
        } else {
            // Full grid scan for cascading matches
            for (int i = 1; i <= SIZE; i++) {
                checkRow(i);
                checkColumn(i);
            }
        }
    }

    private void checkRow(int i) {
        int start = 1, count = 1;
        int currentKind = grid[i][1].getKind();
        for (int j = 2; j <= SIZE; j++) {
            if (grid[i][j].getKind() == currentKind && currentKind != INVALID_KIND) {
                count++;
            } else {
                if (count >= 3) {
                    for (int k = start; k < start + count; k++) {
                        grid[i][k].match = count;
                    }
                }
                start = j;
                count = 1;
                currentKind = grid[i][j].getKind();
            }
        }
        if (count >= 3) {
            for (int k = start; k < start + count; k++) {
                grid[i][k].match = count;
            }
        }
    }

    private void checkColumn(int j) {
        int start = 1, count = 1;
        int currentKind = grid[1][j].getKind();
        for (int i = 2; i <= SIZE; i++) {
            if (grid[i][j].getKind() == currentKind && currentKind != INVALID_KIND) {
                count++;
            } else {
                if (count >= 3) {
                    for (int k = start; k < start + count; k++) {
                        grid[k][j].match = count;
                    }
                }
                start = i;
                count = 1;
                currentKind = grid[i][j].getKind();
            }
        }
        if (count >= 3) {
            for (int k = start; k < start + count; k++) {
                grid[k][j].match = count;
            }
        }
    }

    // Animates piece movement towards their target positions
    private void animateMovement() {
        int speedSwapAnimation = 5;
        isMoving = false;
        for (int i = 1; i <= SIZE; i++) {
            for (int j = 1; j <= SIZE; j++) {
                Piece p = grid[i][j];
                int dx = 0, dy = 0;
                for (int n = 0; n < speedSwapAnimation; n++) {
                    dx = p.getX() - p.getCol() * TILE_SIZE;
                    dy = p.getY() - p.getRow() * TILE_SIZE;
                    if (dx != 0) p.x -= dx / Math.abs(dx);
                    if (dy != 0) p.y -= dy / Math.abs(dy);
                }
                if (dx != 0 || dy != 0)
                    isMoving = true;
            }
        }
    }

    // Processes matches, removes matched gems, and refills the board,calculate score
    private int processMatches() {
        int score = 0;
        // Create a set to track which gems have already been scored to avoid double counting
        boolean[][] scored = new boolean[SIZE + 2][SIZE + 2];

        // Calculate score for each unique match
        for (int i = 1; i <= SIZE; i++) {
            for (int j = 1; j <= SIZE; j++) {
                if (grid[i][j].match > 0 && !scored[i][j]) {
                    int matchLength = grid[i][j].match;

                    // Base score: 100 points for 3-gem match
                    int matchScore = 10;

                    // Add 10% bonus for each gem beyond 3
                    if (matchLength > 3) {
                        double bonusMultiplier = 1.0 + (matchLength - 3) * 0.1;
                        matchScore = (int)(matchScore * bonusMultiplier);
                    }
                    score += matchScore;

                    // Mark all gems in this match as scored
                    markMatchAsScored(i, j, matchLength, scored);
                }
            }
        }

        if (isSwap && !isMoving) {
            if (score == 0) {
                // Invalid move, swap back
                swap(grid[y0][x0], grid[y][x]);
            }
            isSwap = false;
        }

        if (!isMoving && score > 0) {
            // Move gems down to fill gaps
            for (int i = SIZE; i > 0; i--) {
                for (int j = 1; j <= SIZE; j++) {
                    if (grid[i][j].match != 0) {
                        for (int n = i; n > 0; n--) {
                            if (grid[n][j].match == 0) {
                                swap(grid[n][j], grid[i][j]);
                                break;
                            }
                        }
                    }
                }
            }

            // Generate new gems for matched ones
            for (int j = 1; j <= SIZE; j++) {
                for (int i = SIZE, n = 0; i > 0; i--) {
                    if (grid[i][j].match != 0) {
                        grid[i][j].setKind(rand.nextInt(GEM_TYPES));
                        grid[i][j].y = -TILE_SIZE * n++;
                        grid[i][j].match = 0;
                    }
                }
            }
        }
        return score;
    }

    // Helper method to mark all gems in a match as scored
    private void markMatchAsScored(int startRow, int startCol, int matchLength, boolean[][] scored) {
        // Check if this is a horizontal match
        boolean isHorizontalMatch = false;
        if (startCol + matchLength - 1 <= SIZE) {
            isHorizontalMatch = true;
            for (int k = 1; k < matchLength; k++) {
                if (startCol + k > SIZE || grid[startRow][startCol + k].match != matchLength) {
                    isHorizontalMatch = false;
                    break;
                }
            }
        }

        if (isHorizontalMatch) {
            // Mark horizontal match
            for (int k = 0; k < matchLength; k++) {
                if (startCol + k <= SIZE) {
                    scored[startRow][startCol + k] = true;
                }
            }
        } else {
            // Must be vertical match
            for (int k = 0; k < matchLength; k++) {
                if (startRow + k <= SIZE) {
                    scored[startRow + k][startCol] = true;
                }
            }
        }
    }

    // Draws the board and gems to the provided Graphics2D context
    public void draw(Graphics2D g2) {
        for (int i = 1; i <= SIZE; i++) {
            for (int j = 1; j <= SIZE; j++) {
                Piece p = grid[i][j];
                if (p.getKind() != INVALID_KIND) {
                    g2.drawImage(
                            assets.getGems().getSubimage(p.getKind() * 49, 0, 49, 49),
                            p.x + (OFFSET_X - TILE_SIZE),
                            p.y + (OFFSET_Y - TILE_SIZE),
                            50,
                            50,
                            null
                    );

                    if (click == 1 && x0 == j && y0 == i) {
                        g2.drawImage(
                                assets.getCursor(),
                                p.x + (OFFSET_X - TILE_SIZE-2),
                                p.y + (OFFSET_Y - TILE_SIZE+4),
                                assets.getCursor().getWidth(),
                                assets.getCursor().getHeight(),
                                null
                        );
                    }
                }
            }
        }
    }
}