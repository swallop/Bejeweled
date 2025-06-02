import java.awt.Graphics2D;
import java.awt.Color;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Leaderboard {
    private static final int MAX_SCORES = 5;
    private static final String LEADERBOARD_FILE = "leaderboard.txt";

    private List<ScoreEntry> scores;

    public static class ScoreEntry implements Comparable<ScoreEntry> {
        public final String name;
        public final int score;

        public ScoreEntry(String name, int score) {
            this.name = name;
            this.score = score;
        }

        @Override
        public int compareTo(ScoreEntry other) {
            return Integer.compare(other.score, this.score); // Descending order
        }

        @Override
        public String toString() {
            return name + "," + score;
        }

        public static ScoreEntry fromString(String line) {
            String[] parts = line.split(",");
            if (parts.length == 2) {
                try {
                    return new ScoreEntry(parts[0], Integer.parseInt(parts[1]));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        }
    }

    public Leaderboard() {
        scores = new ArrayList<>();
        loadScores();
    }

    public void addScore(int score, String playerName) {
        scores.add(new ScoreEntry(playerName, score));
        Collections.sort(scores);
        if (scores.size() > MAX_SCORES) {
            scores = scores.subList(0, MAX_SCORES);
        }
        saveScores();
    }

    private void loadScores() {
        try (BufferedReader reader = new BufferedReader(new FileReader(LEADERBOARD_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                ScoreEntry entry = ScoreEntry.fromString(line);
                if (entry != null) {
                    scores.add(entry);
                }
            }
            Collections.sort(scores);
        } catch (IOException e) {
            // File doesn't exist or can't be read, start with empty leaderboard
            System.out.println("No existing leaderboard found, starting fresh.");
        }
    }

    private void saveScores() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LEADERBOARD_FILE))) {
            for (ScoreEntry entry : scores) {
                writer.println(entry.toString());
            }
        } catch (IOException e) {
            System.err.println("Error saving leaderboard: " + e.getMessage());
        }
    }

    public void draw(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 15));
        g2.drawString("Leaderboard", 645, 50);

        int y = 80;
        for (int i = 0; i < scores.size(); i++) {
            ScoreEntry entry = scores.get(i);
            String text = String.format("%d. %s - %d", i + 1, entry.name, entry.score);
            g2.drawString(text, 645, y);
            y += 25;
        }
    }

}