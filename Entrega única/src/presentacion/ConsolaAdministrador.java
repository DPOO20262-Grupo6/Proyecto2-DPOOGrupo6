package presentacion;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import Torneos.Torneo;
import Torneos.TorneoIndividual;
import administracion.PagoMensualidad;
import administracion.SolicitudCambioTurnoEmpleado;
import administracion.Sugerencia;
import administracion.TurnoEmpleado;
import articulosYVentas.Articulo;
import club.ClubDeportivoSeneca;
import deportesDeConjunto.Ascun;
import deportesDeConjunto.Competencia;
import deportesDeConjunto.Equipo;
import deportesDeConjunto.NominaCompetencia;
import deportesDeConjunto.PartidoDeCompetencia;
import enumeraciones.EstadoSolicitud;
import enumeraciones.TipoTorneo;
import inscripciones.InscripcionEquipo;
import inventario.UbicacionInventario;
import usuarios.Administrador;
import usuarios.EmpleadoDelClub;
import usuarios.Entrenador;
import usuarios.Socio;
import usuarios.Usuario;


public class ConsolaAdministrador {

	private static final String RUTA_DATOS = "./datos";
	private static final String MENSAJE_NO_EXISTE = "El torneo no existe";

	private ClubDeportivoSeneca club;
	private Scanner scanner;
	private Administrador administrador;

	public ConsolaAdministrador(ClubDeportivoSeneca club) {
		this.club = club;
		this.scanner = new Scanner(System.in);
	}

	public static void main(String[] args) {
		ClubDeportivoSeneca club = new ClubDeportivoSeneca();
		try {
			club.cargarDatos(RUTA_DATOS);
			System.out.println("Datos cargados desde " + RUTA_DATOS);
		} catch (Exception e) {
			System.out.println("No se pudieron cargar datos previos (" + e.getMessage() + "). Se inicia un club vacio.");
		}
		ConsolaAdministrador consola = new ConsolaAdministrador(club);
		consola.ejecutar();
	}

	public void ejecutar() {
		iniciarSesion();
		boolean continuar = true;
		while (continuar) {
			System.out.println("===== Consola del Administrador =====");
			System.out.println("1. Socios y mensualidades");
			System.out.println("2. Equipos");
			System.out.println("3. Competencias");
			System.out.println("4. Inventario y sugerencias");
			System.out.println("5. Turnos del personal");
			System.out.println("6. Historial y reportes");
			System.out.println("7. Torneos internos");
			System.out.println("0. Guardar y salir");
			String opcion = leerOpcion();
			if (opcion.equals("1")) {
				menuSocios();
			} else if (opcion.equals("2")) {
				menuEquipos();
			} else if (opcion.equals("3")) {
				menuCompetencias();
			} else if (opcion.equals("4")) {
				menuInventario();
			} else if (opcion.equals("5")) {
				menuTurnos();
			} else if (opcion.equals("6")) {
				menuReportes();
			} else if (opcion.equals("7")) {
				menuTorneos();
			} else if (opcion.equals("0")) {
				guardarYSalir();
				continuar = false;
			} else {
				System.out.println("Opcion no valida.\n");
			}
		}
	}

	private void iniciarSesion() {
		System.out.println("===== Inicio de sesion - Administrador =====");
		while (administrador == null) {
			System.out.print("Usuario: ");
			String login = scanner.nextLine().trim();
			System.out.print("Contrasena: ");
			String password = scanner.nextLine().trim();

			try {
				Usuario usuario = club.autenticarUsuario(login, password);
				if (usuario instanceof Administrador) {
					administrador = (Administrador) usuario;
					System.out.println("Bienvenido, " + administrador.getNombre() + ".");
					System.out.println();
				} else {
					System.out.println("El usuario no tiene permisos de administrador. Intente de nuevo.");
				}
			} catch (IllegalArgumentException e) {
				System.out.println("Login o contrasena incorrectos. Intente de nuevo.");
			}
		}
	}

	private String leerOpcion() {
		System.out.print("Seleccione una opcion: ");
		String opcion = scanner.nextLine().trim();
		System.out.println();
		return opcion;
	}

	private boolean ejecutarAccion(String opcion, String[] opciones, Runnable[] acciones) {
		if (opcion.equals("0")) {
			return false;
		}
		for (int i = 0; i < opciones.length; i++) {
			if (opciones[i].equals(opcion)) {
				try {
					acciones[i].run();
				} catch (Exception e) {
					System.out.println("Error: " + e.getMessage());
				}
				System.out.println();
				return true;
			}
		}
		System.out.println("Opcion no valida.\n");
		return true;
	}

	private void menuSocios() {
		boolean seguir = true;
		while (seguir) {
			System.out.println("--- Socios y mensualidades ---");
			System.out.println("1. Registrar un socio");
			System.out.println("2. Registrar el pago de una mensualidad");
			System.out.println("3. Listar socios");
			System.out.println("0. Volver");
			seguir = ejecutarAccion(leerOpcion(), new String[] { "1", "2", "3" },
					new Runnable[] { this::registrarSocio, this::registrarMensualidad, this::listarSocios });
		}
	}

