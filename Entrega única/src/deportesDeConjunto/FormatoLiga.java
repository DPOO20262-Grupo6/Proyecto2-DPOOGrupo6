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
		NominaCompetencia nomina = competencia.buscarNomina(partido.getEquipoClub());
		
		if (nomina != null && partido.getResultado() != null) {
			int pts = calcularPuntos(partido);
			nomina.sumarPuntos(pts);
			
			if ("LOCAL".equals(partido.getResultado().getGanador())) {
				nomina.registrarPartidoGanado();
			}
		}
	}

	private int calcularPuntos(PartidoDeCompetencia partido) {
		String deporte = partido.getDeporte();
		boolean ganoLocal = "LOCAL".equals(partido.getResultado().getGanador());
		boolean esEmpate = partido.getResultado().esEmpate();
		
		if (deporte.equalsIgnoreCase("FUTBOL")) {
			if (esEmpate) return 1;
			if (ganoLocal) return 3;
			return 0;
		} else if (deporte.equalsIgnoreCase("BALONCESTO")) {
			if (ganoLocal) return 2;
			return 1;
		} else if (deporte.equalsIgnoreCase("VOLEIBOL")) {
			if (ganoLocal) return 2;
			return 1;
		}
		return 0;
	}

	public List<NominaCompetencia> getTablaOrdenada() {
		List<NominaCompetencia> ordenada = new ArrayList<NominaCompetencia>(competencia.getNominas());
		
		for (int i = 0; i < ordenada.size() - 1; i++) {
			for (int j = i + 1; j < ordenada.size(); j++) {
				NominaCompetencia n1 = ordenada.get(i);
				NominaCompetencia n2 = ordenada.get(j);
				if (n2.getPuntosAcumulados() > n1.getPuntosAcumulados()) {
					ordenada.set(i, n2);
					ordenada.set(j, n1);
				}
			}
		}
		return ordenada;
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