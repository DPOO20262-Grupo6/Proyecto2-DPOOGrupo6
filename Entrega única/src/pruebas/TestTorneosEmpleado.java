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

import Torneos.ReglasEmpleadoTorneo;
import Torneos.Torneo;
import Torneos.TorneoIndividual;
import club.ClubDeportivoSeneca;
import deportesIndividuales.PartidoDeporteIndividual;
import enumeraciones.EstadoTorneo;
import enumeraciones.NivelHabilidad;
import enumeraciones.TipoTorneo;
import estadisticas.EstadisticasPartidoIndividual;
import estadisticas.EstadisticasPartidoTenis;
import inscripciones.InscripcionDisciplinaIndividual;
import inscripciones.InscripcionEmpleadoTorneo;
import instalaciones.CanchaTenis;
import usuarios.Administrador;
import usuarios.Entrenador;
import usuarios.Fisioterapeuta;
import usuarios.Socio;

/**
 * Pruebas de integracion de las historias de empleados en torneos (HU-E07 a HU-E12).
 * Hay un metodo por criterio de aceptacion; en los rechazos se verifica el motivo
 * y que el torneo quede sin cambios.
 */
public class TestTorneosEmpleado {

	private static final double TARIFA = 50000;
	private static final double BONO = 20000;

	private ClubDeportivoSeneca club;
	private Administrador admin;
	private ReglasEmpleadoTorneo reglas;
	private Entrenador entrenador;
	private Fisioterapeuta fisioterapeuta;
	private InscripcionDisciplinaIndividual ana;
	private InscripcionDisciplinaIndividual beto;
	private TorneoIndividual competitivo;
	private TorneoIndividual recreativo;
	private Torneo deConjunto;
	private LocalDate diaTorneo;
	private CanchaTenis cancha;

	@BeforeEach
	public void setUp() throws Exception {
		club = new ClubDeportivoSeneca();
		admin = club.getAdministrador();
		reglas = new ReglasEmpleadoTorneo(club);
		diaTorneo = LocalDate.now().plusDays(10);
		cancha = new CanchaTenis("Cancha 1");

		entrenador = new Entrenador("Carlos", "ent1", "123");
		club.registrarEmpleado(entrenador);
		fisioterapeuta = new Fisioterapeuta("Laura", "fisio1", "123");
		club.registrarEmpleado(fisioterapeuta);

		ana = crearSocioInscrito("Ana", "TENIS");
		beto = crearSocioInscrito("Beto", "TENIS");

		// 5 cupos: 1 preferencial (20 % redondeado hacia arriba) y 4 regulares.
		competitivo = (TorneoIndividual) admin.crearTorneo("Copa Tenis", "TENIS", "SENCILLOS", "Intermedio", 5,
				TipoTorneo.COMPETITIVO, diaTorneo, TARIFA, 0);
		recreativo = (TorneoIndividual) admin.crearTorneo("Festival Padel", "PADEL", "DOBLES", "Abierta", 8,
				TipoTorneo.RECREATIVO, diaTorneo.plusDays(5), 0, BONO);
		deConjunto = admin.crearTorneo("Mini Futbol", "FUTBOL", "CONJUNTO", "Sub-15", 4,
				TipoTorneo.RECREATIVO, diaTorneo, 0, BONO);
	}

	private InscripcionDisciplinaIndividual crearSocioInscrito(String nombre, String disciplina) {
		Socio socio = new Socio(nombre, nombre.toLowerCase(), "123", LocalDate.of(2000, 1, 1));
		club.registrarSocio(socio);
		return club.inscribirEnAcademia(socio, disciplina, NivelHabilidad.INTERMEDIO);
	}

