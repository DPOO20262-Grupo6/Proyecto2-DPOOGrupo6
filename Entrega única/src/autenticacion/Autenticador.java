package autenticacion;

import club.ClubDeportivoSeneca;
import usuarios.Administrador;
import usuarios.EmpleadoDelClub;
import usuarios.Socio;
import usuarios.Usuario;

/**
 * Inicio de sesion comun a las tres consolas (socio, empleado y administrador).
 * Cada consola pide el rol que le corresponde: un usuario valido de otro rol se
 * rechaza con el mismo mensaje que una credencial errada, para no revelar que dato fallo.
 */
public class Autenticador {

	public static final String MENSAJE_RECHAZO = "Login o password incorrectos.";
	public static final int MAXIMO_INTENTOS = 3;

	private ClubDeportivoSeneca club;
	private int intentosFallidos;

	public Autenticador(ClubDeportivoSeneca club) {
		if (club == null) {
			throw new IllegalArgumentException("El autenticador necesita un club.");
		}
		this.club = club;
		this.intentosFallidos = 0;
	}

	/**
	 * Valida las credenciales y que el usuario sea del rol pedido.
	 * @return el usuario autenticado, ya convertido al tipo del rol
	 * @throws IllegalArgumentException si las credenciales no son validas para ese rol
	 * @throws IllegalStateException si ya se agotaron los intentos permitidos
	 */
	public <T extends Usuario> T iniciarSesion(String login, String password, Class<T> rol) {
		if (rol == null) {
			throw new IllegalArgumentException("Debe indicar el rol que inicia sesion.");
		}
		if (estaBloqueado()) {
			throw new IllegalStateException("Se supero el maximo de " + MAXIMO_INTENTOS + " intentos de inicio de sesion.");
		}
		if (estaVacio(login) || estaVacio(password)) {
			return rechazar();
		}

		Usuario usuario;
		try {
			usuario = club.autenticarUsuario(login.trim(), password);
		} catch (IllegalArgumentException e) {
			return rechazar();
		}

		if (!rol.isInstance(usuario)) {
			return rechazar();
		}
		if (usuario instanceof Socio && !((Socio) usuario).isActivo()) {
			return rechazar();
		}

		intentosFallidos = 0;
		return rol.cast(usuario);
	}

	public Socio iniciarSesionSocio(String login, String password) {
		return iniciarSesion(login, password, Socio.class);
	}

	public EmpleadoDelClub iniciarSesionEmpleado(String login, String password) {
		return iniciarSesion(login, password, EmpleadoDelClub.class);
	}

	public Administrador iniciarSesionAdministrador(String login, String password) {
		return iniciarSesion(login, password, Administrador.class);
	}

	private <T extends Usuario> T rechazar() {
		intentosFallidos = intentosFallidos + 1;
		throw new IllegalArgumentException(MENSAJE_RECHAZO);
	}

	private boolean estaVacio(String texto) {
		return texto == null || texto.trim().isEmpty();
	}

	public boolean estaBloqueado() {
		return intentosFallidos >= MAXIMO_INTENTOS;
	}

	public int getIntentosRestantes() {
		return Math.max(0, MAXIMO_INTENTOS - intentosFallidos);
	}

	public int getIntentosFallidos() {
		return intentosFallidos;
	}
}
