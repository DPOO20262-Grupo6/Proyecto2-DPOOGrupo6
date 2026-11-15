package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import club.ClubDeportivoSeneca;
import deportesIndividuales.PartidoDeporteIndividual;
import deportesIndividuales.PartidoTenis;
import deportesIndividuales.ReservaInstalacionDeporteIndividual;
import enumeraciones.Modalidad;
import enumeraciones.NivelHabilidad;
import estadisticas.EstadisticasPartidoIndividual;
import estadisticas.EstadisticasPartidoTenis;
import estadisticas.EstadisticasPartidoTenisDeMesa;
import inscripciones.InscripcionDisciplinaIndividual;
import instalaciones.CanchaTenis;
import usuarios.Socio;

public class TestPartidosDePractica {

	private ClubDeportivoSeneca club;
	private CanchaTenis tenis1;
	private Socio ana;
	private Socio beto;
	private Socio carla;
	private Socio dani;
	private InscripcionDisciplinaIndividual anaTenis;
	private InscripcionDisciplinaIndividual betoTenis;
	private InscripcionDisciplinaIndividual carlaTenis;
	private InscripcionDisciplinaIndividual daniTenis;
	private ReservaInstalacionDeporteIndividual reservaAna;

	@BeforeEach
	public void setUp() {
		club = new ClubDeportivoSeneca();
		tenis1 = new CanchaTenis("Tenis 1");
		ana = crearSocio("Ana");
		beto = crearSocio("Beto");
		carla = crearSocio("Carla");
		dani = crearSocio("Dani");
		anaTenis = club.inscribirEnAcademia(ana, "TENIS", NivelHabilidad.INTERMEDIO);
		betoTenis = club.inscribirEnAcademia(beto, "TENIS", NivelHabilidad.INTERMEDIO);
		carlaTenis = club.inscribirEnAcademia(carla, "TENIS", NivelHabilidad.AVANZADO);
		daniTenis = club.inscribirEnAcademia(dani, "TENIS", NivelHabilidad.PRINCIPIANTE);
		reservaAna = reservar(Arrays.asList(ana, beto), tenis1, 1);
	}

	private Socio crearSocio(String nombre) {
		Socio socio = new Socio(nombre, nombre.toLowerCase(), "123", LocalDate.of(2000, 1, 1));
		club.registrarSocio(socio);
		return socio;
	}

	private ReservaInstalacionDeporteIndividual reservar(List<Socio> socios, CanchaTenis cancha, int dias) {
		return club.reservarInstalacion(socios, cancha, LocalDate.now().plusDays(dias),
				LocalTime.of(10, 0), LocalTime.of(11, 0), Modalidad.SENCILLOS);
	}

	private PartidoDeporteIndividual programar(Socio organizador, ReservaInstalacionDeporteIndividual reserva,
			InscripcionDisciplinaIndividual j1, InscripcionDisciplinaIndividual j2, boolean conEntrenador) {
		return club.programarPartidoPractica(organizador, reserva, Arrays.asList(j1, j2), conEntrenador);
	}

	private List<EstadisticasPartidoIndividual> resultado(int setsGanador, int setsPerdedor) {
		return Arrays.asList(new EstadisticasPartidoTenis(setsGanador, 12, 3, 5, true),
				new EstadisticasPartidoTenis(setsPerdedor, 8, 1, 9, false));
	}

	private PartidoDeporteIndividual jugar(ReservaInstalacionDeporteIndividual reserva, Socio organizador,
			InscripcionDisciplinaIndividual ganador, InscripcionDisciplinaIndividual perdedor) {
		PartidoDeporteIndividual partido = programar(organizador, reserva, ganador, perdedor, false);
		club.registrarResultadoPractica(partido, resultado(2, 1));
		return partido;
	}

	private void verificarRechazo(Class<? extends RuntimeException> tipo, String motivo, Executable accion) {
		int partidos = club.getPartidosPractica().size();
		RuntimeException e = assertThrows(tipo, accion);
		assertTrue(e.getMessage().toLowerCase().contains(motivo), e.getMessage());
		assertEquals(partidos, club.getPartidosPractica().size());
	}

	@Test
	public void testS04_partidoProgramado() {
		PartidoDeporteIndividual partido = programar(ana, reservaAna, anaTenis, betoTenis, false);

		assertTrue(club.getPartidosPractica().contains(partido));
		assertEquals(reservaAna, partido.getReserva());
		assertEquals(Arrays.asList(anaTenis, betoTenis), partido.getParticipantes());
	}

	@Test
	public void testS04_nivelesNoAdyacentesConEntrenadorAceptado() {
		ReservaInstalacionDeporteIndividual reserva = reservar(Arrays.asList(carla, dani), tenis1, 2);

		PartidoDeporteIndividual partido = programar(carla, reserva, carlaTenis, daniTenis, true);

		assertTrue(club.getPartidosPractica().contains(partido));
	}

	@Test
	public void testS04_nivelesNoAdyacentesSinEntrenadorRechazado() {
		ReservaInstalacionDeporteIndividual reserva = reservar(Arrays.asList(carla, dani), tenis1, 2);

		verificarRechazo(IllegalArgumentException.class, "niveles", () -> programar(carla, reserva, carlaTenis, daniTenis, false));
	}

