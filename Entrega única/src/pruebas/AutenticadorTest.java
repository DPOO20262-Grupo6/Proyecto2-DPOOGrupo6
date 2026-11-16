package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import autenticacion.Autenticador;
import club.ClubDeportivoSeneca;
import usuarios.Administrador;
import usuarios.EmpleadoDelClub;
import usuarios.Entrenador;
import usuarios.Fisioterapeuta;
import usuarios.Socio;
import usuarios.Usuario;

/**
 * Pruebas unitarias del inicio de sesion que comparten las tres consolas.
 * Cada consola solo deja entrar a su tipo de usuario (HU-S01, HU-E01 y HU-A01).
 */
public class AutenticadorTest {

	private ClubDeportivoSeneca club;
	private Autenticador autenticador;
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
		autenticador = new Autenticador(club);
	}

	private String mensajeDeRechazo(String login, String password, Class<? extends Usuario> rol) {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> autenticador.iniciarSesion(login, password, rol));
		return e.getMessage();
	}

	// ---------------- Camino de exito por rol ----------------

	@Test
	public void testS01_socioIniciaSesionEnLaConsolaDeSocio() {
		Socio autenticado = autenticador.iniciarSesionSocio("lgomez", "clave123");

		assertSame(socio, autenticado);
		assertEquals(0, autenticador.getIntentosFallidos());
	}

	@Test
	public void testE01_entrenadorIniciaSesionEnLaConsolaDeEmpleado() {
		EmpleadoDelClub autenticado = autenticador.iniciarSesionEmpleado("cruiz", "entrena1");

		assertSame(entrenador, autenticado);
		assertTrue(autenticado instanceof Entrenador);
	}

	@Test
	public void testE01_fisioterapeutaIniciaSesionEnLaConsolaDeEmpleado() {
		EmpleadoDelClub autenticado = autenticador.iniciarSesionEmpleado("atorres", "fisio1");

		assertSame(fisioterapeuta, autenticado);
		assertTrue(autenticado instanceof Fisioterapeuta);
	}

	@Test
	public void testA01_administradorIniciaSesionEnLaConsolaDeAdministrador() {
		Administrador autenticado = autenticador.iniciarSesionAdministrador(ClubDeportivoSeneca.LOGIN_ADMIN, ClubDeportivoSeneca.PASSWORD_ADMIN);

		assertSame(club.getAdministrador(), autenticado);
	}

	@Test
	public void testLoginConEspaciosAlrededorEsAceptado() {
		assertSame(socio, autenticador.iniciarSesionSocio("  lgomez  ", "clave123"));
	}

	// ---------------- Rechazos ----------------

	@Test
	public void testE01_socioNoEntraALaConsolaDeEmpleado() {
		String mensaje = mensajeDeRechazo("lgomez", "clave123", EmpleadoDelClub.class);

		assertEquals(Autenticador.MENSAJE_RECHAZO, mensaje);
	}

	@Test
	public void testA01_empleadoNoEntraALaConsolaDeAdministrador() {
		String mensaje = mensajeDeRechazo("cruiz", "entrena1", Administrador.class);

		assertEquals(Autenticador.MENSAJE_RECHAZO, mensaje);
	}

	@Test
	public void testS01_administradorNoEntraALaConsolaDeSocio() {
		String mensaje = mensajeDeRechazo(ClubDeportivoSeneca.LOGIN_ADMIN, ClubDeportivoSeneca.PASSWORD_ADMIN, Socio.class);

		assertEquals(Autenticador.MENSAJE_RECHAZO, mensaje);
	}

	@Test
	public void testRechazoNoIndicaCualDatoFallo() {
		String loginInexistente = mensajeDeRechazo("noexiste", "clave123", Socio.class);
		String contrasenaErrada = mensajeDeRechazo("lgomez", "otraClave", Socio.class);
		autenticador = new Autenticador(club);
		String rolEquivocado = mensajeDeRechazo("lgomez", "clave123", Administrador.class);

		assertEquals(loginInexistente, contrasenaErrada);
		assertEquals(loginInexistente, rolEquivocado);
	}

	@Test
	public void testDatosVaciosONulosSonRechazados() {
		assertEquals(Autenticador.MENSAJE_RECHAZO, mensajeDeRechazo("", "clave123", Socio.class));
		assertEquals(Autenticador.MENSAJE_RECHAZO, mensajeDeRechazo("lgomez", "   ", Socio.class));
		autenticador = new Autenticador(club);
		assertEquals(Autenticador.MENSAJE_RECHAZO, mensajeDeRechazo(null, "clave123", Socio.class));
		assertEquals(Autenticador.MENSAJE_RECHAZO, mensajeDeRechazo("lgomez", null, Socio.class));
	}

	@Test
	public void testS01_socioDadoDeBajaNoIniciaSesion() {
		socio.darDeBaja();

		assertEquals(Autenticador.MENSAJE_RECHAZO, mensajeDeRechazo("lgomez", "clave123", Socio.class));
	}

	@Test
	public void testRechazoDejaElClubSinCambios() {
		mensajeDeRechazo("lgomez", "otraClave", Socio.class);
		mensajeDeRechazo("cruiz", "entrena1", Administrador.class);

		assertEquals(1, club.getSocios().size());
		assertEquals(2, club.getEmpleados().size());
		assertEquals("clave123", socio.getPassword());
		assertEquals("entrena1", entrenador.getPassword());
		assertSame(socio, club.autenticarUsuario("lgomez", "clave123"));
	}

	// ---------------- Limite de intentos ----------------

	@Test
	public void testCadaRechazoConsumeUnIntento() {
		assertEquals(Autenticador.MAXIMO_INTENTOS, autenticador.getIntentosRestantes());

		mensajeDeRechazo("lgomez", "mala", Socio.class);

		assertEquals(1, autenticador.getIntentosFallidos());
		assertEquals(Autenticador.MAXIMO_INTENTOS - 1, autenticador.getIntentosRestantes());
		assertFalse(autenticador.estaBloqueado());
	}

	@Test
	public void testTresRechazosBloqueanElInicioDeSesion() {
		for (int i = 0; i < Autenticador.MAXIMO_INTENTOS; i++) {
			mensajeDeRechazo("lgomez", "mala" + i, Socio.class);
		}

		assertTrue(autenticador.estaBloqueado());
		assertEquals(0, autenticador.getIntentosRestantes());
		assertThrows(IllegalStateException.class, () -> autenticador.iniciarSesionSocio("lgomez", "clave123"));
	}

	@Test
	public void testInicioExitosoReiniciaLosIntentos() {
		mensajeDeRechazo("lgomez", "mala1", Socio.class);
		mensajeDeRechazo("lgomez", "mala2", Socio.class);

		autenticador.iniciarSesionSocio("lgomez", "clave123");

		assertEquals(0, autenticador.getIntentosFallidos());
		assertEquals(Autenticador.MAXIMO_INTENTOS, autenticador.getIntentosRestantes());
	}

	// ---------------- Datos de construccion ----------------

	@Test
	public void testAutenticadorSinClubEsRechazado() {
		assertThrows(IllegalArgumentException.class, () -> new Autenticador(null));
	}

	@Test
	public void testRolNuloEsRechazadoSinConsumirIntento() {
		assertThrows(IllegalArgumentException.class, () -> autenticador.iniciarSesion("lgomez", "clave123", null));
		assertEquals(0, autenticador.getIntentosFallidos());
	}
}
