package deportesDeConjunto;

public class EquipoVoleibol extends Equipo {
	
	private static final long serialVersionUID = 1L;
	
	private static final int MINIMO_JUGADORES_HABILITADOS = 6;
	private static final String DEPORTE = "VOLEIBOL";

	public EquipoVoleibol(String nombre, String categoria, int cupoMaximoJugadores) {
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