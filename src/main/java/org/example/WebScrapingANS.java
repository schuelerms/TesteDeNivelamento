package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class WebScrapingANS {

    public static final String URL_SITE = "https://www.gov.br/ans/pt-br/acesso-a-informacao/participacao-dasociedade/atualizacao-do-rol-de-procedimentos";
    public static final String DOWNLOAD_DIR = "downloads/";
    public static final String ZIP_FILE = "anexos.zip";

    public List<String> execute() throws Exception {
        List<String> downloadedFiles = new ArrayList<>();

        Files.createDirectories(Paths.get(DOWNLOAD_DIR));

        Document doc = Jsoup.connect(URL_SITE).get();
        Elements links = doc.select("a[href$=.pdf]");

        for (Element link : links) {
            String linkText = link.text().toLowerCase();

            if (linkText.contains("anexo i") || linkText.contains("anexo1") ||
                    linkText.contains("anexo ii") || linkText.contains("anexo2")) {

                String pdfUrl = link.absUrl("href");
                String fileName = DOWNLOAD_DIR + pdfUrl.substring(pdfUrl.lastIndexOf("/") + 1);
                downloadFile(pdfUrl, fileName);
                System.out.println("Baixado: " + fileName);
                downloadedFiles.add(fileName);
            }
        }


        zipFiles(downloadedFiles, ZIP_FILE);
        System.out.println("Arquivos compactados em: " + ZIP_FILE);

        return downloadedFiles;
    }


    private void downloadFile(String fileURL, String destination) throws IOException {
        try (InputStream in = new URL(fileURL).openStream()) {
            Files.copy(in, Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
        }
    }


    private void zipFiles(List<String> filePaths, String zipFilePath) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFilePath))) {
            for (String filePath : filePaths) {
                File file = new File(filePath);
                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(file.getName());
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
    }
}

