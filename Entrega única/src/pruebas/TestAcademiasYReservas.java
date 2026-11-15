package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import club.ClubDeportivoSeneca;
import deportesIndividuales.ReservaInstalacionDeporteIndividual;
import entrenamiento.SesionDeEntrenamiento;
import enumeraciones.Modalidad;
import enumeraciones.NivelHabilidad;
import inscripciones.InscripcionDisciplinaIndividual;
import instalaciones.CanchaDeporteIndividual;
import instalaciones.CanchaPadel;
import instalaciones.CanchaTenis;
import instalaciones.CanchaTenisDeMesa;
import usuarios.Entrenador;
import usuarios.Socio;

public class TestAcademiasYReservas {

	private ClubDeportivoSeneca club;
	private Socio ana;
	private Socio beto;
	private Socio carla;
	private Socio diego;
	private CanchaTenis tenis1;
	private CanchaPadel padel;
	private LocalDate fecha;
	private LocalTime inicio;
	private LocalTime fin;

	@BeforeEach
	public void setUp() {
		club = new ClubDeportivoSeneca();
		ana = crearSocio("Ana", "ana");
		beto = crearSocio("Beto", "beto");
		carla = crearSocio("Carla", "carla");
		diego = crearSocio("Diego", "diego");
		club.inscribirEnAcademia(ana, "TENIS", NivelHabilidad.INTERMEDIO);
		club.inscribirEnAcademia(ana, "PADEL", NivelHabilidad.INTERMEDIO);
		club.inscribirEnAcademia(ana, "TENIS_DE_MESA", NivelHabilidad.INTERMEDIO);
		club.inscribirEnAcademia(carla, "TENIS", NivelHabilidad.AVANZADO);
		tenis1 = new CanchaTenis("Tenis 1");
		padel = new CanchaPadel("Padel 1", false);
		fecha = LocalDate.now().plusDays(1);
		inicio = LocalTime.of(10, 0);
		fin = LocalTime.of(11, 0);
	}

	private Socio crearSocio(String nombre, String login) {
		Socio socio = new Socio(nombre, login, "123", LocalDate.of(2000, 1, 1));
		club.registrarSocio(socio);
		return socio;
	}

	private ReservaInstalacionDeporteIndividual reservar(List<Socio> socios, CanchaDeporteIndividual cancha, Modalidad modalidad) {
		return club.reservarInstalacion(socios, cancha, fecha, inicio, fin, modalidad);
	}

	private void verificarReservaRechazada(ReservaInstalacionDeporteIndividual reserva, String motivo, int reservasAntes) {
		assertEquals("RECHAZADA", reserva.getEstado());
		assertTrue(reserva.getMotivoRechazo().toLowerCase().contains(motivo), reserva.getMotivoRechazo());
		assertEquals(reservasAntes, club.getReservas().size());
		assertFalse(reserva.getCancha().getReservas().contains(reserva));
	}

