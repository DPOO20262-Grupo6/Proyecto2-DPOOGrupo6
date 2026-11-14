package administracion;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import usuarios.EmpleadoDelClub;

public class TurnoEmpleado implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private LocalDate fecha;
	private LocalTime hora;
	private int duracion;
	private EmpleadoDelClub empleado;

	public TurnoEmpleado(EmpleadoDelClub empleado, LocalDate fecha, LocalTime hora, int duracion) {
		this.empleado = empleado;
		this.fecha = fecha;
		this.hora = hora;
		this.duracion = duracion;
	}

	public boolean seCruzaCon(LocalDate otraFecha, LocalTime otraHora) {
		if (!this.fecha.equals(otraFecha)) {
			return false;
		}
		LocalTime fin = this.hora.plusHours(this.duracion);
		return !otraHora.isBefore(this.hora) && otraHora.isBefore(fin);
	}

	public void reprogramar(LocalDate nuevaFecha, LocalTime nuevaHora) {
		this.fecha = nuevaFecha;
		this.hora = nuevaHora;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public void setFecha(LocalDate fecha) {
		this.fecha = fecha;
	}

	public LocalTime getHora() {
		return hora;
	}

	public void setHora(LocalTime hora) {
		this.hora = hora;
	}

	public int getDuracion() {
		return duracion;
	}

	public void setDuracion(int duracion) {
		this.duracion = duracion;
	}

	public EmpleadoDelClub getEmpleado() {
		return empleado;
	}

	public void setEmpleado(EmpleadoDelClub empleado) {
		this.empleado = empleado;
	}
}