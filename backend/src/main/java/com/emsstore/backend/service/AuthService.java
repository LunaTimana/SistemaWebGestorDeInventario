package com.emsstore.backend.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emsstore.backend.dto.LoginRequest;
import com.emsstore.backend.dto.LoginResponse;
import com.emsstore.backend.dto.UsuarioResponse;
import com.emsstore.backend.repository.UsuarioRepository;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final UsuarioRepository usuarios;
    private final long ttlSeconds;

    public AuthService(AuthenticationManager authenticationManager, JwtEncoder jwtEncoder,
                       UsuarioRepository usuarios, @Value("${app.jwt.ttl-seconds:3600}") long ttlSeconds) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.usuarios = usuarios;
        this.ttlSeconds = ttlSeconds;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        request.correo().trim().toLowerCase(Locale.ROOT), request.contrasena()));
        var usuario = usuarios.findByCorreoIgnoreCase(authentication.getName()).orElseThrow();
        usuario.setUltimaConexion(LocalDateTime.now());

        Instant ahora = Instant.now();
        List<String> autoridades = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();
        var claims = JwtClaimsSet.builder()
                .issuer("ems-store")
                .issuedAt(ahora)
                .expiresAt(ahora.plusSeconds(ttlSeconds))
                .subject(usuario.getCorreo())
                .claim("uid", usuario.getId())
                .claim("name", usuario.getNombre() + " " + usuario.getApellido())
                .claim("authorities", autoridades)
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new LoginResponse(token, "Bearer", ttlSeconds, UsuarioResponse.desde(usuario));
    }
}
