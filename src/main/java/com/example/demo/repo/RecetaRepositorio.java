package com.example.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.models.Receta;

public interface RecetaRepositorio extends JpaRepository<Receta, Long> {
}