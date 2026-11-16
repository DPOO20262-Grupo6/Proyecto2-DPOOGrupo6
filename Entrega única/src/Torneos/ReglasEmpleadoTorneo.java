package Torneos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import administracion.TurnoEmpleado;
import club.ClubDeportivoSeneca;
import enumeraciones.EstadoTorneo;
import inscripciones.InscripcionDisciplinaIndividual;
import inscripciones.InscripcionEmpleadoTorneo;
import usuarios.EmpleadoDelClub;
import usuarios.Usuario;

/**
 * Reglas de participacion de los empleados (entrenadores y fisioterapeutas) en los torneos:
 * solo pueden inscribirse en torneos de disciplinas individuales, siempre que no esten de turno
 * el dia del torneo; no pagan la inscripcion de los competitivos y no reciben el premio en efectivo.
 * Todo rechazo lanza una excepcion con el motivo y deja el torneo sin cambios.
 */
public class ReglasEmpleadoTorneo {

	public static final double VALOR_INSCRIPCION_EMPLEADO = InscripcionEmpleadoTorneo.VALOR_INSCRIPCION;

	private ClubDeportivoSeneca club;

	public ReglasEmpleadoTorneo(ClubDeportivoSeneca club) {
		if (club == null) {
			throw new IllegalArgumentException("Las reglas de torneos necesitan un club.");
		}
		this.club = club;
	}

	// ---------------- Turnos ----------------

	/** Turnos del empleado, del mas antiguo al mas reciente. */
	public List<TurnoEmpleado> consultarTurnos(EmpleadoDelClub empleado) {
		validarEmpleado(empleado);
		List<TurnoEmpleado> propios = new ArrayList<TurnoEmpleado>();
		List<TurnoEmpleado> todos = club.getTurnos();
		for (int i = 0; i < todos.size(); i++) {
			TurnoEmpleado turno = todos.get(i);
			if (turno.getEmpleado() != null && turno.getEmpleado().getLogin().equalsIgnoreCase(empleado.getLogin())) {
				int posicion = 0;
				while (posicion < propios.size() && !inicioDe(turno).isBefore(inicioDe(propios.get(posicion)))) {
					posicion = posicion + 1;
				}
				propios.add(posicion, turno);
			}
		}
		return propios;
	}

	/**
	 * Devuelve el turno que el empleado tiene ese dia, o null si esta libre.
	 * Cuenta cualquier turno que toque el dia, sin importar la franja horaria: un turno que
	 * empieza la noche anterior y termina de madrugada tambien lo deja de turno ese dia.
	 */
	public TurnoEmpleado buscarTurnoElDia(EmpleadoDelClub empleado, LocalDate dia) {
		if (dia == null) {
			return null;
		}
		List<TurnoEmpleado> propios = consultarTurnos(empleado);
		for (int i = 0; i < propios.size(); i++) {
			TurnoEmpleado turno = propios.get(i);
			LocalDate primerDia = turno.getFecha();
			LocalDate ultimoDia = finDe(turno).toLocalDate();
			if (!dia.isBefore(primerDia) && !dia.isAfter(ultimoDia)) {
				return turno;
			}
		}
		return null;
	}

	public boolean estaDeTurno(EmpleadoDelClub empleado, LocalDate dia) {
		return buscarTurnoElDia(empleado, dia) != null;
	}

	private LocalDateTime inicioDe(TurnoEmpleado turno) {
		return LocalDateTime.of(turno.getFecha(), turno.getHora());
	}

	/** Ultimo instante trabajado: un turno de 08:00 a 12:00 termina a las 11:59 y no ocupa el dia siguiente. */
	private LocalDateTime finDe(TurnoEmpleado turno) {
		LocalDateTime inicio = inicioDe(turno);
		if (turno.getDuracion() <= 0) {
			return inicio;
		}
		return inicio.plusHours(turno.getDuracion()).minusMinutes(1);
	}

	// ---------------- Consultas ----------------

	/** Torneos individuales que todavia no inician: los unicos en los que un empleado puede inscribirse. */
	public List<TorneoIndividual> consultarTorneosDisponibles(EmpleadoDelClub empleado) {
		return consultarTorneosDisponibles(empleado, null);
	}

