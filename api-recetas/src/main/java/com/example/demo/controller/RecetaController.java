package com.example.demo.controller;

import com.example.demo.models.Receta;
import com.example.demo.models.TecnicaCulinaria;
import com.example.demo.repo.RecetaRepositorio;
import com.example.demo.repo.TecnicaCulinariaRepositorio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/recetas")
@Tag(name = "Recetas", description = "Gestión de recetas base, ingredientes, pasos de preparación y técnicas culinarias (requiere JWT)")
@SecurityRequirement(name = "bearerAuth")
public class RecetaController {

    private final RecetaRepositorio repo;
    private final TecnicaCulinariaRepositorio tecnicaRepo;

    public RecetaController(RecetaRepositorio repo, TecnicaCulinariaRepositorio tecnicaRepo) {
        this.repo = repo;
        this.tecnicaRepo = tecnicaRepo;
    }

    @GetMapping
    @Operation(summary = "Consultar catálogo de recetas", description = "Devuelve todas las recetas registradas con sus ingredientes, pasos y técnicas culinarias.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Catálogo obtenido correctamente"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido")
    })
    public List<Receta> findAllRecetas() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar receta por ID", description = "Obtiene el detalle completo de una receta específica por su identificador.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Receta encontrada"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
        @ApiResponse(responseCode = "404", description = "Receta no encontrada")
    })
    public ResponseEntity<Receta> findRecetaById(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Registrar receta base", description = "Registra una nueva receta con sus ingredientes, pasos de preparación y opcionalmente técnicas culinarias (nuevas o existentes por ID).")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Receta creada correctamente"),
        @ApiResponse(responseCode = "400", description = "Alguna técnica culinaria referenciada por ID no existe"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido")
    })
    public ResponseEntity<?> addReceta(@RequestBody Receta receta) {
        if (receta.getIngredientes() != null) {
            receta.getIngredientes().forEach(ingrediente -> ingrediente.setReceta(receta));
        }
        if (receta.getPasos() != null) {
            receta.getPasos().forEach(paso -> paso.setReceta(receta));
        }

        if (receta.getTecnicasCulinarias() != null && !receta.getTecnicasCulinarias().isEmpty()) {
            Set<TecnicaCulinaria> resolvedTecnicas = new HashSet<>();
            for (TecnicaCulinaria tecnica : receta.getTecnicasCulinarias()) {
                if (tecnica.getId() != null) {
                    Optional<TecnicaCulinaria> existing = tecnicaRepo.findById(tecnica.getId());
                    if (existing.isEmpty()) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "No existe tecnica culinaria con id " + tecnica.getId()));
                    }
                    resolvedTecnicas.add(existing.get());
                } else {
                    resolvedTecnicas.add(tecnica);
                }
            }
            receta.setTecnicasCulinarias(resolvedTecnicas);
        }

        Receta saved = repo.save(receta);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar parámetros de receta", description = "Actualiza nombre, descripción, dificultad, tiempo de preparación y colecciones de una receta existente.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Receta actualizada correctamente"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
        @ApiResponse(responseCode = "404", description = "Receta no encontrada")
    })
    public ResponseEntity<?> updateReceta(@PathVariable Long id, @RequestBody Receta detalles) {
        Optional<Receta> optionalReceta = repo.findById(id);
        if (optionalReceta.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Receta receta = optionalReceta.get();
        if (detalles.getNombre() != null) {
            receta.setNombre(detalles.getNombre());
        }
        if (detalles.getDescripcion() != null) {
            receta.setDescripcion(detalles.getDescripcion());
        }
        if (detalles.getDificultad() != null) {
            receta.setDificultad(detalles.getDificultad());
        }
        if (detalles.getTiempoPreparacion() != null) {
            receta.setTiempoPreparacion(detalles.getTiempoPreparacion());
        }

        if (detalles.getIngredientes() != null && !detalles.getIngredientes().isEmpty()) {
            receta.getIngredientes().clear();
            detalles.getIngredientes().forEach(ingrediente -> {
                ingrediente.setReceta(receta);
                receta.getIngredientes().add(ingrediente);
            });
        }

        if (detalles.getPasos() != null && !detalles.getPasos().isEmpty()) {
            receta.getPasos().clear();
            detalles.getPasos().forEach(paso -> {
                paso.setReceta(receta);
                receta.getPasos().add(paso);
            });
        }

        if (detalles.getTecnicasCulinarias() != null && !detalles.getTecnicasCulinarias().isEmpty()) {
            Set<TecnicaCulinaria> resolvedTecnicas = new HashSet<>();
            for (TecnicaCulinaria tecnica : detalles.getTecnicasCulinarias()) {
                if (tecnica.getId() != null) {
                    Optional<TecnicaCulinaria> existing = tecnicaRepo.findById(tecnica.getId());
                    if (existing.isEmpty()) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("error", "No existe tecnica culinaria con id " + tecnica.getId()));
                    }
                    resolvedTecnicas.add(existing.get());
                } else {
                    resolvedTecnicas.add(tecnica);
                }
            }
            receta.setTecnicasCulinarias(resolvedTecnicas);
        }

        return ResponseEntity.ok(repo.save(receta));
    }

    @PostMapping("/{recetaId}/tecnicas/{tecnicaId}")
    @Operation(summary = "Añadir técnica culinaria a una receta", description = "Asocia una técnica culinaria existente a una receta mediante la relación Muchos a Muchos.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Técnica asociada correctamente"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
        @ApiResponse(responseCode = "404", description = "Receta o técnica culinaria no encontrada")
    })
    public ResponseEntity<Receta> addTecnicaToReceta(
            @PathVariable Long recetaId,
            @PathVariable Long tecnicaId
    ) {
        Optional<Receta> recetaOpt = repo.findById(recetaId);
        Optional<TecnicaCulinaria> tecnicaOpt = tecnicaRepo.findById(tecnicaId);

        if (recetaOpt.isEmpty() || tecnicaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Receta receta = recetaOpt.get();
        receta.getTecnicasCulinarias().add(tecnicaOpt.get());
        return ResponseEntity.ok(repo.save(receta));
    }

    @DeleteMapping("/{recetaId}/tecnicas/{tecnicaId}")
    @Operation(summary = "Desasociar técnica culinaria de una receta", description = "Elimina la asociación entre una receta y una técnica culinaria.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Técnica desasociada correctamente"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
        @ApiResponse(responseCode = "404", description = "Receta o técnica culinaria no encontrada")
    })
    public ResponseEntity<Receta> removeTecnicaFromReceta(
            @PathVariable Long recetaId,
            @PathVariable Long tecnicaId
    ) {
        Optional<Receta> recetaOpt = repo.findById(recetaId);
        Optional<TecnicaCulinaria> tecnicaOpt = tecnicaRepo.findById(tecnicaId);

        if (recetaOpt.isEmpty() || tecnicaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Receta receta = recetaOpt.get();
        receta.getTecnicasCulinarias().remove(tecnicaOpt.get());
        return ResponseEntity.ok(repo.save(receta));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar receta", description = "Elimina una receta existente por su ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Receta eliminada correctamente"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
        @ApiResponse(responseCode = "404", description = "Receta no encontrada")
    })
    public ResponseEntity<Void> deleteReceta(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
