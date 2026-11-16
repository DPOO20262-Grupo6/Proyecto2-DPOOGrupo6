package usuarios;

import java.io.Serializable;

public abstract class Usuario implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String nombre;
	private String login;
	private String password;
	
	public Usuario(String nombre, String login, String password) {
		this.nombre = nombre;
		this.login = login;
		this.password = password;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public boolean verificarCredenciales(String login, String password) {
		return this.login.equals(login) && this.password.equals(password);
	}

	public void acumularPuntos(int cantidad) { }
	
	public void redimirPuntos(int cantidad) { }
	
	public int getPuntosDeFidelidad() { return 0; }
}

