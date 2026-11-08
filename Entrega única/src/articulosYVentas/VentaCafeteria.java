package articulosYVentas;

import java.time.LocalDate;
import usuarios.Usuario;

public class VentaCafeteria extends Venta {
	
	private static final long serialVersionUID = 1L;
	
	public static final double IMPUESTO_CONSUMO = 0.08;
	
	private double porcentajePropina = 0.10;

	public VentaCafeteria(LocalDate fecha, Usuario comprador) {
		super(fecha, comprador);
	}

	@Override
	public double calcularImpuestos() {
		return Math.round(getBaseGravable() * IMPUESTO_CONSUMO * 100.0) / 100.0;
	}

	public double getPorcentajePropina() {
		return porcentajePropina;
	}

	public void setPorcentajePropina(double porcentaje) {
		if (porcentaje < 0) {
			throw new IllegalArgumentException("La propina no puede ser negativa.");
		}
		this.porcentajePropina = porcentaje;
	}

	public double calcularPropina() {
		return Math.round(getBaseGravable() * porcentajePropina * 100.0) / 100.0;
	}

	@Override
	public double calcularTotal() {
		double total = getBaseGravable() + calcularImpuestos() + calcularPropina() - puntosFidelidadUtilizados;
		if (total < 0) {
			total = 0;
		}
		this.valorTotal = Math.round(total * 100.0) / 100.0;
		return this.valorTotal;
	}

	@Override
	public String toString() {
		return "Venta Cafeteria - Subtotal: $" + calcularSubtotal() + ", ImpConsumo: $" + calcularImpuestos() + ", Propina: $" + calcularPropina() + ", Total: $" + calcularTotal();
	}
}
