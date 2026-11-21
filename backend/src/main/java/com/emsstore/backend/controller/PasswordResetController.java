package com.emsstore.backend.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final JdbcTemplate jdbc;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final String mailUsername;
    private final String frontendUrl;

    public PasswordResetController(JdbcTemplate jdbc, JavaMailSender mailSender,
            PasswordEncoder passwordEncoder,
            @Value("${spring.mail.username:}") String mailUsername,
            @Value("${spring.mail.password:}") String mailPassword,
            @Value("${app.frontend-url:http://localhost:4200}") String frontendUrl) {
        this.jdbc = jdbc;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
        this.mailUsername = mailUsername == null ? "" : mailUsername.trim();
        if (mailSender instanceof JavaMailSenderImpl sender && mailPassword != null)
            sender.setPassword(mailPassword.replaceAll("\\s", ""));
        this.frontendUrl = frontendUrl;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> manejarError(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(Map.of("message", ex.getReason() == null ? "No se pudo completar la solicitud." : ex.getReason()));
    }

    @PostMapping("/forgot-password")
    public Map<String, String> solicitar(@Valid @RequestBody Solicitud request) {
        if (mailUsername == null || mailUsername.isBlank())
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "El envío de correo no está configurado. Define MAIL_USERNAME y MAIL_PASSWORD.");
        String generic = "Si el correo pertenece a una cuenta activa, recibirás un enlace para restablecer la contraseña.";
        List<Integer> users = jdbc.query("SELECT id FROM usuarios WHERE LOWER(correo)=LOWER(?) AND estado='activo'", (rs, n) -> rs.getInt(1), request.correo().trim());
        if (users.isEmpty()) return Map.of("mensaje", generic);

        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String hash = hash(token);
        Integer userId = users.get(0);
        jdbc.update("DELETE FROM password_reset_tokens WHERE usuario_id=?", userId);
        jdbc.update("INSERT INTO password_reset_tokens(usuario_id,token_hash,expira_en) VALUES(?,?,?)",
                userId, hash, LocalDateTime.now().plusMinutes(30));

        String link = frontendUrl.replaceAll("/$", "") + "/restablecer-contrasena?token=" + token;
        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(mailUsername);
        email.setTo(request.correo().trim());
        email.setSubject("Recuperación de contraseña | EMS Store");
        email.setText("Recibimos una solicitud para cambiar la contraseña de tu cuenta.\n\n"
                + "Abre este enlace dentro de los próximos 30 minutos:\n" + link
                + "\n\nSi no solicitaste el cambio, ignora este mensaje.");
        try {
            mailSender.send(email);
        } catch (MailException ex) {
            jdbc.update("DELETE FROM password_reset_tokens WHERE token_hash=?", hash);
            System.err.println("No se pudo enviar correo de recuperación con Gmail: " + ex.getMessage());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Gmail rechazó el envío. Verifica MAIL_USERNAME y MAIL_PASSWORD; usa una contraseña de aplicación de Gmail con verificación en dos pasos.");
        }
        return Map.of("mensaje", generic);
    }

    @PostMapping("/reset-password")
    @Transactional
    public Map<String, String> restablecer(@Valid @RequestBody Restablecimiento request) {
        if (!request.nueva().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,72}$"))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La clave debe incluir 8-72 caracteres, mayúscula, minúscula, número y símbolo.");
        List<Integer> users = jdbc.query("SELECT usuario_id FROM password_reset_tokens WHERE token_hash=? AND expira_en > NOW()",
                (rs, n) -> rs.getInt(1), hash(request.token()));
        if (users.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "El enlace venció o ya fue utilizado. Solicita uno nuevo.");
        Integer userId = users.get(0);
        jdbc.update("UPDATE usuarios SET contrasena_hash=?, debe_cambiar_contrasena=FALSE WHERE id=? AND estado='activo'",
                passwordEncoder.encode(request.nueva()), userId);
        jdbc.update("DELETE FROM password_reset_tokens WHERE usuario_id=?", userId);
        return Map.of("mensaje", "Contraseña actualizada. Ya puedes iniciar sesión.");
    }

    private static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(64);
            for (byte b : digest) result.append(String.format("%02x", b));
            return result.toString();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo procesar el token de recuperación", e);
        }
    }

    public record Solicitud(@NotBlank @Email @Size(max = 254) String correo) {}
    public record Restablecimiento(@NotBlank String token, @NotBlank @Size(max = 72) String nueva) {}
}
