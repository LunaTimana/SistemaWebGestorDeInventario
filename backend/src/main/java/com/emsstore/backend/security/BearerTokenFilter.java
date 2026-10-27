package com.emsstore.backend.security;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class BearerTokenFilter extends OncePerRequestFilter {
    private final TokenService tokenService;
    private final JdbcTemplate jdbc;
    public BearerTokenFilter(TokenService tokenService, JdbcTemplate jdbc) { this.tokenService = tokenService; this.jdbc = jdbc; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            TokenService.Claims claims = tokenService.validar(header.substring(7).trim());
            if (claims != null) {
                List<Map<String,Object>> usuarios = jdbc.queryForList(
                        "SELECT r.nombre AS rol FROM usuarios u JOIN roles r ON r.id=u.rol_id WHERE u.id=? AND u.estado='activo'",
                        claims.userId());
                if (!usuarios.isEmpty()) {
                    String rolActual = String.valueOf(usuarios.get(0).get("rol"));
                    var auth = new UsernamePasswordAuthenticationToken(
                            claims.userId(), null, List.of(new SimpleGrantedAuthority("ROLE_" + rolActual)));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }
        chain.doFilter(request, response);
    }
}
