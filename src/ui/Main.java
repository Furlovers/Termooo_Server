package ui;

import java.awt.Color;

import javax.swing.JFrame;

public class Main extends JFrame {

    private GameScreenNova gameScreen;

    public Main() {
        gameScreen = new GameScreenNova();
        gameScreen.setBackground(Color.DARK_GRAY);
        add(gameScreen);

        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
}