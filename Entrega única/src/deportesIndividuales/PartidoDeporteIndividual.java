package deportesIndividuales;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import enumeraciones.Modalidad;
import estadisticas.EstadisticasPartidoIndividual;
import inscripciones.InscripcionDisciplinaIndividual;
import instalaciones.CanchaDeporteIndividual;

public abstract class PartidoDeporteIndividual implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private LocalDate fecha;
	private LocalTime horaInicio;
	private LocalTime horaFin;
	private Modalidad modalidad;
	private CanchaDeporteIndividual cancha;
	private boolean intervieneEntrenador;
	
	protected List<InscripcionDisciplinaIndividual> participantes;
	protected List<EstadisticasPartidoIndividual> estadisticas; 

	public PartidoDeporteIndividual(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, Modalidad modalidad, CanchaDeporteIndividual cancha, boolean intervieneEntrenador) {
		this.fecha = fecha;
		this.horaInicio = horaInicio;
		this.horaFin = horaFin;
		this.modalidad = modalidad;
		this.cancha = cancha;
		this.intervieneEntrenador = intervieneEntrenador;
		this.participantes = new ArrayList<InscripcionDisciplinaIndividual>();
		this.estadisticas = new ArrayList<EstadisticasPartidoIndividual>();
		
		if (!validarModalidad()) {
			throw new IllegalArgumentException("Modalidad no permitida para este deporte.");
		}
	}

	public void agregarParticipante(InscripcionDisciplinaIndividual inscripcion) {
		if (participantes.size() >= getCantidadJugadoresRequerida()) {
			throw new IllegalStateException("Ya estan completos los jugadores del partido.");
		}
		participantes.add(inscripcion);
		
		if (!validarNiveles()) {
			participantes.remove(inscripcion);
			throw new IllegalArgumentException("No se puede programar partido entre niveles no adyacentes sin entrenador.");
		}
	}

	public boolean validarNiveles() {
		if (intervieneEntrenador) {
			return true;
		}
		for (int i = 0; i < participantes.size(); i++) {
			for (int j = i + 1; j < participantes.size(); j++) {
				if (!participantes.get(i).esNivelCompatible(participantes.get(j))) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean validarModalidad() {
		return modalidad == Modalidad.SENCILLOS || modalidad == Modalidad.DOBLES;
	}

	public int getCantidadJugadoresRequerida() {
		if (modalidad == Modalidad.SENCILLOS) {
			return 2;
		}
		return 4;
	}

	public void registrarEstadistica(EstadisticasPartidoIndividual est) {
		if (est == null) {
			throw new IllegalArgumentException("La estadistica no puede ser nula.");
		}
		estadisticas.add(est);
	}

	public void finalizar() {
		if (participantes.size() != getCantidadJugadoresRequerida()) {
			throw new IllegalStateException("Faltan jugadores en el partido.");
		}
		
		if (estadisticas.size() == participantes.size()) {
			InscripcionDisciplinaIndividual j1 = participantes.get(0);
			InscripcionDisciplinaIndividual j2 = participantes.get(1);
			
			EstadisticasPartidoIndividual e1 = estadisticas.get(0);
			EstadisticasPartidoIndividual e2 = estadisticas.get(1);
			
			j1.actualizarRankingElo(j2.getRankingElo(), e1.esGanador());
			j2.actualizarRankingElo(j1.getRankingElo(), e2.esGanador());
		}
	}

	public abstract EstadisticasPartidoIndividual crearEstadisticas();

	public LocalDate getFecha() { return fecha; }
	public void setFecha(LocalDate fecha) { this.fecha = fecha; }
	
	public LocalTime getHoraInicio() { return horaInicio; }
	public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
	
	public LocalTime getHoraFin() { return horaFin; }
	public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
	
	public Modalidad getModalidad() { return modalidad; }
	public void setModalidad(Modalidad modalidad) { this.modalidad = modalidad; }
	
	public CanchaDeporteIndividual getCancha() { return cancha; }
	public void setCancha(CanchaDeporteIndividual cancha) { this.cancha = cancha; }
	
	public boolean isIntervieneEntrenador() { return intervieneEntrenador; }
	public void setIntervieneEntrenador(boolean intervieneEntrenador) { this.intervieneEntrenador = intervieneEntrenador; }
	
	public List<InscripcionDisciplinaIndividual> getParticipantes() { return participantes; }
	public List<EstadisticasPartidoIndividual> getEstadisticas() { return estadisticas; }
}