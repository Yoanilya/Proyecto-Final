package com.example.demo.service;

import com.example.demo.models.Chef;
import com.example.demo.repositorio.ChefRepositorio;
import java.util.Collections;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ChefDetailsService implements UserDetailsService {

    private final ChefRepositorio chefRepositorio;

    public ChefDetailsService(ChefRepositorio chefRepositorio) {
        this.chefRepositorio = chefRepositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Chef chef = chefRepositorio.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Chef no encontrado: " + username));

        return new User(
                chef.getUsername(),
                chef.getPassword(),
                Collections.emptyList()
        );
    }
}
