package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import club.ClubDeportivoSeneca;
import deportesDeConjunto.Ascun;
import deportesDeConjunto.Competencia;
import deportesDeConjunto.Equipo;
import deportesDeConjunto.NominaCompetencia;
import deportesDeConjunto.PartidoDeCompetencia;
import deportesDeConjunto.ResultadoPorPuntos;
import estadisticas.EstadisticasPartidoBaloncesto;
import estadisticas.EstadisticasPartidoFutbol;
import inscripciones.InscripcionEquipo;
import usuarios.Administrador;
import usuarios.Entrenador;
import usuarios.Socio;

public class TestCompetencias {

	private ClubDeportivoSeneca club;
	private Administrador admin;
	private Entrenador eva;
	private Equipo halcones;
	private Equipo leones;
	private Equipo pumas;
	private List<InscripcionEquipo> jugadoresHalcones;
	private List<InscripcionEquipo> jugadoresLeones;
	private List<InscripcionEquipo> jugadoresPumas;
	private Competencia copa;
	private int contador;

	@BeforeEach
	public void setUp() {
		club = new ClubDeportivoSeneca();
		admin = club.getAdministrador();
		eva = new Entrenador("Eva", "eva", "123");
		halcones = admin.crearEquipo("FUTBOL", "Halcones", "MAYORES", 10);
		leones = admin.crearEquipo("FUTBOL", "Leones", "MAYORES", 10);
		pumas = admin.crearEquipo("FUTBOL", "Pumas", "MAYORES", 10);
		jugadoresHalcones = llenar(halcones, 7);
		jugadoresLeones = llenar(leones, 7);
		jugadoresPumas = llenar(pumas, 7);
		copa = admin.registrarCompetencia("Copa Cerros", "Liga Cerros", 2026, "FUTBOL", "MAYORES", "LIGA");
	}

	private List<InscripcionEquipo> llenar(Equipo equipo, int cantidad) {
		List<InscripcionEquipo> lista = new ArrayList<InscripcionEquipo>();
		for (int i = 0; i < cantidad; i++) {
			contador = contador + 1;
			Socio socio = new Socio("Socio" + contador, "s" + contador, "123", LocalDate.of(2000, 1, 1));
			club.registrarSocio(socio);
			lista.add(admin.inscribirJugador(equipo, socio, "Jugador", contador));
		}
		return lista;
	}

	private PartidoDeCompetencia programar(Competencia competencia, Equipo equipo, String rival) {
		return admin.programarPartidoOficial(competencia, equipo, rival, "Estadio", LocalDate.now(), eva);
	}

	private PartidoDeCompetencia jugar(Competencia competencia, Equipo equipo, String rival, int propios, int ajenos) {
		PartidoDeCompetencia partido = programar(competencia, equipo, rival);
		competencia.registrarResultadoOficial(eva, partido, new ResultadoPorPuntos(propios, ajenos));
		return partido;
	}

	private void registrarFutbol(PartidoDeCompetencia partido, InscripcionEquipo jugador, int goles, int asistencias) {
		partido.registrarParticipacion(jugador);
		EstadisticasPartidoFutbol estadisticas = new EstadisticasPartidoFutbol();
		estadisticas.setGoles(goles);
		estadisticas.setAsistencias(asistencias);
		partido.registrarEstadistica(jugador, estadisticas);
	}

	private Competencia ligaDeBaloncesto(List<InscripcionEquipo> jugadoresToros) {
		Equipo toros = jugadoresToros.get(0).getEquipo();
		Competencia baloncesto = admin.registrarCompetencia("Liga Aro", "Liga Aro", 2026, "BALONCESTO", "MAYORES", "LIGA");
		admin.inscribirEquipoEnCompetencia(toros, baloncesto, jugadoresToros);
		return baloncesto;
	}

	private void verificarRechazo(Class<? extends RuntimeException> tipo, String motivo, Executable accion) {
		RuntimeException e = assertThrows(tipo, accion);
		assertTrue(e.getMessage().toLowerCase().contains(motivo), e.getMessage());
	}

