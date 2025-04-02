package org.example;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OperadoraService {
    private List<Operadora> operadoras = new ArrayList<>();

    @PostConstruct
    public void init() {
        String csvFile = "operadoras.csv";
        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
            List<String[]> lines = reader.readAll();

            for (int i = 1; i < lines.size(); i++) {
                String[] line = lines.get(i);
                Operadora op = new Operadora();

                op.setNome(line[0]);
                op.setCnpj(line[1]);
                operadoras.add(op);
            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    public List<Operadora> search(String query) {
        if (query == null || query.isEmpty()) {
            return operadoras;
        }
        String lowerQuery = query.toLowerCase();
        return operadoras.stream()
                .filter(op -> op.getNome().toLowerCase().contains(lowerQuery))
                .collect(Collectors.toList());
    }
}