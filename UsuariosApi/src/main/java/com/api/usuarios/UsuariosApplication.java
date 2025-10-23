package com.api.usuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import com.thewaterfall.throttler.configuration.annotation.EnableThrottler;

@SpringBootApplication
@EnableMongoAuditing
@EnableThrottler
@EnableCaching


public class UsuariosApplication {

	public static void main(String[] args) {
		SpringApplication.run(UsuariosApplication.class, args);
	}

}
