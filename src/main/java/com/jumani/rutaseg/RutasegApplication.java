package com.jumani.rutaseg;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class RutasegApplication {

	public static void main(String[] args) {
		SpringApplication.run(RutasegApplication.class, args);
		log.info("\u001B[32m \n\n--- Aplicación iniciada. Listos para recibir tráfico :) ---\u001B[0m");
	}

}
