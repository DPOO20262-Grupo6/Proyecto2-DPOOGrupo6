package presentacion;

import java.util.List;

import Torneos.ReglasEmpleadoTorneo;
import Torneos.Torneo;
import Torneos.TorneoIndividual;
import administracion.TurnoEmpleado;
import enumeraciones.TipoTorneo;
import inscripciones.InscripcionEmpleadoTorneo;
import usuarios.EmpleadoDelClub;

/**
 * Consola de los empleados del club (entrenadores y fisioterapeutas).
 * Permite consultar los turnos y participar en los torneos de disciplinas individuales.
 */
public class ConsolaEmpleado extends ConsolaBase<EmpleadoDelClub> {

	private ReglasEmpleadoTorneo reglas;

	public static void main(String[] args) {
		new ConsolaEmpleado().ejecutar();
	}

	@Override
	protected Class<EmpleadoDelClub> getTipoUsuario() {
		return EmpleadoDelClub.class;
	}

	@Override
	protected String getNombreConsola() {
		return "Empleado";
	}

	@Override
	protected Menu construirMenuPrincipal() {
		reglas = new ReglasEmpleadoTorneo(club);

		Menu torneos = new Menu("Torneos individuales", "Volver");
		torneos.agregarOpcion("Consultar torneos disponibles", this::consultarTorneosDisponibles);
		torneos.agregarOpcion("Inscribirme en un torneo", this::inscribirme);
		torneos.agregarOpcion("Retirarme de un torneo", this::retirarme);
		torneos.agregarOpcion("Consultar mis torneos", this::consultarMisTorneos);
		torneos.agregarOpcion("Consultar el historial de un torneo", this::consultarHistorial);

		Menu principal = new Menu("Consola del Empleado - " + usuario.getNombre() + " (" + getCargo() + ")", "Guardar y salir");
		principal.agregarOpcion("Consultar mis turnos", this::consultarTurnos);
		principal.agregarSubmenu("Torneos individuales", torneos, lector);
		principal.agregarOpcion("Consultar mis datos", this::consultarMisDatos);
		return principal;
	}

	/** Entrenador o Fisioterapeuta, segun la clase del empleado que inicio sesion. */
	private String getCargo() {
		return usuario.getClass().getSimpleName();
	}

	private void consultarTurnos() {
		lector.mostrar("--- Mis turnos ---");
		List<TurnoEmpleado> turnos = reglas.consultarTurnos(usuario);
		if (turnos.isEmpty()) {
			lector.mostrar("No tiene turnos programados.");
			return;
		}
		for (int i = 0; i < turnos.size(); i++) {
			TurnoEmpleado turno = turnos.get(i);
			lector.mostrar(turno.getFecha() + " | " + turno.getHora() + " a " + turno.getHora().plusHours(turno.getDuracion())
					+ " (" + turno.getDuracion() + " horas)");
		}
	}

	private void consultarTorneosDisponibles() {
		lector.mostrar("--- Torneos individuales disponibles ---");
		String filtro = lector.leerTextoOpcional("Disciplina (TENIS, PADEL, TENIS_DE_MESA) o Enter para ver todas: ");
		String disciplina = filtro.isEmpty() ? null : filtro.toUpperCase().replace(' ', '_');
		mostrarDisponibles(reglas.consultarTorneosDisponibles(usuario, disciplina));
	}

	private boolean mostrarDisponibles(List<TorneoIndividual> torneos) {
		if (torneos.isEmpty()) {
			lector.mostrar("No hay torneos individuales disponibles.");
			return false;
		}
		for (int i = 0; i < torneos.size(); i++) {
			TorneoIndividual torneo = torneos.get(i);
			String turno = reglas.estaDeTurno(usuario, torneo.getFechaInicio()) ? "tiene turno ese dia" : "sin turno ese dia";
			String inscrito = reglas.estaInscrito(usuario, torneo) ? " | YA INSCRITO" : "";
			lector.mostrar(describir(torneo) + " | cupos regulares: " + reglas.cuposParaEmpleados(torneo)
					+ " | costo para usted: $" + reglas.calcularValorInscripcion(usuario, torneo) + " | " + turno + inscrito);
		}
		return true;
	}

