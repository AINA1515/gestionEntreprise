package com.itu.gestionRH;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableTransactionManagement(order = Ordered.LOWEST_PRECEDENCE - 10)
@EnableScheduling
public class GestionRhApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionRhApplication.class, args);
	}

}