	@Test
	public void testA17_competenciaRegistrada() {
		Competencia grupos = admin.registrarCompetencia("Copa ASCUN", "ASCUN", 2026, "VOLEIBOL", "MAYORES", "GRUPOS");

		assertTrue(club.getCompetencias().contains(copa));
		assertTrue(club.getCompetencias().contains(grupos));
		assertEquals("Liga Cerros", copa.getEntidadOrganizadora());
		assertEquals("LIGA", copa.getFormato());
		assertEquals("GRUPOS", grupos.getFormato());
	}

	@Test
	public void testA17_datoFaltanteFormatoInvalidoODuplicadaRechazada() {
		verificarRechazo(IllegalArgumentException.class, "falta", () -> admin.registrarCompetencia("Copa", "", 2026, "FUTBOL", "MAYORES", "LIGA"));
		verificarRechazo(IllegalArgumentException.class, "formato invalido", () -> admin.registrarCompetencia("Copa", "Liga", 2026, "FUTBOL", "MAYORES", "COPA"));
		verificarRechazo(IllegalArgumentException.class, "ya existe", () -> admin.registrarCompetencia("Copa Cerros", "Otra", 2026, "FUTBOL", "MAYORES", "LIGA"));
		assertEquals(1, club.getCompetencias().size());
	}

	@Test
	public void testA18_equipoInscritoConSuNomina() {
		NominaCompetencia nomina = admin.inscribirEquipoEnCompetencia(halcones, copa, jugadoresHalcones);

		assertEquals(nomina, copa.buscarNomina(halcones));
		assertEquals(7, nomina.getJugadores().size());
	}

	@Test
	public void testA18_otraCategoriaODeporteRechazado() {
		Equipo sub17 = admin.crearEquipo("FUTBOL", "Juveniles", "SUB-17", 10);
		Equipo toros = admin.crearEquipo("BALONCESTO", "Toros", "MAYORES", 10);

		verificarRechazo(IllegalArgumentException.class, "categoria o deporte", () -> admin.inscribirEquipoEnCompetencia(sub17, copa, new ArrayList<InscripcionEquipo>()));
		verificarRechazo(IllegalArgumentException.class, "categoria o deporte", () -> admin.inscribirEquipoEnCompetencia(toros, copa, new ArrayList<InscripcionEquipo>()));
		assertTrue(copa.getNominas().isEmpty());
	}

	@Test
	public void testA18_nominaInsuficienteRechazadaIndicandoFaltantes() {
		verificarRechazo(IllegalStateException.class, "faltan 2", () -> admin.inscribirEquipoEnCompetencia(halcones, copa, jugadoresHalcones.subList(0, 5)));
		assertTrue(copa.getNominas().isEmpty());
	}

	@Test
	public void testA18_nominaConJugadorAjenoRechazada() {
		List<InscripcionEquipo> nomina = new ArrayList<InscripcionEquipo>(jugadoresHalcones);
		nomina.add(jugadoresLeones.get(0));

		verificarRechazo(IllegalArgumentException.class, "no pertenece", () -> admin.inscribirEquipoEnCompetencia(halcones, copa, nomina));
		assertTrue(copa.getNominas().isEmpty());
	}

	@Test
	public void testA18_equipoYaInscritoRechazado() {
		admin.inscribirEquipoEnCompetencia(halcones, copa, jugadoresHalcones);

		verificarRechazo(IllegalArgumentException.class, "ya esta inscrito", () -> admin.inscribirEquipoEnCompetencia(halcones, copa, jugadoresHalcones));
		assertEquals(1, copa.getNominas().size());
	}

	@Test
	public void testA19_partidoOficialProgramado() {
		admin.inscribirEquipoEnCompetencia(halcones, copa, jugadoresHalcones);

		PartidoDeCompetencia partido = programar(copa, halcones, "U Andes");

		assertTrue(copa.getPartidos().contains(partido));
		assertEquals("U Andes", partido.getInstitucionRival());
		assertEquals("Estadio", partido.getEscenario());
		assertEquals(eva, partido.getEntrenador());
	}

