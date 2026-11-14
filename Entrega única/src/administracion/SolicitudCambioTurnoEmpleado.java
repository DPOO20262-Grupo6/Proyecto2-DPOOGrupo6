package administracion;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import enumeraciones.EstadoSolicitud;

public class SolicitudCambioTurnoEmpleado implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private EstadoSolicitud estado;
	private LocalDate fechaSolicitud;
	private LocalDate nuevaFecha;
	private LocalTime nuevaHora;
	private TurnoEmpleado turno;

	public SolicitudCambioTurnoEmpleado(TurnoEmpleado turno, LocalDate nuevaFecha, LocalTime nuevaHora) {
		this.turno = turno;
		this.fechaSolicitud = LocalDate.now();
		this.nuevaFecha = nuevaFecha;
		this.nuevaHora = nuevaHora;
		this.estado = EstadoSolicitud.PENDIENTE;
	}

	public void aprobar() {
		this.estado = EstadoSolicitud.APROBADA;
		this.turno.reprogramar(nuevaFecha, nuevaHora);
	}

	public void rechazar() {
		this.estado = EstadoSolicitud.RECHAZADA;
	}

	public EstadoSolicitud getEstado() {
		return estado;
	}

	public void setEstado(EstadoSolicitud estado) {
		this.estado = estado;
	}

	public LocalDate getFechaSolicitud() {
		return fechaSolicitud;
	}

	public void setFechaSolicitud(LocalDate fechaSolicitud) {
		this.fechaSolicitud = fechaSolicitud;
	}

	public LocalDate getNuevaFecha() {
		return nuevaFecha;
	}

	public void setNuevaFecha(LocalDate nuevaFecha) {
		this.nuevaFecha = nuevaFecha;
	}

	public LocalTime getNuevaHora() {
		return nuevaHora;
	}

	public void setNuevaHora(LocalTime nuevaHora) {
		this.nuevaHora = nuevaHora;
	}

	public TurnoEmpleado getTurno() {
		return turno;
	}

	public void setTurno(TurnoEmpleado turno) {
		this.turno = turno;
	}
}