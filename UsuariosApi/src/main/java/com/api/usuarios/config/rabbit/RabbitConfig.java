package com.api.usuarios.config.rabbit;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String QUEUE_USUARIOS = "usuarios.queue";

    @Bean
    public Queue usuariosQueue() {
        return new Queue(QUEUE_USUARIOS, true);
    }
}
