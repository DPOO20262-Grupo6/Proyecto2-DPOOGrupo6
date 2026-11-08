package deportesDeConjunto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import estadisticas.EstadisticasPartidoBaloncesto;
import estadisticas.EstadisticasPartidoDeEquipo;
import estadisticas.EstadisticasPartidoFutbol;
import estadisticas.EstadisticasPartidoVoleibol;
import inscripciones.InscripcionEquipo;

public abstract class PartidoDeEquipo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private LocalDate fecha;
	private String deporte;
	protected ResultadoPartidoEquipo resultado;
	protected List<InscripcionEquipo> participantes;
	protected List<EstadisticasPartidoDeEquipo> estadisticas;

	public PartidoDeEquipo(LocalDate fecha, String deporte) {
		this.fecha = fecha;
		this.deporte = deporte;
		this.participantes = new ArrayList<InscripcionEquipo>();
		this.estadisticas = new ArrayList<EstadisticasPartidoDeEquipo>();
	}

	public void registrarParticipacion(InscripcionEquipo inscripcion) {
		if (inscripcion == null || !inscripcion.estaHabilitado()) {
			throw new IllegalStateException("El jugador no esta habilitado para jugar.");
		}
		if (!participantes.contains(inscripcion)) {
			participantes.add(inscripcion);
		}
	}

	public void registrarEstadistica(InscripcionEquipo inscripcion, EstadisticasPartidoDeEquipo est) {
		if (!participantes.contains(inscripcion)) {
			throw new IllegalArgumentException("El jugador no participo en el partido.");
		}
		if (deporte.equalsIgnoreCase("FUTBOL") && !(est instanceof EstadisticasPartidoFutbol)) {
			throw new IllegalArgumentException("Estadisticas no validas para futbol.");
		}
		if (deporte.equalsIgnoreCase("BALONCESTO") && !(est instanceof EstadisticasPartidoBaloncesto)) {
			throw new IllegalArgumentException("Estadisticas no validas para baloncesto.");
		}
		if (deporte.equalsIgnoreCase("VOLEIBOL") && !(est instanceof EstadisticasPartidoVoleibol)) {
			throw new IllegalArgumentException("Estadisticas no validas para voleibol.");
		}
		estadisticas.add(est);
		procesarTarjetasYSuspensiones(inscripcion, est);
	}

	protected abstract void procesarTarjetasYSuspensiones(InscripcionEquipo inscripcion, EstadisticasPartidoDeEquipo est);

	public void registrarResultado(ResultadoPartidoEquipo resultado) {
		if (!validarCuorum()) {
			throw new IllegalStateException("No se cumple el minimo de jugadores.");
		}
		if (!deporte.equalsIgnoreCase("FUTBOL") && resultado.esEmpate()) {
			throw new IllegalArgumentException("Este deporte no permite empates.");
		}
		this.resultado = resultado;
	}

	public abstract boolean validarCuorum();

	public abstract Equipo getGanador();

	public LocalDate getFecha() { return fecha; }
	public void setFecha(LocalDate fecha) { this.fecha = fecha; }
	public String getDeporte() { return deporte; }
	public void setDeporte(String deporte) { this.deporte = deporte; }
	public ResultadoPartidoEquipo getResultado() { return resultado; }
	public List<InscripcionEquipo> getParticipantes() { return participantes; }
	public List<EstadisticasPartidoDeEquipo> getEstadisticas() { return estadisticas; }
}