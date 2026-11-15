package deportesDeConjunto;

import java.time.LocalDate;
import java.util.List;
import enumeraciones.MotivoSuspension;
import estadisticas.EstadisticasPartidoDeEquipo;
import estadisticas.EstadisticasPartidoFutbol;
import estadisticas.Suspension;
import inscripciones.InscripcionEquipo;
import inscripciones.InscripcionEquipoFutbol;
import usuarios.Entrenador;

public class PartidoInterno extends PartidoDeEquipo {
	
	private static final long serialVersionUID = 1L;
	
	private Equipo equipoLocal;
	private Equipo equipoVisitante;
	private Entrenador entrenador;

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

	public boolean isFinalizado() {
		return resultado != null;
	}

	public void registrarAlineacion(Equipo equipo, List<InscripcionEquipo> jugadores) {
		if (equipo != equipoLocal && equipo != equipoVisitante) {
			throw new IllegalArgumentException("El equipo " + equipo.getNombre() + " no juega este partido.");
		}
		for (int i = 0; i < jugadores.size(); i++) {
			InscripcionEquipo jugador = jugadores.get(i);
			if (!equipo.getInscripciones().contains(jugador) || !jugador.isActiva()) {
				throw new IllegalArgumentException("El jugador " + jugador.getSocio().getNombre() + " no pertenece al equipo " + equipo.getNombre() + ".");
			}
			if (!jugador.estaHabilitado()) {
				throw new IllegalArgumentException("El jugador " + jugador.getSocio().getNombre() + " esta suspendido.");
			}
		}
		for (int i = 0; i < jugadores.size(); i++) {
			registrarParticipacion(jugadores.get(i));
		}
	}

	@Override
	public void registrarEstadistica(InscripcionEquipo inscripcion, EstadisticasPartidoDeEquipo est) {
		if (!isFinalizado()) {
			throw new IllegalStateException("El partido aun no ha finalizado.");
		}
		super.registrarEstadistica(inscripcion, est);
	}

	@Override
	public void registrarResultado(ResultadoPartidoEquipo resultado) {
		super.registrarResultado(resultado);
		cumplirSuspensiones(equipoLocal);
		cumplirSuspensiones(equipoVisitante);
	}

	private void cumplirSuspensiones(Equipo equipo) {
		for (int i = 0; i < equipo.getInscripciones().size(); i++) {
			if (equipo.getInscripciones().get(i) instanceof InscripcionEquipoFutbol) {
				List<Suspension> suspensiones = ((InscripcionEquipoFutbol) equipo.getInscripciones().get(i)).getSuspensiones();
				for (int j = 0; j < suspensiones.size(); j++) {
					if (!suspensiones.get(j).isCumplida()) {
						suspensiones.get(j).seCumpleEn(this);
					}
				}
			}
		}
	}

	public Entrenador getEntrenador() { return entrenador; }
	public void setEntrenador(Entrenador entrenador) { this.entrenador = entrenador; }

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