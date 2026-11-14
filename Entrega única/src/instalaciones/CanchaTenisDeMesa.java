package instalaciones;

import enumeraciones.Modalidad;

public class CanchaTenisDeMesa extends CanchaDeporteIndividual {
	
	private static final long serialVersionUID = 1L;
	
	private boolean techada;

	public CanchaTenisDeMesa(String nombre, boolean techada) {
		super(nombre, 4, "TENIS_DE_MESA");
		this.techada = techada;
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

	@Override
	public boolean permiteBebidaCaliente() {
		return !techada;
	}

	public boolean isTechada() {
		return techada;
	}

	public void setTechada(boolean techada) {
		this.techada = techada;
	}
}