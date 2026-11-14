package articulosYVentas;

import java.io.Serializable;

public abstract class Articulo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String nombre;
	private double precio;

	public Articulo(String nombre, double precio) {
		this.nombre = nombre;
		this.precio = precio;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public double getPrecio() {
		return precio;
	}

	public void setPrecio(double precio) {
		this.precio = precio;
	}
}