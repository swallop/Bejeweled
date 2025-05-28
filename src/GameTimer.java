import java.awt.*;

public class GameTimer {
    private static final double INITIAL_TIME = 30.0;
    private static final double TIME_PER_MATCH = 2.0;
    private static final double SPEED_UP_INTERVAL = 5.0;
    private static final double SPEED_UP_FACTOR = 1.05;
    private static final int BAR_WIDTH = 375;
    private static final int BAR_HEIGHT = 15;
    private static final int BAR_X = 85;
    private static final int BAR_Y = 493;

    private double timeRemaining;
    private double baseSpeed;
    private double currentSpeed;
    private double lastSpeedUpTime;
    private boolean isGameOver;

    public GameTimer() {
        this.timeRemaining = INITIAL_TIME;
        this.baseSpeed = 1.0;
        this.currentSpeed = baseSpeed;
        this.lastSpeedUpTime = 0.0;
        this.isGameOver = false;
    }

    public void update(double deltaTime, int matches) {
        if (isGameOver) return;

        timeRemaining -= deltaTime * currentSpeed;

        lastSpeedUpTime += deltaTime;
        if (lastSpeedUpTime >= SPEED_UP_INTERVAL) {
            currentSpeed *= SPEED_UP_FACTOR;
            lastSpeedUpTime = 0.0;
        }

        // Fix 3: Correct time bonus calculation
        if (matches > 0) {
            timeRemaining += matches * TIME_PER_MATCH; // Was: matches + INITIAL_TIME
            if (timeRemaining > INITIAL_TIME * 2) {
                timeRemaining = INITIAL_TIME * 2;
            }
        }

        if (timeRemaining <= 0) {
            timeRemaining = 0;
            isGameOver = true;
        }
    }

    public void draw(Graphics2D g2) {
        g2.setColor(Color.GRAY);
        g2.fillRect(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT);

        double timeRatio = Math.min(timeRemaining / INITIAL_TIME, 1.0);
        int filledWidth = (int) (BAR_WIDTH * timeRatio);

        if (timeRemaining > INITIAL_TIME) {
            g2.setColor(Color.BLUE);
        } else if (timeRemaining > INITIAL_TIME * 0.3) {
            g2.setColor(Color.GREEN);
        } else {
            g2.setColor(Color.ORANGE);
        }

        g2.fillRect(BAR_X, BAR_Y, filledWidth, BAR_HEIGHT);
        g2.setColor(Color.BLACK);
        g2.drawRect(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT);

        g2.setColor(Color.GREEN);
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 15));
        String timeText = String.format("Time: %.1fs", timeRemaining);
        g2.drawString(timeText, 645, 330);

        if (isGameOver) {
            g2.setColor(Color.RED);
            g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 40));
            g2.drawString("Game Over", 350, 300);
        }
    }

    public boolean isGameOver() { return isGameOver; }
    public double getTimeRemaining() { return timeRemaining; }
    public double getCurrentSpeed() { return currentSpeed; }

    public void reset() {
        this.timeRemaining = INITIAL_TIME;
        this.currentSpeed = baseSpeed;
        this.lastSpeedUpTime = 0.0;
        this.isGameOver = false;
    }
}