package estadisticas;

import java.util.ArrayList;
import java.util.List;
import enumeraciones.TipoTarjeta;

public class EstadisticasPartidoFutbol extends EstadisticasPartidoDeEquipo {
	
	private static final long serialVersionUID = 1L;
	
	private int goles;
	private int asistencias;
	private List<Tarjeta> tarjetas;

	public EstadisticasPartidoFutbol() {
		this.goles = 0;
		this.asistencias = 0;
		this.tarjetas = new ArrayList<Tarjeta>();
	}

	public void registrarGol() {
		this.goles = this.goles + 1;
	}

	public void registrarAsistencia() {
		this.asistencias = this.asistencias + 1;
	}

	public Tarjeta registrarTarjeta(TipoTarjeta tipo, int minuto) {
		Tarjeta t = new Tarjeta(tipo, minuto);
		tarjetas.add(t);
		return t;
	}

	public int getTarjetasAmarillas() {
		int contador = 0;
		for (int i = 0; i < tarjetas.size(); i++) {
			if (!tarjetas.get(i).esRoja()) {
				contador = contador + 1;
			}
		}
		return contador;
	}

	public int getTarjetasRojas() {
		int contador = 0;
		for (int i = 0; i < tarjetas.size(); i++) {
			if (tarjetas.get(i).esRoja()) {
				contador = contador + 1;
			}
		}
		return contador;
	}

	public int getGoles() { return goles; }
	public void setGoles(int goles) { this.goles = goles; }
	public int getAsistencias() { return asistencias; }
	public void setAsistencias(int asistencias) { this.asistencias = asistencias; }
	public List<Tarjeta> getTarjetas() { return tarjetas; }

	@Override
	public String getResumen() {
		return "Goles: " + goles + ", Asistencias: " + asistencias + ", Amarillas: " + getTarjetasAmarillas() + ", Rojas: " + getTarjetasRojas();
	}
}