package deportesDeConjunto;

import java.io.Serializable;
import java.time.LocalDate;

public class FaseEliminacionDirecta implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Competencia competencia;

	public FaseEliminacionDirecta(Competencia competencia) {
		this.competencia = competencia;
	}

	public PartidoDeCompetencia programarLlave(Equipo equipo, String rival, LocalDate fecha, String escenario) {
		PartidoDeCompetencia partido = new PartidoDeCompetencia(fecha, equipo.getDeporte(), competencia, equipo, rival, escenario, true);
		competencia.getPartidos().add(partido);
		return partido;
	}

	public Equipo obtenerGanador(PartidoDeCompetencia partido) {
		if (partido.getResultado() == null || partido.getResultado().esEmpate()) {
			throw new IllegalStateException("El partido debe tener un ganador definido.");
		}
		if ("LOCAL".equals(partido.getResultado().getGanador())) {
			return partido.getEquipoClub();
		}
		return null;
	}

	public Competencia getCompetencia() {
		return competencia;
	}

	public void setCompetencia(Competencia competencia) {
		this.competencia = competencia;
	}
}