	/** Igual que el anterior, filtrando por disciplina (TENIS, PADEL o TENIS_DE_MESA). */
	public List<TorneoIndividual> consultarTorneosDisponibles(EmpleadoDelClub empleado, String disciplina) {
		validarEmpleado(empleado);
		if (disciplina != null && !ClubDeportivoSeneca.esDisciplinaIndividual(disciplina)) {
			throw new IllegalArgumentException("La disciplina " + disciplina + " no es valida. Use TENIS, PADEL o TENIS_DE_MESA.");
		}
		List<TorneoIndividual> disponibles = new ArrayList<TorneoIndividual>();
		List<Torneo> torneos = club.getTorneos();
		for (int i = 0; i < torneos.size(); i++) {
			Torneo torneo = torneos.get(i);
			boolean abierto = torneo.getEstado() == EstadoTorneo.EN_CURSO && !torneo.haIniciado();
			boolean coincide = disciplina == null || torneo.getDeporte().equalsIgnoreCase(disciplina);
			if (torneo instanceof TorneoIndividual && abierto && coincide) {
				disponibles.add((TorneoIndividual) torneo);
			}
		}
		return disponibles;
	}

	/** Torneos en los que el empleado esta o estuvo inscrito, sin importar su estado. */
	public List<TorneoIndividual> consultarTorneosInscritos(EmpleadoDelClub empleado) {
		validarEmpleado(empleado);
		List<TorneoIndividual> inscritos = new ArrayList<TorneoIndividual>();
		List<Torneo> torneos = club.getTorneos();
		for (int i = 0; i < torneos.size(); i++) {
			if (torneos.get(i) instanceof TorneoIndividual) {
				TorneoIndividual torneo = (TorneoIndividual) torneos.get(i);
				if (torneo.buscarParticipante(empleado) != null) {
					inscritos.add(torneo);
				}
			}
		}
		return inscritos;
	}

	public boolean estaInscrito(EmpleadoDelClub empleado, Torneo torneo) {
		return torneo instanceof TorneoIndividual && ((TorneoIndividual) torneo).buscarParticipante(empleado) != null;
	}

	/** Historial (participantes, encuentros y posiciones) de cualquier torneo, en curso o finalizado. */
	public String consultarHistorial(String idTorneo) {
		return obtenerTorneo(idTorneo).consultarHistorial();
	}

	/** "Por iniciar", "En curso" o "Finalizado": lo que ve el empleado al consultar sus torneos. */
	public String describirEstado(Torneo torneo) {
		if (torneo.getEstado() == EstadoTorneo.FINALIZADO) {
			return "Finalizado";
		}
		if (torneo.haIniciado()) {
			return "En curso";
		}
		return "Por iniciar";
	}

	/** Cupos que puede tomar un empleado: los regulares. Los preferenciales son para los mejores del ranking. */
	public int cuposParaEmpleados(Torneo torneo) {
		if (torneo.isCuposPreferencialesLiberados()) {
			return torneo.cuposDisponibles();
		}
		return torneo.cuposRegularesDisponibles();
	}

	// ---------------- Costo y premio ----------------

	/** Valor que paga un usuario por inscribirse: los empleados no pagan; los demas, la tarifa del torneo. */
	public double calcularValorInscripcion(Usuario usuario, Torneo torneo) {
		if (usuario == null || torneo == null) {
			throw new IllegalArgumentException("Debe indicar el usuario y el torneo.");
		}
		if (usuario instanceof EmpleadoDelClub) {
			return VALOR_INSCRIPCION_EMPLEADO;
		}
		return torneo.getTarifaInscripcion();
	}

	public boolean puedeRecibirPremioEnEfectivo(Usuario usuario) {
		return usuario != null && !(usuario instanceof EmpleadoDelClub);
	}

	// ---------------- Inscripcion y retiro ----------------