	private void jugar(TorneoIndividual torneo, InscripcionDisciplinaIndividual ganador, InscripcionDisciplinaIndividual perdedor) {
		List<InscripcionDisciplinaIndividual> jugadores = new ArrayList<InscripcionDisciplinaIndividual>();
		jugadores.add(ganador);
		jugadores.add(perdedor);
		PartidoDeporteIndividual partido = torneo.programarPartido(jugadores, torneo.getFechaInicio().plusDays(1),
				LocalTime.of(8, 0), LocalTime.of(10, 0), cancha);
		List<EstadisticasPartidoIndividual> estadisticas = new ArrayList<EstadisticasPartidoIndividual>();
		estadisticas.add(new EstadisticasPartidoTenis(2, 12, 3, 5, true));
		estadisticas.add(new EstadisticasPartidoTenis(0, 6, 1, 10, false));
		torneo.registrarResultado(partido, estadisticas);
	}

	private void finalizar(Torneo torneo) {
		torneo.setFechaInicio(LocalDate.now());
		torneo.finalizar();
	}

	private String rechazoPorEstado(Entrenador empleado, String idTorneo) {
		return assertThrows(IllegalStateException.class, () -> reglas.inscribir(empleado, idTorneo)).getMessage();
	}

	// ============ HU-E07 · Consultar los torneos individuales en los que puedo participar ============

	@Test
	public void testE07_veLosTorneosIndividualesQueNoHanIniciado() {
		List<TorneoIndividual> disponibles = reglas.consultarTorneosDisponibles(entrenador);

		assertEquals(2, disponibles.size());
		assertTrue(disponibles.contains(competitivo));
		assertTrue(disponibles.contains(recreativo));
		assertEquals(4, reglas.cuposParaEmpleados(competitivo));
		assertEquals(0, reglas.calcularValorInscripcion(entrenador, competitivo));
		assertFalse(reglas.estaDeTurno(entrenador, competitivo.getFechaInicio()));
	}

	@Test
	public void testE07_losTorneosDeConjuntoNoAparecen() {
		List<TorneoIndividual> disponibles = reglas.consultarTorneosDisponibles(entrenador);

		assertFalse(disponibles.contains(deConjunto));
		assertEquals(3, club.getTorneos().size());
	}

	@Test
	public void testE07_filtroPorDisciplina() {
		List<TorneoIndividual> soloPadel = reglas.consultarTorneosDisponibles(entrenador, "padel");

		assertEquals(1, soloPadel.size());
		assertEquals(recreativo, soloPadel.get(0));
		assertTrue(reglas.consultarTorneosDisponibles(entrenador, "TENIS_DE_MESA").isEmpty());
	}

	@Test
	public void testE07_sinTorneosPorIniciarLaListaQuedaVacia() {
		competitivo.setFechaInicio(LocalDate.now());
		finalizar(recreativo);

		assertTrue(reglas.consultarTorneosDisponibles(entrenador).isEmpty());
	}

