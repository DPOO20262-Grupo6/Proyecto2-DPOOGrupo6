package inscripciones;

import java.util.ArrayList;
import java.util.List;
import deportesDeConjunto.Equipo;
import estadisticas.Suspension;
import usuarios.Socio;

public class InscripcionEquipoFutbol extends InscripcionEquipo {
	
	private static final long serialVersionUID = 1L;
	
	private int tarjetasAmarillasAcumuladasInterno;
	private List<Suspension> suspensiones;

	public InscripcionEquipoFutbol(Socio socio, Equipo equipo, String posicion, int numeroCamiseta) {
		super(socio, equipo, posicion, numeroCamiseta);
		this.tarjetasAmarillasAcumuladasInterno = 0;
		this.suspensiones = new ArrayList<Suspension>();
	}

	public int getTarjetasAmarillasAcumuladasInterno() {
		return tarjetasAmarillasAcumuladasInterno;
	}

	public void setTarjetasAmarillasAcumuladasInterno(int tarjetasAmarillasAcumuladasInterno) {
		this.tarjetasAmarillasAcumuladasInterno = tarjetasAmarillasAcumuladasInterno;
	}

	public void incrementarAmarillasInterno() {
		this.tarjetasAmarillasAcumuladasInterno = this.tarjetasAmarillasAcumuladasInterno + 1;
	}

	public void reiniciarAmarillasInterno() {
		this.tarjetasAmarillasAcumuladasInterno = 0;
	}

	public boolean estaSuspendidoInterno() {
		for (int i = 0; i < suspensiones.size(); i++) {
			Suspension suspension = suspensiones.get(i);
			if (!suspension.isCumplida()) {
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
		return super.estaHabilitado() && !estaSuspendidoInterno();
	}
}