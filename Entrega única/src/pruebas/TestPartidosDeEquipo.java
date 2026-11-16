package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import club.ClubDeportivoSeneca;
import deportesDeConjunto.Equipo;
import deportesDeConjunto.PartidoDeCompetencia;
import deportesDeConjunto.PartidoInterno;
import deportesDeConjunto.ResultadoPartidoEquipo;
import deportesDeConjunto.ResultadoPorPuntos;
import deportesDeConjunto.ResultadoVoleibol;
import deportesDeConjunto.SetVoleibol;
import deportesDeConjunto.TorneoDeLosCerros;
import enumeraciones.TipoTarjeta;
import estadisticas.EstadisticasPartidoBaloncesto;
import estadisticas.EstadisticasPartidoFutbol;
import inscripciones.InscripcionEquipo;
import inscripciones.InscripcionEquipoFutbol;
import usuarios.Administrador;
import usuarios.Entrenador;
import usuarios.Socio;

public class TestPartidosDeEquipo {

	private ClubDeportivoSeneca club;
	private Administrador admin;
	private Entrenador eva;
	private Equipo halcones;
	private Equipo leones;
	private List<InscripcionEquipo> jugadoresHalcones;
	private List<InscripcionEquipo> jugadoresLeones;
	private int contador;

	@BeforeEach
	public void setUp() {
		club = new ClubDeportivoSeneca();
		admin = club.getAdministrador();
		eva = new Entrenador("Eva", "eva", "123");
		halcones = admin.crearEquipo("FUTBOL", "Halcones", "MAYORES", 10);
		leones = admin.crearEquipo("FUTBOL", "Leones", "MAYORES", 10);
		jugadoresHalcones = llenar(halcones, 8);
		jugadoresLeones = llenar(leones, 7);
	}

	private List<InscripcionEquipo> llenar(Equipo equipo, int cantidad) {
		List<InscripcionEquipo> lista = new ArrayList<InscripcionEquipo>();
		for (int i = 1; i <= cantidad; i++) {
			contador = contador + 1;
			Socio socio = new Socio("Socio" + contador, "s" + contador, "123", LocalDate.of(2000, 1, 1));
			club.registrarSocio(socio);
			lista.add(admin.inscribirJugador(equipo, socio, "Jugador", contador));
		}
		return lista;
	}

	private PartidoInterno programar(Equipo local, Equipo visitante) {
		return club.programarPartidoInterno(eva, local, visitante, LocalDate.now().plusDays(1));
	}

	private PartidoInterno jugar(Equipo local, Equipo visitante, ResultadoPartidoEquipo resultado) {
		PartidoInterno partido = programar(local, visitante);
		partido.setFecha(LocalDate.now());
		club.registrarResultadoInterno(eva, partido, resultado);
		return partido;
	}

	private EstadisticasPartidoFutbol tarjetas(TipoTarjeta... tipos) {
		EstadisticasPartidoFutbol estadisticas = new EstadisticasPartidoFutbol();
		for (int i = 0; i < tipos.length; i++) {
			estadisticas.registrarTarjeta(tipos[i], 10 + i);
		}
		return estadisticas;
	}

	private PartidoInterno partidoConTarjetas(InscripcionEquipo jugador, TipoTarjeta... tipos) {
		PartidoInterno partido = jugar(halcones, leones, new ResultadoPorPuntos(1, 0));
		partido.registrarAlineacion(halcones, Arrays.asList(jugador));
		partido.registrarEstadistica(jugador, tarjetas(tipos));
		return partido;
	}

	private ResultadoVoleibol voleibol(String... ganadores) {
		ResultadoVoleibol resultado = new ResultadoVoleibol();
		for (int i = 0; i < ganadores.length; i++) {
			resultado.agregarSet("LOCAL".equals(ganadores[i]) ? new SetVoleibol(i + 1, 25, 20) : new SetVoleibol(i + 1, 20, 25));
		}
		return resultado;
	}

	private void verificarRechazo(String motivo, Executable accion) {
		int partidos = club.getPartidosInternos().size();
		RuntimeException e = assertThrows(RuntimeException.class, accion);
		assertTrue(e.getMessage().toLowerCase().contains(motivo), e.getMessage());
		assertEquals(partidos, club.getPartidosInternos().size());
	}

	@Test
	public void testT04_partidoProgramado() {
		PartidoInterno partido = programar(halcones, leones);

		assertTrue(club.getPartidosInternos().contains(partido));
		assertEquals(eva, partido.getEntrenador());
		assertFalse(partido.isFinalizado());
	}

