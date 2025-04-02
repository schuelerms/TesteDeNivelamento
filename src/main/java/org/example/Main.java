package org.example;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {

            WebScrapingANS webScraping = new WebScrapingANS();
            List<String> downloadedFiles = webScraping.execute();


            String anexoIPath = null;
            for (String filePath : downloadedFiles) {
                if (filePath.toLowerCase().contains("anexo_i") || filePath.toLowerCase().contains("anexo1")) {
                    anexoIPath = filePath;
                    break;
                }
            }

            if (anexoIPath == null) {
                System.out.println("Arquivo do Anexo I não encontrado.");
                return;
            }

            String csvOutputPath = "rol_procedimentos.csv";
            String zipCsvOutputPath = "Teste_MeuNome.zip";
            PDFDataExtractor pdfExtractor = new PDFDataExtractor();
            pdfExtractor.extractTableData(anexoIPath, csvOutputPath, zipCsvOutputPath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
