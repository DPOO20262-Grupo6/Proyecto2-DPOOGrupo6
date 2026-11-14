package deportesDeConjunto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import inscripciones.InscripcionEquipo;
import inscripciones.InscripcionEquipoFutbol;

public class NominaCompetencia implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private LocalDate fechaInscripcion;
	private int puntosAcumulados;
	private int partidosGanados;
	private Equipo equipo;
	private Competencia competencia;
	private List<JugadorEnNomina> jugadores;

	public NominaCompetencia(Equipo equipo, Competencia competencia, LocalDate fechaInscripcion) {
		this.equipo = equipo;
		this.competencia = competencia;
		this.fechaInscripcion = fechaInscripcion;
		this.puntosAcumulados = 0;
		this.partidosGanados = 0;
		this.jugadores = new ArrayList<JugadorEnNomina>();
	}

	public JugadorEnNomina agregarJugador(InscripcionEquipo inscripcion) {
		if (buscarJugador(inscripcion) != null) {
			throw new IllegalArgumentException("El jugador ya se encuentra en esta nomina.");
		}
		
		JugadorEnNomina jn = null;
		if (inscripcion instanceof InscripcionEquipoFutbol) {
			jn = new JugadorEnNominaFutbol(inscripcion);
		} else {
			jn = new JugadorEnNomina(inscripcion);
		}
		jugadores.add(jn);
		return jn;
	}

	public boolean cumpleNominaMinima() {
		int habilitados = 0;
		for (int i = 0; i < jugadores.size(); i++) {
			if (jugadores.get(i).estaHabilitado()) {
				habilitados = habilitados + 1;
			}
		}
		return habilitados >= competencia.getJugadoresMinimosNomina();
	}

	public void sumarPuntos(int puntos) {
		this.puntosAcumulados = this.puntosAcumulados + puntos;
	}

	public void registrarPartidoGanado() {
		this.partidosGanados = this.partidosGanados + 1;
	}

	public JugadorEnNomina buscarJugador(InscripcionEquipo inscripcion) {
		for (int i = 0; i < jugadores.size(); i++) {
			JugadorEnNomina j = jugadores.get(i);
			if (j.getInscripcion().equals(inscripcion)) {
				return j;
			}
		}
		return null;
	}

	public LocalDate getFechaInscripcion() {
		return fechaInscripcion;
	}

	public void setFechaInscripcion(LocalDate fechaInscripcion) {
		this.fechaInscripcion = fechaInscripcion;
	}

	public int getPuntosAcumulados() {
		return puntosAcumulados;
	}

	public void setPuntosAcumulados(int puntosAcumulados) {
		this.puntosAcumulados = puntosAcumulados;
	}

	public int getPartidosGanados() {
		return partidosGanados;
	}

	public void setPartidosGanados(int partidosGanados) {
		this.partidosGanados = partidosGanados;
	}

	public Equipo getEquipo() {
		return equipo;
	}

	public void setEquipo(Equipo equipo) {
		this.equipo = equipo;
	}

	public Competencia getCompetencia() {
		return competencia;
	}

	public void setCompetencia(Competencia competencia) {
		this.competencia = competencia;
	}

	public List<JugadorEnNomina> getJugadores() {
		return jugadores;
	}

	public void setJugadores(List<JugadorEnNomina> jugadores) {
		this.jugadores = jugadores;
	}
}