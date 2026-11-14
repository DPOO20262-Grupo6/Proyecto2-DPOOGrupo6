package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Torneos.TorneoEquipo;
import deportesDeConjunto.Equipo;
import deportesDeConjunto.EquipoBaloncesto;
import deportesDeConjunto.EquipoFutbol;
import deportesDeConjunto.PartidoTorneo;
import deportesDeConjunto.ResultadoPorPuntos;
import enumeraciones.EstadoTorneo;
import enumeraciones.TipoTorneo;
import inscripciones.InscripcionEquipo;
import usuarios.Socio;

/**
 * Pruebas de los requerimientos funcionales de TorneoEquipo
 * (HU-A01 crear torneo, HU-A02 estado, HU-A03 historial, HU-A04 cupos preferenciales).
 */
public class TorneoEquipoTest {

	private LocalDate fechaInicio;
	private TorneoEquipo torneo;
	private EquipoBaloncesto halcones;
	private EquipoBaloncesto tigres;

	@BeforeEach
	public void setUp() throws Exception {
		fechaInicio = LocalDate.now().plusDays(10);
		torneo = new TorneoEquipo("E1", "Liga Baloncesto", "baloncesto", "MAYORES", 7,
				TipoTorneo.RECREATIVO, fechaInicio, 0, 30000);
		halcones = crearEquipo("Halcones");
		tigres = crearEquipo("Tigres");
	}

	/** Equipo de baloncesto MAYORES con 5 jugadores habilitados (minimo para registrar resultados). */
	private EquipoBaloncesto crearEquipo(String nombre) {
		EquipoBaloncesto equipo = new EquipoBaloncesto(nombre, "MAYORES", 12);
		for (int i = 1; i <= 5; i++) {
			Socio socio = new Socio(nombre + i, nombre.toLowerCase() + i, "123", LocalDate.of(1995, 1, 1));
			equipo.agregarJugador(new InscripcionEquipo(socio, equipo, "Base", i));
		}
		return equipo;
	}

	// ======================= HU-A01: Crear torneo =======================

	@Test
	public void testCrearTorneoConDatosValidos() {
		assertEquals("E1", torneo.getId());
		assertEquals("Liga Baloncesto", torneo.getNombre());
		assertEquals("BALONCESTO", torneo.getDeporte());
		assertEquals("MAYORES", torneo.getCategoria());
		assertEquals(7, torneo.getCuposTotales());
		assertEquals(TipoTorneo.RECREATIVO, torneo.getTipo());
		assertEquals(fechaInicio, torneo.getFechaInicio());
		assertEquals(EstadoTorneo.EN_CURSO, torneo.getEstado());
		assertEquals(0, torneo.getNumeroInscritos());
	}

	@Test
	public void testTorneoRecreativoGuardaBonoYNoTarifa() throws Exception {
		TorneoEquipo recreativo = new TorneoEquipo("E2", "Recre", "FUTBOL", "MAYORES", 8,
				TipoTorneo.RECREATIVO, fechaInicio, 20000, 30000);
		assertEquals(0, recreativo.getTarifaInscripcion());
		assertEquals(30000, recreativo.getValorBono());
	}

	@Test
	public void testTorneoCompetitivoGuardaTarifaYNoBono() throws Exception {
		TorneoEquipo competitivo = new TorneoEquipo("E3", "Copa", "VOLEIBOL", "MAYORES", 8,
				TipoTorneo.COMPETITIVO, fechaInicio, 20000, 30000);
		assertEquals(20000, competitivo.getTarifaInscripcion());
		assertEquals(0, competitivo.getValorBono());
	}

	@Test
	public void testCrearTorneoSinIdFalla() {
		assertThrows(Exception.class, () -> new TorneoEquipo(null, "Liga", "FUTBOL", "MAYORES", 7,
				TipoTorneo.RECREATIVO, fechaInicio, 0, 30000));
	}

	@Test
	public void testCrearTorneoSinNombreFalla() {
		assertThrows(Exception.class, () -> new TorneoEquipo("E9", " ", "FUTBOL", "MAYORES", 7,
				TipoTorneo.RECREATIVO, fechaInicio, 0, 30000));
	}

	@Test
	public void testCrearTorneoSinDeporteFalla() {
		assertThrows(Exception.class, () -> new TorneoEquipo("E9", "Liga", "", "MAYORES", 7,
				TipoTorneo.RECREATIVO, fechaInicio, 0, 30000));
	}

	@Test
	public void testCrearTorneoConCuposCeroONegativosFalla() {
		assertThrows(Exception.class, () -> new TorneoEquipo("E9", "Liga", "FUTBOL", "MAYORES", 0,
				TipoTorneo.RECREATIVO, fechaInicio, 0, 30000));
		assertThrows(Exception.class, () -> new TorneoEquipo("E9", "Liga", "FUTBOL", "MAYORES", -5,
				TipoTorneo.RECREATIVO, fechaInicio, 0, 30000));
	}

	@Test
	public void testCrearTorneoSinTipoFalla() {
		assertThrows(Exception.class, () -> new TorneoEquipo("E9", "Liga", "FUTBOL", "MAYORES", 7,
				null, fechaInicio, 0, 30000));
	}

