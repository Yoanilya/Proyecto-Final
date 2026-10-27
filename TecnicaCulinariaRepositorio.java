package com.example.demo.repo;

import com.example.demo.models.TecnicaCulinaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TecnicaCulinariaRepositorio extends JpaRepository<TecnicaCulinaria, Long> {
}