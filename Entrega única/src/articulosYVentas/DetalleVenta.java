package articulosYVentas;

import java.io.Serializable;

public class DetalleVenta implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Articulo articulo;
	private int cantidadArticulo;
	private double precioUnitario;

	public DetalleVenta(Articulo articulo, int cantidadArticulo) {
		this.articulo = articulo;
		this.cantidadArticulo = cantidadArticulo;
		this.precioUnitario = articulo.getPrecio();
	}

	public double calcularSubtotal() {
		return cantidadArticulo * precioUnitario;
	}

	public Articulo getArticulo() {
		return articulo;
	}

	public void setArticulo(Articulo articulo) {
		this.articulo = articulo;
	}

	public int getCantidadArticulo() {
		return cantidadArticulo;
	}

	public void setCantidadArticulo(int cantidadArticulo) {
		this.cantidadArticulo = cantidadArticulo;
	}

	public double getPrecioUnitario() {
		return precioUnitario;
	}

	public void setPrecioUnitario(double precioUnitario) {
		this.precioUnitario = precioUnitario;
	}
}
