package com.api.usuarios.entities;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public abstract class GenericEntity {
    @Id
    private String id;
    private LocalDateTime created;
    private LocalDateTime updated;
}
