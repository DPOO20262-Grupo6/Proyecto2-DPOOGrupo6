package persistencia;

/** Error al leer o escribir los archivos de datos del club. */
public class PersistenciaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PersistenciaException(String mensaje, Throwable causa) {
		super(mensaje, causa);
	}
}
