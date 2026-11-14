package estadisticas;

public class EstadisticasPartidoPadel extends EstadisticasPartidoIndividual {
	
	private static final long serialVersionUID = 1L;
	
	private int setsGanados;
	private int gamesGanados;
	private int aces;
	private int erroresNoForzados;

	public EstadisticasPartidoPadel() { }

	public EstadisticasPartidoPadel(int setsGanados, int gamesGanados, int aces, int erroresNoForzados, boolean ganador) {
		this.setsGanados = setsGanados;
		this.gamesGanados = gamesGanados;
		this.aces = aces;
		this.erroresNoForzados = erroresNoForzados;
		this.ganador = ganador;
	}

	public int getSetsGanados() { return setsGanados; }
	public void setSetsGanados(int setsGanados) { this.setsGanados = setsGanados; }
	public int getGamesGanados() { return gamesGanados; }
	public void setGamesGanados(int gamesGanados) { this.gamesGanados = gamesGanados; }
	public int getAces() { return aces; }
	public void setAces(int aces) { this.aces = aces; }
	public int getErroresNoForzados() { return erroresNoForzados; }
	public void setErroresNoForzados(int erroresNoForzados) { this.erroresNoForzados = erroresNoForzados; }

	@Override
	public String getResumen() {
		return "Sets: " + setsGanados + ", Games: " + gamesGanados + ", Aces: " + aces + ", Errores: " + erroresNoForzados;
	}

	@Override
	public boolean esGanador() { return ganador; }
}