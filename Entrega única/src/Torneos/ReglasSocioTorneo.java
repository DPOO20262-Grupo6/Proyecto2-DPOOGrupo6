package Torneos;

import java.util.ArrayList;
import java.util.List;

import articulosYVentas.Bono;
import club.ClubDeportivoSeneca;
import deportesDeConjunto.Equipo;
import deportesDeConjunto.PartidoTorneo;
import enumeraciones.EstadoTorneo;
import enumeraciones.Modalidad;
import enumeraciones.NivelHabilidad;
import inscripciones.InscripcionDisciplinaIndividual;
import inscripciones.InscripcionEquipo;
import inscripciones.InscripcionParticipanteExterno;
import usuarios.ParticipanteExterno;
import usuarios.Socio;

/**
 * Reglas de participacion de los socios en los torneos internos: inscribir y retirar equipos,
 * jugadores, parejas y personas invitadas (estudiantes matriculados o externos), y guardar y usar
 * el bono del torneo recreativo. Todo rechazo lanza una excepcion con el motivo y deja el torneo
 * sin cambios.
 */
public class ReglasSocioTorneo {

	/** Un torneo reserva el 20 % de sus cupos para los mejores del ranking de la categoria. */
	private static final double FRACCION_MEJOR_RANKING = 0.20;

	private ClubDeportivoSeneca club;

	public ReglasSocioTorneo(ClubDeportivoSeneca club) {
		if (club == null) {
			throw new IllegalArgumentException("Las reglas de torneos necesitan un club.");
		}
		this.club = club;
	}

	// ---------------- Consultas ----------------

	/** Torneos que todavia aceptan inscripciones. */
	public List<Torneo> consultarTorneosAbiertos() {
		List<Torneo> abiertos = new ArrayList<Torneo>();
		List<Torneo> torneos = club.getTorneos();
		for (int i = 0; i < torneos.size(); i++) {
			if (torneos.get(i).getEstado() == EstadoTorneo.EN_CURSO && !torneos.get(i).haIniciado()) {
				abiertos.add(torneos.get(i));
			}
		}
		return abiertos;
	}

	public List<Torneo> consultarTorneos() {
		return new ArrayList<Torneo>(club.getTorneos());
	}

	public Torneo obtenerTorneo(String idTorneo) {
		Torneo torneo = idTorneo == null ? null : club.buscarTorneo(idTorneo.trim());
		if (torneo == null) {
			throw new IllegalArgumentException("No existe un torneo con id " + idTorneo + ".");
		}
		return torneo;
	}

	/** "Por iniciar", "En curso" o "Finalizado". */
	public String describirEstado(Torneo torneo) {
		if (torneo.getEstado() == EstadoTorneo.FINALIZADO) {
			return "Finalizado";
		}
		if (torneo.haIniciado()) {
			return "En curso";
		}
		return "Por iniciar";
	}

	/** Una linea con los datos que el socio necesita para decidir si se inscribe. */
	public String describirTorneo(Torneo torneo) {
		String modalidad = "EQUIPOS";
		if (torneo instanceof TorneoIndividual) {
			modalidad = ((TorneoIndividual) torneo).getModalidad().toString();
		}
		return torneo.getId() + " | " + torneo.getNombre() + " | " + torneo.getDeporte() + " " + modalidad + " | "
				+ torneo.getCategoria() + " | " + torneo.getTipo() + " | " + torneo.getFechaInicio() + " | " + describirEstado(torneo)
				+ " | cupos libres: " + torneo.cuposDisponibles() + " (preferenciales: " + torneo.cuposPreferencialesDisponibles()
				+ ", regulares: " + torneo.cuposRegularesDisponibles() + ") | inscripcion: $" + calcularValorInscripcion(torneo);
	}

	/** Los socios y las personas invitadas pagan la tarifa del torneo; en los recreativos es $0. */
	public double calcularValorInscripcion(Torneo torneo) {
		if (torneo == null) {
			throw new IllegalArgumentException("El torneo no existe.");
		}
		return torneo.getTarifaInscripcion();
	}

	/** Historial (participantes, encuentros y posiciones) de un torneo en cualquier estado. */
	public String consultarHistorial(String idTorneo) {
		return obtenerTorneo(idTorneo).consultarHistorial();
	}

	/** Torneos en los que el socio participa: como jugador, como pareja o con alguno de sus equipos. */
	public List<Torneo> consultarMisTorneos(Socio socio) {
		validarSocio(socio);
		List<Equipo> misEquipos = consultarEquiposDelSocio(socio);
		List<Torneo> mios = new ArrayList<Torneo>();
		List<Torneo> torneos = club.getTorneos();
		for (int i = 0; i < torneos.size(); i++) {
			Torneo torneo = torneos.get(i);
			if (torneo instanceof TorneoIndividual && ((TorneoIndividual) torneo).buscarParticipante(socio) != null) {
				mios.add(torneo);
			} else if (torneo instanceof TorneoEquipo) {
				for (int j = 0; j < misEquipos.size(); j++) {
					if (((TorneoEquipo) torneo).estaInscrito(misEquipos.get(j))) {
						mios.add(torneo);
						break;
					}
				}
			}
		}
		return mios;
	}

