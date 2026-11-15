package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import articulosYVentas.ArticuloDeportivo;
import articulosYVentas.Bebida;
import articulosYVentas.Cafeteria;
import articulosYVentas.Snack;
import articulosYVentas.TiendaDeportiva;
import articulosYVentas.VentaCafeteria;
import articulosYVentas.VentaTienda;
import club.ClubDeportivoSeneca;
import enumeraciones.Modalidad;
import enumeraciones.NivelHabilidad;
import instalaciones.CanchaPadel;
import usuarios.Entrenador;
import usuarios.Socio;
import usuarios.Usuario;

public class TestVentas {

	private ClubDeportivoSeneca club;
	private TiendaDeportiva tienda;
	private Cafeteria cafeteria;
	private Socio ana;
	private Entrenador eva;
	private Entrenador hugo;
	private ArticuloDeportivo raqueta;
	private ArticuloDeportivo balon;
	private Bebida agua;
	private Bebida cafe;
	private Snack barra;
	private Snack galleta;

	@BeforeEach
	public void setUp() {
		club = new ClubDeportivoSeneca();
		tienda = club.getTienda();
		cafeteria = club.getCafeteria();
		ana = crearSocio("Ana");
		eva = new Entrenador("Eva", "eva", "123");
		hugo = new Entrenador("Hugo", "hugo", "123");
		club.registrarEmpleado(eva);
		club.registrarEmpleado(hugo);
		raqueta = new ArticuloDeportivo("Raqueta", 100000, "Tenis");
		balon = new ArticuloDeportivo("Balon", 50000, "Futbol");
		tienda.agregarArticuloAlCatalogo(raqueta);
		tienda.agregarArticuloAlCatalogo(balon);
		tienda.getUbicacionTienda().agregarExistencias(raqueta, 5);
		tienda.getUbicacionTienda().agregarExistencias(balon, 2);
		agua = new Bebida("Agua", 4000, false);
		cafe = new Bebida("Cafe", 5000, true);
		barra = new Snack("Barra", 6000, Arrays.asList("Mani"));
		galleta = new Snack("Galleta", 3000, new ArrayList<String>());
		cafeteria.agregarProducto(agua);
		cafeteria.agregarProducto(cafe);
		cafeteria.agregarProducto(barra);
		cafeteria.agregarProducto(galleta);
		cafeteria.reabastecer(agua, 10);
		cafeteria.reabastecer(cafe, 10);
		cafeteria.reabastecer(barra, 10);
		cafeteria.reabastecer(galleta, 10);
	}

	private Socio crearSocio(String nombre) {
		Socio socio = new Socio(nombre, nombre.toLowerCase(), "123", LocalDate.of(2000, 1, 1));
		club.registrarSocio(socio);
		return socio;
	}

	private VentaTienda ventaTienda(Usuario comprador, ArticuloDeportivo articulo, int cantidad) {
		VentaTienda venta = new VentaTienda(LocalDate.now(), comprador);
		venta.agregarDetalle(articulo, cantidad);
		return venta;
	}

	private VentaCafeteria ventaCafeteria(Usuario comprador) {
		VentaCafeteria venta = new VentaCafeteria(LocalDate.now(), comprador);
		venta.agregarDetalle(agua, 1);
		return venta;
	}

	private void verificarSinCambios(Class<? extends RuntimeException> tipo, String motivo, Executable accion) {
		int stockRaqueta = tienda.getUbicacionTienda().obtenerCantidad(raqueta);
		int stockAgua = cafeteria.getInventario().obtenerCantidad(agua);
		int ventas = tienda.getVentas().size() + cafeteria.getVentas().size();
		int puntosAna = ana.getPuntosDeFidelidad();
		int puntosEva = eva.getPuntosDeFidelidad();
		RuntimeException e = assertThrows(tipo, accion);
		assertTrue(e.getMessage().toLowerCase().contains(motivo.toLowerCase()), e.getMessage());
		assertEquals(stockRaqueta, tienda.getUbicacionTienda().obtenerCantidad(raqueta));
		assertEquals(stockAgua, cafeteria.getInventario().obtenerCantidad(agua));
		assertEquals(ventas, tienda.getVentas().size() + cafeteria.getVentas().size());
		assertEquals(puntosAna, ana.getPuntosDeFidelidad());
		assertEquals(puntosEva, eva.getPuntosDeFidelidad());
	}