	@Test
	public void testT04_equiposOFechaInvalidosRechazados() {
		Equipo sub17 = admin.crearEquipo("FUTBOL", "Pumas", "SUB-17", 10);
		Equipo baloncesto = admin.crearEquipo("BALONCESTO", "Toros", "MAYORES", 10);

		verificarRechazo("categoria", () -> programar(halcones, sub17));
		verificarRechazo("deporte", () -> programar(halcones, baloncesto));
		verificarRechazo("mismo deporte", () -> programar(halcones, halcones));
		verificarRechazo("pasada", () -> club.programarPartidoInterno(eva, halcones, leones, LocalDate.now().minusDays(1)));
	}

	@Test
	public void testT04_equipoBajoElMinimoRechazado() {
		Equipo pumas = admin.crearEquipo("FUTBOL", "Pumas", "MAYORES", 10);
		llenar(pumas, 4);

		verificarRechazo("pumas le faltan 3", () -> programar(halcones, pumas));
	}

	@Test
	public void testT05_alineacionGuardada() {
		PartidoInterno partido = programar(halcones, leones);

		partido.registrarAlineacion(halcones, jugadoresHalcones);

		assertEquals(jugadoresHalcones, partido.getParticipantes());
	}

	@Test
	public void testT05_jugadorConUnaAmarillaAceptado() {
		InscripcionEquipo jugador = jugadoresHalcones.get(0);
		partidoConTarjetas(jugador, TipoTarjeta.AMARILLA);
		PartidoInterno siguiente = programar(halcones, leones);

		siguiente.registrarAlineacion(halcones, Arrays.asList(jugador));

		assertTrue(siguiente.getParticipantes().contains(jugador));
	}

	@Test
	public void testT05_jugadorSuspendidoRechazado() {
		InscripcionEquipo jugador = jugadoresHalcones.get(0);
		partidoConTarjetas(jugador, TipoTarjeta.ROJA);
		PartidoInterno siguiente = programar(halcones, leones);

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> siguiente.registrarAlineacion(halcones, jugadoresHalcones));

