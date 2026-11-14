package usuarios;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import administracion.SolicitudCambioTurnoEmpleado;
import administracion.Sugerencia;
import administracion.TurnoEmpleado;
import articulosYVentas.Articulo;
import articulosYVentas.ArticuloDeportivo;
import club.ClubDeportivoSeneca;
import deportesDeConjunto.Competencia;
import deportesDeConjunto.Equipo;
import deportesDeConjunto.EquipoBaloncesto;
import deportesDeConjunto.EquipoFutbol;
import deportesDeConjunto.EquipoVoleibol;
import deportesDeConjunto.NominaCompetencia;
import deportesDeConjunto.PartidoDeEquipo;
import deportesDeConjunto.PartidoInterno;
import deportesDeConjunto.ResultadoPartidoEquipo;
import inscripciones.InscripcionEquipo;
import inscripciones.InscripcionEquipoFutbol;
import inventario.UbicacionInventario;
import Torneos.Torneo;
import Torneos.TorneoEquipo;
import Torneos.TorneoIndividual;
import deportesDeConjunto.PartidoTorneo;
import deportesIndividuales.PartidoDeporteIndividual;
import enumeraciones.EstadoTorneo;
import enumeraciones.Modalidad;
import enumeraciones.TipoTorneo;
import estadisticas.EstadisticasPartidoIndividual;

public class Administrador extends Usuario {
	
	private static final long serialVersionUID = 1L;
	
	private ClubDeportivoSeneca club;

	public Administrador(String nombre, String login, String password, ClubDeportivoSeneca club) {
		super(nombre, login, password);
		this.club = club;
	}

	public Equipo crearEquipo(String tipo, String nombre, String categoria, int cupo) {
		Equipo eq = null;
		if (tipo.equalsIgnoreCase("FUTBOL")) {
			eq = new EquipoFutbol(nombre, categoria, cupo);
		} else if (tipo.equalsIgnoreCase("BALONCESTO")) {
			eq = new EquipoBaloncesto(nombre, categoria, cupo);
		} else if (tipo.equalsIgnoreCase("VOLEIBOL")) {
			eq = new EquipoVoleibol(nombre, categoria, cupo);
		} else {
			throw new IllegalArgumentException("Deporte no valido: " + tipo);
		}
		club.agregarEquipo(eq);
		return eq;
	}

	public void darDeBajaEquipo(Equipo equipo) {
		club.getEquipos().remove(equipo);
	}

	public InscripcionEquipo inscribirJugador(Equipo equipo, Socio socio, String posicion, int numeroCamiseta) {
		InscripcionEquipo ie = null;
		if (equipo instanceof EquipoFutbol) {
			ie = new InscripcionEquipoFutbol(socio, equipo, posicion, numeroCamiseta);
		} else {
			ie = new InscripcionEquipo(socio, equipo, posicion, numeroCamiseta);
		}
		equipo.agregarJugador(ie);
		return ie;
	}

	public void darDeBajaJugador(Equipo equipo, InscripcionEquipo inscripcion) {
		equipo.retirarJugador(inscripcion);
	}

	public NominaCompetencia inscribirEquipoEnCompetencia(Equipo equipo, Competencia competencia, List<InscripcionEquipo> jugadores) {
		return competencia.inscribirEquipo(equipo, jugadores);
	}

	public PartidoInterno programarPartidoInterno(Equipo local, Equipo visitante, LocalDate fecha) {
		PartidoInterno partido = new PartidoInterno(fecha, local, visitante);
		club.getPartidosInternos().add(partido);
		return partido;
	}

	public void registrarResultado(PartidoDeEquipo partido, ResultadoPartidoEquipo resultado) {
		partido.registrarResultado(resultado);
	}

	public TurnoEmpleado asignarTurno(EmpleadoDelClub empleado, LocalDate fecha, LocalTime hora, int duracion) {
		TurnoEmpleado turno = new TurnoEmpleado(empleado, fecha, hora, duracion);
		club.getTurnos().add(turno);
		return turno;
	}

	public void evaluarSolicitudCambio(SolicitudCambioTurnoEmpleado solicitud) {
		TurnoEmpleado turno = solicitud.getTurno();
		LocalDate fechaOriginal = turno.getFecha();
		LocalTime horaOriginal = turno.getHora();

		turno.reprogramar(solicitud.getNuevaFecha(), solicitud.getNuevaHora());
		boolean cumple = club.verificarCoberturaMinima(fechaOriginal, horaOriginal);

		if (cumple) {
			solicitud.aprobar();
		} else {
			turno.reprogramar(fechaOriginal, horaOriginal);
			solicitud.rechazar();
			throw new IllegalStateException("No se puede aprobar el cambio porque incumple el minimo de 2 entrenadores y 1 fisioterapeuta");
		}
	}

	public void reabastecer(Articulo articulo, UbicacionInventario ubicacion, int cantidad) {
		ubicacion.agregarExistencias(articulo, cantidad);
	}

	public void trasladarArticulo(Articulo articulo, UbicacionInventario origen, UbicacionInventario destino, int cantidad) {
		origen.retirarExistencias(articulo, cantidad);
		destino.agregarExistencias(articulo, cantidad);
	}

