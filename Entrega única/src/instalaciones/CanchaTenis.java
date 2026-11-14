package instalaciones;

import enumeraciones.Modalidad;

public class CanchaTenis extends CanchaDeporteIndividual {
	
	private static final long serialVersionUID = 1L;

	public CanchaTenis(String nombre) {
		super(nombre, 4, "TENIS");
	}

	@Override
	public boolean admiteCantidadJugadores(int cantidad, Modalidad modalidad) {
		if (modalidad == Modalidad.SENCILLOS && cantidad == 2) {
			return true;
		}
		if (modalidad == Modalidad.DOBLES && cantidad == 4) {
			return true;
		}
		return false;
	}
}