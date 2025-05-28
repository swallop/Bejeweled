import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class Game extends JPanel implements Runnable {
    public static final int WIDTH = 900;
    public static final int HEIGHT = 563;
    private static final int TARGET_FPS = 60;
    private static final long TARGET_FRAME_TIME = 1000 / TARGET_FPS;

    private Thread gameThread;
    private volatile boolean isRunning;
    private Assets assets;
    private Board board;
    private MouseHandler input;

    private GameTimer timer;
    private Leaderboard leaderboard;
    private ScoreManager scoreManager;
    private boolean gameOverHandled = false;

    public Game() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        input = new MouseHandler();
        addMouseListener(input);
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
    }

    private void update() {
        if (timer.isGameOver() && !gameOverHandled) {
            handleGameOver();
            gameOverHandled = true;
            return;
        }

        if (!timer.isGameOver()) {
            MouseEvent e = input.getMouseEvent();
            if (e != null) {
                board.handleMouseInput(e);
                input.consumeMouse();
            }

            int matches = board.update();

            // Fix 1: Use actual elapsed time instead of fixed 0.05
            long currentTime = System.nanoTime();
            double deltaTime = TARGET_FRAME_TIME / 1000.0; // Convert to seconds
            timer.update(deltaTime, matches);

            // Fix 2: Update score when matches occur
            if (matches > 0) {
                scoreManager.update(matches);
            }
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

        // Ask if player wants to play again
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Would you like to play again?",
                "Play Again?",
                JOptionPane.YES_NO_OPTION
        );

        if (choice == JOptionPane.YES_OPTION) {
            restartGame();
        } else {
            stop();
        }
    }

    private void restartGame() {
        timer.reset();
        scoreManager.reset();
        board = new Board(assets); // Reset board
        gameOverHandled = false;
    }

    private void draw() {
        Graphics2D g2 = (Graphics2D) assets.getView().getGraphics();
        try {
            g2.drawImage(assets.getBackground(), 0, 0, WIDTH, HEIGHT, null);
            board.draw(g2);
            timer.draw(g2);
            scoreManager.draw(g2, timer.isGameOver());
            leaderboard.draw(g2);
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
