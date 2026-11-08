package estadisticas;

import java.io.Serializable;

public abstract class EstadisticasPartidoIndividual implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	protected boolean ganador;

	public boolean isGanador() { return ganador; }
	public void setGanador(boolean ganador) { this.ganador = ganador; }

	public abstract String getResumen();
	public abstract boolean esGanador();
}