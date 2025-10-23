package com.api.usuarios.utilities;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ResponseUtil {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResponseObject<T> {
        private String message;
        private T data;
        private String search;
        private Integer total;
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
