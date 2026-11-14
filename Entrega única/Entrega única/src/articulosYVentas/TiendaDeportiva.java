package articulosYVentas;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import inventario.UbicacionInventario;
import usuarios.EmpleadoDelClub;

public class TiendaDeportiva implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private UbicacionInventario ubicacionTienda;
	private List<ArticuloDeportivo> catalogo;
	private List<VentaTienda> ventas;
	private List<String> codigosEmpleadosValidos;

	public TiendaDeportiva(UbicacionInventario ubicacionTienda) {
		this.ubicacionTienda = ubicacionTienda;
		this.catalogo = new ArrayList<ArticuloDeportivo>();
		this.ventas = new ArrayList<VentaTienda>();
		this.codigosEmpleadosValidos = new ArrayList<String>();
	}

	public void agregarArticuloAlCatalogo(ArticuloDeportivo articulo) {
		if (!catalogo.contains(articulo)) {
			catalogo.add(articulo);
		}
	}

	public void registrarCodigoEmpleado(String codigo) {
		if (codigo != null && !codigosEmpleadosValidos.contains(codigo)) {
			codigosEmpleadosValidos.add(codigo);
		}
	}

	public boolean verificarExistencias(Articulo articulo, int cantidad) {
		return ubicacionTienda.obtenerCantidad(articulo) >= cantidad;
	}

	public void procesarVenta(VentaTienda venta, String codigoDescuento) {
		for (int i = 0; i < venta.getDetalles().size(); i++) {
			DetalleVenta detalle = venta.getDetalles().get(i);
			if (!verificarExistencias(detalle.getArticulo(), detalle.getCantidadArticulo())) {
				throw new IllegalStateException("No hay suficiente stock en tienda para " + detalle.getArticulo().getNombre());
			}
		}

		if (venta.getComprador() instanceof EmpleadoDelClub) {
			if (codigoDescuento != null && !codigoDescuento.equals("")) {
				throw new IllegalStateException("El descuento de empleado no es acumulable con otros codigos.");
			}
			venta.aplicarDescuento(EmpleadoDelClub.DESCUENTO_PROPIO);
		} else if (codigoDescuento != null && !codigoDescuento.equals("")) {
			if (!codigosEmpleadosValidos.contains(codigoDescuento)) {
				throw new IllegalArgumentException("El codigo de descuento no es valido.");
			}
			venta.aplicarDescuento(EmpleadoDelClub.DESCUENTO_TERCEROS);
		}

		for (int i = 0; i < venta.getDetalles().size(); i++) {
			DetalleVenta detalle = venta.getDetalles().get(i);
			ubicacionTienda.retirarExistencias(detalle.getArticulo(), detalle.getCantidadArticulo());
		}

		venta.calcularTotal();
		int generados = venta.calcularPuntosGenerados();
		if (venta.getPuntosFidelidadUtilizados() > 0) {
		    venta.getComprador().redimirPuntos(venta.getPuntosFidelidadUtilizados());
		}
		venta.getComprador().acumularPuntos(generados);
		ventas.add(venta);
	}

	public UbicacionInventario getUbicacionTienda() { return ubicacionTienda; }
	public void setUbicacionTienda(UbicacionInventario ubicacionTienda) { this.ubicacionTienda = ubicacionTienda; }
	public List<ArticuloDeportivo> getCatalogo() { return catalogo; }
	public void setCatalogo(List<ArticuloDeportivo> catalogo) { this.catalogo = catalogo; }
	public List<VentaTienda> getVentas() { return ventas; }
	public void setVentas(List<VentaTienda> ventas) { this.ventas = ventas; }
	public List<String> getCodigosEmpleadosValidos() { return codigosEmpleadosValidos; }
	public void setCodigosEmpleadosValidos(List<String> codigosEmpleadosValidos) { this.codigosEmpleadosValidos = codigosEmpleadosValidos; }
}