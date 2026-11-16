package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import articulosYVentas.Articulo;
import articulosYVentas.ArticuloDeportivo;
import articulosYVentas.Bebida;
import articulosYVentas.Bono;
import articulosYVentas.ComprasSocio;
import articulosYVentas.Snack;
import articulosYVentas.VentaCafeteria;
import articulosYVentas.VentaTienda;
import club.ClubDeportivoSeneca;
import usuarios.Entrenador;
import usuarios.Socio;

/**
 * Pruebas de las compras del socio (HU-S13 a HU-S17) y de la historia HU-S22, usar un bono:
 * el bono se guarda en el socio, se usa en una compra de la tienda o la cafeteria y no se acumula
 * con otros descuentos. En los rechazos se verifica el motivo y que no cambien existencias,
 * puntos ni bonos.
 */
public class ComprasSocioTest {

	private ClubDeportivoSeneca club;
	private ComprasSocio compras;
	private Socio ana;
	private Socio beto;
	private ArticuloDeportivo raqueta;
	private ArticuloDeportivo balon;
	private Bebida cafe;
	private Bebida jugo;
	private Snack galleta;
	private Snack barra;

	@BeforeEach
	public void setUp() {
		club = new ClubDeportivoSeneca();
		compras = new ComprasSocio(club);
		ana = crearSocio("Ana", "ana");
		beto = crearSocio("Beto", "beto");

		raqueta = new ArticuloDeportivo("Raqueta", 200000, "TENIS");
		balon = new ArticuloDeportivo("Balon", 80000, "FUTBOL");
		club.getTienda().agregarArticuloAlCatalogo(raqueta);
		club.getTienda().agregarArticuloAlCatalogo(balon);
		club.getTienda().getUbicacionTienda().agregarExistencias(raqueta, 5);
		club.getTienda().getUbicacionTienda().agregarExistencias(balon, 3);

		cafe = new Bebida("Cafe", 5000, true);
		jugo = new Bebida("Jugo", 4000, false);
		galleta = new Snack("Galleta", 3000, List.of("gluten", "huevo"));
		barra = new Snack("Barra", 2500, new ArrayList<String>());
		club.getCafeteria().agregarProducto(cafe);
		club.getCafeteria().agregarProducto(jugo);
		club.getCafeteria().agregarProducto(galleta);
		club.getCafeteria().agregarProducto(barra);
		club.getCafeteria().reabastecer(cafe, 10);
		club.getCafeteria().reabastecer(jugo, 10);
		club.getCafeteria().reabastecer(galleta, 10);
		club.getCafeteria().reabastecer(barra, 10);
	}

	private Socio crearSocio(String nombre, String login) {
		Socio socio = new Socio(nombre, "doc-" + login, login, "123", LocalDate.of(1995, 1, 1));
		club.registrarSocio(socio);
		return socio;
	}

	private Map<Articulo, Integer> carrito(Object... lineas) {
		Map<Articulo, Integer> carrito = new LinkedHashMap<Articulo, Integer>();
		for (int i = 0; i < lineas.length; i = i + 2) {
			carrito.put((Articulo) lineas[i], (Integer) lineas[i + 1]);
		}
		return carrito;
	}

	private int existenciasTienda(Articulo articulo) {
		return club.getTienda().getUbicacionTienda().obtenerCantidad(articulo);
	}

	// ============ Bono: clase ============

	@Test
	public void testBonoPorValorGuardaElValorYSeUsaUnaSolaVez() {
		Bono bono = Bono.porValor("B1", 15000);

		assertTrue(bono.esPorValor());
		assertEquals(15000, bono.getValorFijo());
		assertEquals("$15000.0", bono.describir());
		assertFalse(bono.isUsado());
		assertTrue(bono.usar());
		assertFalse(bono.usar());
		assertTrue(bono.isUsado());
	}

	// ============ HU-S22 · Usar un bono en una compra ============

	@Test
	public void testS22_unBonoDeValorSeDescuentaDelSubtotalDeUnaCompraEnLaTienda() {
		Bono bono = Bono.porValor("B1", 15000);
		ana.agregarBono(bono);

		VentaTienda venta = compras.comprarEnTienda(ana, carrito(raqueta, 1), null, 0, bono);

		// Subtotal 200000 - bono 15000 = 185000; IVA 19 % = 35150.
		assertEquals(185000, venta.getBaseGravable(), 0.01);
		assertEquals(220150, venta.getValorTotal(), 0.01);
		assertTrue(bono.isUsado());
		assertSame(bono, venta.getBonoAplicado());
		assertEquals(4, existenciasTienda(raqueta));
		assertEquals(4403, ana.getPuntosDeFidelidad());
	}

	@Test
	public void testS22_unBonoSePuedeUsarEnLaCafeteria() {
		Bono bono = Bono.porValor("B1", 2000);
		ana.agregarBono(bono);

		VentaCafeteria venta = compras.comprarEnCafeteria(ana, carrito(cafe, 1), null, 0, bono, ComprasSocio.PROPINA_SUGERIDA);

		// Subtotal 5000 - bono 2000 = 3000; consumo 8 % = 240; propina 10 % = 300.
		assertEquals(3000, venta.getBaseGravable(), 0.01);
		assertEquals(3540, venta.getValorTotal(), 0.01);
		assertTrue(bono.isUsado());
		assertEquals(9, compras.consultarExistenciasCafeteria(cafe));
	}

