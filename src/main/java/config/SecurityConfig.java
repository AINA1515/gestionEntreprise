package com.itu.gestionRH.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()          // temporaire : toutes les pages visibles
            )
            .formLogin(form -> form
                .loginPage("/login")               // votre page, pas celle de Spring
                .permitAll()
            )
            .csrf(csrf -> csrf.disable());         // temporaire : le formulaire n'a pas de token
        return http.build();
    }
}