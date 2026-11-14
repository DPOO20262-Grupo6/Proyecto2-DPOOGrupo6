package deportesDeConjunto;

import java.time.LocalDate;
import enumeraciones.MotivoSuspension;
import estadisticas.EstadisticasPartidoDeEquipo;
import estadisticas.EstadisticasPartidoFutbol;
import estadisticas.Suspension;
import inscripciones.InscripcionEquipo;
import inscripciones.InscripcionEquipoFutbol;

public class PartidoInterno extends PartidoDeEquipo {
	
	private static final long serialVersionUID = 1L;
	
	private Equipo equipoLocal;
	private Equipo equipoVisitante;

	public PartidoInterno(LocalDate fecha, Equipo equipoLocal, Equipo equipoVisitante) {
		super(fecha, equipoLocal.getDeporte());
		this.equipoLocal = equipoLocal;
		this.equipoVisitante = equipoVisitante;
		
		if (!validarEquipos()) {
			throw new IllegalArgumentException("Los equipos deben ser del mismo deporte y categoria.");
		}
	}

	public boolean validarEquipos() {
		if (equipoLocal == null || equipoVisitante == null || equipoLocal.equals(equipoVisitante)) {
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

	public Equipo getEquipoLocal() { return equipoLocal; }
	public void setEquipoLocal(Equipo equipoLocal) { this.equipoLocal = equipoLocal; }
	public Equipo getEquipoVisitante() { return equipoVisitante; }
	public void setEquipoVisitante(Equipo equipoVisitante) { this.equipoVisitante = equipoVisitante; }

	@Override
	public String toString() {
		String texto = equipoLocal.getNombre() + " vs " + equipoVisitante.getNombre() + " (" + getFecha() + ")";
		if (resultado != null) {
			texto = texto + " Resultado: " + resultado.toString();
		}
		return texto;
	}
}