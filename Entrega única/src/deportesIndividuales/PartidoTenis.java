package deportesIndividuales;

import java.time.LocalDate;
import java.time.LocalTime;
import enumeraciones.Modalidad;
import estadisticas.EstadisticasPartidoIndividual;
import estadisticas.EstadisticasPartidoTenis;
import instalaciones.CanchaDeporteIndividual;

public class PartidoTenis extends PartidoDeporteIndividual {
	
	private static final long serialVersionUID = 1L;

	public PartidoTenis(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, Modalidad modalidad, CanchaDeporteIndividual cancha, boolean intervieneEntrenador) {
		super(fecha, horaInicio, horaFin, modalidad, cancha, intervieneEntrenador);
	}

	@Override
	public EstadisticasPartidoIndividual crearEstadisticas() {
		return new EstadisticasPartidoTenis();
	}
}