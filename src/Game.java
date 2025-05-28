import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

// Main game panel handling the game loop, rendering, and input processing
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

    // Constructor sets up the panel and input handler
    public Game() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        input = new MouseHandler();
        addMouseListener(input);
    }

    // Called when the component is added to a container
    @Override
    public void addNotify() {
        super.addNotify();
        if (gameThread == null) {
            gameThread = new Thread(this, "GameThread");
            isRunning = true;
            gameThread.start();
        }
    }

    // Initializes game resources
    public void init() {
        assets = new Assets();
        assets.load();
        board = new Board(assets);
    }

    // Updates game state
    private void update() {
        MouseEvent e = input.getMouseEvent();
        if (e != null) {
            board.handleMouseInput(e);
            input.consumeMouse();
        }
        board.update();
    }

    // Renders the game to the screen
    private void draw() {
        Graphics2D g2 = (Graphics2D) assets.getView().getGraphics();
        try {
            g2.drawImage(assets.getBackground(), 0, 0, WIDTH, HEIGHT, null);
            board.draw(g2);
        } finally {
            g2.dispose(); // Ensure Graphics2D is disposed
        }

        Graphics g = getGraphics();
        if (g != null) {
            try {
                g.drawImage(assets.getView(), 0, 0, WIDTH, HEIGHT, null);
            } finally {
                g.dispose(); // Ensure Graphics is disposed
            }
        } else {
            System.err.println("Warning: Graphics context unavailable, skipping draw.");
        }
    }

    // Main game loop
    @Override
    public void run() {
        init();
        long lastTime = System.nanoTime();
        while (isRunning) {
            long currentTime = System.nanoTime();
            long elapsedTime = (currentTime - lastTime) / 1_000_000; // Convert to ms

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
                System.err.println("Game loop interrupted: " + e.getMessage());
                isRunning = false;
            }
        }
    }

    // Stops the game loop gracefully
    public void stop() {
        isRunning = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            System.err.println("Error stopping game thread: " + e.getMessage());
        }
    }
}