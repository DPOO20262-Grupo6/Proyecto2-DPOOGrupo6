package inscripciones;

import enumeraciones.NivelHabilidad;
import usuarios.EmpleadoDelClub;
import usuarios.Socio;
import usuarios.Usuario;

/**
 * Inscripcion de un empleado (entrenador o fisioterapeuta) en un torneo individual.
 * Hereda de la inscripcion de disciplina para reutilizar los partidos, el ranking y la
 * tabla de posiciones del torneo, y redefine las dos reglas propias del empleado:
 * no paga la inscripcion y no recibe el premio en efectivo.
 */
public class InscripcionEmpleadoTorneo extends InscripcionDisciplinaIndividual {

	private static final long serialVersionUID = 1L;

	/** INTERMEDIO es adyacente a los otros dos niveles: el empleado puede enfrentar a cualquier inscrito. */
	public static final NivelHabilidad NIVEL_EMPLEADO = NivelHabilidad.INTERMEDIO;
	public static final double VALOR_INSCRIPCION = 0;
	public static final String ETIQUETA = " (empleado)";

	private EmpleadoDelClub empleado;

	public InscripcionEmpleadoTorneo(EmpleadoDelClub empleado, String disciplina) {
		super(crearFichaDeJugador(empleado), disciplina, NIVEL_EMPLEADO);
		this.empleado = empleado;
	}

	/**
	 * Los partidos y la tabla de posiciones del Proyecto 1 identifican a cada jugador por un Socio.
	 * El empleado no es socio, asi que se crea una ficha de jugador con su nombre y su login.
	 * La ficha no se registra en el club: no sirve para iniciar sesion ni aparece entre los socios.
	 */
	private static Socio crearFichaDeJugador(EmpleadoDelClub empleado) {
		if (empleado == null) {
			throw new IllegalArgumentException("Debe indicar el empleado que se inscribe.");
		}
		return new Socio(empleado.getNombre() + ETIQUETA, empleado.getLogin(), "", null);
	}

	@Override
	public boolean correspondeA(Usuario usuario) {
		return usuario instanceof EmpleadoDelClub && usuario.getLogin().equalsIgnoreCase(empleado.getLogin());
	}

	@Override
	public boolean pagaInscripcionTorneo() {
		return false;
	}

	@Override
	public boolean recibePremioEnEfectivo() {
		return false;
	}

	public double getValorPagado() {
		return VALOR_INSCRIPCION;
	}

	public EmpleadoDelClub getEmpleado() {
		return empleado;
	}
}
