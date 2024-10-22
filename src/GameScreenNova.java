

import entities.Letter;
import entities.Square;
import helpers.StateEnum;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class GameScreenNova extends JPanel implements KeyListener, ActionListener {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ResourceBundle messages; // Bundle para internacionalização
    private int cellSize = 64;
    private int squareIndex = 0;
    private int wordIndex = 1;
    private boolean canWrite = true;
    private boolean win = false;
    private JButton submitButton;
    private JDialog dialog;

    private List<List<Letter>> words = Arrays.asList(
            Arrays.asList(new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' ')),
            Arrays.asList(new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' ')),
            Arrays.asList(new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' ')),
            Arrays.asList(new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' ')),
            Arrays.asList(new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' ')),
            Arrays.asList(new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' ')));

    private List<Letter> letters = Arrays.asList(
            new Letter('A'), new Letter('B'), new Letter('C'), new Letter('D'),
            new Letter('E'), new Letter('F'), new Letter('G'), new Letter('H'),
            new Letter('I'), new Letter('J'), new Letter('K'), new Letter('L'),
            new Letter('M'), new Letter('N'), new Letter('O'), new Letter('P'),
            new Letter('Q'), new Letter('R'), new Letter('S'), new Letter('T'),
            new Letter('U'), new Letter('V'), new Letter('W'), new Letter('X'),
            new Letter('Y'), new Letter('Z'));

    public GameScreenNova(Socket socket, BufferedReader in, PrintWriter out, ResourceBundle messages) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.messages = messages;

        setBackground(Color.DARK_GRAY);
        setLayout(null);
        drawButton();
        setFocusable(true);
        addKeyListener(this);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        drawLetters(g);

        for (int i = 0; i < words.size(); i++) {
            List<Letter> currentWord = words.get(i);
            for (int j = 0; j < currentWord.size(); j++) {
                drawSquare(g, getSquare(j + (i * 5)), currentWord.get(j));
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == submitButton) {
            canWrite = true;
            submitButton.setFocusable(false);

            StringBuilder guessedWord = new StringBuilder();
            for (Letter letter : words.get(wordIndex - 1)) {
                guessedWord.append(letter.getLetter());
            }

            out.println("GUESS:" + guessedWord.toString());
            System.out.println("Enviou guess");

            receiveServerResponse();

            if (win) {
                showFinalDialog(messages.getString("victory.message"), messages.getString("victory.title"),
                        JOptionPane.INFORMATION_MESSAGE);
            } else if (wordIndex == 6) {
                showFinalDialog(messages.getString("loss.message"), messages.getString("loss.title"),
                        JOptionPane.ERROR_MESSAGE);
            } else {
                wordIndex++;
                squareIndex = (wordIndex - 1) * 5;
            }
        }
    }

    private void receiveServerResponse() {
        try {
            if (socket.isClosed()) {
                System.out.println("Conexão foi fechada pelo servidor.");
                return;
            }

            System.out.println("Esperando resposta do servidor...");
            String response = in.readLine();

            if (response == null) {
                System.out.println("Conexão foi fechada pelo servidor ou não houve resposta.");
                return;
            }

            System.out.println("Resposta do servidor: " + response);
            updateLettersWithResponse(response);

            win = checkIfWin();
            repaint();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateLettersWithResponse(String response) {
        String[] letterStates = response.split(",");
        List<Letter> currentWord = words.get(wordIndex - 1);

        for (int i = 0; i < letterStates.length; i++) {
            String[] parts = letterStates[i].split(":");
            char letterChar = parts[0].charAt(0);
            String stateString = parts[1];

            Letter letter = currentWord.get(i);
            letter.setLetter(letterChar);
            letter.setState(StateEnum.valueOf(stateString));
        }
    }

    private boolean checkIfWin() {
        List<Letter> currentWord = words.get(wordIndex - 1);
        for (Letter letter : currentWord) {
            if (letter.getStatesEnum() != StateEnum.DISCOVERED_AND_RIGHT) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        char typedChar = e.getKeyChar();

        if (typedChar == KeyEvent.VK_ENTER) {
            submitButton.doClick();
        } else if (typedChar == KeyEvent.VK_BACK_SPACE) {
            if (squareIndex > (wordIndex - 1) * 5) {
                squareIndex--;
                words.get(wordIndex - 1).get(squareIndex % 5).setLetter(' ');
                canWrite = true;
                repaint();
            }
        } else if (Character.isLetter(typedChar) && canWrite) {
            words.get(wordIndex - 1).get(squareIndex % 5).setLetter(Character.toUpperCase(typedChar));
            squareIndex++;

            if (squareIndex % 5 == 0) {
                canWrite = false;
            }
            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    private Square getSquare(int index) {
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int gridWidth = 5;
        int gridHeight = 6;
        int cellSpacing = 8;
        int gridTotalWidth = gridWidth * cellSize + (gridWidth - 1) * cellSpacing;
        int gridTotalHeight = gridHeight * cellSize + (gridHeight - 1) * cellSpacing;
        int startX = (panelWidth - gridTotalWidth) / 2;
        int startY = (panelHeight - gridTotalHeight) / 3;

        int xPos = startX + (index % gridWidth) * (cellSize + cellSpacing);
        int yPos = startY + (index / gridWidth) * (cellSize + cellSpacing);

        return new Square(xPos, yPos);
    }

    private void drawSquare(Graphics g, Square square, Letter letter) {
        int arcWidth = 16;
        int arcHeight = 16;

        switch (letter.getStatesEnum()) {
            case UNDISCOVERED:
                g.setColor(Color.black);
                break;
            case WRONG:
                g.setColor(Color.black);
                break;
            case DISCOVERED_AND_WRONG:
                g.setColor(Color.yellow);
                break;
            case DISCOVERED_AND_RIGHT:
                g.setColor(Color.green);
                break;
        }
        g.fillRoundRect(square.getXPos(), square.getYPos(), cellSize, cellSize, arcWidth, arcHeight);

        FontMetrics fm = g.getFontMetrics();
        int letterWidth = fm.stringWidth(String.valueOf(letter.getLetter()));
        int letterHeight = fm.getAscent();

        int xPos = square.getXPos() + (cellSize - letterWidth) / 2;
        int yPos = square.getYPos() + (cellSize + letterHeight) / 2;

        g.setColor(Color.white);
        String letterString = letter.getLetter() == ' ' ? "" : String.valueOf(letter.getLetter());
        g.drawString(letterString, xPos, yPos);
    }

    private void drawButton() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();

        int buttonWidth = 200;
        int buttonHeight = 50;

        submitButton = new JButton(messages.getString("login.button"));
        submitButton.setFont(new Font("Arial", Font.BOLD, 16));
        submitButton.setFocusPainted(false);
        submitButton.setBorderPainted(false);
        submitButton.setBackground(Color.black);
        submitButton.setForeground(Color.WHITE);
        submitButton.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        submitButton.setBounds((screenWidth - buttonWidth) / 2, screenHeight - 200, buttonWidth, buttonHeight);
        submitButton.addActionListener(this);
        add(submitButton);
    }

    public void drawLetters(Graphics g) {
        int startX = 40;
        int startY = 60;
        int spacingX = 40;
        int spacingY = 40;
        int columns = 13;

        letters.forEach(letter -> {
            switch (letter.getStatesEnum()) {
                case DISCOVERED_AND_RIGHT:
                    g.setColor(Color.GREEN);
                    break;
                case DISCOVERED_AND_WRONG:
                    g.setColor(Color.YELLOW);
                    break;
                case WRONG:
                    g.setColor(Color.DARK_GRAY);
                    break;
                default:
                    g.setColor(Color.BLACK);
                    break;
            }

            int letterIndex = letter.getLetter() - 'A';
            int column = letterIndex % columns;
            int row = letterIndex / columns;
            int xPos = startX + column * spacingX;
            int yPos = startY + row * spacingY;
            g.drawString(letter.getLetter() + "", xPos, yPos);
        });
    }

    private void showFinalDialog(String message, String title, int messageType) {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(message);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setForeground(Color.BLACK);
        label.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(label);

        JOptionPane optionPane = new JOptionPane(panel, messageType, JOptionPane.DEFAULT_OPTION, null, new Object[] {},
                null);
        dialog = optionPane.createDialog(this, title);
        dialog.setModal(true);
        dialog.setVisible(true);
    }
}
