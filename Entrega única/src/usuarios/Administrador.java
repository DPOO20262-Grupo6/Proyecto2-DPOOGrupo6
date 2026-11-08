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
}