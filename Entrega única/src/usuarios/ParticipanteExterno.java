package usuarios;

/**
 * Persona que participa en los torneos sin ser socio: un estudiante matriculado en la universidad
 * del club o una persona externa. Se identifica por su documento, que se guarda como login.
 * No inicia sesion en ninguna consola.
 */
public class ParticipanteExterno extends Usuario{
	private static final long serialVersionUID = 1L;

	public static final String ESTUDIANTE_MATRICULADO = "ESTUDIANTE_MATRICULADO";
	public static final String EXTERNO = "EXTERNO";

	private String tipoParticipante;

	public ParticipanteExterno(String nombre, String login, String password, String tipoParticipante) {
		super(nombre, login, password);
		this.tipoParticipante = tipoParticipante;
	}

	public String getTipoParticipante() {
		return tipoParticipante;
	}

	public boolean esEstudiante() {
		return ESTUDIANTE_MATRICULADO.equals(tipoParticipante);
	}

	public String getDocumento() {
		return getLogin();
	}

}
