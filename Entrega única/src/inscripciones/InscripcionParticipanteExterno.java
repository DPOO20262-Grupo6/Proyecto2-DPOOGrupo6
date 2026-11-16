package inscripciones;

import articulosYVentas.Bono;
import enumeraciones.NivelHabilidad;
import usuarios.ParticipanteExterno;
import usuarios.Socio;
import usuarios.Usuario;

/**
 * Inscripcion de un estudiante matriculado o de una persona externa en un torneo individual.
 * Igual que la del empleado, reutiliza los partidos y la tabla de posiciones de la inscripcion de
 * disciplina. A diferencia del empleado, si paga la tarifa y si puede ganar el premio en efectivo.
 * No tiene cuenta de socio, asi que no guarda bonos. La inscribe un socio, que es quien puede retirarla.
 */
public class InscripcionParticipanteExterno extends InscripcionDisciplinaIndividual {

	private static final long serialVersionUID = 1L;

	public static final NivelHabilidad NIVEL_PARTICIPANTE = NivelHabilidad.INTERMEDIO;

	private ParticipanteExterno participante;
	private String loginSocioResponsable;

	public InscripcionParticipanteExterno(ParticipanteExterno participante, String disciplina, String loginSocioResponsable) {
		super(crearFichaDeJugador(participante), disciplina, NIVEL_PARTICIPANTE);
		this.participante = participante;
		this.loginSocioResponsable = loginSocioResponsable;
	}

	/** Los partidos identifican al jugador por un Socio: se crea una ficha que no se registra en el club. */
	private static Socio crearFichaDeJugador(ParticipanteExterno participante) {
		if (participante == null) {
			throw new IllegalArgumentException("Debe indicar el participante que se inscribe.");
		}
		String etiqueta = participante.esEstudiante() ? " (estudiante)" : " (externo)";
		return new Socio(participante.getNombre() + etiqueta, participante.getLogin(), "", null);
	}

	@Override
	public boolean correspondeA(Usuario usuario) {
		return usuario instanceof ParticipanteExterno && usuario.getLogin().equalsIgnoreCase(participante.getLogin());
	}

	@Override
	public void recibirBono(Bono bono) {
	}

	public ParticipanteExterno getParticipante() {
		return participante;
	}

	/** Login del socio que inscribio a esta persona. */
	public String getLoginSocioResponsable() {
		return loginSocioResponsable;
	}
}
