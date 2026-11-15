package deportesDeConjunto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import inscripciones.InscripcionEquipo;
import usuarios.Entrenador;

public abstract class Competencia implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String nombre;
	private String entidadOrganizadora;
	private int anioTemporada;
	private String deporte;
	private String categoriaHabilitada;
	private int jugadoresMinimosNomina;
	private boolean finalizada;
	
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
		if (buscarNomina(equipo) != null) {
			throw new IllegalArgumentException("El equipo " + equipo.getNombre() + " ya esta inscrito en la competencia.");
		}
		NominaCompetencia nomina = new NominaCompetencia(equipo, this, LocalDate.now());
		for (int i = 0; i < jugadores.size(); i++) {
			InscripcionEquipo jugador = jugadores.get(i);
			if (!equipo.getInscripciones().contains(jugador) || !jugador.isActiva()) {
				throw new IllegalArgumentException("El jugador " + jugador.getSocio().getNombre() + " no pertenece al equipo " + equipo.getNombre() + ".");
			}
			nomina.agregarJugador(jugador);
		}
		int habilitados = 0;
		for (int i = 0; i < nomina.getJugadores().size(); i++) {
			if (nomina.getJugadores().get(i).estaHabilitado()) {
				habilitados = habilitados + 1;
			}
		}
		if (habilitados < jugadoresMinimosNomina) {
			throw new IllegalStateException("No se cumple el minimo de jugadores en la nomina: faltan " + (jugadoresMinimosNomina - habilitados) + " habilitados.");
		}
		nominas.add(nomina);
		return nomina;
	}

	public abstract String getFormato();

	protected abstract PartidoDeCompetencia crearPartido(Equipo equipo, String rival, LocalDate fecha, String escenario);

	public PartidoDeCompetencia programarPartidoOficial(Equipo equipo, String rival, String escenario, LocalDate fecha, Entrenador entrenador) {
		if (buscarNomina(equipo) == null) {
			throw new IllegalArgumentException("El equipo no esta inscrito en la competencia.");
		}
		if (rival == null || rival.trim().isEmpty()) {
			throw new IllegalArgumentException("Falta la institucion rival.");
		}
		if (escenario == null || escenario.trim().isEmpty()) {
			throw new IllegalArgumentException("Falta el escenario.");
		}
		if (entrenador == null) {
			throw new IllegalArgumentException("Falta el entrenador responsable.");
		}
		if (fecha == null) {
			throw new IllegalArgumentException("Falta la fecha del partido.");
		}
		PartidoDeCompetencia partido = crearPartido(equipo, rival, fecha, escenario);
		partido.setEntrenador(entrenador);
		return partido;
	}

	public void registrarResultadoOficial(Entrenador entrenador, PartidoDeCompetencia partido, ResultadoPartidoEquipo resultado) {
		if (partido == null || !partidos.contains(partido)) {
			throw new IllegalArgumentException("El partido no pertenece a la competencia.");
		}
		if (partido.getEntrenador() != entrenador) {
			throw new IllegalArgumentException("No es el entrenador responsable del partido.");
		}
		if (partido.getResultado() != null) {
			throw new IllegalStateException("El partido ya tiene resultado.");
		}
		if (resultado == null) {
			throw new IllegalArgumentException("Debe indicar el marcador.");
		}
		partido.registrarResultado(resultado);
		actualizarTabla(partido);
	}

	public void actualizarTabla(PartidoDeCompetencia partido) {
		NominaCompetencia nomina = buscarNomina(partido.getEquipoClub());
		if (nomina == null || partido.getResultado() == null || partido.isContabilizado()) {
			return;
		}
		int[] marcador = marcadorDelClub(partido);
		nomina.registrarPartido(marcador[0], marcador[1], puntosPorMarcador(marcador[0], marcador[1]));
		partido.setContabilizado(true);
	}

	public static int[] marcadorDelClub(PartidoDeCompetencia partido) {
		int local = 0;
		int visitante = 0;
		if (partido.getResultado() instanceof ResultadoPorPuntos) {
			local = ((ResultadoPorPuntos) partido.getResultado()).getPuntosLocal();
			visitante = ((ResultadoPorPuntos) partido.getResultado()).getPuntosVisitante();
		} else if (partido.getResultado() instanceof ResultadoVoleibol) {
			local = ((ResultadoVoleibol) partido.getResultado()).getSetsGanadosLocal();
			visitante = ((ResultadoVoleibol) partido.getResultado()).getSetsGanadosVisitante();
		}
		return partido.isEsLocal() ? new int[] { local, visitante } : new int[] { visitante, local };
	}

	public int puntosPorMarcador(int aFavor, int enContra) {
		if (aFavor > enContra) {
			return 3;
		}
		if (aFavor == enContra && deporte.equalsIgnoreCase("FUTBOL")) {
			return 1;
		}
		return 0;
	}

	public boolean tieneResultados() {
		for (int i = 0; i < partidos.size(); i++) {
			if (partidos.get(i).getResultado() != null) {
				return true;
			}
		}
		return false;
	}

	public List<NominaCompetencia> ordenarTabla(List<NominaCompetencia> tabla) {
		List<NominaCompetencia> ordenada = new ArrayList<NominaCompetencia>(tabla);
		for (int i = 0; i < ordenada.size() - 1; i++) {
			for (int j = i + 1; j < ordenada.size(); j++) {
				if (vaAntes(ordenada.get(j), ordenada.get(i))) {
					NominaCompetencia temporal = ordenada.get(i);
					ordenada.set(i, ordenada.get(j));
					ordenada.set(j, temporal);
				}
			}
		}
		return ordenada;
	}

	private boolean vaAntes(NominaCompetencia a, NominaCompetencia b) {
		if (a.getPuntosAcumulados() != b.getPuntosAcumulados()) {
			return a.getPuntosAcumulados() > b.getPuntosAcumulados();
		}
		int directoA = puntosEnfrentamientoDirecto(a, b);
		int directoB = puntosEnfrentamientoDirecto(b, a);
		if (directoA != directoB) {
			return directoA > directoB;
		}
		return a.getDiferencia() > b.getDiferencia();
	}

	public int puntosEnfrentamientoDirecto(NominaCompetencia equipo, NominaCompetencia rival) {
		int puntos = 0;
		for (int i = 0; i < partidos.size(); i++) {
			PartidoDeCompetencia partido = partidos.get(i);
			if (partido.getResultado() == null) {
				continue;
			}
			int[] marcador = marcadorDelClub(partido);
			if (partido.getEquipoClub() == equipo.getEquipo() && rival.getEquipo().getNombre().equalsIgnoreCase(partido.getInstitucionRival())) {
				puntos = puntos + puntosPorMarcador(marcador[0], marcador[1]);
			} else if (partido.getEquipoClub() == rival.getEquipo() && equipo.getEquipo().getNombre().equalsIgnoreCase(partido.getInstitucionRival())) {
				puntos = puntos + puntosPorMarcador(marcador[1], marcador[0]);
			}
		}
		return puntos;
	}

	public boolean isEnCurso() {
		return !finalizada;
	}

	public void finalizar() {
		this.finalizada = true;
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