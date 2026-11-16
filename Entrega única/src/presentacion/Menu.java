package presentacion;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu numerado de una consola. Muestra las opciones, valida la eleccion, ejecuta la accion
 * y vuelve a mostrarse hasta que el usuario elige salir (opcion 0). Si una accion es rechazada
 * por la logica, muestra el motivo y sigue: un rechazo nunca cierra la consola.
 * Para agregar una funcionalidad a una consola basta con agregar una opcion.
 */
public class Menu {

	public static final int OPCION_SALIR = 0;

	private String titulo;
	private String textoSalida;
	private List<OpcionMenu> opciones;

	public Menu(String titulo, String textoSalida) {
		this.titulo = titulo;
		this.textoSalida = textoSalida;
		this.opciones = new ArrayList<OpcionMenu>();
	}

	public Menu agregarOpcion(String texto, AccionMenu accion) {
		opciones.add(new OpcionMenu(texto, accion));
		return this;
	}

	/** Agrega una opcion que abre otro menu; al salir de el se regresa a este. */
	public Menu agregarSubmenu(String texto, Menu submenu, LectorEntradas lector) {
		return agregarOpcion(texto, () -> submenu.ejecutar(lector));
	}

	public void ejecutar(LectorEntradas lector) {
		boolean continuar = true;
		while (continuar) {
			mostrar(lector);
			int eleccion;
			try {
				eleccion = lector.leerEntero("Seleccione una opcion: ", OPCION_SALIR, opciones.size());
			} catch (EntradaCanceladaException e) {
				if (e.esFinDeEntrada()) {
					throw e;
				}
				continue;
			}
			lector.mostrar("");
			if (eleccion == OPCION_SALIR) {
				continuar = false;
			} else {
				ejecutarOpcion(opciones.get(eleccion - 1), lector);
				lector.mostrar("");
			}
		}
	}

	private void mostrar(LectorEntradas lector) {
		lector.mostrar("===== " + titulo + " =====");
		for (int i = 0; i < opciones.size(); i++) {
			lector.mostrar((i + 1) + ". " + opciones.get(i).getTexto());
		}
		lector.mostrar(OPCION_SALIR + ". " + textoSalida);
	}

	private void ejecutarOpcion(OpcionMenu opcion, LectorEntradas lector) {
		try {
			opcion.getAccion().ejecutar();
		} catch (EntradaCanceladaException e) {
			if (e.esFinDeEntrada()) {
				throw e;
			}
			lector.mostrar("Operacion cancelada. No se hizo ningun cambio.");
		} catch (IllegalArgumentException | IllegalStateException e) {
			lector.mostrar("No se pudo completar la operacion: " + e.getMessage());
		} catch (Exception e) {
			lector.mostrar("Error inesperado: " + e.getMessage());
		}
	}

	public String getTitulo() {
		return titulo;
	}

	public List<OpcionMenu> getOpciones() {
		return opciones;
	}
}
