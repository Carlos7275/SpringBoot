package com.api.usuarios.exceptions;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.api.usuarios.annotations.AddBaseUrl;
import com.api.usuarios.utilities.ResponseUtil.ResponseObject;

import io.swagger.v3.oas.annotations.Hidden;

/**
 * Interceptor global que agrega automáticamente la URL base
 * a los campos anotados con {@link AddBaseUrl} dentro de las respuestas JSON.
 * <p>
 * Funciona con respuestas envueltas en {@link ResponseObject},
 * incluso si éstas están dentro de un {@link ResponseEntity}.
 * </p>
 */
@Hidden
@ControllerAdvice
public class AddBaseUrlAdvice implements ResponseBodyAdvice<Object> {

    /**
     * Indica si este interceptor debe aplicarse al tipo de respuesta actual.
     *
     * @param returnType     Tipo de retorno del método del controlador.
     * @param converterType  Tipo de convertidor de mensaje HTTP.
     * @return {@code true} si debe evaluarse la respuesta.
     */
    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    /**
     * Intercepta la respuesta antes de ser escrita en el cuerpo HTTP.
     * Si el cuerpo es un {@link ResponseObject}, agrega la URL base
     * a todos los campos anotados con {@link AddBaseUrl}.
     *
     * @param body                  Cuerpo de la respuesta.
     * @param returnType            Tipo de retorno del método.
     * @param selectedContentType   Tipo de contenido seleccionado.
     * @param selectedConverterType Convertidor utilizado.
     * @param request               Petición HTTP actual.
     * @param response              Respuesta HTTP actual.
     * @return Objeto modificado o el original si no aplica.
     */
    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        if (body == null) return null;

        Object realBody = body;
        if (body instanceof ResponseEntity<?> entity) {
            realBody = entity.getBody();
        }

        if (realBody instanceof ResponseObject<?> wrapper) {
            Object data = wrapper.getData();
            if (data != null) {
                String baseUrl = request.getURI().getScheme() + "://" +
                                 request.getURI().getHost() +
                                 ((request.getURI().getPort() != -1)
                                         ? ":" + request.getURI().getPort()
                                         : "");
                addBaseUrlToFields(data, baseUrl);
            }
        }

        return body;
    }

    /**
     * Agrega la URL base a los campos anotados con {@link AddBaseUrl}
     * dentro del objeto recibido, aplicando recursión para estructuras anidadas.
     *
     * @param object  Objeto a procesar.
     * @param baseUrl URL base a agregar.
     */
    private void addBaseUrlToFields(Object object, String baseUrl) {
        if (object == null) return;

        Class<?> clazz = object.getClass();

        if (object instanceof Collection<?> collection) {
            for (Object item : collection) addBaseUrlToFields(item, baseUrl);
            return;
        }

        if (clazz.isArray()) {
            int length = Array.getLength(object);
            for (int i = 0; i < length; i++) {
                addBaseUrlToFields(Array.get(object, i), baseUrl);
            }
            return;
        }

        if (object instanceof Map<?, ?> map) {
            for (Object value : map.values()) addBaseUrlToFields(value, baseUrl);
            return;
        }

        if (clazz.isPrimitive() || clazz.isEnum() || clazz.getName().startsWith("java.")) {
            return;
        }

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(object);

                if (field.isAnnotationPresent(AddBaseUrl.class)
                        && value instanceof String str
                        && !str.startsWith("http")) {

                    String fixed = str.startsWith("/") ? str : "/" + str;
                    field.set(object, baseUrl + fixed);

                } else {
                    addBaseUrlToFields(value, baseUrl);
                }

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