	/** Equipos activos en los que el socio es jugador activo. */
	public List<Equipo> consultarEquiposDelSocio(Socio socio) {
		validarSocio(socio);
		List<Equipo> propios = new ArrayList<Equipo>();
		List<Equipo> equipos = club.getEquipos();
		for (int i = 0; i < equipos.size(); i++) {
			if (equipos.get(i).isActivo() && perteneceAlEquipo(socio, equipos.get(i))) {
				propios.add(equipos.get(i));
			}
		}
		return propios;
	}

	// ---------------- Equipos ----------------

	/** Inscribe un equipo completo. Solo lo puede hacer un jugador del equipo. */
	public void inscribirEquipo(Socio socio, String idTorneo, Equipo equipo) {
		validarSocio(socio);
		TorneoEquipo torneo = obtenerTorneoDeEquipos(idTorneo);
		validarEquipoDelSocio(socio, equipo);
		if (torneo.getCategoria() != null && !torneo.getCategoria().equalsIgnoreCase(equipo.getCategoria())) {
			throw new IllegalArgumentException("El equipo " + equipo.getNombre() + " es de la categoria " + equipo.getCategoria()
					+ " y el torneo es de la categoria " + torneo.getCategoria() + ".");
		}
		torneo.inscribir(equipo, tieneBuenRanking(equipo));
	}

	/** Retira un equipo antes de que el torneo inicie y libera su cupo. */
	public void retirarEquipo(Socio socio, String idTorneo, Equipo equipo) {
		validarSocio(socio);
		TorneoEquipo torneo = obtenerTorneoDeEquipos(idTorneo);
		validarEquipoDelSocio(socio, equipo);
		torneo.retirar(equipo);
	}

	private TorneoEquipo obtenerTorneoDeEquipos(String idTorneo) {
		Torneo torneo = obtenerTorneo(idTorneo);
		if (!(torneo instanceof TorneoEquipo)) {
			throw new IllegalArgumentException("El torneo " + torneo.getNombre() + " es de una disciplina individual: se inscriben jugadores, no equipos.");
		}
		return (TorneoEquipo) torneo;
	}

	private void validarEquipoDelSocio(Socio socio, Equipo equipo) {
		if (equipo == null) {
			throw new IllegalArgumentException("El equipo no existe.");
		}
		if (!equipo.isActivo()) {
			throw new IllegalStateException("El equipo " + equipo.getNombre() + " esta inactivo.");
		}
		if (!perteneceAlEquipo(socio, equipo)) {
			throw new IllegalStateException("Solo un jugador del equipo " + equipo.getNombre() + " puede inscribirlo o retirarlo.");
		}
	}

	private boolean perteneceAlEquipo(Socio socio, Equipo equipo) {
		for (int i = 0; i < equipo.getInscripciones().size(); i++) {
			InscripcionEquipo inscripcion = equipo.getInscripciones().get(i);
			if (inscripcion.isActiva() && inscripcion.getSocio() != null && inscripcion.getSocio().equals(socio)) {
				return true;
			}
		}
		return false;
	}

	// ---------------- Jugadores y parejas ----------------

	/**
	 * Inscribe al socio en un torneo individual. Debe estar inscrito en la academia de esa disciplina:
	 * de ahi salen su nivel y su ranking. En los torneos de dobles se inscribe solo, o con
	 * {@link #inscribirPareja}.
	 */
	public InscripcionDisciplinaIndividual inscribirIndividual(Socio socio, String idTorneo) {
		validarSocio(socio);
		TorneoIndividual torneo = obtenerTorneoIndividual(idTorneo);
		InscripcionDisciplinaIndividual inscripcion = obtenerInscripcionDeAcademia(socio, torneo);
		torneo.inscribir(inscripcion, tieneBuenRanking(inscripcion));
		return inscripcion;
	}

