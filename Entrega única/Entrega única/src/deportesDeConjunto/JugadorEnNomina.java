package deportesDeConjunto;

import java.io.Serializable;
import inscripciones.InscripcionEquipo;

public class JugadorEnNomina implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private InscripcionEquipo inscripcion;

	public JugadorEnNomina(InscripcionEquipo inscripcion) {
		this.inscripcion = inscripcion;
	}

	public boolean estaHabilitado() {
		return inscripcion != null && inscripcion.getSocio() != null;
	}

	public InscripcionEquipo getInscripcion() {
		return inscripcion;
	}

	public void setInscripcion(InscripcionEquipo inscripcion) {
		this.inscripcion = inscripcion;
	}
}