	private String describir(TorneoIndividual torneo) {
		return torneo.getId() + " | " + torneo.getNombre() + " | " + torneo.getDeporte() + " " + torneo.getModalidad()
				+ " | " + torneo.getCategoria() + " | " + torneo.getTipo() + " | " + torneo.getFechaInicio();
	}

	private void inscribirme() {
		lector.mostrar("--- Inscribirme en un torneo ---");
		if (!mostrarDisponibles(reglas.consultarTorneosDisponibles(usuario))) {
			return;
		}
		String id = lector.leerTexto("Id del torneo: ");
		Torneo torneo = club.buscarTorneo(id);
		reglas.validarInscripcion(usuario, torneo);

		lector.mostrar("Torneo: " + torneo.getNombre() + " (" + torneo.getTipo() + ") el " + torneo.getFechaInicio());
		lector.mostrar("Valor de la inscripcion para empleados: $" + reglas.calcularValorInscripcion(usuario, torneo));
		if (torneo.getTipo() == TipoTorneo.COMPETITIVO) {
			lector.mostrar("Aviso: como empleado participa sin optar al premio en efectivo.");
		}
		if (!lector.leerConfirmacion("Confirma la inscripcion?")) {
			lector.mostrar("Inscripcion no realizada.");
			return;
		}

		InscripcionEmpleadoTorneo inscripcion = reglas.inscribir(usuario, id);
		lector.mostrar("Inscripcion registrada en " + torneo.getNombre() + ". Valor pagado: $" + inscripcion.getValorPagado()
				+ ". Cupos regulares restantes: " + reglas.cuposParaEmpleados(torneo) + ".");
	}

	private void retirarme() {
		lector.mostrar("--- Retirarme de un torneo ---");
		if (!mostrarMisTorneos()) {
			return;
		}
		String id = lector.leerTexto("Id del torneo: ");
		Torneo torneo = club.buscarTorneo(id);
		if (torneo == null) {
			throw new IllegalArgumentException("No existe un torneo con id " + id + ".");
		}
		if (!lector.leerConfirmacion("Confirma el retiro de " + torneo.getNombre() + "?")) {
			lector.mostrar("Retiro no realizado.");
			return;
		}
		reglas.retirar(usuario, id);
		lector.mostrar("Quedo retirado de " + torneo.getNombre() + ". Cupos disponibles del torneo: " + torneo.cuposDisponibles() + ".");
	}

	private void consultarMisTorneos() {
		lector.mostrar("--- Mis torneos ---");
		mostrarMisTorneos();
	}

	private boolean mostrarMisTorneos() {
		List<TorneoIndividual> torneos = reglas.consultarTorneosInscritos(usuario);
		if (torneos.isEmpty()) {
			lector.mostrar("No esta inscrito en ningun torneo.");
			return false;
		}
		for (int i = 0; i < torneos.size(); i++) {
			lector.mostrar(describir(torneos.get(i)) + " | " + reglas.describirEstado(torneos.get(i)));
		}
		return true;
	}

	private void consultarHistorial() {
		lector.mostrar("--- Historial de un torneo ---");
		String id = lector.leerTexto("Id del torneo: ");
		lector.mostrar(reglas.consultarHistorial(id));
	}

	private void consultarMisDatos() {
		lector.mostrar("--- Mis datos ---");
		lector.mostrar("Nombre: " + usuario.getNombre());
		lector.mostrar("Cargo: " + getCargo());
		lector.mostrar("Login: " + usuario.getLogin());
		lector.mostrar("Codigo de descuento: " + usuario.getCodigoDescuento());
		lector.mostrar("Puntos de fidelidad: " + usuario.getPuntosDeFidelidad());
	}
}
