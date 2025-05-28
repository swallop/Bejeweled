import java.awt.Graphics2D;
import java.awt.Color;

public class ScoreManager {
    private static final int POINTS_PER_MATCH = 10;
    private static final int SCORE_X = 645;
    private static final int SCORE_Y = 350;

    private int score;

    public ScoreManager() {
        this.score = 0;
    }

    public void update(int matches) {
        score += matches * POINTS_PER_MATCH;
    }

    public void reset() {
        score = 0;
    }

    public void draw(Graphics2D g2, boolean isGameOver) {
        g2.setColor(Color.YELLOW);
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 20));
        g2.drawString("Score: " + score, SCORE_X, SCORE_Y);
    }

    public int getScore() {
        return score;
    }
}