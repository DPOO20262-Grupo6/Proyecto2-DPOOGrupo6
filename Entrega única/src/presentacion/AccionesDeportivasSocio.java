package presentacion;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import Torneos.ReglasSocioTorneo;
import club.ClubDeportivoSeneca;
import deportesDeConjunto.Equipo;
import deportesIndividuales.PartidoDeporteIndividual;
import deportesIndividuales.ReservaInstalacionDeporteIndividual;
import enumeraciones.Modalidad;
import enumeraciones.NivelHabilidad;
import inscripciones.InscripcionDisciplinaIndividual;
import instalaciones.CanchaDeporteIndividual;
import instalaciones.InstalacionDeportiva;
import usuarios.Socio;

/**
 * Acciones deportivas del socio: academias, reservas, partidos de practica, ranking, entrenamientos,
 * estadisticas de su equipo y competencias (HU-S02 a HU-S12). La consola solo pide los datos: las
 * reglas viven en el club.
 */
public class AccionesDeportivasSocio extends AccionesSocioBase {

	private static final String[] DISCIPLINAS = { "TENIS", "PADEL", "TENIS_DE_MESA" };

	private final ReglasSocioTorneo reglasTorneo;

	public AccionesDeportivasSocio(ClubDeportivoSeneca club, Socio socio, LectorEntradas lector) {
		super(club, socio, lector);
		this.reglasTorneo = new ReglasSocioTorneo(club);
	}

	public Menu construirMenuAcademiasYReservas() {
		Menu menu = new Menu("Academias y reservas", "Volver");
		menu.agregarOpcion("Inscribirme en una academia", this::inscribirseEnAcademia);
		menu.agregarOpcion("Reservar una instalacion", this::reservarInstalacion);
		menu.agregarOpcion("Mis reservas e iniciar el uso de una reserva", this::consultarReservasEIniciarUso);
		return menu;
	}

	public Menu construirMenuPartidosYRanking() {
		Menu menu = new Menu("Partidos de practica y ranking", "Volver");
		menu.agregarOpcion("Programar un partido de practica", this::programarPartidoPractica);
		menu.agregarOpcion("Consultar mi ranking", this::consultarRanking);
		menu.agregarOpcion("Consultar mi historial de partidos", this::consultarHistorial);
		return menu;
	}

	public Menu construirMenuEntrenamientos() {
		Menu menu = new Menu("Entrenamientos", "Volver");
		menu.agregarOpcion("Consultar mis convocatorias", () -> lector.mostrar(club.consultarConvocatorias(socio)));
		menu.agregarOpcion("Consultar mi asistencia y evaluaciones", () -> lector.mostrar(club.consultarAsistencia(socio)));
		return menu;
	}

	public Menu construirMenuEquiposYCompetencias() {
		Menu menu = new Menu("Mi equipo y competencias", "Volver");
		menu.agregarOpcion("Estadisticas de mi equipo y las mias", this::consultarEstadisticasEquipo);
		menu.agregarOpcion("Tabla de posiciones de una competencia", this::consultarTabla);
		menu.agregarOpcion("Tablas filtradas por deporte o categoria", this::consultarTablasFiltradas);
		menu.agregarOpcion("Goleadores de una competencia", () -> lector.mostrar(club.consultarGoleadores(pedirCompetencia())));
		menu.agregarOpcion("Jugadores destacados de una competencia", () -> lector.mostrar(club.consultarDestacados(pedirCompetencia())));
		return menu;
	}

	// ---------------- Academias y reservas ----------------

	private void inscribirseEnAcademia() {
		lector.mostrar("--- Inscribirme en una academia ---");
		String disciplina = lector.leerOpcion("Disciplina (TENIS, PADEL, TENIS_DE_MESA): ", DISCIPLINAS);
		NivelHabilidad nivel = lector.leerEnumeracion("Nivel inicial (PRINCIPIANTE, INTERMEDIO, AVANZADO): ", NivelHabilidad.class);
		InscripcionDisciplinaIndividual inscripcion = club.inscribirEnAcademia(socio, disciplina, nivel);
		lector.mostrar("Inscripcion creada en " + inscripcion.getDisciplina() + " con nivel " + inscripcion.getNivel() + " y ELO " + inscripcion.getRankingElo() + ".");
	}