	@Test
	public void testE07_disciplinaInexistenteRechazada() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> reglas.consultarTorneosDisponibles(entrenador, "AJEDREZ"));

		assertTrue(e.getMessage().contains("AJEDREZ"));
	}

	// ============ HU-E08 · Inscribirme en un torneo de disciplina individual ============

	@Test
	public void testE08_inscripcionExitosaOcupaUnCupoRegular() {
		InscripcionEmpleadoTorneo inscripcion = reglas.inscribir(entrenador, competitivo.getId());

		assertTrue(reglas.estaInscrito(entrenador, competitivo));
		assertEquals(entrenador, inscripcion.getEmpleado());
		assertEquals("TENIS", inscripcion.getDisciplina());
		assertEquals(1, competitivo.getNumeroInscritos());
		assertEquals(4, competitivo.cuposDisponibles());
		assertEquals(3, reglas.cuposParaEmpleados(competitivo));
	}

	@Test
	public void testE08_elFisioterapeutaTambienPuedeInscribirse() {
		reglas.inscribir(fisioterapeuta, competitivo.getId());

		assertTrue(reglas.estaInscrito(fisioterapeuta, competitivo));
		assertFalse(reglas.estaInscrito(entrenador, competitivo));
	}

	@Test
	public void testE08_tambienPuedeInscribirseEnUnTorneoDeDobles() {
		reglas.inscribir(entrenador, recreativo.getId());

		assertTrue(reglas.estaInscrito(entrenador, recreativo));
		assertEquals(1, recreativo.getNumeroInscritos());
	}

	@Test
	public void testE08_rechazoPorTorneoDeDeporteDeConjunto() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> reglas.inscribir(entrenador, deConjunto.getId()));

		assertTrue(e.getMessage().contains("individuales"));
		assertEquals(0, deConjunto.getNumeroInscritos());
		assertTrue(reglas.consultarTorneosInscritos(entrenador).isEmpty());
	}

	@Test
	public void testE08_rechazoPorTurnoElDiaDelTorneo() {
		admin.asignarTurno(entrenador, diaTorneo, LocalTime.of(6, 0), 2);

		String mensaje = rechazoPorEstado(entrenador, competitivo.getId());

		assertTrue(mensaje.contains("turno"));
		assertTrue(mensaje.contains(diaTorneo.toString()));
		assertEquals(0, competitivo.getNumeroInscritos());
		assertFalse(reglas.estaInscrito(entrenador, competitivo));
	}

	@Test
	public void testE08_unTurnoEnOtroDiaNoImpideLaInscripcion() {
		admin.asignarTurno(entrenador, diaTorneo.minusDays(1), LocalTime.of(8, 0), 4);
		admin.asignarTurno(entrenador, diaTorneo.plusDays(1), LocalTime.of(8, 0), 4);

		reglas.inscribir(entrenador, competitivo.getId());

		assertTrue(reglas.estaInscrito(entrenador, competitivo));
	}

	@Test
	public void testE08_elTurnoDeOtroEmpleadoNoImpideLaInscripcion() {
		admin.asignarTurno(fisioterapeuta, diaTorneo, LocalTime.of(8, 0), 4);

		reglas.inscribir(entrenador, competitivo.getId());

		assertTrue(reglas.estaInscrito(entrenador, competitivo));
		assertThrows(IllegalStateException.class, () -> reglas.inscribir(fisioterapeuta, competitivo.getId()));
		assertEquals(1, competitivo.getNumeroInscritos());
	}

	@Test
	public void testE08_rechazoCuandoSoloQuedanCuposPreferenciales() {
		competitivo.inscribir(ana);
		competitivo.inscribir(beto);
		competitivo.inscribir(crearSocioInscrito("Carla", "TENIS"));
		competitivo.inscribir(crearSocioInscrito("Dario", "TENIS"));

		String mensaje = rechazoPorEstado(entrenador, competitivo.getId());

		assertTrue(mensaje.contains("preferenciales"));
		assertEquals(4, competitivo.getNumeroInscritos());
		assertEquals(1, competitivo.cuposDisponibles());
	}

	@Test
	public void testE08_rechazoCuandoElTorneoEstaLleno() {
		competitivo.inscribir(ana);
		competitivo.inscribir(beto);
		competitivo.inscribir(crearSocioInscrito("Carla", "TENIS"));
		competitivo.inscribir(crearSocioInscrito("Dario", "TENIS"));
		competitivo.inscribir(crearSocioInscrito("Elena", "TENIS"));

		String mensaje = rechazoPorEstado(entrenador, competitivo.getId());

		assertTrue(mensaje.contains("no tiene cupos"));
		assertEquals(5, competitivo.getNumeroInscritos());
	}

	@Test
	public void testE08_rechazoPorInscripcionDuplicada() {
		reglas.inscribir(entrenador, competitivo.getId());

		String mensaje = rechazoPorEstado(entrenador, competitivo.getId());

		assertTrue(mensaje.contains("Ya esta inscrito"));
		assertEquals(1, competitivo.getNumeroInscritos());
	}

	@Test
	public void testE08_rechazoPorTorneoInexistente() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> reglas.inscribir(entrenador, "NO-EXISTE"));

		assertTrue(e.getMessage().contains("NO-EXISTE"));
		assertThrows(IllegalArgumentException.class, () -> reglas.inscribir(entrenador, null));
	}

	@Test
	public void testE08_rechazoPorTorneoYaIniciado() {
		competitivo.setFechaInicio(LocalDate.now());

		String mensaje = rechazoPorEstado(entrenador, competitivo.getId());

		assertTrue(mensaje.contains("ya inicio"));
		assertEquals(0, competitivo.getNumeroInscritos());
	}

	@Test
	public void testE08_rechazoPorTorneoFinalizado() {
		finalizar(competitivo);

		String mensaje = rechazoPorEstado(entrenador, competitivo.getId());

		assertTrue(mensaje.contains("ya finalizo"));
		assertEquals(EstadoTorneo.FINALIZADO, competitivo.getEstado());
		assertEquals(0, competitivo.getNumeroInscritos());
	}

	// ============ HU-E09 · Participar sin costo en un torneo competitivo ============

	@Test
	public void testE09_elEmpleadoNoPagaYElRecaudoNoCambia() {
		competitivo.inscribir(ana);
		double recaudoAntes = competitivo.getTotalRecaudado();

		InscripcionEmpleadoTorneo inscripcion = reglas.inscribir(entrenador, competitivo.getId());

		assertEquals(0, inscripcion.getValorPagado());
		assertEquals(0, reglas.calcularValorInscripcion(entrenador, competitivo));
		assertEquals(TARIFA, recaudoAntes);
		assertEquals(recaudoAntes, competitivo.getTotalRecaudado());
	}

	@Test
	public void testE09_alSocioSiSeLeCobraLaTarifa() {
		reglas.inscribir(entrenador, competitivo.getId());
		double recaudoAntes = competitivo.getTotalRecaudado();

		competitivo.inscribir(ana);

		assertEquals(TARIFA, reglas.calcularValorInscripcion(ana.getSocio(), competitivo));
		assertEquals(recaudoAntes + TARIFA, competitivo.getTotalRecaudado());
	}

	@Test
	public void testE09_elRecaudoSoloSumaLasInscripcionesPagadas() {
		competitivo.inscribir(ana);
		competitivo.inscribir(beto);
		reglas.inscribir(entrenador, competitivo.getId());
		reglas.inscribir(fisioterapeuta, competitivo.getId());

		assertEquals(4, competitivo.getNumeroInscritos());
		assertEquals(2, competitivo.getNumeroInscritosQuePagan());
		assertEquals(2 * TARIFA, competitivo.getTotalRecaudado());
	}

	@Test
	public void testE09_elTorneoRecreativoNoTieneCostoParaNadie() {
		reglas.inscribir(entrenador, recreativo.getId());

		assertEquals(0, reglas.calcularValorInscripcion(entrenador, recreativo));
		assertEquals(0, reglas.calcularValorInscripcion(ana.getSocio(), recreativo));
		assertEquals(0, recreativo.getTotalRecaudado());
	}

	@Test
	public void testE09_quienNoEsEmpleadoNoRecibeLaExencion() {
		assertEquals(TARIFA, reglas.calcularValorInscripcion(ana.getSocio(), competitivo));
		assertEquals(TARIFA, reglas.calcularValorInscripcion(admin, competitivo));
		assertThrows(IllegalArgumentException.class, () -> reglas.calcularValorInscripcion(null, competitivo));
	}

	// ============ HU-E10 · Conocer que no opto al premio en efectivo ============

	@Test
	public void testE10_elEmpleadoNoOptaAlPremioEnEfectivo() {
		InscripcionEmpleadoTorneo inscripcion = reglas.inscribir(entrenador, competitivo.getId());

		assertFalse(reglas.puedeRecibirPremioEnEfectivo(entrenador));
		assertFalse(reglas.puedeRecibirPremioEnEfectivo(fisioterapeuta));
		assertFalse(inscripcion.recibePremioEnEfectivo());
		assertTrue(reglas.puedeRecibirPremioEnEfectivo(ana.getSocio()));
		assertTrue(ana.recibePremioEnEfectivo());
	}

	@Test
	public void testE10_siElEmpleadoGanaElPremioPasaAlMejorParticipanteQueNoEsEmpleado() {
		competitivo.inscribir(ana);
		competitivo.inscribir(beto);
		InscripcionEmpleadoTorneo carlos = reglas.inscribir(entrenador, competitivo.getId());
		jugar(competitivo, carlos, ana);
		jugar(competitivo, carlos, beto);
		jugar(competitivo, ana, beto);

		finalizar(competitivo);

		assertEquals(carlos, competitivo.getGanador());
		assertTrue(competitivo.getTablaPosiciones().get(0).startsWith("1. Carlos (empleado)"));
		assertEquals("Ana", competitivo.getNombreBeneficiarioPremioEfectivo());
		assertEquals("Premio en efectivo de $100000.0 para Ana (el ganador Carlos (empleado) no recibe premio en efectivo)",
				competitivo.getPremioEntregado());
	}

	@Test
	public void testE10_siElEmpleadoNoGanaLaPremiacionNoCambia() {
		competitivo.inscribir(ana);
		competitivo.inscribir(beto);
		InscripcionEmpleadoTorneo carlos = reglas.inscribir(entrenador, competitivo.getId());
		jugar(competitivo, ana, carlos);
		jugar(competitivo, ana, beto);

		finalizar(competitivo);

		assertEquals(ana, competitivo.getGanador());
		assertEquals("Premio en efectivo de $100000.0 para Ana", competitivo.getPremioEntregado());
	}

	@Test
	public void testE10_siElEmpleadoGanaUnRecreativoRecibeElBono() throws Exception {
		TorneoIndividual recreativoTenis = (TorneoIndividual) admin.crearTorneo("Recreativo Tenis", "TENIS", "SENCILLOS",
				"Abierta", 5, TipoTorneo.RECREATIVO, diaTorneo, 0, BONO);
		recreativoTenis.inscribir(ana);
		InscripcionEmpleadoTorneo carlos = reglas.inscribir(entrenador, recreativoTenis.getId());
		jugar(recreativoTenis, carlos, ana);

		finalizar(recreativoTenis);

		assertEquals("Bono de descuento por $20000.0 para Carlos (empleado)", recreativoTenis.getPremioEntregado());
	}

	@Test
	public void testE10_siSoloHayEmpleadosElPremioEnEfectivoNoSeEntrega() {
		InscripcionEmpleadoTorneo carlos = reglas.inscribir(entrenador, competitivo.getId());
		InscripcionEmpleadoTorneo laura = reglas.inscribir(fisioterapeuta, competitivo.getId());
		jugar(competitivo, carlos, laura);

		finalizar(competitivo);

		assertEquals(null, competitivo.getNombreBeneficiarioPremioEfectivo());
		assertEquals(0, competitivo.getTotalRecaudado());
		assertTrue(competitivo.getPremioEntregado().contains("sin entregar"));
		assertTrue(competitivo.getPremioEntregado().contains("Carlos (empleado)"));
	}

	// ============ HU-E11 · Retirarme de un torneo antes de que inicie ============

	@Test
	public void testE11_elRetiroLiberaElCupo() {
		reglas.inscribir(entrenador, competitivo.getId());
		assertEquals(4, competitivo.cuposDisponibles());

		reglas.retirar(entrenador, competitivo.getId());

		assertFalse(reglas.estaInscrito(entrenador, competitivo));
		assertEquals(0, competitivo.getNumeroInscritos());
		assertEquals(5, competitivo.cuposDisponibles());
		assertEquals(4, reglas.cuposParaEmpleados(competitivo));
	}

	@Test
	public void testE11_despuesDeRetirarsePuedeVolverAInscribirse() {
		reglas.inscribir(entrenador, competitivo.getId());
		reglas.retirar(entrenador, competitivo.getId());

		reglas.inscribir(entrenador, competitivo.getId());

		assertTrue(reglas.estaInscrito(entrenador, competitivo));
		assertEquals(1, competitivo.getNumeroInscritos());
	}

	@Test
	public void testE11_rechazoSiElTorneoYaInicio() {
		reglas.inscribir(entrenador, competitivo.getId());
		competitivo.setFechaInicio(LocalDate.now());

		IllegalStateException e = assertThrows(IllegalStateException.class,
				() -> reglas.retirar(entrenador, competitivo.getId()));

		assertTrue(e.getMessage().contains("ya inicio"));
		assertTrue(reglas.estaInscrito(entrenador, competitivo));
		assertEquals(1, competitivo.getNumeroInscritos());
	}

	@Test
	public void testE11_rechazoSiNoEstaInscrito() {
		competitivo.inscribir(ana);

		IllegalStateException e = assertThrows(IllegalStateException.class,
				() -> reglas.retirar(entrenador, competitivo.getId()));

		assertTrue(e.getMessage().contains("No esta inscrito"));
		assertEquals(1, competitivo.getNumeroInscritos());
		assertThrows(IllegalStateException.class, () -> reglas.retirar(entrenador, deConjunto.getId()));
	}

	@Test
	public void testE11_rechazoPorTorneoInexistente() {
		assertThrows(IllegalArgumentException.class, () -> reglas.retirar(entrenador, "NO-EXISTE"));
	}

	// ============ HU-E12 · Consultar mis torneos y su historial ============

	@Test
	public void testE12_consultaSusTorneosConElEstadoDeCadaUno() {
		reglas.inscribir(entrenador, competitivo.getId());
		reglas.inscribir(entrenador, recreativo.getId());

		List<TorneoIndividual> misTorneos = reglas.consultarTorneosInscritos(entrenador);
		assertEquals(2, misTorneos.size());
		assertEquals("Por iniciar", reglas.describirEstado(competitivo));

		competitivo.setFechaInicio(LocalDate.now());
		assertEquals("En curso", reglas.describirEstado(competitivo));

		competitivo.finalizar();
		assertEquals("Finalizado", reglas.describirEstado(competitivo));
		assertEquals(2, reglas.consultarTorneosInscritos(entrenador).size());
	}

	@Test
	public void testE12_elHistorialMuestraParticipantesEncuentrosYPosiciones() {
		competitivo.inscribir(ana);
		InscripcionEmpleadoTorneo carlos = reglas.inscribir(entrenador, competitivo.getId());
		jugar(competitivo, carlos, ana);

		String historial = reglas.consultarHistorial(competitivo.getId());

		assertTrue(historial.contains("Copa Tenis"));
		assertTrue(historial.contains("- Carlos (empleado)"));
		assertTrue(historial.contains("Carlos (empleado) vs Ana"));
		assertTrue(historial.contains("1. Carlos (empleado) - 1 victorias"));
		assertTrue(historial.contains("2. Ana - 0 victorias"));
	}

	@Test
	public void testE12_elHistorialSePuedeConsultarConElTorneoFinalizado() {
		competitivo.inscribir(ana);
		reglas.inscribir(entrenador, competitivo.getId());
		finalizar(competitivo);

		String historial = reglas.consultarHistorial(competitivo.getId());

		assertTrue(historial.contains("FINALIZADO"));
		assertTrue(historial.contains("Premio:"));
	}

	@Test
	public void testE12_sinInscripcionesLaListaQuedaVacia() {
		competitivo.inscribir(ana);

		assertTrue(reglas.consultarTorneosInscritos(entrenador).isEmpty());
	}

	@Test
	public void testE12_historialDeTorneoInexistenteRechazado() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> reglas.consultarHistorial("NO-EXISTE"));

		assertTrue(e.getMessage().contains("NO-EXISTE"));
	}
}
