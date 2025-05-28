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
    private final Assets assets;
    private int click = 0;
    private int x0, y0, x, y;
    private boolean isSwap = false;
    private boolean isMoving;

    // Constructor initializes the grid and assets
    public Board(Assets assets) {
        this.assets = assets;
        this.grid = new Piece[SIZE + 2][SIZE + 2];
        initGrid();
    }

    // Initializes the grid with pieces, setting borders to invalid
    private void initGrid() {
        Random rand = new Random();
        for (int i = 0; i < SIZE + 2; i++) {
            for (int j = 0; j < SIZE + 2; j++) {
                grid[i][j] = new Piece();
            }
        }
        for (int i = 1; i <= SIZE; i++) {
            for (int j = 1; j <= SIZE; j++) {
                grid[i][j].setKind(rand.nextInt(GEM_TYPES));
                grid[i][j].setRow(i);
                grid[i][j].setCol(j);
                grid[i][j].setX(j * TILE_SIZE);
                grid[i][j].setY(i * TILE_SIZE);
            }
        }
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
                if (isValidPosition(x, y) && Math.abs(x - x0) + Math.abs(y - y0) == 1) {
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
        for (int i = 1; i <= SIZE; i++) {
            for (int j = 1; j <= SIZE; j++) {
                Piece p = grid[i][j];
                p.match = 0;
                if (p.getKind() == grid[i + 1][j].getKind() && p.getKind() == grid[i - 1][j].getKind()) {
                    grid[i - 1][j].match++;
                    p.match++;
                    grid[i + 1][j].match++;
                }
                if (p.getKind() == grid[i][j + 1].getKind() && p.getKind() == grid[i][j - 1].getKind()) {
                    grid[i][j - 1].match++;
                    p.match++;
                    grid[i][j + 1].match++;
                }
            }
        }
    }

    // Animates piece movement towards their target positions
    private void animateMovement() {
        int speedSwapAnimation = 4;
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
                if (dx != 0 || dy != 0) isMoving = true;
            }
        }
    }

    // Processes matches, removes matched gems, and refills the board
    private int processMatches() {
        int score = 0;
        for (int i = 1; i <= SIZE; i++) {
            for (int j = 1; j <= SIZE; j++) {
                score += grid[i][j].match;
            }
        }

        if (isSwap && !isMoving) {
            if (score == 0) {
                swap(grid[y0][x0], grid[y][x]);
            }
            isSwap = false;
        }

        if (!isMoving) {
            Random rand = new Random();
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
                            49,
                            49,
                            null
                    );

                    if (click == 1 && x0 == j && y0 == i) {
                        g2.drawImage(
                                assets.getCursor(),
                                p.x + (OFFSET_X - TILE_SIZE),
                                p.y + (OFFSET_Y - TILE_SIZE),
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