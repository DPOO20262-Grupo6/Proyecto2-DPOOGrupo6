package deportesDeConjunto;

public class ResultadoPorPuntos extends ResultadoPartidoEquipo {
	
	private static final long serialVersionUID = 1L;
	
	private int puntosLocal;
	private int puntosVisitante;

	public ResultadoPorPuntos(int puntosLocal, int puntosVisitante) {
		this.puntosLocal = puntosLocal;
		this.puntosVisitante = puntosVisitante;
	}

	@Override
	public String getGanador() {
		if (puntosLocal > puntosVisitante) {
			return "LOCAL";
		} else if (puntosVisitante > puntosLocal) {
			return "VISITANTE";
		}
		return "EMPATE";
	}

	@Override
	public boolean esEmpate() {
		return puntosLocal == puntosVisitante;
	}

	public int getPuntosLocal() {
		return puntosLocal;
	}

	public void setPuntosLocal(int puntosLocal) {
		this.puntosLocal = puntosLocal;
	}

	public int getPuntosVisitante() {
		return puntosVisitante;
	}

	public void setPuntosVisitante(int puntosVisitante) {
		this.puntosVisitante = puntosVisitante;
	}

	@Override
	public String toString() {
		return puntosLocal + " - " + puntosVisitante;
	}
}