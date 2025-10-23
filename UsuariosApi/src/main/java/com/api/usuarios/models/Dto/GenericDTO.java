package com.api.usuarios.models.Dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class GenericDTO {
    private String id;
    private LocalDateTime created;
    private LocalDateTime updated;

}
