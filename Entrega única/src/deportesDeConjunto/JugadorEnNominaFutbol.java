package deportesDeConjunto;

import java.util.ArrayList;
import java.util.List;
import estadisticas.Suspension;
import inscripciones.InscripcionEquipo;

public class JugadorEnNominaFutbol extends JugadorEnNomina {
	
	private static final long serialVersionUID = 1L;
	
	private int tarjetasAmarillasAcumuladasCompetencia;
	private List<Suspension> suspensiones;

	public JugadorEnNominaFutbol(InscripcionEquipo inscripcion) {
		super(inscripcion);
		this.tarjetasAmarillasAcumuladasCompetencia = 0;
		this.suspensiones = new ArrayList<Suspension>();
	}

	public int getTarjetasAmarillasAcumuladasCompetencia() {
		return tarjetasAmarillasAcumuladasCompetencia;
	}

	public void setTarjetasAmarillasAcumuladasCompetencia(int tarjetasAmarillasAcumuladasCompetencia) {
		this.tarjetasAmarillasAcumuladasCompetencia = tarjetasAmarillasAcumuladasCompetencia;
	}

	public void incrementarAmarillasCompetencia() {
		this.tarjetasAmarillasAcumuladasCompetencia = this.tarjetasAmarillasAcumuladasCompetencia + 1;
	}

	public void reiniciarAmarillasCompetencia() {
		this.tarjetasAmarillasAcumuladasCompetencia = 0;
	}

	public boolean estaSuspendidoCompetencia() {
		for (int i = 0; i < suspensiones.size(); i++) {
			if (!suspensiones.get(i).isCumplida()) {
				return true;
			}
		}
		return false;
	}

	public void agregarSuspension(Suspension suspension) {
		if (suspension != null) {
			this.suspensiones.add(suspension);
		}
	}

	public List<Suspension> getSuspensiones() {
		return suspensiones;
	}

	public void setSuspensiones(List<Suspension> suspensiones) {
		this.suspensiones = suspensiones;
	}

	@Override
	public boolean estaHabilitado() {
		return super.estaHabilitado() && !estaSuspendidoCompetencia();
	}
}