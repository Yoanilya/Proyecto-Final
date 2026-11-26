package com.example.demo.controller;

import com.example.demo.models.Chef;
import com.example.demo.repo.ChefRepositorio;
import com.example.demo.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Endpoints públicos para registro e inicio de sesión con JWT")
@SecurityRequirements
public class AuthController {

    private final ChefRepositorio chefRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(
        ChefRepositorio chefRepositorio,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtService jwtService
    ) {
        this.chefRepositorio = chefRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo chef", description = "Crea una cuenta de chef cifrando la contraseña con BCrypt y devuelve un token JWT.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Chef registrado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o usuario/correo ya registrado")
    })
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request == null || isBlank(request.username()) || isBlank(request.email()) || isBlank(request.password())) {
            return ResponseEntity.badRequest().body(Map.of("error", "username, email y password son obligatorios"));
        }
        if (chefRepositorio.existsByUsername(request.username())) {
            return ResponseEntity.badRequest().body(Map.of("error", "El nombre de usuario ya existe"));
        }
        if (chefRepositorio.existsByEmail(request.email())) {
            return ResponseEntity.badRequest().body(Map.of("error", "El correo ya esta registrado"));
        }

        Chef chef = new Chef();
        chef.setUsername(request.username().trim());
        chef.setEmail(request.email().trim());
        chef.setPassword(passwordEncoder.encode(request.password()));
        chefRepositorio.save(chef);

        String token = jwtService.generateToken(chef.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("token", token, "username", chef.getUsername()));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica al chef con sus credenciales y devuelve un token JWT Bearer.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Autenticación exitosa"),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request == null || isBlank(request.username()) || isBlank(request.password())) {
            return ResponseEntity.badRequest().body(Map.of("error", "username y password son obligatorios"));
        }
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Credenciales invalidas"));
        }
        String token = jwtService.generateToken(request.username());
        return ResponseEntity.ok(Map.of("token", token, "username", request.username()));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public record RegisterRequest(String username, String email, String password) {}
    public record LoginRequest(String username, String password) {}
}