	private void menuEquipos() {
		boolean seguir = true;
		while (seguir) {
			System.out.println("--- Equipos ---");
			System.out.println("1. Crear un equipo");
			System.out.println("2. Inscribir a un socio en un equipo");
			System.out.println("3. Dar de baja a un jugador");
			System.out.println("4. Dar de baja un equipo");
			System.out.println("5. Listar equipos");
			System.out.println("0. Volver");
			seguir = ejecutarAccion(leerOpcion(), new String[] { "1", "2", "3", "4", "5" },
					new Runnable[] { this::crearEquipo, this::inscribirJugador, this::darDeBajaJugador, this::darDeBajaEquipo, this::listarEquipos });
		}
	}

	private void menuCompetencias() {
		boolean seguir = true;
		while (seguir) {
			System.out.println("--- Competencias ---");
			System.out.println("1. Registrar una competencia");
			System.out.println("2. Inscribir un equipo en una competencia");
			System.out.println("3. Asignar grupo a un equipo (formato GRUPOS)");
			System.out.println("4. Programar un partido oficial");
			System.out.println("5. Listar competencias");
			System.out.println("0. Volver");
			seguir = ejecutarAccion(leerOpcion(), new String[] { "1", "2", "3", "4", "5" },
					new Runnable[] { this::registrarCompetencia, this::inscribirEnCompetencia, this::asignarGrupo, this::programarPartidoOficial, this::listarCompetencias });
		}
	}

	private void menuInventario() {
		boolean seguir = true;
		while (seguir) {
			System.out.println("--- Inventario y sugerencias ---");
			System.out.println("1. Consultar el estado del inventario");
			System.out.println("2. Mover articulos entre ubicaciones");
			System.out.println("3. Reabastecer un producto");
			System.out.println("4. Aprobar o rechazar sugerencias de productos");
			System.out.println("0. Volver");
			seguir = ejecutarAccion(leerOpcion(), new String[] { "1", "2", "3", "4" },
					new Runnable[] { this::consultarInventario, this::moverArticulos, this::reabastecer, this::resolverSugerencia });
		}
	}

	private void menuTurnos() {
		boolean seguir = true;
		while (seguir) {
			System.out.println("--- Turnos del personal ---");
			System.out.println("1. Programar los turnos de un dia");
			System.out.println("2. Resolver solicitudes de cambio de turno");
			System.out.println("3. Listar empleados y turnos");
			System.out.println("0. Volver");
			seguir = ejecutarAccion(leerOpcion(), new String[] { "1", "2", "3" },
					new Runnable[] { this::programarTurnos, this::resolverSolicitudCambio, this::listarTurnos });
		}
	}

	private void menuReportes() {
		boolean seguir = true;
		while (seguir) {
			System.out.println("--- Historial y reportes ---");
			System.out.println("1. Consultar historial de reservas, partidos y sesiones");
			System.out.println("2. Consultar reporte de ventas");
			System.out.println("3. Consultar reporte deportivo");
			System.out.println("0. Volver");
			seguir = ejecutarAccion(leerOpcion(), new String[] { "1", "2", "3" },
					new Runnable[] { this::consultarHistorialGeneral, this::consultarReporteVentas, this::consultarReporteDeportivo });
		}
	}

	private void menuTorneos() {
		boolean seguir = true;
		while (seguir) {
			System.out.println("--- Torneos internos ---");
			System.out.println("1. Crear torneo");
			System.out.println("2. Consultar estado de un torneo");
			System.out.println("3. Consultar historial de un torneo");
			System.out.println("4. Consultar cupos preferenciales de un torneo");
			System.out.println("5. Listar torneos");
			System.out.println("0. Volver");
			seguir = ejecutarAccion(leerOpcion(), new String[] { "1", "2", "3", "4", "5" },
					new Runnable[] { this::crearTorneoSeguro, this::consultarEstado, this::consultarHistorial, this::consultarCuposPreferenciales, this::listarTorneos });
		}
	}

	private void registrarSocio() {
		System.out.println("--- Registrar socio ---");
		String nombre = leerTexto("Nombre: ");
		String documento = leerTexto("Documento: ");
		LocalDate nacimiento = leerFecha("Fecha de nacimiento (AAAA-MM-DD): ");
		String login = leerTexto("Login: ");
		String password = leerTexto("Contrasena: ");
		Socio socio = administrador.registrarSocio(nombre, documento, nacimiento, login, password);
		System.out.println("Socio registrado y activo: " + socio.getNombre() + " (" + socio.getLogin() + ").");
	}

