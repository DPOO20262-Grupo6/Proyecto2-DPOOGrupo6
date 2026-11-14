package deportesDeConjunto;

import java.util.ArrayList;
import java.util.List;

public class Ascun extends Competencia {
	
	private static final long serialVersionUID = 1L;
	
	private String faseActual;
	private FormatoLiga faseGrupos;
	private FaseEliminacionDirecta faseEliminacion;

	public Ascun(String nombre, int añoTemporada, String deporte, String categoriaHabilitada, int jugadoresMinimosNomina) {
		super(nombre, "ASCUN", añoTemporada, deporte, categoriaHabilitada, jugadoresMinimosNomina);
		this.faseActual = "GRUPOS";
		this.faseGrupos = new FormatoLiga(this);
		this.faseEliminacion = new FaseEliminacionDirecta(this);
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