package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import club.ClubDeportivoSeneca;
import inscripciones.InscripcionDisciplinaIndividual;
import inscripciones.InscripcionEquipo;
import usuarios.Administrador;
import usuarios.Entrenador;
import usuarios.Fisioterapeuta;
import usuarios.Socio;
import usuarios.Usuario;


public class TestAutenticacion {

	private ClubDeportivoSeneca club;
	private Socio socio;
	private Entrenador entrenador;
	private Fisioterapeuta fisioterapeuta;

	@BeforeEach
	public void setUp() {
		club = new ClubDeportivoSeneca();

		socio = new Socio("Laura Gomez", "lgomez", "clave123", LocalDate.of(2000, 3, 15));
		club.registrarSocio(socio);

		entrenador = new Entrenador("Carlos Ruiz", "cruiz", "entrena1");
		club.registrarEmpleado(entrenador);

		fisioterapeuta = new Fisioterapeuta("Ana Torres", "atorres", "fisio1");
		club.registrarEmpleado(fisioterapeuta);
	}

	private String mensajeDeRechazo(String login, String password) {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> club.autenticarUsuario(login, password));
		return e.getMessage();
	}

	private void verificarSistemaSinCambios() {
		assertEquals(1, club.getSocios().size());
		assertEquals(2, club.getEmpleados().size());
		assertEquals("clave123", socio.getPassword());
		assertEquals("entrena1", entrenador.getPassword());
		assertEquals("fisio1", fisioterapeuta.getPassword());
		assertEquals(socio, club.autenticarUsuario("lgomez", "clave123"));
	}

	private void verificarMensajeNoRevelaDato(String mensaje) {
		String texto = mensaje.toLowerCase();
		assertFalse(texto.contains("inexistente"));
		assertFalse(texto.contains("no existe"));
		assertFalse(texto.contains("no encontrado"));
		assertFalse(texto.contains("no registrado"));
		boolean mencionaLogin = texto.contains("login") || texto.contains("usuario");
		boolean mencionaContrasena = texto.contains("password") || texto.contains("contrasena") || texto.contains("contraseña");
		assertEquals(mencionaLogin, mencionaContrasena);
	}


	@Test
	public void testS01_loginCorrecto() {
		Usuario usuario = club.autenticarUsuario("lgomez", "clave123");

		assertTrue(usuario instanceof Socio);
		assertEquals(socio, usuario);
		assertEquals("Laura Gomez", usuario.getNombre());
		assertFalse(usuario instanceof Administrador);
	}

	@Test
	public void testS01_loginInexistenteRechazado() {
		String mensaje = mensajeDeRechazo("noexiste", "clave123");

		verificarMensajeNoRevelaDato(mensaje);
		verificarSistemaSinCambios();
	}

	@Test
	public void testS01_contrasenaErradaRechazada() {
		String mensaje = mensajeDeRechazo("lgomez", "otraClave");

		verificarMensajeNoRevelaDato(mensaje);
		verificarSistemaSinCambios();
	}

	@Test
	public void testS01_rechazoNoIndicaCualDatoFallo() {
		String mensajeLoginInexistente = mensajeDeRechazo("noexiste", "clave123");
		String mensajeContrasenaErrada = mensajeDeRechazo("lgomez", "otraClave");

		assertEquals(mensajeLoginInexistente, mensajeContrasenaErrada);
	}

	@Test
	public void testE01_loginCorrectoEntrenador() {
		Usuario usuario = club.autenticarUsuario("cruiz", "entrena1");

		assertTrue(usuario instanceof Entrenador);
		assertFalse(usuario instanceof Fisioterapeuta);
		assertFalse(usuario instanceof Administrador);
		assertEquals(entrenador, usuario);
	}

	@Test
	public void testE01_loginCorrectoFisioterapeuta() {
		Usuario usuario = club.autenticarUsuario("atorres", "fisio1");

		assertTrue(usuario instanceof Fisioterapeuta);
		assertFalse(usuario instanceof Entrenador);
		assertFalse(usuario instanceof Administrador);
		assertEquals(fisioterapeuta, usuario);
	}

	@Test
	public void testE01_credencialesIncorrectasRechazadas() {
		String mensajeContrasenaErrada = mensajeDeRechazo("cruiz", "malaClave");
		String mensajeLoginInexistente = mensajeDeRechazo("empleadoX", "entrena1");

		verificarMensajeNoRevelaDato(mensajeContrasenaErrada);
		assertEquals(mensajeLoginInexistente, mensajeContrasenaErrada);
		verificarSistemaSinCambios();
	}

	@Test
	public void testE01_contrasenaDeOtroEmpleadoRechazada() {
		String mensaje = mensajeDeRechazo("cruiz", "fisio1");

		verificarMensajeNoRevelaDato(mensaje);
		verificarSistemaSinCambios();
	}

	
	@Test
	public void testA01_loginCorrecto() {
		Usuario usuario = club.autenticarUsuario(ClubDeportivoSeneca.LOGIN_ADMIN, ClubDeportivoSeneca.PASSWORD_ADMIN);

		assertTrue(usuario instanceof Administrador);
		assertEquals(club.getAdministrador(), usuario);
	}

	@Test
	public void testA01_contrasenaIncorrectaRechazada() {
		String mensaje = mensajeDeRechazo(ClubDeportivoSeneca.LOGIN_ADMIN, "incorrecta");

		verificarMensajeNoRevelaDato(mensaje);
		verificarSistemaSinCambios();
	}

	@Test
	public void testA01_loginInexistenteRechazado() {
		String mensaje = mensajeDeRechazo("administrador2", ClubDeportivoSeneca.PASSWORD_ADMIN);

		verificarMensajeNoRevelaDato(mensaje);
		verificarSistemaSinCambios();
	}

	@Test
	public void testA01_rechazoNoRevelaCualDatoFallo() {
		String mensajeContrasenaErrada = mensajeDeRechazo(ClubDeportivoSeneca.LOGIN_ADMIN, "incorrecta");
		String mensajeLoginInexistente = mensajeDeRechazo("administrador2", ClubDeportivoSeneca.PASSWORD_ADMIN);

		assertEquals(mensajeLoginInexistente, mensajeContrasenaErrada);
	}

	@Test
	public void testA01_soloElAdministradorAccedeAFuncionesAdministrativas() {
		assertFalse(club.autenticarUsuario("lgomez", "clave123") instanceof Administrador);
		assertFalse(club.autenticarUsuario("cruiz", "entrena1") instanceof Administrador);
		assertFalse(club.autenticarUsuario("atorres", "fisio1") instanceof Administrador);
	}


	@Test
	public void testA20_administradorNoPuedeReservarInstalacion() throws Exception {
		Usuario usuario = club.autenticarUsuario(ClubDeportivoSeneca.LOGIN_ADMIN, ClubDeportivoSeneca.PASSWORD_ADMIN);
		assertTrue(usuario instanceof Administrador);

		assertFalse(usuario instanceof Socio);
		assertFalse(Socio.class.isAssignableFrom(Administrador.class));

		Method reservar = ClubDeportivoSeneca.class.getMethod("reservarInstalacion", List.class,
				instalaciones.CanchaDeporteIndividual.class, LocalDate.class, java.time.LocalTime.class,
				java.time.LocalTime.class, enumeraciones.Modalidad.class);
		Type tipoLista = reservar.getGenericParameterTypes()[0];
		assertTrue(tipoLista instanceof ParameterizedType);
		assertEquals(Socio.class, ((ParameterizedType) tipoLista).getActualTypeArguments()[0]);

		Method[] metodos = Administrador.class.getMethods();
		for (int i = 0; i < metodos.length; i++) {
			assertFalse(metodos[i].getName().toLowerCase().contains("reserv"),
					"El administrador no deberia poder reservar: " + metodos[i].getName());
		}
	}

	@Test
	public void testA20_administradorNoPuedeInscribirseComoParticipanteDePartido() throws Exception {
		Usuario usuario = club.autenticarUsuario(ClubDeportivoSeneca.LOGIN_ADMIN, ClubDeportivoSeneca.PASSWORD_ADMIN);
		assertTrue(usuario instanceof Administrador);
		assertFalse(usuario instanceof Socio);

		
		assertEquals(Socio.class, InscripcionDisciplinaIndividual.class.getMethod("getSocio").getReturnType());
		assertEquals(Socio.class, InscripcionEquipo.class.getMethod("getSocio").getReturnType());

		Method[] metodos = Administrador.class.getMethods();
		for (int i = 0; i < metodos.length; i++) {
			String nombre = metodos[i].getName().toLowerCase();
			assertFalse(nombre.contains("inscribirse") || nombre.contains("participar") || nombre.contains("unirse"),
					"El administrador no deberia poder inscribirse en partidos: " + metodos[i].getName());
		}
	}

	@Test
	public void testA20_administradorNoQuedaRegistradoComoSocio() {
		club.autenticarUsuario(ClubDeportivoSeneca.LOGIN_ADMIN, ClubDeportivoSeneca.PASSWORD_ADMIN);

		assertEquals(null, club.buscarSocio(ClubDeportivoSeneca.LOGIN_ADMIN));
		for (int i = 0; i < club.getSocios().size(); i++) {
			assertFalse(club.getSocios().get(i).getLogin().equals(ClubDeportivoSeneca.LOGIN_ADMIN));
		}
	}
}
