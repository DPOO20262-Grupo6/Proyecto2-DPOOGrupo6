package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Torneos.ReglasSocioTorneo;
import Torneos.TorneoEquipo;
import Torneos.TorneoIndividual;
import club.ClubDeportivoSeneca;
import deportesDeConjunto.EquipoBaloncesto;
import deportesDeConjunto.PartidoTorneo;
import deportesDeConjunto.ResultadoPorPuntos;
import deportesIndividuales.PartidoDeporteIndividual;
import enumeraciones.NivelHabilidad;
import enumeraciones.TipoCupoTorneo;
import enumeraciones.TipoTorneo;
import estadisticas.EstadisticasPartidoIndividual;
import estadisticas.EstadisticasPartidoPadel;
import estadisticas.EstadisticasPartidoTenis;
import inscripciones.InscripcionDisciplinaIndividual;
import inscripciones.InscripcionEquipo;
import inscripciones.InscripcionParticipanteExterno;
import instalaciones.CanchaPadel;
import instalaciones.CanchaTenis;
import usuarios.Administrador;
import usuarios.ParticipanteExterno;
import usuarios.Socio;

/**
 * Pruebas de integracion de las historias del socio en los torneos:
 * HU-S18 inscribirse, HU-S19 salirse, HU-S20 estudiantes y personas externas, HU-S21 ganar un bono.
 * (HU-S22, usar un bono, esta en ComprasSocioTest.) Hay un metodo por criterio de aceptacion; en los
 * rechazos se verifica el motivo y que el torneo quede sin cambios.
 */
public class TestTorneosSocio {

	private static final double TARIFA = 30000;
	private static final double BONO = 15000;

	private ClubDeportivoSeneca club;
	private Administrador admin;
	private ReglasSocioTorneo reglas;
	private LocalDate fecha;

	private Socio ana;
	private Socio beto;
	private Socio carla;
	private Socio dario;
	private TorneoIndividual tenis;
	private TorneoIndividual dobles;
	private TorneoEquipo baloncesto;
	private EquipoBaloncesto halcones;
	private EquipoBaloncesto tigres;

	@BeforeEach
	public void setUp() throws Exception {
		club = new ClubDeportivoSeneca();
		admin = club.getAdministrador();
		reglas = new ReglasSocioTorneo(club);
		fecha = LocalDate.now().plusDays(10);

		ana = crearSocio("Ana", "ana");
		beto = crearSocio("Beto", "beto");
		carla = crearSocio("Carla", "carla");
		dario = crearSocio("Dario", "dario");
		for (Socio socio : new Socio[] { ana, beto, carla, dario }) {
			club.inscribirEnAcademia(socio, "TENIS", NivelHabilidad.INTERMEDIO);
			club.inscribirEnAcademia(socio, "PADEL", NivelHabilidad.INTERMEDIO);
		}

		// 5 cupos: 1 preferencial (20 % redondeado hacia arriba) y 4 regulares.
		tenis = (TorneoIndividual) admin.crearTorneo("Copa Tenis", "TENIS", "SENCILLOS", "Intermedio", 5,
				TipoTorneo.COMPETITIVO, fecha, TARIFA, 0);
		dobles = (TorneoIndividual) admin.crearTorneo("Festival Padel", "PADEL", "DOBLES", "Abierta", 8,
				TipoTorneo.RECREATIVO, fecha, 0, BONO);
		baloncesto = (TorneoEquipo) admin.crearTorneo("Copa Baloncesto", "BALONCESTO", "CONJUNTO", "MAYORES", 4,
				TipoTorneo.RECREATIVO, fecha, 0, BONO);
		halcones = crearEquipo("Halcones", ana);
		tigres = crearEquipo("Tigres", null);
	}

	private Socio crearSocio(String nombre, String login) {
		Socio socio = new Socio(nombre, "doc-" + login, login, "123", LocalDate.of(1995, 1, 1));
		club.registrarSocio(socio);
		return socio;
	}

