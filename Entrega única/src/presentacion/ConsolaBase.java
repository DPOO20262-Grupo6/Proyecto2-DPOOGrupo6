package presentacion;

import autenticacion.Autenticador;
import club.ClubDeportivoSeneca;
import persistencia.PersistenciaClub;
import persistencia.PersistenciaException;
import usuarios.Usuario;

/**
 * Esqueleto comun de las consolas del club. Define el orden de toda ejecucion:
 * cargar los datos, pedir el inicio de sesion, mostrar el menu y guardar al terminar.
 * Cada consola concreta solo declara el tipo de usuario que atiende y arma su menu.
 *
 * @param <T> tipo de usuario que puede usar la consola (Socio, EmpleadoDelClub o Administrador)
 */
public abstract class ConsolaBase<T extends Usuario> {

	protected ClubDeportivoSeneca club;
	protected LectorEntradas lector;
	protected T usuario;

	private PersistenciaClub persistencia;

	protected ConsolaBase() {
		this(new PersistenciaClub(), new LectorEntradas());
	}

	protected ConsolaBase(PersistenciaClub persistencia, LectorEntradas lector) {
		this.persistencia = persistencia;
		this.lector = lector;
	}

	/** Tipo de usuario que esta consola deja entrar. */
	protected abstract Class<T> getTipoUsuario();

	/** Nombre que se muestra en el encabezado, por ejemplo "Empleado". */
	protected abstract String getNombreConsola();

	/** Menu principal. Se arma despues del inicio de sesion, cuando ya se conoce al usuario. */
	protected abstract Menu construirMenuPrincipal();

	public final void ejecutar() {
		if (!cargarDatos()) {
			return;
		}
		if (!iniciarSesion()) {
			lector.mostrar("No se inicio sesion. La consola se cierra sin cambios.");
			return;
		}
		try {
			construirMenuPrincipal().ejecutar(lector);
		} catch (EntradaCanceladaException e) {
			lector.mostrar("");
			lector.mostrar("Se cerro la entrada: la consola termina.");
		} finally {
			guardarDatos();
		}
	}

	private boolean cargarDatos() {
		try {
			boolean hayDatos = persistencia.existenDatos();
			club = persistencia.cargar();
			if (hayDatos) {
				lector.mostrar("Datos cargados desde " + persistencia.getArchivoDatos().getPath());
			} else {
				lector.mostrar("No hay datos guardados en " + persistencia.getCarpeta().getPath() + ": se inicia un club nuevo.");
			}
			return true;
		} catch (PersistenciaException e) {
			lector.mostrar(e.getMessage());
			lector.mostrar("La consola se cierra sin modificar el archivo de datos.");
			return false;
		}
	}

	private boolean iniciarSesion() {
		Autenticador autenticador = new Autenticador(club);
		lector.mostrar("");
		lector.mostrar("===== Inicio de sesion - " + getNombreConsola() + " =====");
		try {
			while (!autenticador.estaBloqueado()) {
				String login = lector.leerTexto("Usuario: ");
				String password = lector.leerTexto("Contrasena: ");
				try {
					usuario = autenticador.iniciarSesion(login, password, getTipoUsuario());
					lector.mostrar("Bienvenido, " + usuario.getNombre() + ".");
					lector.mostrar("");
					return true;
				} catch (IllegalArgumentException e) {
					lector.mostrar(e.getMessage() + " Intentos restantes: " + autenticador.getIntentosRestantes() + ".");
				}
			}
			lector.mostrar("Se supero el maximo de " + Autenticador.MAXIMO_INTENTOS + " intentos.");
		} catch (EntradaCanceladaException e) {
			lector.mostrar("");
		}
		return false;
	}

	private void guardarDatos() {
		try {
			persistencia.guardar(club);
			lector.mostrar("Datos guardados en " + persistencia.getCarpeta().getPath() + ". Hasta luego.");
		} catch (PersistenciaException e) {
			lector.mostrar(e.getMessage());
		}
	}
}
