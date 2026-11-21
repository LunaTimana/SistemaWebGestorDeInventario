package com.emsstore.backend;

import com.emsstore.backend.security.TokenService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {
    @Test
    void tokenFirmadoSeValidaYConservaUsuarioYRol() {
        TokenService service = new TokenService("test-secret-with-more-than-enough-entropy", 600);
        String token = service.crearToken(42, "Almacén");
        TokenService.Claims claims = service.validar(token);
        assertNotNull(claims);
        assertEquals(42, claims.userId());
        assertEquals("Almacén", claims.role());
    }

    @Test
    void tokenAlteradoSeRechaza() {
        TokenService service = new TokenService("test-secret-with-more-than-enough-entropy", 600);
        String token = service.crearToken(42, "Administrador");
        assertNull(service.validar(token + "x"));
    }

    @Test
    void rechazaConfiguracionDeClaveDebilODuracionInvalida() {
        assertThrows(IllegalStateException.class, () -> new TokenService("weak", 600));
        assertThrows(IllegalStateException.class, () -> new TokenService("test-secret-with-more-than-enough-entropy", -1));
    }
}