	private void verificarInscripcionRechazada(Socio socio, String disciplina, NivelHabilidad nivel, String motivo) {
		int antes = club.getInscripcionesIndividuales().size();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> club.inscribirEnAcademia(socio, disciplina, nivel));
		assertTrue(e.getMessage().toLowerCase().contains(motivo), e.getMessage());
		assertEquals(antes, club.getInscripcionesIndividuales().size());
	}

	@Test
	public void testS02_inscripcionExitosaConEloInicial() {
		InscripcionDisciplinaIndividual inscripcion = club.inscribirEnAcademia(beto, "PADEL", NivelHabilidad.PRINCIPIANTE);

		assertEquals(NivelHabilidad.PRINCIPIANTE, inscripcion.getNivel());
		assertEquals(1000.0, inscripcion.getRankingElo());
		assertEquals(inscripcion, club.buscarInscripcion(beto, "PADEL"));
	}

	@Test
	public void testS02_cadaDisciplinaConservaSuElo() {
		club.buscarInscripcion(ana, "TENIS").actualizarRankingElo(1000.0, true);

		assertEquals(1016.0, club.buscarInscripcion(ana, "TENIS").getRankingElo());
		assertEquals(1000.0, club.buscarInscripcion(ana, "PADEL").getRankingElo());
	}

	@Test
	public void testS02_inscripcionDuplicadaRechazada() {
		verificarInscripcionRechazada(ana, "TENIS", NivelHabilidad.AVANZADO, "duplicada");
	}

	@Test
	public void testS02_nivelODisciplinaInvalidaRechazada() {
		verificarInscripcionRechazada(beto, "TENIS", null, "nivel");
		verificarInscripcionRechazada(beto, "SQUASH", NivelHabilidad.PRINCIPIANTE, "no existe");
	}

	@Test
	public void testS03_reservaExitosa() {
		ReservaInstalacionDeporteIndividual reserva = reservar(Arrays.asList(ana, beto), tenis1, Modalidad.SENCILLOS);

		assertEquals("CONFIRMADA", reserva.getEstado());
		assertTrue(club.getReservasDeSocio(ana).contains(reserva));
		assertTrue(tenis1.getReservas().contains(reserva));
	}

	@Test
	public void testS03_cupoCompletoRechazado() {
		club.setMaximoInstalaciones("TENIS", 1);
		reservar(Arrays.asList(ana, beto), tenis1, Modalidad.SENCILLOS);

		ReservaInstalacionDeporteIndividual reserva = reservar(Arrays.asList(carla, diego), new CanchaTenis("Tenis 2"), Modalidad.SENCILLOS);

		verificarReservaRechazada(reserva, "cupo", 1);
	}

	@Test
	public void testS03_capacidadInvalidaRechazada() {
		verificarReservaRechazada(reservar(Arrays.asList(ana, beto), padel, Modalidad.DOBLES), "jugadores", 0);
		verificarReservaRechazada(reservar(Arrays.asList(ana, beto, carla), tenis1, Modalidad.SENCILLOS), "jugadores", 0);
		verificarReservaRechazada(reservar(Arrays.asList(ana, beto), tenis1, Modalidad.DOBLES), "jugadores", 0);
		verificarReservaRechazada(reservar(Arrays.asList(ana, beto, carla), new CanchaTenisDeMesa("Mesa 1", false), Modalidad.DOBLES), "jugadores", 0);
	}

	@Test
	public void testS03_traslapeConReservaOSesionRechazado() {
		reservar(Arrays.asList(ana, beto), tenis1, Modalidad.SENCILLOS);
		verificarReservaRechazada(club.reservarInstalacion(Arrays.asList(carla, diego), tenis1, fecha,
				LocalTime.of(10, 30), LocalTime.of(11, 30), Modalidad.SENCILLOS), "disponible", 1);

		CanchaTenis tenis2 = new CanchaTenis("Tenis 2");
		Entrenador entrenador = new Entrenador("Eva", "eva", "123");
		SesionDeEntrenamiento sesion = entrenador.planearSesion(fecha, LocalTime.of(9, 30), 60, tenis2, new ArrayList<Socio>());
		club.getSesiones().add(sesion);
		verificarReservaRechazada(reservar(Arrays.asList(carla, diego), tenis2, Modalidad.SENCILLOS), "disponible", 1);
	}

	@Test
	public void testS03_socioNoInscritoRechazado() {
		verificarReservaRechazada(reservar(Arrays.asList(beto, ana), tenis1, Modalidad.SENCILLOS), "inscrito", 0);
	}

	@Test
	public void testS05_padelOMesaTechadaSinBebidaCalientePermitido() {
		ReservaInstalacionDeporteIndividual reserva = reservar(Arrays.asList(ana, beto, carla, diego), padel, Modalidad.DOBLES);

		reserva.iniciarUso(ana, false);

		assertTrue(reserva.isEnUso());
	}

	@Test
	public void testS05_bebidaCalienteEnPadelOMesaTechadaRechazada() {
		ReservaInstalacionDeporteIndividual enPadel = reservar(Arrays.asList(ana, beto, carla, diego), padel, Modalidad.DOBLES);
		ReservaInstalacionDeporteIndividual enMesaTechada = reservar(Arrays.asList(ana, beto),
				new CanchaTenisDeMesa("Mesa techada", true), Modalidad.SENCILLOS);

		IllegalStateException e1 = assertThrows(IllegalStateException.class, () -> enPadel.iniciarUso(ana, true));
		IllegalStateException e2 = assertThrows(IllegalStateException.class, () -> enMesaTechada.iniciarUso(ana, true));

		assertTrue(e1.getMessage().toLowerCase().contains("bebida"));
		assertTrue(e2.getMessage().toLowerCase().contains("bebida"));
		assertFalse(enPadel.isEnUso());
		assertFalse(enMesaTechada.isEnUso());
	}

	@Test
	public void testS05_tenisOMesaNoTechadaConBebidaCalientePermitido() {
		ReservaInstalacionDeporteIndividual enTenis = reservar(Arrays.asList(ana, beto), tenis1, Modalidad.SENCILLOS);
		ReservaInstalacionDeporteIndividual enMesaAbierta = reservar(Arrays.asList(ana, beto),
				new CanchaTenisDeMesa("Mesa abierta", false), Modalidad.SENCILLOS);

		enTenis.iniciarUso(ana, true);
		enMesaAbierta.iniciarUso(ana, true);

		assertTrue(enTenis.isEnUso());
		assertTrue(enMesaAbierta.isEnUso());
	}
}
