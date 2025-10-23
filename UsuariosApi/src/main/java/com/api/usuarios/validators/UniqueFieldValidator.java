package com.api.usuarios.validators;

import org.springframework.context.ApplicationContext;

import com.api.usuarios.annotations.UniqueField;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueFieldValidator implements ConstraintValidator<UniqueField, String> {

    private Class<?> serviceClass;
    private String methodName;
    private String fieldName;
    private final ApplicationContext context;

    public UniqueFieldValidator(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public void initialize(UniqueField annotation) {
        this.serviceClass = annotation.service();
        this.methodName = annotation.method();
        this.fieldName = annotation.field();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty())
            return true;

        try {
            Object service = this.context.getBean(serviceClass);
            var method = serviceClass.getMethod(methodName, String.class, Object.class);
            Object result = method.invoke(service, fieldName, value);

            // TRUE si el valor es único (no existe)
            if (result instanceof Boolean exists) {
                return !exists;
            }
            if (result instanceof java.util.Optional<?> opt) {
                return opt.isEmpty();
            }
            return result == null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
