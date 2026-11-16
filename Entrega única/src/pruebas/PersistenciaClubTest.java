package pruebas;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import Torneos.ReglasEmpleadoTorneo;
import Torneos.Torneo;
import Torneos.TorneoIndividual;
import club.ClubDeportivoSeneca;
import enumeraciones.NivelHabilidad;
import enumeraciones.TipoTorneo;
import inscripciones.InscripcionDisciplinaIndividual;
import persistencia.PersistenciaClub;
import persistencia.PersistenciaException;
import usuarios.EmpleadoDelClub;
import usuarios.Entrenador;
import usuarios.Fisioterapeuta;
import usuarios.Socio;

/**
 * Pruebas de la carga y el guardado de los datos del club en archivos predeterminados.
 * Cada prueba usa una carpeta temporal propia: nunca toca los datos reales del proyecto.
 */
public class PersistenciaClubTest {

	@TempDir
	File carpetaTemporal;

	private File carpetaDatos;
	private PersistenciaClub persistencia;
	private ClubDeportivoSeneca club;
	private Entrenador entrenador;
	private Socio socio;
	private Torneo torneo;

	@BeforeEach
	public void setUp() throws Exception {
		carpetaDatos = new File(carpetaTemporal, PersistenciaClub.NOMBRE_CARPETA);
		persistencia = new PersistenciaClub(carpetaDatos);

		club = new ClubDeportivoSeneca();
		socio = new Socio("Laura Gomez", "lgomez", "clave123", LocalDate.of(2000, 3, 15));
		club.registrarSocio(socio);
		entrenador = new Entrenador("Carlos Ruiz", "cruiz", "entrena1");
		club.registrarEmpleado(entrenador);
		club.registrarEmpleado(new Fisioterapeuta("Ana Torres", "atorres", "fisio1"));
		club.getAdministrador().asignarTurno(entrenador, LocalDate.now().plusDays(3), LocalTime.of(8, 0), 4);

		torneo = club.getAdministrador().crearTorneo("Copa Tenis", "TENIS", "SENCILLOS", "Abierta", 8,
				TipoTorneo.COMPETITIVO, LocalDate.now().plusDays(10), 50000, 0);
		InscripcionDisciplinaIndividual inscripcionSocio = club.inscribirEnAcademia(socio, "TENIS", NivelHabilidad.INTERMEDIO);
		((TorneoIndividual) torneo).inscribir(inscripcionSocio);
		new ReglasEmpleadoTorneo(club).inscribir(entrenador, torneo.getId());
	}

	// ---------------- Cargar ----------------

	@Test
	public void testCargarSinArchivoDevuelveUnClubNuevo() {
		assertFalse(persistencia.existenDatos());

		ClubDeportivoSeneca cargado = persistencia.cargar();

		assertNotNull(cargado.getAdministrador());
		assertEquals(0, cargado.getSocios().size());
		assertEquals(0, cargado.getTorneos().size());
		assertFalse(carpetaDatos.exists());
	}

	@Test
	public void testGuardarYCargarConservaTorneosEInscripcionesDeEmpleados() {
		persistencia.guardar(club);
		ClubDeportivoSeneca cargado = persistencia.cargar();

		Torneo torneoCargado = cargado.buscarTorneo(torneo.getId());
		EmpleadoDelClub empleadoCargado = (EmpleadoDelClub) cargado.autenticarUsuario("cruiz", "entrena1");
		assertNotNull(torneoCargado);
		assertEquals("Copa Tenis", torneoCargado.getNombre());
		assertEquals(2, torneoCargado.getNumeroInscritos());
		assertEquals(1, torneoCargado.getNumeroInscritosQuePagan());
		assertEquals(50000, torneoCargado.getTotalRecaudado());
		assertTrue(new ReglasEmpleadoTorneo(cargado).estaInscrito(empleadoCargado, torneoCargado));
	}

	@Test
	public void testLosCambiosDeUnaEjecucionSeVenEnLaSiguiente() {
		persistencia.guardar(club);

		ClubDeportivoSeneca primeraEjecucion = persistencia.cargar();
		EmpleadoDelClub empleado = (EmpleadoDelClub) primeraEjecucion.autenticarUsuario("cruiz", "entrena1");
		new ReglasEmpleadoTorneo(primeraEjecucion).retirar(empleado, torneo.getId());
		persistencia.guardar(primeraEjecucion);

		ClubDeportivoSeneca segundaEjecucion = persistencia.cargar();
		EmpleadoDelClub empleadoDespues = (EmpleadoDelClub) segundaEjecucion.autenticarUsuario("cruiz", "entrena1");
		assertEquals(1, segundaEjecucion.buscarTorneo(torneo.getId()).getNumeroInscritos());
		assertFalse(new ReglasEmpleadoTorneo(segundaEjecucion).estaInscrito(empleadoDespues, segundaEjecucion.buscarTorneo(torneo.getId())));
	}

	@Test
	public void testArchivoDanadoSeInformaYNoSeModifica() throws Exception {
		carpetaDatos.mkdirs();
		byte[] basura = "esto no es un club".getBytes();
		Files.write(persistencia.getArchivoDatos().toPath(), basura);

		PersistenciaException e = assertThrows(PersistenciaException.class, () -> persistencia.cargar());

		assertTrue(e.getMessage().contains(PersistenciaClub.NOMBRE_ARCHIVO));
		assertArrayEquals(basura, Files.readAllBytes(persistencia.getArchivoDatos().toPath()));
	}

	// ---------------- Guardar ----------------

	@Test
	public void testSiElGuardadoFallaSeConservanLosDatosAnteriores() throws Exception {
		persistencia.guardar(club);
		byte[] antes = Files.readAllBytes(persistencia.getArchivoDatos().toPath());
		ClubDeportivoSeneca clubQueFalla = new ClubDeportivoSeneca() {
			private static final long serialVersionUID = 1L;

			@Override
			public void guardarDatos(String carpeta) {
				throw new RuntimeException("disco lleno");
			}
		};

		PersistenciaException e = assertThrows(PersistenciaException.class, () -> persistencia.guardar(clubQueFalla));

		assertTrue(e.getMessage().contains("disco lleno"));
		assertArrayEquals(antes, Files.readAllBytes(persistencia.getArchivoDatos().toPath()));
		assertEquals(1, persistencia.cargar().getSocios().size());
	}

	// ---------------- Carpeta predeterminada ----------------

}
