package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/operadoras")
public class OperadoraController {

    @Autowired
    private OperadoraService operadoraService;

    @GetMapping
    public List<Operadora> searchOperadoras(@RequestParam(value = "search", required = false) String query) {
        return operadoraService.search(query);
    }
}