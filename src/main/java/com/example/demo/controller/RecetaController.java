package com.example.demo.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.models.Receta;
import com.example.demo.repo.RecetaRepository;

@RestController
public class RecetaController {
    private final RecetaRepository repo;

    public RecetaController(RecetaRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/recetas")
    public List<Receta> findAllRecetas() {
        return repo.findAll();
    }

    @PostMapping("/recetas")
    public Receta addReceta(@RequestBody Receta receta) {
        return repo.save(receta);
    }
}