package presentacion;

/** Una linea de un menu: el texto que se muestra y la accion que ejecuta. */
public class OpcionMenu {

	private String texto;
	private AccionMenu accion;

	public OpcionMenu(String texto, AccionMenu accion) {
		if (texto == null || texto.trim().isEmpty() || accion == null) {
			throw new IllegalArgumentException("Una opcion de menu necesita texto y accion.");
		}
		this.texto = texto;
		this.accion = accion;
	}

	public String getTexto() {
		return texto;
	}

	public AccionMenu getAccion() {
		return accion;
	}
}
