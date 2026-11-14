package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Torneos.TorneoIndividual;
import deportesIndividuales.PartidoDeporteIndividual;
import enumeraciones.EstadoTorneo;
import enumeraciones.Modalidad;
import enumeraciones.NivelHabilidad;
import enumeraciones.TipoTorneo;
import estadisticas.EstadisticasPartidoIndividual;
import estadisticas.EstadisticasPartidoTenis;
import inscripciones.InscripcionDisciplinaIndividual;
import instalaciones.CanchaTenis;
import usuarios.Socio;

public class TorneoIndividualTest {

	private LocalDate fechaInicio;
	private TorneoIndividual torneo;
	private CanchaTenis cancha;
	private InscripcionDisciplinaIndividual ana;
	private InscripcionDisciplinaIndividual beto;

	@BeforeEach
	public void setUp() throws Exception {
		fechaInicio = LocalDate.now().plusDays(10);
		torneo = new TorneoIndividual("T1", "Copa Tenis", "tenis", Modalidad.SENCILLOS, "Abierta", 7,
				TipoTorneo.COMPETITIVO, fechaInicio, 50000, 0);
		cancha = new CanchaTenis("Cancha 1");
		ana = crearJugador("Ana", "TENIS");
		beto = crearJugador("Beto", "TENIS");
	}

	private InscripcionDisciplinaIndividual crearJugador(String nombre, String disciplina) {
		Socio socio = new Socio(nombre, nombre.toLowerCase(), "123", LocalDate.of(2000, 1, 1));
		return new InscripcionDisciplinaIndividual(socio, disciplina, NivelHabilidad.INTERMEDIO);
	}

	private PartidoDeporteIndividual jugarPartido(InscripcionDisciplinaIndividual ganador, InscripcionDisciplinaIndividual perdedor) {
		List<InscripcionDisciplinaIndividual> jugadores = new ArrayList<InscripcionDisciplinaIndividual>();
		jugadores.add(ganador);
		jugadores.add(perdedor);
		PartidoDeporteIndividual partido = torneo.programarPartido(jugadores, fechaInicio.plusDays(1),
				LocalTime.of(8, 0), LocalTime.of(10, 0), cancha);
		List<EstadisticasPartidoIndividual> estadisticas = new ArrayList<EstadisticasPartidoIndividual>();
		estadisticas.add(new EstadisticasPartidoTenis(2, 12, 3, 5, true));
		estadisticas.add(new EstadisticasPartidoTenis(0, 6, 1, 10, false));
		torneo.registrarResultado(partido, estadisticas);
		return partido;
	}

	@Test
	public void testCrearTorneoConDatosValidos() {
		assertEquals("T1", torneo.getId());
		assertEquals("Copa Tenis", torneo.getNombre());
		assertEquals("TENIS", torneo.getDeporte());
		assertEquals(Modalidad.SENCILLOS, torneo.getModalidad());
		assertEquals("Abierta", torneo.getCategoria());
		assertEquals(7, torneo.getCuposTotales());
		assertEquals(TipoTorneo.COMPETITIVO, torneo.getTipo());
		assertEquals(fechaInicio, torneo.getFechaInicio());
		assertEquals(EstadoTorneo.EN_CURSO, torneo.getEstado());
		assertEquals(0, torneo.getNumeroInscritos());
	}

	@Test
	public void testTorneoCompetitivoGuardaTarifaYNoBono() throws Exception {
		TorneoIndividual competitivo = new TorneoIndividual("T2", "Copa", "PADEL", Modalidad.DOBLES, "Abierta", 8,
				TipoTorneo.COMPETITIVO, fechaInicio, 40000, 15000);
		assertEquals(40000, competitivo.getTarifaInscripcion());
		assertEquals(0, competitivo.getValorBono());
	}

	@Test
	public void testTorneoRecreativoGuardaBonoYNoTarifa() throws Exception {
		TorneoIndividual recreativo = new TorneoIndividual("T3", "Recre", "TENIS_DE_MESA", Modalidad.SENCILLOS, "Abierta", 8,
				TipoTorneo.RECREATIVO, fechaInicio, 40000, 15000);
		assertEquals(0, recreativo.getTarifaInscripcion());
		assertEquals(15000, recreativo.getValorBono());
	}

