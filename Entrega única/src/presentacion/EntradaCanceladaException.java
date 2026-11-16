package presentacion;

/**
 * El usuario escribio "cancelar" en medio de una operacion, o la entrada se cerro.
 * La operacion en curso se abandona sin cambiar nada.
 */
public class EntradaCanceladaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private boolean finDeEntrada;

	public EntradaCanceladaException(String mensaje, boolean finDeEntrada) {
		super(mensaje);
		this.finDeEntrada = finDeEntrada;
	}

	/** Verdadero cuando ya no hay mas entrada que leer: la consola debe terminar. */
	public boolean esFinDeEntrada() {
		return finDeEntrada;
	}
}
