package com.api.usuarios.controllers.v1.roles;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.usuarios.config.security.SecuredEndpoint;
import com.api.usuarios.entities.roles.Roles;
import com.api.usuarios.services.RolesService;
import com.api.usuarios.utilities.ResponseUtil;

import io.swagger.v3.oas.annotations.tags.Tag;



@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "Roles", description = "Endpoints para la gestion de Roles")
public class RolesController {

    @Autowired
    private RolesService _rolesService;

    @GetMapping("/")
    @SecuredEndpoint
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> obtenerRoles() {
        List<Roles> roles = _rolesService.findAll();

        return new ResponseEntity<>(
                ResponseUtil.Response("Operación Exitosa", roles),
                HttpStatus.OK);
    }
}