	@Test
	public void testCrearTorneoSinIdFalla() {
		assertThrows(Exception.class, () -> new TorneoIndividual("  ", "Copa", "TENIS", Modalidad.SENCILLOS,
				"Abierta", 7, TipoTorneo.COMPETITIVO, fechaInicio, 50000, 0));
	}

	@Test
	public void testCrearTorneoSinNombreFalla() {
		assertThrows(Exception.class, () -> new TorneoIndividual("T9", "", "TENIS", Modalidad.SENCILLOS,
				"Abierta", 7, TipoTorneo.COMPETITIVO, fechaInicio, 50000, 0));
		assertThrows(Exception.class, () -> new TorneoIndividual("T9", null, "TENIS", Modalidad.SENCILLOS,
				"Abierta", 7, TipoTorneo.COMPETITIVO, fechaInicio, 50000, 0));
	}

	@Test
	public void testCrearTorneoSinDeporteFalla() {
		assertThrows(Exception.class, () -> new TorneoIndividual("T9", "Copa", null, Modalidad.SENCILLOS,
				"Abierta", 7, TipoTorneo.COMPETITIVO, fechaInicio, 50000, 0));
	}

	@Test
	public void testCrearTorneoSinModalidadFalla() {
		assertThrows(IllegalArgumentException.class, () -> new TorneoIndividual("T9", "Copa", "TENIS", null,
				"Abierta", 7, TipoTorneo.COMPETITIVO, fechaInicio, 50000, 0));
	}

	@Test
	public void testCrearTorneoConCuposCeroFalla() {
		assertThrows(Exception.class, () -> new TorneoIndividual("T9", "Copa", "TENIS", Modalidad.SENCILLOS,
				"Abierta", 0, TipoTorneo.COMPETITIVO, fechaInicio, 50000, 0));
	}

	@Test
	public void testCrearTorneoConCuposNegativosFalla() {
		assertThrows(Exception.class, () -> new TorneoIndividual("T9", "Copa", "TENIS", Modalidad.SENCILLOS,
				"Abierta", -3, TipoTorneo.COMPETITIVO, fechaInicio, 50000, 0));
	}

	@Test
	public void testCrearTorneoSinTipoFalla() {
		assertThrows(Exception.class, () -> new TorneoIndividual("T9", "Copa", "TENIS", Modalidad.SENCILLOS,
				"Abierta", 7, null, fechaInicio, 50000, 0));
	}

	@Test
	public void testCrearTorneoSinFechaFalla() {
		assertThrows(Exception.class, () -> new TorneoIndividual("T9", "Copa", "TENIS", Modalidad.SENCILLOS,
				"Abierta", 7, TipoTorneo.COMPETITIVO, null, 50000, 0));
	}

	@Test
	public void testCrearTorneoConTarifaOBonoNegativoFalla() {
		assertThrows(Exception.class, () -> new TorneoIndividual("T9", "Copa", "TENIS", Modalidad.SENCILLOS,
				"Abierta", 7, TipoTorneo.COMPETITIVO, fechaInicio, -1, 0));
		assertThrows(Exception.class, () -> new TorneoIndividual("T9", "Copa", "TENIS", Modalidad.SENCILLOS,
				"Abierta", 7, TipoTorneo.RECREATIVO, fechaInicio, 0, -1));
	}

	@Test
	public void testEstadoInicialEsEnCurso() {
		assertEquals(EstadoTorneo.EN_CURSO, torneo.getEstado());
		assertFalse(torneo.haIniciado());
	}

	@Test
	public void testFinalizarTorneoIniciadoCambiaEstadoAFinalizado() {
		torneo.setFechaInicio(LocalDate.now());
		assertTrue(torneo.haIniciado());
		torneo.finalizar();
		assertEquals(EstadoTorneo.FINALIZADO, torneo.getEstado());
	}

	@Test
	public void testFinalizarTorneoNoIniciadoFalla() {
		assertThrows(IllegalStateException.class, () -> torneo.finalizar());
		assertEquals(EstadoTorneo.EN_CURSO, torneo.getEstado());
	}

	@Test
	public void testFinalizarTorneoYaFinalizadoFalla() {
		torneo.setFechaInicio(LocalDate.now());
		torneo.finalizar();
		assertThrows(IllegalStateException.class, () -> torneo.finalizar());
	}

