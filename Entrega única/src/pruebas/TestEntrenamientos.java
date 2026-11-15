package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import club.ClubDeportivoSeneca;
import deportesDeConjunto.EquipoFutbol;
import entrenamiento.RegistroAsistencia;
import entrenamiento.SesionDeEntrenamiento;
import enumeraciones.EstadoAsistencia;
import enumeraciones.Modalidad;
import enumeraciones.NivelHabilidad;
import inscripciones.InscripcionEquipo;
import instalaciones.CanchaPadel;
import instalaciones.CanchaTenis;
import instalaciones.InstalacionDeporteConjunto;
import usuarios.Entrenador;
import usuarios.Socio;

public class TestEntrenamientos {

	private ClubDeportivoSeneca club;
	private Entrenador eva;
	private Entrenador otroEntrenador;
	private Socio ana;
	private Socio beto;
	private Socio carla;
	private CanchaTenis tenis1;
	private InstalacionDeporteConjunto canchaFutbol;
	private EquipoFutbol equipo;
	private InscripcionEquipo inactivo;
	private LocalDate manana;
	private LocalTime diez;

	@BeforeEach
	public void setUp() {
		club = new ClubDeportivoSeneca();
		eva = new Entrenador("Eva", "eva", "123");
		otroEntrenador = new Entrenador("Hugo", "hugo", "123");
		ana = crearSocio("Ana");
		beto = crearSocio("Beto");
		carla = crearSocio("Carla");
		club.inscribirEnAcademia(ana, "TENIS", NivelHabilidad.INTERMEDIO);
		club.inscribirEnAcademia(beto, "TENIS", NivelHabilidad.INTERMEDIO);
		tenis1 = new CanchaTenis("Tenis 1");
		canchaFutbol = new InstalacionDeporteConjunto("Cancha central", "FUTBOL");
		equipo = new EquipoFutbol("Halcones", "MAYORES", 20);
		for (int i = 1; i <= 8; i++) {
			equipo.agregarJugador(new InscripcionEquipo(crearSocio("Jugador" + i), equipo, "Delantero", i));
		}
		inactivo = equipo.getInscripciones().get(7);
		inactivo.setActiva(false);
		manana = LocalDate.now().plusDays(1);
		diez = LocalTime.of(10, 0);
	}

	private Socio crearSocio(String nombre) {
		Socio socio = new Socio(nombre, nombre.toLowerCase(), "123", LocalDate.of(2000, 1, 1));
		club.registrarSocio(socio);
		return socio;
	}

	private SesionDeEntrenamiento sesionAcademia() {
		return club.planearSesionAcademia(eva, manana, diez, 60, tenis1, Arrays.asList(ana, beto));
	}

	private SesionDeEntrenamiento sesionFinalizada() {
		SesionDeEntrenamiento sesion = sesionAcademia();
		sesion.setFecha(LocalDate.now().minusDays(1));
		return sesion;
	}

