package administracion;

import java.io.Serializable;
import articulosYVentas.Articulo;
import enumeraciones.EstadoSolicitud;

public abstract class Sugerencia implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String nombrePropuesto;
	private double precioPropuesto;
	private EstadoSolicitud estado;

	public Sugerencia(String nombrePropuesto, double precioPropuesto) {
		this.nombrePropuesto = nombrePropuesto;
		this.precioPropuesto = precioPropuesto;
		this.estado = EstadoSolicitud.PENDIENTE;
	}

	public void aprobar() {
		this.estado = EstadoSolicitud.APROBADA;
	}

	public void rechazar() {
		this.estado = EstadoSolicitud.RECHAZADA;
	}

	public abstract Articulo convertirEnArticulo();

	public String getNombrePropuesto() {
		return nombrePropuesto;
	}

	public void setNombrePropuesto(String nombrePropuesto) {
		this.nombrePropuesto = nombrePropuesto;
	}

	public double getPrecioPropuesto() {
		return precioPropuesto;
	}

	public void setPrecioPropuesto(double precioPropuesto) {
		this.precioPropuesto = precioPropuesto;
	}

	public EstadoSolicitud getEstado() {
		return estado;
	}

	public void setEstado(EstadoSolicitud estado) {
		this.estado = estado;
	}
}