	@Test
	public void testA19_equipoNoInscritoODatosFaltantesRechazado() {
		admin.inscribirEquipoEnCompetencia(halcones, copa, jugadoresHalcones);
		LocalDate hoy = LocalDate.now();

		verificarRechazo(IllegalArgumentException.class, "no esta inscrito", () -> programar(copa, pumas, "U Andes"));
		verificarRechazo(IllegalArgumentException.class, "rival", () -> admin.programarPartidoOficial(copa, halcones, " ", "Estadio", hoy, eva));
		verificarRechazo(IllegalArgumentException.class, "escenario", () -> admin.programarPartidoOficial(copa, halcones, "U Andes", null, hoy, eva));
		verificarRechazo(IllegalArgumentException.class, "entrenador", () -> admin.programarPartidoOficial(copa, halcones, "U Andes", "Estadio", hoy, null));
		assertTrue(copa.getPartidos().isEmpty());
	}

	@Test
	public void testT09_futbolVictoriaTresPuntosEmpateUno() {
		admin.inscribirEquipoEnCompetencia(halcones, copa, jugadoresHalcones);
		admin.inscribirEquipoEnCompetencia(leones, copa, jugadoresLeones);
		admin.inscribirEquipoEnCompetencia(pumas, copa, jugadoresPumas);

		jugar(copa, halcones, "U Andes", 2, 0);
		jugar(copa, leones, "U Rosario", 1, 1);
		jugar(copa, pumas, "U Externado", 0, 1);

		assertEquals(3, copa.buscarNomina(halcones).getPuntosAcumulados());
		assertEquals(1, copa.buscarNomina(leones).getPuntosAcumulados());
		assertEquals(0, copa.buscarNomina(pumas).getPuntosAcumulados());
	}

	@Test
	public void testT09_baloncestoVictoriaTresPuntosDerrotaCero() {
		Equipo toros = admin.crearEquipo("BALONCESTO", "Toros", "MAYORES", 10);
		Equipo osos = admin.crearEquipo("BALONCESTO", "Osos", "MAYORES", 10);
		Competencia baloncesto = ligaDeBaloncesto(llenar(toros, 5));
		admin.inscribirEquipoEnCompetencia(osos, baloncesto, llenar(osos, 5));

		jugar(baloncesto, toros, "U Andes", 80, 70);
		jugar(baloncesto, osos, "U Andes", 60, 70);

		assertEquals(3, baloncesto.buscarNomina(toros).getPuntosAcumulados());
		assertEquals(0, baloncesto.buscarNomina(osos).getPuntosAcumulados());
	}

	@Test
	public void testT09_empateEnBaloncestoNegativoOConResultadoRechazado() {
		Equipo toros = admin.crearEquipo("BALONCESTO", "Toros", "MAYORES", 10);
		Competencia baloncesto = ligaDeBaloncesto(llenar(toros, 5));
		PartidoDeCompetencia partidoBaloncesto = programar(baloncesto, toros, "U Andes");
		admin.inscribirEquipoEnCompetencia(halcones, copa, jugadoresHalcones);
		PartidoDeCompetencia partidoFutbol = programar(copa, halcones, "U Andes");
		PartidoDeCompetencia jugado = jugar(copa, halcones, "U Rosario", 1, 0);

		verificarRechazo(IllegalArgumentException.class, "empate", () -> baloncesto.registrarResultadoOficial(eva, partidoBaloncesto, new ResultadoPorPuntos(70, 70)));
		verificarRechazo(IllegalArgumentException.class, "negativo", () -> copa.registrarResultadoOficial(eva, partidoFutbol, new ResultadoPorPuntos(-1, 0)));
		verificarRechazo(IllegalStateException.class, "ya tiene resultado", () -> copa.registrarResultadoOficial(eva, jugado, new ResultadoPorPuntos(2, 0)));
		assertEquals(0, baloncesto.buscarNomina(toros).getJugados());
		assertEquals(1, copa.buscarNomina(halcones).getJugados());
		assertEquals(3, copa.buscarNomina(halcones).getPuntosAcumulados());
	}

