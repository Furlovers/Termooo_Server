package helpers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ConnectionDB {
    /*
     * Classe para conexão e operações envolvendo o banco de dados.
     * Requer que haja uma instância MySQL configurada de acordo com
     * os parâmetros especificados no método connect.
     */

    public static final String driver = "com.mysql.cj.jdbc.Driver";
    public static final String error_msg = "Erro na Conexão com o BD: ";

    public static final String server = "localhost";
    public static final String port = "3306";
    public static final String database = "Termooo";
    public static final String user = "user-termo";
    public static final String password = "termooo";

    public Connection connect() throws SQLException {

        System.out.println("Chamou a função connect()");

        try {
            Class.forName(driver);
            System.out.println("Conseguiu conectar");
            return DriverManager.getConnection(
                    "jdbc:mysql://" + server + ":" + port + "/" + database + "?user=" + user + "&password=" + password);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(error_msg + e);
        }

    }

    public void setDatabase() throws SQLException {

        System.out.println("Chamou a função setDatabase()");
    
        java.sql.Connection setupConnection = null;
    
        String driver_URL = "jdbc:mysql://" + server + ":" + port + "/";
    
        String createDatabase = "CREATE DATABASE IF NOT EXISTS " + database;
        String useDatabase = "USE " + database;

        String createDict = "CREATE TABLE IF NOT EXISTS dicionario (palavra VARCHAR(6))";

        String createUserReg = "CREATE TABLE IF NOT EXISTS user_register (" +
                               "id INT(5) NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                               "usuario VARCHAR(255) NOT NULL," +
                               "senha VARCHAR(255) NOT NULL)";
    
        String createLogReg = "CREATE TABLE IF NOT EXISTS logs (" +
                              "log_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                              "id_usuario INT," +
                              "horario TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                              "palavra VARCHAR(6)," +
                              "tentativas INT," +
                              "FOREIGN KEY (id_usuario) REFERENCES user_register(id))";
    
        String createLeaderboard = "CREATE TABLE IF NOT EXISTS leaderboard (" +
                                   "leaderboard_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                                   "palavra VARCHAR(6)," +
                                   "tentativas INT," +
                                   "usuario VARCHAR(255))";
    
        PreparedStatement createDBStmt = null;
        PreparedStatement useStmt = null;
        PreparedStatement createDictStmt = null;
        PreparedStatement createUserRegStmt = null;
        PreparedStatement createLogRegStmt = null;
        PreparedStatement createLeaderboardStmt = null;
    
        try {
            Class.forName(driver);
            System.out.println("Conseguiu conectar - Criação do database");
    
            setupConnection = DriverManager.getConnection(driver_URL, user, password);
    
            createDBStmt = setupConnection.prepareStatement(createDatabase);
            useStmt = setupConnection.prepareStatement(useDatabase);
            createDictStmt = setupConnection.prepareStatement(createDict);
            createUserRegStmt = setupConnection.prepareStatement(createUserReg);
            createLogRegStmt = setupConnection.prepareStatement(createLogReg);
            createLeaderboardStmt = setupConnection.prepareStatement(createLeaderboard);
    
            // Criando o banco de dados
            createDBStmt.executeUpdate();
            System.out.println("Executou a query de criação do db");
    
            // Usando o banco de dados
            useStmt.executeUpdate();
            System.out.println("Executou a query de uso do db");
    
            // Criando a tabela dicionário
            createDictStmt.executeUpdate();
            System.out.println("Executou a query de criação da tabela dicionário");
    
            // Criando a tabela de registros de usuários
            createUserRegStmt.executeUpdate();
            System.out.println("Executou a query de criação da tabela user_register");
    
            // Criando a tabela de logs
            createLogRegStmt.executeUpdate();
            System.out.println("Executou a query de criação da tabela logs");
    
            // Criando a tabela de leaderboard
            createLeaderboardStmt.executeUpdate();
            System.out.println("Executou a query de criação da tabela leaderboard");
    
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(error_msg + e);
        }
    
        setupConnection.close();
    }

    public static void disconect(Connection conn) throws SQLException {

        System.out.println("Fechando a conexão");
        conn.close();
    }

    public static String getWord(Connection conn) throws SQLException {

        String getWordQuery = "SELECT palavra FROM dicionario ORDER BY RAND() LIMIT 1;";

        PreparedStatement stmt = null;
        try {

            stmt = conn.prepareStatement(getWordQuery);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }

            } catch (Exception e) {
            }
            conn.close();

        } catch (SQLException sqle) {
            System.out.println(sqle.getStackTrace());
        }

        System.err.println(stmt.executeQuery().getString("palavra"));

        return stmt.executeQuery().getString("palavra");
    }

    public void populate_db(Connection conn, ArrayList<String> words) {

        /*
         * words => ArrayList contendo as palavras a serem inseridas no BD.
         * 
         * conn => Conexão para o Banco de Dados
         */

        /*
         * String populateQuery = "INSERT INTO " + table_name + " VALUES ('BRENO')";
         * PreparedStatement stmt = conn.prepareStatement(populateQuery);
         * stmt.executeQuery();
         */
        String populateQuery = "INSERT INTO dicionario(palavra) VALUES (?)";

        PreparedStatement stmt = null;
        try {
            for (String word : words) {
                stmt = conn.prepareStatement(populateQuery);
                stmt.setString(1, word);
                stmt.execute();
            }
            conn.close();

        } catch (SQLException sqle) {
            System.out.println(sqle.getStackTrace());
        }

    }

}