	private void verificarSesionRechazada(String motivo, Executable accion) {
		int sesiones = club.getSesiones().size();
		int sesionesEva = eva.getSesiones().size();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, accion);
		assertTrue(e.getMessage().toLowerCase().contains(motivo), e.getMessage());
		assertEquals(sesiones, club.getSesiones().size());
		assertEquals(sesionesEva, eva.getSesiones().size());
	}

	@Test
	public void testT01_sesionDeEquipoConvocaJugadoresActivos() {
		SesionDeEntrenamiento sesion = club.planearSesionEquipo(eva, manana, diez, 90, canchaFutbol, equipo);

		assertTrue(club.getSesiones().contains(sesion));
		assertEquals(7, sesion.getConvocados().size());
		assertFalse(sesion.getConvocados().contains(inactivo.getSocio()));
	}

	@Test
	public void testT01_sesionDeAcademiaCreada() {
		SesionDeEntrenamiento sesion = club.planearSesionAcademia(eva, manana, diez, 60, tenis1, Arrays.asList(ana));

		assertTrue(club.getSesiones().contains(sesion));
		assertEquals(Arrays.asList(ana), sesion.getConvocados());
	}

	@Test
	public void testT01_instalacionOcupadaRechazada() {
		club.reservarInstalacion(Arrays.asList(ana, beto), tenis1, manana, diez, LocalTime.of(11, 0), Modalidad.SENCILLOS);
		CanchaTenis tenis2 = new CanchaTenis("Tenis 2");
		club.planearSesionAcademia(otroEntrenador, manana, diez, 60, tenis2, Arrays.asList(ana));

		verificarSesionRechazada("ocupada", () -> club.planearSesionAcademia(eva, manana, LocalTime.of(10, 30), 60, tenis1, Arrays.asList(beto)));
		verificarSesionRechazada("ocupada", () -> club.planearSesionAcademia(eva, manana, LocalTime.of(10, 30), 60, tenis2, Arrays.asList(beto)));
	}

	@Test
	public void testT01_instalacionIncompatibleRechazada() {
		verificarSesionRechazada("compatible", () -> club.planearSesionEquipo(eva, manana, diez, 60, new CanchaPadel("Padel 1", false), equipo));
		verificarSesionRechazada("compatible", () -> club.planearSesionEquipo(eva, manana, diez, 60, new InstalacionDeporteConjunto("Coliseo", "BALONCESTO"), equipo));
	}

	@Test
	public void testT01_convocadoAjenoRechazado() {
		SesionDeEntrenamiento sesionEquipo = club.planearSesionEquipo(eva, manana, diez, 90, canchaFutbol, equipo);

		verificarSesionRechazada("no pertenece", () -> club.planearSesionAcademia(eva, manana, LocalTime.of(15, 0), 60, tenis1, Arrays.asList(ana, carla)));
		verificarSesionRechazada("no pertenece", () -> club.convocar(sesionEquipo, ana));
		assertFalse(sesionEquipo.getConvocados().contains(ana));
	}

	@Test
	public void testT01_datosInvalidosRechazados() {
		sesionAcademia();
		CanchaTenis tenis2 = new CanchaTenis("Tenis 2");

		verificarSesionRechazada("duracion", () -> club.planearSesionAcademia(eva, manana, LocalTime.of(15, 0), 0, tenis2, Arrays.asList(ana)));
		verificarSesionRechazada("pasada", () -> club.planearSesionAcademia(eva, LocalDate.now().minusDays(1), diez, 60, tenis2, Arrays.asList(ana)));
		verificarSesionRechazada("traslapada", () -> club.planearSesionAcademia(eva, manana, LocalTime.of(10, 30), 60, tenis2, Arrays.asList(ana)));
	}

	@Test
	public void testT02_asistenciaRegistrada() {
		SesionDeEntrenamiento sesion = sesionFinalizada();

		eva.registrarAsistencia(sesion, ana, EstadoAsistencia.ASISTIO);
		eva.registrarAsistencia(sesion, beto, EstadoAsistencia.LLEGO_TARDE);

		assertEquals(EstadoAsistencia.ASISTIO, sesion.buscarRegistro(ana).getEstadoAsistencia());
		assertEquals(EstadoAsistencia.LLEGO_TARDE, sesion.buscarRegistro(beto).getEstadoAsistencia());
	}

	@Test
	public void testT02_jugadorNoConvocadoRechazado() {
		SesionDeEntrenamiento sesion = sesionFinalizada();

		assertThrows(IllegalArgumentException.class, () -> eva.registrarAsistencia(sesion, carla, EstadoAsistencia.ASISTIO));
		assertTrue(sesion.getAsistencias().isEmpty());
	}

	@Test
	public void testT02_sesionNoFinalizadaEstadoInvalidoUOtroEntrenadorRechazado() {
		SesionDeEntrenamiento finalizada = sesionFinalizada();
		SesionDeEntrenamiento futura = sesionAcademia();

		assertThrows(IllegalStateException.class, () -> eva.registrarAsistencia(futura, ana, EstadoAsistencia.ASISTIO));
		assertThrows(IllegalArgumentException.class, () -> eva.registrarAsistencia(finalizada, ana, null));
		assertThrows(IllegalArgumentException.class, () -> otroEntrenador.registrarAsistencia(finalizada, ana, EstadoAsistencia.ASISTIO));
		assertTrue(futura.getAsistencias().isEmpty());
		assertTrue(finalizada.getAsistencias().isEmpty());
	}

	@Test
	public void testT03_evaluacionGuardada() {
		SesionDeEntrenamiento sesion = sesionFinalizada();
		eva.registrarAsistencia(sesion, ana, EstadoAsistencia.LLEGO_TARDE);

		eva.registrarEvaluacion(sesion, ana, 4, "Buen trabajo");

		RegistroAsistencia registro = sesion.buscarRegistro(ana);
		assertEquals(4, registro.getNotaDesempeno());
		assertEquals("Buen trabajo", registro.getComentario());
	}

	@Test
	public void testT03_asistenciaValidaSinEvaluacion() {
		SesionDeEntrenamiento sesion = sesionFinalizada();

		eva.registrarAsistencia(sesion, ana, EstadoAsistencia.ASISTIO);

		assertEquals(EstadoAsistencia.ASISTIO, sesion.buscarRegistro(ana).getEstadoAsistencia());
		assertEquals(null, sesion.buscarRegistro(ana).getNotaDesempeno());
	}

	@Test
	public void testT03_notaFueraDeRangoONoEnteraRechazada() {
		SesionDeEntrenamiento sesion = sesionFinalizada();
		eva.registrarAsistencia(sesion, ana, EstadoAsistencia.ASISTIO);

		assertThrows(IllegalArgumentException.class, () -> eva.registrarEvaluacion(sesion, ana, 0, "x"));
		assertThrows(IllegalArgumentException.class, () -> eva.registrarEvaluacion(sesion, ana, 6, "x"));
		assertThrows(IllegalArgumentException.class, () -> eva.registrarEvaluacion(sesion, ana, 3.5, "x"));
		assertEquals(null, sesion.buscarRegistro(ana).getNotaDesempeno());
	}

	@Test
	public void testT03_jugadorQueFaltoOSinAsistenciaRechazado() {
		SesionDeEntrenamiento sesion = sesionFinalizada();
		eva.registrarAsistencia(sesion, ana, EstadoAsistencia.FALTO);

		assertThrows(IllegalStateException.class, () -> eva.registrarEvaluacion(sesion, ana, 3, "x"));
		assertThrows(IllegalStateException.class, () -> eva.registrarEvaluacion(sesion, beto, 3, "x"));
		assertEquals(null, sesion.buscarRegistro(ana).getNotaDesempeno());
	}

	@Test
	public void testS08_consultarConvocatorias() {
		sesionFinalizada();
		club.planearSesionAcademia(eva, manana, LocalTime.of(15, 0), 45, tenis1, Arrays.asList(ana));

		String convocatorias = club.consultarConvocatorias(ana);

		assertTrue(convocatorias.contains(LocalDate.now().minusDays(1) + " 10:00 (60 min) - Tenis 1 - Entrenador: Eva"));
		assertTrue(convocatorias.contains(manana + " 15:00 (45 min) - Tenis 1 - Entrenador: Eva"));
	}

	@Test
	public void testS08_sinConvocatorias() {
		assertTrue(club.consultarConvocatorias(carla).contains("No tiene convocatorias"));
	}

	@Test
	public void testS09_consultarAsistenciaYEvaluacion() {
		SesionDeEntrenamiento sesion = sesionFinalizada();
		eva.registrarAsistencia(sesion, ana, EstadoAsistencia.ASISTIO);
		eva.registrarEvaluacion(sesion, ana, 5, "Excelente");

		assertTrue(club.consultarAsistencia(ana).contains("ASISTIO - Nota: 5 - Excelente"));
	}

	@Test
	public void testS09_sesionSinAsistenciaPendiente() {
		sesionAcademia();

		String asistencia = club.consultarAsistencia(ana);

		assertTrue(asistencia.contains("Pendiente"));
		assertFalse(asistencia.contains("ASISTIO") || asistencia.contains("FALTO") || asistencia.contains("LLEGO_TARDE"));
	}

	@Test
	public void testS09_sinRegistros() {
		assertTrue(club.consultarAsistencia(carla).contains("No hay registros"));
	}
}
