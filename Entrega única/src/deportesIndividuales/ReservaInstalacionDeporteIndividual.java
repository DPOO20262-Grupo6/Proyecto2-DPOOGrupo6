package deportesIndividuales;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import enumeraciones.Modalidad;
import instalaciones.CanchaDeporteIndividual;
import usuarios.Socio;

public class ReservaInstalacionDeporteIndividual implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private LocalDate fecha;
	private LocalTime horaInicio;
	private LocalTime horaFin;
	private int cantidadJugadores;
	private Modalidad modalidad;
	private String estado;
	private String motivoRechazo;
	private CanchaDeporteIndividual cancha;
	private List<Socio> socios;

	public ReservaInstalacionDeporteIndividual(List<Socio> socios, CanchaDeporteIndividual cancha, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, int cantidadJugadores, Modalidad modalidad) {
		this.socios = new ArrayList<Socio>();
		if (socios != null) {
			for (int i = 0; i < socios.size(); i++) {
				this.socios.add(socios.get(i));
			}
		}
		this.cancha = cancha;
		this.fecha = fecha;
		this.horaInicio = horaInicio;
		this.horaFin = horaFin;
		this.cantidadJugadores = cantidadJugadores;
		this.modalidad = modalidad;
		this.estado = "PENDIENTE";
	}

	public void confirmar() {
		this.estado = "CONFIRMADA";
		this.cancha.agregarReserva(this);
	}

	public void rechazar(String motivo) {
		this.estado = "RECHAZADA";
		this.motivoRechazo = motivo;
	}

	public boolean seCruzaCon(LocalDate otraFecha, LocalTime otroInicio, LocalTime otroFin) {
		if (!this.fecha.equals(otraFecha)) {
			return false;
		}
		return this.horaInicio.isBefore(otroFin) && otroInicio.isBefore(this.horaFin);
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public void setFecha(LocalDate fecha) {
		this.fecha = fecha;
	}

	public LocalTime getHoraInicio() {
		return horaInicio;
	}

	public void setHoraInicio(LocalTime horaInicio) {
		this.horaInicio = horaInicio;
	}

	public LocalTime getHoraFin() {
		return horaFin;
	}

	public void setHoraFin(LocalTime horaFin) {
		this.horaFin = horaFin;
	}

	public int getCantidadJugadores() {
		return cantidadJugadores;
	}

	public void setCantidadJugadores(int cantidadJugadores) {
		this.cantidadJugadores = cantidadJugadores;
	}

	public Modalidad getModalidad() {
		return modalidad;
	}

	public void setModalidad(Modalidad modalidad) {
		this.modalidad = modalidad;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getMotivoRechazo() {
		return motivoRechazo;
	}

	public void setMotivoRechazo(String motivoRechazo) {
		this.motivoRechazo = motivoRechazo;
	}

	public CanchaDeporteIndividual getCancha() {
		return cancha;
	}

	public void setCancha(CanchaDeporteIndividual cancha) {
		this.cancha = cancha;
	}

	public List<Socio> getSocios() {
		return socios;
	}

	public void setSocios(List<Socio> socios) {
		this.socios = socios;
	}

	@Override
	public String toString() {
		String info = "Reserva en " + cancha.getNombre() + " (" + fecha + " " + horaInicio + "-" + horaFin + ") Estado: " + estado;
		if (motivoRechazo != null) {
			info = info + " - Motivo: " + motivoRechazo;
		}
		return info;
	}
}