	/** Inscribe a dos socios como pareja en un torneo de dobles: se inscriben los dos o ninguno. */
	public void inscribirPareja(Socio socio, String loginPareja, String idTorneo) {
		validarSocio(socio);
		TorneoIndividual torneo = obtenerTorneoIndividual(idTorneo);
		if (torneo.getModalidad() != Modalidad.DOBLES) {
			throw new IllegalStateException("El torneo " + torneo.getNombre() + " es de sencillos: no admite parejas.");
		}
		Socio companero = loginPareja == null ? null : club.buscarSocio(loginPareja.trim());
		if (companero == null) {
			throw new IllegalArgumentException("No existe un socio con el login " + loginPareja + ".");
		}
		if (companero.equals(socio)) {
			throw new IllegalArgumentException("La pareja debe estar formada por dos jugadores distintos.");
		}
		validarSocio(companero);
		InscripcionDisciplinaIndividual primero = obtenerInscripcionDeAcademia(socio, torneo);
		InscripcionDisciplinaIndividual segundo = obtenerInscripcionDeAcademia(companero, torneo);
		torneo.inscribirPareja(primero, segundo, tieneBuenRanking(primero) && tieneBuenRanking(segundo));
	}

	/** Retira al socio (y a su pareja, si se inscribio con una) y libera los cupos. */
	public void retirarJugador(Socio socio, String idTorneo) {
		validarSocio(socio);
		TorneoIndividual torneo = obtenerTorneoIndividual(idTorneo);
		InscripcionDisciplinaIndividual inscripcion = torneo.buscarParticipante(socio);
		if (inscripcion == null) {
			throw new IllegalStateException("No esta inscrito en el torneo " + torneo.getNombre() + ".");
		}
		validarRetiro(torneo);
		torneo.retirar(inscripcion);
	}

	private TorneoIndividual obtenerTorneoIndividual(String idTorneo) {
		Torneo torneo = obtenerTorneo(idTorneo);
		if (!(torneo instanceof TorneoIndividual)) {
			throw new IllegalArgumentException("El torneo " + torneo.getNombre() + " es de un deporte de conjunto: se inscriben equipos completos.");
		}
		return (TorneoIndividual) torneo;
	}

	private void validarRetiro(Torneo torneo) {
		if (torneo.getEstado() == EstadoTorneo.FINALIZADO || torneo.haIniciado()) {
			throw new IllegalStateException("El torneo " + torneo.getNombre() + " ya inicio: no es posible retirarse.");
		}
	}

	private InscripcionDisciplinaIndividual obtenerInscripcionDeAcademia(Socio socio, TorneoIndividual torneo) {
		InscripcionDisciplinaIndividual inscripcion = club.buscarInscripcion(socio, torneo.getDeporte());
		if (inscripcion == null) {
			throw new IllegalStateException(socio.getNombre() + " debe inscribirse primero en la academia de " + torneo.getDeporte() + ".");
		}
		NivelHabilidad nivelDelTorneo = nivelDeLaCategoria(torneo.getCategoria());
		if (nivelDelTorneo != null && nivelDelTorneo != inscripcion.getNivel()) {
			throw new IllegalStateException("El torneo " + torneo.getNombre() + " es de nivel " + nivelDelTorneo + " y " + socio.getNombre()
					+ " esta inscrito como " + inscripcion.getNivel() + ".");
		}
		return inscripcion;
	}

