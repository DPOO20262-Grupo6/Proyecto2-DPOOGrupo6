package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import administracion.Sugerencia;
import articulosYVentas.ArticuloDeportivo;
import articulosYVentas.Bebida;
import club.ClubDeportivoSeneca;
import enumeraciones.EstadoSolicitud;
import enumeraciones.Modalidad;
import enumeraciones.NivelHabilidad;
import instalaciones.CanchaTenis;
import inventario.UbicacionInventario;
import usuarios.Administrador;
import usuarios.Entrenador;
import usuarios.Socio;

public class TestInventario {

	private ClubDeportivoSeneca club;
	private Administrador admin;
	private Entrenador eva;
	private ArticuloDeportivo raqueta;
	private UbicacionInventario bodega;
	private UbicacionInventario tienda;

	@BeforeEach
	public void setUp() {
		club = new ClubDeportivoSeneca();
		admin = club.getAdministrador();
		eva = new Entrenador("Eva", "eva", "123");
		raqueta = new ArticuloDeportivo("Raqueta", 100000, "Tenis");
		club.getTienda().agregarArticuloAlCatalogo(raqueta);
		club.getCafeteria().agregarProducto(new Bebida("Agua", 4000, false));
		bodega = club.getBodegaCentral();
		tienda = club.getTienda().getUbicacionTienda();
		admin.reabastecer(raqueta, bodega, 10);
		admin.reabastecer(raqueta, tienda, 2);
	}

	private void verificarSinCambios(String motivo, Executable accion) {
		RuntimeException e = assertThrows(RuntimeException.class, accion);
		assertTrue(e.getMessage().toLowerCase().contains(motivo), e.getMessage());
		assertEquals(10, bodega.obtenerCantidad(raqueta));
		assertEquals(2, tienda.obtenerCantidad(raqueta));
	}

	@Test
	public void testA07_inventarioPorUbicacionEInstalaciones() {
		CanchaTenis cancha = new CanchaTenis("Tenis 1");
		club.registrarInstalacion(cancha);
		Socio ana = new Socio("Ana", "ana", "123", LocalDate.of(2000, 1, 1));
		club.registrarSocio(ana);
		club.inscribirEnAcademia(ana, "TENIS", NivelHabilidad.INTERMEDIO);
		club.reservarInstalacion(Arrays.asList(ana, new Socio("Beto", "beto", "1", LocalDate.of(2000, 1, 1))),
				cancha, LocalDate.now(), LocalTime.MIN, LocalTime.MAX, Modalidad.SENCILLOS);

		String inventario = club.consultarInventario(null);

		assertTrue(inventario.contains("Raqueta: Bodega 10, Tienda Principal 2, Cafeteria 0, Total 12"), inventario);
		assertTrue(inventario.contains("Agua: Bodega 0, Tienda Principal 0, Cafeteria 0, Total 0"), inventario);
		assertTrue(inventario.contains("Tenis 1 - TENIS, capacidad 4, En uso"), inventario);
	}

	@Test
	public void testA07_ubicacionInexistenteRechazada() {
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> club.consultarInventario("Sotano"));