	@Test
	public void testTorneoFinalizadoNoAceptaInscripcionesNiPartidos() {
		assertTrue(torneo.inscribir(ana));
		assertTrue(torneo.inscribir(beto));
		torneo.setFechaInicio(LocalDate.now());
		torneo.finalizar();

		assertFalse(torneo.inscribir(crearJugador("Carla", "TENIS")));
		List<InscripcionDisciplinaIndividual> jugadores = new ArrayList<InscripcionDisciplinaIndividual>();
		jugadores.add(ana);
		jugadores.add(beto);
		assertThrows(IllegalStateException.class, () -> torneo.programarPartido(jugadores,
				LocalDate.now().plusDays(1), LocalTime.of(8, 0), LocalTime.of(10, 0), cancha));
	}

	@Test
	public void testHistorialTorneoSinParticipantes() {
		String historial = torneo.consultarHistorial();
		assertTrue(historial.contains("Copa Tenis"));
		assertTrue(historial.contains("EN_CURSO"));
		assertTrue(historial.contains("(sin participantes)"));
		assertTrue(historial.contains("(sin encuentros)"));
	}

	@Test
	public void testHistorialMuestraParticipantesEncuentrosYPosiciones() {
		torneo.inscribir(ana);
		torneo.inscribir(beto);
		jugarPartido(ana, beto);

		String historial = torneo.consultarHistorial();
		assertTrue(historial.contains("- Ana"));
		assertTrue(historial.contains("- Beto"));
		assertTrue(historial.contains("Ana vs Beto"));
		assertTrue(historial.contains("Gano: Ana"));
		assertTrue(historial.contains("1. Ana - 1 victorias"));
		assertTrue(historial.contains("2. Beto - 0 victorias"));
	}

	@Test
	public void testHistorialMuestraEncuentroPendiente() {
		torneo.inscribir(ana);
		torneo.inscribir(beto);
		List<InscripcionDisciplinaIndividual> jugadores = new ArrayList<InscripcionDisciplinaIndividual>();
		jugadores.add(ana);
		jugadores.add(beto);
		torneo.programarPartido(jugadores, fechaInicio, LocalTime.of(8, 0), LocalTime.of(10, 0), cancha);

		assertTrue(torneo.consultarHistorial().contains("Pendiente"));
	}

	@Test
	public void testTablaDePosicionesYGanador() {
		torneo.inscribir(ana);
		torneo.inscribir(beto);
		jugarPartido(beto, ana);

		List<String> tabla = torneo.getTablaPosiciones();
		assertEquals(2, tabla.size());
		assertTrue(tabla.get(0).startsWith("1. Beto"));
		assertTrue(tabla.get(1).startsWith("2. Ana"));
		assertEquals(1, torneo.contarVictorias(beto));
		assertEquals(0, torneo.contarVictorias(ana));
		assertEquals(beto, torneo.getGanador());
		assertEquals("Beto", torneo.getNombreGanador());
	}

	@Test
	public void testSinPartidosJugadosNoHayGanador() {
		torneo.inscribir(ana);
		torneo.inscribir(beto);
		assertEquals(null, torneo.getGanador());
		assertEquals(null, torneo.getNombreGanador());
	}

	@Test
	public void testHistorialDeTorneoFinalizadoIncluyePremio() {
		torneo.inscribir(ana);
		torneo.inscribir(beto);
		jugarPartido(ana, beto);
		torneo.setFechaInicio(LocalDate.now());
		torneo.finalizar();

		String historial = torneo.consultarHistorial();
		assertTrue(historial.contains("FINALIZADO"));
		assertEquals("Premio en efectivo de $100000.0 para Ana", torneo.getPremioEntregado());
		assertTrue(historial.contains("Premio en efectivo de $100000.0 para Ana"));
	}

	@Test
	public void testPartidoConJugadorNoInscritoFalla() {
		torneo.inscribir(ana);
		List<InscripcionDisciplinaIndividual> jugadores = new ArrayList<InscripcionDisciplinaIndividual>();
		jugadores.add(ana);
		jugadores.add(beto);
		assertThrows(IllegalArgumentException.class, () -> torneo.programarPartido(jugadores,
				fechaInicio, LocalTime.of(8, 0), LocalTime.of(10, 0), cancha));
	}

