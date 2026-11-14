package articulosYVentas;

import java.util.ArrayList;
import java.util.List;

public class Snack extends Articulo {
	
	private static final long serialVersionUID = 1L;
	
	private List<String> alergenos;

	public Snack(String nombre, double precio, List<String> alergenos) {
		super(nombre, precio);
		this.alergenos = new ArrayList<String>();
		if (alergenos != null) {
			for (int i = 0; i < alergenos.size(); i++) {
				this.alergenos.add(alergenos.get(i));
			}
		}
	}

	public List<String> getAlergenos() {
		return alergenos;
	}

	public void setAlergenos(List<String> alergenos) {
		this.alergenos = alergenos;
	}
}