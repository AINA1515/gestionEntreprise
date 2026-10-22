package com.itu.gestionRH;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@SpringBootTest
@EnabledIfSystemProperty(named = "oracle.tests.enabled", matches = "true")
class GestionRhApplicationTests {

	@Test
	void contextLoads() {
	}

}