	private void registrarMensualidad() {
		System.out.println("--- Registrar pago de mensualidad ---");
		String login = leerTexto("Login del socio: ");
		YearMonth mes = leerMes("Mes (AAAA-MM): ");
		Double valor = leerDouble("Valor: ");
		PagoMensualidad pago = administrador.registrarPagoMensualidad(login, mes, valor == null ? 0 : valor);
		System.out.println("Pago registrado en el rubro mensualidades: " + pago);
	}

	private void listarSocios() {
		System.out.println("--- Socios ---");
		if (club.getSocios().isEmpty()) {
			System.out.println("No hay socios registrados.");
		}
		for (int i = 0; i < club.getSocios().size(); i++) {
			Socio socio = club.getSocios().get(i);
			System.out.println(socio.getLogin() + " | " + socio.getNombre() + " | documento " + socio.getDocumento()
					+ " | " + (socio.isActivo() ? "activo" : "inactivo"));
		}
	}

	private void crearEquipo() {
		System.out.println("--- Crear equipo ---");
		String deporte = leerTexto("Deporte (FUTBOL, BALONCESTO, VOLEIBOL): ");
		String nombre = leerTexto("Nombre: ");
		String categoria = leerTexto("Categoria (SUB-13, SUB-15, SUB-17, MAYORES): ");
		Integer cupo = leerEntero("Cupo maximo de jugadores: ");
		Equipo equipo = administrador.crearEquipo(deporte, nombre, categoria, cupo == null ? 0 : cupo);
		System.out.println("Equipo creado: " + equipo.getNombre() + " (" + equipo.getDeporte() + ", " + equipo.getCategoria() + "), activo y sin jugadores.");
	}

	private void inscribirJugador() {
		System.out.println("--- Inscribir socio en equipo ---");
		Equipo equipo = pedirEquipo();
		Socio socio = pedirSocio();
		String posicion = leerTexto("Posicion: ");
		Integer camiseta = leerEntero("Numero de camiseta: ");
		administrador.inscribirJugador(equipo, socio, posicion, camiseta == null ? -1 : camiseta);
		System.out.println("Socio " + socio.getNombre() + " inscrito en " + equipo.getNombre() + " con la camiseta " + camiseta + ".");
	}

	private void darDeBajaJugador() {
		System.out.println("--- Dar de baja a un jugador ---");
		Equipo equipo = pedirEquipo();
		Integer camiseta = leerEntero("Numero de camiseta del jugador: ");
		InscripcionEquipo jugador = null;
		for (int i = 0; i < equipo.getInscripciones().size(); i++) {
			if (camiseta != null && equipo.getInscripciones().get(i).getNumeroCamiseta() == camiseta) {
				jugador = equipo.getInscripciones().get(i);
			}
		}
		if (jugador == null) {
			throw new IllegalArgumentException("No hay un jugador con esa camiseta en el equipo (ya fue dado de baja o no existe).");
		}
		administrador.darDeBajaJugador(equipo, jugador);
		System.out.println("Jugador " + jugador.getSocio().getNombre() + " dado de baja. Se liberaron su cupo y su camiseta.");
	}

	private void darDeBajaEquipo() {
		System.out.println("--- Dar de baja un equipo ---");
		Equipo equipo = pedirEquipo();
		administrador.darDeBajaEquipo(equipo);
		System.out.println("Equipo " + equipo.getNombre() + " inactivo. Sus integrantes quedaron liberados y su historial se conserva.");
	}

	private void listarEquipos() {
		System.out.println("--- Equipos ---");
		if (club.getEquipos().isEmpty()) {
			System.out.println("No hay equipos registrados.");
		}
		for (int i = 0; i < club.getEquipos().size(); i++) {
			Equipo equipo = club.getEquipos().get(i);
			System.out.println(equipo.getNombre() + " | " + equipo.getDeporte() + " | " + equipo.getCategoria() + " | "
					+ equipo.getInscripciones().size() + "/" + equipo.getCupoMaximoJugadores() + " jugadores | "
					+ (equipo.isActivo() ? "activo" : "inactivo"));
			for (int j = 0; j < equipo.getInscripciones().size(); j++) {
				InscripcionEquipo jugador = equipo.getInscripciones().get(j);
				System.out.println("   #" + jugador.getNumeroCamiseta() + " " + jugador.getSocio().getNombre() + " (" + jugador.getPosicion() + ")");
			}
		}
	}

	private void registrarCompetencia() {
		System.out.println("--- Registrar competencia ---");
		String nombre = leerTexto("Nombre: ");
		String entidad = leerTexto("Entidad organizadora: ");
		Integer temporada = leerEntero("Temporada (anio): ");
		String deporte = leerTexto("Deporte (FUTBOL, BALONCESTO, VOLEIBOL): ");
		String categoria = leerTexto("Categoria: ");
		String formato = leerTexto("Formato (LIGA o GRUPOS): ");
		Competencia competencia = administrador.registrarCompetencia(nombre, entidad, temporada == null ? 0 : temporada, deporte, categoria, formato);
		System.out.println("Competencia registrada: " + competencia.getNombre() + " (" + competencia.getFormato() + ").");
	}

