package presentacion;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import articulosYVentas.Articulo;
import articulosYVentas.Bono;
import articulosYVentas.ComprasSocio;
import articulosYVentas.Venta;
import articulosYVentas.VentaCafeteria;
import articulosYVentas.VentaTienda;
import club.ClubDeportivoSeneca;
import usuarios.Socio;

/**
 * Compras del socio en la tienda y la cafeteria, con puntos de fidelidad, codigo de descuento de un
 * empleado y bono (HU-S13 a HU-S17). La consola arma el carrito y pregunta por los descuentos;
 * la venta la valida y la procesa ComprasSocio.
 */
public class AccionesComprasSocio extends AccionesSocioBase {

	private final ComprasSocio compras;

	public AccionesComprasSocio(ClubDeportivoSeneca club, Socio socio, LectorEntradas lector) {
		super(club, socio, lector);
		this.compras = new ComprasSocio(club);
	}

	public Menu construirMenu() {
		Menu menu = new Menu("Compras y puntos", "Volver");
		menu.agregarOpcion("Comprar en la tienda", this::comprarEnTienda);
		menu.agregarOpcion("Comprar en la cafeteria", this::comprarEnCafeteria);
		menu.agregarOpcion("Consultar mis puntos y mis bonos", this::consultarPuntosYBonos);
		return menu;
	}

	private void consultarPuntosYBonos() {
		lector.mostrar("--- Mis puntos y mis bonos ---");
		lector.mostrar("Puntos de fidelidad: " + socio.getPuntosDeFidelidad() + " (1 punto = $1 de descuento)");
		List<Bono> disponibles = disponibles();
		if (disponibles.isEmpty()) {
			lector.mostrar("No tiene bonos disponibles.");
		} else {
			listar(disponibles, b -> b.getCodigo() + " | descuento " + b.describir());
		}
	}

	// ---------------- Tienda ----------------

	private void comprarEnTienda() {
		lector.mostrar("--- Comprar en la tienda ---");
		List<? extends Articulo> catalogo = compras.consultarCatalogoTienda();
		if (catalogo.isEmpty()) {
			lector.mostrar("La tienda no tiene articulos.");
			return;
		}
		Map<Articulo, Integer> carrito = armarCarrito(catalogo, true);
		if (carrito.isEmpty()) {
			lector.mostrar("No agrego articulos. Compra cancelada.");
			return;
		}
		lector.mostrar("Subtotal: $" + compras.calcularSubtotal(carrito) + " (mas IVA 19 %)");
		Descuento descuento = pedirDescuento();
		if (!lector.leerConfirmacion("Confirma la compra?")) {
			lector.mostrar("Compra no realizada.");
			return;
		}
		VentaTienda venta = compras.comprarEnTienda(socio, carrito, descuento.codigo, descuento.puntos, descuento.bono);
		mostrarResultado(venta);
	}

	// ---------------- Cafeteria ----------------

	private void comprarEnCafeteria() {
		lector.mostrar("--- Comprar en la cafeteria ---");
		List<Articulo> productos = compras.consultarProductosCafeteria();
		if (productos.isEmpty()) {
			lector.mostrar("La cafeteria no tiene productos.");
			return;
		}
		Map<Articulo, Integer> carrito = armarCarrito(productos, false);
		if (carrito.isEmpty()) {
			lector.mostrar("No agrego productos. Compra cancelada.");
			return;
		}
		String alergenos = compras.describirAlergenos(carrito);
		if (!alergenos.isEmpty()) {
			lector.mostrar("Alergenos de los snacks de su compra:");
			lector.mostrar(alergenos.trim());
		}
		lector.mostrar("Subtotal: $" + compras.calcularSubtotal(carrito) + " (mas impuesto al consumo 8 % y propina)");
		Descuento descuento = pedirDescuento();
		double propina = lector.leerDecimal("Propina en porcentaje (sugerida " + (int) (ComprasSocio.PROPINA_SUGERIDA * 100) + ", puede ser 0): ", 0) / 100.0;
		if (!lector.leerConfirmacion("Confirma la compra?")) {
			lector.mostrar("Compra no realizada.");
			return;
		}
		VentaCafeteria venta = compras.comprarEnCafeteria(socio, carrito, descuento.codigo, descuento.puntos, descuento.bono, propina);
		mostrarResultado(venta);
	}

