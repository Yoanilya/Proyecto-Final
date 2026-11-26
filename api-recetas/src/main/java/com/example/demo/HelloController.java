package com.example.demo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Estado", description = "Verificación de disponibilidad del servicio")
public class HelloController {

    @GetMapping("/")
    @SecurityRequirements
    @Operation(summary = "Verificar estado de la API", description = "Endpoint público de bienvenida para comprobar que el servidor está en ejecución.")
    public String sayHello() {
        return "¡API de Diseño de Recetas y Técnicas Culinarias en ejecución! Documentación disponible en /swagger-ui/index.html";
    }
}