	@Test
	public void testS13_compraRegistradaConDesgloseYExistenciasDescontadas() {
		VentaTienda venta = ventaTienda(ana, raqueta, 1);
		venta.agregarDetalle(balon, 2);

		tienda.procesarVenta(venta, null);

		assertTrue(tienda.getVentas().contains(venta));
		assertEquals(LocalDate.now(), venta.getFecha());
		assertEquals(200000.0, venta.calcularSubtotal());
		assertEquals(38000.0, venta.calcularImpuestos());
		assertEquals(238000.0, venta.calcularTotal());
		assertEquals(4, tienda.getUbicacionTienda().obtenerCantidad(raqueta));
		assertEquals(0, tienda.getUbicacionTienda().obtenerCantidad(balon));
	}

	@Test
	public void testS13_faltaExistenciaRechazaTodaLaCompra() {
		VentaTienda venta = ventaTienda(ana, raqueta, 1);
		venta.agregarDetalle(balon, 3);

		verificarSinCambios(IllegalStateException.class, "Balon", () -> tienda.procesarVenta(venta, null));
		assertEquals(2, tienda.getUbicacionTienda().obtenerCantidad(balon));
	}

	@Test
	public void testS13_articuloInexistenteOCantidadInvalidaRechazada() {
		ArticuloDeportivo fueraDeCatalogo = new ArticuloDeportivo("Casco", 70000, "Ciclismo");
		VentaTienda venta = ventaTienda(ana, fueraDeCatalogo, 1);

		verificarSinCambios(IllegalArgumentException.class, "no existe", () -> tienda.procesarVenta(venta, null));
		assertThrows(IllegalArgumentException.class, () -> venta.agregarDetalle(raqueta, 0));
		assertThrows(IllegalArgumentException.class, () -> venta.agregarDetalle(raqueta, -2));
	}

	@Test
	public void testS14_compraConImpuestoAlConsumoYPropinaSugerida() {
		VentaCafeteria venta = ventaCafeteria(ana);
		venta.agregarDetalle(barra, 1);

		assertTrue(cafeteria.informarAlergenos(Arrays.asList(agua, barra)).contains("Mani"));
		club.procesarVentaCafeteria(venta, null);

		assertTrue(cafeteria.getVentas().contains(venta));
		assertEquals(800.0, venta.calcularImpuestos());
		assertEquals(1000.0, venta.calcularPropina());
		assertEquals(11800.0, venta.calcularTotal());
		assertEquals(9, cafeteria.getInventario().obtenerCantidad(agua));
	}

	@Test
	public void testS14_propinaModificadaPorElSocio() {
		VentaCafeteria venta = ventaCafeteria(ana);
		venta.setPorcentajePropina(0);

		club.procesarVentaCafeteria(venta, null);

		assertEquals(0.0, venta.calcularPropina());
		assertEquals(4320.0, venta.calcularTotal());
	}

	@Test
	public void testS14_sinExistenciaSnackSinAlergenosOBebidaCalienteConReservaRechazada() {
		VentaCafeteria sinExistencia = new VentaCafeteria(LocalDate.now(), ana);
		sinExistencia.agregarDetalle(agua, 11);
		VentaCafeteria sinAlergenos = new VentaCafeteria(LocalDate.now(), ana);
		sinAlergenos.agregarDetalle(galleta, 1);
		club.inscribirEnAcademia(ana, "PADEL", NivelHabilidad.INTERMEDIO);
		club.reservarInstalacion(Arrays.asList(ana, crearSocio("Beto"), crearSocio("Carla"), crearSocio("Dani")),
				new CanchaPadel("Padel 1", true), LocalDate.now(), LocalTime.MIN, LocalTime.MAX, Modalidad.DOBLES);
		VentaCafeteria caliente = new VentaCafeteria(LocalDate.now(), ana);
		caliente.agregarDetalle(cafe, 1);

		verificarSinCambios(IllegalStateException.class, "existencia", () -> club.procesarVentaCafeteria(sinExistencia, null));
		verificarSinCambios(IllegalStateException.class, "alergenos", () -> club.procesarVentaCafeteria(sinAlergenos, null));
		verificarSinCambios(IllegalStateException.class, "bebidas calientes", () -> club.procesarVentaCafeteria(caliente, null));
		assertEquals(10, cafeteria.getInventario().obtenerCantidad(cafe));
	}

	@Test
	public void testS15_puntosDelDosPorCientoSinPropinaTruncados() {
		tienda.procesarVenta(ventaTienda(ana, raqueta, 2), null);
		club.procesarVentaCafeteria(ventaCafeteria(ana), null);

		assertEquals(4760 + 86, ana.getPuntosDeFidelidad());
	}

	@Test
	public void testS15_compraRechazadaNoCambiaElSaldo() {
		ana.setPuntosDeFidelidad(300);

		verificarSinCambios(IllegalStateException.class, "stock", () -> tienda.procesarVenta(ventaTienda(ana, raqueta, 9), null));
		assertEquals(300, ana.getPuntosDeFidelidad());
	}

