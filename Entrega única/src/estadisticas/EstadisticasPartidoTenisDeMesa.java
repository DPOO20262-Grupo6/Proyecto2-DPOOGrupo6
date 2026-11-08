package estadisticas;

public class EstadisticasPartidoTenisDeMesa extends EstadisticasPartidoIndividual {
	
	private static final long serialVersionUID = 1L;
	
	private int setsGanados;
	private int puntosTotales;

	public EstadisticasPartidoTenisDeMesa() { }

	public EstadisticasPartidoTenisDeMesa(int setsGanados, int puntosTotales, boolean ganador) {
		this.setsGanados = setsGanados;
		this.puntosTotales = puntosTotales;
		this.ganador = ganador;
	}

	public int getSetsGanados() { return setsGanados; }
	public void setSetsGanados(int setsGanados) { this.setsGanados = setsGanados; }
	public int getPuntosTotales() { return puntosTotales; }
	public void setPuntosTotales(int puntosTotales) { this.puntosTotales = puntosTotales; }

	@Override
	public String getResumen() {
		return "Sets: " + setsGanados + ", Puntos: " + puntosTotales;
	}

	@Override
	public boolean esGanador() { return ganador; }
}