package ui;

import javax.swing.JOptionPane;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import entities.Letter;
import entities.Square;
import helpers.ConnectionDB;
import helpers.StateEnum;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameScreen extends JPanel implements KeyListener, ActionListener {

    /*
     * Classe de criação da interface gráfica para o jogo. Implementa os métodos de
     * exibição, reconhecimento de entradas e formatação para o tabuleiro.
     */

    private String word;
    private int cellSize = 64;
    private int squareIndex = 0;
    private int wordIndex = 1;
    private ArrayList<Square> squares;
    private JButton submitButton;
    private boolean canWrite = true;
    private boolean win = false;
    private List<List<Letter>> words = Arrays.asList(
            Arrays.asList(new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' ')),
            Arrays.asList(new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' ')),
            Arrays.asList(new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' ')),
            Arrays.asList(new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' ')),
            Arrays.asList(new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' ')),
            Arrays.asList(new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' ')));

    private List<Letter> letters = Arrays.asList(new Letter('A'), new Letter('B'), new Letter('C'), new Letter('D'),
            new Letter('E'), new Letter('F'), new Letter('G'), new Letter('H'), new Letter('I'), new Letter('J'),
            new Letter('K'), new Letter('L'), new Letter('M'), new Letter('N'), new Letter('O'), new Letter('P'),
            new Letter('Q'), new Letter('R'), new Letter('S'), new Letter('T'), new Letter('U'), new Letter('V'),
            new Letter('W'), new Letter('X'), new Letter('Y'), new Letter('Z'));

    public GameScreen() {
        setLayout(null);
        drawButton();
        setFocusable(true);
        addKeyListener(this);
        try {
            Connection con = new ConnectionDB().connect();
            word = ConnectionDB.getWord(con).toUpperCase();
            System.err.println("Palavra: " + word);
        } catch (Exception e) {
            System.err.println("Palavra não encontrada");
        }
    }

    public void paintComponent(Graphics g) {

        /*
         * Utiliza os métodos drawetter e drawSquare para exibir ao
         * usuário o resultado da rodada atual do jogo.
         */
        super.paintComponent(g);
        if (squareIndex == 0) {
            squares = getSquares();
        }

        g.setFont(new Font("Arial", Font.BOLD, 40));

        drawLettters(g);

        words.forEach(word -> {
            for (int i = 0; i < word.size(); i++) {
                drawSquare(g, squares.get(i + (5 * (words.indexOf(word)))), word.get(i));
            }
        });
    }

    @Override
    public void keyTyped(KeyEvent e) {

        /*
         * Método para reagir ao evento de digitação de uma tecla
         * pelo usuário. Verifica qual a rodada corrente da partida
         * para determinar à partir de qual quadrado deverá ser iniciada
         * a impressão de novos caracteres na tela. Verifica se o usuário
         * pode escrever (ou seja, se naquela rodada digitou menos que 5 letras)
         * e restringe os caracteres possíveis de serem digitados para apenas letras
         * maiúsculas ou minúsculas.
         */
        if (e.getKeyChar() == KeyEvent.VK_ENTER) {
            submitButton.doClick();
        }

        char typedChar = e.getKeyChar();
        String typedCharString = String.valueOf(typedChar).toUpperCase();
        typedChar = typedCharString.charAt(0);
        if (squareIndex <= squares.size() && !win) {
            if ((int) e.getKeyChar() == 8) { // backspace code
                if (squareIndex <= 0) {
                    return;
                }
                switch (wordIndex) {
                    case 2:
                        if (squareIndex == 5) {
                            return;
                        }
                        break;
                    case 3:
                        if (squareIndex == 10) {
                            return;
                        }
                        break;
                    case 4:
                        if (squareIndex == 15) {
                            return;
                        }
                        break;
                    case 5:
                        if (squareIndex == 20) {
                            return;
                        }
                        break;

                    case 6:
                        if (squareIndex == 25) {
                            return;
                        }
                        break;

                }
                if (squareIndex % 5 == 0) {
                    canWrite = true;
                }
                if ((int) e.getKeyChar() == 10) {

                }
                squareIndex--;
                words.get(wordIndex - 1).get(squareIndex - (5 * (wordIndex - 1))).setLetter(' ');
                squares.get(squareIndex).setLetter(' ');
            } else {
                if (squareIndex == squares.size() || !String.valueOf(typedChar).matches("[a-zA-Z]") || !canWrite) {
                    return;
                }
                words.get(wordIndex - 1).get(squareIndex - (5 * (wordIndex - 1))).setLetter(typedChar);
                squares.get(squareIndex).setLetter(typedChar);
                squareIndex++;
                if (squareIndex % 5 == 0) {
                    canWrite = false;
                }
            }
            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent arg0) {
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
    }

    public ArrayList<Square> getSquares() {

        /*
         * Prepara um ArrayList de objetos "Square", definindo o componente
         * sobre o qual serão "desenhados" de fato os quadrados do tabuleiro.
         * Utiliza-se os métodos getWidth() e getHeight() para determinar as
         * dimensões da tela de exibição e então, a partir do número de linhas
         * e colunas e do espaçamento entre os quadrados, demarca-se o tabuleiro.
         */

        ArrayList<Square> squares = new ArrayList<Square>();

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        int gridWidth = 5;
        int gridHeight = 6;
        int cellSpacing = 8;

        int gridTotalWidth = gridWidth * cellSize + (gridWidth - 1) * cellSpacing;
        int gridTotalHeight = gridHeight * cellSize + (gridHeight - 1) * cellSpacing;

        int startX = (panelWidth - gridTotalWidth) / 2;
        int startY = (panelHeight - gridTotalHeight) / 3;

        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                Square square = new Square(startX + x * (cellSize + cellSpacing),
                        startY + y * (cellSize + cellSpacing));
                squares.add(square);
            }
        }

        return squares;
    }

    public void drawSquare(Graphics g, Square square, Letter letter) {

        /*
         * Desenha um quadrado na tela a partir dos atributos de um objeto
         * "Square". Aplica uma formatação condicional com base no estado da
         * objeto "Letter" represntado naquele quadrado.
         */

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

    @Override
    public void actionPerformed(ActionEvent e) {

        /*
         * Método para tratar eventos gerados pelo botão de submissão
         * de palavras. Confirma o envio da palavra informada pelo usuário
         * e aplica sobre ela a lógica de validação, verificando o estado
         * de cada letra. Caso todas as letras estejam corretas, o jogador
         * venceu naquela rodada.
         */

        if (e.getSource() == submitButton) {
            canWrite = true;
            submitButton.setFocusable(false);

            int targetSquareIndex = (wordIndex - 1) * 5 + 5;

            if (squareIndex == targetSquareIndex && !win) {
                win = true;
                List<Letter> currentWord = words.get(wordIndex - 1);

                for (int i = 0; i < currentWord.size(); i++) {
                    char guessedChar = currentWord.get(i).getLetter();
                    char actualChar = word.charAt(i);

                    if (guessedChar == actualChar) {
                        currentWord.get(i).setState(StateEnum.DISCOVERED_AND_RIGHT);
                    } else {
                        win = false;
                    }
                }

                for (int i = 0; i < currentWord.size(); i++) {
                    Letter currentLetter = currentWord.get(i);
                    if (currentLetter.getStatesEnum() != StateEnum.DISCOVERED_AND_RIGHT) {
                        char guessedChar = currentLetter.getLetter();
                        if (word.contains(String.valueOf(guessedChar))) {
                            long greenCountInWord = currentWord.stream()
                                    .filter(letter -> letter.getLetter() == guessedChar
                                            && letter.getStatesEnum() == StateEnum.DISCOVERED_AND_RIGHT)
                                    .count();
                            long totalCountInAnswer = word.chars().filter(ch -> ch == guessedChar).count();
                            if (totalCountInAnswer > greenCountInWord) {
                                currentLetter.setState(StateEnum.DISCOVERED_AND_WRONG);
                            } else {
                                currentLetter.setState(StateEnum.WRONG);
                            }
                        } else {
                            currentLetter.setState(StateEnum.WRONG);
                        }
                    }
                }

                for (Letter letter : currentWord) {
                    for (Letter keyboardLetter : letters) {
                        if (keyboardLetter.getLetter() == letter.getLetter()) {
                            if (letter.getStatesEnum() == StateEnum.DISCOVERED_AND_RIGHT) {
                                keyboardLetter.setState(StateEnum.DISCOVERED_AND_RIGHT);
                            } else if (letter.getStatesEnum() == StateEnum.DISCOVERED_AND_WRONG
                                    && keyboardLetter.getStatesEnum() != StateEnum.DISCOVERED_AND_RIGHT) {
                                keyboardLetter.setState(StateEnum.DISCOVERED_AND_WRONG);
                            } else if (keyboardLetter.getStatesEnum() == StateEnum.UNDISCOVERED) {
                                keyboardLetter.setState(StateEnum.WRONG);
                            }
                        }
                    }
                }

                if (win) {
                    showFinalDialog("Parabéns, você acertou!", "Vitória", JOptionPane.INFORMATION_MESSAGE);
                } else if (wordIndex == 6) {
                    showFinalDialog("Que pena, você não acertou! A palavra era: " + word, "Derrota",
                            JOptionPane.ERROR_MESSAGE);
                }

                wordIndex++;
                repaint();
            }
        }
    }

    public void drawButton() {

        /*
         * Implementa um botão "Enter", a fim de confirmar a palavra
         * digitada pelo usuário e então aplicar a lógica de verificação
         * para aquela rodada.
         */

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = (int) screenSize.getWidth();
        int screenHeight = (int) screenSize.getHeight();

        int buttonWidth = 200;
        int buttonHeight = 50;

        submitButton = new JButton("Enter");
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

    public void drawLettters(Graphics g) {

        /*
         * Imprime uma letra em um quadrado do
         * tabuleiro do jogo a partir do atributo 'letter'
         * do objeto "Letter". É aplicada uma formatação
         * condicional para a cor do quadrado do tabuleiro
         * em função do atributo 'state' do objeto "Letter".
         * 
         * Ademais, posiciona-se adequadamente cada letra
         * nos quadrados do tabuleiro a partir da linha e coluna
         * de cada letra, definidos através de seu índice.
         */

        super.paintComponent(g);
        int startX = 40;
        int startY = 60;
        int spacingX = 40;
        int spacingY = 40;
        int columns = 13;

        letters.forEach(letter -> {
            if (letter.getStatesEnum() == StateEnum.DISCOVERED_AND_RIGHT) {
                g.setColor(Color.GREEN);
            } else if (letter.getStatesEnum() == StateEnum.DISCOVERED_AND_WRONG) {
                g.setColor(Color.YELLOW);
            } else if (letter.getStatesEnum() == StateEnum.WRONG) {
                g.setColor(Color.DARK_GRAY);
            } else {
                g.setColor(Color.BLACK);
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
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);

        try {
            Connection con = new ConnectionDB().connect();
            word = ConnectionDB.getWord(con).toUpperCase();
            System.err.println("Palavra: " + word);
        } catch (Exception e) {
            System.err.println("Palavra não encontrada");
        }
        squareIndex = 0;
        wordIndex = 0;
        canWrite = true;
        win = false;
        words = Arrays.asList(
                Arrays.asList(new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' ')),
                Arrays.asList(new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' ')),
                Arrays.asList(new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' ')),
                Arrays.asList(new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' ')),
                Arrays.asList(new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' ')),
                Arrays.asList(new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' ')));

        letters = Arrays.asList(new Letter('A'), new Letter('B'), new Letter('C'), new Letter('D'),
                new Letter('E'), new Letter('F'), new Letter('G'), new Letter('H'), new Letter('I'), new Letter('J'),
                new Letter('K'), new Letter('L'), new Letter('M'), new Letter('N'), new Letter('O'), new Letter('P'),
                new Letter('Q'), new Letter('R'), new Letter('S'), new Letter('T'), new Letter('U'), new Letter('V'),
                new Letter('W'), new Letter('X'), new Letter('Y'), new Letter('Z'));

        JOptionPane.showMessageDialog(this, panel, title, messageType);
    }
}
