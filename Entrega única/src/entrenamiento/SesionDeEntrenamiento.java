package entrenamiento;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import enumeraciones.EstadoAsistencia;
import instalaciones.InstalacionDeportiva;
import usuarios.Entrenador;
import usuarios.Socio;

public class SesionDeEntrenamiento implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private LocalDate fecha;
	private LocalTime hora;
	private int duracion;
	private Entrenador entrenador;
	private InstalacionDeportiva instalacion;
	private List<Socio> convocados;
	private List<RegistroAsistencia> asistencias;

	public SesionDeEntrenamiento(LocalDate fecha, LocalTime hora, int duracion, Entrenador entrenador, InstalacionDeportiva instalacion) {
		this.fecha = fecha;
		this.hora = hora;
		this.duracion = duracion;
		this.entrenador = entrenador;
		this.instalacion = instalacion;
		this.convocados = new ArrayList<Socio>();
		this.asistencias = new ArrayList<RegistroAsistencia>();
	}

	public void convocar(Socio socio) {
		if (socio != null && !convocados.contains(socio)) {
			convocados.add(socio);
		}
	}

	public boolean seCruzaCon(LocalDate otraFecha, LocalTime otraHora, int otraDuracion) {
		if (!this.fecha.equals(otraFecha)) {
			return false;
		}
		LocalTime finActual = this.hora.plusMinutes(this.duracion);
		LocalTime finOtra = otraHora.plusMinutes(otraDuracion);
		return this.hora.isBefore(finOtra) && otraHora.isBefore(finActual);
	}

	public void registrarAsistencia(Socio socio, EstadoAsistencia estado, Integer nota, String comentario) {
		if (!convocados.contains(socio)) {
			throw new IllegalArgumentException("El socio no fue convocado a la sesion.");
		}
		RegistroAsistencia registro = new RegistroAsistencia(socio, this, estado, nota, comentario);
		asistencias.add(registro);
	}

	public LocalDate getFecha() { return fecha; }
	public void setFecha(LocalDate fecha) { this.fecha = fecha; }
	
	public LocalTime getHora() { return hora; }
	public void setHora(LocalTime hora) { this.hora = hora; }
	
	public int getDuracion() { return duracion; }
	public void setDuracion(int duracion) { this.duracion = duracion; }
	
	public Entrenador getEntrenador() { return entrenador; }
	public void setEntrenador(Entrenador entrenador) { this.entrenador = entrenador; }
	
	public InstalacionDeportiva getInstalacion() { return instalacion; }
	public void setInstalacion(InstalacionDeportiva instalacion) { this.instalacion = instalacion; }
	
	public List<Socio> getConvocados() { return convocados; }
	public void setConvocados(List<Socio> convocados) { this.convocados = convocados; }
	
	public List<RegistroAsistencia> getAsistencias() { return asistencias; }
	public void setAsistencias(List<RegistroAsistencia> asistencias) { this.asistencias = asistencias; }
}