	private void inscribirEnCompetencia() {
		System.out.println("--- Inscribir equipo en competencia ---");
		Competencia competencia = pedirCompetencia();
		Equipo equipo = pedirEquipo();
		String camisetas = leerTexto("Camisetas de la nomina separadas por coma (vacio = todos los jugadores activos): ");
		List<InscripcionEquipo> nomina = new ArrayList<InscripcionEquipo>();
		for (int i = 0; i < equipo.getInscripciones().size(); i++) {
			InscripcionEquipo jugador = equipo.getInscripciones().get(i);
			boolean elegido = camisetas.isEmpty() ? jugador.isActiva() : ("," + camisetas.replace(" ", "") + ",").contains("," + jugador.getNumeroCamiseta() + ",");
			if (elegido) {
				nomina.add(jugador);
			}
		}
		NominaCompetencia inscrita = administrador.inscribirEquipoEnCompetencia(equipo, competencia, nomina);
		System.out.println("Equipo " + equipo.getNombre() + " inscrito en " + competencia.getNombre() + " con " + inscrita.getJugadores().size() + " jugadores en la nomina.");
	}

	private void asignarGrupo() {
		System.out.println("--- Asignar grupo ---");
		Competencia competencia = pedirCompetencia();
		if (!(competencia instanceof Ascun)) {
			throw new IllegalArgumentException("La competencia no tiene formato de grupos.");
		}
		Equipo equipo = pedirEquipo();
		String grupo = leerTexto("Grupo: ");
		((Ascun) competencia).asignarGrupo(equipo, grupo);
		System.out.println("Equipo " + equipo.getNombre() + " asignado al grupo " + grupo.toUpperCase() + ".");
	}

	private void programarPartidoOficial() {
		System.out.println("--- Programar partido oficial ---");
		Competencia competencia = pedirCompetencia();
		Equipo equipo = pedirEquipo();
		LocalDate fecha = leerFecha("Fecha (AAAA-MM-DD): ");
		String rival = leerTexto("Institucion rival: ");
		String escenario = leerTexto("Escenario: ");
		EmpleadoDelClub empleado = pedirEmpleado("Login del entrenador responsable: ");
		if (!(empleado instanceof Entrenador)) {
			throw new IllegalArgumentException("Falta el entrenador responsable.");
		}
		PartidoDeCompetencia partido = administrador.programarPartidoOficial(competencia, equipo, rival, escenario, fecha, (Entrenador) empleado);
		System.out.println("Partido programado: " + partido + " el " + partido.getFecha() + ", responsable " + empleado.getNombre() + ".");
	}

	private void listarCompetencias() {
		System.out.println("--- Competencias ---");
		if (club.getCompetencias().isEmpty()) {
			System.out.println("No hay competencias registradas.");
		}
		for (int i = 0; i < club.getCompetencias().size(); i++) {
			Competencia competencia = club.getCompetencias().get(i);
			System.out.println(competencia.getNombre() + " | " + competencia.getEntidadOrganizadora() + " | " + competencia.getAnioTemporada()
					+ " | " + competencia.getDeporte() + " | " + competencia.getCategoriaHabilitada() + " | " + competencia.getFormato()
					+ " | " + competencia.getNominas().size() + " equipos | " + (competencia.isEnCurso() ? "en curso" : "finalizada"));
		}
	}

	private void consultarInventario() {
		System.out.println("--- Estado del inventario ---");
		String ubicacion = leerTexto("Ubicacion a filtrar (vacio = todas): ");
		String inventario = club.consultarInventario(ubicacion.isEmpty() ? null : ubicacion);
		System.out.println(inventario.isEmpty() ? "No hay productos ni instalaciones registrados." : inventario);
	}

	private void moverArticulos() {
		System.out.println("--- Mover articulos ---");
		Articulo articulo = pedirProducto();
		UbicacionInventario origen = pedirUbicacion("Ubicacion de origen: ");
		UbicacionInventario destino = pedirUbicacion("Ubicacion de destino: ");
		Integer cantidad = leerEntero("Cantidad: ");
		administrador.trasladarArticulo(articulo, origen, destino, cantidad == null ? 0 : cantidad);
		System.out.println("Movimiento registrado. " + origen.getNombre() + ": " + origen.obtenerCantidad(articulo) + " | "
				+ destino.getNombre() + ": " + destino.obtenerCantidad(articulo));
	}

	private void reabastecer() {
		System.out.println("--- Reabastecer ---");
		Articulo articulo = pedirProducto();
		UbicacionInventario ubicacion = pedirUbicacion("Ubicacion: ");
		Integer cantidad = leerEntero("Cantidad: ");
		administrador.reabastecer(articulo, ubicacion, cantidad == null ? 0 : cantidad);
		System.out.println("Existencia de " + articulo.getNombre() + " en " + ubicacion.getNombre() + ": " + ubicacion.obtenerCantidad(articulo));
	}

