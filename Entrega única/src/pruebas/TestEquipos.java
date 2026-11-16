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
import deportesDeConjunto.Equipo;
import deportesDeConjunto.PartidoInterno;
import deportesDeConjunto.ResultadoPorPuntos;
import deportesDeConjunto.TorneoDeLosCerros;
import inscripciones.InscripcionEquipo;
import usuarios.Administrador;
import usuarios.Socio;

public class TestEquipos {

	private ClubDeportivoSeneca club;
	private Administrador admin;
	private Equipo halcones;
	private List<InscripcionEquipo> jugadores;
	private Socio libre;
	private int contador;

	@BeforeEach
	public void setUp() {
		club = new ClubDeportivoSeneca();
		admin = club.getAdministrador();
		halcones = admin.crearEquipo("FUTBOL", "Halcones", "MAYORES", 8);
		jugadores = llenar(halcones, 7);
		libre = crearSocio(20);
	}

	private Socio crearSocio(int edad) {
		contador = contador + 1;
		Socio socio = new Socio("Socio" + contador, "s" + contador, "123", LocalDate.now().minusYears(edad).minusDays(1));
		club.registrarSocio(socio);
		return socio;
	}

	private List<InscripcionEquipo> llenar(Equipo equipo, int cantidad) {
		List<InscripcionEquipo> lista = new ArrayList<InscripcionEquipo>();
		for (int i = 1; i <= cantidad; i++) {
			lista.add(admin.inscribirJugador(equipo, crearSocio(20), "Delantero", i));
		}
		return lista;
	}

	private void verificarRechazo(String motivo, Executable accion) {
		int equipos = club.getEquipos().size();
		int inscritos = halcones.getInscripciones().size();
		boolean activo = halcones.isActivo();
		RuntimeException e = assertThrows(RuntimeException.class, accion);
		assertTrue(e.getMessage().toLowerCase().contains(motivo), e.getMessage());
		assertEquals(equipos, club.getEquipos().size());
		assertEquals(inscritos, halcones.getInscripciones().size());
		assertEquals(activo, halcones.isActivo());
	}

	private TorneoDeLosCerros inscribirEnCompetencia() {
		TorneoDeLosCerros competencia = new TorneoDeLosCerros("Cerros", 2026, "FUTBOL", "MAYORES", 7);
		admin.inscribirEquipoEnCompetencia(halcones, competencia, jugadores);
		return competencia;
	}

	@Test
	public void testA03_equipoCreadoActivoYSinJugadores() {
		Equipo equipo = admin.crearEquipo("VOLEIBOL", "Tigres", "SUB-17", 12);

		assertTrue(club.getEquipos().contains(equipo));
		assertTrue(equipo.isActivo());
		assertTrue(equipo.getInscripciones().isEmpty());
	}

	@Test
	public void testA03_equipoRepetidoRechazado() {
		verificarRechazo("ya existe", () -> admin.crearEquipo("FUTBOL", "Halcones", "MAYORES", 10));
	}

	@Test
	public void testA03_datoFaltanteOCupoInvalidoRechazado() {
		verificarRechazo("faltan datos", () -> admin.crearEquipo("FUTBOL", " ", "MAYORES", 10));
		verificarRechazo("faltan datos", () -> admin.crearEquipo("FUTBOL", "Leones", null, 10));
		verificarRechazo("cupo", () -> admin.crearEquipo("FUTBOL", "Leones", "MAYORES", 0));
	}

	@Test
	public void testA03_cupoMenorAlMinimoRechazado() {
		verificarRechazo("minimo", () -> admin.crearEquipo("FUTBOL", "Leones", "MAYORES", 6));
		verificarRechazo("minimo", () -> admin.crearEquipo("BALONCESTO", "Leones", "MAYORES", 4));
	}

	@Test
	public void testA04_socioInscrito() {
		InscripcionEquipo inscripcion = admin.inscribirJugador(halcones, libre, "Portero", 8);

		assertTrue(halcones.getInscripciones().contains(inscripcion));
		assertEquals(libre, inscripcion.getSocio());
		assertEquals("Portero", inscripcion.getPosicion());
		assertEquals(8, inscripcion.getNumeroCamiseta());
	}

	@Test
	public void testA04_edadNoCorrespondeRechazada() {
		Equipo sub15 = admin.crearEquipo("BALONCESTO", "Pumas", "SUB-15", 10);

		assertThrows(IllegalArgumentException.class, () -> admin.inscribirJugador(sub15, libre, "Base", 1));
		assertTrue(sub15.getInscripciones().isEmpty());
	}