	private void reservarInstalacion() {
		lector.mostrar("--- Reservar una instalacion ---");
		List<CanchaDeporteIndividual> canchas = consultarCanchas();
		if (canchas.isEmpty()) {
			lector.mostrar("No hay instalaciones registradas en el club.");
			return;
		}
		CanchaDeporteIndividual cancha = elegir("Instalacion (numero): ", canchas, c -> c.getNombre() + " (" + c.getDisciplina() + ")");
		LocalDate fecha = lector.leerFechaDesde("Fecha (AAAA-MM-DD): ", LocalDate.now());
		LocalTime inicio = lector.leerHora("Hora de inicio (HH:MM): ");
		LocalTime fin = lector.leerHora("Hora de fin (HH:MM): ");
		if (!fin.isAfter(inicio)) {
			throw new IllegalArgumentException("La hora de fin debe ser posterior a la de inicio.");
		}
		int cantidad = lector.leerEntero("Numero de jugadores (contandose usted): ", 1, 4);
		Modalidad modalidad = lector.leerEnumeracion("Modalidad (SENCILLOS o DOBLES): ", Modalidad.class);

		List<Socio> jugadores = new ArrayList<Socio>();
		jugadores.add(socio);
		for (int i = 2; i <= cantidad; i++) {
			jugadores.add(pedirSocio("Login del jugador " + i + ": "));
		}
		ReservaInstalacionDeporteIndividual reserva = club.reservarInstalacion(jugadores, cancha, fecha, inicio, fin, modalidad);
		if ("CONFIRMADA".equalsIgnoreCase(reserva.getEstado())) {
			lector.mostrar("Reserva confirmada: " + describirReserva(reserva));
		} else {
			lector.mostrar("Reserva rechazada: " + reserva.getMotivoRechazo());
		}
	}

	private void consultarReservasEIniciarUso() {
		lector.mostrar("--- Mis reservas ---");
		List<ReservaInstalacionDeporteIndividual> reservas = club.getReservasDeSocio(socio);
		if (reservas.isEmpty()) {
			lector.mostrar("No tiene reservas.");
			return;
		}
		ReservaInstalacionDeporteIndividual reserva = elegir("Reserva para iniciar su uso (numero): ", reservas, this::describirReserva);
		boolean llevaBebidaCaliente = lector.leerConfirmacion("Lleva una bebida caliente?");
		reserva.iniciarUso(socio, llevaBebidaCaliente);
		lector.mostrar("Uso iniciado. Que disfrute su partido.");
	}

	private String describirReserva(ReservaInstalacionDeporteIndividual reserva) {
		return reserva.getCancha().getNombre() + " | " + reserva.getFecha() + " " + reserva.getHoraInicio() + "-" + reserva.getHoraFin()
				+ " | " + reserva.getCantidadJugadores() + " jugadores " + reserva.getModalidad() + " | " + reserva.getEstado()
				+ (reserva.isEnUso() ? " | en uso" : "");
	}

	/** Canchas registradas en el club y las que ya aparecen en alguna reserva. */
	private List<CanchaDeporteIndividual> consultarCanchas() {
		List<CanchaDeporteIndividual> canchas = new ArrayList<CanchaDeporteIndividual>();
		List<InstalacionDeportiva> instalaciones = club.getInstalaciones();
		for (int i = 0; i < instalaciones.size(); i++) {
			if (instalaciones.get(i) instanceof CanchaDeporteIndividual && !canchas.contains(instalaciones.get(i))) {
				canchas.add((CanchaDeporteIndividual) instalaciones.get(i));
			}
		}
		List<ReservaInstalacionDeporteIndividual> reservas = club.getReservas();
		for (int i = 0; i < reservas.size(); i++) {
			if (!canchas.contains(reservas.get(i).getCancha())) {
				canchas.add(reservas.get(i).getCancha());
			}
		}
		return canchas;
	}

	// ---------------- Partidos de practica y ranking ----------------

	private void programarPartidoPractica() {
		lector.mostrar("--- Programar un partido de practica ---");
		List<ReservaInstalacionDeporteIndividual> activas = new ArrayList<ReservaInstalacionDeporteIndividual>();
		List<ReservaInstalacionDeporteIndividual> mias = club.getReservasDeSocio(socio);
		for (int i = 0; i < mias.size(); i++) {
			if ("CONFIRMADA".equalsIgnoreCase(mias.get(i).getEstado())) {
				activas.add(mias.get(i));
			}
		}
		if (activas.isEmpty()) {
			lector.mostrar("No tiene reservas activas: reserve una instalacion primero.");
			return;
		}
		ReservaInstalacionDeporteIndividual reserva = elegir("Reserva (numero): ", activas, this::describirReserva);
		String disciplina = reserva.getCancha().getDisciplina();

		List<InscripcionDisciplinaIndividual> participantes = new ArrayList<InscripcionDisciplinaIndividual>();
		participantes.add(obtenerInscripcion(socio, disciplina));
		for (int i = 2; i <= reserva.getCantidadJugadores(); i++) {
			participantes.add(obtenerInscripcion(pedirSocio("Login del participante " + i + ": "), disciplina));
		}
		boolean participaEntrenador = lector.leerConfirmacion("Participa un entrenador en el partido?");
		PartidoDeporteIndividual partido = club.programarPartidoPractica(socio, reserva, participantes, participaEntrenador);
		lector.mostrar("Partido programado el " + partido.getFecha() + " a las " + partido.getHoraInicio() + " con " + partido.getParticipantes().size() + " participantes.");
	}

