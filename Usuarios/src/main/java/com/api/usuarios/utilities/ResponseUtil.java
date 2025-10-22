package com.api.usuarios.utilities;

import com.fasterxml.jackson.annotation.JsonInclude;

public class ResponseUtil {
    @JsonInclude(JsonInclude.Include.NON_NULL) // Incluir solo campos no nulos
    public static class ResponseObject<T> {
        public String message;
        public T data;
        public String search;
        public Integer total;

        public ResponseObject(String message, T data, String search, Integer total) {
            this.message = message;
            this.data = data;
            this.search = search;
            this.total = total;
        }
    }

    public static <T> ResponseObject<T> Response(String message, T data, String search, Integer total) {
        return new ResponseObject<>(message, data, search, total);
    }

    public static <T> ResponseObject<T> Response(String message, T data) {
        return new ResponseObject<>(message, data, null, null);
    }

    public static <T> ResponseObject<T> Response(String message) {
        return new ResponseObject<>(message, null, null, null);
    }
}