	private EquipoBaloncesto crearEquipo(String nombre, Socio primerJugador) {
		EquipoBaloncesto equipo = new EquipoBaloncesto(nombre, "MAYORES", 12);
		int camiseta = 1;
		if (primerJugador != null) {
			equipo.agregarJugador(new InscripcionEquipo(primerJugador, equipo, "Base", camiseta));
			camiseta = camiseta + 1;
		}
		for (int i = camiseta; i <= 5; i++) {
			Socio socio = crearSocio(nombre + i, nombre.toLowerCase() + i);
			equipo.agregarJugador(new InscripcionEquipo(socio, equipo, "Base", i));
		}
		club.agregarEquipo(equipo);
		return equipo;
	}

	private InscripcionDisciplinaIndividual academia(Socio socio, String disciplina) {
		return club.buscarInscripcion(socio, disciplina);
	}

	private String motivoDeRechazo(Class<? extends RuntimeException> tipo, org.junit.jupiter.api.function.Executable accion) {
		return assertThrows(tipo, accion).getMessage();
	}

	private void iniciar(TorneoIndividual torneo) {
		torneo.setFechaInicio(LocalDate.now());
	}

	// ============ HU-S18 · Inscribirme en un torneo ============

	@Test
	public void testS18_socioSeInscribeComoJugadorConSuAcademia() {
		InscripcionDisciplinaIndividual inscripcion = reglas.inscribirIndividual(ana, tenis.getId());

		assertSame(academia(ana, "TENIS"), inscripcion);
		assertSame(inscripcion, tenis.buscarParticipante(ana));
		assertEquals(TipoCupoTorneo.REGULAR, inscripcion.getTipoCupo());
		assertEquals(1, tenis.getNumeroInscritos());
		assertEquals(4, tenis.cuposDisponibles());
	}

	@Test
	public void testS18_rechazoSiNoEstaInscritoEnLaAcademiaDeLaDisciplina() {
		Socio nuevo = crearSocio("Nuevo", "nuevo");

		String motivo = motivoDeRechazo(IllegalStateException.class, () -> reglas.inscribirIndividual(nuevo, tenis.getId()));

		assertTrue(motivo.contains("academia de TENIS"));
		assertEquals(0, tenis.getNumeroInscritos());
	}

	@Test
	public void testS18_rechazoPorInscripcionDuplicada() {
		reglas.inscribirIndividual(ana, tenis.getId());

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> reglas.inscribirIndividual(ana, tenis.getId()));

