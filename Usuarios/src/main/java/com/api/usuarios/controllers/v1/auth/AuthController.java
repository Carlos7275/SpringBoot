package com.api.usuarios.controllers.v1.auth;

import org.springframework.beans.factory.annotation.Autowired;


import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.usuarios.services.AuthService;

@RestController
@Tag(name = "Auth", description = "")
@RequestMapping("v1/api/auth/")
public class AuthController {

    @Autowired
    private AuthService _authService;

    @PostMapping("login")
    public String login() {
        return new String();
    }

    @GetMapping("me")
    public String me() {
        return new String();
    }

    @PostMapping("logout")
    public String logout() {
        return null;
    }

    @GetMapping("refresh")
    public String refresh() {
        return new String();
    }

}