	public void evaluarSugerencia(Sugerencia sugerencia, boolean aprobada) {
		if (aprobada) {
			sugerencia.aprobar();
			Articulo nuevo = sugerencia.convertirEnArticulo();
			if (nuevo instanceof ArticuloDeportivo) {
				club.getTienda().agregarArticuloAlCatalogo((ArticuloDeportivo) nuevo);
			} else {
				club.getCafeteria().agregarProducto(nuevo);
			}
		} else {
			sugerencia.rechazar();
		}
	}

	// ---------------- Torneos internos (HU-A01 a HU-A06) ----------------

	/**
	 * HU-A01. Deportes individuales (TENIS, PADEL, TENIS_DE_MESA) crean un TorneoIndividual;
	 * deportes de conjunto (FUTBOL, BALONCESTO, VOLEIBOL) crean un TorneoEquipo y la modalidad se ignora.
	 * La modalidad acepta INDIVIDUAL/SENCILLOS o DOBLES.
	 * @throws Exception 
	 */
	public Torneo crearTorneo(String nombre, String deporte, String modalidad, String categoria, int cupos, TipoTorneo tipo, LocalDate fechaInicio, double tarifa, double bono) throws Exception {
		if (deporte == null) {
			throw new IllegalArgumentException("Debe indicar el deporte del torneo.");
		}
		if (fechaInicio != null && fechaInicio.isBefore(LocalDate.now())) {
			throw new IllegalArgumentException("La fecha de inicio no puede estar en el pasado.");
		}
		if (tipo == TipoTorneo.COMPETITIVO && tarifa <= 0) {
			throw new IllegalArgumentException("Un torneo competitivo debe tener tarifa de inscripcion.");
		}
		if (tipo == TipoTorneo.RECREATIVO && bono <= 0) {
			throw new IllegalArgumentException("Un torneo recreativo debe tener valor de bono para el ganador.");
		}

		String id = club.generarIdTorneo();
		String deporteMayus = deporte.trim().toUpperCase();
		Torneo torneo;
		if (deporteMayus.equals("TENIS") || deporteMayus.equals("PADEL") || deporteMayus.equals("TENIS_DE_MESA")) {
			torneo = new TorneoIndividual(id, nombre, deporteMayus, convertirModalidad(modalidad), categoria, cupos, tipo, fechaInicio, tarifa, bono);
		} else if (deporteMayus.equals("FUTBOL") || deporteMayus.equals("BALONCESTO") || deporteMayus.equals("VOLEIBOL")) {
			torneo = new TorneoEquipo(id, nombre, deporteMayus, categoria, cupos, tipo, fechaInicio, tarifa, bono);
		} else {
			throw new IllegalArgumentException("Deporte no valido: " + deporte);
		}
		club.agregarTorneo(torneo);
		return torneo;
	}

	private Modalidad convertirModalidad(String modalidad) {
		if (modalidad == null) {
			throw new IllegalArgumentException("Debe indicar la modalidad (INDIVIDUAL o DOBLES).");
		}
		String m = modalidad.trim().toUpperCase();
		if (m.equals("INDIVIDUAL") || m.equals("SENCILLOS")) {
			return Modalidad.SENCILLOS;
		}
		if (m.equals("DOBLES")) {
			return Modalidad.DOBLES;
		}
		throw new IllegalArgumentException("Modalidad no valida: " + modalidad);
	}

	private Torneo obtenerTorneo(String id) {
		Torneo torneo = club.buscarTorneo(id);
		if (torneo == null) {
			throw new IllegalArgumentException("No existe un torneo con id " + id);
		}
		return torneo;
	}

	/** HU-A02 */
	public EstadoTorneo consultarEstadoTorneo(String id) {
		return obtenerTorneo(id).getEstado();
	}

	/** HU-A03 */
	public String consultarHistorialTorneo(String id) {
		return obtenerTorneo(id).consultarHistorial();
	}

	/** HU-A05 */
	public void finalizarTorneo(String id) {
		obtenerTorneo(id).finalizar();
	}

	/** HU-A06 - torneo por equipos */
	public void registrarResultadoTorneo(String id, PartidoTorneo partido, ResultadoPartidoEquipo resultado) {
		Torneo torneo = obtenerTorneo(id);
		if (!(torneo instanceof TorneoEquipo)) {
			throw new IllegalArgumentException("El torneo " + id + " no es por equipos.");
		}
		((TorneoEquipo) torneo).registrarResultado(partido, resultado);
	}

	/** HU-A06 - torneo individual: una estadistica por participante, en el orden del partido */
	public void registrarResultadoTorneo(String id, PartidoDeporteIndividual partido, List<EstadisticasPartidoIndividual> resultado) {
		Torneo torneo = obtenerTorneo(id);
		if (!(torneo instanceof TorneoIndividual)) {
			throw new IllegalArgumentException("El torneo " + id + " no es individual.");
		}
		((TorneoIndividual) torneo).registrarResultado(partido, resultado);
	}
}
