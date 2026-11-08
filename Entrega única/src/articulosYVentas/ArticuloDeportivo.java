package articulosYVentas;

public class ArticuloDeportivo extends Articulo {
	
	private static final long serialVersionUID = 1L;
	
	private String categoria;

	public ArticuloDeportivo(String nombre, double precio, String categoria) {
		super(nombre, precio);
		this.categoria = categoria;
	}

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}
}