	private void resolverSugerencia() {
		System.out.println("--- Sugerencias pendientes ---");
		List<Sugerencia> pendientes = new ArrayList<Sugerencia>();
		for (int i = 0; i < club.getSugerencias().size(); i++) {
			if (club.getSugerencias().get(i).getEstado() == EstadoSolicitud.PENDIENTE) {
				pendientes.add(club.getSugerencias().get(i));
			}
		}
		if (pendientes.isEmpty()) {
			System.out.println("No hay sugerencias pendientes.");
			return;
		}
		for (int i = 0; i < pendientes.size(); i++) {
			Sugerencia sugerencia = pendientes.get(i);
			System.out.println((i + 1) + ". " + sugerencia.getNombrePropuesto() + " ($" + sugerencia.getPrecioPropuesto() + ") - "
					+ sugerencia.getDescripcion() + " | autor: " + sugerencia.getAutor());
		}
		Sugerencia elegida = pendientes.get(pedirIndice(pendientes.size()));
		String decision = leerTexto("Aprobar (A) o rechazar (R): ").toUpperCase();
		if (decision.equals("A")) {
			administrador.evaluarSugerencia(elegida, true, null);
			System.out.println("Sugerencia aprobada: el producto entra al catalogo con cantidad 0.");
		} else if (decision.equals("R")) {
			administrador.evaluarSugerencia(elegida, false, leerTexto("Motivo del rechazo: "));
			System.out.println("Sugerencia rechazada. El empleado podra ver el motivo.");
		} else {
			System.out.println("Opcion no valida.");
		}
	}

	private void programarTurnos() {
		System.out.println("--- Programar turnos de un dia ---");
		System.out.println("Horario de atencion: " + ClubDeportivoSeneca.HORA_APERTURA + " a " + ClubDeportivoSeneca.HORA_CIERRE
				+ ". Se exigen 2 entrenadores y 1 fisioterapeuta en todo momento.");
		LocalDate fecha = leerFecha("Fecha (AAAA-MM-DD): ");
		List<TurnoEmpleado> programacion = new ArrayList<TurnoEmpleado>();
		while (true) {
			String login = leerTexto("Login del empleado (vacio para terminar): ");
			if (login.isEmpty()) {
				break;
			}
			EmpleadoDelClub empleado = buscarEmpleado(login);
			LocalTime hora = leerHora("Hora de inicio (HH:MM): ");
			Integer horas = leerEntero("Duracion en horas: ");
			programacion.add(new TurnoEmpleado(empleado, fecha, hora, horas == null ? 0 : horas));
		}
		club.publicarProgramacion(programacion);
		System.out.println("Programacion publicada con " + programacion.size() + " turnos.");
	}

	private void resolverSolicitudCambio() {
		System.out.println("--- Solicitudes de cambio pendientes ---");
		List<SolicitudCambioTurnoEmpleado> pendientes = new ArrayList<SolicitudCambioTurnoEmpleado>();
		for (int i = 0; i < club.getSolicitudesCambio().size(); i++) {
			if (club.getSolicitudesCambio().get(i).getEstado() == EstadoSolicitud.PENDIENTE) {
				pendientes.add(club.getSolicitudesCambio().get(i));
			}
		}
		if (pendientes.isEmpty()) {
			System.out.println("No hay solicitudes pendientes.");
			return;
		}
		for (int i = 0; i < pendientes.size(); i++) {
			SolicitudCambioTurnoEmpleado solicitud = pendientes.get(i);
			TurnoEmpleado turno = solicitud.getTurno();
			System.out.println((i + 1) + ". " + turno.getEmpleado().getNombre() + ": " + turno.getFecha() + " " + turno.getHora()
					+ " -> " + solicitud.getNuevaFecha() + " " + solicitud.getNuevaHora());
		}
		SolicitudCambioTurnoEmpleado elegida = pendientes.get(pedirIndice(pendientes.size()));
		String decision = leerTexto("Aprobar (A) o rechazar (R): ").toUpperCase();
		if (decision.equals("A")) {
			administrador.evaluarSolicitudCambio(elegida);
			System.out.println("Solicitud aprobada: el turno fue actualizado.");
		} else if (decision.equals("R")) {
			elegida.rechazar(leerTexto("Motivo del rechazo: "));
			System.out.println("Solicitud rechazada.");
		} else {
			System.out.println("Opcion no valida.");
		}
	}

	private void listarTurnos() {
		System.out.println("--- Empleados y turnos ---");
		if (club.getEmpleados().isEmpty()) {
			System.out.println("No hay empleados registrados.");
		}
		for (int i = 0; i < club.getEmpleados().size(); i++) {
			EmpleadoDelClub empleado = club.getEmpleados().get(i);
			String cargo = empleado instanceof Entrenador ? "Entrenador" : "Fisioterapeuta";
			System.out.println(empleado.getLogin() + " | " + empleado.getNombre() + " | " + cargo);
			System.out.print("   " + club.consultarTurnos(empleado).replace("\n", "\n   "));
			System.out.println();
		}
	}

