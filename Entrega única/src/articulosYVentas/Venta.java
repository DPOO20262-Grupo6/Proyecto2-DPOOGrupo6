package articulosYVentas;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import usuarios.Usuario;

public abstract class Venta implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final double PORCENTAJE_PUNTOS = 0.02;
	
	private LocalDate fecha;
	protected double valorTotal;
	protected double descuentoAplicado;
	protected int puntosFidelidadGenerados;
	protected int puntosFidelidadUtilizados;
	private Usuario comprador;
	protected List<DetalleVenta> detalles;

	public Venta(LocalDate fecha, Usuario comprador) {
		this.fecha = fecha;
		this.comprador = comprador;
		this.detalles = new ArrayList<DetalleVenta>();
	}

	public void agregarDetalle(Articulo articulo, int cantidad) {
		if (articulo == null) {
			throw new IllegalArgumentException("El articulo no existe.");
		}
		if (cantidad <= 0) {
			throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
		}
		detalles.add(new DetalleVenta(articulo, cantidad));
	}

	public void validarRedencion() {
		if (puntosFidelidadUtilizados <= 0) {
			return;
		}
		int saldo = comprador.getPuntosDeFidelidad();
		double totalAntesDeRedimir = getBaseGravable() + calcularImpuestos();
		if (puntosFidelidadUtilizados > saldo || puntosFidelidadUtilizados > totalAntesDeRedimir) {
			throw new IllegalArgumentException("No se pueden redimir " + puntosFidelidadUtilizados + " puntos. Saldo disponible: " + saldo + ".");
		}
	}

	public void aplicarDescuentoValidandoRedencion(double porcentaje) {
		double anterior = descuentoAplicado;
		if (porcentaje > 0) {
			aplicarDescuento(porcentaje);
		}
		try {
			validarRedencion();
		} catch (IllegalArgumentException e) {
			descuentoAplicado = anterior;
			throw e;
		}
	}

	public void registrarPuntos() {
		calcularTotal();
		int generados = calcularPuntosGenerados();
		if (puntosFidelidadUtilizados > 0) {
			comprador.redimirPuntos(puntosFidelidadUtilizados);
		}
		comprador.acumularPuntos(generados);
	}

	public double calcularSubtotal() {
		double sub = 0;
		for (int i = 0; i < detalles.size(); i++) {
			sub = sub + detalles.get(i).calcularSubtotal();
		}
		return sub;
	}

	public void aplicarDescuento(double porcentaje) {
		if (this.descuentoAplicado > 0) {
			throw new IllegalStateException("No se pueden acumular descuentos.");
		}
		this.descuentoAplicado = porcentaje;
	}

	public void redimirPuntos(int puntos) {
		if (puntos < 0) {
			throw new IllegalArgumentException("Los puntos no pueden ser negativos.");
		}
		this.puntosFidelidadUtilizados = puntos;
	}

	public double getBaseGravable() {
		return calcularSubtotal() * (1.0 - descuentoAplicado);
	}

	public abstract double calcularImpuestos();

	public double calcularTotalSinPropina() {
		double total = getBaseGravable() + calcularImpuestos() - puntosFidelidadUtilizados;
		if (total < 0) {
			total = 0;
		}
		return Math.round(total * 100.0) / 100.0;
	}

	public double calcularTotal() {
		this.valorTotal = calcularTotalSinPropina();
		return this.valorTotal;
	}

	public int calcularPuntosGenerados() {
		this.puntosFidelidadGenerados = (int) (calcularTotalSinPropina() * PORCENTAJE_PUNTOS);
		return this.puntosFidelidadGenerados;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public void setFecha(LocalDate fecha) {
		this.fecha = fecha;
	}

	public double getValorTotal() {
		return valorTotal;
	}

	public void setValorTotal(double valorTotal) {
		this.valorTotal = valorTotal;
	}

	public double getDescuentoAplicado() {
		return descuentoAplicado;
	}

	public void setDescuentoAplicado(double descuentoAplicado) {
		this.descuentoAplicado = descuentoAplicado;
	}

	public int getPuntosFidelidadGenerados() {
		return puntosFidelidadGenerados;
	}

	public void setPuntosFidelidadGenerados(int puntosFidelidadGenerados) {
		this.puntosFidelidadGenerados = puntosFidelidadGenerados;
	}

	public int getPuntosFidelidadUtilizados() {
		return puntosFidelidadUtilizados;
	}

	public void setPuntosFidelidadUtilizados(int puntosFidelidadUtilizados) {
		this.puntosFidelidadUtilizados = puntosFidelidadUtilizados;
	}

	public Usuario getComprador() {
		return comprador;
	}

	public void setComprador(Usuario comprador) {
		this.comprador = comprador;
	}

	public List<DetalleVenta> getDetalles() {
		return detalles;
	}

	public void setDetalles(List<DetalleVenta> detalles) {
		this.detalles = detalles;
	}
}