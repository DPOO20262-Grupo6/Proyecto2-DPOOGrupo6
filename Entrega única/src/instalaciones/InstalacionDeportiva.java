package instalaciones;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import deportesIndividuales.ReservaInstalacionDeporteIndividual;

public abstract class InstalacionDeportiva implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String nombre;
	protected List<ReservaInstalacionDeporteIndividual> reservas;

	public InstalacionDeportiva(String nombre) {
		this.nombre = nombre;
		this.reservas = new ArrayList<ReservaInstalacionDeporteIndividual>();
	}

	public boolean estaDisponible(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
		for (int i = 0; i < reservas.size(); i++) {
			ReservaInstalacionDeporteIndividual reserva = reservas.get(i);
			if ("CONFIRMADA".equalsIgnoreCase(reserva.getEstado()) && reserva.seCruzaCon(fecha, horaInicio, horaFin)) {
				return false;
			}
		}
		return true;
	}

	public void agregarReserva(ReservaInstalacionDeporteIndividual reserva) {
		if (reserva != null) {
			reservas.add(reserva);
		}
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public List<ReservaInstalacionDeporteIndividual> getReservas() {
		return reservas;
	}

	public void setReservas(List<ReservaInstalacionDeporteIndividual> reservas) {
		this.reservas = reservas;
	}
}