	private void consultarHistorialGeneral() {
		System.out.println("--- Historial ---");
		String tipo = leerTexto("Tipo (RESERVAS, PARTIDOS_INTERNOS, PARTIDOS_OFICIALES, PARTIDOS_PRACTICA, SESIONES): ");
		LocalDate desde = leerFecha("Desde (AAAA-MM-DD): ");
		LocalDate hasta = leerFecha("Hasta (AAAA-MM-DD): ");
		String loginSocio = leerTexto("Filtrar por login de socio (vacio = sin filtro): ");
		String loginEntrenador = leerTexto("Filtrar por login de entrenador (vacio = sin filtro): ");
		String disciplina = leerTexto("Filtrar por disciplina (vacio = sin filtro): ");
		Socio socio = loginSocio.isEmpty() ? null : buscarSocio(loginSocio);
		Entrenador entrenador = null;
		if (!loginEntrenador.isEmpty()) {
			EmpleadoDelClub empleado = buscarEmpleado(loginEntrenador);
			if (!(empleado instanceof Entrenador)) {
				throw new IllegalArgumentException("El empleado " + loginEntrenador + " no es entrenador.");
			}
			entrenador = (Entrenador) empleado;
		}
		System.out.println(club.consultarHistorial(administrador, tipo, desde, hasta, socio, entrenador, disciplina.isEmpty() ? null : disciplina));
	}

	private void consultarReporteVentas() {
		System.out.println("--- Reporte de ventas ---");
		String rubro = leerTexto("Rubro (TIENDA, CAFETERIA, MENSUALIDADES): ");
		String periodo = leerTexto("Periodo (DIARIO, SEMANAL, MENSUAL): ");
		LocalDate fecha = leerFecha("Fecha de referencia (AAAA-MM-DD): ");
		System.out.println(club.consultarReporteVentas(rubro, periodo, fecha));
	}

	private void consultarReporteDeportivo() {
		System.out.println("--- Reporte deportivo ---");
		String tipo = leerTexto("Tipo (ASISTENCIA, RESULTADOS, EVOLUCION_ELO): ");
		LocalDate desde = leerFecha("Desde (AAAA-MM-DD): ");
		LocalDate hasta = leerFecha("Hasta (AAAA-MM-DD): ");
		System.out.println(club.consultarReporteDeportivo(administrador, tipo, desde, hasta));
	}