	@Test
	public void testS04_contextoInvalidoRechazado() {
		InscripcionDisciplinaIndividual anaPadel = club.inscribirEnAcademia(ana, "PADEL", NivelHabilidad.INTERMEDIO);
		InscripcionDisciplinaIndividual noRegistrada = carla.inscribirseDisciplina("TENIS", NivelHabilidad.AVANZADO);

		verificarRechazo(IllegalArgumentException.class, "reserva activa", () -> programar(carla, reservaAna, anaTenis, betoTenis, false));
		verificarRechazo(IllegalArgumentException.class, "otra disciplina", () -> programar(ana, reservaAna, anaPadel, betoTenis, false));
		verificarRechazo(IllegalArgumentException.class, "no esta inscrito", () -> programar(ana, reservaAna, anaTenis, noRegistrada, false));
		verificarRechazo(IllegalArgumentException.class, "repetido", () -> programar(ana, reservaAna, anaTenis, anaTenis, false));
	}

	@Test
	public void testS04_agendaOcupadaRechazado() {
		programar(ana, reservaAna, anaTenis, betoTenis, false);
		ReservaInstalacionDeporteIndividual otraReserva = reservar(Arrays.asList(carla, ana), new CanchaTenis("Tenis 2"), 1);

		verificarRechazo(IllegalArgumentException.class, "agenda", () -> programar(carla, otraReserva, carlaTenis, anaTenis, true));
	}

	@Test
	public void testS06_rankingConPartidoJugado() {
		jugar(reservaAna, ana, anaTenis, betoTenis);

		assertEquals("TENIS: ELO 1016.0, posicion 1 de 4, partidos jugados: 1", club.consultarRanking(ana, "TENIS"));
		assertEquals("TENIS: ELO 984.0, posicion 4 de 4, partidos jugados: 1", club.consultarRanking(beto, "TENIS"));
	}

	@Test
	public void testS06_rankingSinPartidos() {
		String ranking = club.consultarRanking(carla, "TENIS");

		assertTrue(ranking.contains("ELO 1000.0"));
		assertTrue(ranking.contains("no hay partidos"));
	}

	@Test
	public void testS06_noInscritoRechazado() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> club.consultarRanking(ana, "PADEL"));

		assertTrue(e.getMessage().contains("no esta inscrito"));
	}

	@Test
	public void testS07_historialCronologico() {
		PartidoDeporteIndividual posterior = jugar(reservar(Arrays.asList(ana, carla), tenis1, 3), ana, anaTenis, betoTenis);
		PartidoDeporteIndividual anterior = jugar(reservaAna, ana, anaTenis, betoTenis);

		assertEquals(Arrays.asList(anterior, posterior), club.consultarHistorialPractica(ana, ana, "TENIS", null, null));
		assertEquals(Arrays.asList(posterior), club.consultarHistorialPractica(ana, ana, "TENIS",
				LocalDate.now().plusDays(2), LocalDate.now().plusDays(5)));
		assertEquals(1030.53, club.buscarInscripcion(ana, "TENIS").getRankingElo());
	}

	@Test
	public void testS07_historialVacio() {
		assertTrue(club.consultarHistorialPractica(carla, carla, "TENIS", null, null).isEmpty());
	}

	@Test
	public void testS07_consultaInvalidaRechazada() {
		LocalDate hoy = LocalDate.now();

		assertThrows(IllegalArgumentException.class, () -> club.consultarHistorialPractica(ana, ana, "PADEL", null, null));
		assertThrows(IllegalArgumentException.class, () -> club.consultarHistorialPractica(ana, beto, "TENIS", null, null));
		assertThrows(IllegalArgumentException.class, () -> club.consultarHistorialPractica(ana, ana, "TENIS", hoy, hoy.minusDays(1)));
	}

	@Test
	public void testT10_resultadoRegistradoActualizaElo() {
		PartidoDeporteIndividual partido = programar(ana, reservaAna, anaTenis, betoTenis, false);

		club.registrarResultadoPractica(partido, resultado(2, 0));

		assertTrue(partido.tieneResultado());
		assertEquals(1016.0, anaTenis.getRankingElo());
		assertEquals(984.0, betoTenis.getRankingElo());
	}

	@Test
	public void testT10_partidoInexistenteOConResultadoRechazado() {
		PartidoDeporteIndividual ajeno = new PartidoTenis(LocalDate.now(), LocalTime.of(8, 0), LocalTime.of(9, 0),
				Modalidad.SENCILLOS, tenis1, false);
		PartidoDeporteIndividual jugado = jugar(reservaAna, ana, anaTenis, betoTenis);

		verificarRechazo(IllegalArgumentException.class, "no existe", () -> club.registrarResultadoPractica(ajeno, resultado(2, 0)));
		verificarRechazo(IllegalStateException.class, "ya tiene resultado", () -> club.registrarResultadoPractica(jugado, resultado(2, 0)));
		assertEquals(1016.0, anaTenis.getRankingElo());
	}

	@Test
	public void testT10_setsImposiblesOEstadisticaDeOtraDisciplinaRechazado() {
		PartidoDeporteIndividual partido = programar(ana, reservaAna, anaTenis, betoTenis, false);
		List<EstadisticasPartidoIndividual> deMesa = Arrays.asList(new EstadisticasPartidoTenisDeMesa(2, 40, true),
				new EstadisticasPartidoTenisDeMesa(1, 30, false));

		verificarRechazo(IllegalArgumentException.class, "imposible", () -> club.registrarResultadoPractica(partido, resultado(2, 2)));
		verificarRechazo(IllegalArgumentException.class, "disciplina", () -> club.registrarResultadoPractica(partido, deMesa));
		assertFalse(partido.tieneResultado());
		assertEquals(1000.0, anaTenis.getRankingElo());
	}
}
