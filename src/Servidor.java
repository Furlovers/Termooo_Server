import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.util.*;
import helpers.ConnectionDB;
import entities.Letter;
import helpers.StateEnum;

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

        try {
            Connection conn = new ConnectionDB().connect();
            secretWord = ConnectionDB.getWord(conn).toUpperCase();
            System.out.println("Palavra sorteada: " + secretWord);
        } catch (Exception e) {
            System.out.println("Erro ao obter palavra do banco de dados: " + e.getMessage());
            return;
        }

        String guess;
        while ((guess = in.readLine()) != null) {
            if (attempts >= MAX_ATTEMPTS) {
                out.println("Fim de jogo! A palavra era: " + secretWord);
                break;
            }

            List<Letter> result = validateGuess(guess);
            out.println(serializeLetters(result));

            attempts++;
            if (guess.equalsIgnoreCase(secretWord)) {
                out.println("Parabéns, você acertou!");
                break;
            } else if (attempts >= MAX_ATTEMPTS) {
                out.println("Você perdeu! A palavra era: " + secretWord);
                break;
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
}