		assertTrue(e.getMessage().contains("No se encontro"));
	}

	@Test
	public void testA08_trasladoEntreUbicaciones() {
		admin.trasladarArticulo(raqueta, bodega, tienda, 4);

		assertEquals(6, bodega.obtenerCantidad(raqueta));
		assertEquals(6, tienda.obtenerCantidad(raqueta));
	}

	@Test
	public void testA08_cantidadMayorALaExistenteRechazada() {
		verificarSinCambios("disponible en bodega 10", () -> admin.trasladarArticulo(raqueta, bodega, tienda, 11));
	}

	@Test
	public void testA08_mismoOrigenCantidadInvalidaOUbicacionInexistenteRechazado() {
		UbicacionInventario ajena = new UbicacionInventario("Sotano", "BODEGA");

		verificarSinCambios("distintos", () -> admin.trasladarArticulo(raqueta, bodega, bodega, 1));
		verificarSinCambios("mayor que cero", () -> admin.trasladarArticulo(raqueta, bodega, tienda, 0));
		verificarSinCambios("no existe", () -> admin.trasladarArticulo(raqueta, bodega, ajena, 1));
	}

	@Test
	public void testA09_reabastecimientoAumentaExistencia() {
		admin.reabastecer(raqueta, tienda, 5);

		assertEquals(7, tienda.obtenerCantidad(raqueta));
	}

	@Test
	public void testA09_cantidadCeroONegativaRechazada() {
		verificarSinCambios("mayor que cero", () -> admin.reabastecer(raqueta, tienda, 0));
		verificarSinCambios("mayor que cero", () -> admin.reabastecer(raqueta, tienda, -3));
	}

	@Test
	public void testA09_productoOUbicacionInexistenteRechazado() {
		ArticuloDeportivo casco = new ArticuloDeportivo("Casco", 70000, "Ciclismo");

		verificarSinCambios("producto no existe", () -> admin.reabastecer(casco, tienda, 5));
		verificarSinCambios("ubicacion no existe", () -> admin.reabastecer(raqueta, new UbicacionInventario("Sotano", "BODEGA"), 5));
		assertEquals(0, tienda.obtenerCantidad(casco));
	}

	@Test
	public void testE05_sugerenciaRegistradaPendienteConAutor() {
		Sugerencia sugerencia = club.proponerSugerenciaArticulo(eva, "Cuerda", 30000, "Accesorios", "Cuerda de repuesto para raquetas");

		assertTrue(club.getSugerencias().contains(sugerencia));
		assertEquals(EstadoSolicitud.PENDIENTE, sugerencia.getEstado());
		assertEquals("Eva", sugerencia.getAutor());
	}

	@Test
	public void testE05_nombreODescripcionVaciosRechazados() {
		IllegalArgumentException sinNombre = assertThrows(IllegalArgumentException.class,
				() -> club.proponerSugerenciaArticulo(eva, " ", 30000, "Accesorios", "Descripcion"));
		IllegalArgumentException sinDescripcion = assertThrows(IllegalArgumentException.class,
				() -> club.proponerSugerenciaSnack(eva, "Mani", 3000, Arrays.asList("Mani"), ""));

		assertTrue(sinNombre.getMessage().contains("nombre"));
		assertTrue(sinDescripcion.getMessage().contains("descripcion"));
		assertTrue(club.getSugerencias().isEmpty());
	}

	@Test
	public void testA10_aprobacionAgregaProductoConCantidadCero() {
		Sugerencia sugerencia = club.proponerSugerenciaArticulo(eva, "Cuerda", 30000, "Accesorios", "Cuerda de repuesto");

		admin.evaluarSugerencia(sugerencia, true, null);

		assertEquals(EstadoSolicitud.APROBADA, sugerencia.getEstado());
		assertTrue(club.getTienda().getCatalogo().contains(sugerencia.getArticuloCreado()));
		assertEquals(0, tienda.obtenerCantidad(sugerencia.getArticuloCreado()));
	}

	@Test
	public void testA10_rechazoConMotivoVisibleParaElEmpleado() {
		Sugerencia sugerencia = club.proponerSugerenciaSnack(eva, "Mani", 3000, Arrays.asList("Mani"), "Snack salado");

		admin.evaluarSugerencia(sugerencia, false, "Ya hay un producto similar");

		assertEquals(EstadoSolicitud.RECHAZADA, sugerencia.getEstado());
		assertEquals("Ya hay un producto similar", sugerencia.getMotivoRechazo());
	}

	@Test
	public void testA10_sugerenciaResueltaORechazoSinMotivoRechazado() {
		Sugerencia pendiente = club.proponerSugerenciaArticulo(eva, "Cuerda", 30000, "Accesorios", "Cuerda de repuesto");
		Sugerencia resuelta = club.proponerSugerenciaArticulo(eva, "Grip", 10000, "Accesorios", "Grip para raqueta");
		admin.evaluarSugerencia(resuelta, true, null);
		int catalogo = club.getTienda().getCatalogo().size();

		assertThrows(IllegalArgumentException.class, () -> admin.evaluarSugerencia(pendiente, false, " "));
		assertThrows(IllegalStateException.class, () -> admin.evaluarSugerencia(resuelta, false, "Tarde"));
		assertEquals(EstadoSolicitud.PENDIENTE, pendiente.getEstado());
		assertEquals(EstadoSolicitud.APROBADA, resuelta.getEstado());
		assertEquals(catalogo, club.getTienda().getCatalogo().size());
	}
}
