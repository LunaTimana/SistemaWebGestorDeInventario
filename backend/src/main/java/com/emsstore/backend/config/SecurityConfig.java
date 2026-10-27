package com.emsstore.backend.config;

import com.emsstore.backend.security.BearerTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, BearerTokenFilter bearerTokenFilter) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login", "/api/health", "/error").permitAll()
                .requestMatchers("/api/usuarios/**", "/api/reportes/**").hasRole("Administrador")
                .requestMatchers(HttpMethod.POST, "/api/productos/**").hasAnyRole("Administrador", "Almacén")
                .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasAnyRole("Administrador", "Almacén")
                .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasRole("Administrador")
                .requestMatchers(HttpMethod.POST, "/api/variantes/**").hasAnyRole("Administrador", "Almacén")
                .requestMatchers(HttpMethod.PUT, "/api/variantes/**").hasAnyRole("Administrador", "Almacén")
                .requestMatchers(HttpMethod.DELETE, "/api/variantes/**").hasRole("Administrador")
                .requestMatchers(HttpMethod.POST, "/api/marcas/**", "/api/categorias/**", "/api/proveedores/**").hasRole("Administrador")
                .requestMatchers(HttpMethod.PUT, "/api/marcas/**", "/api/categorias/**", "/api/proveedores/**").hasRole("Administrador")
                .requestMatchers(HttpMethod.DELETE, "/api/marcas/**", "/api/categorias/**", "/api/proveedores/**").hasRole("Administrador")
                .requestMatchers(HttpMethod.POST, "/api/inventario/importar/excel", "/api/movimientos/**").hasAnyRole("Administrador", "Almacén")
                .requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/marcas/**", "/api/categorias/**", "/api/proveedores/**", "/api/variantes/**", "/api/movimientos/**", "/api/alertas/**", "/api/dashboard/**", "/api/inventario/exportar/**").hasAnyRole("Administrador", "Almacén", "Ventas")
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll())
            .addFilterBefore(bearerTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
