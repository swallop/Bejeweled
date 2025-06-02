import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public class ButtonPanel extends JPanel {
    private final Game game;
    private final Rectangle startButtonRect;
    private final Rectangle resetButtonRect;
    private final Rectangle stopButtonRect;
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 30;
    private static final int BUTTON_GAP = 10;
    private static final int PANEL_HEIGHT = 50;

    public ButtonPanel(Game game) {
        this.game = game;
        setPreferredSize(new Dimension(Game.WIDTH, PANEL_HEIGHT));
        setBackground(Color.DARK_GRAY);

        // Define button rectangles (right-aligned)
        int startX = Game.WIDTH - (3 * BUTTON_WIDTH + 2 * BUTTON_GAP) - 10; // 10px padding from right
        startButtonRect = new Rectangle(startX, 10, BUTTON_WIDTH, BUTTON_HEIGHT);
        resetButtonRect = new Rectangle(startX + BUTTON_WIDTH + BUTTON_GAP, 10, BUTTON_WIDTH, BUTTON_HEIGHT);
        stopButtonRect = new Rectangle(startX + 2 * (BUTTON_WIDTH + BUTTON_GAP), 10, BUTTON_WIDTH, BUTTON_HEIGHT);

        // Add mouse listener for button clicks
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (game.getGameState() == Game.GameState.STOPPED || game.getGameState() == Game.GameState.PAUSED) {
                    if (startButtonRect.contains(e.getPoint())) {
                        game.startGame();
                    }
                }
                if (resetButtonRect.contains(e.getPoint())) {
                    game.resetGame();
                }
                if (game.getGameState() == Game.GameState.STARTED && stopButtonRect.contains(e.getPoint())) {
                    game.stopGame();
                }
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Draw Start button
        g2.setColor(game.getGameState() == Game.GameState.STOPPED || game.getGameState() == Game.GameState.PAUSED ? Color.GREEN : Color.GRAY);
        g2.fillRect(startButtonRect.x, startButtonRect.y, startButtonRect.width, startButtonRect.height);
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Consolas", Font.BOLD, 14));
        g2.drawString("Start", startButtonRect.x + 30, startButtonRect.y + 20);

        // Draw Reset button
        g2.setColor(Color.YELLOW);
        g2.fillRect(resetButtonRect.x, resetButtonRect.y, resetButtonRect.width, resetButtonRect.height);
        g2.setColor(Color.BLACK);
        g2.drawString("Reset", resetButtonRect.x + 30, resetButtonRect.y + 20);

        // Draw Stop button
        g2.setColor(game.getGameState() == Game.GameState.STARTED ? Color.RED : Color.GRAY);
        g2.fillRect(stopButtonRect.x, stopButtonRect.y, stopButtonRect.width, stopButtonRect.height);
        g2.setColor(Color.BLACK);
        g2.drawString("Stop", stopButtonRect.x + 30, stopButtonRect.y + 20);
    }
}