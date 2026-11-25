package com.example.demo.repo;

import com.example.demo.models.Chef;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChefRepositorio extends JpaRepository<Chef, Long> {
    Optional<Chef> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}