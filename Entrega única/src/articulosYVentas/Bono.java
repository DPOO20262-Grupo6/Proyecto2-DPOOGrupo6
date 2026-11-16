package articulosYVentas;

import java.io.Serializable;

/**
 * Bono de descuento que gana el socio al ganar un torneo recreativo. Se guarda en el socio y se
 * redime una sola vez en una compra de la tienda o la cafeteria. Puede ser un porcentaje
 * (por ejemplo 10 %) o un valor fijo en pesos (por ejemplo $15000), como lo define el torneo.
 */
public class Bono implements Serializable {
	private static final long serialVersionUID = 1L;

	private String codigo;
	private double porcentajeDescuento;
	private double valorFijo;
	private boolean usado;

	public Bono(String codigo, double porcentajeDescuento) {
		this.codigo = codigo;
		this.porcentajeDescuento = porcentajeDescuento;
		this.usado = false;
	}

	/** Bono de un valor fijo en pesos: descuenta ese valor del subtotal de la compra. */
	public static Bono porValor(String codigo, double valor) {
		if (valor <= 0) {
			throw new IllegalArgumentException("El valor del bono debe ser mayor que cero.");
		}
		Bono bono = new Bono(codigo, 0);
		bono.valorFijo = valor;
		return bono;
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

	public boolean esPorValor() {
		return valorFijo > 0;
	}

	/** Texto corto para mostrar el bono: "$15000.0" o "10.0%". */
	public String describir() {
		if (esPorValor()) {
			return "$" + valorFijo;
		}
		return porcentajeDescuento + "%";
	}

	public double getPorcentajeDescuento() {
		return porcentajeDescuento;
	}

	public double getValorFijo() {
		return valorFijo;
	}

	public String getCodigo() {
		return codigo;
	}

}
