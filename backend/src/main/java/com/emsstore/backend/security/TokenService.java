package com.emsstore.backend.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    private final byte[] secret;
    private final long durationSeconds;

    public TokenService(
            @Value("${ems.jwt.secret:${JWT_SECRET:EMS-Store-dev-secret-change-before-production-2026}}") String secret,
            @Value("${ems.jwt.duration-seconds:28800}") long durationSeconds) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.durationSeconds = durationSeconds;
    }

    public String crearToken(int userId, String role) {
        long expires = Instant.now().getEpochSecond() + durationSeconds;
        String payload = userId + "|" + role + "|" + expires;
        String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return encoded + "." + firmar(encoded);
    }

    public Claims validar(String token) {
        try {
            String[] parts = token.split("\\.", -1);
            if (parts.length != 2 || !constanteIgual(firmar(parts[0]), parts[1])) return null;
            String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String[] values = payload.split("\\|", -1);
            if (values.length != 3) return null;
            int id = Integer.parseInt(values[0]);
            long expiry = Long.parseLong(values[2]);
            if (expiry <= Instant.now().getEpochSecond()) return null;
            if (!values[1].matches("Administrador|Almacén|Ventas")) return null;
            return new Claims(id, values[1]);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private String firmar(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo firmar el token", ex);
        }
    }

    private static boolean constanteIgual(String a, String b) {
        return java.security.MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    public record Claims(int userId, String role) {}
}
