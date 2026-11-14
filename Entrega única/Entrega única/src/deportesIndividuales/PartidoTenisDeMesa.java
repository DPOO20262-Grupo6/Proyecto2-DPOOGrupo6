package deportesIndividuales;

import java.time.LocalDate;
import java.time.LocalTime;
import enumeraciones.Modalidad;
import estadisticas.EstadisticasPartidoIndividual;
import estadisticas.EstadisticasPartidoTenisDeMesa;
import instalaciones.CanchaDeporteIndividual;

public class PartidoTenisDeMesa extends PartidoDeporteIndividual {
	
	private static final long serialVersionUID = 1L;

	public PartidoTenisDeMesa(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, Modalidad modalidad, CanchaDeporteIndividual cancha, boolean intervieneEntrenador) {
		super(fecha, horaInicio, horaFin, modalidad, cancha, intervieneEntrenador);
	}

	@Override
	public EstadisticasPartidoIndividual crearEstadisticas() {
		return new EstadisticasPartidoTenisDeMesa();
	}
}