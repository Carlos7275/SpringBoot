package com.api.usuarios.validators;

import java.time.LocalDate;

import com.api.usuarios.annotations.EdadMinima;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EdadMinimaValidator implements ConstraintValidator<EdadMinima, LocalDate> {

    private int edadMinima;

    @Override
    public void initialize(EdadMinima constraintAnnotation) {
        this.edadMinima = constraintAnnotation.minEdad();
    }

   

    @Override
    public boolean isValid(LocalDate fechaNacimiento, ConstraintValidatorContext context) {
        if (fechaNacimiento == null) return true; // dejar que @NotNull lo valide si se requiere
        return fechaNacimiento.isBefore(LocalDate.now().minusYears(edadMinima));
    }
}
