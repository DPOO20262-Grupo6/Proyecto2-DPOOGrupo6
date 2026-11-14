package entrenamiento;

import java.io.Serializable;
import enumeraciones.EstadoAsistencia;
import usuarios.Socio;

public class RegistroAsistencia implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private EstadoAsistencia estadoAsistencia;
	private Integer notaDesempeno;
	private String comentario;
	private Socio socio;
	private SesionDeEntrenamiento sesion;

	public RegistroAsistencia(Socio socio, SesionDeEntrenamiento sesion, EstadoAsistencia estadoAsistencia, Integer notaDesempeno, String comentario) {
		if (notaDesempeno != null && (notaDesempeno < 1 || notaDesempeno > 5)) {
			throw new IllegalArgumentException("La nota debe estar entre 1 y 5.");
		}
		this.socio = socio;
		this.sesion = sesion;
		this.estadoAsistencia = estadoAsistencia;
		this.notaDesempeno = notaDesempeno;
		this.comentario = comentario;
	}

	public EstadoAsistencia getEstadoAsistencia() {
		return estadoAsistencia;
	}

	public void setEstadoAsistencia(EstadoAsistencia estadoAsistencia) {
		this.estadoAsistencia = estadoAsistencia;
	}

	public Integer getNotaDesempeno() {
		return notaDesempeno;
	}

	public void setNotaDesempeno(Integer notaDesempeno) {
		this.notaDesempeno = notaDesempeno;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public Socio getSocio() {
		return socio;
	}

	public void setSocio(Socio socio) {
		this.socio = socio;
	}

	public SesionDeEntrenamiento getSesion() {
		return sesion;
	}

	public void setSesion(SesionDeEntrenamiento sesion) {
		this.sesion = sesion;
	}

	@Override
	public String toString() {
		return socio.getNombre() + " - " + estadoAsistencia + " (Nota: " + notaDesempeno + ", " + comentario + ")";
	}
}
