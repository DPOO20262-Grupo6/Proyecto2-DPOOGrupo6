package deportesDeConjunto;

public class EquipoBaloncesto extends Equipo {
	
	private static final long serialVersionUID = 1L;
	
	private static final int MINIMO_JUGADORES_HABILITADOS = 5;
	private static final String DEPORTE = "BALONCESTO";

	public EquipoBaloncesto(String nombre, String categoria, int cupoMaximoJugadores) {
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