		assertTrue(e.getMessage().contains("ya esta inscrito"));
		assertEquals(1, tenis.getNumeroInscritos());
	}

	@Test
	public void testS18_rechazoSiElTorneoYaInicio() {
		iniciar(tenis);

		IllegalStateException e = assertThrows(IllegalStateException.class, () -> reglas.inscribirIndividual(ana, tenis.getId()));

		assertTrue(e.getMessage().contains("ya ha iniciado"));
		assertEquals(0, tenis.getNumeroInscritos());
	}

	@Test
	public void testS18_equipoSeInscribeSiElSocioEsJugadorDelEquipo() {
		reglas.inscribirEquipo(ana, baloncesto.getId(), halcones);

		assertTrue(baloncesto.estaInscrito(halcones));
		assertEquals(TipoCupoTorneo.REGULAR, baloncesto.getTipoCupo(halcones));
		assertEquals(3, baloncesto.cuposDisponibles());
	}

	@Test
	public void testS18_equipoRechazadoSiElSocioNoPerteneceAEl() {
		String motivo = motivoDeRechazo(IllegalStateException.class, () -> reglas.inscribirEquipo(carla, baloncesto.getId(), halcones));

		assertTrue(motivo.contains("Solo un jugador del equipo"));
		assertFalse(baloncesto.estaInscrito(halcones));
	}

	@Test
	public void testS18_parejaSeInscribeEnUnTorneoDeDobles() {
		reglas.inscribirPareja(ana, "beto", dobles.getId());

		InscripcionDisciplinaIndividual deAna = academia(ana, "PADEL");
		InscripcionDisciplinaIndividual deBeto = academia(beto, "PADEL");
		assertEquals(2, dobles.getNumeroInscritos());
		assertSame(deBeto, dobles.getPareja(deAna));
		assertSame(deAna, dobles.getPareja(deBeto));
		assertEquals(6, dobles.cuposDisponibles());
	}

	@Test
	public void testS18_parejaRechazadaSiLaParejaNoEstaEnLaAcademiaYNoQuedaNadieInscrito() {
		Socio sinAcademia = crearSocio("Sin", "sin");

		String motivo = motivoDeRechazo(IllegalStateException.class, () -> reglas.inscribirPareja(ana, "sin", dobles.getId()));

		assertTrue(motivo.contains("academia de PADEL"));
		assertEquals(0, dobles.getNumeroInscritos());
		assertNull(dobles.buscarParticipante(ana));
	}

	@Test
	public void testS18_buenRankingTomaUnCupoPreferencial() {
		academia(ana, "TENIS").setRankingElo(1100);

		InscripcionDisciplinaIndividual inscripcion = reglas.inscribirIndividual(ana, tenis.getId());

		assertTrue(reglas.tieneBuenRanking(inscripcion));
		assertEquals(TipoCupoTorneo.PREFERENCIAL, inscripcion.getTipoCupo());
		assertEquals(0, tenis.cuposPreferencialesDisponibles());
		assertEquals(4, tenis.cuposRegularesDisponibles());
	}

	// ============ HU-S19 · Retirarme de un torneo ============

	@Test
	public void testS19_elRetiroLiberaElCupo() {
		reglas.inscribirIndividual(ana, tenis.getId());

		reglas.retirarJugador(ana, tenis.getId());

		assertNull(tenis.buscarParticipante(ana));
		assertEquals(0, tenis.getNumeroInscritos());
		assertEquals(5, tenis.cuposDisponibles());
		assertEquals(4, tenis.cuposRegularesDisponibles());
	}

	@Test
	public void testS19_alRetirarseUnoDeLaParejaSeRetiranLosDos() {
		reglas.inscribirPareja(ana, "beto", dobles.getId());

		reglas.retirarJugador(beto, dobles.getId());

		assertEquals(0, dobles.getNumeroInscritos());
		assertNull(dobles.buscarParticipante(ana));
		assertNull(dobles.getPareja(academia(ana, "PADEL")));
		assertEquals(8, dobles.cuposDisponibles());
	}

	@Test
	public void testS19_elRetiroDeUnEquipoLiberaSuCupo() {
		reglas.inscribirEquipo(ana, baloncesto.getId(), halcones);

		reglas.retirarEquipo(ana, baloncesto.getId(), halcones);

		assertFalse(baloncesto.estaInscrito(halcones));
		assertEquals(4, baloncesto.cuposDisponibles());
	}

	@Test
	public void testS19_rechazoSiNoEstaInscrito() {
		reglas.inscribirIndividual(beto, tenis.getId());

		IllegalStateException e = assertThrows(IllegalStateException.class, () -> reglas.retirarJugador(ana, tenis.getId()));

		assertTrue(e.getMessage().contains("No esta inscrito"));
		assertEquals(1, tenis.getNumeroInscritos());
	}

	@Test
	public void testS19_rechazoSiElTorneoYaInicio() {
		reglas.inscribirIndividual(ana, tenis.getId());
		iniciar(tenis);

		IllegalStateException e = assertThrows(IllegalStateException.class, () -> reglas.retirarJugador(ana, tenis.getId()));

		assertTrue(e.getMessage().contains("ya inicio"));
		assertNotNull(tenis.buscarParticipante(ana));
		assertEquals(1, tenis.getNumeroInscritos());
	}

	// ============ HU-S20 · Inscribir a un estudiante o a una persona externa ============

	@Test
	public void testS20_unEstudianteMatriculadoSeInscribe() {
		InscripcionParticipanteExterno inscripcion = reglas.inscribirParticipante(ana, tenis.getId(), "Luis Perez", "9001",
				ParticipanteExterno.ESTUDIANTE_MATRICULADO);

		assertEquals(1, tenis.getNumeroInscritos());
		assertEquals("Luis Perez (estudiante)", inscripcion.getSocio().getNombre());
		assertEquals(TipoCupoTorneo.REGULAR, inscripcion.getTipoCupo());
		assertTrue(inscripcion.getParticipante().esEstudiante());
		assertEquals("ana", inscripcion.getLoginSocioResponsable());
	}

	@Test
	public void testS20_laPersonaInvitadaPagaLaTarifaDelTorneo() {
		reglas.inscribirParticipante(ana, tenis.getId(), "Luis Perez", "9001", ParticipanteExterno.EXTERNO);
		reglas.inscribirIndividual(beto, tenis.getId());

		assertEquals(2, tenis.getNumeroInscritosQuePagan());
		assertEquals(2 * TARIFA, tenis.getTotalRecaudado());
	}

	@Test
	public void testS20_rechazoSiElDocumentoYaEstaInscritoEnElTorneo() {
		reglas.inscribirParticipante(ana, tenis.getId(), "Luis Perez", "9001", ParticipanteExterno.EXTERNO);

		IllegalStateException e = assertThrows(IllegalStateException.class,
				() -> reglas.inscribirParticipante(beto, tenis.getId(), "Otro Nombre", "9001", ParticipanteExterno.EXTERNO));

		assertTrue(e.getMessage().contains("ya esta inscrito"));
		assertEquals(1, tenis.getNumeroInscritos());
	}

	@Test
	public void testS20_rechazoSiElDocumentoPerteneceAUnSocio() {
		IllegalStateException e = assertThrows(IllegalStateException.class,
				() -> reglas.inscribirParticipante(ana, tenis.getId(), "Beto", "doc-beto", ParticipanteExterno.EXTERNO));

		assertTrue(e.getMessage().contains("ya pertenece a un socio"));
		assertEquals(0, tenis.getNumeroInscritos());
	}

	@Test
	public void testS20_soloElSocioQueLaInscribioPuedeRetirarla() {
		reglas.inscribirParticipante(ana, tenis.getId(), "Luis Perez", "9001", ParticipanteExterno.EXTERNO);

		IllegalStateException e = assertThrows(IllegalStateException.class, () -> reglas.retirarParticipante(beto, tenis.getId(), "9001"));
		assertTrue(e.getMessage().contains("Solo el socio que inscribio"));
		assertEquals(1, tenis.getNumeroInscritos());

		reglas.retirarParticipante(ana, tenis.getId(), "9001");

		assertEquals(0, tenis.getNumeroInscritos());
		assertEquals(5, tenis.cuposDisponibles());
	}

	// ============ HU-S21 · Ganar un bono ============

	private TorneoIndividual crearTenisRecreativo() throws Exception {
		return (TorneoIndividual) admin.crearTorneo("Recreativo Tenis", "TENIS", "SENCILLOS", "Intermedio", 4,
				TipoTorneo.RECREATIVO, fecha, 0, BONO);
	}

	private void jugarTenis(TorneoIndividual torneo, InscripcionDisciplinaIndividual ganador, InscripcionDisciplinaIndividual perdedor) {
		List<InscripcionDisciplinaIndividual> jugadores = new ArrayList<InscripcionDisciplinaIndividual>();
		jugadores.add(ganador);
		jugadores.add(perdedor);
		PartidoDeporteIndividual partido = torneo.programarPartido(jugadores, fecha, LocalTime.of(8, 0), LocalTime.of(10, 0), new CanchaTenis("Cancha 1"));
		List<EstadisticasPartidoIndividual> estadisticas = new ArrayList<EstadisticasPartidoIndividual>();
		estadisticas.add(new EstadisticasPartidoTenis(2, 12, 3, 5, true));
		estadisticas.add(new EstadisticasPartidoTenis(0, 6, 1, 10, false));
		torneo.registrarResultado(partido, estadisticas);
	}

	@Test
	public void testS21_elGanadorDeUnTorneoRecreativoIndividualRecibeUnBono() throws Exception {
		TorneoIndividual recreativo = crearTenisRecreativo();
		reglas.inscribirIndividual(ana, recreativo.getId());
		reglas.inscribirIndividual(beto, recreativo.getId());
		jugarTenis(recreativo, academia(ana, "TENIS"), academia(beto, "TENIS"));

		recreativo.setFechaInicio(LocalDate.now());
		recreativo.finalizar();

		assertEquals(1, ana.getBonos().size());
		assertTrue(ana.getBonos().get(0).esPorValor());
		assertEquals(BONO, ana.getBonos().get(0).getValorFijo());
		assertFalse(ana.getBonos().get(0).isUsado());
		assertEquals(0, beto.getBonos().size());
		assertEquals(1, reglas.consultarBonosDisponibles(ana).size());
	}

	@Test
	public void testS21_cadaJugadorDelEquipoGanadorRecibeUnBono() {
		reglas.inscribirEquipo(ana, baloncesto.getId(), halcones);
		baloncesto.inscribir(tigres);
		PartidoTorneo partido = baloncesto.programarPartido(halcones, tigres, fecha);
		baloncesto.registrarResultado(partido, new ResultadoPorPuntos(90, 70));

		baloncesto.setFechaInicio(LocalDate.now());
		baloncesto.finalizar();

		for (InscripcionEquipo jugador : halcones.getInscripciones()) {
			assertEquals(1, jugador.getSocio().getBonos().size());
		}
		for (InscripcionEquipo jugador : tigres.getInscripciones()) {
			assertEquals(0, jugador.getSocio().getBonos().size());
		}
		assertTrue(baloncesto.getPremioEntregado().contains("Bono de descuento"));
	}

	@Test
	public void testS21_laParejaGanadoraRecibeUnBonoCadaUno() {
		reglas.inscribirPareja(ana, "beto", dobles.getId());
		reglas.inscribirPareja(carla, "dario", dobles.getId());
		List<InscripcionDisciplinaIndividual> cuatro = new ArrayList<InscripcionDisciplinaIndividual>();
		cuatro.add(academia(ana, "PADEL"));
		cuatro.add(academia(beto, "PADEL"));
		cuatro.add(academia(carla, "PADEL"));
		cuatro.add(academia(dario, "PADEL"));
		PartidoDeporteIndividual partido = dobles.programarPartido(cuatro, fecha, LocalTime.of(8, 0), LocalTime.of(10, 0), new CanchaPadel("Padel 1", false));
		List<EstadisticasPartidoIndividual> estadisticas = new ArrayList<EstadisticasPartidoIndividual>();
		estadisticas.add(new EstadisticasPartidoPadel(2, 12, 3, 5, true));
		estadisticas.add(new EstadisticasPartidoPadel(2, 12, 1, 5, true));
		estadisticas.add(new EstadisticasPartidoPadel(0, 6, 1, 10, false));
		estadisticas.add(new EstadisticasPartidoPadel(0, 6, 0, 10, false));
		dobles.registrarResultado(partido, estadisticas);

		dobles.setFechaInicio(LocalDate.now());
		dobles.finalizar();

		assertEquals(1, ana.getBonos().size());
		assertEquals(1, beto.getBonos().size());
		assertEquals(0, carla.getBonos().size());
		assertEquals(0, dario.getBonos().size());
	}

	@Test
	public void testS21_unTorneoCompetitivoNoEntregaBono() {
		reglas.inscribirIndividual(ana, tenis.getId());
		reglas.inscribirIndividual(beto, tenis.getId());
		jugarTenis(tenis, academia(ana, "TENIS"), academia(beto, "TENIS"));

		tenis.setFechaInicio(LocalDate.now());
		tenis.finalizar();

		assertEquals(0, ana.getBonos().size());
		assertTrue(tenis.getPremioEntregado().contains("Premio en efectivo"));
	}

	@Test
	public void testS21_elBonoSeEntregaUnaSolaVez() throws Exception {
		TorneoIndividual recreativo = crearTenisRecreativo();
		reglas.inscribirIndividual(ana, recreativo.getId());
		reglas.inscribirIndividual(beto, recreativo.getId());
		jugarTenis(recreativo, academia(ana, "TENIS"), academia(beto, "TENIS"));
		recreativo.setFechaInicio(LocalDate.now());
		recreativo.finalizar();

		recreativo.entregarPremio();
		recreativo.entregarPremio();

		assertEquals(1, ana.getBonos().size());
	}

}
