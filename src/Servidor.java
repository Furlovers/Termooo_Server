import entities.Letter;
import helpers.ConnectionDB;
import helpers.StateEnum;
import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Servidor {

    private static final int PORT = 3305;
    private static final int MAX_ATTEMPTS = 6;
    private String secretWord;
    private int attempts = 0;

    public static void main(String[] args) {
        new Servidor().start();
    }

    public void start() {
        System.out.println("Servidor iniciado...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    System.out.println("Cliente conectado.");
                    handleClient(clientSocket);
                } catch (IOException e) {
                    System.out.println("Erro ao conectar com o cliente: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao iniciar o servidor: " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

        // Processar login
        String input = in.readLine();
        if (input.startsWith("LOGIN:")) {
            String[] parts = input.split(":");
            String username = parts[1];
            String encryptedPassword = parts[2];

            try {
                if (authenticateOrRegisterUser(username, encryptedPassword)) {
                    out.println("SUCCESS");
                    System.out.println("Usuário autenticado: " + username);
                } else {
                    out.println("FAIL");
                    return;
                }
            } catch (Exception e) {
                out.println("ERROR");
                return;
            }
        }

        // Iniciar jogo após login bem-sucedido
        try {
            Connection conn = new ConnectionDB().connect();
            secretWord = ConnectionDB.getWord(conn).toUpperCase(); // Pega a palavra secreta
            System.out.println("Palavra sorteada: " + secretWord);
        } catch (Exception e) {
            System.out.println("Erro ao obter palavra do banco de dados: " + e.getMessage());
            return;
        }

        if (input.startsWith("NEW_WORD")) {
            try {
                Connection conn = new ConnectionDB().connect();
                secretWord = ConnectionDB.getWord(conn).toUpperCase(); // Pega a palavra secreta
                System.out.println("Palavra sorteada: " + secretWord);
            } catch (Exception e) {
                System.out.println("Erro ao obter palavra do banco de dados: " + e.getMessage());
                return;
            }
        }

        try {
            // Loop para processar os palpites do cliente
            String guess;
            // Dentro do método handleClient
            while ((guess = in.readLine()) != null) {
                if (guess.startsWith("GUESS:")) {
                    String guessedWord = guess.substring(6); // Remove o prefixo "GUESS:"
                    System.out.println("Palavra adivinhada: " + guessedWord);

                    System.out.println(attempts);
                    if (attempts >= MAX_ATTEMPTS) {
                        out.println("Fim de jogo! A palavra era: " + secretWord);
                        break;
                    }

                    List<Letter> result = validateGuess(guessedWord);
                    out.println(serializeLetters(result));

                    attempts++;
                    if (guessedWord.equalsIgnoreCase(secretWord)) {
                        out.println("Parabéns, você acertou!");
                        break;
                    } else if (attempts >= MAX_ATTEMPTS) {
                        out.println("Você perdeu! A palavra era: " + secretWord);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao processar palpite: " + e.getMessage());
        }

        while (in.readLine() != null) {
            if (in.readLine().equals("NEW_WORD")) {
                try {
                    Connection conn = new ConnectionDB().connect();
                    secretWord = ConnectionDB.getWord(conn).toUpperCase(); // Pega a palavra secreta
                    System.out.println("Palavra sorteada: " + secretWord);
                } catch (Exception e) {
                    System.out.println("Erro ao obter palavra do banco de dados: " + e.getMessage());
                    return;
                }
            }
        }

        System.out.println("Cliente desconectado.");
    }

    private List<Letter> validateGuess(String guess) {
        List<Letter> feedback = new ArrayList<>();
        for (int i = 0; i < secretWord.length(); i++) {
            char guessedChar = guess.charAt(i);
            char actualChar = secretWord.charAt(i);

            Letter letter = new Letter(guessedChar);
            if (guessedChar == actualChar) {
                letter.setState(StateEnum.DISCOVERED_AND_RIGHT);
            } else if (secretWord.contains(Character.toString(guessedChar))) {
                letter.setState(StateEnum.DISCOVERED_AND_WRONG);
            } else {
                letter.setState(StateEnum.WRONG);
            }
            feedback.add(letter);
        }
        return feedback;
    }

    private String serializeLetters(List<Letter> letters) {
        StringBuilder serialized = new StringBuilder();
        for (Letter letter : letters) {
            serialized.append(letter.getLetter()).append(":").append(letter.getStatesEnum()).append(",");
        }
        return serialized.toString();
    }

    /**
     * Tenta autenticar o usuário. Se não encontrar o usuário, cria um novo com a
     * senha fornecida.
     */
    private boolean authenticateOrRegisterUser(String username, String encryptedPassword)
            throws SQLException, NoSuchAlgorithmException {
        Connection conn = new ConnectionDB().connect();
        String query = "SELECT senha FROM user_register WHERE usuario = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            // Usuário existe, verificar senha
            String storedPassword = rs.getString("senha");
            return storedPassword.equals(encryptedPassword);
        } else {
            // Usuário não existe, então cria um novo
            registerUser(username, encryptedPassword, conn);
            return true; // Após registrar, login é considerado bem-sucedido
        }
    }

    /**
     * Registra um novo usuário no banco de dados.
     */
    private void registerUser(String username, String encryptedPassword, Connection conn) throws SQLException {
        String insertUserQuery = "INSERT INTO user_register (usuario, senha) VALUES (?, ?)";
        PreparedStatement stmt = conn.prepareStatement(insertUserQuery);
        stmt.setString(1, username);
        stmt.setString(2, encryptedPassword);
        stmt.executeUpdate();
        System.out.println("Novo usuário registrado: " + username);
    }
}
