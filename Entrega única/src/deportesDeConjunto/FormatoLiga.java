package deportesDeConjunto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FormatoLiga implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Competencia competencia;
	private String tablaDePosiciones;

	public FormatoLiga(Competencia competencia) {
		this.competencia = competencia;
		this.tablaDePosiciones = "Sin disputar";
	}

	public PartidoDeCompetencia programarPartido(Equipo equipo, String rival, LocalDate fecha, String escenario) {
		PartidoDeCompetencia partido = new PartidoDeCompetencia(fecha, equipo.getDeporte(), competencia, equipo, rival, escenario, true);
		competencia.getPartidos().add(partido);
		return partido;
	}

	public void actualizarTabla(PartidoDeCompetencia partido) {
		competencia.actualizarTabla(partido);
	}

	public List<NominaCompetencia> getTablaOrdenada() {
		return competencia.ordenarTabla(competencia.getNominas());
	}

	public List<NominaCompetencia> getTablaOrdenada(String grupo) {
		List<NominaCompetencia> delGrupo = new ArrayList<NominaCompetencia>();
		for (int i = 0; i < competencia.getNominas().size(); i++) {
			NominaCompetencia nomina = competencia.getNominas().get(i);
			if (grupo != null && grupo.equalsIgnoreCase(nomina.getGrupo())) {
				delGrupo.add(nomina);
			}
		}
		return competencia.ordenarTabla(delGrupo);
	}

	public Competencia getCompetencia() {
		return competencia;
	}

	public void setCompetencia(Competencia competencia) {
		this.competencia = competencia;
	}

	public String getTablaDePosiciones() {
		return tablaDePosiciones;
	}

	public void setTablaDePosiciones(String tablaDePosiciones) {
		this.tablaDePosiciones = tablaDePosiciones;
	}
}