package com.api.usuarios.models.Dto.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginDto(
        @NotBlank(message = "El correo no debe estar vácio") @Email(message = "El correo ingresado no es válido") String email,
        @NotBlank(message = "La contraseña no debe estar vacía") String password,
        Boolean sesionactiva) {

}