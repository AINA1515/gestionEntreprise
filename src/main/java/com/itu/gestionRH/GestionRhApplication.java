package com.itu.gestionRH;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.core.Ordered;

@SpringBootApplication
@EnableTransactionManagement(order = Ordered.LOWEST_PRECEDENCE - 10)
public class GestionRhApplication {

	public static void main(String[] args) {
		SpringApplication.run(GestionRhApplication.class, args);
	}

}
