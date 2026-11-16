package articulosYVentas;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import instalaciones.CanchaDeporteIndividual;
import instalaciones.InstalacionDeportiva;
import inventario.UbicacionInventario;

public class Cafeteria implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<Bebida> bebidas;
	private List<Snack> snacks;
	private List<VentaCafeteria> ventas;
	private UbicacionInventario inventario;

	public Cafeteria() {
		this.bebidas = new ArrayList<Bebida>();
		this.snacks = new ArrayList<Snack>();
		this.ventas = new ArrayList<VentaCafeteria>();
	}

	public void agregarProducto(Articulo producto) {
		if (producto instanceof Bebida) {
			bebidas.add((Bebida) producto);
		} else if (producto instanceof Snack) {
			snacks.add((Snack) producto);
		}
	}

	public String informarAlergenos(List<Articulo> productos) {
		String info = "Alergenos: ";
		for (int i = 0; i < productos.size(); i++) {
			Articulo a = productos.get(i);
			if (a instanceof Snack) {
				Snack s = (Snack) a;
				info = info + s.getNombre() + " " + s.getAlergenos().toString() + " ";
			}
		}
		return info;
	}

	public UbicacionInventario getInventario() {
		if (inventario == null) {
			inventario = new UbicacionInventario("Cafeteria", "CAFETERIA");
		}
		return inventario;
	}

	public void reabastecer(Articulo producto, int cantidad) {
		if (cantidad <= 0) {
			throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
		}
		getInventario().agregarExistencias(producto, cantidad);
	}

	public void procesarVenta(VentaCafeteria venta, InstalacionDeportiva instalacionDelComprador) {
		String prohibicion = null;
		if (instalacionDelComprador instanceof CanchaDeporteIndividual && !((CanchaDeporteIndividual) instalacionDelComprador).permiteBebidaCaliente()) {
			prohibicion = "No se permite despachar bebidas calientes en " + instalacionDelComprador.getNombre();
		}
		procesarVenta(venta, prohibicion, 0);
	}

	public void procesarVenta(VentaCafeteria venta, String prohibicionBebidaCaliente, double descuento) {
		for (int i = 0; i < venta.getDetalles().size(); i++) {
			DetalleVenta detalle = venta.getDetalles().get(i);
			Articulo producto = detalle.getArticulo();
			if (!bebidas.contains(producto) && !snacks.contains(producto)) {
				throw new IllegalArgumentException("El producto " + producto.getNombre() + " no existe en la cafeteria.");
			}
			if (getInventario().obtenerCantidad(producto) < detalle.getCantidadArticulo()) {
				throw new IllegalStateException("No hay existencia suficiente de " + producto.getNombre() + ".");
			}
			if (producto instanceof Snack && (((Snack) producto).getAlergenos() == null || ((Snack) producto).getAlergenos().isEmpty())) {
				throw new IllegalStateException("El snack " + producto.getNombre() + " no tiene alergenos registrados.");
			}
			if (producto instanceof Bebida && ((Bebida) producto).esCaliente() && prohibicionBebidaCaliente != null) {
				throw new IllegalStateException(prohibicionBebidaCaliente);
			}
		}
		venta.aplicarDescuentoValidandoRedencion(descuento);
		for (int i = 0; i < venta.getDetalles().size(); i++) {
			DetalleVenta detalle = venta.getDetalles().get(i);
			getInventario().retirarExistencias(detalle.getArticulo(), detalle.getCantidadArticulo());
		}
		venta.registrarPuntos();
		ventas.add(venta);
	}

	public List<VentaCafeteria> getVentas() { return ventas; }
	public List<Snack> getSnacks() { return snacks; }
	public List<Bebida> getBebidas() { return bebidas; }
}
