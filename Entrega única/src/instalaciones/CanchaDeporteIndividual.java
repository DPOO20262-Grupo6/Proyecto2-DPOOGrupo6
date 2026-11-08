package instalaciones;

import enumeraciones.Modalidad;

public abstract class CanchaDeporteIndividual extends InstalacionDeportiva {
	
	private static final long serialVersionUID = 1L;
	
	private int capacidad;
	private String disciplina;

	public CanchaDeporteIndividual(String nombre, int capacidad, String disciplina) {
		super(nombre);
		this.capacidad = capacidad;
		this.disciplina = disciplina;
	}

	public boolean admiteCantidadJugadores(int cantidad, Modalidad modalidad) {
		if (modalidad == Modalidad.SENCILLOS) {
			return cantidad == 2;
		}
		if (modalidad == Modalidad.DOBLES) {
			return cantidad == 4 && capacidad >= 4;
		}
		return false;
	}

	public boolean permiteBebidaCaliente() {
		return true;
	}

	public int getCapacidad() {
		return capacidad;
	}

	public void setCapacidad(int capacidad) {
		this.capacidad = capacidad;
	}

	public String getDisciplina() {
		return disciplina;
	}

	public void setDisciplina(String disciplina) {
		this.disciplina = disciplina;
	}
}