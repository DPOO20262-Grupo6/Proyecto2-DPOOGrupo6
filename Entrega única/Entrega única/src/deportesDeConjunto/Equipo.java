package deportesDeConjunto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import inscripciones.InscripcionEquipo;
import usuarios.Socio;

public abstract class Equipo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String nombre;
	private String categoria;
	private int cupoMaximoJugadores;
	private List<InscripcionEquipo> inscripciones;

	public Equipo(String nombre, String categoria, int cupoMaximoJugadores) {
		this.nombre = nombre;
		this.categoria = categoria;
		this.cupoMaximoJugadores = cupoMaximoJugadores;
		this.inscripciones = new ArrayList<InscripcionEquipo>();
	}

	public void agregarJugador(InscripcionEquipo inscripcion) {
		if (inscripciones.size() >= cupoMaximoJugadores) {
			throw new IllegalStateException("El equipo ya tiene el cupo maximo de jugadores.");
		}
		if (!validarCategoria(inscripcion.getSocio())) {
			throw new IllegalArgumentException("La edad del socio no corresponde a la categoria " + categoria);
		}
		if (!numeroCamisetaDisponible(inscripcion.getNumeroCamiseta())) {
			throw new IllegalArgumentException("El numero de camiseta ya esta ocupado en el equipo.");
		}
		inscripciones.add(inscripcion);
	}

	public void retirarJugador(InscripcionEquipo inscripcion) {
		inscripciones.remove(inscripcion);
		inscripcion.setActiva(false);
	}

	public boolean validarCategoria(Socio socio) {
		int edad = socio.calcularEdad(LocalDate.now());
		if (categoria.equalsIgnoreCase("SUB-13")) {
			return edad <= 13;
		} else if (categoria.equalsIgnoreCase("SUB-15")) {
			return edad > 13 && edad <= 15;
		} else if (categoria.equalsIgnoreCase("SUB-17")) {
			return edad > 15 && edad <= 17;
		} else if (categoria.equalsIgnoreCase("MAYORES")) {
			return edad >= 17;
		}
		return false;
	}

	public boolean numeroCamisetaDisponible(int numero) {
		for (int i = 0; i < inscripciones.size(); i++) {
			if (inscripciones.get(i).getNumeroCamiseta() == numero) {
				return false;
			}
		}
		return true;
	}

	public int contarJugadoresHabilitados() {
		int contador = 0;
		for (int i = 0; i < inscripciones.size(); i++) {
			if (inscripciones.get(i).estaHabilitado()) {
				contador = contador + 1;
			}
		}
		return contador;
	}

	public boolean cumpleMinimoJugadores() {
		return contarJugadoresHabilitados() >= getMinimoJugadoresHabilitados();
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	public int getCupoMaximoJugadores() {
		return cupoMaximoJugadores;
	}

	public void setCupoMaximoJugadores(int cupoMaximoJugadores) {
		this.cupoMaximoJugadores = cupoMaximoJugadores;
	}

	public List<InscripcionEquipo> getInscripciones() {
		return inscripciones;
	}

	public abstract int getMinimoJugadoresHabilitados();

	public abstract String getDeporte();
}