package presentacion;

import java.util.ArrayList;
import java.util.List;

import Torneos.ReglasSocioTorneo;
import Torneos.Torneo;
import Torneos.TorneoEquipo;
import Torneos.TorneoIndividual;
import articulosYVentas.Bono;
import club.ClubDeportivoSeneca;
import deportesDeConjunto.Equipo;
import enumeraciones.Modalidad;
import inscripciones.InscripcionDisciplinaIndividual;
import inscripciones.InscripcionParticipanteExterno;
import usuarios.ParticipanteExterno;
import usuarios.Socio;

/**
 * Acciones del socio en los torneos internos: consultar, inscribir y retirar equipos, jugadores,
 * parejas, estudiantes y personas externas, y ver sus bonos. Las reglas viven en ReglasSocioTorneo.
 */
public class AccionesTorneosSocio extends AccionesSocioBase {

	private final ReglasSocioTorneo reglas;

	public AccionesTorneosSocio(ClubDeportivoSeneca club, Socio socio, LectorEntradas lector) {
		super(club, socio, lector);
		this.reglas = new ReglasSocioTorneo(club);
	}

	public Menu construirMenu() {
		Menu menu = new Menu("Torneos", "Volver");
		menu.agregarOpcion("Ver los torneos y su estado", this::verTorneos);
		menu.agregarOpcion("Inscribir a mi equipo", this::inscribirEquipo);
		menu.agregarOpcion("Inscribirme como jugador", this::inscribirJugador);
		menu.agregarOpcion("Inscribirme con una pareja (torneo de dobles)", this::inscribirPareja);
		menu.agregarOpcion("Inscribir a un estudiante o persona externa", this::inscribirInvitado);
		menu.agregarOpcion("Retirar a mi equipo", this::retirarEquipo);
		menu.agregarOpcion("Retirarme (y a mi pareja)", this::retirarJugador);
		menu.agregarOpcion("Retirar a una persona que inscribi", this::retirarInvitado);
		menu.agregarOpcion("Mis torneos", this::verMisTorneos);
		menu.agregarOpcion("Historial de un torneo", this::verHistorial);
		menu.agregarOpcion("Mis bonos", this::verMisBonos);
		return menu;
	}

	// ---------------- Consultas ----------------

	private void verTorneos() {
		lector.mostrar("--- Torneos ---");
		List<Torneo> torneos = reglas.consultarTorneos();
		if (torneos.isEmpty()) {
			lector.mostrar("No hay torneos registrados.");
			return;
		}
		listar(torneos, reglas::describirTorneo);
	}

	private void verMisTorneos() {
		lector.mostrar("--- Mis torneos ---");
		List<Torneo> torneos = reglas.consultarMisTorneos(socio);
		if (torneos.isEmpty()) {
			lector.mostrar("No participa en ningun torneo.");
			return;
		}
		listar(torneos, reglas::describirTorneo);
	}

	private void verHistorial() {
		lector.mostrar("--- Historial de un torneo ---");
		lector.mostrar(reglas.consultarHistorial(lector.leerTexto("Id del torneo: ")));
	}

	private void verMisBonos() {
		lector.mostrar("--- Mis bonos ---");
		List<Bono> bonos = socio.getBonos();
		if (bonos.isEmpty()) {
			lector.mostrar("No tiene bonos. Se ganan al ganar un torneo recreativo y se usan en la tienda o la cafeteria.");
			return;
		}
		listar(bonos, b -> b.getCodigo() + " | descuento " + b.describir() + (b.isUsado() ? " | usado" : " | disponible"));
	}

	// ---------------- Inscripciones ----------------

	private void inscribirEquipo() {
		lector.mostrar("--- Inscribir a mi equipo ---");
		List<Equipo> equipos = reglas.consultarEquiposDelSocio(socio);
		if (equipos.isEmpty()) {
			lector.mostrar("No pertenece a ningun equipo activo.");
			return;
		}
		Equipo equipo = elegir("Equipo (numero): ", equipos, e -> e.getNombre() + " (" + e.getDeporte() + ", " + e.getCategoria() + ")");
		Torneo torneo = elegirTorneoAbierto(TorneoEquipo.class);
		if (torneo == null) {
			return;
		}
		if (!confirmarInscripcion(torneo)) {
			return;
		}
		reglas.inscribirEquipo(socio, torneo.getId(), equipo);
		lector.mostrar("El equipo " + equipo.getNombre() + " quedo inscrito en " + torneo.getNombre() + " con cupo "
				+ ((TorneoEquipo) torneo).getTipoCupo(equipo) + ".");
	}

	private void inscribirJugador() {
		lector.mostrar("--- Inscribirme como jugador ---");
		Torneo torneo = elegirTorneoAbierto(TorneoIndividual.class);
		if (torneo == null) {
			return;
		}
		if (!confirmarInscripcion(torneo)) {
			return;
		}
		InscripcionDisciplinaIndividual inscripcion = reglas.inscribirIndividual(socio, torneo.getId());
		lector.mostrar("Quedo inscrito en " + torneo.getNombre() + " con cupo " + inscripcion.getTipoCupo() + ".");
	}

