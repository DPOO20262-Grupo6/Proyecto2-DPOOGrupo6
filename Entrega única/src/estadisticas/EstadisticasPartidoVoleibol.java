package estadisticas;

public class EstadisticasPartidoVoleibol extends EstadisticasPartidoDeEquipo {
	
	private static final long serialVersionUID = 1L;
	
	private int aces;
	private int bloqueos;

	public EstadisticasPartidoVoleibol() {
		this.aces = 0;
		this.bloqueos = 0;
	}

	public void registrarAce() {
		this.aces = this.aces + 1;
	}

	public void registrarBloqueo() {
		this.bloqueos = this.bloqueos + 1;
	}

	public int getAces() { return aces; }
	public void setAces(int aces) { this.aces = aces; }
	public int getBloqueos() { return bloqueos; }
	public void setBloqueos(int bloqueos) { this.bloqueos = bloqueos; }

	@Override
	public String getResumen() {
		return "Aces: " + aces + ", Bloqueos: " + bloqueos;
	}
}