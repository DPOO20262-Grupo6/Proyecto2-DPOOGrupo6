package presentacion;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

import Torneos.Torneo;
import Torneos.TorneoIndividual;
import club.ClubDeportivoSeneca;
import enumeraciones.TipoTorneo;
import persistencia.PersistenciaClub;
import persistencia.PersistenciaException;
import usuarios.Administrador;
import usuarios.Usuario;


public class ConsolaAdministrador {

	private static final String MENSAJE_NO_EXISTE = "El torneo no existe";

	private ClubDeportivoSeneca club;
	private Scanner scanner;
	private Administrador administrador;
	/** Misma carga y guardado que las demas consolas: encuentra la carpeta de datos sin importar desde donde se ejecute. */
	private PersistenciaClub persistencia = new PersistenciaClub();

	public ConsolaAdministrador(ClubDeportivoSeneca club) {
		this.club = club;
		this.scanner = new Scanner(System.in);
	}

	public static void main(String[] args) {
		PersistenciaClub persistencia = new PersistenciaClub();
		ClubDeportivoSeneca club;
		try {
			boolean hayDatos = persistencia.existenDatos();
			club = persistencia.cargar();
			if (hayDatos) {
				System.out.println("Datos cargados desde " + persistencia.getArchivoDatos().getPath());
			} else {
				System.out.println("No hay datos guardados en " + persistencia.getCarpeta().getPath() + ": se inicia un club nuevo.");
			}
		} catch (PersistenciaException e) {
			// Un archivo danado no se reemplaza por un club vacio: se cierra para no perder los datos.
			System.out.println(e.getMessage());
			System.out.println("La consola se cierra sin modificar el archivo de datos.");
			return;
		}
		ConsolaAdministrador consola = new ConsolaAdministrador(club);
		consola.ejecutar();
	}

	public void ejecutar() {
		iniciarSesion();
		boolean continuar = true;
		while (continuar) {
			mostrarMenu();
			String opcion = scanner.nextLine().trim();
			System.out.println();
			try {
				if (opcion.equals("1")) {
					crearTorneo();
				} else if (opcion.equals("2")) {
					consultarEstado();
				} else if (opcion.equals("3")) {
					consultarHistorial();
				} else if (opcion.equals("4")) {
					consultarCuposPreferenciales();
				} else if (opcion.equals("5")) {
					listarTorneos();
				} else if (opcion.equals("0")) {
					guardarYSalir();
					continuar = false;
				} else {
					System.out.println("Opcion no valida.");
				}
			} catch (Exception e) {
				System.out.println("Error: " + e.getMessage());
			}
			System.out.println();
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

	private void mostrarMenu() {
		System.out.println("===== Consola del Administrador - Torneos =====");
		System.out.println("1. Crear torneo");
		System.out.println("2. Consultar estado de un torneo");
		System.out.println("3. Consultar historial de un torneo");
		System.out.println("4. Consultar cupos preferenciales de un torneo");
		System.out.println("5. Listar torneos");
		System.out.println("0. Guardar y salir");
		System.out.print("Seleccione una opcion: ");
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

		LocalDate fechaInicio = leerFecha("Fecha de inicio (AAAA-MM-DD): ");
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
			persistencia.guardar(club);
			System.out.println("Datos guardados en " + persistencia.getCarpeta().getPath() + ". Hasta luego.");
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

	private LocalDate leerFecha(String mensaje) {
		System.out.print(mensaje);
		try {
			return LocalDate.parse(scanner.nextLine().trim());
		} catch (DateTimeParseException e) {
			return null;
		}
	}

	private boolean esDeporteIndividual(String deporte) {
		return deporte.equals("TENIS") || deporte.equals("PADEL") || deporte.equals("TENIS_DE_MESA");
	}

	private boolean esDeporteConjunto(String deporte) {
		return deporte.equals("FUTBOL") || deporte.equals("BALONCESTO") || deporte.equals("VOLEIBOL");
	}
}
