package usuarios;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import administracion.SolicitudCambioTurnoEmpleado;
import administracion.Sugerencia;
import administracion.SugerenciaArticulo;
import administracion.SugerenciaSnack;
import administracion.TurnoEmpleado;

public abstract class EmpleadoDelClub extends Usuario {
	
	private static final long serialVersionUID = 1L;
	
	public static final double DESCUENTO_PROPIO = 0.15;
	public static final double DESCUENTO_TERCEROS = 0.08;
	
	private String codigoDescuento;
	private int puntosDeFidelidad;

	public EmpleadoDelClub(String nombre, String login, String password) {
		super(nombre, login, password);
		this.puntosDeFidelidad = 0;
		this.codigoDescuento = "COD-" + login.toUpperCase();
	}

	public double getPorcentajeDescuentoPropio() {
		return DESCUENTO_PROPIO;
	}

	public Sugerencia proponerSugerenciaArticulo(String nombre, double precio, String categoria) {
		return new SugerenciaArticulo(nombre, precio, categoria);
	}

	public Sugerencia proponerSugerenciaSnack(String nombre, double precio, List<String> alergenos) {
		return new SugerenciaSnack(nombre, precio, alergenos);
	}

	public SolicitudCambioTurnoEmpleado solicitarCambioTurno(TurnoEmpleado turno, LocalDate nuevaFecha, LocalTime nuevaHora) {
		return new SolicitudCambioTurnoEmpleado(turno, nuevaFecha, nuevaHora);
	}

	@Override
	public void acumularPuntos(int cantidad) {
		if (cantidad > 0) {
			this.puntosDeFidelidad = this.puntosDeFidelidad + cantidad;
		}
	}

	@Override
	public void redimirPuntos(int cantidad) {
		if (cantidad <= 0) {
			throw new IllegalArgumentException("La cantidad a redimir debe ser mayor a cero.");
		}
		if (cantidad > this.puntosDeFidelidad) {
			throw new IllegalArgumentException("No tiene suficientes puntos. Saldo actual: " + this.puntosDeFidelidad);
		}
		this.puntosDeFidelidad = this.puntosDeFidelidad - cantidad;
	}

	public String getCodigoDescuento() {
		return codigoDescuento;
	}

	@Override
	public int getPuntosDeFidelidad() {
		return puntosDeFidelidad;
	}
}