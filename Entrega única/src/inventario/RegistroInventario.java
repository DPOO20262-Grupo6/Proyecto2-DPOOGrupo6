package inventario;

import java.io.Serializable;
import articulosYVentas.Articulo;

public class RegistroInventario implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private int cantidad;
	private Articulo articulo;

	public RegistroInventario(Articulo articulo, int cantidad) {
		this.articulo = articulo;
		this.cantidad = cantidad;
	}

	public void aumentar(int cantidad) {
		if (cantidad < 0) {
			throw new IllegalArgumentException("La cantidad no puede ser negativa.");
		}
		this.cantidad = this.cantidad + cantidad;
	}

	public void disminuir(int cantidad) {
		if (cantidad < 0 || cantidad > this.cantidad) {
			throw new IllegalStateException("No hay suficientes existencias de " + articulo.getNombre());
		}
		this.cantidad = this.cantidad - cantidad;
	}

	public int getCantidad() {
		return cantidad;
	}

	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}

	public Articulo getArticulo() {
		return articulo;
	}

	public void setArticulo(Articulo articulo) {
		this.articulo = articulo;
	}
}
