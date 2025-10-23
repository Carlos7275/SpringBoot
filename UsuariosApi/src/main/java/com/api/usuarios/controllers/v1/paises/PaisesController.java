package com.api.usuarios.controllers.v1.paises;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.usuarios.entities.paises.Paises;
import com.api.usuarios.services.PaisesService;
import com.api.usuarios.services.RedisService;
import com.api.usuarios.utilities.ResponseUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Paises", description = "Endpoints de gestión de paises")
@RequestMapping("/api/v1/paises/")
public class PaisesController {

    @Autowired
    private PaisesService _paisesService;

    @Autowired
    private RedisService _redisService;

    @GetMapping
    public ResponseEntity<?> obtenerPaises() throws JsonProcessingException {
        String cacheKey = "paisesTodos";

        String cachedJson = (String) _redisService.get(cacheKey);
        if (cachedJson != null) {
            List<Paises> paises = Arrays.asList(
                    new ObjectMapper()
                            .registerModule(new JavaTimeModule())
                            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                            .readValue(cachedJson, Paises[].class));

            return ResponseEntity.ok(
                    ResponseUtil.Response("Operación Exitosa", paises));
        }

        List<Paises> paises = _paisesService.findAll();

        String jsonToCache = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .writeValueAsString(paises);

        _redisService.set(cacheKey, jsonToCache, Duration.ofMinutes(10));

        return ResponseEntity.ok(ResponseUtil.Response("Operación Exitosa", paises));

    }

}
