import javax.swing.*;
public class Main {
    public static void main(String[] args) {
        JFrame window = new JFrame("Bejeweled");
        window.setResizable(false);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.add(new Game());
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}