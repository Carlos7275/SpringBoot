package com.api.usuarios.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.api.usuarios.validators.EdadMinimaValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EdadMinimaValidator.class)
public @interface EdadMinima {
    String message() default "La fecha de nacimiento no es válida";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    int minEdad() default 0;    // edad mínima
    int maxEdad() default 120;  // edad máxima lógica
}

