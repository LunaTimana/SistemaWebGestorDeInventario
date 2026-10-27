package com.emsstore.backend;

import com.emsstore.backend.security.TokenService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {
    @Test
    void tokenFirmadoSeValidaYConservaUsuarioYRol() {
        TokenService service = new TokenService("test-secret-with-enough-entropy", 600);
        String token = service.crearToken(42, "Almacén");
        TokenService.Claims claims = service.validar(token);
        assertNotNull(claims);
        assertEquals(42, claims.userId());
        assertEquals("Almacén", claims.role());
    }

    @Test
    void tokenAlteradoSeRechaza() {
        TokenService service = new TokenService("test-secret-with-enough-entropy", 600);
        String token = service.crearToken(42, "Administrador");
        assertNull(service.validar(token + "x"));
    }

    @Test
    void tokenExpiradoSeRechaza() {
        TokenService service = new TokenService("test-secret-with-enough-entropy", -1);
        assertNull(service.validar(service.crearToken(7, "Ventas")));
    }
}
