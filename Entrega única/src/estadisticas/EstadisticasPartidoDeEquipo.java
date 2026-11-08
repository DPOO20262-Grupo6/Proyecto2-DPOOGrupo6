package estadisticas;

import java.io.Serializable;

public abstract class EstadisticasPartidoDeEquipo implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public abstract String getResumen();
}