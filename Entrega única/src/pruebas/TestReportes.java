package pruebas;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import articulosYVentas.Bebida;
import articulosYVentas.Snack;
import articulosYVentas.VentaCafeteria;
import club.ClubDeportivoSeneca;
import deportesIndividuales.PartidoDeporteIndividual;
import deportesIndividuales.ReservaInstalacionDeporteIndividual;
import enumeraciones.Modalidad;
import enumeraciones.NivelHabilidad;
import estadisticas.EstadisticasPartidoTenis;
import inscripciones.InscripcionDisciplinaIndividual;
import instalaciones.CanchaTenis;
import usuarios.Administrador;
import usuarios.Entrenador;
import usuarios.Socio;

public class TestReportes {

	private ClubDeportivoSeneca club;
	private Administrador admin;
	private Socio ana;
	private Socio beto;
	private Entrenador eva;
	private CanchaTenis cancha;
	private LocalDate hoy;
	private LocalDate manana;

	@BeforeEach
	public void setUp() {
		club = new ClubDeportivoSeneca();
		admin = club.getAdministrador();
		ana = crearSocio("Ana");
		beto = crearSocio("Beto");
		eva = new Entrenador("Eva", "eva", "123");
		cancha = new CanchaTenis("Tenis 1");
		hoy = LocalDate.now();
		manana = hoy.plusDays(1);
		InscripcionDisciplinaIndividual anaTenis = club.inscribirEnAcademia(ana, "TENIS", NivelHabilidad.INTERMEDIO);
		InscripcionDisciplinaIndividual betoTenis = club.inscribirEnAcademia(beto, "TENIS", NivelHabilidad.INTERMEDIO);
		ReservaInstalacionDeporteIndividual reserva = club.reservarInstalacion(Arrays.asList(ana, beto), cancha, manana,
				LocalTime.of(10, 0), LocalTime.of(11, 0), Modalidad.SENCILLOS);
		PartidoDeporteIndividual partido = club.programarPartidoPractica(ana, reserva, Arrays.asList(anaTenis, betoTenis), false);
		club.registrarResultadoPractica(partido, Arrays.asList(new EstadisticasPartidoTenis(2, 12, 3, 5, true),
				new EstadisticasPartidoTenis(0, 6, 1, 9, false)));
		club.planearSesionAcademia(eva, manana, LocalTime.of(15, 0), 60, cancha, Arrays.asList(ana));
	}

	private Socio crearSocio(String nombre) {
		Socio socio = new Socio(nombre, nombre.toLowerCase(), "123", LocalDate.of(2000, 1, 1));
		club.registrarSocio(socio);
		return socio;
	}

	@Test
	public void testA13_historialDelPeriodoConDatosCompletos() {
		String reservas = club.consultarHistorial(admin, "RESERVAS", hoy, manana, null, null, null);
		String practica = club.consultarHistorial(admin, "PARTIDOS_PRACTICA", hoy, manana, null, null, null);

		assertTrue(reservas.contains(manana + " 10:00-11:00 | Reserva Tenis 1 (TENIS) | Socios: Ana, Beto"), reservas);
		assertTrue(practica.contains(manana + " 10:00 | Practica TENIS Ana vs Beto | Resultado: gano Ana"), practica);
	}

	@Test
	public void testA13_filtrosPorSocioEntrenadorODisciplina() {
		Entrenador hugo = new Entrenador("Hugo", "hugo", "123");
		club.planearSesionAcademia(hugo, manana, LocalTime.of(18, 0), 60, cancha, Arrays.asList(beto));

		String deEva = club.consultarHistorial(admin, "SESIONES", hoy, manana, null, eva, null);
		String deBeto = club.consultarHistorial(admin, "SESIONES", hoy, manana, beto, null, null);

		assertTrue(deEva.contains("Entrenador: Eva") && !deEva.contains("Hugo"), deEva);
		assertTrue(deBeto.contains("Entrenador: Hugo") && !deBeto.contains("Eva"), deBeto);
		assertTrue(club.consultarHistorial(admin, "SESIONES", hoy, manana, null, null, "PADEL").contains("No hay registros"));
	}

	@Test
	public void testA13_periodoSinRegistros() {
		assertTrue(club.consultarHistorial(admin, "RESERVAS", hoy.minusDays(10), hoy.minusDays(5), null, null, null).contains("No hay registros"));
	}

	@Test
	public void testA13_rangoInvalidoOUsuarioNoAdministradorRechazado() {
		assertThrows(IllegalArgumentException.class, () -> club.consultarHistorial(admin, "RESERVAS", manana, hoy, null, null, null));
		assertThrows(IllegalArgumentException.class, () -> club.consultarHistorial(ana, "RESERVAS", hoy, manana, null, null, null));
	}

	@Test
	public void testA14_reporteDeVentasPorRubroYPeriodo() {
		Bebida agua = new Bebida("Agua", 4000, false);
		Snack barra = new Snack("Barra", 6000, Arrays.asList("Mani"));
		club.getCafeteria().agregarProducto(agua);
		club.getCafeteria().agregarProducto(barra);
		club.getCafeteria().reabastecer(agua, 5);
		club.getCafeteria().reabastecer(barra, 5);
		VentaCafeteria venta = new VentaCafeteria(hoy, ana);
		venta.agregarDetalle(agua, 1);
		venta.agregarDetalle(barra, 1);
		club.procesarVentaCafeteria(venta, null);

		String reporte = club.consultarReporteVentas("CAFETERIA", "MENSUAL", hoy);

		assertTrue(reporte.contains("ventas 1, neto 10000.0, IVA 0.0, impuesto al consumo 800.0, propinas 1000.0, total 11800.0"), reporte);
	}

	@Test
	public void testA14_periodoSinVentasInformaQueNoHayDatos() {
		assertTrue(club.consultarReporteVentas("TIENDA", "DIARIO", hoy).contains("No hay datos"));
	}

	@Test
	public void testA14_rubroOPeriodoInvalidoRechazado() {
		assertThrows(IllegalArgumentException.class, () -> club.consultarReporteVentas("BAR", "DIARIO", hoy));
		assertThrows(IllegalArgumentException.class, () -> club.consultarReporteVentas("TIENDA", "ANUAL", hoy));
	}

	@Test
	public void testA15_reporteDeEvolucionDelElo() {
		String reporte = club.consultarReporteDeportivo(admin, "EVOLUCION_ELO", hoy, manana);

		assertTrue(reporte.contains("Ana (TENIS): 1000.0 -> 1016.0"), reporte);
		assertTrue(reporte.contains("Beto (TENIS): 1000.0 -> 984.0"), reporte);
	}

	@Test
	public void testA15_periodoSinRegistrosInformaQueNoHayDatos() {
		String reporte = club.consultarReporteDeportivo(admin, "RESULTADOS", hoy.minusDays(10), hoy.minusDays(5));

		assertTrue(reporte.contains("No hay datos"));
		assertFalse(reporte.contains("Ana"));
	}

	@Test
	public void testA15_periodoInvalidoOUsuarioNoAdministradorRechazado() {
		assertThrows(IllegalArgumentException.class, () -> club.consultarReporteDeportivo(admin, "ASISTENCIA", manana, hoy));
		assertThrows(IllegalArgumentException.class, () -> club.consultarReporteDeportivo(eva, "ASISTENCIA", hoy, manana));
	}
}
