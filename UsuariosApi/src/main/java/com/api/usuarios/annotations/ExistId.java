package com.api.usuarios.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.api.usuarios.validators.ExistIdValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = ExistIdValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ExistId {
    String message() default "El ID no existe en la colección correspondiente";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    // Indicar la colección o servicio a validar
    Class<?> service();
}
