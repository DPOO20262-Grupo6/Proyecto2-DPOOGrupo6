package deportesDeConjunto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import inscripciones.InscripcionEquipo;

public abstract class Competencia implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String nombre;
	private String entidadOrganizadora;
	private int anioTemporada;
	private String deporte;
	private String categoriaHabilitada;
	private int jugadoresMinimosNomina;
	
	protected List<NominaCompetencia> nominas;
	protected List<PartidoDeCompetencia> partidos;

	public Competencia(String nombre, String entidadOrganizadora, int anioTemporada, String deporte, String categoriaHabilitada, int jugadoresMinimosNomina) {
		this.nombre = nombre;
		this.entidadOrganizadora = entidadOrganizadora;
		this.anioTemporada = anioTemporada;
		this.deporte = deporte;
		this.categoriaHabilitada = categoriaHabilitada;
		this.jugadoresMinimosNomina = jugadoresMinimosNomina;
		this.nominas = new ArrayList<NominaCompetencia>();
		this.partidos = new ArrayList<PartidoDeCompetencia>();
	}

	public NominaCompetencia inscribirEquipo(Equipo equipo, List<InscripcionEquipo> jugadores) {
		if (!validarCategoria(equipo)) {
			throw new IllegalArgumentException("El equipo no cumple la categoria o deporte de la competencia.");
		}
		
		NominaCompetencia nomina = new NominaCompetencia(equipo, this, LocalDate.now());
		
		for (int i = 0; i < jugadores.size(); i++) {
			nomina.agregarJugador(jugadores.get(i));
		}
		
		if (!nomina.cumpleNominaMinima()) {
			throw new IllegalStateException("No se cumple el minimo de jugadores en la nomina.");
		}
		
		nominas.add(nomina);
		return nomina;
	}

	public boolean validarCategoria(Equipo equipo) {
		if (equipo == null) {
			return false;
		}
		return equipo.getDeporte().equalsIgnoreCase(deporte) && equipo.getCategoria().equalsIgnoreCase(categoriaHabilitada);
	}

	public boolean estaEnNomina(InscripcionEquipo inscripcion) {
		for (int i = 0; i < nominas.size(); i++) {
			if (nominas.get(i).buscarJugador(inscripcion) != null) {
				return true;
			}
		}
		return false;
	}

	public NominaCompetencia buscarNomina(Equipo equipo) {
		for (int i = 0; i < nominas.size(); i++) {
			if (nominas.get(i).getEquipo().equals(equipo)) {
				return nominas.get(i);
			}
		}
		return null;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getEntidadOrganizadora() {
		return entidadOrganizadora;
	}

	public void setEntidadOrganizadora(String entidadOrganizadora) {
		this.entidadOrganizadora = entidadOrganizadora;
	}

	public int getAnioTemporada() {
		return anioTemporada;
	}

	public void setAnioTemporada(int anioTemporada) {
		this.anioTemporada = anioTemporada;
	}

	public String getDeporte() {
		return deporte;
	}

	public void setDeporte(String deporte) {
		this.deporte = deporte;
	}

	public String getCategoriaHabilitada() {
		return categoriaHabilitada;
	}

	public void setCategoriaHabilitada(String categoriaHabilitada) {
		this.categoriaHabilitada = categoriaHabilitada;
	}

	public int getJugadoresMinimosNomina() {
		return jugadoresMinimosNomina;
	}

	public void setJugadoresMinimosNomina(int jugadoresMinimosNomina) {
		this.jugadoresMinimosNomina = jugadoresMinimosNomina;
	}

	public List<NominaCompetencia> getNominas() {
		return nominas;
	}

	public List<PartidoDeCompetencia> getPartidos() {
		return partidos;
	}
}