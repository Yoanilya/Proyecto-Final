package com.example.demo.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "receta")

public class Receta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;

    @JsonManagedReference
    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ingrediente> ingredientes = new ArrayList<>();

    @JsonManagedReference
    @OneToMany(mappedBy = "receta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PasoPreparacion> pasos = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "receta_tecnica",
        joinColumns = @JoinColumn(name = "receta_id"),
        inverseJoinColumns = @JoinColumn(name = "tecnica_id")
    )
    private Set<TecnicaCulinaria> tecnicasCulinarias = new HashSet<>();

    public List<Ingrediente> getIngredientes() {
        return ingredientes;
    }

    public List<PasoPreparacion> getPasos() {
        return pasos;
    }

    public Set<TecnicaCulinaria> getTecnicasCulinarias() {
        return tecnicasCulinarias;
    }

    public void setTecnicasCulinarias(Set<TecnicaCulinaria> tecnicasCulinarias) {
        this.tecnicasCulinarias = tecnicasCulinarias;
    }
}