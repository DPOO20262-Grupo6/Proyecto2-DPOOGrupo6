package administracion;

import java.util.List;
import articulosYVentas.Articulo;
import articulosYVentas.Snack;

public class SugerenciaSnack extends Sugerencia {
	
	private static final long serialVersionUID = 1L;
	
	private List<String> alergenosPropuestos;

	public SugerenciaSnack(String nombrePropuesto, double precioPropuesto, List<String> alergenosPropuestos) {
		super(nombrePropuesto, precioPropuesto);
		this.alergenosPropuestos = alergenosPropuestos;
	}

	@Override
	public Articulo convertirEnArticulo() {
		return new Snack(getNombrePropuesto(), getPrecioPropuesto(), alergenosPropuestos);
	}

	public List<String> getAlergenosPropuestos() {
		return alergenosPropuestos;
	}

	public void setAlergenosPropuestos(List<String> alergenosPropuestos) {
		this.alergenosPropuestos = alergenosPropuestos;
	}
}