	@Test
	public void testT09_entrenadorNoResponsableRechazado() {
		admin.inscribirEquipoEnCompetencia(halcones, copa, jugadoresHalcones);
		PartidoDeCompetencia partido = programar(copa, halcones, "U Andes");

		verificarRechazo(IllegalArgumentException.class, "entrenador responsable",
				() -> copa.registrarResultadoOficial(new Entrenador("Hugo", "hugo", "1"), partido, new ResultadoPorPuntos(1, 0)));
		assertEquals(null, partido.getResultado());
	}

	@Test
	public void testS11_tablaDeLigaOrdenadaConDesempatePorEnfrentamientoDirecto() {
		admin.inscribirEquipoEnCompetencia(halcones, copa, jugadoresHalcones);
		admin.inscribirEquipoEnCompetencia(leones, copa, jugadoresLeones);
		admin.inscribirEquipoEnCompetencia(pumas, copa, jugadoresPumas);
		jugar(copa, halcones, "Leones", 1, 0);
		jugar(copa, leones, "U Andes", 5, 0);

		String tabla = club.consultarTablaPosiciones("Copa Cerros", null);

		assertTrue(tabla.contains("1. Halcones - 3 pts | PJ 1, G 1, E 0, P 0, DIF 1"), tabla);
		assertTrue(tabla.contains("2. Leones - 3 pts | PJ 1, G 1, E 0, P 0, DIF 5"), tabla);
		assertTrue(tabla.contains("3. Pumas - 0 pts | PJ 0, G 0, E 0, P 0, DIF 0"), tabla);
	}

	@Test
	public void testS11_tablaDeGrupoYClasificados() {
		Ascun ascun = (Ascun) admin.registrarCompetencia("Copa ASCUN", "ASCUN", 2026, "FUTBOL", "MAYORES", "GRUPOS");
		admin.inscribirEquipoEnCompetencia(halcones, ascun, jugadoresHalcones);
		admin.inscribirEquipoEnCompetencia(leones, ascun, jugadoresLeones);
		admin.inscribirEquipoEnCompetencia(pumas, ascun, jugadoresPumas);
		ascun.asignarGrupo(halcones, "A");
		ascun.asignarGrupo(leones, "A");
		ascun.asignarGrupo(pumas, "A");
		jugar(ascun, pumas, "U Andes", 0, 2);
		jugar(ascun, leones, "U Andes", 1, 1);
		jugar(ascun, halcones, "U Andes", 3, 0);

		String tabla = club.consultarTablaPosiciones("Copa ASCUN", "A");

		assertTrue(tabla.indexOf("1. Halcones") < tabla.indexOf("2. Leones") && tabla.indexOf("2. Leones") < tabla.indexOf("3. Pumas"), tabla);
		assertTrue(tabla.contains("Clasifican: Halcones, Leones"), tabla);
	}

	@Test
	public void testS11_filtroPorDeporteOCategoria() {
		admin.inscribirEquipoEnCompetencia(halcones, copa, jugadoresHalcones);
		jugar(copa, halcones, "U Andes", 1, 0);
		Equipo toros = admin.crearEquipo("BALONCESTO", "Toros", "MAYORES", 10);
		Competencia baloncesto = ligaDeBaloncesto(llenar(toros, 5));
		jugar(baloncesto, toros, "U Andes", 80, 70);

		String soloBaloncesto = club.consultarTablas("BALONCESTO", null);

		assertTrue(soloBaloncesto.contains("Liga Aro") && soloBaloncesto.contains("Toros"));
		assertFalse(soloBaloncesto.contains("Copa Cerros") || soloBaloncesto.contains("Halcones"));
		assertTrue(club.consultarTablas(null, "SUB-17").contains("No hay informacion"));
	}