	@Test
	public void testCrearTorneoSinFechaFalla() {
		assertThrows(Exception.class, () -> new TorneoEquipo("E9", "Liga", "FUTBOL", "MAYORES", 7,
				TipoTorneo.RECREATIVO, null, 0, 30000));
	}

	@Test
	public void testCrearTorneoConTarifaOBonoNegativoFalla() {
		assertThrows(Exception.class, () -> new TorneoEquipo("E9", "Liga", "FUTBOL", "MAYORES", 7,
				TipoTorneo.COMPETITIVO, fechaInicio, -100, 0));
		assertThrows(Exception.class, () -> new TorneoEquipo("E9", "Liga", "FUTBOL", "MAYORES", 7,
				TipoTorneo.RECREATIVO, fechaInicio, 0, -100));
	}

	// ======================= HU-A02: Consultar estado =======================

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
	public void testTorneoFinalizadoNoAceptaInscripcionesNiPartidosNiResultados() {
		torneo.inscribir(halcones);
		torneo.inscribir(tigres);
		PartidoTorneo partido = torneo.programarPartido(halcones, tigres, fechaInicio);
		torneo.setFechaInicio(LocalDate.now());
		torneo.finalizar();

		assertFalse(torneo.inscribir(crearEquipo("Leones")));
		assertThrows(IllegalStateException.class, () -> torneo.programarPartido(halcones, tigres, LocalDate.now()));
		assertThrows(IllegalStateException.class, () -> torneo.registrarResultado(partido, new ResultadoPorPuntos(80, 70)));
	}

	// ======================= HU-A03: Consultar historial =======================

	@Test
	public void testHistorialTorneoSinParticipantes() {
		String historial = torneo.consultarHistorial();
		assertTrue(historial.contains("Liga Baloncesto"));
		assertTrue(historial.contains("EN_CURSO"));
		assertTrue(historial.contains("(sin participantes)"));
		assertTrue(historial.contains("(sin encuentros)"));
	}

	@Test
	public void testHistorialMuestraParticipantesEncuentrosYPosiciones() {
		torneo.inscribir(halcones);
		torneo.inscribir(tigres);
		PartidoTorneo partido = torneo.programarPartido(halcones, tigres, fechaInicio.plusDays(1));
		torneo.registrarResultado(partido, new ResultadoPorPuntos(80, 75));

		String historial = torneo.consultarHistorial();
		assertTrue(historial.contains("- Halcones"));
		assertTrue(historial.contains("- Tigres"));
		assertTrue(historial.contains("Halcones vs Tigres"));
		assertTrue(historial.contains("Resultado: 80 - 75"));
		assertTrue(historial.contains("1. Halcones - 2 pts"));
		assertTrue(historial.contains("2. Tigres - 1 pts"));
	}

	@Test
	public void testHistorialMuestraEncuentroPendiente() {
		torneo.inscribir(halcones);
		torneo.inscribir(tigres);
		torneo.programarPartido(halcones, tigres, fechaInicio);
		assertTrue(torneo.consultarHistorial().contains("Halcones vs Tigres (" + fechaInicio + ") Pendiente"));
	}

	@Test
	public void testTablaDePosicionesYGanador() {
		torneo.inscribir(halcones);
		torneo.inscribir(tigres);
		PartidoTorneo partido = torneo.programarPartido(halcones, tigres, fechaInicio);
		torneo.registrarResultado(partido, new ResultadoPorPuntos(60, 90));

		List<Equipo> ordenados = torneo.getEquiposOrdenados();
		assertEquals(tigres, ordenados.get(0));
		assertEquals(halcones, ordenados.get(1));
		assertEquals(2, torneo.getTablaPosiciones().size());
		assertEquals("1. Tigres - 2 pts", torneo.getTablaPosiciones().get(0));
		assertEquals(tigres, torneo.getGanador());
		assertEquals("Tigres", torneo.getNombreGanador());
	}

	@Test
	public void testSinResultadosNoHayGanador() {
		torneo.inscribir(halcones);
		torneo.inscribir(tigres);
		torneo.programarPartido(halcones, tigres, fechaInicio);
		assertEquals(null, torneo.getGanador());
		assertEquals(null, torneo.getNombreGanador());
	}

	@Test
	public void testHistorialDeTorneoFinalizadoIncluyePremio() {
		torneo.inscribir(halcones);
		torneo.inscribir(tigres);
		PartidoTorneo partido = torneo.programarPartido(halcones, tigres, fechaInicio);
		torneo.registrarResultado(partido, new ResultadoPorPuntos(80, 75));
		torneo.setFechaInicio(LocalDate.now());
		torneo.finalizar();

		String historial = torneo.consultarHistorial();
		assertTrue(historial.contains("FINALIZADO"));
		assertEquals("Bono de descuento por $30000.0 para Halcones", torneo.getPremioEntregado());
		assertTrue(historial.contains("Premio: Bono de descuento por $30000.0 para Halcones"));
	}