	private void crearTorneoSeguro() {
		try {
			crearTorneo();
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	private void crearTorneo() throws Exception {
		System.out.println("--- Crear torneo ---");
		String nombre = leerTextoObligatorio("Nombre", "Nombre del torneo: ");
		if (nombre == null) return;

		System.out.println("Deportes: TENIS, PADEL, TENIS_DE_MESA (individuales) | FUTBOL, BALONCESTO, VOLEIBOL (conjunto)");
		String deporte = leerTextoObligatorio("Deporte", "Deporte o disciplina: ");
		if (deporte == null) return;
		deporte = deporte.toUpperCase().replace(' ', '_');

		String modalidad = "CONJUNTO";
		if (esDeporteIndividual(deporte)) {
			modalidad = leerTextoObligatorio("Modalidad", "Modalidad (INDIVIDUAL o DOBLES): ");
			if (modalidad == null) return;
		} else if (!esDeporteConjunto(deporte)) {
			System.out.println("Dato invalido: Deporte. Debe ser uno de los deportes listados.");
			return;
		}

		String categoria = leerTextoObligatorio("Categoria", "Categoria o nivel: ");
		if (categoria == null) return;

		Integer cupos = leerEntero("Numero de cupos: ");
		if (cupos == null) {
			System.out.println("Dato invalido: Cupos. Debe ser un numero entero.");
			return;
		}
		if (cupos <= 0) {
			System.out.println("Dato invalido: Cupos. Debe ser un entero mayor que 0.");
			return;
		}

		String tipoTexto = leerTextoObligatorio("Tipo", "Tipo (RECREATIVO o COMPETITIVO): ");
		if (tipoTexto == null) return;
		TipoTorneo tipo;
		try {
			tipo = TipoTorneo.valueOf(tipoTexto.toUpperCase());
		} catch (IllegalArgumentException e) {
			System.out.println("Dato invalido: Tipo. Debe ser RECREATIVO o COMPETITIVO.");
			return;
		}

		LocalDate fechaInicio = leerFechaOpcional("Fecha de inicio (AAAA-MM-DD): ");
		if (fechaInicio == null) {
			System.out.println("Dato invalido: Fecha de inicio. Use el formato AAAA-MM-DD.");
			return;
		}
		if (fechaInicio.isBefore(LocalDate.now())) {
			System.out.println("Dato invalido: Fecha de inicio. No puede ser una fecha pasada.");
			return;
		}

		double tarifa = 0;
		double bono = 0;
		if (tipo == TipoTorneo.COMPETITIVO) {
			Double valor = leerDouble("Tarifa de inscripcion: ");
			if (valor == null || valor <= 0) {
				System.out.println("Dato invalido: Tarifa de inscripcion. Debe ser un numero mayor que 0.");
				return;
			}
			tarifa = valor;
		} else {
			Double valor = leerDouble("Valor del bono para el ganador: ");
			if (valor == null || valor <= 0) {
				System.out.println("Dato invalido: Valor del bono. Debe ser un numero mayor que 0.");
				return;
			}
			bono = valor;
		}

		Torneo torneo = administrador.crearTorneo(nombre, deporte, modalidad, categoria, cupos, tipo, fechaInicio, tarifa, bono);

		System.out.println("Torneo creado exitosamente.");
		System.out.println("  Id: " + torneo.getId());
		System.out.println("  Estado: " + torneo.getEstado());
		System.out.println("  Cupos preferenciales: " + torneo.getCuposPreferenciales());
		System.out.println("  Cupos regulares: " + (torneo.getCuposTotales() - torneo.getCuposPreferenciales()));
	}

	private void consultarEstado() {
		System.out.println("--- Consultar estado de un torneo ---");
		Torneo torneo = pedirTorneo();
		if (torneo == null) {
			System.out.println(MENSAJE_NO_EXISTE);
			return;
		}
		System.out.println("Torneo: " + torneo.getNombre());
		System.out.println("Estado: " + administrador.consultarEstadoTorneo(torneo.getId()));
	}

	private void consultarHistorial() {
		System.out.println("--- Consultar historial de un torneo ---");
		Torneo torneo = pedirTorneo();
		if (torneo == null) {
			System.out.println(MENSAJE_NO_EXISTE);
			return;
		}
		System.out.println(administrador.consultarHistorialTorneo(torneo.getId()));
	}

	private void consultarCuposPreferenciales() {
		System.out.println("--- Cupos preferenciales de un torneo ---");
		Torneo torneo = pedirTorneo();
		if (torneo == null) {
			System.out.println(MENSAJE_NO_EXISTE);
			return;
		}
		int regularesTotales = torneo.getCuposTotales() - torneo.getCuposPreferenciales();
		System.out.println("Torneo: " + torneo.getNombre() + " (" + torneo.getCategoria() + ")");
		System.out.println("Cupos totales: " + torneo.getCuposTotales());
		System.out.println("Cupos preferenciales (20%): " + torneo.getCuposPreferenciales()
				+ " | disponibles: " + torneo.cuposPreferencialesDisponibles());
		System.out.println("Cupos regulares: " + regularesTotales
				+ " | disponibles: " + torneo.cuposRegularesDisponibles());
		System.out.println("Inscritos: " + torneo.getNumeroInscritos()
				+ " | cupos libres: " + torneo.cuposDisponibles());
		if (torneo.cuposRegularesDisponibles() == 0 && torneo.cuposPreferencialesDisponibles() > 0) {
			System.out.println("Solo quedan cupos preferenciales");
		}
	}

	private void listarTorneos() {
		System.out.println("--- Torneos registrados ---");
		List<Torneo> torneos = club.getTorneos();
		if (torneos.isEmpty()) {
			System.out.println("No hay torneos registrados.");
			return;
		}
		for (int i = 0; i < torneos.size(); i++) {
			Torneo torneo = torneos.get(i);
			String modalidad = "CONJUNTO";
			if (torneo instanceof TorneoIndividual) {
				modalidad = ((TorneoIndividual) torneo).getModalidad().toString();
			}
			System.out.println(torneo.getId() + " | " + torneo.getNombre() + " | " + torneo.getDeporte()
					+ " | " + modalidad + " | " + torneo.getTipo() + " | " + torneo.getEstado()
					+ " | inicio " + torneo.getFechaInicio());
		}
	}

	private void guardarYSalir() {
		try {
			club.guardarDatos(RUTA_DATOS);
			System.out.println("Datos guardados en " + RUTA_DATOS + ". Hasta luego.");
		} catch (Exception e) {
			System.out.println("No se pudieron guardar los datos: " + e.getMessage());
		}
	}

	private Torneo pedirTorneo() {
		System.out.print("Id del torneo: ");
		String id = scanner.nextLine().trim();
		if (id.isEmpty()) {
			return null;
		}
		return club.buscarTorneo(id);
	}

	private Socio pedirSocio() {
		return buscarSocio(leerTexto("Login del socio: "));
	}

	private Socio buscarSocio(String login) {
		Socio socio = club.buscarSocio(login);
		if (socio == null) {
			throw new IllegalArgumentException("No existe un socio con el login " + login + ".");
		}
		return socio;
	}

	private Equipo pedirEquipo() {
		String nombre = leerTexto("Nombre del equipo: ");
		Equipo inactivo = null;
		for (int i = 0; i < club.getEquipos().size(); i++) {
			Equipo equipo = club.getEquipos().get(i);
			if (equipo.getNombre().equalsIgnoreCase(nombre)) {
				if (equipo.isActivo()) {
					return equipo;
				}
				inactivo = equipo;
			}
		}
		if (inactivo == null) {
			throw new IllegalArgumentException("No existe el equipo " + nombre + ".");
		}
		return inactivo;
	}

	private Competencia pedirCompetencia() {
		String nombre = leerTexto("Nombre de la competencia: ");
		Competencia competencia = club.buscarCompetencia(nombre);
		if (competencia == null) {
			throw new IllegalArgumentException("No existe la competencia " + nombre + ".");
		}
		return competencia;
	}

	private EmpleadoDelClub pedirEmpleado(String mensaje) {
		return buscarEmpleado(leerTexto(mensaje));
	}

	private EmpleadoDelClub buscarEmpleado(String login) {
		for (int i = 0; i < club.getEmpleados().size(); i++) {
			if (club.getEmpleados().get(i).getLogin().equalsIgnoreCase(login)) {
				return club.getEmpleados().get(i);
			}
		}
		throw new IllegalArgumentException("No existe un empleado con el login " + login + ".");
	}

	private Articulo pedirProducto() {
		String nombre = leerTexto("Nombre del producto: ");
		List<Articulo> productos = new ArrayList<Articulo>(club.getTienda().getCatalogo());
		productos.addAll(club.getCafeteria().getBebidas());
		productos.addAll(club.getCafeteria().getSnacks());
		for (int i = 0; i < productos.size(); i++) {
			if (productos.get(i).getNombre().equalsIgnoreCase(nombre)) {
				return productos.get(i);
			}
		}
		throw new IllegalArgumentException("El producto no existe.");
	}

	private UbicacionInventario pedirUbicacion(String mensaje) {
		String nombre = leerTexto(mensaje + "(" + nombresUbicaciones() + "): ");
		for (int i = 0; i < club.getUbicaciones().size(); i++) {
			if (club.getUbicaciones().get(i).getNombre().equalsIgnoreCase(nombre)) {
				return club.getUbicaciones().get(i);
			}
		}
		throw new IllegalArgumentException("La ubicacion no existe.");
	}

	private String nombresUbicaciones() {
		StringBuilder texto = new StringBuilder();
		for (int i = 0; i < club.getUbicaciones().size(); i++) {
			texto.append(i == 0 ? "" : ", ").append(club.getUbicaciones().get(i).getNombre());
		}
		return texto.toString();
	}

	private int pedirIndice(int cantidad) {
		Integer numero = leerEntero("Numero: ");
		if (numero == null || numero < 1 || numero > cantidad) {
			throw new IllegalArgumentException("Numero fuera de la lista.");
		}
		return numero - 1;
	}

	private String leerTexto(String mensaje) {
		System.out.print(mensaje);
		return scanner.nextLine().trim();
	}

	private String leerTextoObligatorio(String campo, String mensaje) {
		System.out.print(mensaje);
		String texto = scanner.nextLine().trim();
		if (texto.isEmpty()) {
			System.out.println("Dato invalido: " + campo + ". El campo es obligatorio.");
			return null;
		}
		return texto;
	}

	private Integer leerEntero(String mensaje) {
		System.out.print(mensaje);
		try {
			return Integer.parseInt(scanner.nextLine().trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private Double leerDouble(String mensaje) {
		System.out.print(mensaje);
		try {
			return Double.parseDouble(scanner.nextLine().trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private LocalDate leerFechaOpcional(String mensaje) {
		System.out.print(mensaje);
		try {
			return LocalDate.parse(scanner.nextLine().trim());
		} catch (DateTimeParseException e) {
			return null;
		}
	}

	private LocalDate leerFecha(String mensaje) {
		LocalDate fecha = leerFechaOpcional(mensaje);
		if (fecha == null) {
			throw new IllegalArgumentException("Fecha invalida. Use el formato AAAA-MM-DD.");
		}
		return fecha;
	}

	private LocalTime leerHora(String mensaje) {
		System.out.print(mensaje);
		try {
			return LocalTime.parse(scanner.nextLine().trim());
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("Hora invalida. Use el formato HH:MM.");
		}
	}

	private YearMonth leerMes(String mensaje) {
		System.out.print(mensaje);
		try {
			return YearMonth.parse(scanner.nextLine().trim());
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("Mes invalido. Use el formato AAAA-MM.");
		}
	}

	private boolean esDeporteIndividual(String deporte) {
		return deporte.equals("TENIS") || deporte.equals("PADEL") || deporte.equals("TENIS_DE_MESA");
	}

	private boolean esDeporteConjunto(String deporte) {
		return deporte.equals("FUTBOL") || deporte.equals("BALONCESTO") || deporte.equals("VOLEIBOL");
	}
}
