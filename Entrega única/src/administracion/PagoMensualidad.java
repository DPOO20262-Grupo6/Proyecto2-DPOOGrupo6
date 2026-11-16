package administracion;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.YearMonth;
import usuarios.Socio;

public class PagoMensualidad implements Serializable {


	private Socio socio;
	private YearMonth mes;
	private double valor;
	private LocalDate fechaPago;

	public PagoMensualidad(Socio socio, YearMonth mes, double valor) {
		this.socio = socio;
		this.mes = mes;
		this.valor = valor;
		this.fechaPago = LocalDate.now();
	}

	public Socio getSocio() { return socio; }
	public YearMonth getMes() { return mes; }
	public double getValor() { return valor; }
	public LocalDate getFechaPago() { return fechaPago; }

	@Override
	public String toString() {
		return "Mensualidad " + mes + " - " + socio.getNombre() + " - $" + valor;
	}
}
