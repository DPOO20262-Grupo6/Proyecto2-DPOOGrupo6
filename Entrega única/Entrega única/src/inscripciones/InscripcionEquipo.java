package inscripciones;

import java.io.Serializable;
import deportesDeConjunto.Equipo;
import usuarios.Socio;

public class InscripcionEquipo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String posicion;
	private int numeroCamiseta;
	private Socio socio;
	private Equipo equipo;
	private boolean activa;

	public InscripcionEquipo(Socio socio, Equipo equipo, String posicion, int numeroCamiseta) {
		this.socio = socio;
		this.equipo = equipo;
		this.posicion = posicion;
		this.numeroCamiseta = numeroCamiseta;
		this.activa = true;
	}

	public boolean estaHabilitado() {
		return activa;
	}

	public String getPosicion() {
		return posicion;
	}

	public void setPosicion(String posicion) {
		this.posicion = posicion;
	}

	public int getNumeroCamiseta() {
		return numeroCamiseta;
	}

	public void setNumeroCamiseta(int numeroCamiseta) {
		this.numeroCamiseta = numeroCamiseta;
	}

	public Socio getSocio() {
		return socio;
	}

	public void setSocio(Socio socio) {
		this.socio = socio;
	}

	public Equipo getEquipo() {
		return equipo;
	}

	public void setEquipo(Equipo equipo) {
		this.equipo = equipo;
	}

	public boolean isActiva() {
		return activa;
	}

	public void setActiva(boolean activa) {
		this.activa = activa;
	}
}