package com.api.usuarios.validators;

import java.lang.reflect.Method;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.api.usuarios.annotations.ExistId;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ExistIdValidator implements ConstraintValidator<ExistId, String> {

    private Class<?> serviceClass;

    @Autowired
    private ApplicationContext context;

    @Override
    public void initialize(ExistId constraintAnnotation) {
        this.serviceClass = constraintAnnotation.service();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) return true; // opcional

        try {
            Object service = this.context.getBean(serviceClass);
            Method method = findMethod(serviceClass, "findById");

            if (method == null) {
                throw new RuntimeException("El método findById no existe en " + serviceClass.getName());
            }

            Object result = method.invoke(service, value);

            if (result instanceof Optional<?> opt) {
                return opt.isPresent();
            }

            if (result instanceof Boolean b) {
                return b;
            }

            return result != null;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Method findMethod(Class<?> clazz, String name) {
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == 1) {
                return m;
            }
        }
        return null;
    }
}
