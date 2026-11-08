package club;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import administracion.TurnoEmpleado;
import articulosYVentas.Cafeteria;
import articulosYVentas.TiendaDeportiva;
import articulosYVentas.VentaCafeteria;
import articulosYVentas.VentaTienda;
import deportesDeConjunto.Competencia;
import deportesDeConjunto.Equipo;
import deportesDeConjunto.PartidoDeCompetencia;
import deportesDeConjunto.PartidoInterno;
import deportesIndividuales.ReservaInstalacionDeporteIndividual;
import entrenamiento.RegistroAsistencia;
import entrenamiento.SesionDeEntrenamiento;
import enumeraciones.Modalidad;
import estadisticas.EstadisticasPartidoDeEquipo;
import estadisticas.EstadisticasPartidoFutbol;
import inscripciones.InscripcionDisciplinaIndividual;
import instalaciones.CanchaDeporteIndividual;
import instalaciones.InstalacionDeportiva;
import inventario.UbicacionInventario;
import usuarios.Administrador;
import usuarios.EmpleadoDelClub;
import usuarios.Entrenador;
import usuarios.Fisioterapeuta;
import usuarios.Socio;
import usuarios.Usuario;

public class ClubDeportivoSeneca implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Map<String, Integer> maximoInstalacionesPorDisciplina;
	private Administrador administrador;
	private List<Socio> socios;
	private List<EmpleadoDelClub> empleados;
	private List<Equipo> equipos;
	private List<InstalacionDeportiva> instalaciones;
	private List<ReservaInstalacionDeporteIndividual> reservas;
	private List<SesionDeEntrenamiento> sesiones;
	private List<PartidoInterno> partidosInternos;
	private List<TurnoEmpleado> turnos;
	private List<InscripcionDisciplinaIndividual> inscripcionesIndividuales;
	
	private TiendaDeportiva tienda;
	private Cafeteria cafeteria;
	private UbicacionInventario bodegaCentral;

	public ClubDeportivoSeneca() {
		this.maximoInstalacionesPorDisciplina = new HashMap<String, Integer>();
		this.maximoInstalacionesPorDisciplina.put("TENIS", 2);
		this.maximoInstalacionesPorDisciplina.put("PADEL", 2);
		this.maximoInstalacionesPorDisciplina.put("TENIS_DE_MESA", 2);
		
		this.socios = new ArrayList<Socio>();
		this.empleados = new ArrayList<EmpleadoDelClub>();
		this.equipos = new ArrayList<Equipo>();
		this.instalaciones = new ArrayList<InstalacionDeportiva>();
		this.reservas = new ArrayList<ReservaInstalacionDeporteIndividual>();
		this.sesiones = new ArrayList<SesionDeEntrenamiento>();
		this.partidosInternos = new ArrayList<PartidoInterno>();
		this.turnos = new ArrayList<TurnoEmpleado>();
		this.inscripcionesIndividuales = new ArrayList<InscripcionDisciplinaIndividual>();

		this.administrador = new Administrador("Administrador", "admin", "admin123", this);
		UbicacionInventario ubicacionTienda = new UbicacionInventario("Tienda Principal", "TIENDA");
		this.bodegaCentral = new UbicacionInventario("Bodega", "BODEGA");
		this.tienda = new TiendaDeportiva(ubicacionTienda);
		this.cafeteria = new Cafeteria();
	}

	public Usuario autenticarUsuario(String login, String password) {
		if (administrador != null && administrador.verificarCredenciales(login, password)) {
			return administrador;
		}
		for (int i = 0; i < socios.size(); i++) {
			Socio socio = socios.get(i);
			if (socio.verificarCredenciales(login, password)) {
				return socio;
			}
		}
		for (int i = 0; i < empleados.size(); i++) {
			EmpleadoDelClub empleado = empleados.get(i);
			if (empleado.verificarCredenciales(login, password)) {
				return empleado;
			}
		}
		throw new IllegalArgumentException("Login o password incorrectos.");
	}

	public void registrarSocio(Socio socio) {
		socios.add(socio);
	}

	public void registrarEmpleado(EmpleadoDelClub empleado) {
		empleados.add(empleado);
		tienda.registrarCodigoEmpleado(empleado.getCodigoDescuento());
	}

	public void registrarInscripcionIndividual(InscripcionDisciplinaIndividual inscripcion) {
		inscripcionesIndividuales.add(inscripcion);
	}

	public Socio buscarSocio(String login) {
		for (int i = 0; i < socios.size(); i++) {
			if (socios.get(i).getLogin().equalsIgnoreCase(login)) {
				return socios.get(i);
			}
		}
		return null;
	}

	public Equipo buscarEquipo(String nombre) {
		for (int i = 0; i < equipos.size(); i++) {
			if (equipos.get(i).getNombre().equalsIgnoreCase(nombre)) {
				return equipos.get(i);
			}
		}
		return null;
	}

	public ReservaInstalacionDeporteIndividual reservarInstalacion(List<Socio> sociosReserva, CanchaDeporteIndividual cancha, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, Modalidad modalidad) {
		int cantidad = sociosReserva.size();
		ReservaInstalacionDeporteIndividual reserva = new ReservaInstalacionDeporteIndividual(sociosReserva, cancha, fecha, horaInicio, horaFin, cantidad, modalidad);
		reservas.add(reserva);

		if (!cancha.admiteCantidadJugadores(cantidad, modalidad)) {
			reserva.rechazar("Cantidad de jugadores o modalidad no valida.");
			return reserva;
		}
		if (!cancha.estaDisponible(fecha, horaInicio, horaFin)) {
			reserva.rechazar("La cancha no esta disponible en ese horario.");
			return reserva;
		}
		
		int ocupadas = contarInstalacionesOcupadas(cancha.getDisciplina(), fecha, horaInicio, horaFin);
		
		Integer maximoObjeto = maximoInstalacionesPorDisciplina.get(cancha.getDisciplina());
		int maximo = 1;
		if (maximoObjeto != null) {
			maximo = maximoObjeto.intValue();
		}
		
		if (ocupadas >= maximo) {
			reserva.rechazar("Cupo maximo de canchas simultaneas alcanzado.");
			return reserva;
		}
		reserva.confirmar();
		return reserva;
	}

	public int contarInstalacionesOcupadas(String disciplina, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
		int contador = 0;
		for (int i = 0; i < reservas.size(); i++) {
			ReservaInstalacionDeporteIndividual reserva = reservas.get(i);
			if ("CONFIRMADA".equalsIgnoreCase(reserva.getEstado()) && reserva.getCancha().getDisciplina().equalsIgnoreCase(disciplina) && reserva.seCruzaCon(fecha, horaInicio, horaFin)) {
				contador = contador + 1;
			}
		}
		return contador;
	}

	public boolean verificarCoberturaMinima(LocalDate fecha, LocalTime hora) {
		int entrenadores = 0;
		int fisioterapeutas = 0;
		for (int i = 0; i < turnos.size(); i++) {
			TurnoEmpleado turno = turnos.get(i);
			if (turno.seCruzaCon(fecha, hora)) {
				if (turno.getEmpleado() instanceof Entrenador) {
					entrenadores = entrenadores + 1;
				} else if (turno.getEmpleado() instanceof Fisioterapeuta) {
					fisioterapeutas = fisioterapeutas + 1;
				}
			}
		}
		return entrenadores >= 2 && fisioterapeutas >= 1;
	}

	public String generarReporteVentas(String rubro, LocalDate fechaInicio, LocalDate fechaFin) {
		double totalTienda = 0;
		double totalCafeteria = 0;
		int cantidad = 0;

		if (rubro.equalsIgnoreCase("TIENDA") || rubro.equalsIgnoreCase("TODOS")) {
			List<VentaTienda> ventasTienda = tienda.getVentas();
			for (int i = 0; i < ventasTienda.size(); i++) {
				VentaTienda ventaTienda = ventasTienda.get(i);
				if (!ventaTienda.getFecha().isBefore(fechaInicio) && !ventaTienda.getFecha().isAfter(fechaFin)) {
					totalTienda = totalTienda + ventaTienda.calcularTotal();
					cantidad = cantidad + 1;
				}
			}
		}
		if (rubro.equalsIgnoreCase("CAFETERIA") || rubro.equalsIgnoreCase("TODOS")) {
			List<VentaCafeteria> ventasCafeteria = cafeteria.getVentas();
			for (int i = 0; i < ventasCafeteria.size(); i++) {
				VentaCafeteria ventaCafeteria = ventasCafeteria.get(i);
				if (!ventaCafeteria.getFecha().isBefore(fechaInicio) && !ventaCafeteria.getFecha().isAfter(fechaFin)) {
					totalCafeteria = totalCafeteria + ventaCafeteria.calcularTotal();
					cantidad = cantidad + 1;
				}
			}
		}
		if (cantidad == 0) {
			return "No hay ventas registradas en ese periodo.";
		}
		return "Ventas Tienda: $" + totalTienda + " | Ventas Cafeteria: $" + totalCafeteria + " | Total: $" + (totalTienda + totalCafeteria);
	}

	public String generarReporteGoleadores(Competencia competencia) {
		String resultado = "Goleadores " + competencia.getNombre() + ": ";
		List<PartidoDeCompetencia> partidos = competencia.getPartidos();
		
		for (int i = 0; i < partidos.size(); i++) {
			PartidoDeCompetencia partido = partidos.get(i);
			List<EstadisticasPartidoDeEquipo> estadisticasPartido = partido.getEstadisticas();
			
			for (int j = 0; j < estadisticasPartido.size(); j++) {
				EstadisticasPartidoDeEquipo estadistica = estadisticasPartido.get(j);
				if (estadistica instanceof EstadisticasPartidoFutbol) {
					EstadisticasPartidoFutbol estadisticaFutbol = (EstadisticasPartidoFutbol) estadistica;
					if (estadisticaFutbol.getGoles() > 0) {
						resultado = resultado + partido.getEquipoClub().getNombre() + " (" + estadisticaFutbol.getGoles() + " goles) ";
					}
				}
			}
		}
		return resultado;
	}

	public String generarReporteAsistencia(LocalDate fechaInicio, LocalDate fechaFin) {
		String texto = "Asistencias: ";
		for (int i = 0; i < sesiones.size(); i++) {
			SesionDeEntrenamiento sesion = sesiones.get(i);
			if (!sesion.getFecha().isBefore(fechaInicio) && !sesion.getFecha().isAfter(fechaFin)) {
				List<RegistroAsistencia> asistencias = sesion.getAsistencias();
				for (int j = 0; j < asistencias.size(); j++) {
					texto = texto + asistencias.get(j).toString() + " | ";
				}
			}
		}
		return texto;
	}

	public String generarReporteEvolucionElo(String disciplina) {
		String texto = "Ranking ELO " + disciplina + ": ";
		for (int i = 0; i < inscripcionesIndividuales.size(); i++) {
			InscripcionDisciplinaIndividual inscripcion = inscripcionesIndividuales.get(i);
			if (inscripcion.getDisciplina().equalsIgnoreCase(disciplina)) {
				texto = texto + inscripcion.getSocio().getNombre() + " " + inscripcion.getHistorialElo().toString() + " ";
			}
		}
		return texto;
	}

	public void guardarDatos(String carpeta) {
		try {
			File directorio = new File(carpeta);
			if (!directorio.exists()) {
				directorio.mkdirs();
			}
			
			ObjectOutputStream salidaObjetos = new ObjectOutputStream(new FileOutputStream(new File(directorio, "club_seneca.dat")));
			salidaObjetos.writeObject(this);
			salidaObjetos.close();

			PrintWriter escritor = new PrintWriter(new FileWriter(new File(directorio, "resumen_usuarios.csv")));
			escritor.println("TIPO,LOGIN,NOMBRE,PUNTOS");
			for (int i = 0; i < socios.size(); i++) {
				Socio socio = socios.get(i);
				escritor.println("SOCIO," + socio.getLogin() + "," + socio.getNombre() + "," + socio.getPuntosDeFidelidad());
			}
			for (int i = 0; i < empleados.size(); i++) {
				EmpleadoDelClub empleado = empleados.get(i);
				escritor.println("EMPLEADO," + empleado.getLogin() + "," + empleado.getNombre() + "," + empleado.getPuntosDeFidelidad());
			}
			escritor.close();
		} catch (IOException e) {
			throw new RuntimeException("Error al guardar los datos: " + e.getMessage());
		}
	}

	public void cargarDatos(String carpeta) {
		File archivo = new File(carpeta, "club_seneca.dat");
		if (!archivo.exists()) {
			throw new IllegalStateException("No existe el archivo de datos.");
		}
		try {
			ObjectInputStream entradaObjetos = new ObjectInputStream(new FileInputStream(archivo));
			ClubDeportivoSeneca clubCargado = (ClubDeportivoSeneca) entradaObjetos.readObject();
			entradaObjetos.close();

			this.administrador = clubCargado.administrador;
			this.socios = clubCargado.socios;
			this.empleados = clubCargado.empleados;
			this.equipos = clubCargado.equipos;
			this.instalaciones = clubCargado.instalaciones;
			this.reservas = clubCargado.reservas;
			this.sesiones = clubCargado.sesiones;
			this.partidosInternos = clubCargado.partidosInternos;
			this.turnos = clubCargado.turnos;
			this.inscripcionesIndividuales = clubCargado.inscripcionesIndividuales;
			this.tienda = clubCargado.tienda;
			this.cafeteria = clubCargado.cafeteria;
			this.bodegaCentral = clubCargado.bodegaCentral;
			this.maximoInstalacionesPorDisciplina = clubCargado.maximoInstalacionesPorDisciplina;
		} catch (Exception e) {
			throw new RuntimeException("Error al cargar los datos: " + e.getMessage());
		}
	}

	public void agregarEquipo(Equipo equipo) {
		equipos.add(equipo);
	}

	public void setMaximoInstalaciones(String disciplina, int maximo) {
		maximoInstalacionesPorDisciplina.put(disciplina.toUpperCase(), maximo);
	}

	public Administrador getAdministrador() { return administrador; }
	public List<Socio> getSocios() { return socios; }
	public List<EmpleadoDelClub> getEmpleados() { return empleados; }
	public List<Equipo> getEquipos() { return equipos; }
	public List<PartidoInterno> getPartidosInternos() { return partidosInternos; }
	public List<TurnoEmpleado> getTurnos() { return turnos; }
	public List<SesionDeEntrenamiento> getSesiones() { return sesiones; }
	public List<InscripcionDisciplinaIndividual> getInscripcionesIndividuales() { return inscripcionesIndividuales; }
	public TiendaDeportiva getTienda() { return tienda; }
	public Cafeteria getCafeteria() { return cafeteria; }
	public UbicacionInventario getBodegaCentral() { return bodegaCentral; }
}