	@Test
	public void testRegistrarResultadoDosVecesFalla() {
		torneo.inscribir(ana);
		torneo.inscribir(beto);
		PartidoDeporteIndividual partido = jugarPartido(ana, beto);
		List<EstadisticasPartidoIndividual> estadisticas = new ArrayList<EstadisticasPartidoIndividual>();
		estadisticas.add(new EstadisticasPartidoTenis(2, 12, 3, 5, true));
		estadisticas.add(new EstadisticasPartidoTenis(0, 6, 1, 10, false));
		assertThrows(IllegalStateException.class, () -> torneo.registrarResultado(partido, estadisticas));
	}

	@Test
	public void testTorneoDeSieteCuposTieneDosPreferencialesYCincoRegulares() {
		assertEquals(2, torneo.getCuposPreferenciales());
		assertEquals(2, torneo.cuposPreferencialesDisponibles());
		assertEquals(5, torneo.cuposRegularesDisponibles());
		assertEquals(7, torneo.cuposDisponibles());
	}

	@Test
	public void testCuposPreferencialesSeRedondeanHaciaArriba() throws Exception {
		TorneoIndividual unCupo = new TorneoIndividual("T4", "Uno", "TENIS", Modalidad.SENCILLOS, "Abierta", 1,
				TipoTorneo.RECREATIVO, fechaInicio, 0, 10000);
		TorneoIndividual diezCupos = new TorneoIndividual("T5", "Diez", "TENIS", Modalidad.SENCILLOS, "Abierta", 10,
				TipoTorneo.RECREATIVO, fechaInicio, 0, 10000);
		TorneoIndividual onceCupos = new TorneoIndividual("T6", "Once", "TENIS", Modalidad.SENCILLOS, "Abierta", 11,
				TipoTorneo.RECREATIVO, fechaInicio, 0, 10000);
		assertEquals(1, unCupo.getCuposPreferenciales());
		assertEquals(2, diezCupos.getCuposPreferenciales());
		assertEquals(3, onceCupos.getCuposPreferenciales());
		assertEquals(8, onceCupos.cuposRegularesDisponibles());
	}

	@Test
	public void testAlLlenarCuposRegularesSoloQuedanPreferenciales() {
		for (int i = 0; i < 5; i++) {
			assertTrue(torneo.inscribir(crearJugador("Jugador" + i, "TENIS")));
		}
		assertEquals(5, torneo.getNumeroInscritos());
		assertEquals(0, torneo.cuposRegularesDisponibles());
		assertEquals(2, torneo.cuposPreferencialesDisponibles());
		assertEquals(2, torneo.cuposDisponibles());
	}

	@Test
	public void testInscripcionAceptadaOcupaUnCupo() {
		assertTrue(torneo.inscribir(ana));
		assertTrue(torneo.estaInscrito(ana));
		assertEquals(1, torneo.getNumeroInscritos());
		assertEquals(6, torneo.cuposDisponibles());
	}

	@Test
	public void testInscripcionRechazadaSiNoHayCupos() throws Exception {
		TorneoIndividual pequeno = new TorneoIndividual("T7", "Mini", "TENIS", Modalidad.SENCILLOS, "Abierta", 2,
				TipoTorneo.RECREATIVO, fechaInicio, 0, 10000);
		assertTrue(pequeno.inscribir(ana));
		assertTrue(pequeno.inscribir(beto));
		assertFalse(pequeno.inscribir(crearJugador("Carla", "TENIS")));
		assertEquals(2, pequeno.getNumeroInscritos());
	}

	@Test
	public void testInscripcionRechazadaSiYaEstaInscrito() {
		assertTrue(torneo.inscribir(ana));
		assertFalse(torneo.inscribir(ana));
		assertEquals(1, torneo.getNumeroInscritos());
	}

	@Test
	public void testInscripcionRechazadaSiEsDeOtraDisciplina() {
		assertFalse(torneo.inscribir(crearJugador("Pedro", "PADEL")));
		assertFalse(torneo.inscribir(null));
		assertEquals(0, torneo.getNumeroInscritos());
	}

	@Test
	public void testInscripcionRechazadaSiElTorneoYaInicio() {
		torneo.setFechaInicio(LocalDate.now());
		assertFalse(torneo.inscribir(ana));
	}

	@Test
	public void testRetirarParticipanteLiberaCupo() {
		torneo.inscribir(ana);
		assertTrue(torneo.retirar(ana));
		assertFalse(torneo.estaInscrito(ana));
		assertEquals(7, torneo.cuposDisponibles());
	}
}
