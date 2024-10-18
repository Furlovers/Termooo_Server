// import java.io.*;

// import java.net.*;

// import java.util.Scanner;



// public class Cliente {

//     private static final String SERVER_ADDRESS = "localhost";

//     private static final int SERVER_PORT = 3305;



//     public static void main(String[] args) {

//         try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);

//              BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

//              PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

//              Scanner scanner = new Scanner(System.in)) {



//             System.out.println("Conectado ao servidor.");



//             String guess;

//             while (true) {

//                 System.out.print("Digite sua palavra: ");

//                 guess = scanner.nextLine();



//                 out.println(guess);  // Envia a palavra para o servidor



//                 // Recebe a resposta do servidor

//                 String response;

//                 while ((response = in.readLine()) != null) {

//                     System.out.println(response);

//                     if (response.contains("Parabéns")) {

//                         break;  // Palavra correta, encerra o jogo

//                     }

//                 }

//             }



//         } catch (IOException e) {

//             System.out.println("Erro ao se conectar ao servidor: " + e.getMessage());

//         }

//     }

// }
