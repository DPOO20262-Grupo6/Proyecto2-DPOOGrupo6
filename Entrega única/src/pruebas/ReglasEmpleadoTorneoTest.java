package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Torneos.ReglasEmpleadoTorneo;
import Torneos.TorneoIndividual;
import administracion.TurnoEmpleado;
import club.ClubDeportivoSeneca;
import enumeraciones.NivelHabilidad;
import enumeraciones.TipoTorneo;
import inscripciones.InscripcionDisciplinaIndividual;
import inscripciones.InscripcionEmpleadoTorneo;
import usuarios.Administrador;
import usuarios.Entrenador;
import usuarios.Fisioterapeuta;
import usuarios.Socio;

/**
 * Pruebas unitarias de ReglasEmpleadoTorneo e InscripcionEmpleadoTorneo:
 * el calculo de "estar de turno un dia", los cupos y la inscripcion del empleado.
 */
public class ReglasEmpleadoTorneoTest {

	private ClubDeportivoSeneca club;
	private Administrador admin;
	private ReglasEmpleadoTorneo reglas;
	private Entrenador entrenador;
	private Fisioterapeuta fisioterapeuta;
	private TorneoIndividual torneo;
	private LocalDate dia;

	@BeforeEach
	public void setUp() throws Exception {
		club = new ClubDeportivoSeneca();
		admin = club.getAdministrador();
		reglas = new ReglasEmpleadoTorneo(club);
		entrenador = new Entrenador("Carlos", "ent1", "123");
		club.registrarEmpleado(entrenador);
		fisioterapeuta = new Fisioterapeuta("Laura", "fisio1", "123");
		club.registrarEmpleado(fisioterapeuta);
		dia = LocalDate.now().plusDays(10);
		torneo = (TorneoIndividual) admin.crearTorneo("Copa Tenis", "TENIS", "SENCILLOS", "Abierta", 10,
				TipoTorneo.COMPETITIVO, dia, 50000, 0);
	}

	// ---------------- Estar de turno un dia ----------------

	@Test
	public void testSinTurnosNoEstaDeTurno() {
		assertFalse(reglas.estaDeTurno(entrenador, dia));
		assertNull(reglas.buscarTurnoElDia(entrenador, dia));
		assertTrue(reglas.consultarTurnos(entrenador).isEmpty());
	}

	@Test
	public void testCualquierTurnoDelDiaCuentaSinImportarLaFranja() {
		TurnoEmpleado turno = admin.asignarTurno(entrenador, dia, LocalTime.of(20, 0), 2);

		assertTrue(reglas.estaDeTurno(entrenador, dia));
		assertSame(turno, reglas.buscarTurnoElDia(entrenador, dia));
		assertFalse(reglas.estaDeTurno(entrenador, dia.minusDays(1)));
		assertFalse(reglas.estaDeTurno(entrenador, dia.plusDays(1)));
	}

	@Test
	public void testTurnoQueCruzaLaMedianocheOcupaTambienElDiaSiguiente() {
		admin.asignarTurno(entrenador, dia.minusDays(1), LocalTime.of(22, 0), 4);

		assertTrue(reglas.estaDeTurno(entrenador, dia.minusDays(1)));
		assertTrue(reglas.estaDeTurno(entrenador, dia));
		assertFalse(reglas.estaDeTurno(entrenador, dia.plusDays(1)));
	}

	@Test
	public void testTurnoQueTerminaALaMedianocheNoOcupaElDiaSiguiente() {
		admin.asignarTurno(entrenador, dia.minusDays(1), LocalTime.of(20, 0), 4);

		assertTrue(reglas.estaDeTurno(entrenador, dia.minusDays(1)));
		assertFalse(reglas.estaDeTurno(entrenador, dia));
	}

	@Test
	public void testElTurnoDeOtroEmpleadoNoCuenta() {
		admin.asignarTurno(fisioterapeuta, dia, LocalTime.of(8, 0), 4);

		assertFalse(reglas.estaDeTurno(entrenador, dia));
		assertTrue(reglas.estaDeTurno(fisioterapeuta, dia));
	}

	@Test
	public void testSiElTurnoSeReprogramaAlDiaDelTorneoYaNoPuedeInscribirse() {
		TurnoEmpleado turno = admin.asignarTurno(entrenador, dia.minusDays(3), LocalTime.of(8, 0), 4);
		turno.reprogramar(dia, LocalTime.of(8, 0));

		assertThrows(IllegalStateException.class, () -> reglas.inscribir(entrenador, torneo.getId()));
		assertEquals(0, torneo.getNumeroInscritos());
	}

	@Test
	public void testConsultarTurnosDevuelveSoloLosPropiosEnOrdenCronologico() {
		TurnoEmpleado tercero = admin.asignarTurno(entrenador, dia.plusDays(2), LocalTime.of(8, 0), 4);
		TurnoEmpleado primero = admin.asignarTurno(entrenador, dia, LocalTime.of(6, 0), 4);
		admin.asignarTurno(fisioterapeuta, dia, LocalTime.of(7, 0), 4);
		TurnoEmpleado segundo = admin.asignarTurno(entrenador, dia, LocalTime.of(14, 0), 4);

		List<TurnoEmpleado> turnos = reglas.consultarTurnos(entrenador);

		assertEquals(3, turnos.size());
		assertSame(primero, turnos.get(0));
		assertSame(segundo, turnos.get(1));
		assertSame(tercero, turnos.get(2));
	}

