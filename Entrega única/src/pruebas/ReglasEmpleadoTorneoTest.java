package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Torneos.ReglasEmpleadoTorneo;
import Torneos.TorneoIndividual;
import administracion.TurnoEmpleado;
import club.ClubDeportivoSeneca;
import enumeraciones.TipoTorneo;
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

	// ---------------- Inscripcion del empleado ----------------

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

}
