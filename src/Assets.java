import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

// Manages game assets such as images, ensuring proper loading and access.
public class Assets {
    // Constants for game dimensions
    public static final int WIDTH = 900;
    public static final int HEIGHT = 563;

    // Image assets for the game
    private BufferedImage background;
    private BufferedImage gems;
    private BufferedImage cursor;
    private BufferedImage view;

    // Constructor initializes the view buffer
    public Assets() {
        this.view = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    }

    // Loads all required image assets from the resource directory
    public void load() {
        try {
            background = ImageIO.read(getClass().getResource("/res/background.png"));
            gems = ImageIO.read(getClass().getResource("/res/gems.png"));
            cursor = ImageIO.read(getClass().getResource("/res/cursor.png"));
        } catch (IOException e) {
            System.err.println("Failed to load assets: " + e.getMessage());
            throw new RuntimeException("Asset loading failed, cannot start game.", e);
        }
    }

    // Getter methods for accessing assets
    public BufferedImage getBackground() {
        return background;
    }

    public BufferedImage getGems() {
        return gems;
    }

    public BufferedImage getCursor() {
        return cursor;
    }

    public BufferedImage getView() {
        return view;
    }
}