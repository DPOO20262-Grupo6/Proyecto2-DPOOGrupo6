package usuarios;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import entrenamiento.SesionDeEntrenamiento;
import enumeraciones.EstadoAsistencia;
import instalaciones.InstalacionDeportiva;

public class Entrenador extends EmpleadoDelClub {
	
	private static final long serialVersionUID = 1L;
	
	private List<SesionDeEntrenamiento> sesiones;

	public Entrenador(String nombre, String login, String password) {
		super(nombre, login, password);
		this.sesiones = new ArrayList<SesionDeEntrenamiento>();
	}

	public SesionDeEntrenamiento planearSesion(LocalDate fecha, LocalTime hora, int duracion, InstalacionDeportiva instalacion, List<Socio> convocados) {
		if (tieneConflictoHorario(fecha, hora, duracion)) {
			throw new IllegalStateException("El entrenador ya tiene una sesion programada en ese horario.");
		}
		
		SesionDeEntrenamiento sesion = new SesionDeEntrenamiento(fecha, hora, duracion, this, instalacion);
		
		if (convocados != null) {
			for (int i = 0; i < convocados.size(); i++) {
				sesion.convocar(convocados.get(i));
			}
		}
		
		sesiones.add(sesion);
		return sesion;
	}

	public boolean tieneConflictoHorario(LocalDate fecha, LocalTime hora, int duracion) {
		for (int i = 0; i < sesiones.size(); i++) {
			if (sesiones.get(i).seCruzaCon(fecha, hora, duracion)) {
				return true;
			}
		}
		return false;
	}

	public void registrarAsistencia(SesionDeEntrenamiento sesion, Socio socio, EstadoAsistencia estado, Integer nota, String comentario) {
		sesion.registrarAsistencia(socio, estado, nota, comentario);
	}

	public List<SesionDeEntrenamiento> getSesiones() {
		return sesiones;
	}
}