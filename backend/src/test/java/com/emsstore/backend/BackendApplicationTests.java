package com.emsstore.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import com.emsstore.backend.model.EstadoUsuario;
import com.emsstore.backend.model.Rol;
import com.emsstore.backend.model.Usuario;
import com.emsstore.backend.repository.RolRepository;
import com.emsstore.backend.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class BackendApplicationTests {
	@Autowired
	private MockMvc mvc;
	@Autowired
	private UsuarioRepository usuarios;
	@Autowired
	private RolRepository roles;
	@Autowired
	private PasswordEncoder encoder;

	@Test
	void contextLoads() {
	}

	@Test
	void protegeApiYPublicaLogin() throws Exception {
		mvc.perform(get("/api/inventario")).andExpect(status().isUnauthorized());
		mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"correo\":\"noexiste@emsstore.com\",\"contrasena\":\"incorrecta\"}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void loginConBCryptEmiteJwtAceptadoPorApiProtegida() throws Exception {
		Rol rol = new Rol();
		rol.setNombre("Administrador");
		rol = roles.save(rol);
		Usuario usuario = new Usuario();
		usuario.setNombre("Admin");
		usuario.setApellido("Prueba");
		usuario.setCorreo("admin@emsstore.test");
		usuario.setContrasenaHash(encoder.encode("clave-segura"));
		usuario.setDebeCambiarContrasena(false);
		usuario.setEstado(EstadoUsuario.activo);
		usuario.setRol(rol);
		usuarios.save(usuario);

		var respuesta = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"correo\":\"admin@emsstore.test\",\"contrasena\":\"clave-segura\"}"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.tipo").value("Bearer"))
				.andReturn();
		String token = com.jayway.jsonpath.JsonPath.read(respuesta.getResponse().getContentAsString(), "$.token");
		mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk());
	}

}
