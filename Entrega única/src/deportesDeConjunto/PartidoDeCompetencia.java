package deportesDeConjunto;

import java.time.LocalDate;
import enumeraciones.MotivoSuspension;
import estadisticas.EstadisticasPartidoDeEquipo;
import estadisticas.EstadisticasPartidoFutbol;
import estadisticas.Suspension;
import inscripciones.InscripcionEquipo;

public class PartidoDeCompetencia extends PartidoDeEquipo {
	
	private static final long serialVersionUID = 1L;
	
	private String institucionRival;
	private String escenario;
	private boolean esLocal;
	private Competencia competencia;
	private Equipo equipoClub;

	public PartidoDeCompetencia(LocalDate fecha, String deporte, Competencia competencia, Equipo equipoClub, String institucionRival, String escenario, boolean esLocal) {
		super(fecha, deporte);
		this.competencia = competencia;
		this.equipoClub = equipoClub;
		this.institucionRival = institucionRival;
		this.escenario = escenario;
		this.esLocal = esLocal;
	}

	public boolean validarAlineacion(InscripcionEquipo inscripcion) {
		NominaCompetencia nomina = competencia.buscarNomina(equipoClub);
		if (nomina == null) {
			return false;
		}
		JugadorEnNomina jugadorNomina = nomina.buscarJugador(inscripcion);
		return jugadorNomina != null && jugadorNomina.estaHabilitado();
	}

	@Override
	public void registrarParticipacion(InscripcionEquipo inscripcion) {
		if (!validarAlineacion(inscripcion)) {
			throw new IllegalStateException("El jugador no esta habilitado en la nomina de la competencia.");
		}
		participantes.add(inscripcion);
	}

	@Override
	public boolean validarCuorum() {
		return equipoClub.cumpleMinimoJugadores();
	}

	@Override
	protected void procesarTarjetasYSuspensiones(InscripcionEquipo inscripcion, EstadisticasPartidoDeEquipo estadisticas) {
		NominaCompetencia nomina = competencia.buscarNomina(equipoClub);
		if (nomina == null) {
			return;
		}
		JugadorEnNomina jugadorNomina = nomina.buscarJugador(inscripcion);
		
		if (jugadorNomina instanceof JugadorEnNominaFutbol && estadisticas instanceof EstadisticasPartidoFutbol) {
			JugadorEnNominaFutbol jugadorNominaFutbol = (JugadorEnNominaFutbol) jugadorNomina;
			EstadisticasPartidoFutbol estadisticasFutbol = (EstadisticasPartidoFutbol) estadisticas;

			if (estadisticasFutbol.getTarjetasRojas() > 0) {
				jugadorNominaFutbol.agregarSuspension(new Suspension(MotivoSuspension.ROJA));
			} else if (estadisticasFutbol.getTarjetasAmarillas() >= 2) {
				jugadorNominaFutbol.agregarSuspension(new Suspension(MotivoSuspension.ACUMULACION_AMARILLAS));
			} else if (estadisticasFutbol.getTarjetasAmarillas() == 1) {
				jugadorNominaFutbol.incrementarAmarillasCompetencia();
				if (jugadorNominaFutbol.getTarjetasAmarillasAcumuladasCompetencia() >= 2) {
					jugadorNominaFutbol.agregarSuspension(new Suspension(MotivoSuspension.ACUMULACION_AMARILLAS));
					jugadorNominaFutbol.reiniciarAmarillasCompetencia();
				}
			}
		}
	}

	@Override
	public Equipo getGanador() {
		if (resultado == null || resultado.esEmpate()) {
			return null;
		}
		if ("LOCAL".equals(resultado.getGanador())) {
			return equipoClub;
		}
		return null;
	}

	public Equipo getEquipoClub() { return equipoClub; }
	public void setEquipoClub(Equipo equipoClub) { this.equipoClub = equipoClub; }
	public String getInstitucionRival() { return institucionRival; }
	public void setInstitucionRival(String institucionRival) { this.institucionRival = institucionRival; }
	public String getEscenario() { return escenario; }
	public void setEscenario(String escenario) { this.escenario = escenario; }
	public boolean isEsLocal() { return esLocal; }
	public void setEsLocal(boolean esLocal) { this.esLocal = esLocal; }
	public Competencia getCompetencia() { return competencia; }
	public void setCompetencia(Competencia competencia) { this.competencia = competencia; }

	@Override
	public String toString() {
		return equipoClub.getNombre() + " vs " + institucionRival + " en " + escenario;
	}
}