	/** Si la categoria del torneo es un nivel (Principiante, Intermedio o Avanzado), devuelve ese nivel; si no, null. */
	private NivelHabilidad nivelDeLaCategoria(String categoria) {
		if (categoria == null) {
			return null;
		}
		try {
			return NivelHabilidad.valueOf(categoria.trim().toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	// ---------------- Estudiantes y personas externas ----------------

	/**
	 * Un socio inscribe a un estudiante matriculado o a una persona externa en un torneo individual.
	 * La persona se identifica por su documento. Paga la tarifa y puede ganar el premio en efectivo.
	 */
	public InscripcionParticipanteExterno inscribirParticipante(Socio socio, String idTorneo, String nombre, String documento, String tipo) {
		validarSocio(socio);
		TorneoIndividual torneo = obtenerTorneoIndividual(idTorneo);
		if (estaVacio(nombre)) {
			throw new IllegalArgumentException("Falta el nombre de la persona.");
		}
		if (estaVacio(documento)) {
			throw new IllegalArgumentException("Falta el documento de la persona.");
		}
		if (!ParticipanteExterno.ESTUDIANTE_MATRICULADO.equals(tipo) && !ParticipanteExterno.EXTERNO.equals(tipo)) {
			throw new IllegalArgumentException("El tipo debe ser ESTUDIANTE_MATRICULADO o EXTERNO.");
		}
		if (club.buscarSocioPorDocumento(documento.trim()) != null) {
			throw new IllegalStateException("El documento " + documento.trim() + " ya pertenece a un socio: debe inscribirse con su propia cuenta.");
		}
		ParticipanteExterno persona = new ParticipanteExterno(nombre.trim(), documento.trim(), "", tipo);
		if (torneo.buscarParticipante(persona) != null) {
			throw new IllegalStateException("El documento " + documento.trim() + " ya esta inscrito en el torneo " + torneo.getNombre() + ".");
		}
		InscripcionParticipanteExterno inscripcion = new InscripcionParticipanteExterno(persona, torneo.getDeporte(), socio.getLogin());
		torneo.inscribir(inscripcion, false);
		return inscripcion;
	}

	/** Retira a una persona invitada. Solo puede hacerlo el socio que la inscribio. */
	public void retirarParticipante(Socio socio, String idTorneo, String documento) {
		validarSocio(socio);
		TorneoIndividual torneo = obtenerTorneoIndividual(idTorneo);
		ParticipanteExterno buscado = new ParticipanteExterno("", estaVacio(documento) ? "" : documento.trim(), "", ParticipanteExterno.EXTERNO);
		InscripcionDisciplinaIndividual inscripcion = torneo.buscarParticipante(buscado);
		if (!(inscripcion instanceof InscripcionParticipanteExterno)) {
			throw new IllegalStateException("No hay una persona invitada con el documento " + documento + " en el torneo " + torneo.getNombre() + ".");
		}
		if (!socio.getLogin().equalsIgnoreCase(((InscripcionParticipanteExterno) inscripcion).getLoginSocioResponsable())) {
			throw new IllegalStateException("Solo el socio que inscribio a esta persona puede retirarla.");
		}
		validarRetiro(torneo);
		torneo.retirar(inscripcion);
	}

	// ---------------- Ranking y cupos preferenciales ----------------

	/**
	 * El 20 % de los cupos es para los mejores del ranking de la categoria. Un jugador tiene buen
	 * ranking si ya gano partidos (su ELO supera el inicial) y esta en el 20 % superior (redondeado
	 * hacia arriba) de los jugadores inscritos en esa disciplina.
	 */
	public boolean tieneBuenRanking(InscripcionDisciplinaIndividual inscripcion) {
		if (inscripcion.getRankingElo() <= InscripcionDisciplinaIndividual.ELO_INICIAL) {
			return false;
		}
		int total = 0;
		int mejores = 0;
		List<InscripcionDisciplinaIndividual> todas = club.getInscripcionesIndividuales();
		for (int i = 0; i < todas.size(); i++) {
			if (todas.get(i).getDisciplina().equalsIgnoreCase(inscripcion.getDisciplina())) {
				total = total + 1;
				if (todas.get(i).getRankingElo() > inscripcion.getRankingElo()) {
					mejores = mejores + 1;
				}
			}
		}
		return total > 0 && mejores + 1 <= (int) Math.ceil(total * FRACCION_MEJOR_RANKING);
	}

	/**
	 * Un equipo tiene buen desempeno si ya sumo puntos en los torneos de equipos y esta en el 20 %
	 * superior (redondeado hacia arriba) de los equipos activos de su deporte y categoria.
	 */
	public boolean tieneBuenRanking(Equipo equipo) {
		int puntos = calcularPuntos(equipo);
		if (puntos <= 0) {
			return false;
		}
		int total = 0;
		int mejores = 0;
		List<Equipo> equipos = club.getEquipos();
		for (int i = 0; i < equipos.size(); i++) {
			Equipo otro = equipos.get(i);
			if (otro.isActivo() && otro.getDeporte().equalsIgnoreCase(equipo.getDeporte()) && otro.getCategoria().equalsIgnoreCase(equipo.getCategoria())) {
				total = total + 1;
				if (calcularPuntos(otro) > puntos) {
					mejores = mejores + 1;
				}
			}
		}
		return total > 0 && mejores + 1 <= (int) Math.ceil(total * FRACCION_MEJOR_RANKING);
	}

	private int calcularPuntos(Equipo equipo) {
		List<PartidoTorneo> partidos = new ArrayList<PartidoTorneo>();
		List<Torneo> torneos = club.getTorneos();
		for (int i = 0; i < torneos.size(); i++) {
			if (torneos.get(i) instanceof TorneoEquipo) {
				partidos.addAll(((TorneoEquipo) torneos.get(i)).getPartidos());
			}
		}
		return equipo.calcularPuntosLiga(partidos);
	}

	// ---------------- Bonos ----------------

	/** Bonos que el socio ha ganado y todavia no usa. */
	public List<Bono> consultarBonosDisponibles(Socio socio) {
		validarSocio(socio);
		List<Bono> disponibles = new ArrayList<Bono>();
		List<Bono> todos = socio.getBonos();
		for (int i = 0; i < todos.size(); i++) {
			if (!todos.get(i).isUsado()) {
				disponibles.add(todos.get(i));
			}
		}
		return disponibles;
	}

	private void validarSocio(Socio socio) {
		if (socio == null) {
			throw new IllegalArgumentException("Debe indicar el socio.");
		}
		if (!socio.isActivo()) {
			throw new IllegalStateException("El socio " + socio.getLogin() + " no esta activo.");
		}
	}

	private boolean estaVacio(String texto) {
		return texto == null || texto.trim().isEmpty();
	}
}
