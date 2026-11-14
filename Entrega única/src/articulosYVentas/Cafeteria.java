package articulosYVentas;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import instalaciones.CanchaDeporteIndividual;
import instalaciones.InstalacionDeportiva;

public class Cafeteria implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<Bebida> bebidas;
	private List<Snack> snacks;
	private List<VentaCafeteria> ventas;

	public Cafeteria() {
		this.bebidas = new ArrayList<Bebida>();
		this.snacks = new ArrayList<Snack>();
		this.ventas = new ArrayList<VentaCafeteria>();
	}

	public void agregarProducto(Articulo producto) {
		if (producto instanceof Bebida) {
			bebidas.add((Bebida) producto);
		} else if (producto instanceof Snack) {
			snacks.add((Snack) producto);
		}
	}

	public String informarAlergenos(List<Articulo> productos) {
		String info = "Alergenos: ";
		for (int i = 0; i < productos.size(); i++) {
			Articulo a = productos.get(i);
			if (a instanceof Snack) {
				Snack s = (Snack) a;
				info = info + s.getNombre() + " " + s.getAlergenos().toString() + " ";
			}
		}
		return info;
	}

	public void procesarVenta(VentaCafeteria venta, InstalacionDeportiva instalacionDelComprador) {
		for (int i = 0; i < venta.getDetalles().size(); i++) {
			DetalleVenta detalle = venta.getDetalles().get(i);
			if (detalle.getArticulo() instanceof Bebida) {
				Bebida bebida = (Bebida) detalle.getArticulo();
				if (bebida.esCaliente() && instalacionDelComprador instanceof CanchaDeporteIndividual) {
					CanchaDeporteIndividual canchaIndividual = (CanchaDeporteIndividual) instalacionDelComprador;
					if (!canchaIndividual.permiteBebidaCaliente()) {
						throw new IllegalStateException("No se permite despachar bebidas calientes en " + canchaIndividual.getNombre());
					}
				}
			}
		}
		
		venta.calcularTotal();
		int generados = venta.calcularPuntosGenerados();
		if (venta.getPuntosFidelidadUtilizados() > 0) {
		    venta.getComprador().redimirPuntos(venta.getPuntosFidelidadUtilizados());
		}
		venta.getComprador().acumularPuntos(generados);
		ventas.add(venta);
	}

	public List<VentaCafeteria> getVentas() { return ventas; }
	public List<Snack> getSnacks() { return snacks; }
	public List<Bebida> getBebidas() { return bebidas; }
}
