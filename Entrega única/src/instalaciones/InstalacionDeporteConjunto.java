package instalaciones;

public class InstalacionDeporteConjunto extends InstalacionDeportiva {

	private static final long serialVersionUID = 1L;

	private String deporte;

	public InstalacionDeporteConjunto(String nombre) {
		super(nombre);
	}

	public InstalacionDeporteConjunto(String nombre, String deporte) {
		super(nombre);
		this.deporte = deporte;
	}

	public String getDeporte() {
		return deporte;
	}

	public void setDeporte(String deporte) {
		this.deporte = deporte;
	}
}
