package org.example;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PDFDataExtractor {


    public void extractTableData(String pdfFilePath, String csvOutputPath, String zipOutputPath) throws Exception {
        try (PDDocument document = PDDocument.load(new File(pdfFilePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            String[] lines = text.split("\\r?\\n");
            List<String[]> tableData = new ArrayList<>();
            boolean tabelaEncontrada = false;

            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                if (!tabelaEncontrada && line.toLowerCase().contains("rol de procedimentos")) {
                    tabelaEncontrada = true;
                }
                if (tabelaEncontrada) {
                    String[] columns = line.split("\\s{2,}");
                    tableData.add(columns);
                }
            }

            if (tableData.isEmpty()) {
                throw new Exception("Tabela não encontrada no PDF.");
            }


            String[] header = tableData.get(0);
            for (int i = 0; i < header.length; i++) {
                if (header[i].equalsIgnoreCase("OD")) {
                    header[i] = "Descrição Completa para OD";
                }
                if (header[i].equalsIgnoreCase("AMB")) {
                    header[i] = "Descrição Completa para AMB";
                }
            }
            tableData.set(0, header);


            saveCSV(tableData, csvOutputPath);


            zipFile(csvOutputPath, zipOutputPath);

            System.out.println("Dados extraídos e CSV gerado: " + csvOutputPath);
            System.out.println("CSV compactado em: " + zipOutputPath);
        }
    }

    private void saveCSV(List<String[]> data, String csvFilePath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(csvFilePath))) {
            for (String[] row : data) {
                String line = String.join(",", row);
                writer.write(line);
                writer.newLine();
            }
        }
    }

    private void zipFile(String filePath, String zipFilePath) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath));
             FileInputStream fis = new FileInputStream(filePath)) {
            ZipEntry zipEntry = new ZipEntry(new File(filePath).getName());
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            zos.closeEntry();
        }
    }
}
