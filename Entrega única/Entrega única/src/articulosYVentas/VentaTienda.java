package articulosYVentas;

import java.time.LocalDate;
import usuarios.Usuario;

public class VentaTienda extends Venta {
	
	private static final long serialVersionUID = 1L;
	
	public static final double IVA = 0.19;

	public VentaTienda(LocalDate fecha, Usuario comprador) {
		super(fecha, comprador);
	}

	@Override
	public double calcularImpuestos() {
		return Math.round(getBaseGravable() * IVA * 100.0) / 100.0;
	}

	@Override
	public String toString() {
		return "Venta Tienda - Subtotal: $" + calcularSubtotal() + ", IVA: $" + calcularImpuestos() + ", Total: $" + calcularTotal();
	}
}