package com.costuras.disponibilidad.security;




import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) { this.jwtUtil = jwtUtil; }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(c -> c.disable())
            .authorizeHttpRequests(auth -> auth
                // Solo admins pueden cancelar citas de otros usuarios
                .requestMatchers(HttpMethod.DELETE, "/reservas/admin/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.GET,    "/reservas/admin/**").hasAuthority("ADMIN")
                // Endpoint interno usado por MS-Agenda (requiere JWT válido)
                .requestMatchers(HttpMethod.GET, "/reservas/horas-ocupadas").authenticated()
                // El resto requiere autenticación
                .anyRequest().authenticated()
            )
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
