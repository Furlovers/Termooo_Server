package ui;

import javax.swing.JOptionPane;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import entities.Letter;
import entities.Square;
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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameScreenNova extends JPanel implements KeyListener, ActionListener {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private int cellSize = 64;
    private int squareIndex = 0;
    private int wordIndex = 1;
    private boolean canWrite = true;
    private boolean win = false;
    private JButton submitButton;

    private List<List<Letter>> words = Arrays.asList(
            Arrays.asList(new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' ')),
            Arrays.asList(new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' ')),
            Arrays.asList(new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' ')),
            Arrays.asList(new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' ')),
            Arrays.asList(new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' ')),
            Arrays.asList(new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' '), new Letter(' '))
    );

    private List<Letter> letters = Arrays.asList(new Letter('A'), new Letter('B'), new Letter('C'), new Letter('D'),
            new Letter('E'), new Letter('F'), new Letter('G'), new Letter('H'), new Letter('I'), new Letter('J'),
            new Letter('K'), new Letter('L'), new Letter('M'), new Letter('N'), new Letter('O'), new Letter('P'),
            new Letter('Q'), new Letter('R'), new Letter('S'), new Letter('T'), new Letter('U'), new Letter('V'),
            new Letter('W'), new Letter('X'), new Letter('Y'), new Letter('Z'));

    public GameScreenNova() {
        setLayout(null);
        drawButton();
        setFocusable(true);
        addKeyListener(this);
        connectToServer(); // Estabelece a conexão com o servidor ao inicializar
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 3305);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            System.out.println("Conectado ao servidor");
        } catch (Exception e) {
            e.printStackTrace();
        }
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

            out.println(guessedWord.toString()); // Envia a palavra ao servidor
            receiveServerResponse(); // Recebe o feedback do servidor

            // Após a tentativa, incrementa o wordIndex para a próxima linha do tabuleiro
            wordIndex++;
            squareIndex = (wordIndex - 1) * 5; // Reinicia o squareIndex para o início da nova linha
        }
    }

    private void receiveServerResponse() {
        try {
            String response = in.readLine();
            System.out.println("Resposta do servidor: " + response);
    
            // Processa a resposta do servidor, que contém as letras e seus estados
            updateLettersWithResponse(response);
    
            // Redesenha a tela com o novo estado das letras
            repaint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updateLettersWithResponse(String response) {
        // Exemplo de resposta: "A:DISCOVERED_AND_RIGHT,B:WRONG,C:DISCOVERED_AND_WRONG,D:WRONG,E:DISCOVERED_AND_RIGHT"
        String[] letterStates = response.split(",");
        
        List<Letter> currentWord = words.get(wordIndex - 1);
        
        for (int i = 0; i < letterStates.length; i++) {
            String[] parts = letterStates[i].split(":");
            char letterChar = parts[0].charAt(0);
            String stateString = parts[1];
            
            // Atualiza a letra e seu estado com base na resposta do servidor
            Letter letter = currentWord.get(i);
            letter.setLetter(letterChar);
            letter.setState(StateEnum.valueOf(stateString));
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        char typedChar = e.getKeyChar();

        // Verifica se a tecla Enter foi pressionada e submete a palavra
        if (typedChar == KeyEvent.VK_ENTER) {
            submitButton.doClick(); // Simula o clique no botão de submissão
        }

        // Se a tecla pressionada for Backspace
        if (typedChar == KeyEvent.VK_BACK_SPACE) {
            if (squareIndex > (wordIndex - 1) * 5) {
                squareIndex--;
                words.get(wordIndex - 1).get(squareIndex % 5).setLetter(' '); // Apaga a letra
                canWrite = true; // Permitir que o jogador continue escrevendo após apagar
                repaint();
            }
        } 
        // Certifica-se de que apenas letras são processadas e que o jogador pode escrever
        else if (Character.isLetter(typedChar) && canWrite) {
            // Atualiza a letra na posição correta da linha atual (determinado por wordIndex)
            words.get(wordIndex - 1).get(squareIndex % 5).setLetter(Character.toUpperCase(typedChar));
            squareIndex++;

            // Quando o jogador completar uma palavra (5 letras), ele não pode mais escrever até submeter
            if (squareIndex % 5 == 0) {
                canWrite = false;
            }
            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) { }
    @Override
    public void keyReleased(KeyEvent e) { }

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


    public void drawLetters(Graphics g) {

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
}
