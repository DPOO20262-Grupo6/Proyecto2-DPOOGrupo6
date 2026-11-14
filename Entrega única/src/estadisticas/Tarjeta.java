package estadisticas;

import java.io.Serializable;
import enumeraciones.TipoTarjeta;

public class Tarjeta implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private TipoTarjeta tipo;
	private int minuto;

	public Tarjeta(TipoTarjeta tipo, int minuto) {
		this.tipo = tipo;
		this.minuto = minuto;
	}

	public boolean esRoja() {
		return tipo == TipoTarjeta.ROJA;
	}

	public TipoTarjeta getTipo() { return tipo; }
	public void setTipo(TipoTarjeta tipo) { this.tipo = tipo; }
	public int getMinuto() { return minuto; }
	public void setMinuto(int minuto) { this.minuto = minuto; }
}