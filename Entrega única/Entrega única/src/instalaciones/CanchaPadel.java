package instalaciones;

import enumeraciones.Modalidad;

public class CanchaPadel extends CanchaDeporteIndividual {
	
	private static final long serialVersionUID = 1L;
	
	public static final int CAPACIDAD = 4;
	
	private boolean techada;

	public CanchaPadel(String nombre, boolean techada) {
		super(nombre, CAPACIDAD, "PADEL");
		this.techada = techada;
	}

	@Override
	public boolean admiteCantidadJugadores(int cantidad, Modalidad modalidad) {
		return modalidad == Modalidad.DOBLES && cantidad == CAPACIDAD;
	}

	@Override
	public boolean permiteBebidaCaliente() {
		return false;
	}

	public boolean isTechada() {
		return techada;
	}

	public void setTechada(boolean techada) {
		this.techada = techada;
	}
}