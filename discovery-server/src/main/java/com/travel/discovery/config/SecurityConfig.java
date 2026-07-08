package com.travel.discovery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Secures the Eureka dashboard and registration API with HTTP Basic auth.
 *
 * In production, EUREKA_USERNAME and EUREKA_PASSWORD are injected as
 * Kubernetes secrets — never committed to source control.
 *
 * CSRF is disabled because Eureka clients use the registration API
 * programmatically, not through browser forms.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/eureka/**"))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .anyRequest().authenticated())
            .httpBasic(basic -> {})
            .build();
    }
}
