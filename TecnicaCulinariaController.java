package com.example.demo.controller;

import com.example.demo.models.TecnicaCulinaria;
import com.example.demo.repo.TecnicaCulinariaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tecnicas")
public class TecnicaCulinariaController {

    @Autowired
    private TecnicaCulinariaRepositorio repositorio;

    
    @GetMapping
    public List<TecnicaCulinaria> obtenerTodas() {
        return repositorio.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TecnicaCulinaria> obtenerPorId(@PathVariable Long id) {
        return repositorio.findById(id)
                .map(tecnica -> ResponseEntity.ok().body(tecnica))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public TecnicaCulinaria crear(@RequestBody TecnicaCulinaria tecnica) {
        return repositorio.save(tecnica);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TecnicaCulinaria> actualizar(@PathVariable Long id, @RequestBody TecnicaCulinaria detalles) {
        return repositorio.findById(id).map(tecnica -> {
            tecnica.setNombre(detalles.getNombre());
            tecnica.setDescripcion(detalles.getDescripcion());
            TecnicaCulinaria actualizada = repositorio.save(tecnica);
            return ResponseEntity.ok().body(actualizada);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        return repositorio.findById(id).map(tecnica -> {
            repositorio.delete(tecnica);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}