	@Test
	public void testS16_redencionDescuentaDelTotalYDelSaldo() {
		ana.setPuntosDeFidelidad(1000);
		VentaCafeteria venta = ventaCafeteria(ana);
		venta.redimirPuntos(500);

		club.procesarVentaCafeteria(venta, null);

		assertEquals(3820.0, venta.calcularTotalSinPropina());
		assertEquals(4220.0, venta.calcularTotal());
		assertEquals(1000 - 500 + 76, ana.getPuntosDeFidelidad());
	}

	@Test
	public void testS16_redimirMasQueElSaldoOElTotalRechazado() {
		ana.setPuntosDeFidelidad(1000);
		VentaTienda masQueSaldo = ventaTienda(ana, raqueta, 1);
		masQueSaldo.redimirPuntos(2000);

		verificarSinCambios(IllegalArgumentException.class, "Saldo disponible: 1000", () -> tienda.procesarVenta(masQueSaldo, null));

		ana.setPuntosDeFidelidad(6000);
		VentaCafeteria masQueTotal = ventaCafeteria(ana);
		masQueTotal.redimirPuntos(5000);
		verificarSinCambios(IllegalArgumentException.class, "Saldo disponible: 6000", () -> club.procesarVentaCafeteria(masQueTotal, null));
	}

	@Test
	public void testS17_codigoDeEmpleadoAplicaOchoPorCiento() {
		VentaTienda enTienda = ventaTienda(ana, raqueta, 1);
		VentaCafeteria enCafeteria = ventaCafeteria(ana);

		tienda.procesarVenta(enTienda, eva.getCodigoDescuento());
		club.procesarVentaCafeteria(enCafeteria, eva.getCodigoDescuento());

		assertEquals(0.08, enTienda.getDescuentoAplicado());
		assertEquals(92000.0, enTienda.getBaseGravable());
		assertEquals(109480.0, enTienda.calcularTotal());
		assertEquals(0.08, enCafeteria.getDescuentoAplicado());
		assertEquals(3680.0, enCafeteria.getBaseGravable());
	}

	@Test
	public void testS17_codigoInexistenteOInactivoRechazado() {
		tienda.desactivarCodigo(hugo.getCodigoDescuento());

		verificarSinCambios(IllegalArgumentException.class, "no existe o esta inactivo", () -> tienda.procesarVenta(ventaTienda(ana, raqueta, 1), "COD-NADIE"));
		verificarSinCambios(IllegalArgumentException.class, "no existe o esta inactivo", () -> club.procesarVentaCafeteria(ventaCafeteria(ana), hugo.getCodigoDescuento()));
	}

	@Test
	public void testS17_otraPromocionAplicadaNoSeAcumula() {
		VentaTienda venta = ventaTienda(ana, raqueta, 1);
		venta.aplicarDescuento(0.10);

		verificarSinCambios(IllegalStateException.class, "acumular", () -> tienda.procesarVenta(venta, eva.getCodigoDescuento()));
		assertEquals(0.10, venta.getDescuentoAplicado());
	}

	@Test
	public void testE06_codigoPropioAplicaQuincePorCientoYGeneraPuntos() {
		VentaTienda venta = ventaTienda(eva, raqueta, 1);

		tienda.procesarVenta(venta, eva.getCodigoDescuento());

		assertEquals(0.15, venta.getDescuentoAplicado());
		assertEquals(85000.0, venta.getBaseGravable());
		assertEquals(101150.0, venta.calcularTotal());
		assertEquals(2023, eva.getPuntosDeFidelidad());
	}

	@Test
	public void testE06_codigoDeOtroEmpleadoAplicaOchoPorCiento() {
		VentaTienda venta = ventaTienda(eva, raqueta, 1);

		tienda.procesarVenta(venta, hugo.getCodigoDescuento());

		assertEquals(0.08, venta.getDescuentoAplicado());
	}

	@Test
	public void testE06_codigoInexistenteOInactivoRechazado() {
		tienda.desactivarCodigo(hugo.getCodigoDescuento());

		verificarSinCambios(IllegalArgumentException.class, "no existe o esta inactivo", () -> tienda.procesarVenta(ventaTienda(eva, raqueta, 1), "COD-NADIE"));
		verificarSinCambios(IllegalArgumentException.class, "no existe o esta inactivo", () -> tienda.procesarVenta(ventaTienda(eva, raqueta, 1), hugo.getCodigoDescuento()));
	}

	@Test
	public void testE06_otraPromocionAplicadaNoSeAcumula() {
		VentaTienda venta = ventaTienda(eva, raqueta, 1);
		venta.aplicarDescuento(0.10);

		verificarSinCambios(IllegalStateException.class, "acumular", () -> tienda.procesarVenta(venta, eva.getCodigoDescuento()));
	}
}
