package deportesDeConjunto;

import java.util.List;

public class TorneoDeLosCerros extends Competencia {
	
	private static final long serialVersionUID = 1L;
	
	private FormatoLiga formatoLiga;

	public TorneoDeLosCerros(String nombre, int anioTemporada, String deporte, String categoriaHabilitada, int jugadoresMinimosNomina) {
		super(nombre, "Torneo de los Cerros", anioTemporada, deporte, categoriaHabilitada, jugadoresMinimosNomina);
		this.formatoLiga = new FormatoLiga(this);
	}

	public List<NominaCompetencia> getTablaPosiciones() {
		return formatoLiga.getTablaOrdenada();
	}

	public FormatoLiga getFormatoLiga() {
		return formatoLiga;
	}

	public void setFormatoLiga(FormatoLiga formatoLiga) {
		this.formatoLiga = formatoLiga;
	}
}