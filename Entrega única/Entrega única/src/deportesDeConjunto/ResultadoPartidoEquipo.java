package deportesDeConjunto;

import java.io.Serializable;

public abstract class ResultadoPartidoEquipo implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public abstract String getGanador();

	public abstract boolean esEmpate();
}