	// ---------------- Cupos ----------------

	@Test
	public void testLosEmpleadosSoloCuentanConLosCuposRegulares() {
		assertEquals(10, torneo.cuposDisponibles());
		assertEquals(2, torneo.cuposPreferencialesDisponibles());
		assertEquals(8, reglas.cuposParaEmpleados(torneo));

		reglas.inscribir(entrenador, torneo.getId());

		assertEquals(7, reglas.cuposParaEmpleados(torneo));
		assertEquals(2, torneo.cuposPreferencialesDisponibles());
		assertEquals(0, torneo.getCuposPreferencialesOcupados());
	}

	// ---------------- Validacion ----------------

	@Test
	public void testValidarInscripcionNoModificaElTorneo() {
		reglas.validarInscripcion(entrenador, torneo);

		assertEquals(0, torneo.getNumeroInscritos());
		assertFalse(reglas.estaInscrito(entrenador, torneo));
	}

	@Test
	public void testDatosNulosSonRechazados() {
		assertThrows(IllegalArgumentException.class, () -> new ReglasEmpleadoTorneo(null));
		assertThrows(IllegalArgumentException.class, () -> reglas.validarInscripcion(null, torneo));
		assertThrows(IllegalArgumentException.class, () -> reglas.validarInscripcion(entrenador, null));
		assertThrows(IllegalArgumentException.class, () -> reglas.inscribir(null, torneo.getId()));
		assertThrows(IllegalArgumentException.class, () -> reglas.retirar(null, torneo.getId()));
		assertThrows(IllegalArgumentException.class, () -> reglas.consultarTurnos(null));
		assertThrows(IllegalArgumentException.class, () -> new InscripcionEmpleadoTorneo(null, "TENIS"));
	}

	// ---------------- Inscripcion del empleado ----------------

	@Test
	public void testLaInscripcionDelEmpleadoTomaLaDisciplinaDelTorneoYNivelIntermedio() {
		InscripcionEmpleadoTorneo inscripcion = reglas.inscribir(entrenador, torneo.getId());

		assertEquals("TENIS", inscripcion.getDisciplina());
		assertEquals(NivelHabilidad.INTERMEDIO, inscripcion.getNivel());
		assertEquals(InscripcionDisciplinaIndividual.ELO_INICIAL, inscripcion.getRankingElo());
		assertEquals("Carlos (empleado)", inscripcion.getSocio().getNombre());
		assertSame(entrenador, inscripcion.getEmpleado());
	}

	@Test
	public void testElNivelDelEmpleadoEsCompatibleConCualquierSocio() {
		InscripcionEmpleadoTorneo inscripcion = reglas.inscribir(entrenador, torneo.getId());
		Socio socio = new Socio("Ana", "ana", "123", LocalDate.of(2000, 1, 1));

		assertTrue(inscripcion.esNivelCompatible(new InscripcionDisciplinaIndividual(socio, "TENIS", NivelHabilidad.PRINCIPIANTE)));
		assertTrue(inscripcion.esNivelCompatible(new InscripcionDisciplinaIndividual(socio, "TENIS", NivelHabilidad.AVANZADO)));
	}

	@Test
	public void testLaInscripcionCorrespondeSoloASuEmpleado() {
		InscripcionEmpleadoTorneo inscripcion = reglas.inscribir(entrenador, torneo.getId());
		Socio socioConElMismoLogin = new Socio("Otro", "ent1", "123", LocalDate.of(2000, 1, 1));

		assertTrue(inscripcion.correspondeA(entrenador));
		assertFalse(inscripcion.correspondeA(fisioterapeuta));
		assertFalse(inscripcion.correspondeA(socioConElMismoLogin));
		assertFalse(inscripcion.correspondeA(null));
		assertSame(inscripcion, torneo.buscarParticipante(entrenador));
		assertNull(torneo.buscarParticipante(fisioterapeuta));
	}

	@Test
	public void testLaInscripcionDeUnSocioCorrespondeASuSocio() {
		Socio socio = new Socio("Ana", "ana", "123", LocalDate.of(2000, 1, 1));
		club.registrarSocio(socio);
		InscripcionDisciplinaIndividual inscripcion = club.inscribirEnAcademia(socio, "TENIS", NivelHabilidad.INTERMEDIO);
		torneo.inscribir(inscripcion);

		assertTrue(inscripcion.correspondeA(socio));
		assertFalse(inscripcion.correspondeA(entrenador));
		assertTrue(inscripcion.pagaInscripcionTorneo());
		assertSame(inscripcion, torneo.buscarParticipante(socio));
	}

	@Test
	public void testInscribirseEnUnTorneoNoConvierteAlEmpleadoEnSocio() {
		reglas.inscribir(entrenador, torneo.getId());

		assertEquals(0, club.getSocios().size());
		assertEquals(0, club.getInscripcionesIndividuales().size());
		assertNull(club.buscarSocio("ent1"));
		assertSame(entrenador, club.autenticarUsuario("ent1", "123"));
		assertThrows(IllegalArgumentException.class, () -> club.autenticarUsuario("ent1", ""));
	}
}