	@Test
	public void testS11_competenciaInexistenteOSinResultadosSinInformacion() {
		admin.inscribirEquipoEnCompetencia(halcones, copa, jugadoresHalcones);
		programar(copa, halcones, "U Andes");

		assertTrue(club.consultarTablaPosiciones("No existe", null).contains("No hay informacion"));
		assertTrue(club.consultarTablaPosiciones("Copa Cerros", null).contains("No hay informacion"));
	}

	@Test
	public void testS12_goleadoresSoloDeEsaCompetencia() {
		admin.inscribirEquipoEnCompetencia(halcones, copa, jugadoresHalcones);
		admin.inscribirEquipoEnCompetencia(leones, copa, jugadoresLeones);
		PartidoDeCompetencia partidoHalcones = jugar(copa, halcones, "U Andes", 3, 0);
		registrarFutbol(partidoHalcones, jugadoresHalcones.get(0), 1, 0);
		registrarFutbol(partidoHalcones, jugadoresHalcones.get(1), 2, 1);
		registrarFutbol(jugar(copa, leones, "U Andes", 1, 0), jugadoresLeones.get(0), 1, 3);
		Competencia otra = admin.registrarCompetencia("Copa Norte", "Liga Norte", 2026, "FUTBOL", "MAYORES", "LIGA");
		admin.inscribirEquipoEnCompetencia(halcones, otra, jugadoresHalcones);
		registrarFutbol(jugar(otra, halcones, "U Andes", 5, 0), jugadoresHalcones.get(0), 5, 0);

		String goleadores = club.consultarGoleadores("Copa Cerros");

		String primero = jugadoresHalcones.get(1).getSocio().getNombre();
		String segundo = jugadoresHalcones.get(0).getSocio().getNombre();
		assertTrue(goleadores.startsWith("1. " + primero + " (Halcones) - 2 goles"), goleadores);
		assertTrue(goleadores.contains("2. " + segundo + " (Halcones) - 1 goles"), goleadores);
	}

	@Test
	public void testS12_destacadosPorAsistenciasOPuntos() {
		admin.inscribirEquipoEnCompetencia(halcones, copa, jugadoresHalcones);
		admin.inscribirEquipoEnCompetencia(leones, copa, jugadoresLeones);
		registrarFutbol(jugar(copa, halcones, "U Andes", 1, 0), jugadoresHalcones.get(0), 1, 1);
		registrarFutbol(jugar(copa, leones, "U Andes", 1, 0), jugadoresLeones.get(0), 0, 3);
		Equipo toros = admin.crearEquipo("BALONCESTO", "Toros", "MAYORES", 10);
		List<InscripcionEquipo> jugadoresToros = llenar(toros, 5);
		Competencia baloncesto = ligaDeBaloncesto(jugadoresToros);
		PartidoDeCompetencia partidoBaloncesto = jugar(baloncesto, toros, "U Andes", 80, 70);
		partidoBaloncesto.registrarParticipacion(jugadoresToros.get(0));
		EstadisticasPartidoBaloncesto puntos = new EstadisticasPartidoBaloncesto();
		puntos.registrarPuntos(20);
		partidoBaloncesto.registrarEstadistica(jugadoresToros.get(0), puntos);

		assertTrue(club.consultarDestacados("Copa Cerros").startsWith("1. " + jugadoresLeones.get(0).getSocio().getNombre() + " (Leones) - 3 asistencias"));
		assertTrue(club.consultarDestacados("Liga Aro").startsWith("1. " + jugadoresToros.get(0).getSocio().getNombre() + " (Toros) - 20 puntos"));
	}

	@Test
	public void testS12_competenciaSinEstadisticasSinInformacion() {
		admin.inscribirEquipoEnCompetencia(halcones, copa, jugadoresHalcones);
		jugar(copa, halcones, "U Andes", 1, 0);

		assertTrue(club.consultarGoleadores("Copa Cerros").contains("No hay informacion"));
		assertTrue(club.consultarDestacados("Copa Cerros").contains("No hay informacion"));
	}
}
