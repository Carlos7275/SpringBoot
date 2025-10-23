package com.api.usuarios.controllers.v1.generos;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.api.usuarios.entities.generos.Generos;
import com.api.usuarios.services.GenerosService;
import com.api.usuarios.services.RedisService;
import com.api.usuarios.utilities.ResponseUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controlador para manejar los endpoints de Géneros.
 */
@RestController
@Tag(name = "Generos", description = "Endpoints para gestión de géneros")
@RequestMapping("api/v1/generos/")
public class GenerosController {

    @Autowired
    private RedisService _redisService;
    @Autowired
    private GenerosService _generosService;

    /**
     * Obtiene la lista de todos los géneros registrados.
     *
     * @return ResponseEntity con la lista de géneros
     * @throws JsonProcessingException
     * @throws JsonMappingException
     */
    @Operation(summary = "Obtener todos los géneros", description = "Devuelve la lista completa de géneros")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de géneros obtenida correctamente")
    })
    @GetMapping
    public ResponseEntity<?> obtenerGeneros() throws JsonMappingException, JsonProcessingException {
        String cacheKey = "generosTodos";

        // 1️⃣ Intentar obtener desde cache
        String cachedJson = (String) _redisService.get(cacheKey);
        if (cachedJson != null) {
            List<Generos> generos = Arrays.asList(
                    new ObjectMapper()
                            .registerModule(new JavaTimeModule())
                            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                            .readValue(cachedJson, Generos[].class));

            return ResponseEntity.ok(
                    ResponseUtil.Response("Operación Exitosa", generos));
        }

        // 2️⃣ Obtener desde la base de datos
        List<Generos> generos = _generosService.findAll();

        // 3️⃣ Guardar en cache como JSON
        String jsonToCache = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .writeValueAsString(generos);

        _redisService.set(cacheKey, jsonToCache, Duration.ofMinutes(10));

        // 4️⃣ Responder
        return ResponseEntity.ok(ResponseUtil.Response("Operación Exitosa", generos));

    }

    /**
     * Obtiene un género específico por su ID.
     *
     * @param id ID del género a buscar
     * @return ResponseEntity con el género encontrado
     */
    @Operation(summary = "Obtener género por ID", description = "Devuelve un género específico según su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Género encontrado correctamente"),
            @ApiResponse(responseCode = "404", description = "No se encontró el género con el ID proporcionado")
    })

    @GetMapping("{id}")
    public ResponseEntity<?> obtenerGeneroEspecifico(
            @Parameter(description = "ID del género", required = true) @PathVariable String id) {

        Generos genero = _generosService.findById(id).orElse(null);

        if (genero == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "No se encontró el género con id: " + id);
        }

        return ResponseEntity.ok(ResponseUtil.Response("Operación Exitosa", genero));
    }

}
