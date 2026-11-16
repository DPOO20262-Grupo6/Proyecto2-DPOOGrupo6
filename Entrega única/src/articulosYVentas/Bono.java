package articulosYVentas;

import java.io.Serializable;

public class Bono implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String codigo;
	private double porcentajeDescuento;
	private boolean usado;
	
	public Bono(String codigo, double porcentajeDescuento) {
		this.codigo = codigo;
		this.porcentajeDescuento = porcentajeDescuento;
		this.usado = false;
	}
	
	public boolean usar() {
		if (!usado) {
			usado = true;
			return true;
		}
		return false;
	}
	
	public boolean isUsado() {
		return usado;
	}
	
	public double getPorcentajeDescuento() {
		return porcentajeDescuento;
	}
	
	public String getCodigo() {
		return codigo;
	}
	
}
