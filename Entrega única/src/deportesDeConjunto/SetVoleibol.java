package deportesDeConjunto;

import java.io.Serializable;

public class SetVoleibol implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private int numeroSet;
	private int puntosLocal;
	private int puntosVisitante;

	public SetVoleibol(int numeroSet, int puntosLocal, int puntosVisitante) {
		if (puntosLocal == puntosVisitante) {
			throw new IllegalArgumentException("Un set no puede quedar empatado.");
		}
		this.numeroSet = numeroSet;
		this.puntosLocal = puntosLocal;
		this.puntosVisitante = puntosVisitante;
	}

	public String getGanadorSet() {
		if (puntosLocal > puntosVisitante) {
			return "LOCAL";
		}
		return "VISITANTE";
	}

	public int getNumeroSet() {
		return numeroSet;
	}

	public void setNumeroSet(int numeroSet) {
		this.numeroSet = numeroSet;
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
}