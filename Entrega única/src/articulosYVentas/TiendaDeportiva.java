package articulosYVentas;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import inventario.UbicacionInventario;
import usuarios.EmpleadoDelClub;
import usuarios.Usuario;

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

	public void desactivarCodigo(String codigo) {
		codigosEmpleadosValidos.remove(codigo);
	}

	public double resolverDescuento(Usuario comprador, String codigoDescuento) {
		boolean sinCodigo = codigoDescuento == null || codigoDescuento.trim().isEmpty();
		if (sinCodigo) {
			if (comprador instanceof EmpleadoDelClub && codigosEmpleadosValidos.contains(((EmpleadoDelClub) comprador).getCodigoDescuento())) {
				return EmpleadoDelClub.DESCUENTO_PROPIO;
			}
			return 0;
		}
		if (!codigosEmpleadosValidos.contains(codigoDescuento)) {
			throw new IllegalArgumentException("El codigo de descuento " + codigoDescuento + " no existe o esta inactivo.");
		}
		if (comprador instanceof EmpleadoDelClub && codigoDescuento.equals(((EmpleadoDelClub) comprador).getCodigoDescuento())) {
			return EmpleadoDelClub.DESCUENTO_PROPIO;
		}
		return EmpleadoDelClub.DESCUENTO_TERCEROS;
	}

	public void procesarVenta(VentaTienda venta, String codigoDescuento) {
		for (int i = 0; i < venta.getDetalles().size(); i++) {
			DetalleVenta detalle = venta.getDetalles().get(i);
			if (!catalogo.contains(detalle.getArticulo())) {
				throw new IllegalArgumentException("El articulo " + detalle.getArticulo().getNombre() + " no existe en el catalogo.");
			}
			if (!verificarExistencias(detalle.getArticulo(), detalle.getCantidadArticulo())) {
				throw new IllegalStateException("No hay suficiente stock en tienda para " + detalle.getArticulo().getNombre());
			}
		}
		venta.aplicarDescuentoValidandoRedencion(resolverDescuento(venta.getComprador(), codigoDescuento));
		for (int i = 0; i < venta.getDetalles().size(); i++) {
			DetalleVenta detalle = venta.getDetalles().get(i);
			ubicacionTienda.retirarExistencias(detalle.getArticulo(), detalle.getCantidadArticulo());
		}
		venta.registrarPuntos();
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