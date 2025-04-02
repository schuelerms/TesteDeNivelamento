package org.example;

import java.sql.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DatabaseManager {


    private static final String DB_URL = "jdbc:mysql://localhost:3306/testenivelamento";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "senha";


    private static final String OPERADORAS_CSV_URL = "https://dadosabertos.ans.gov.br/FTP/PDA/operadoras_de_plano_de_saude_ativas/operadoras.csv";
    private static final String CSV_FILE = "operadoras.csv";

    public void execute() throws Exception {

        downloadCSV(OPERADORAS_CSV_URL, CSV_FILE);
        System.out.println("CSV de operadoras baixado: " + CSV_FILE);


        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            createOperadorasTable(conn);

            importOperadorasCSV(conn, CSV_FILE);

            executeAnalyticQueries(conn);
        }
    }

    private void downloadCSV(String fileURL, String destination) throws IOException {
        try (InputStream in = new URL(fileURL).openStream()) {
            Files.copy(in, Paths.get(destination));
        }
    }

    private void createOperadorasTable(Connection conn) throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS operadoras (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "nome VARCHAR(255)," +
                "cnpj VARCHAR(20)" +

                ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("Tabela 'operadoras' criada.");
        }
    }

    private void importOperadorasCSV(Connection conn, String csvFile) throws SQLException, IOException {

        String absolutePath = new File(csvFile).getAbsolutePath().replace("\\", "/");
        String loadSQL = "LOAD DATA LOCAL INFILE '" + absolutePath + "' " +
                "INTO TABLE operadoras " +
                "FIELDS TERMINATED BY ',' " +
                "ENCLOSED BY '\"' " +
                "LINES TERMINATED BY '\\n' " +
                "IGNORE 1 ROWS " +
                "(nome, cnpj)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(loadSQL);
            System.out.println("Dados do CSV importados para a tabela 'operadoras'.");
        }
    }

    private void executeAnalyticQueries(Connection conn) throws SQLException {

        String queryUltimoTrimestre = "SELECT operadora, SUM(despesa) AS total_despesa " +
                "FROM despesas " +
                "WHERE data >= DATE_SUB(CURDATE(), INTERVAL 3 MONTH) " +
                "AND categoria = 'EVENTOS/ SINISTROS CONHECIDOS OU AVISADOS DE ASSISTÊNCIA A SAÚDE MEDICO HOSPITALAR' " +
                "GROUP BY operadora " +
                "ORDER BY total_despesa DESC " +
                "LIMIT 10";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(queryUltimoTrimestre)) {
            System.out.println("Top 10 operadoras - Último Trimestre:");
            while (rs.next()) {
                System.out.println(rs.getString("operadora") + " - " + rs.getDouble("total_despesa"));
            }
        }

    }

    public static void main(String[] args) {
        try {
            DatabaseManager dbManager = new DatabaseManager();
            dbManager.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