	private InscripcionDisciplinaIndividual obtenerInscripcion(Socio jugador, String disciplina) {
		InscripcionDisciplinaIndividual inscripcion = club.buscarInscripcion(jugador, disciplina);
		if (inscripcion == null) {
			throw new IllegalStateException(jugador.getNombre() + " no esta inscrito en la academia de " + disciplina + ".");
		}
		return inscripcion;
	}

	private void consultarRanking() {
		lector.mostrar("--- Mi ranking ---");
		String disciplina = lector.leerOpcion("Disciplina (TENIS, PADEL, TENIS_DE_MESA): ", DISCIPLINAS);
		lector.mostrar(club.consultarRanking(socio, disciplina));
	}

	private void consultarHistorial() {
		lector.mostrar("--- Mi historial de partidos de practica ---");
		String disciplina = lector.leerOpcion("Disciplina (TENIS, PADEL, TENIS_DE_MESA): ", DISCIPLINAS);
		LocalDate desde = lector.leerFechaOpcional("Fecha inicial AAAA-MM-DD (Enter para omitir): ");
		LocalDate hasta = lector.leerFechaOpcional("Fecha final AAAA-MM-DD (Enter para omitir): ");
		List<PartidoDeporteIndividual> historial = club.consultarHistorialPractica(socio, socio, disciplina, desde, hasta);
		InscripcionDisciplinaIndividual inscripcion = club.buscarInscripcion(socio, disciplina);
		if (historial.isEmpty()) {
			lector.mostrar("No tiene partidos con resultado en ese periodo.");
		}
		for (int i = 0; i < historial.size(); i++) {
			lector.mostrar(describirPartido(historial.get(i)));
		}
		lector.mostrar("ELO actual en " + disciplina + ": " + inscripcion.getRankingElo());
	}

	private String describirPartido(PartidoDeporteIndividual partido) {
		StringBuilder texto = new StringBuilder(partido.getFecha() + " " + partido.getHoraInicio() + " | ");
		for (int i = 0; i < partido.getParticipantes().size(); i++) {
			if (i > 0) {
				texto.append(" vs ");
			}
			texto.append(partido.getParticipantes().get(i).getSocio().getNombre());
			if (i < partido.getEstadisticas().size()) {
				texto.append(" [").append(partido.getEstadisticas().get(i).getResumen());
				texto.append(partido.getEstadisticas().get(i).esGanador() ? ", ganador]" : "]");
			}
		}
		return texto.toString();
	}

	// ---------------- Equipos y competencias ----------------

	private void consultarEstadisticasEquipo() {
		lector.mostrar("--- Estadisticas de mi equipo ---");
		List<Equipo> equipos = reglasTorneo.consultarEquiposDelSocio(socio);
		if (equipos.isEmpty()) {
			lector.mostrar("No pertenece a ningun equipo.");
			return;
		}
		Equipo equipo = elegir("Equipo (numero): ", equipos, e -> e.getNombre() + " (" + e.getDeporte() + ", " + e.getCategoria() + ")");
		lector.mostrar(club.consultarEstadisticasEquipo(socio, equipo));
	}

	private void consultarTabla() {
		lector.mostrar("--- Tabla de posiciones ---");
		String nombre = pedirCompetencia();
		String grupo = lector.leerTextoOpcional("Grupo (Enter si la competencia es una liga): ");
		lector.mostrar(club.consultarTablaPosiciones(nombre, grupo.isEmpty() ? null : grupo));
	}

	private void consultarTablasFiltradas() {
		lector.mostrar("--- Tablas por deporte o categoria ---");
		String deporte = lector.leerTextoOpcional("Deporte (Enter para todos): ");
		String categoria = lector.leerTextoOpcional("Categoria (Enter para todas): ");
		lector.mostrar(club.consultarTablas(deporte.isEmpty() ? null : deporte, categoria.isEmpty() ? null : categoria));
	}

	/** Muestra las competencias registradas y pide el nombre de una. */
	private String pedirCompetencia() {
		if (club.getCompetencias().isEmpty()) {
			lector.mostrar("No hay competencias registradas.");
		} else {
			listar(club.getCompetencias(), c -> c.getNombre() + " (" + c.getDeporte() + ", " + c.getCategoriaHabilitada() + ", " + c.getAnioTemporada() + ")");
		}
		return lector.leerTexto("Nombre de la competencia: ");
	}

	private Socio pedirSocio(String pregunta) {
		String login = lector.leerTexto(pregunta);
		Socio otro = club.buscarSocio(login);
		if (otro == null) {
			throw new IllegalArgumentException("No existe un socio con el login " + login + ".");
		}
		return otro;
	}
}