	/**
	 * Revisa todas las condiciones antes de inscribir. No modifica nada.
	 * @throws IllegalArgumentException si los datos no son validos (torneo inexistente o de conjunto)
	 * @throws IllegalStateException si la inscripcion no se permite en este momento (turno, cupo, estado, duplicado)
	 */
	public void validarInscripcion(EmpleadoDelClub empleado, Torneo torneo) {
		validarEmpleado(empleado);
		if (torneo == null) {
			throw new IllegalArgumentException("El torneo no existe.");
		}
		if (!(torneo instanceof TorneoIndividual)) {
			throw new IllegalArgumentException("Los empleados solo pueden inscribirse en torneos de disciplinas individuales: "
					+ torneo.getNombre() + " es un torneo de " + torneo.getDeporte() + ".");
		}
		if (torneo.getEstado() == EstadoTorneo.FINALIZADO) {
			throw new IllegalStateException("El torneo " + torneo.getNombre() + " ya finalizo.");
		}
		if (torneo.haIniciado()) {
			throw new IllegalStateException("El torneo " + torneo.getNombre() + " ya inicio (" + torneo.getFechaInicio() + "): no admite inscripciones.");
		}
		if (estaInscrito(empleado, torneo)) {
			throw new IllegalStateException("Ya esta inscrito en el torneo " + torneo.getNombre() + ".");
		}
		TurnoEmpleado turno = buscarTurnoElDia(empleado, torneo.getFechaInicio());
		if (turno != null) {
			throw new IllegalStateException("Tiene un turno el dia del torneo (" + torneo.getFechaInicio() + "): turno del "
					+ turno.getFecha() + " a las " + turno.getHora() + " por " + turno.getDuracion() + " horas.");
		}
		if (cuposParaEmpleados(torneo) <= 0) {
			if (torneo.cuposDisponibles() > 0) {
				throw new IllegalStateException("Solo quedan cupos preferenciales, reservados para los mejores del ranking.");
			}
			throw new IllegalStateException("El torneo " + torneo.getNombre() + " no tiene cupos disponibles.");
		}
	}

	/** Inscribe al empleado en el torneo individual indicado, sin costo. */
	public InscripcionEmpleadoTorneo inscribir(EmpleadoDelClub empleado, String idTorneo) {
		validarEmpleado(empleado);
		Torneo torneo = obtenerTorneo(idTorneo);
		validarInscripcion(empleado, torneo);

		InscripcionEmpleadoTorneo inscripcion = new InscripcionEmpleadoTorneo(empleado, torneo.getDeporte());
		if (!((TorneoIndividual) torneo).inscribir(inscripcion)) {
			throw new IllegalStateException("No fue posible registrar la inscripcion en el torneo " + torneo.getNombre() + ".");
		}
		return inscripcion;
	}

	/** Retira al empleado de un torneo que no ha iniciado y libera su cupo. */
	public void retirar(EmpleadoDelClub empleado, String idTorneo) {
		validarEmpleado(empleado);
		Torneo torneo = obtenerTorneo(idTorneo);
		if (!(torneo instanceof TorneoIndividual)) {
			throw new IllegalStateException("No esta inscrito en el torneo " + torneo.getNombre() + ".");
		}
		TorneoIndividual individual = (TorneoIndividual) torneo;
		InscripcionDisciplinaIndividual inscripcion = individual.buscarParticipante(empleado);
		if (inscripcion == null) {
			throw new IllegalStateException("No esta inscrito en el torneo " + torneo.getNombre() + ".");
		}
		if (torneo.getEstado() == EstadoTorneo.FINALIZADO || torneo.haIniciado()) {
			throw new IllegalStateException("El torneo " + torneo.getNombre() + " ya inicio: no es posible retirarse.");
		}
		if (!individual.retirar(inscripcion)) {
			throw new IllegalStateException("No fue posible retirar la inscripcion del torneo " + torneo.getNombre() + ".");
		}
	}

	private Torneo obtenerTorneo(String idTorneo) {
		Torneo torneo = idTorneo == null ? null : club.buscarTorneo(idTorneo.trim());
		if (torneo == null) {
			throw new IllegalArgumentException("No existe un torneo con id " + idTorneo + ".");
		}
		return torneo;
	}

	private void validarEmpleado(EmpleadoDelClub empleado) {
		if (empleado == null) {
			throw new IllegalArgumentException("Debe indicar el empleado.");
		}
	}
}