		assertTrue(e.getMessage().contains("suspendido"));
		assertTrue(siguiente.getParticipantes().isEmpty());
	}

	@Test
	public void testT05_jugadorDeOtroEquipoOFueraDeNominaRechazado() {
		PartidoInterno partido = programar(halcones, leones);
		assertThrows(IllegalArgumentException.class, () -> partido.registrarAlineacion(halcones, Arrays.asList(jugadoresLeones.get(0))));
		assertTrue(partido.getParticipantes().isEmpty());

		TorneoDeLosCerros competencia = new TorneoDeLosCerros("Cerros", 2026, "FUTBOL", "MAYORES", 7);
		admin.inscribirEquipoEnCompetencia(halcones, competencia, jugadoresHalcones);
		InscripcionEquipo fueraDeNomina = llenar(halcones, 1).get(0);
		PartidoDeCompetencia oficial = competencia.getFormatoLiga().programarPartido(halcones, "Rival", LocalDate.now(), "Estadio");
		assertThrows(IllegalStateException.class, () -> oficial.registrarParticipacion(fueraDeNomina));
		assertTrue(oficial.getParticipantes().isEmpty());
	}

	@Test
	public void testT05_suspensionCumplidaPermiteAlinear() {
		InscripcionEquipo jugador = jugadoresHalcones.get(0);
		partidoConTarjetas(jugador, TipoTarjeta.ROJA);
		jugar(halcones, leones, new ResultadoPorPuntos(0, 0));
		PartidoInterno posterior = programar(halcones, leones);

		posterior.registrarAlineacion(halcones, Arrays.asList(jugador));

		assertTrue(posterior.getParticipantes().contains(jugador));
	}

	@Test
	public void testT06_resultadoRegistradoYPartidoFinalizado() {
		PartidoInterno partido = jugar(halcones, leones, new ResultadoPorPuntos(3, 1));

		assertTrue(partido.isFinalizado());
		assertEquals(halcones, partido.getGanador());
	}

	@Test
	public void testT06_marcadorInvalidoRechazado() {
		Equipo toros = admin.crearEquipo("BALONCESTO", "Toros", "MAYORES", 10);
		Equipo osos = admin.crearEquipo("BALONCESTO", "Osos", "MAYORES", 10);
		Equipo aguilas = admin.crearEquipo("VOLEIBOL", "Aguilas", "MAYORES", 10);
		Equipo garzas = admin.crearEquipo("VOLEIBOL", "Garzas", "MAYORES", 10);
		llenar(toros, 5);
		llenar(osos, 5);
		llenar(aguilas, 6);
		llenar(garzas, 6);
		PartidoInterno futbol = programar(halcones, leones);
		PartidoInterno baloncesto = programar(toros, osos);
		PartidoInterno voley = programar(aguilas, garzas);
		futbol.setFecha(LocalDate.now());
		baloncesto.setFecha(LocalDate.now());
		voley.setFecha(LocalDate.now());

		verificarRechazo("empate", () -> club.registrarResultadoInterno(eva, baloncesto, new ResultadoPorPuntos(80, 80)));
		verificarRechazo("empate", () -> club.registrarResultadoInterno(eva, voley, voleibol("LOCAL", "VISITANTE")));
		verificarRechazo("negativo", () -> club.registrarResultadoInterno(eva, futbol, new ResultadoPorPuntos(-1, 2)));
		verificarRechazo("3-0, 3-1 o 3-2", () -> club.registrarResultadoInterno(eva, voley, voleibol("LOCAL", "LOCAL", "VISITANTE")));
		assertFalse(futbol.isFinalizado() || baloncesto.isFinalizado() || voley.isFinalizado());
	}

	@Test
	public void testT06_partidoInexistenteAjenoOConResultadoRechazado() {
		PartidoInterno ajeno = admin.programarPartidoInterno(halcones, leones, LocalDate.now());
		PartidoInterno deEva = programar(halcones, leones);
		deEva.setFecha(LocalDate.now());
		PartidoInterno jugado = jugar(halcones, leones, new ResultadoPorPuntos(1, 0));
		club.getPartidosInternos().remove(ajeno);

		verificarRechazo("no existe", () -> club.registrarResultadoInterno(eva, ajeno, new ResultadoPorPuntos(1, 0)));
		verificarRechazo("no fue programado", () -> club.registrarResultadoInterno(new Entrenador("Hugo", "hugo", "1"), deEva, new ResultadoPorPuntos(1, 0)));
		verificarRechazo("ya tiene resultado", () -> club.registrarResultadoInterno(eva, jugado, new ResultadoPorPuntos(2, 0)));
		assertFalse(deEva.isFinalizado());
	}

	@Test
	public void testT07_estadisticasAlmacenadas() {
		PartidoInterno partido = jugar(halcones, leones, new ResultadoPorPuntos(1, 0));
		InscripcionEquipo jugador = jugadoresHalcones.get(0);
		partido.registrarAlineacion(halcones, Arrays.asList(jugador));
		EstadisticasPartidoFutbol estadisticas = new EstadisticasPartidoFutbol();
		estadisticas.registrarGol();
		estadisticas.registrarAsistencia();

		partido.registrarEstadistica(jugador, estadisticas);

		assertEquals(Arrays.asList(estadisticas), partido.getEstadisticasDe(jugador));
	}

	@Test
	public void testT07_estadisticaDeOtroDeporteONegativaRechazada() {
		PartidoInterno partido = jugar(halcones, leones, new ResultadoPorPuntos(1, 0));
		InscripcionEquipo jugador = jugadoresHalcones.get(0);
		partido.registrarAlineacion(halcones, Arrays.asList(jugador));
		EstadisticasPartidoFutbol negativa = new EstadisticasPartidoFutbol();
		negativa.setGoles(-1);

		assertThrows(IllegalArgumentException.class, () -> partido.registrarEstadistica(jugador, new EstadisticasPartidoBaloncesto()));
		assertThrows(IllegalArgumentException.class, () -> partido.registrarEstadistica(jugador, negativa));
		assertTrue(partido.getEstadisticas().isEmpty());
	}

	@Test
	public void testT07_jugadorQueNoParticipoRechazado() {
		PartidoInterno partido = jugar(halcones, leones, new ResultadoPorPuntos(1, 0));

		assertThrows(IllegalArgumentException.class, () -> partido.registrarEstadistica(jugadoresHalcones.get(0), new EstadisticasPartidoFutbol()));
		assertTrue(partido.getEstadisticas().isEmpty());
	}

	@Test
	public void testT08_rojaSuspendeParaElSiguientePartido() {
		InscripcionEquipoFutbol jugador = (InscripcionEquipoFutbol) jugadoresHalcones.get(0);

		partidoConTarjetas(jugador, TipoTarjeta.ROJA);

		assertTrue(jugador.estaSuspendidoInterno());
	}

	@Test
	public void testT08_dosAmarillasEnPartidoInternoSuspenden() {
		InscripcionEquipoFutbol jugador = (InscripcionEquipoFutbol) jugadoresHalcones.get(0);

		partidoConTarjetas(jugador, TipoTarjeta.AMARILLA, TipoTarjeta.AMARILLA);

		assertTrue(jugador.estaSuspendidoInterno());
	}

	@Test
	public void testT08_segundaAmarillaEnCompetenciaSuspendeParaElSiguienteOficial() {
		TorneoDeLosCerros competencia = new TorneoDeLosCerros("Cerros", 2026, "FUTBOL", "MAYORES", 7);
		admin.inscribirEquipoEnCompetencia(halcones, competencia, jugadoresHalcones);
		InscripcionEquipo jugador = jugadoresHalcones.get(0);
		for (int i = 0; i < 2; i++) {
			PartidoDeCompetencia oficial = competencia.getFormatoLiga().programarPartido(halcones, "Rival" + i, LocalDate.now(), "Estadio");
			oficial.registrarParticipacion(jugador);
			oficial.registrarEstadistica(jugador, tarjetas(TipoTarjeta.AMARILLA));
		}
		PartidoDeCompetencia siguiente = competencia.getFormatoLiga().programarPartido(halcones, "Rival3", LocalDate.now(), "Estadio");

		assertFalse(competencia.buscarNomina(halcones).buscarJugador(jugador).estaHabilitado());
		assertThrows(IllegalStateException.class, () -> siguiente.registrarParticipacion(jugador));
	}

	@Test
	public void testT08_unaSolaAmarillaSinSancion() {
		InscripcionEquipoFutbol jugador = (InscripcionEquipoFutbol) jugadoresHalcones.get(0);

		partidoConTarjetas(jugador, TipoTarjeta.AMARILLA);

		assertFalse(jugador.estaSuspendidoInterno());
	}

	@Test
	public void testT08_tarjetaEnBaloncestoOJugadorNoAlineadoRechazada() {
		Equipo toros = admin.crearEquipo("BALONCESTO", "Toros", "MAYORES", 10);
		Equipo osos = admin.crearEquipo("BALONCESTO", "Osos", "MAYORES", 10);
		List<InscripcionEquipo> jugadoresToros = llenar(toros, 5);
		llenar(osos, 5);
		PartidoInterno baloncesto = jugar(toros, osos, new ResultadoPorPuntos(80, 70));
		baloncesto.registrarAlineacion(toros, jugadoresToros);
		PartidoInterno futbol = jugar(halcones, leones, new ResultadoPorPuntos(1, 0));

		assertThrows(IllegalArgumentException.class, () -> baloncesto.registrarEstadistica(jugadoresToros.get(0), tarjetas(TipoTarjeta.ROJA)));
		assertThrows(IllegalArgumentException.class, () -> futbol.registrarEstadistica(jugadoresHalcones.get(0), tarjetas(TipoTarjeta.ROJA)));
		assertFalse(((InscripcionEquipoFutbol) jugadoresHalcones.get(0)).estaSuspendidoInterno());
	}

	@Test
	public void testS10_estadisticasDelEquipoYPropias() {
		InscripcionEquipo jugador = jugadoresHalcones.get(0);
		PartidoInterno interno = jugar(halcones, leones, new ResultadoPorPuntos(2, 1));
		interno.registrarAlineacion(halcones, Arrays.asList(jugador));
		EstadisticasPartidoFutbol estadisticas = new EstadisticasPartidoFutbol();
		estadisticas.registrarGol();
		estadisticas.registrarAsistencia();
		interno.registrarEstadistica(jugador, estadisticas);
		TorneoDeLosCerros competencia = new TorneoDeLosCerros("Cerros", 2026, "FUTBOL", "MAYORES", 7);
		admin.inscribirEquipoEnCompetencia(halcones, competencia, jugadoresHalcones);
		competencia.getFormatoLiga().programarPartido(halcones, "Rival", LocalDate.now(), "Estadio").registrarResultado(new ResultadoPorPuntos(0, 0));

		String consulta = club.consultarEstadisticasEquipo(jugador.getSocio(), halcones);

		assertTrue(consulta.contains("jugados 2, ganados 1, empatados 1, perdidos 0, a favor 2, en contra 1"));
		assertTrue(consulta.contains("Mis estadisticas: goles 1, asistencias 1"));
	}

	@Test
	public void testS10_equipoSinPartidos() {
		String consulta = club.consultarEstadisticasEquipo(jugadoresHalcones.get(0).getSocio(), halcones);

		assertTrue(consulta.contains("jugados 0, ganados 0, empatados 0, perdidos 0, a favor 0, en contra 0"));
		assertTrue(consulta.contains("no hay partidos"));
	}

	@Test
	public void testS10_equipoAjenoRechazado() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> club.consultarEstadisticasEquipo(jugadoresLeones.get(0).getSocio(), halcones));

		assertTrue(e.getMessage().contains("no pertenece"));
	}
}
