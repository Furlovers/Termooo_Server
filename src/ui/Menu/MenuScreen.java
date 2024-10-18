package ui.Menu;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JButton;
import javax.swing.JPanel;

public class MenuScreen extends JPanel {
    public MenuScreen() {
        this.setLayout(null);
        this.setSize(1280, 720);
        this.setBackground(Color.DARK_GRAY);
    }

    // ImageIcon icon = new ImageIcon("resources/play_defalut_button.png");
    JButton button = new JButton();

    @Override
    protected void paintComponent(Graphics arg0) {
        add(button);
    }
}
