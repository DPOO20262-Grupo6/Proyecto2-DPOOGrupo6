package articulosYVentas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import club.ClubDeportivoSeneca;
import usuarios.Socio;

/**
 * Compras de un socio en la tienda y en la cafeteria (HU-S13 a HU-S17). Arma la venta con el
 * carrito, aplica los puntos, el codigo de descuento o el bono y la procesa en el club.
 * Si la compra es rechazada no cambia nada: ni las existencias, ni los puntos, ni el bono.
 */
public class ComprasSocio {

	/** Propina sugerida en la cafeteria: 10 % del valor de la compra. */
	public static final double PROPINA_SUGERIDA = 0.10;

	private ClubDeportivoSeneca club;

	public ComprasSocio(ClubDeportivoSeneca club) {
		if (club == null) {
			throw new IllegalArgumentException("Las compras necesitan un club.");
		}
		this.club = club;
	}

	// ---------------- Catalogos ----------------

	public List<ArticuloDeportivo> consultarCatalogoTienda() {
		return new ArrayList<ArticuloDeportivo>(club.getTienda().getCatalogo());
	}

	public int consultarExistenciasTienda(Articulo articulo) {
		return club.getTienda().getUbicacionTienda().obtenerCantidad(articulo);
	}

	/** Bebidas y snacks de la cafeteria. */
	public List<Articulo> consultarProductosCafeteria() {
		List<Articulo> productos = new ArrayList<Articulo>();
		productos.addAll(club.getCafeteria().getBebidas());
		productos.addAll(club.getCafeteria().getSnacks());
		return productos;
	}

	public int consultarExistenciasCafeteria(Articulo producto) {
		return club.getCafeteria().getInventario().obtenerCantidad(producto);
	}

	/** Texto de un producto de la cafeteria, con los alergenos de los snacks (HU-S14). */
	public String describirProducto(Articulo producto) {
		String texto = producto.getNombre() + " | $" + producto.getPrecio();
		if (producto instanceof Bebida) {
			texto = texto + (((Bebida) producto).esCaliente() ? " | bebida caliente" : " | bebida fria");
		}
		if (producto instanceof Snack) {
			texto = texto + " | alergenos: " + ((Snack) producto).getAlergenos();
		}
		return texto;
	}

	/** Alergenos de los snacks del carrito, para mostrarlos antes de confirmar la compra. Vacio si no hay snacks. */
	public String describirAlergenos(Map<Articulo, Integer> carrito) {
		StringBuilder texto = new StringBuilder();
		for (Articulo articulo : carrito.keySet()) {
			if (articulo instanceof Snack) {
				List<String> alergenos = ((Snack) articulo).getAlergenos();
				texto.append(articulo.getNombre()).append(": ")
					.append(alergenos == null || alergenos.isEmpty() ? "sin alergenos registrados" : alergenos.toString()).append("\n");
			}
		}
		return texto.toString();
	}

	/** Valor de la compra antes de descuentos e impuestos. */
	public double calcularSubtotal(Map<Articulo, Integer> carrito) {
		double subtotal = 0;
		for (Map.Entry<Articulo, Integer> linea : carrito.entrySet()) {
			subtotal = subtotal + linea.getKey().getPrecio() * linea.getValue();
		}
		return subtotal;
	}

	// ---------------- Compras ----------------

	/**
	 * Compra en la tienda (IVA 19 %). Se puede usar un codigo de descuento de empleado, redimir
	 * puntos (1 peso por punto) o usar un bono; estos descuentos no se acumulan.
	 * @param codigoDescuento codigo de un empleado, o null
	 * @param puntos puntos a redimir, 0 si no se redimen
	 * @param bono bono del socio a usar, o null
	 */
	public VentaTienda comprarEnTienda(Socio socio, Map<Articulo, Integer> carrito, String codigoDescuento, int puntos, Bono bono) {
		validarCompra(socio, carrito, codigoDescuento, puntos, bono);
		VentaTienda venta = new VentaTienda(LocalDate.now(), socio);
		agregarDetalles(venta, carrito);
		aplicarDescuentos(venta, puntos, bono);
		club.getTienda().procesarVenta(venta, codigoDescuento);
		return venta;
	}

	/**
	 * Compra en la cafeteria (impuesto al consumo 8 %, propina sugerida 10 %). La propina es una
	 * fraccion del valor (0.10 es el 10 %) y puede ser 0.
	 */
	public VentaCafeteria comprarEnCafeteria(Socio socio, Map<Articulo, Integer> carrito, String codigoDescuento, int puntos, Bono bono, double propina) {
		validarCompra(socio, carrito, codigoDescuento, puntos, bono);
		VentaCafeteria venta = new VentaCafeteria(LocalDate.now(), socio);
		venta.setPorcentajePropina(propina);
		agregarDetalles(venta, carrito);
		aplicarDescuentos(venta, puntos, bono);
		club.procesarVentaCafeteria(venta, codigoDescuento);
		return venta;
	}

	private void validarCompra(Socio socio, Map<Articulo, Integer> carrito, String codigoDescuento, int puntos, Bono bono) {
		if (socio == null) {
			throw new IllegalArgumentException("Debe indicar el socio que compra.");
		}
		if (!socio.isActivo()) {
			throw new IllegalStateException("El socio " + socio.getLogin() + " no esta activo.");
		}
		if (carrito == null || carrito.isEmpty()) {
			throw new IllegalArgumentException("La compra no tiene articulos.");
		}
		if (puntos < 0) {
			throw new IllegalArgumentException("Los puntos a redimir no pueden ser negativos.");
		}
		boolean hayCodigo = codigoDescuento != null && !codigoDescuento.trim().isEmpty();
		if (bono != null) {
			if (!socio.getBonos().contains(bono)) {
				throw new IllegalArgumentException("El bono no pertenece al socio.");
			}
			if (bono.isUsado()) {
				throw new IllegalArgumentException("El bono ya fue usado.");
			}
			if (puntos > 0 || hayCodigo) {
				throw new IllegalStateException("El bono no se acumula con otros descuentos: no puede usarlo junto con puntos o con un codigo de descuento.");
			}
		}
		if (puntos > 0 && hayCodigo) {
			throw new IllegalStateException("Los descuentos no se acumulan: use los puntos o el codigo de descuento, no ambos.");
		}
	}

	private void agregarDetalles(Venta venta, Map<Articulo, Integer> carrito) {
		for (Map.Entry<Articulo, Integer> linea : carrito.entrySet()) {
			venta.agregarDetalle(linea.getKey(), linea.getValue());
		}
	}

	private void aplicarDescuentos(Venta venta, int puntos, Bono bono) {
		if (puntos > 0) {
			venta.redimirPuntos(puntos);
		}
		if (bono != null) {
			venta.aplicarBono(bono);
		}
	}
}
