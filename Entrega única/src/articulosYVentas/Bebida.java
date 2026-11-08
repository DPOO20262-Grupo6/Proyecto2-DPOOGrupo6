package articulosYVentas;

public class Bebida extends Articulo {
	
	private static final long serialVersionUID = 1L;
	
	private boolean caliente;

	public Bebida(String nombre, double precio, boolean caliente) {
		super(nombre, precio);
		this.caliente = caliente;
	}

	public boolean esCaliente() {
		return caliente;
	}

	public void setCaliente(boolean caliente) {
		this.caliente = caliente;
	}
}