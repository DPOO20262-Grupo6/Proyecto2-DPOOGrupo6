package administracion;

import articulosYVentas.Articulo;
import articulosYVentas.ArticuloDeportivo;

public class SugerenciaArticulo extends Sugerencia {
	
	private static final long serialVersionUID = 1L;
	
	private String categoriaPropuesta;

	public SugerenciaArticulo(String nombrePropuesto, double precioPropuesto, String categoriaPropuesta) {
		super(nombrePropuesto, precioPropuesto);
		this.categoriaPropuesta = categoriaPropuesta;
	}

	@Override
	public Articulo convertirEnArticulo() {
		return new ArticuloDeportivo(getNombrePropuesto(), getPrecioPropuesto(), categoriaPropuesta);
	}

	public String getCategoriaPropuesta() {
		return categoriaPropuesta;
	}

	public void setCategoriaPropuesta(String categoriaPropuesta) {
		this.categoriaPropuesta = categoriaPropuesta;
	}
}