package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoApplicationTests {

    @LocalServerPort
    private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void contextLoadsAndRubricFlowWorksEndToEnd() throws Exception {
        // 1. Swagger / OpenAPI accesible y con esquema bearerAuth
        HttpResponse<String> swaggerUi = httpClient.send(
                HttpRequest.newBuilder(URI.create(url("/swagger-ui/index.html"))).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, swaggerUi.statusCode(), "Swagger UI debe responder 200");

        HttpResponse<String> openApi = httpClient.send(
                HttpRequest.newBuilder(URI.create(url("/v3/api-docs"))).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, openApi.statusCode(), "/v3/api-docs debe responder 200");
        assertTrue(openApi.body().contains("bearerAuth"), "OpenAPI debe declarar bearerAuth");
        assertTrue(openApi.body().contains("/recetas"), "OpenAPI debe documentar /recetas");
        assertTrue(openApi.body().contains("/api/tecnicas"), "OpenAPI debe documentar /api/tecnicas");

        // 2. Rutas protegidas bloquean peticiones sin JWT (401)
        HttpResponse<String> unauthRecetas = httpClient.send(
                HttpRequest.newBuilder(URI.create(url("/recetas"))).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(401, unauthRecetas.statusCode(), "GET /recetas sin token debe responder 401");

        HttpResponse<String> unauthTecnicas = httpClient.send(
                HttpRequest.newBuilder(URI.create(url("/api/tecnicas"))).GET().build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(401, unauthTecnicas.statusCode(), "GET /api/tecnicas sin token debe responder 401");

        // 3. Registro y Login JWT públicos
        String registerBody = """
                {
                  "username": "chef_test",
                  "email": "chef_test@example.com",
                  "password": "Password123!"
                }
                """;
        HttpResponse<String> registerRes = httpClient.send(
                HttpRequest.newBuilder(URI.create(url("/auth/register")))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(registerBody))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(201, registerRes.statusCode(), "POST /auth/register debe responder 201");

        String loginBody = """
                {
                  "username": "chef_test",
                  "password": "Password123!"
                }
                """;
        HttpResponse<String> loginRes = httpClient.send(
                HttpRequest.newBuilder(URI.create(url("/auth/login")))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(loginBody))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, loginRes.statusCode(), "POST /auth/login debe responder 200");
        String token = extractJsonString(loginRes.body(), "token");
        assertNotNull(token, "Login debe devolver un JWT");

        // Login con contraseña incorrecta debe devolver 401
        HttpResponse<String> badLoginRes = httpClient.send(
                HttpRequest.newBuilder(URI.create(url("/auth/login")))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString("{\"username\":\"chef_test\",\"password\":\"wrong\"}"))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(401, badLoginRes.statusCode(), "Credenciales inválidas deben responder 401");

        // 4. Crear técnica culinaria protegida con JWT
        HttpResponse<String> tecnicaRes = httpClient.send(
                HttpRequest.newBuilder(URI.create(url("/api/tecnicas")))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .POST(HttpRequest.BodyPublishers.ofString("{\"nombre\":\"Salteado\",\"descripcion\":\"Coccion rapida a fuego alto\"}"))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(201, tecnicaRes.statusCode(), "POST /api/tecnicas debe responder 201");
        String tecnicaId = extractJsonNumber(tecnicaRes.body(), "id");
        assertNotNull(tecnicaId, "La técnica creada debe incluir id");

        // 5. Crear receta con ingredientes, pasos y relación @ManyToMany (técnica existente + técnica nueva)
        String recetaBody = """
                {
                  "nombre": "Pasta al pesto",
                  "descripcion": "Receta de pasta con pesto casero",
                  "dificultad": "Media",
                  "tiempoPreparacion": 25,
                  "ingredientes": [
                    { "nombre": "Pasta", "cantidad": "200", "unidad": "gramos" }
                  ],
                  "pasos": [
                    { "orden": 1, "descripcion": "Hervir la pasta" }
                  ],
                  "tecnicasCulinarias": [
                    { "id": %s },
                    { "nombre": "Emulsion", "descripcion": "Mezcla homogenea de aceite y albahaca" }
                  ]
                }
                """.formatted(tecnicaId);

        HttpResponse<String> createRecetaRes = httpClient.send(
                HttpRequest.newBuilder(URI.create(url("/recetas")))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .POST(HttpRequest.BodyPublishers.ofString(recetaBody))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(201, createRecetaRes.statusCode(), "POST /recetas debe responder 201");
        String recetaId = extractJsonNumber(createRecetaRes.body(), "id");
        assertNotNull(recetaId, "La receta creada debe incluir id");

        // 6. Consultar GET /recetas y GET /recetas/{id} sin ciclos JSON
        HttpResponse<String> getAllRes = httpClient.send(
                HttpRequest.newBuilder(URI.create(url("/recetas")))
                        .header("Authorization", "Bearer " + token)
                        .GET()
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, getAllRes.statusCode(), "GET /recetas con token debe responder 200");
        assertTrue(getAllRes.body().contains("Pasta al pesto"));
        assertTrue(getAllRes.body().contains("Salteado"));
        assertTrue(getAllRes.body().contains("Emulsion"));
        assertTrue(getAllRes.body().contains("Hervir la pasta"));

        // 7. Actualizar parámetros con PUT /recetas/{id} sin perder colecciones existentes
        HttpResponse<String> updateRes = httpClient.send(
                HttpRequest.newBuilder(URI.create(url("/recetas/" + recetaId)))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .PUT(HttpRequest.BodyPublishers.ofString("{\"dificultad\":\"Facil\",\"tiempoPreparacion\":20}"))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, updateRes.statusCode(), "PUT /recetas/{id} debe responder 200");
        assertTrue(updateRes.body().contains("Facil"));
        assertTrue(updateRes.body().contains("Pasta"), "PUT parcial debe conservar ingredientes");
        assertTrue(updateRes.body().contains("Hervir la pasta"), "PUT parcial debe conservar pasos");
        assertTrue(updateRes.body().contains("Salteado"), "PUT parcial debe conservar técnicas @ManyToMany");

        // 8. Asociar una técnica adicional vía POST /recetas/{recetaId}/tecnicas/{tecnicaId}
        HttpResponse<String> tecnica2Res = httpClient.send(
                HttpRequest.newBuilder(URI.create(url("/api/tecnicas")))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .POST(HttpRequest.BodyPublishers.ofString("{\"nombre\":\"Flameado\",\"descripcion\":\"Encendido breve con licor\"}"))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(201, tecnica2Res.statusCode());
        String tecnica2Id = extractJsonNumber(tecnica2Res.body(), "id");

        HttpResponse<String> linkRes = httpClient.send(
                HttpRequest.newBuilder(URI.create(url("/recetas/" + recetaId + "/tecnicas/" + tecnica2Id)))
                        .header("Authorization", "Bearer " + token)
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, linkRes.statusCode(), "POST /recetas/{id}/tecnicas/{tecnicaId} debe responder 200");
        assertTrue(linkRes.body().contains("Flameado"));

        // 9. Verificar CORS preflight OPTIONS
        HttpResponse<String> corsRes = httpClient.send(
                HttpRequest.newBuilder(URI.create(url("/recetas")))
                        .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                        .header("Origin", "http://localhost:5500")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization,Content-Type")
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        );
        assertEquals(200, corsRes.statusCode(), "Preflight CORS OPTIONS debe responder 200");
    }

    private String extractJsonString(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
        return m.find() ? m.group(1) : null;
    }

    private String extractJsonNumber(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*(\\d+)").matcher(json);
        return m.find() ? m.group(1) : null;
    }
}
