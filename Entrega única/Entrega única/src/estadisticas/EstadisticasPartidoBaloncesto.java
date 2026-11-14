package estadisticas;

public class EstadisticasPartidoBaloncesto extends EstadisticasPartidoDeEquipo {
	
	private static final long serialVersionUID = 1L;
	
	private int puntos;
	private int rebotes;

	public EstadisticasPartidoBaloncesto() {
		this.puntos = 0;
		this.rebotes = 0;
	}

	public void registrarPuntos(int cantidad) {
		if (cantidad < 0) {
			throw new IllegalArgumentException("Los puntos no pueden ser negativos.");
		}
		this.puntos = this.puntos + cantidad;
	}

	public void registrarRebote() {
		this.rebotes = this.rebotes + 1;
	}

	public int getPuntos() { return puntos; }
	public void setPuntos(int puntos) { this.puntos = puntos; }
	public int getRebotes() { return rebotes; }
	public void setRebotes(int rebotes) { this.rebotes = rebotes; }

	@Override
	public String getResumen() {
		return "Puntos: " + puntos + ", Rebotes: " + rebotes;
	}
}