package usuarios;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import articulosYVentas.Bono;
import enumeraciones.NivelHabilidad;
import inscripciones.InscripcionDisciplinaIndividual;

public class Socio extends Usuario {
	
	private static final long serialVersionUID = 1L;

	private LocalDate fechaNacimiento;
	private int puntosDeFidelidad;
	private List<Bono> bonos;
	
	public Socio(String nombre, String login, String password, LocalDate fechaNacimiento) {
		super(nombre, login, password);
		this.fechaNacimiento = fechaNacimiento;
		this.puntosDeFidelidad = 0;
		this.bonos = new ArrayList<>();
	}
	
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		if (this.bonos == null) {
			this.bonos = new ArrayList<>();
		}
	}
	
	public void agregarBono(Bono bono) {
		if (bono != null)
			bonos = new ArrayList<>();{
			this.bonos.add(bono);
		}
	}
	
	public Bono obtenerBonoDisponible() {
		if (bonos == null ) return null;
		for (Bono bono : bonos) {
			if (!bono.isUsado()) {
				return bono;
			}
		}
		return null;
	}
	
	public List<Bono> getBonos() {
		return bonos;
	}

	public InscripcionDisciplinaIndividual inscribirseDisciplina(String disciplina, NivelHabilidad nivel) {
		return new InscripcionDisciplinaIndividual(this, disciplina, nivel);
	}
	
	public int calcularEdad(LocalDate fechaActual) {
		return Period.between(this.fechaNacimiento, fechaActual).getYears();
	}

	@Override
	public void acumularPuntos(int cantidad) {
		if (cantidad > 0) {
			this.puntosDeFidelidad = this.puntosDeFidelidad + cantidad;
		}
	}

	@Override
	public void redimirPuntos(int cantidad) {
		if (cantidad <= 0) {
			throw new IllegalArgumentException("La cantidad a redimir debe ser mayor a cero.");
		}
		if (cantidad > this.puntosDeFidelidad) {
			throw new IllegalArgumentException("No tiene suficientes puntos. Saldo actual: " + this.puntosDeFidelidad);
		}
		this.puntosDeFidelidad = this.puntosDeFidelidad - cantidad;
	}

	public LocalDate getFechaNacimiento() {
		return fechaNacimiento;
	}

	public void setFechaNacimiento(LocalDate fechaNacimiento) {
		this.fechaNacimiento = fechaNacimiento;
	}

	@Override
	public int getPuntosDeFidelidad() {
		return puntosDeFidelidad;
	}

	public void setPuntosDeFidelidad(int puntosDeFidelidad) {
		this.puntosDeFidelidad = puntosDeFidelidad;
	}
}
