import java.awt.*;

public class GameTimer {
    private static final double INITIAL_TIME = 20.0;
    private static final double TIME_PER_MATCH = 2.0;
    private static final double SPEED_UP_INTERVAL = 5.0;
    private static final double SPEED_UP_FACTOR = 1.1;
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
            lastSpeedUpTime = 1.0;
        }
        if (matches > 0) {
            timeRemaining += matches * TIME_PER_MATCH / 3.0; // Scale by average match length
            if (timeRemaining > INITIAL_TIME) {
                timeRemaining = INITIAL_TIME;
            }
        }
        if (timeRemaining <= 0) {
            timeRemaining = 0;
            isGameOver = true;
        }
    }

    private Color getBarColor(double timeRatio) {
        if (timeRatio > 1.0) return Color.BLUE;
        if (timeRatio > 0.3) {
            // Interpolate between GREEN and ORANGE
            float ratio = (float)((timeRatio - 0.3) / 0.7);
            int red = (int)(Color.GREEN.getRed() * ratio + Color.ORANGE.getRed() * (1 - ratio));
            int green = (int)(Color.GREEN.getGreen() * ratio + Color.ORANGE.getGreen() * (1 - ratio));
            int blue = (int)(Color.GREEN.getBlue() * ratio + Color.ORANGE.getBlue() * (1 - ratio));
            return new Color(red, green, blue);
        }
        return Color.ORANGE;
    }

    public void draw(Graphics2D g2) {
        g2.setColor(Color.GRAY);
        g2.fillRect(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT);

        double timeRatio = Math.min(timeRemaining / INITIAL_TIME, 1.0);
        int filledWidth = (int) (BAR_WIDTH * timeRatio);

        g2.setColor(getBarColor(timeRatio));
        g2.fillRect(BAR_X, BAR_Y, filledWidth, BAR_HEIGHT);
        g2.setColor(Color.BLACK);
        g2.drawRect(BAR_X, BAR_Y, BAR_WIDTH, BAR_HEIGHT);

        g2.setColor(Color.GREEN);
        g2.setFont(new java.awt.Font("Consolas", java.awt.Font.BOLD, 15));
        String timeText = String.format("Time: %.1fs", timeRemaining);
        g2.drawString(timeText, 645, 330);

        if (isGameOver) {
            g2.setColor(Color.RED);
            g2.setFont(new java.awt.Font("Consolas", java.awt.Font.BOLD, 40));
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