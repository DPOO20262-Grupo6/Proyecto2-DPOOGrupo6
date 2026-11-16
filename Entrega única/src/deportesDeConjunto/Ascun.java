package deportesDeConjunto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Ascun extends Competencia {
	
	private static final long serialVersionUID = 1L;

	public static final int CLASIFICADOS_POR_GRUPO = 2;
	
	private String faseActual;
	private FormatoLiga faseGrupos;
	private FaseEliminacionDirecta faseEliminacion;

	public Ascun(String nombre, int añoTemporada, String deporte, String categoriaHabilitada, int jugadoresMinimosNomina) {
		super(nombre, "ASCUN", añoTemporada, deporte, categoriaHabilitada, jugadoresMinimosNomina);
		this.faseActual = "GRUPOS";
		this.faseGrupos = new FormatoLiga(this);
		this.faseEliminacion = new FaseEliminacionDirecta(this);
	}

	public Ascun(String nombre, String entidadOrganizadora, int anioTemporada, String deporte, String categoriaHabilitada, int jugadoresMinimosNomina) {
		super(nombre, entidadOrganizadora, anioTemporada, deporte, categoriaHabilitada, jugadoresMinimosNomina);
		this.faseActual = "GRUPOS";
		this.faseGrupos = new FormatoLiga(this);
		this.faseEliminacion = new FaseEliminacionDirecta(this);
	}

	@Override
	public String getFormato() {
		return "GRUPOS";
	}

	@Override
	protected PartidoDeCompetencia crearPartido(Equipo equipo, String rival, LocalDate fecha, String escenario) {
		if ("GRUPOS".equals(faseActual)) {
			return faseGrupos.programarPartido(equipo, rival, fecha, escenario);
		}
		return faseEliminacion.programarLlave(equipo, rival, fecha, escenario);
	}

	public void asignarGrupo(Equipo equipo, String grupo) {
		NominaCompetencia nomina = buscarNomina(equipo);
		if (nomina == null) {
			throw new IllegalArgumentException("El equipo no esta inscrito en la competencia.");
		}
		if (grupo == null || grupo.trim().isEmpty()) {
			throw new IllegalArgumentException("Debe indicar el grupo.");
		}
		nomina.setGrupo(grupo.trim().toUpperCase());
	}

	public List<NominaCompetencia> getTablaGrupo(String grupo) {
		return faseGrupos.getTablaOrdenada(grupo);
	}

	public List<Equipo> getClasificados(String grupo) {
		List<Equipo> clasificados = new ArrayList<Equipo>();
		List<NominaCompetencia> tabla = getTablaGrupo(grupo);
		for (int i = 0; i < tabla.size() && i < CLASIFICADOS_POR_GRUPO; i++) {
			clasificados.add(tabla.get(i).getEquipo());
		}
		return clasificados;
	}

	public void avanzarFase() {
		this.faseActual = "ELIMINACION_DIRECTA";
	}

	public List<Equipo> definirClasificados() {
		List<Equipo> clasificados = new ArrayList<Equipo>();
		List<NominaCompetencia> tabla = faseGrupos.getTablaOrdenada();
		for (int i = 0; i < tabla.size(); i++) {
			clasificados.add(tabla.get(i).getEquipo());
		}
		return clasificados;
	}

	public String getFaseActual() {
		return faseActual;
	}

	public void setFaseActual(String faseActual) {
		this.faseActual = faseActual;
	}

	public FormatoLiga getFaseGrupos() {
		return faseGrupos;
	}

	public void setFaseGrupos(FormatoLiga faseGrupos) {
		this.faseGrupos = faseGrupos;
	}

	public FaseEliminacionDirecta getFaseEliminacion() {
		return faseEliminacion;
	}

	public void setFaseEliminacion(FaseEliminacionDirecta faseEliminacion) {
		this.faseEliminacion = faseEliminacion;
	}
}