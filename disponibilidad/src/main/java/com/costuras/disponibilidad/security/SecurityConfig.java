package com.costuras.disponibilidad.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse; 

@Configuration
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) { 
        this.jwtUtil = jwtUtil; 
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(c -> c.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.DELETE, "/reservas/admin/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.GET,    "/reservas/admin/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.GET, "/reservas/horas-ocupadas").authenticated()
                .requestMatchers(
                        "/auth/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
            
            
            .exceptionHandling(exception -> exception
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN); 
                    response.setContentType("text/plain;charset=UTF-8");
                    response.getWriter().write("No tienes acceso");
                    response.getWriter().flush();
                })
            );

        return http.build();
    }
}