	@Test
	public void testA04_socioEnOtroEquipoDelMismoDeporteRechazado() {
		Equipo otro = admin.crearEquipo("FUTBOL", "Leones", "MAYORES", 10);

		assertThrows(IllegalArgumentException.class, () -> admin.inscribirJugador(otro, jugadores.get(0).getSocio(), "Defensa", 1));
		assertTrue(otro.getInscripciones().isEmpty());
	}

	@Test
	public void testA04_camisetaUsadaOEquipoSinCupoRechazado() {
		verificarRechazo("camiseta", () -> admin.inscribirJugador(halcones, libre, "Portero", 1));
		admin.inscribirJugador(halcones, crearSocio(20), "Portero", 8);
		verificarRechazo("cupo", () -> admin.inscribirJugador(halcones, libre, "Defensa", 9));
	}

	@Test
	public void testA04_equipoInactivoRechazado() {
		admin.darDeBajaEquipo(halcones);

		verificarRechazo("inactivo", () -> admin.inscribirJugador(halcones, libre, "Portero", 8));
	}

	@Test
	public void testA04_equipoLlenoRechazado() {
		admin.inscribirJugador(halcones, crearSocio(20), "Portero", 8);

		assertEquals(halcones.getCupoMaximoJugadores(), halcones.getInscripciones().size());
		verificarRechazo("cupo maximo", () -> admin.inscribirJugador(halcones, libre, "Defensa", 9));
	}

	@Test
	public void testA05_bajaLiberaCupoYCamisetaYConservaHistorial() {
		Equipo rival = admin.crearEquipo("FUTBOL", "Leones", "MAYORES", 8);
		llenar(rival, 7);
		PartidoInterno partido = admin.programarPartidoInterno(halcones, rival, LocalDate.now());
		InscripcionEquipo jugador = jugadores.get(0);
		partido.registrarParticipacion(jugador);

		admin.darDeBajaJugador(halcones, jugador);

		assertFalse(halcones.getInscripciones().contains(jugador));
		assertFalse(jugador.isActiva());
		assertTrue(halcones.numeroCamisetaDisponible(1));
		assertTrue(partido.getParticipantes().contains(jugador));
		admin.inscribirJugador(halcones, libre, "Delantero", 1);
	}

	@Test
	public void testA05_jugadorEnNominaDeCompetenciaEnCursoRechazado() {
		inscribirEnCompetencia();

		verificarRechazo("competencia en curso", () -> admin.darDeBajaJugador(halcones, jugadores.get(0)));
	}

	@Test
	public void testA05_jugadorYaDadoDeBajaRechazado() {
		admin.darDeBajaJugador(halcones, jugadores.get(0));

		verificarRechazo("ya fue dado de baja", () -> admin.darDeBajaJugador(halcones, jugadores.get(0)));
	}

	@Test
	public void testA06_bajaDejaInactivoLiberaIntegrantesYConservaHistorial() {
		Equipo rival = admin.crearEquipo("FUTBOL", "Leones", "MAYORES", 8);
		llenar(rival, 7);
		PartidoInterno partido = admin.programarPartidoInterno(halcones, rival, LocalDate.now());
		admin.registrarResultado(partido, new ResultadoPorPuntos(2, 1));

		admin.darDeBajaEquipo(halcones);

		assertFalse(halcones.isActivo());
		assertTrue(club.getEquipos().contains(halcones));
		assertTrue(club.getPartidosInternos().contains(partido));
		assertEquals(7, halcones.getInscripciones().size());
		assertFalse(jugadores.get(0).isActiva());
		Equipo nuevo = admin.crearEquipo("FUTBOL", "Pumas", "MAYORES", 10);
		admin.inscribirJugador(nuevo, jugadores.get(0).getSocio(), "Delantero", 1);
	}

	@Test
	public void testA06_equipoConPartidosPendientesOCompetenciaEnCursoRechazado() {
		Equipo rival = admin.crearEquipo("FUTBOL", "Leones", "MAYORES", 8);
		llenar(rival, 7);
		PartidoInterno partido = admin.programarPartidoInterno(halcones, rival, LocalDate.now().plusDays(3));
		verificarRechazo("pendiente", () -> admin.darDeBajaEquipo(halcones));

		admin.registrarResultado(partido, new ResultadoPorPuntos(1, 0));
		inscribirEnCompetencia();
		verificarRechazo("competencia en curso", () -> admin.darDeBajaEquipo(halcones));
	}

	@Test
	public void testA06_equipoYaInactivoRechazado() {
		admin.darDeBajaEquipo(halcones);

		verificarRechazo("ya esta inactivo", () -> admin.darDeBajaEquipo(halcones));
	}
}
