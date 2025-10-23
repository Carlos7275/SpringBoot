package com.api.usuarios.services;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitService {

    private final RabbitTemplate rabbitTemplate;

    public RabbitService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Enviar mensaje a cualquier cola
     */
    public <T> void enviarMensaje(String queueName, T mensaje) {
        rabbitTemplate.convertAndSend(queueName, mensaje);
        System.out.println("Mensaje enviado a " + queueName + ": " + mensaje);
    }
}