	private void inscribirPareja() {
		lector.mostrar("--- Inscribirme con una pareja ---");
		Torneo torneo = elegirTorneoAbierto(TorneoIndividual.class);
		if (torneo == null) {
			return;
		}
		String loginPareja = lector.leerTexto("Login de su pareja: ");
		if (!confirmarInscripcion(torneo)) {
			return;
		}
		reglas.inscribirPareja(socio, loginPareja, torneo.getId());
		lector.mostrar("La pareja quedo inscrita en " + torneo.getNombre() + ". Cada jugador ocupa un cupo.");
	}

	private void inscribirInvitado() {
		lector.mostrar("--- Inscribir a un estudiante o persona externa ---");
		Torneo torneo = elegirTorneoAbierto(TorneoIndividual.class);
		if (torneo == null) {
			return;
		}
		String tipo = lector.leerOpcion("Tipo (ESTUDIANTE_MATRICULADO o EXTERNO): ", ParticipanteExterno.ESTUDIANTE_MATRICULADO, ParticipanteExterno.EXTERNO);
		String nombre = lector.leerTexto("Nombre completo: ");
		String documento = lector.leerTexto("Documento: ");
		if (!confirmarInscripcion(torneo)) {
			return;
		}
		InscripcionParticipanteExterno inscripcion = reglas.inscribirParticipante(socio, torneo.getId(), nombre, documento, tipo);
		lector.mostrar(inscripcion.getSocio().getNombre() + " quedo inscrito en " + torneo.getNombre() + " con cupo " + inscripcion.getTipoCupo() + ".");
	}

	// ---------------- Retiros ----------------

	private void retirarEquipo() {
		lector.mostrar("--- Retirar a mi equipo ---");
		List<Equipo> equipos = reglas.consultarEquiposDelSocio(socio);
		if (equipos.isEmpty()) {
			lector.mostrar("No pertenece a ningun equipo activo.");
			return;
		}
		Equipo equipo = elegir("Equipo (numero): ", equipos, e -> e.getNombre() + " (" + e.getDeporte() + ")");
		String id = lector.leerTexto("Id del torneo: ");
		if (!lector.leerConfirmacion("Confirma el retiro del equipo " + equipo.getNombre() + "?")) {
			lector.mostrar("Retiro no realizado.");
			return;
		}
		reglas.retirarEquipo(socio, id, equipo);
		lector.mostrar("El equipo " + equipo.getNombre() + " se retiro y su cupo quedo libre.");
	}

	private void retirarJugador() {
		lector.mostrar("--- Retirarme de un torneo ---");
		String id = lector.leerTexto("Id del torneo: ");
		if (!lector.leerConfirmacion("Confirma el retiro? Si se inscribio con pareja, ella tambien se retira")) {
			lector.mostrar("Retiro no realizado.");
			return;
		}
		reglas.retirarJugador(socio, id);
		lector.mostrar("Quedo retirado y sus cupos quedaron libres.");
	}

	private void retirarInvitado() {
		lector.mostrar("--- Retirar a una persona que inscribi ---");
		String id = lector.leerTexto("Id del torneo: ");
		String documento = lector.leerTexto("Documento de la persona: ");
		if (!lector.leerConfirmacion("Confirma el retiro?")) {
			lector.mostrar("Retiro no realizado.");
			return;
		}
		reglas.retirarParticipante(socio, id, documento);
		lector.mostrar("La persona se retiro y su cupo quedo libre.");
	}

	// ---------------- Apoyo ----------------

	/**
	 * Muestra los torneos abiertos del tipo pedido y pide el id de uno.
	 * @return el torneo elegido, o null si no hay torneos abiertos de ese tipo
	 */
	private Torneo elegirTorneoAbierto(Class<? extends Torneo> tipo) {
		List<Torneo> abiertos = new ArrayList<Torneo>();
		List<Torneo> todos = reglas.consultarTorneosAbiertos();
		for (int i = 0; i < todos.size(); i++) {
			if (tipo.isInstance(todos.get(i))) {
				abiertos.add(todos.get(i));
			}
		}
		if (abiertos.isEmpty()) {
			lector.mostrar("No hay torneos abiertos de este tipo.");
			return null;
		}
		listar(abiertos, reglas::describirTorneo);
		return reglas.obtenerTorneo(lector.leerTexto("Id del torneo: "));
	}

	private boolean confirmarInscripcion(Torneo torneo) {
		lector.mostrar("Torneo: " + torneo.getNombre() + " (" + torneo.getTipo() + ") el " + torneo.getFechaInicio());
		lector.mostrar("Valor de la inscripcion: $" + reglas.calcularValorInscripcion(torneo) + " (el pago lo gestiona un tercero)");
		if (torneo instanceof TorneoIndividual && ((TorneoIndividual) torneo).getModalidad() == Modalidad.DOBLES) {
			lector.mostrar("Es un torneo de dobles: puede inscribirse solo o con una pareja.");
		}
		boolean confirmada = lector.leerConfirmacion("Confirma la inscripcion?");
		if (!confirmada) {
			lector.mostrar("Inscripcion no realizada.");
		}
		return confirmada;
	}
}
