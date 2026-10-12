import javax.swing.*;
import java.awt.*;

public class MainMenu extends JFrame {

    public MainMenu() {
        setTitle("SKY GLIDER - Endless Flight");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel background = new JPanel();
        background.setLayout(new BoxLayout(background, BoxLayout.Y_AXIS));

        background.setBackground(new Color(15, 35, 75));
        background.setBorder(BorderFactory.createEmptyBorder(100, 50, 70, 50));

        JLabel title = new JLabel("SKY GLIDER");
        title.setFont(new Font("Arial", Font.BOLD, 58));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("ENDLESS FLIGHT");
        subtitle.setFont(new Font("Arial", Font.BOLD, 18));
        subtitle.setForeground(new Color(100, 210, 255));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton playButton = createButton("PLAY GAME");
        JButton instructionsButton = createButton("INSTRUCTIONS");
        JButton exitButton = createButton("EXIT GAME");

        background.add(title);
        background.add(Box.createVerticalStrut(10));
        background.add(subtitle);
        background.add(Box.createVerticalStrut(55));
        background.add(playButton);
        background.add(Box.createVerticalStrut(18));
        background.add(instructionsButton);
        background.add(Box.createVerticalStrut(18));
        background.add(exitButton);

        add(background);

        playButton.addActionListener(e -> {
            dispose();

            SwingUtilities.invokeLater(() -> {
                JFrame frame = new JFrame("SKY GLIDER - Endless Flight");
                SkyGame game = new SkyGame();

                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setContentPane(game);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                game.requestFocusInWindow();
            });
        });

        instructionsButton.addActionListener(e ->
                JOptionPane.showMessageDialog(
                        this,
                        "HOW TO PLAY SKY GLIDER\n\n" +
                                "UP ARROW - Move Up\n" +
                                "DOWN ARROW - Move Down\n" +
                                "LEFT ARROW - Move Left\n" +
                                "RIGHT ARROW - Move Right\n\n" +
                                "Avoid obstacles and collect coins!\n" +
                                "You have 3 lives. Good luck, pilot!",
                        "Instructions",
                        JOptionPane.INFORMATION_MESSAGE
                )
        );

        exitButton.addActionListener(e -> System.exit(0));
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);

        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(25, 125, 210));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(260, 48));
        button.setPreferredSize(new Dimension(260, 48));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainMenu menu = new MainMenu();
            menu.setVisible(true);
        });
    }
}