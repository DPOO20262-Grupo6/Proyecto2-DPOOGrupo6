package deportesDeConjunto;

import java.time.LocalDate;
import Torneos.TorneoEquipo;
import enumeraciones.MotivoSuspension;
import estadisticas.EstadisticasPartidoDeEquipo;
import estadisticas.EstadisticasPartidoFutbol;
import estadisticas.Suspension;
import inscripciones.InscripcionEquipo;
import inscripciones.InscripcionEquipoFutbol;

public class PartidoTorneo extends PartidoDeEquipo {

	private static final long serialVersionUID = 1L;

	private TorneoEquipo torneo;
	private Equipo equipoLocal;
	private Equipo equipoVisitante;

	public PartidoTorneo(LocalDate fecha, Equipo equipoLocal, Equipo equipoVisitante, TorneoEquipo torneo) {
		super(fecha, torneo.getDeporte());
		this.torneo = torneo;
		this.equipoLocal = equipoLocal;
		this.equipoVisitante = equipoVisitante;

		if (!validarEquipos()) {
			throw new IllegalArgumentException("Ambos equipos deben estar inscritos en el torneo y ser de la misma categoria.");
		}
	}

	/** Ambos equipos inscritos en el torneo, distintos y de la misma categoria (HU-A06). */
	public boolean validarEquipos() {
		if (torneo == null || equipoLocal == null || equipoVisitante == null || equipoLocal.equals(equipoVisitante)) {
			return false;
		}
		if (!torneo.estaInscrito(equipoLocal) || !torneo.estaInscrito(equipoVisitante)) {
			return false;
		}
		boolean mismoDeporte = equipoLocal.getDeporte().equalsIgnoreCase(equipoVisitante.getDeporte());
		boolean mismaCategoria = equipoLocal.getCategoria().equalsIgnoreCase(equipoVisitante.getCategoria());
		return mismoDeporte && mismaCategoria;
	}

	@Override
	public boolean validarCuorum() {
		return equipoLocal.cumpleMinimoJugadores() && equipoVisitante.cumpleMinimoJugadores();
	}

	@Override
	protected void procesarTarjetasYSuspensiones(InscripcionEquipo inscripcion, EstadisticasPartidoDeEquipo estadisticas) {
		if (inscripcion instanceof InscripcionEquipoFutbol && estadisticas instanceof EstadisticasPartidoFutbol) {
			InscripcionEquipoFutbol inscripcionFutbol = (InscripcionEquipoFutbol) inscripcion;
			EstadisticasPartidoFutbol estadisticasFutbol = (EstadisticasPartidoFutbol) estadisticas;

			if (estadisticasFutbol.getTarjetasRojas() > 0) {
				inscripcionFutbol.agregarSuspension(new Suspension(MotivoSuspension.ROJA));
			} else if (estadisticasFutbol.getTarjetasAmarillas() >= 2) {
				inscripcionFutbol.agregarSuspension(new Suspension(MotivoSuspension.ACUMULACION_AMARILLAS));
			} else if (estadisticasFutbol.getTarjetasAmarillas() == 1) {
				inscripcionFutbol.incrementarAmarillasInterno();
				if (inscripcionFutbol.getTarjetasAmarillasAcumuladasInterno() >= 2) {
					inscripcionFutbol.agregarSuspension(new Suspension(MotivoSuspension.ACUMULACION_AMARILLAS));
					inscripcionFutbol.reiniciarAmarillasInterno();
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
			return equipoLocal;
		}
		return equipoVisitante;
	}

	public boolean participa(Equipo equipo) {
		return equipo != null && (equipo.equals(equipoLocal) || equipo.equals(equipoVisitante));
	}

	public TorneoEquipo getTorneo() { return torneo; }
	public Equipo getEquipoLocal() { return equipoLocal; }
	public Equipo getEquipoVisitante() { return equipoVisitante; }

	@Override
	public String toString() {
		String texto = equipoLocal.getNombre() + " vs " + equipoVisitante.getNombre() + " (" + getFecha() + ")";
		if (resultado != null) {
			texto = texto + " Resultado: " + resultado.toString();
		} else {
			texto = texto + " Pendiente";
		}
		return texto;
	}
}
