package administracion;

import java.io.Serializable;
import articulosYVentas.Articulo;
import enumeraciones.EstadoSolicitud;

public abstract class Sugerencia implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String nombrePropuesto;
	private double precioPropuesto;
	private EstadoSolicitud estado;
	private String descripcion;
	private String autor;
	private String motivoRechazo;
	private Articulo articuloCreado;

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

	public void rechazar(String motivo) {
		this.estado = EstadoSolicitud.RECHAZADA;
		this.motivoRechazo = motivo;
	}

	public String getDescripcion() { return descripcion; }
	public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
	public String getAutor() { return autor; }
	public void setAutor(String autor) { this.autor = autor; }
	public String getMotivoRechazo() { return motivoRechazo; }
	public Articulo getArticuloCreado() { return articuloCreado; }
	public void setArticuloCreado(Articulo articuloCreado) { this.articuloCreado = articuloCreado; }

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