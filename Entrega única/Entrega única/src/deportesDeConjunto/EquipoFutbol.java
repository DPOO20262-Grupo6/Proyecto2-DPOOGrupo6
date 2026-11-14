package deportesDeConjunto;

public class EquipoFutbol extends Equipo {
	
	private static final long serialVersionUID = 1L;
	
	private static final int MINIMO_JUGADORES_HABILITADOS = 7;
	private static final String DEPORTE = "FUTBOL";

	public EquipoFutbol(String nombre, String categoria, int cupoMaximoJugadores) {
		super(nombre, categoria, cupoMaximoJugadores);
	}

	@Override
	public int getMinimoJugadoresHabilitados() {
		return MINIMO_JUGADORES_HABILITADOS;
	}

	@Override
	public String getDeporte() {
		return DEPORTE;
	}
}