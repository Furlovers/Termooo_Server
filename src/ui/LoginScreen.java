package ui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.swing.*;

public class LoginScreen extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public LoginScreen() {
        setTitle("Login");
        setLayout(new GridBagLayout()); // Usando GridBagLayout para melhor controle de layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Configurando o fundo e as cores
        getContentPane().setBackground(Color.DARK_GRAY);

        // Campo de usuário
        JLabel usernameLabel = new JLabel("Usuário:");
        usernameLabel.setForeground(Color.WHITE); // Fonte branca
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10); // Espaçamento
        gbc.anchor = GridBagConstraints.CENTER;
        add(usernameLabel, gbc);

        usernameField = new JTextField(15);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 20));
        gbc.gridx = 1;
        add(usernameField, gbc);

        // Campo de senha
        JLabel passwordLabel = new JLabel("Senha:");
        passwordLabel.setForeground(Color.WHITE);
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(passwordLabel, gbc);

        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 20));
        gbc.gridx = 1;
        add(passwordField, gbc);

        // Botão de Login
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 20));
        loginButton.setBackground(Color.BLACK);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(loginButton, gbc);

        // Status do login
        statusLabel = new JLabel("Entre com suas credenciais", JLabel.CENTER);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(statusLabel, gbc);

        // Ação do botão de login
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    connectToServer();
                    String username = usernameField.getText();
                    String password = new String(passwordField.getPassword());

                    // Criptografar a senha usando SHA-256
                    String encryptedPassword = encryptPassword(password);

                    out.println("LOGIN:" + username + ":" + encryptedPassword);
                    String response = in.readLine();

                    if (response.equals("SUCCESS")) {
                        statusLabel.setText("Login bem-sucedido!");
                        openGameScreen(socket, in, out);
                    } else {
                        statusLabel.setText("Falha no login. Tente novamente.");
                    }

                } catch (IOException | NoSuchAlgorithmException ex) {
                    statusLabel.setText("Erro: " + ex.getMessage());
                }
            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Maximiza a janela ao abrir
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    private void connectToServer() throws IOException {
        socket = new Socket("localhost", 3305);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
    }

    private String encryptPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    // Método para abrir a tela de jogo usando a conexão existente
    private void openGameScreen(Socket socket, BufferedReader in, PrintWriter out) {
        dispose(); // Fecha a tela de login
        GameScreenNova gameScreen = new GameScreenNova(socket, in, out); // Passa a conexão
        JFrame gameFrame = new JFrame();
        gameFrame.add(gameScreen);

        // Configura a janela para tela cheia
        gameFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setVisible(true);
    }
}