	@Test
	public void testS22_unBonoUsadoNoSePuedeUsarDeNuevo() {
		Bono bono = Bono.porValor("B1", 15000);
		ana.agregarBono(bono);
		compras.comprarEnTienda(ana, carrito(raqueta, 1), null, 0, bono);

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
				() -> compras.comprarEnTienda(ana, carrito(balon, 1), null, 0, bono));

		assertTrue(e.getMessage().contains("ya fue usado"));
		assertEquals(3, existenciasTienda(balon));
	}

	@Test
	public void testS22_elBonoNoSeAcumulaConPuntos() {
		Bono bono = Bono.porValor("B1", 15000);
		ana.agregarBono(bono);
		ana.setPuntosDeFidelidad(5000);

		IllegalStateException e = assertThrows(IllegalStateException.class,
				() -> compras.comprarEnTienda(ana, carrito(balon, 1), null, 1000, bono));

		assertTrue(e.getMessage().contains("no se acumula"));
		assertFalse(bono.isUsado());
		assertEquals(5000, ana.getPuntosDeFidelidad());
		assertEquals(3, existenciasTienda(balon));
	}

	@Test
	public void testS22_elBonoNoSeAcumulaConUnCodigoDeEmpleado() {
		club.registrarEmpleado(new Entrenador("Carlos", "ent1", "123"));
		Bono bono = Bono.porValor("B1", 15000);
		ana.agregarBono(bono);

		IllegalStateException e = assertThrows(IllegalStateException.class,
				() -> compras.comprarEnTienda(ana, carrito(balon, 1), "COD-ENT1", 0, bono));

		assertTrue(e.getMessage().contains("no se acumula"));
		assertFalse(bono.isUsado());
		assertEquals(3, existenciasTienda(balon));
	}

	@Test
	public void testS22_unaCompraRechazadaPorFaltaDeExistenciasNoGastaElBono() {
		Bono bono = Bono.porValor("B1", 15000);
		ana.agregarBono(bono);

		IllegalStateException e = assertThrows(IllegalStateException.class,
				() -> compras.comprarEnTienda(ana, carrito(raqueta, 99), null, 0, bono));

		assertTrue(e.getMessage().contains("Raqueta"));
		assertFalse(bono.isUsado());
		assertEquals(5, existenciasTienda(raqueta));
		assertEquals(0, ana.getPuntosDeFidelidad());
		assertTrue(club.getTienda().getVentas().isEmpty());
	}

	// ============ Compras: tienda ============

	@Test
	public void testCompraEnLaTiendaDescuentaExistenciasYRegistraLaVenta() {
		VentaTienda venta = compras.comprarEnTienda(ana, carrito(raqueta, 2, balon, 1), null, 0, null);

		// Subtotal 480000 + IVA 19 % = 571200.
		assertEquals(571200, venta.getValorTotal(), 0.01);
		assertEquals(3, existenciasTienda(raqueta));
		assertEquals(2, existenciasTienda(balon));
		assertEquals(1, club.getTienda().getVentas().size());
		assertEquals(11424, ana.getPuntosDeFidelidad());
	}

	@Test
	public void testCompraEnLaTiendaConPuntosRedimeYAcumula() {
		ana.setPuntosDeFidelidad(1000);

		VentaTienda venta = compras.comprarEnTienda(ana, carrito(balon, 1), null, 1000, null);

		// 80000 + 15200 de IVA - 1000 puntos = 94200; puntos ganados 1884.
		assertEquals(94200, venta.getValorTotal(), 0.01);
		assertEquals(1884, ana.getPuntosDeFidelidad());
	}

	@Test
	public void testCompraConElCodigoDeUnEmpleadoTieneOchoPorCiento() {
		club.registrarEmpleado(new Entrenador("Carlos", "ent1", "123"));

		VentaTienda venta = compras.comprarEnTienda(ana, carrito(balon, 1), "COD-ENT1", 0, null);

		// 80000 - 8 % = 73600; IVA 19 % = 13984.
		assertEquals(87584, venta.getValorTotal(), 0.01);
	}

	// ============ Compras: cafeteria ============

	@Test
	public void testCompraEnLaCafeteriaConLaPropinaSugerida() {
		VentaCafeteria venta = compras.comprarEnCafeteria(ana, carrito(jugo, 2), null, 0, null, ComprasSocio.PROPINA_SUGERIDA);

		// Subtotal 8000 + consumo 8 % (640) + propina 10 % (800) = 9440.
		assertEquals(9440, venta.getValorTotal(), 0.01);
		assertEquals(8, compras.consultarExistenciasCafeteria(jugo));
		assertEquals(172, ana.getPuntosDeFidelidad());
	}

	// ============ Catalogos y subtotal ============

}