	// ---------------- Apoyo ----------------

	/** Pide productos y cantidades hasta que el usuario escribe 0. */
	private Map<Articulo, Integer> armarCarrito(List<? extends Articulo> productos, boolean esTienda) {
		Map<Articulo, Integer> carrito = new LinkedHashMap<Articulo, Integer>();
		while (true) {
			for (int i = 0; i < productos.size(); i++) {
				Articulo producto = productos.get(i);
				int existencias = esTienda ? compras.consultarExistenciasTienda(producto) : compras.consultarExistenciasCafeteria(producto);
				String detalle = esTienda ? producto.getNombre() + " | $" + producto.getPrecio() : compras.describirProducto(producto);
				lector.mostrar((i + 1) + ". " + detalle + " | existencias: " + existencias);
			}
			int eleccion = lector.leerEntero("Producto (numero, 0 para terminar): ", 0, productos.size());
			if (eleccion == 0) {
				return carrito;
			}
			Articulo elegido = productos.get(eleccion - 1);
			int cantidad = lector.leerEntero("Cantidad: ", 1);
			Integer anterior = carrito.get(elegido);
			carrito.put(elegido, (anterior == null ? 0 : anterior) + cantidad);
			lector.mostrar("Agregado. Productos en la compra: " + carrito.size());
		}
	}

	/** Un solo descuento por compra: codigo de empleado, bono o puntos. */
	private Descuento pedirDescuento() {
		Descuento descuento = new Descuento();
		descuento.codigo = lector.leerTextoOpcional("Codigo de descuento de un empleado (Enter si no tiene): ");
		if (!descuento.codigo.isEmpty()) {
			return descuento;
		}
		List<Bono> bonos = disponibles();
		if (!bonos.isEmpty() && lector.leerConfirmacion("Tiene " + bonos.size() + " bono(s) disponible(s). Usar uno")) {
			descuento.bono = elegir("Bono (numero): ", bonos, b -> b.getCodigo() + " | descuento " + b.describir());
			return descuento;
		}
		if (socio.getPuntosDeFidelidad() > 0 && lector.leerConfirmacion("Tiene " + socio.getPuntosDeFidelidad() + " puntos. Redimir puntos")) {
			descuento.puntos = lector.leerEntero("Puntos a redimir (1 punto = $1): ", 1, socio.getPuntosDeFidelidad());
		}
		return descuento;
	}

	private List<Bono> disponibles() {
		List<Bono> disponibles = new java.util.ArrayList<Bono>();
		for (int i = 0; i < socio.getBonos().size(); i++) {
			if (!socio.getBonos().get(i).isUsado()) {
				disponibles.add(socio.getBonos().get(i));
			}
		}
		return disponibles;
	}

	private void mostrarResultado(Venta venta) {
		lector.mostrar("Compra registrada el " + venta.getFecha() + ".");
		lector.mostrar(venta.toString());
		if (venta.getPuntosFidelidadUtilizados() > 0) {
			lector.mostrar("Puntos redimidos: " + venta.getPuntosFidelidadUtilizados());
		}
		if (venta.getBonoAplicado() != null) {
			lector.mostrar("Bono usado: " + venta.getBonoAplicado().getCodigo() + " (" + venta.getBonoAplicado().describir() + ")");
		}
		lector.mostrar("Puntos ganados: " + venta.getPuntosFidelidadGenerados() + ". Saldo de puntos: " + socio.getPuntosDeFidelidad());
	}

	/** Descuento elegido para una compra. */
	private static class Descuento {
		String codigo = "";
		int puntos = 0;
		Bono bono = null;
	}
}
