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
import java.util.List;
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
@RequestMapping("/api/tecnicas")
@Tag(name = "Técnicas Culinarias", description = "Catálogo CRUD de técnicas culinarias para asociar a recetas (requiere JWT)")
@SecurityRequirement(name = "bearerAuth")
public class TecnicaCulinariaController {

    private final TecnicaCulinariaRepositorio repositorio;
    private final RecetaRepositorio recetaRepositorio;

    public TecnicaCulinariaController(
            TecnicaCulinariaRepositorio repositorio,
            RecetaRepositorio recetaRepositorio
    ) {
        this.repositorio = repositorio;
        this.recetaRepositorio = recetaRepositorio;
    }

    @GetMapping
    @Operation(summary = "Listar técnicas culinarias", description = "Obtiene todas las técnicas culinarias registradas.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido")
    })
    public List<TecnicaCulinaria> obtenerTodas() {
        return repositorio.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar técnica por ID", description = "Obtiene una técnica culinaria específica por su ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Técnica encontrada"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
        @ApiResponse(responseCode = "404", description = "Técnica no encontrada")
    })
    public ResponseEntity<TecnicaCulinaria> obtenerPorId(@PathVariable Long id) {
        return repositorio.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear técnica culinaria", description = "Registra una nueva técnica culinaria en el catálogo.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Técnica creada correctamente"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido")
    })
    public ResponseEntity<TecnicaCulinaria> crear(@RequestBody TecnicaCulinaria tecnica) {
        TecnicaCulinaria saved = repositorio.save(tecnica);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar técnica culinaria", description = "Actualiza el nombre y descripción de una técnica culinaria existente.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Técnica actualizada correctamente"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
        @ApiResponse(responseCode = "404", description = "Técnica no encontrada")
    })
    public ResponseEntity<TecnicaCulinaria> actualizar(
            @PathVariable Long id,
            @RequestBody TecnicaCulinaria detalles
    ) {
        return repositorio.findById(id).map(tecnica -> {
            if (detalles.getNombre() != null) {
                tecnica.setNombre(detalles.getNombre());
            }
            if (detalles.getDescripcion() != null) {
                tecnica.setDescripcion(detalles.getDescripcion());
            }
            return ResponseEntity.ok(repositorio.save(tecnica));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar técnica culinaria", description = "Desasocia la técnica de sus recetas y la elimina del catálogo.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Técnica eliminada correctamente"),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente o inválido"),
        @ApiResponse(responseCode = "404", description = "Técnica no encontrada")
    })
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        return repositorio.findById(id).map(tecnica -> {
            for (Receta receta : List.copyOf(tecnica.getRecetas())) {
                receta.getTecnicasCulinarias().remove(tecnica);
                recetaRepositorio.save(receta);
            }
            repositorio.delete(tecnica);
            return ResponseEntity.noContent().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
