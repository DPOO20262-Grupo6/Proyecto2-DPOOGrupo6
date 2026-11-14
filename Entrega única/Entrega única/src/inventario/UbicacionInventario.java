package inventario;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import articulosYVentas.Articulo;

public class UbicacionInventario implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String nombre;
	private String tipo;
	private Map<Articulo, RegistroInventario> registros;

	public UbicacionInventario(String nombre, String tipo) {
		this.nombre = nombre;
		this.tipo = tipo;
		this.registros = new HashMap<Articulo, RegistroInventario>();
	}

	public int obtenerCantidad(Articulo articulo) {
		RegistroInventario registro = registros.get(articulo);
		if (registro != null) {
			return registro.getCantidad();
		}
		return 0;
	}

	public void agregarExistencias(Articulo articulo, int cantidad) {
		RegistroInventario registro = registros.get(articulo);
		if (registro == null) {
			registro = new RegistroInventario(articulo, 0);
			registros.put(articulo, registro);
		}
		registro.aumentar(cantidad);
	}

	public void retirarExistencias(Articulo articulo, int cantidad) {
		RegistroInventario registro = registros.get(articulo);
		if (registro == null) {
			throw new IllegalStateException("El articulo no existe en la ubicacion " + nombre);
		}
		registro.disminuir(cantidad);
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public Map<Articulo, RegistroInventario> getRegistros() {
		return registros;
	}

	public void setRegistros(Map<Articulo, RegistroInventario> registros) {
		this.registros = registros;
	}
}