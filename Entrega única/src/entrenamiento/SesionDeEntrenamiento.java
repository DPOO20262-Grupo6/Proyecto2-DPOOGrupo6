package entrenamiento;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import deportesDeConjunto.Equipo;
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
	private Equipo equipo;
	private String disciplina;

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

	public boolean haFinalizado() {
		LocalDateTime fin = LocalDateTime.of(fecha, hora).plusMinutes(duracion);
		return !LocalDateTime.now().isBefore(fin);
	}

	public RegistroAsistencia buscarRegistro(Socio socio) {
		for (int i = 0; i < asistencias.size(); i++) {
			if (asistencias.get(i).getSocio().equals(socio)) {
				return asistencias.get(i);
			}
		}
		return null;
	}

	public void registrarAsistencia(Socio socio, EstadoAsistencia estado) {
		if (!haFinalizado()) {
			throw new IllegalStateException("La sesion aun no ha finalizado.");
		}
		if (!convocados.contains(socio)) {
			throw new IllegalArgumentException("El socio no fue convocado a la sesion.");
		}
		if (estado == null) {
			throw new IllegalArgumentException("El estado debe ser ASISTIO, FALTO o LLEGO_TARDE.");
		}
		RegistroAsistencia existente = buscarRegistro(socio);
		if (existente != null) {
			existente.setEstadoAsistencia(estado);
			return;
		}
		asistencias.add(new RegistroAsistencia(socio, this, estado, null, null));
	}

	public void registrarEvaluacion(Socio socio, double nota, String comentario) {
		if (nota != Math.floor(nota) || nota < 1 || nota > 5) {
			throw new IllegalArgumentException("La nota debe ser un entero entre 1 y 5.");
		}
		RegistroAsistencia registro = buscarRegistro(socio);
		if (registro == null || registro.getEstadoAsistencia() == EstadoAsistencia.FALTO) {
			throw new IllegalStateException("Solo se puede evaluar a un jugador que asistio o llego tarde.");
		}
		registro.setNotaDesempeno((int) nota);
		registro.setComentario(comentario);
	}

	public Equipo getEquipo() { return equipo; }
	public void setEquipo(Equipo equipo) { this.equipo = equipo; }

	public String getDisciplina() { return disciplina; }
	public void setDisciplina(String disciplina) { this.disciplina = disciplina; }

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