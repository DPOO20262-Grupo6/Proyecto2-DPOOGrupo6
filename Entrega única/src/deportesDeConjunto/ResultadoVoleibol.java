package deportesDeConjunto;

import java.util.ArrayList;
import java.util.List;

public class ResultadoVoleibol extends ResultadoPartidoEquipo {
	
	private static final long serialVersionUID = 1L;
	
	private List<SetVoleibol> sets;

	public ResultadoVoleibol() {
		this.sets = new ArrayList<SetVoleibol>();
	}

	public void agregarSet(SetVoleibol set) {
		if (set != null) {
			this.sets.add(set);
		}
	}

	public int getSetsGanadosLocal() {
		int contador = 0;
		for (int i = 0; i < sets.size(); i++) {
			if ("LOCAL".equals(sets.get(i).getGanadorSet())) {
				contador = contador + 1;
			}
		}
		return contador;
	}

	public int getSetsGanadosVisitante() {
		int contador = 0;
		for (int i = 0; i < sets.size(); i++) {
			if ("VISITANTE".equals(sets.get(i).getGanadorSet())) {
				contador = contador + 1;
			}
		}
		return contador;
	}

	@Override
	public String getGanador() {
		if (esEmpate()) {
			return "EMPATE";
		}
		if (getSetsGanadosLocal() > getSetsGanadosVisitante()) {
			return "LOCAL";
		}
		return "VISITANTE";
	}

	@Override
	public boolean esEmpate() {
		return getSetsGanadosLocal() == getSetsGanadosVisitante();
	}

	public List<SetVoleibol> getSets() {
		return sets;
	}

	public void setSets(List<SetVoleibol> sets) {
		this.sets = sets;
	}

	@Override
	public String toString() {
		return getSetsGanadosLocal() + " - " + getSetsGanadosVisitante();
	}
}