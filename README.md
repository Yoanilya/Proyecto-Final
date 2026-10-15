# Plataforma de Diseño de Recetas y Técnicas Culinarias

## 1. Descripción general del proyecto
Una API REST diseñada para que chefs y estudiantes de gastronomía puedan documentar, construir y consultar recetas paso a paso. En lugar de contar existencias, el sistema permite registrar preparaciones base y enriquecerlas progresivamente aplicando diferentes técnicas o variaciones de ingredientes.

## 2. Lista de integrantes del equipo
* Morales Franco Clara Zairy
* Becerra Salazar Valeria
* Mendoza Vásquez Manuel Heyoan

## 3. Tabla de requerimientos (5)

Requerimientos
1. Registrar Receta Base | Permite registrar una nueva receta base en el sistema.
2. Consultar Catálogo    | Permite consultar el catálogo completo de todas las recetas disponibles.
3. Búsqueda por ID       | Permite buscar una receta específica por su identificador para consultar sus detalles mediante el método.
4. Añadir Técnica Culinaria | Permite agregar instrucciones de una técnica culinaria específica a una receta existente.
5. Actualizar Parámetros | Permite actualizar la dificultad o el tiempo de preparación de un platillo en el sistema.

## 4. Funcionalidades resueltas con paquetes de Spring Boot
* Autenticación: Garantiza la identificación de cada chef o estudiante, controlando de manera precisa quién ejecuta métodos para alterar el recetario.
* Seguridad: Incorpora protocolos avanzados para proteger la integridad de las rutas de la API, previniendo manipulaciones no autorizadas en el catálogo de técnicas y decoradores gastronómicos.
* Logs: Facilita el rastreo profundo de errores de sistema y la captura de métricas, permitiendo auditar cada petición realizada sobre los platillos disponibles en el entorno de desarrollo.
* Conexión a base de datos: Orquesta la persistencia de datos para almacenar de forma permanente y relacional toda la información estructural de las recetas, variaciones de ingredientes y tiempos de cocción.
* Documentación: Integra herramientas automatizadas para generar especificaciones técnicas detalladas y estandarizadas, acelerando el entendimiento de los servicios REST implementados en el proyecto.

## 5. Patrones de Diseño Aplicados

* Patrón Decorator: Aplicado en la creación dinámica de recetas
* Patrón Singleton: Implementado en el conversor de medidas global para estandarizar las conversiones en memoria.