	@Test
	public void testProgramarPartidoConEquipoNoInscritoFalla() {
		torneo.inscribir(halcones);
		assertThrows(IllegalArgumentException.class, () -> torneo.programarPartido(halcones, tigres, fechaInicio));
	}

	@Test
	public void testProgramarPartidoAntesDelInicioFalla() {
		torneo.inscribir(halcones);
		torneo.inscribir(tigres);
		assertThrows(IllegalArgumentException.class,
				() -> torneo.programarPartido(halcones, tigres, fechaInicio.minusDays(1)));
	}

	@Test
	public void testRegistrarResultadoDePartidoAjenoFalla() throws Exception {
		torneo.inscribir(halcones);
		torneo.inscribir(tigres);
		TorneoEquipo otro = new TorneoEquipo("E8", "Otra", "BALONCESTO", "MAYORES", 4,
				TipoTorneo.RECREATIVO, fechaInicio, 0, 10000);
		otro.inscribir(halcones);
		otro.inscribir(tigres);
		PartidoTorneo ajeno = otro.programarPartido(halcones, tigres, fechaInicio);
		assertThrows(IllegalArgumentException.class, () -> torneo.registrarResultado(ajeno, new ResultadoPorPuntos(80, 75)));
	}

	@Test
	public void testRegistrarResultadoNuloFalla() {
		torneo.inscribir(halcones);
		torneo.inscribir(tigres);
		PartidoTorneo partido = torneo.programarPartido(halcones, tigres, fechaInicio);
		assertThrows(IllegalArgumentException.class, () -> torneo.registrarResultado(partido, null));
	}

	// ======================= HU-A04: Cupos preferenciales =======================

	@Test
	public void testTorneoDeSieteCuposTieneDosPreferencialesYCincoRegulares() {
		assertEquals(2, torneo.getCuposPreferenciales());
		assertEquals(2, torneo.cuposPreferencialesDisponibles());
		assertEquals(5, torneo.cuposRegularesDisponibles());
		assertEquals(7, torneo.cuposDisponibles());
	}

	@Test
	public void testCuposPreferencialesSeRedondeanHaciaArriba() throws Exception {
		TorneoEquipo tres = new TorneoEquipo("E4", "Tres", "FUTBOL", "MAYORES", 3,
				TipoTorneo.RECREATIVO, fechaInicio, 0, 10000);
		TorneoEquipo veinte = new TorneoEquipo("E5", "Veinte", "FUTBOL", "MAYORES", 20,
				TipoTorneo.RECREATIVO, fechaInicio, 0, 10000);
		assertEquals(1, tres.getCuposPreferenciales());
		assertEquals(2, tres.cuposRegularesDisponibles());
		assertEquals(4, veinte.getCuposPreferenciales());
		assertEquals(16, veinte.cuposRegularesDisponibles());
	}

	@Test
	public void testAlLlenarCuposRegularesSoloQuedanPreferenciales() {
		for (int i = 0; i < 5; i++) {
			assertTrue(torneo.inscribir(crearEquipo("Equipo" + i)));
		}
		assertEquals(5, torneo.getNumeroInscritos());
		assertEquals(0, torneo.cuposRegularesDisponibles());
		assertEquals(2, torneo.cuposPreferencialesDisponibles());
		assertEquals(2, torneo.cuposDisponibles());
	}

	@Test
	public void testInscripcionAceptadaOcupaUnCupo() {
		assertTrue(torneo.inscribir(halcones));
		assertTrue(torneo.estaInscrito(halcones));
		assertEquals(1, torneo.getNumeroInscritos());
		assertEquals(6, torneo.cuposDisponibles());
	}

	@Test
	public void testInscripcionRechazadaSiNoHayCupos() throws Exception {
		TorneoEquipo pequeno = new TorneoEquipo("E6", "Mini", "BALONCESTO", "MAYORES", 2,
				TipoTorneo.RECREATIVO, fechaInicio, 0, 10000);
		assertTrue(pequeno.inscribir(halcones));
		assertTrue(pequeno.inscribir(tigres));
		assertFalse(pequeno.inscribir(crearEquipo("Leones")));
		assertEquals(2, pequeno.getNumeroInscritos());
	}

	@Test
	public void testInscripcionRechazadaSiYaEstaInscrito() {
		assertTrue(torneo.inscribir(halcones));
		assertFalse(torneo.inscribir(halcones));
		assertEquals(1, torneo.getNumeroInscritos());
	}

	@Test
	public void testInscripcionRechazadaSiEsDeOtroDeporte() {
		assertFalse(torneo.inscribir(new EquipoFutbol("Rayos", "MAYORES", 20)));
		assertFalse(torneo.inscribir(null));
		assertEquals(0, torneo.getNumeroInscritos());
	}

	@Test
	public void testInscripcionRechazadaSiElTorneoYaInicio() {
		torneo.setFechaInicio(LocalDate.now());
		assertFalse(torneo.inscribir(halcones));
	}

	@Test
	public void testRetirarEquipoLiberaCupo() {
		torneo.inscribir(halcones);
		assertTrue(torneo.retirar(halcones));
		assertFalse(torneo.estaInscrito(halcones));
		assertEquals(7, torneo.cuposDisponibles());
	}
}
