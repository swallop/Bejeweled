import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class Game extends JPanel implements Runnable {
    public static final int WIDTH = 900;
    public static final int HEIGHT = 563;
    public static final int BUTTON_PANEL_HEIGHT = 50; // Height of ButtonPanel
    private static final int TARGET_FPS = 60;
    private static final long TARGET_FRAME_TIME = 1000 / TARGET_FPS;

    public enum GameState {
        STOPPED, STARTED, PAUSED
    }

    private Thread gameThread;
    private volatile boolean isRunning;
    private GameState gameState;
    private Assets assets;
    private Board board;
    private MouseHandler input;
    private KeyHandler keyInput;
    private GameTimer timer;
    private Leaderboard leaderboard;
    private ScoreManager scoreManager;
    private boolean gameOverHandled = false;

    public Game() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(WIDTH, HEIGHT + BUTTON_PANEL_HEIGHT));
        setFocusable(true);
        input = new MouseHandler();
        addMouseListener(input);
        keyInput = new KeyHandler(this);
        addKeyListener(keyInput);
        gameState = GameState.STOPPED;
        add(new ButtonPanel(this), BorderLayout.SOUTH); // MODIFIED: Changed to SOUTH
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (gameThread == null) {
            gameThread = new Thread(this, "GameThread");
            isRunning = true;
            gameThread.start();
        }
    }

    public void init() {
        assets = new Assets();
        assets.load();
        board = new Board(assets);
        timer = new GameTimer();
        leaderboard = new Leaderboard();
        scoreManager = new ScoreManager();
        gameOverHandled = false;
        gameState = GameState.STOPPED;
    }

    public void startGame() {
        if (gameState == GameState.STOPPED || gameState == GameState.PAUSED) {
            if (gameState == GameState.STOPPED) {
                init();
            }
            gameState = GameState.STARTED;
            gameOverHandled = false;
            requestFocus();
        }
    }

    public void resetGame() {
        init();
        gameState = GameState.STOPPED;
        repaint();
    }

    public void stopGame() {
        if (gameState == GameState.STARTED) {
            gameState = GameState.PAUSED;
        }
    }

    public GameState getGameState() {
        return gameState;
    }

    private void update() {
        if (gameState != GameState.STARTED || timer.isGameOver()) {
            if (timer.isGameOver() && !gameOverHandled) {
                handleGameOver();
                gameOverHandled = true;
            }
            return;
        }

        MouseEvent e = input.getMouseEvent();
        if (e != null) {
            board.handleMouseInput(e);
            input.consumeMouse();
        }

        int matches = board.update();
        long currentTime = System.nanoTime();
        double deltaTime = TARGET_FRAME_TIME / 1000.0;
        timer.update(deltaTime, matches);
        if (matches > 0) {
            scoreManager.addScore(matches);
        }
    }

    private void handleGameOver() {
        // Get player name for leaderboard
        String playerName = JOptionPane.showInputDialog(
                this,
                "Game Over! Your score: " + scoreManager.getScore() + "\nEnter your name:",
                "Game Over",
                JOptionPane.PLAIN_MESSAGE
        );

        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Anonymous";
        }

        // Add score to leaderboard with name
        leaderboard.addScore(scoreManager.getScore(), playerName.trim());
        gameState = GameState.STOPPED;

        // Ask if player wants to play again
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Would you like to play again?",
                "Play Again?",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            resetGame();
        } else {
            stop();
        }
    }

    private void draw() {
        Graphics2D g2 = (Graphics2D) assets.getView().getGraphics();
        try {
            g2.drawImage(assets.getBackground(), 0, 0, WIDTH, HEIGHT, null);
            board.draw(g2);
            timer.draw(g2);
            scoreManager.draw(g2, timer.isGameOver());
            leaderboard.draw(g2);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Consolas", Font.BOLD, 30));
            if (gameState == GameState.STOPPED) {
                g2.drawString("Press Start or Enter to Begin", 250, 300);
            } else if (gameState == GameState.PAUSED) {
                g2.drawString("Game Paused", 350, 300);
            }
        } finally {
            g2.dispose();
        }

        Graphics g = getGraphics();
        if (g != null) {
            try {
                g.drawImage(assets.getView(), 0, 0, WIDTH, HEIGHT, null);
            } finally {
                g.dispose();
            }
        }
    }

    @Override
    public void run() {
        init();
        long lastTime = System.nanoTime();
        while (isRunning) {
            long currentTime = System.nanoTime();
            long elapsedTime = (currentTime - lastTime) / 1_000_000;

            if (elapsedTime >= TARGET_FRAME_TIME) {
                update();
                draw();
                lastTime = currentTime - (elapsedTime % TARGET_FRAME_TIME);
            }

            try {
                long sleepTime = TARGET_FRAME_TIME - elapsedTime;
                if (sleepTime > 0) {
                    Thread.sleep(sleepTime);
                }
            } catch (InterruptedException e) {
                isRunning = false;
            }
        }
    }

    public void stop() {
        isRunning = false;
        try {
            if (gameThread != null) {
                gameThread.join();
            }
        } catch (InterruptedException e) {
            System.err.println("Error stopping game thread: " + e.getMessage());
        }
    }
}