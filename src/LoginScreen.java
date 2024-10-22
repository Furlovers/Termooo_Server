import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.*;

public class LoginScreen extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;
    private JComboBox<String> languageSelector;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ResourceBundle messages;

    private Locale selectedLocale;

    public LoginScreen() {
        setTitle("Login");

        // Usando GridBagLayout para melhor controle de layout
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Configurando o fundo e as cores
        getContentPane().setBackground(Color.DARK_GRAY);

        // Adicionando o seletor de idioma
        String[] languages = { "Português", "English", "Español", "Français", "Deutsch" };
        languageSelector = new JComboBox<>(languages);
        languageSelector.setFont(new Font("Arial", Font.PLAIN, 16));
        languageSelector.setSelectedIndex(0); // Português como padrão
        languageSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateLocale(languageSelector.getSelectedIndex());
                updateLabels();
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 20, 10); // Mais espaçamento embaixo do seletor de idiomas
        add(languageSelector, gbc);

        // Campo de usuário
        JLabel usernameLabel = new JLabel();
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.insets = new Insets(5, 10, 5, 10); // Espaçamento entre os elementos
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        add(usernameLabel, gbc);

        usernameField = new JTextField(15);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 20));
        gbc.gridx = 1;
        add(usernameField, gbc);

        // Campo de senha
        JLabel passwordLabel = new JLabel();
        passwordLabel.setForeground(Color.WHITE);
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(passwordLabel, gbc);

        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 20));
        gbc.gridx = 1;
        add(passwordField, gbc);

        // Botão de Login
        loginButton = new JButton();
        loginButton.setFont(new Font("Arial", Font.BOLD, 20));
        loginButton.setBackground(Color.BLACK);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 10, 10, 10); // Espaçamento maior embaixo do botão de login
        add(loginButton, gbc);

        // Status do login
        statusLabel = new JLabel("", JLabel.CENTER);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 18)); // Aumenta o tamanho da fonte da mensagem de status
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 10, 10);
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
                        statusLabel.setText(messages.getString("login.success"));
                        openGameScreen(socket, in, out);
                    } else {
                        statusLabel.setText(messages.getString("login.failure"));
                    }

                } catch (IOException | NoSuchAlgorithmException ex) {
                    statusLabel.setText("Erro: " + ex.getMessage());
                }
            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);

        // Inicializar com o idioma padrão
        updateLocale(0); // Português
        updateLabels();
    }

    // Método para atualizar o locale selecionado
    private void updateLocale(int languageIndex) {
        switch (languageIndex) {
            case 0:
                selectedLocale = Locale.of("pt", "BR");
                break;
            case 1:
                selectedLocale = Locale.of("en", "US");
                break;
            case 2:
                selectedLocale = Locale.of("es", "ES");
                break;
            case 3:
                selectedLocale = Locale.of("fr", "FR");
                break;
            case 4:
                selectedLocale = Locale.of("de", "DE");
                break;
        }
        messages = ResourceBundle.getBundle("messages", selectedLocale);
    }

    // Atualizar os textos na interface
    private void updateLabels() {
        setTitle(messages.getString("login.title"));
        loginButton.setText(messages.getString("login.button"));
        statusLabel.setText(messages.getString("login.status"));
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

    // Método para abrir a tela de jogo usando a conexão existente e passando o
    // locale
    private void openGameScreen(Socket socket, BufferedReader in, PrintWriter out) {
        dispose(); // Fecha a tela de login
        GameScreenNova gameScreen = new GameScreenNova(socket, in, out, messages); // Passa a conexão e o bundle de
                                                                                   // mensagens
        JFrame gameFrame = new JFrame();
        gameFrame.add(gameScreen);

        gameFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setVisible(true);
    }
}
