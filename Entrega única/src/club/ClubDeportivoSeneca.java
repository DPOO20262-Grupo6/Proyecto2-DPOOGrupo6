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
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import Torneos.Torneo;
import administracion.PagoMensualidad;
import administracion.SolicitudCambioTurnoEmpleado;
import administracion.Sugerencia;
import enumeraciones.EstadoSolicitud;
import articulosYVentas.Articulo;
import administracion.TurnoEmpleado;
import articulosYVentas.Cafeteria;
import articulosYVentas.TiendaDeportiva;
import articulosYVentas.VentaCafeteria;

import articulosYVentas.VentaTienda;
import deportesDeConjunto.Ascun;
import deportesDeConjunto.Competencia;
import deportesDeConjunto.JugadorEnNomina;
import deportesDeConjunto.NominaCompetencia;
import deportesDeConjunto.Equipo;
import deportesDeConjunto.PartidoDeCompetencia;
import deportesDeConjunto.PartidoDeEquipo;
import deportesDeConjunto.PartidoInterno;
import deportesDeConjunto.ResultadoPartidoEquipo;
import deportesDeConjunto.ResultadoPorPuntos;
import deportesDeConjunto.ResultadoVoleibol;
import deportesIndividuales.PartidoDeporteIndividual;
import deportesIndividuales.PartidoPadel;
import deportesIndividuales.PartidoTenis;
import deportesIndividuales.PartidoTenisDeMesa;
import deportesIndividuales.ReservaInstalacionDeporteIndividual;
import entrenamiento.RegistroAsistencia;
import entrenamiento.SesionDeEntrenamiento;
import enumeraciones.Modalidad;
import enumeraciones.NivelHabilidad;
import estadisticas.EstadisticasPartidoBaloncesto;
import estadisticas.EstadisticasPartidoDeEquipo;
import estadisticas.EstadisticasPartidoVoleibol;
import estadisticas.EstadisticasPartidoFutbol;
import estadisticas.EstadisticasPartidoIndividual;
import inscripciones.InscripcionDisciplinaIndividual;
import instalaciones.CanchaDeporteIndividual;
import inscripciones.InscripcionEquipo;
import instalaciones.InstalacionDeporteConjunto;
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

	public static final String NOMBRE_ADMIN = "Administrador";
	public static final String LOGIN_ADMIN = "admin";
	public static final String PASSWORD_ADMIN = "admin123";
	public static final LocalTime HORA_APERTURA = LocalTime.of(6, 0);
	public static final LocalTime HORA_CIERRE = LocalTime.of(22, 0);
	public static final int MINUTOS_BLOQUE = 15;
	
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
	private List<Torneo> torneos;
	private List<PagoMensualidad> pagosMensualidades;
	private List<PartidoDeporteIndividual> partidosPractica;
	private List<Competencia> competencias;
	private List<Sugerencia> sugerencias;
	private List<SolicitudCambioTurnoEmpleado> solicitudesCambio;

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
		this.torneos = new ArrayList<Torneo>();
		this.pagosMensualidades = new ArrayList<PagoMensualidad>();
		this.partidosPractica = new ArrayList<PartidoDeporteIndividual>();
		this.competencias = new ArrayList<Competencia>();

		this.administrador = new Administrador(NOMBRE_ADMIN, LOGIN_ADMIN, PASSWORD_ADMIN, this);
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

	public Socio buscarSocioPorDocumento(String documento) {
		for (int i = 0; i < socios.size(); i++) {
			if (documento != null && documento.equalsIgnoreCase(socios.get(i).getDocumento())) {
				return socios.get(i);
			}
		}
		return null;
	}

	public boolean existeLogin(String login) {
		if (administrador != null && administrador.getLogin().equalsIgnoreCase(login)) {
			return true;
		}
		if (buscarSocio(login) != null) {
			return true;
		}
		for (int i = 0; i < empleados.size(); i++) {
			if (empleados.get(i).getLogin().equalsIgnoreCase(login)) {
				return true;
			}
		}
		return false;
	}

	public void agregarPagoMensualidad(PagoMensualidad pago) {
		pagosMensualidades.add(pago);
	}

	public PagoMensualidad buscarPagoMensualidad(Socio socio, YearMonth mes) {
		for (int i = 0; i < pagosMensualidades.size(); i++) {
			PagoMensualidad pago = pagosMensualidades.get(i);
			if (pago.getSocio().equals(socio) && pago.getMes().equals(mes)) {
				return pago;
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

	public static boolean esDisciplinaIndividual(String disciplina) {
		return disciplina != null && (disciplina.equalsIgnoreCase("TENIS") || disciplina.equalsIgnoreCase("PADEL") || disciplina.equalsIgnoreCase("TENIS_DE_MESA"));
	}

	public InscripcionDisciplinaIndividual buscarInscripcion(Socio socio, String disciplina) {
		for (int i = 0; i < inscripcionesIndividuales.size(); i++) {
			InscripcionDisciplinaIndividual inscripcion = inscripcionesIndividuales.get(i);
			if (inscripcion.getSocio().equals(socio) && inscripcion.getDisciplina().equalsIgnoreCase(disciplina)) {
				return inscripcion;
			}
		}
		return null;
	}

	public InscripcionDisciplinaIndividual inscribirEnAcademia(Socio socio, String disciplina, NivelHabilidad nivel) {
		if (socio == null) {
			throw new IllegalArgumentException("Debe indicar el socio.");
		}
		if (!esDisciplinaIndividual(disciplina)) {
			throw new IllegalArgumentException("La disciplina " + disciplina + " no existe. Use TENIS, PADEL o TENIS_DE_MESA.");
		}
		if (nivel == null) {
			throw new IllegalArgumentException("El nivel debe ser PRINCIPIANTE, INTERMEDIO o AVANZADO.");
		}
		if (buscarInscripcion(socio, disciplina) != null) {
			throw new IllegalArgumentException("El socio ya esta inscrito en " + disciplina.toUpperCase() + " (inscripcion duplicada).");
		}
		InscripcionDisciplinaIndividual inscripcion = socio.inscribirseDisciplina(disciplina, nivel);
		inscripcionesIndividuales.add(inscripcion);
		return inscripcion;
	}

	public ReservaInstalacionDeporteIndividual reservarInstalacion(List<Socio> sociosReserva, CanchaDeporteIndividual cancha, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, Modalidad modalidad) {
		int cantidad = sociosReserva.size();
		ReservaInstalacionDeporteIndividual reserva = new ReservaInstalacionDeporteIndividual(sociosReserva, cancha, fecha, horaInicio, horaFin, cantidad, modalidad);

		if (cantidad == 0 || buscarInscripcion(sociosReserva.get(0), cancha.getDisciplina()) == null) {
			reserva.rechazar("El socio no esta inscrito en la disciplina " + cancha.getDisciplina() + ".");
			return reserva;
		}
		if (!cancha.admiteCantidadJugadores(cantidad, modalidad)) {
			reserva.rechazar("Cantidad de jugadores o modalidad no valida.");
			return reserva;
		}
		if (!cancha.estaDisponible(fecha, horaInicio, horaFin) || tieneSesionEnHorario(cancha, fecha, horaInicio, horaFin)) {
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
		reservas.add(reserva);
		return reserva;
	}

	private boolean tieneSesionEnHorario(CanchaDeporteIndividual cancha, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
		int duracion = (int) java.time.Duration.between(horaInicio, horaFin).toMinutes();
		for (int i = 0; i < sesiones.size(); i++) {
			SesionDeEntrenamiento sesion = sesiones.get(i);
			if (sesion.getInstalacion() == cancha && sesion.seCruzaCon(fecha, horaInicio, duracion)) {
				return true;
			}
		}
		return false;
	}

	public void registrarInstalacion(InstalacionDeportiva instalacion) {
		if (instalacion != null && !instalaciones.contains(instalacion)) {
			instalaciones.add(instalacion);
		}
	}

	public List<UbicacionInventario> getUbicaciones() {
		List<UbicacionInventario> ubicaciones = new ArrayList<UbicacionInventario>();
		ubicaciones.add(bodegaCentral);
		ubicaciones.add(tienda.getUbicacionTienda());
		ubicaciones.add(cafeteria.getInventario());
		return ubicaciones;
	}

	public boolean esUbicacionDelClub(UbicacionInventario ubicacion) {
		return ubicacion != null && getUbicaciones().contains(ubicacion);
	}

	public boolean existeProducto(Articulo producto) {
		return producto != null && (tienda.getCatalogo().contains(producto) || cafeteria.getBebidas().contains(producto) || cafeteria.getSnacks().contains(producto));
	}

	public String consultarInventario(String nombreUbicacion) {
		List<UbicacionInventario> ubicaciones = getUbicaciones();
		if (nombreUbicacion != null) {
			List<UbicacionInventario> filtradas = new ArrayList<UbicacionInventario>();
			for (int i = 0; i < ubicaciones.size(); i++) {
				if (ubicaciones.get(i).getNombre().equalsIgnoreCase(nombreUbicacion)) {
					filtradas.add(ubicaciones.get(i));
				}
			}
			if (filtradas.isEmpty()) {
				throw new IllegalArgumentException("No se encontro la ubicacion " + nombreUbicacion + ".");
			}
			ubicaciones = filtradas;
		}
		List<Articulo> productos = new ArrayList<Articulo>(tienda.getCatalogo());
		productos.addAll(cafeteria.getBebidas());
		productos.addAll(cafeteria.getSnacks());
		StringBuilder texto = new StringBuilder();
		for (int i = 0; i < productos.size(); i++) {
			Articulo producto = productos.get(i);
			int total = 0;
			texto.append(producto.getNombre()).append(":");
			for (int j = 0; j < ubicaciones.size(); j++) {
				int cantidad = ubicaciones.get(j).obtenerCantidad(producto);
				total = total + cantidad;
				texto.append(" ").append(ubicaciones.get(j).getNombre()).append(" ").append(cantidad).append(",");
			}
			texto.append(" Total ").append(total).append("\n");
		}
		for (int i = 0; i < instalaciones.size(); i++) {
			InstalacionDeportiva instalacion = instalaciones.get(i);
			texto.append(instalacion.getNombre()).append(" - ");
			if (instalacion instanceof CanchaDeporteIndividual) {
				CanchaDeporteIndividual cancha = (CanchaDeporteIndividual) instalacion;
				texto.append(cancha.getDisciplina()).append(", capacidad ").append(cancha.getCapacidad());
			} else if (instalacion instanceof InstalacionDeporteConjunto) {
				texto.append(((InstalacionDeporteConjunto) instalacion).getDeporte());
			}
			texto.append(", ").append(estaEnUso(instalacion) ? "En uso" : "Disponible").append("\n");
		}
		return texto.toString();
	}

	private boolean estaEnUso(InstalacionDeportiva instalacion) {
		LocalTime ahora = LocalTime.now();
		for (int i = 0; i < instalacion.getReservas().size(); i++) {
			ReservaInstalacionDeporteIndividual reserva = instalacion.getReservas().get(i);
			if ("CONFIRMADA".equalsIgnoreCase(reserva.getEstado()) && reserva.getFecha().equals(LocalDate.now())
					&& !ahora.isBefore(reserva.getHoraInicio()) && ahora.isBefore(reserva.getHoraFin())) {
				return true;
			}
		}
		return false;
	}

	public Sugerencia proponerSugerenciaArticulo(EmpleadoDelClub autor, String nombre, double precio, String categoria, String descripcion) {
		validarSugerencia(nombre, descripcion);
		return registrarSugerencia(autor.proponerSugerenciaArticulo(nombre, precio, categoria), autor, descripcion);
	}

	public Sugerencia proponerSugerenciaSnack(EmpleadoDelClub autor, String nombre, double precio, List<String> alergenos, String descripcion) {
		validarSugerencia(nombre, descripcion);
		return registrarSugerencia(autor.proponerSugerenciaSnack(nombre, precio, alergenos), autor, descripcion);
	}

	private void validarSugerencia(String nombre, String descripcion) {
		if (nombre == null || nombre.trim().isEmpty()) {
			throw new IllegalArgumentException("Falta el nombre de la sugerencia.");
		}
		if (descripcion == null || descripcion.trim().isEmpty()) {
			throw new IllegalArgumentException("Falta la descripcion de la sugerencia.");
		}
	}

	private Sugerencia registrarSugerencia(Sugerencia sugerencia, EmpleadoDelClub autor, String descripcion) {
		sugerencia.setAutor(autor.getNombre());
		sugerencia.setDescripcion(descripcion);
		getSugerencias().add(sugerencia);
		return sugerencia;
	}

	public List<Sugerencia> getSugerencias() {
		if (sugerencias == null) {
			sugerencias = new ArrayList<Sugerencia>();
		}
		return sugerencias;
	}

	public List<InstalacionDeportiva> getInstalaciones() { return instalaciones; }

	public void procesarVentaCafeteria(VentaCafeteria venta, String codigoDescuento) {
		String prohibicion = null;
		LocalDate hoy = LocalDate.now();
		LocalTime ahora = LocalTime.now();
		for (int i = 0; i < reservas.size() && prohibicion == null; i++) {
			ReservaInstalacionDeporteIndividual reserva = reservas.get(i);
			boolean activaAhora = "CONFIRMADA".equalsIgnoreCase(reserva.getEstado()) && reserva.getFecha().equals(hoy)
					&& !ahora.isBefore(reserva.getHoraInicio()) && ahora.isBefore(reserva.getHoraFin());
			if (activaAhora && reserva.getSocios().contains(venta.getComprador()) && !reserva.getCancha().permiteBebidaCaliente()) {
				prohibicion = "Tiene una reserva activa en " + reserva.getCancha().getNombre() + ": no se permiten bebidas calientes.";
			}
		}
		cafeteria.procesarVenta(venta, prohibicion, tienda.resolverDescuento(venta.getComprador(), codigoDescuento));
	}

	public List<ReservaInstalacionDeporteIndividual> getReservasDeSocio(Socio socio) {
		List<ReservaInstalacionDeporteIndividual> resultado = new ArrayList<ReservaInstalacionDeporteIndividual>();
		for (int i = 0; i < reservas.size(); i++) {
			if (reservas.get(i).getSocios().contains(socio)) {
				resultado.add(reservas.get(i));
			}
		}
		return resultado;
	}

	public PartidoDeporteIndividual programarPartidoPractica(Socio organizador, ReservaInstalacionDeporteIndividual reserva, List<InscripcionDisciplinaIndividual> participantes, boolean intervieneEntrenador) {
		if (reserva == null || !"CONFIRMADA".equalsIgnoreCase(reserva.getEstado()) || !reserva.getSocios().contains(organizador)) {
			throw new IllegalArgumentException("No hay una reserva activa del socio para el partido.");
		}
		if (participantes == null || participantes.size() != reserva.getCantidadJugadores()) {
			throw new IllegalArgumentException("El numero de participantes debe ser " + reserva.getCantidadJugadores() + ", igual al de la reserva.");
		}
		String disciplina = reserva.getCancha().getDisciplina();
		for (int i = 0; i < participantes.size(); i++) {
			InscripcionDisciplinaIndividual participante = participantes.get(i);
			if (participantes.indexOf(participante) != i) {
				throw new IllegalArgumentException("El participante " + participante.getSocio().getNombre() + " esta repetido.");
			}
			if (!participante.getDisciplina().equalsIgnoreCase(disciplina)) {
				throw new IllegalArgumentException("El participante " + participante.getSocio().getNombre() + " es de otra disciplina.");
			}
			if (!inscripcionesIndividuales.contains(participante)) {
				throw new IllegalArgumentException("El participante " + participante.getSocio().getNombre() + " no esta inscrito.");
			}
			if (tieneAgendaOcupada(participante, reserva.getFecha(), reserva.getHoraInicio(), reserva.getHoraFin())) {
				throw new IllegalArgumentException("El participante " + participante.getSocio().getNombre() + " tiene otro partido o sesion en ese bloque (agenda).");
			}
		}
		PartidoDeporteIndividual partido = crearPartidoPractica(disciplina, reserva, intervieneEntrenador);
		for (int i = 0; i < participantes.size(); i++) {
			partido.agregarParticipante(participantes.get(i));
		}
		partido.setReserva(reserva);
		partidosPractica.add(partido);
		return partido;
	}

	private PartidoDeporteIndividual crearPartidoPractica(String disciplina, ReservaInstalacionDeporteIndividual reserva, boolean intervieneEntrenador) {
		if (disciplina.equalsIgnoreCase("PADEL")) {
			return new PartidoPadel(reserva.getFecha(), reserva.getHoraInicio(), reserva.getHoraFin(), reserva.getModalidad(), reserva.getCancha(), intervieneEntrenador);
		}
		if (disciplina.equalsIgnoreCase("TENIS_DE_MESA")) {
			return new PartidoTenisDeMesa(reserva.getFecha(), reserva.getHoraInicio(), reserva.getHoraFin(), reserva.getModalidad(), reserva.getCancha(), intervieneEntrenador);
		}
		return new PartidoTenis(reserva.getFecha(), reserva.getHoraInicio(), reserva.getHoraFin(), reserva.getModalidad(), reserva.getCancha(), intervieneEntrenador);
	}

	private boolean tieneAgendaOcupada(InscripcionDisciplinaIndividual participante, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
		for (int i = 0; i < partidosPractica.size(); i++) {
			PartidoDeporteIndividual otro = partidosPractica.get(i);
			if (otro.seCruzaCon(fecha, horaInicio, horaFin)) {
				for (int j = 0; j < otro.getParticipantes().size(); j++) {
					if (otro.getParticipantes().get(j).getSocio().equals(participante.getSocio())) {
						return true;
					}
				}
			}
		}
		int duracion = (int) java.time.Duration.between(horaInicio, horaFin).toMinutes();
		for (int i = 0; i < sesiones.size(); i++) {
			SesionDeEntrenamiento sesion = sesiones.get(i);
			if (sesion.getConvocados().contains(participante.getSocio()) && sesion.seCruzaCon(fecha, horaInicio, duracion)) {
				return true;
			}
		}
		return false;
	}

	public void registrarResultadoPractica(PartidoDeporteIndividual partido, List<EstadisticasPartidoIndividual> estadisticas) {
		if (partido == null || !partidosPractica.contains(partido)) {
			throw new IllegalArgumentException("El partido de practica no existe.");
		}
		if (partido.tieneResultado()) {
			throw new IllegalStateException("El partido ya tiene resultado.");
		}
		if (estadisticas == null || estadisticas.size() != partido.getParticipantes().size()) {
			throw new IllegalArgumentException("Debe registrar una estadistica por cada participante.");
		}
		Class<?> tipoEsperado = partido.crearEstadisticas().getClass();
		int ganadores = 0;
		for (int i = 0; i < estadisticas.size(); i++) {
			EstadisticasPartidoIndividual estadistica = estadisticas.get(i);
			if (estadistica == null || estadistica.getClass() != tipoEsperado) {
				throw new IllegalArgumentException("La estadistica no corresponde a la disciplina del partido.");
			}
			int sets = estadistica.getSetsGanados();
			boolean setsValidos = estadistica.esGanador() ? sets == partido.getSetsParaGanar() : sets >= 0 && sets < partido.getSetsParaGanar();
			if (!setsValidos) {
				throw new IllegalArgumentException("Marcador de sets imposible para el partido.");
			}
			if (estadistica.esGanador()) {
				ganadores = ganadores + 1;
			}
		}
		if (ganadores * 2 != estadisticas.size()) {
			throw new IllegalArgumentException("Marcador de sets imposible: debe haber un lado ganador y uno perdedor.");
		}
		for (int i = 0; i < estadisticas.size(); i++) {
			partido.registrarEstadistica(estadisticas.get(i));
		}
		partido.finalizar();
	}

	public String consultarRanking(Socio socio, String disciplina) {
		InscripcionDisciplinaIndividual inscripcion = buscarInscripcion(socio, disciplina);
		if (inscripcion == null) {
			throw new IllegalArgumentException("El socio no esta inscrito en " + disciplina + ".");
		}
		int posicion = 1;
		int total = 0;
		for (int i = 0; i < inscripcionesIndividuales.size(); i++) {
			InscripcionDisciplinaIndividual otra = inscripcionesIndividuales.get(i);
			if (otra.getDisciplina().equalsIgnoreCase(disciplina)) {
				total = total + 1;
				if (otra.getRankingElo() > inscripcion.getRankingElo()) {
					posicion = posicion + 1;
				}
			}
		}
		int jugados = contarPartidosJugados(inscripcion);
		String texto = disciplina.toUpperCase() + ": ELO " + inscripcion.getRankingElo() + ", posicion " + posicion + " de " + total;
		if (jugados == 0) {
			return texto + ", no hay partidos jugados";
		}
		return texto + ", partidos jugados: " + jugados;
	}

	private int contarPartidosJugados(InscripcionDisciplinaIndividual inscripcion) {
		int jugados = 0;
		for (int i = 0; i < partidosPractica.size(); i++) {
			if (partidosPractica.get(i).tieneResultado() && partidosPractica.get(i).getParticipantes().contains(inscripcion)) {
				jugados = jugados + 1;
			}
		}
		return jugados;
	}

	public List<PartidoDeporteIndividual> consultarHistorialPractica(Socio solicitante, Socio socio, String disciplina, LocalDate desde, LocalDate hasta) {
		if (solicitante == null || !solicitante.equals(socio)) {
			throw new IllegalArgumentException("No puede consultar el historial de otro socio.");
		}
		InscripcionDisciplinaIndividual inscripcion = buscarInscripcion(socio, disciplina);
		if (inscripcion == null) {
			throw new IllegalArgumentException("El socio no esta inscrito en " + disciplina + ".");
		}
		if (desde != null && hasta != null && hasta.isBefore(desde)) {
			throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial (rango invalido).");
		}
		List<PartidoDeporteIndividual> historial = new ArrayList<PartidoDeporteIndividual>();
		for (int i = 0; i < partidosPractica.size(); i++) {
			PartidoDeporteIndividual partido = partidosPractica.get(i);
			boolean enRango = (desde == null || !partido.getFecha().isBefore(desde)) && (hasta == null || !partido.getFecha().isAfter(hasta));
			if (partido.tieneResultado() && partido.getParticipantes().contains(inscripcion) && enRango) {
				historial.add(partido);
			}
		}
		historial.sort((a, b) -> {
			int porFecha = a.getFecha().compareTo(b.getFecha());
			return porFecha != 0 ? porFecha : a.getHoraInicio().compareTo(b.getHoraInicio());
		});
		return historial;
	}

	public SesionDeEntrenamiento planearSesionAcademia(Entrenador entrenador, LocalDate fecha, LocalTime hora, int duracion, CanchaDeporteIndividual cancha, List<Socio> convocados) {
		validarDatosSesion(entrenador, fecha, hora, duracion, cancha);
		if (convocados == null || convocados.isEmpty()) {
			throw new IllegalArgumentException("Debe convocar al menos un socio.");
		}
		for (int i = 0; i < convocados.size(); i++) {
			if (buscarInscripcion(convocados.get(i), cancha.getDisciplina()) == null) {
				throw new IllegalArgumentException("El convocado " + convocados.get(i).getNombre() + " no pertenece a la disciplina " + cancha.getDisciplina() + ".");
			}
		}
		SesionDeEntrenamiento sesion = entrenador.planearSesion(fecha, hora, duracion, cancha, convocados);
		sesion.setDisciplina(cancha.getDisciplina());
		sesiones.add(sesion);
		return sesion;
	}

	public SesionDeEntrenamiento planearSesionEquipo(Entrenador entrenador, LocalDate fecha, LocalTime hora, int duracion, InstalacionDeportiva instalacion, Equipo equipo) {
		if (equipo == null) {
			throw new IllegalArgumentException("Debe indicar el equipo.");
		}
		validarDatosSesion(entrenador, fecha, hora, duracion, instalacion);
		if (!(instalacion instanceof InstalacionDeporteConjunto) || !equipo.getDeporte().equalsIgnoreCase(((InstalacionDeporteConjunto) instalacion).getDeporte())) {
			throw new IllegalArgumentException("La instalacion " + instalacion.getNombre() + " no es compatible con la disciplina " + equipo.getDeporte() + ".");
		}
		List<Socio> jugadores = new ArrayList<Socio>();
		for (int i = 0; i < equipo.getInscripciones().size(); i++) {
			if (equipo.getInscripciones().get(i).estaHabilitado()) {
				jugadores.add(equipo.getInscripciones().get(i).getSocio());
			}
		}
		SesionDeEntrenamiento sesion = entrenador.planearSesion(fecha, hora, duracion, instalacion, jugadores);
		sesion.setEquipo(equipo);
		sesion.setDisciplina(equipo.getDeporte());
		sesiones.add(sesion);
		return sesion;
	}

	private void validarDatosSesion(Entrenador entrenador, LocalDate fecha, LocalTime hora, int duracion, InstalacionDeportiva instalacion) {
		if (entrenador == null || instalacion == null || fecha == null || hora == null) {
			throw new IllegalArgumentException("Faltan datos de la sesion.");
		}
		if (duracion <= 0) {
			throw new IllegalArgumentException("La duracion debe ser mayor que 0.");
		}
		if (fecha.isBefore(LocalDate.now())) {
			throw new IllegalArgumentException("La fecha de la sesion no puede ser pasada.");
		}
		if (entrenador.tieneConflictoHorario(fecha, hora, duracion)) {
			throw new IllegalArgumentException("El entrenador ya tiene una sesion traslapada en ese horario.");
		}
		boolean ocupada = !instalacion.estaDisponible(fecha, hora, hora.plusMinutes(duracion));
		for (int i = 0; i < sesiones.size() && !ocupada; i++) {
			ocupada = sesiones.get(i).getInstalacion() == instalacion && sesiones.get(i).seCruzaCon(fecha, hora, duracion);
		}
		if (ocupada) {
			throw new IllegalArgumentException("La instalacion " + instalacion.getNombre() + " esta ocupada en ese horario.");
		}
	}

	public void convocar(SesionDeEntrenamiento sesion, Socio socio) {
		boolean pertenece;
		if (sesion.getEquipo() != null) {
			pertenece = false;
			for (int i = 0; i < sesion.getEquipo().getInscripciones().size(); i++) {
				InscripcionEquipo inscripcion = sesion.getEquipo().getInscripciones().get(i);
				if (inscripcion.estaHabilitado() && inscripcion.getSocio().equals(socio)) {
					pertenece = true;
				}
			}
		} else {
			pertenece = buscarInscripcion(socio, sesion.getDisciplina()) != null;
		}
		if (!pertenece) {
			throw new IllegalArgumentException("El socio no pertenece al equipo o a la disciplina de la sesion.");
		}
		sesion.convocar(socio);
	}

	private List<SesionDeEntrenamiento> sesionesDeSocio(Socio socio) {
		List<SesionDeEntrenamiento> resultado = new ArrayList<SesionDeEntrenamiento>();
		for (int i = 0; i < sesiones.size(); i++) {
			if (sesiones.get(i).getConvocados().contains(socio)) {
				resultado.add(sesiones.get(i));
			}
		}
		resultado.sort((a, b) -> {
			int porFecha = a.getFecha().compareTo(b.getFecha());
			return porFecha != 0 ? porFecha : a.getHora().compareTo(b.getHora());
		});
		return resultado;
	}

	public String consultarConvocatorias(Socio socio) {
		List<SesionDeEntrenamiento> lista = sesionesDeSocio(socio);
		if (lista.isEmpty()) {
			return "No tiene convocatorias a entrenamiento.";
		}
		StringBuilder texto = new StringBuilder();
		for (int i = 0; i < lista.size(); i++) {
			SesionDeEntrenamiento sesion = lista.get(i);
			texto.append(sesion.getFecha()).append(" ").append(sesion.getHora())
				.append(" (").append(sesion.getDuracion()).append(" min) - ")
				.append(sesion.getInstalacion().getNombre()).append(" - Entrenador: ")
				.append(sesion.getEntrenador().getNombre()).append("\n");
		}
		return texto.toString();
	}

	public String consultarAsistencia(Socio socio) {
		List<SesionDeEntrenamiento> lista = sesionesDeSocio(socio);
		if (lista.isEmpty()) {
			return "No hay registros de asistencia.";
		}
		StringBuilder texto = new StringBuilder();
		for (int i = 0; i < lista.size(); i++) {
			SesionDeEntrenamiento sesion = lista.get(i);
			RegistroAsistencia registro = sesion.buscarRegistro(socio);
			texto.append(sesion.getFecha()).append(" ").append(sesion.getHora()).append(" - ");
			if (registro == null) {
				texto.append("Pendiente");
			} else {
				texto.append(registro.getEstadoAsistencia());
				if (registro.getNotaDesempeno() != null) {
					texto.append(" - Nota: ").append(registro.getNotaDesempeno()).append(" - ").append(registro.getComentario());
				}
			}
			texto.append("\n");
		}
		return texto.toString();
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

	public boolean tieneTurnoTraslapado(EmpleadoDelClub empleado, LocalDate fecha, LocalTime hora, int duracion, List<TurnoEmpleado> lista, TurnoEmpleado excluido) {
		for (int i = 0; i < lista.size(); i++) {
			TurnoEmpleado otro = lista.get(i);
			if (otro != excluido && otro.getEmpleado() == empleado && otro.seTraslapaCon(fecha, hora, duracion)) {
				return true;
			}
		}
		return false;
	}

	public String primerBloqueSinCobertura(LocalDate fecha, LocalTime desde, LocalTime hasta, List<TurnoEmpleado> lista) {
		LocalTime momento = desde;
		while (momento.isBefore(hasta)) {
			int entrenadores = 0;
			int fisioterapeutas = 0;
			for (int i = 0; i < lista.size(); i++) {
				TurnoEmpleado turno = lista.get(i);
				if (turno.seCruzaCon(fecha, momento)) {
					if (turno.getEmpleado() instanceof Entrenador) {
						entrenadores = entrenadores + 1;
					} else if (turno.getEmpleado() instanceof Fisioterapeuta) {
						fisioterapeutas = fisioterapeutas + 1;
					}
				}
			}
			if (entrenadores < 2 || fisioterapeutas < 1) {
				return fecha + " " + momento;
			}
			LocalTime siguiente = momento.plusMinutes(MINUTOS_BLOQUE);
			if (siguiente.isBefore(momento)) {
				break;
			}
			momento = siguiente;
		}
		return null;
	}

	public void publicarProgramacion(List<TurnoEmpleado> programacion) {
		List<TurnoEmpleado> combinados = new ArrayList<TurnoEmpleado>(turnos);
		for (int i = 0; i < programacion.size(); i++) {
			TurnoEmpleado turno = programacion.get(i);
			if (tieneTurnoTraslapado(turno.getEmpleado(), turno.getFecha(), turno.getHora(), turno.getDuracion(), combinados, null)) {
				throw new IllegalArgumentException("El empleado " + turno.getEmpleado().getNombre() + " ya tiene un turno en ese bloque (traslape).");
			}
			combinados.add(turno);
		}
		List<LocalDate> dias = new ArrayList<LocalDate>();
		for (int i = 0; i < programacion.size(); i++) {
			if (!dias.contains(programacion.get(i).getFecha())) {
				dias.add(programacion.get(i).getFecha());
			}
		}
		for (int i = 0; i < dias.size(); i++) {
			String bloque = primerBloqueSinCobertura(dias.get(i), HORA_APERTURA, HORA_CIERRE, combinados);
			if (bloque != null) {
				throw new IllegalArgumentException("Bloque sin cobertura minima (2 entrenadores y 1 fisioterapeuta): " + bloque + ".");
			}
		}
		turnos.addAll(programacion);
	}

	public SolicitudCambioTurnoEmpleado solicitarCambioTurno(EmpleadoDelClub empleado, TurnoEmpleado turno, LocalDate nuevaFecha, LocalTime nuevaHora) {
		if (turno == null || turno.getEmpleado() != empleado) {
			throw new IllegalArgumentException("El turno no pertenece al empleado.");
		}
		if (nuevaFecha == null || nuevaHora == null) {
			throw new IllegalArgumentException("Debe indicar el nuevo horario.");
		}
		if (nuevaFecha.equals(turno.getFecha()) && nuevaHora.equals(turno.getHora())) {
			throw new IllegalArgumentException("El nuevo horario es igual al actual.");
		}
		LocalTime fin = nuevaHora.plusHours(turno.getDuracion());
		if (nuevaHora.isBefore(HORA_APERTURA) || fin.isAfter(HORA_CIERRE) || fin.isBefore(nuevaHora)) {
			throw new IllegalArgumentException("El nuevo horario esta fuera del horario de atencion (" + HORA_APERTURA + " a " + HORA_CIERRE + ").");
		}
		for (int i = 0; i < getSolicitudesCambio().size(); i++) {
			SolicitudCambioTurnoEmpleado otra = getSolicitudesCambio().get(i);
			if (otra.getTurno() == turno && otra.getEstado() == EstadoSolicitud.PENDIENTE) {
				throw new IllegalStateException("El turno ya tiene una solicitud pendiente.");
			}
		}
		SolicitudCambioTurnoEmpleado solicitud = empleado.solicitarCambioTurno(turno, nuevaFecha, nuevaHora);
		getSolicitudesCambio().add(solicitud);
		return solicitud;
	}

	public List<SolicitudCambioTurnoEmpleado> getSolicitudesCambio() {
		if (solicitudesCambio == null) {
			solicitudesCambio = new ArrayList<SolicitudCambioTurnoEmpleado>();
		}
		return solicitudesCambio;
	}

	public String consultarTurnos(EmpleadoDelClub empleado) {
		List<TurnoEmpleado> propios = new ArrayList<TurnoEmpleado>();
		for (int i = 0; i < turnos.size(); i++) {
			if (turnos.get(i).getEmpleado() == empleado) {
				propios.add(turnos.get(i));
			}
		}
		if (propios.isEmpty()) {
			return "No tiene turnos programados.";
		}
		propios.sort((a, b) -> {
			int porFecha = a.getFecha().compareTo(b.getFecha());
			return porFecha != 0 ? porFecha : a.getHora().compareTo(b.getHora());
		});
		StringBuilder texto = new StringBuilder();
		for (int i = 0; i < propios.size(); i++) {
			TurnoEmpleado turno = propios.get(i);
			texto.append(turno.getFecha().getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, new java.util.Locale("es"))).append(" ").append(turno.getFecha()).append(" ")
				.append(turno.getHora()).append("-").append(turno.getHoraFin()).append("\n");
		}
		return texto.toString();
	}

	public String consultarSolicitudes(EmpleadoDelClub empleado) {
		StringBuilder texto = new StringBuilder();
		for (int i = 0; i < getSolicitudesCambio().size(); i++) {
			SolicitudCambioTurnoEmpleado solicitud = getSolicitudesCambio().get(i);
			if (solicitud.getTurno().getEmpleado() == empleado) {
				texto.append("Cambio a ").append(solicitud.getNuevaFecha()).append(" ").append(solicitud.getNuevaHora())
					.append(": ").append(solicitud.getEstado());
				if (solicitud.getMotivoRechazo() != null) {
					texto.append(" (motivo: ").append(solicitud.getMotivoRechazo()).append(")");
				}
				texto.append("\n");
			}
		}
		if (texto.length() == 0) {
			return "No tiene solicitudes de cambio.";
		}
		return texto.toString();
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

	private void validarConsultaAdministrativa(Usuario usuario, LocalDate desde, LocalDate hasta) {
		if (!(usuario instanceof Administrador)) {
			throw new IllegalArgumentException("Solo el administrador puede hacer esta consulta.");
		}
		if (desde == null || hasta == null || hasta.isBefore(desde)) {
			throw new IllegalArgumentException("Periodo invalido: la fecha final no puede ser anterior a la inicial.");
		}
	}

	private boolean enRango(LocalDate fecha, LocalDate desde, LocalDate hasta) {
		return !fecha.isBefore(desde) && !fecha.isAfter(hasta);
	}

	private boolean coincideDisciplina(String filtro, String disciplina) {
		return filtro == null || filtro.equalsIgnoreCase(disciplina);
	}

	private boolean equipoTieneSocio(Equipo equipo, Socio socio) {
		for (int i = 0; i < equipo.getInscripciones().size(); i++) {
			if (equipo.getInscripciones().get(i).getSocio().equals(socio)) {
				return true;
			}
		}
		return false;
	}

	private String nombresInscritos(List<InscripcionDisciplinaIndividual> inscripciones) {
		StringBuilder texto = new StringBuilder();
		for (int i = 0; i < inscripciones.size(); i++) {
			texto.append(i == 0 ? "" : " vs ").append(inscripciones.get(i).getSocio().getNombre());
		}
		return texto.toString();
	}

	private String ganadorPractica(PartidoDeporteIndividual partido) {
		for (int i = 0; i < partido.getEstadisticas().size(); i++) {
			if (partido.getEstadisticas().get(i).esGanador()) {
				return partido.getParticipantes().get(i).getSocio().getNombre();
			}
		}
		return "";
	}

	public String consultarHistorial(Usuario usuario, String tipo, LocalDate desde, LocalDate hasta, Socio socio, Entrenador entrenador, String disciplina) {
		validarConsultaAdministrativa(usuario, desde, hasta);
		String clase = tipo == null ? "" : tipo.toUpperCase();
		StringBuilder texto = new StringBuilder();
		if (clase.equals("RESERVAS")) {
			for (int i = 0; i < reservas.size(); i++) {
				ReservaInstalacionDeporteIndividual reserva = reservas.get(i);
				if (enRango(reserva.getFecha(), desde, hasta) && (socio == null || reserva.getSocios().contains(socio)) && entrenador == null
						&& coincideDisciplina(disciplina, reserva.getCancha().getDisciplina())) {
					texto.append(reserva.getFecha()).append(" ").append(reserva.getHoraInicio()).append("-").append(reserva.getHoraFin())
						.append(" | Reserva ").append(reserva.getCancha().getNombre()).append(" (").append(reserva.getCancha().getDisciplina())
						.append(") | Socios: ");
					for (int j = 0; j < reserva.getSocios().size(); j++) {
						texto.append(j == 0 ? "" : ", ").append(reserva.getSocios().get(j).getNombre());
					}
					texto.append("\n");
				}
			}
		} else if (clase.equals("PARTIDOS_INTERNOS")) {
			for (int i = 0; i < partidosInternos.size(); i++) {
				PartidoInterno partido = partidosInternos.get(i);
				boolean deSocio = socio == null || equipoTieneSocio(partido.getEquipoLocal(), socio) || equipoTieneSocio(partido.getEquipoVisitante(), socio);
				if (enRango(partido.getFecha(), desde, hasta) && deSocio && (entrenador == null || partido.getEntrenador() == entrenador)
						&& coincideDisciplina(disciplina, partido.getDeporte())) {
					texto.append(partido.getFecha()).append(" | Interno ").append(partido.getDeporte()).append(" ")
						.append(partido.getEquipoLocal().getNombre()).append(" vs ").append(partido.getEquipoVisitante().getNombre())
						.append(" | Entrenador: ").append(partido.getEntrenador() == null ? "-" : partido.getEntrenador().getNombre())
						.append(" | Resultado: ").append(partido.getResultado() == null ? "Pendiente" : partido.getResultado().toString()).append("\n");
				}
			}
		} else if (clase.equals("PARTIDOS_OFICIALES")) {
			for (int i = 0; i < competencias.size(); i++) {
				for (int j = 0; j < competencias.get(i).getPartidos().size(); j++) {
					PartidoDeCompetencia partido = competencias.get(i).getPartidos().get(j);
					if (enRango(partido.getFecha(), desde, hasta) && (socio == null || equipoTieneSocio(partido.getEquipoClub(), socio))
							&& (entrenador == null || partido.getEntrenador() == entrenador) && coincideDisciplina(disciplina, partido.getDeporte())) {
						texto.append(partido.getFecha()).append(" | Oficial ").append(partido.getDeporte()).append(" ").append(partido.toString())
							.append(" (").append(competencias.get(i).getNombre()).append(") | Entrenador: ")
							.append(partido.getEntrenador() == null ? "-" : partido.getEntrenador().getNombre())
							.append(" | Resultado: ").append(partido.getResultado() == null ? "Pendiente" : partido.getResultado().toString()).append("\n");
					}
				}
			}
		} else if (clase.equals("PARTIDOS_PRACTICA")) {
			for (int i = 0; i < partidosPractica.size(); i++) {
				PartidoDeporteIndividual partido = partidosPractica.get(i);
				boolean deSocio = socio == null;
				for (int j = 0; j < partido.getParticipantes().size() && !deSocio; j++) {
					deSocio = partido.getParticipantes().get(j).getSocio().equals(socio);
				}
				if (enRango(partido.getFecha(), desde, hasta) && deSocio && entrenador == null
						&& coincideDisciplina(disciplina, partido.getCancha().getDisciplina())) {
					texto.append(partido.getFecha()).append(" ").append(partido.getHoraInicio()).append(" | Practica ")
						.append(partido.getCancha().getDisciplina()).append(" ").append(nombresInscritos(partido.getParticipantes()))
						.append(" | Resultado: ").append(partido.tieneResultado() ? "gano " + ganadorPractica(partido) : "Pendiente").append("\n");
				}
			}
		} else if (clase.equals("SESIONES")) {
			for (int i = 0; i < sesiones.size(); i++) {
				SesionDeEntrenamiento sesion = sesiones.get(i);
				String disciplinaSesion = sesion.getDisciplina() != null ? sesion.getDisciplina()
						: sesion.getInstalacion() instanceof CanchaDeporteIndividual ? ((CanchaDeporteIndividual) sesion.getInstalacion()).getDisciplina() : "";
				if (enRango(sesion.getFecha(), desde, hasta) && (socio == null || sesion.getConvocados().contains(socio))
						&& (entrenador == null || sesion.getEntrenador() == entrenador) && coincideDisciplina(disciplina, disciplinaSesion)) {
					texto.append(sesion.getFecha()).append(" ").append(sesion.getHora()).append(" (").append(sesion.getDuracion())
						.append(" min) | Sesion ").append(disciplinaSesion).append(" en ").append(sesion.getInstalacion().getNombre())
						.append(" | Entrenador: ").append(sesion.getEntrenador().getNombre()).append(" | Convocados: ");
					for (int j = 0; j < sesion.getConvocados().size(); j++) {
						texto.append(j == 0 ? "" : ", ").append(sesion.getConvocados().get(j).getNombre());
					}
					texto.append("\n");
				}
			}
		} else {
			throw new IllegalArgumentException("Tipo de historial invalido: " + tipo + ".");
		}
		if (texto.length() == 0) {
			return "No hay registros en ese periodo.";
		}
		return texto.toString();
	}

	public String consultarReporteVentas(String rubro, String periodo, LocalDate fecha) {
		String r = rubro == null ? "" : rubro.toUpperCase();
		String p = periodo == null ? "" : periodo.toUpperCase();
		if (!r.equals("TIENDA") && !r.equals("CAFETERIA") && !r.equals("MENSUALIDADES")) {
			throw new IllegalArgumentException("Rubro invalido: " + rubro + ".");
		}
		LocalDate desde;
		LocalDate hasta;
		if (fecha != null && p.equals("DIARIO")) {
			desde = fecha;
			hasta = fecha;
		} else if (fecha != null && p.equals("SEMANAL")) {
			desde = fecha.minusDays(fecha.getDayOfWeek().getValue() - 1);
			hasta = desde.plusDays(6);
		} else if (fecha != null && p.equals("MENSUAL")) {
			desde = fecha.withDayOfMonth(1);
			hasta = fecha.withDayOfMonth(fecha.lengthOfMonth());
		} else {
			throw new IllegalArgumentException("Periodo invalido: " + periodo + ".");
		}
		int cantidad = 0;
		double neto = 0;
		double iva = 0;
		double consumo = 0;
		double propinas = 0;
		double total = 0;
		if (r.equals("TIENDA")) {
			for (int i = 0; i < tienda.getVentas().size(); i++) {
				VentaTienda venta = tienda.getVentas().get(i);
				if (enRango(venta.getFecha(), desde, hasta)) {
					cantidad = cantidad + 1;
					neto = neto + venta.getBaseGravable();
					iva = iva + venta.calcularImpuestos();
					total = total + venta.calcularTotal();
				}
			}
		} else if (r.equals("CAFETERIA")) {
			for (int i = 0; i < cafeteria.getVentas().size(); i++) {
				VentaCafeteria venta = cafeteria.getVentas().get(i);
				if (enRango(venta.getFecha(), desde, hasta)) {
					cantidad = cantidad + 1;
					neto = neto + venta.getBaseGravable();
					consumo = consumo + venta.calcularImpuestos();
					propinas = propinas + venta.calcularPropina();
					total = total + venta.calcularTotal();
				}
			}
		} else {
			for (int i = 0; i < pagosMensualidades.size(); i++) {
				PagoMensualidad pago = pagosMensualidades.get(i);
				if (enRango(pago.getFechaPago(), desde, hasta)) {
					cantidad = cantidad + 1;
					neto = neto + pago.getValor();
					total = total + pago.getValor();
				}
			}
		}
		if (cantidad == 0) {
			return "No hay datos de ventas de " + r + " para ese periodo.";
		}
		return "Reporte " + r + " " + p + " (" + desde + " a " + hasta + "): ventas " + cantidad + ", neto " + neto + ", IVA " + iva
				+ ", impuesto al consumo " + consumo + ", propinas " + propinas + ", total " + total;
	}

	public String consultarReporteDeportivo(Usuario usuario, String tipo, LocalDate desde, LocalDate hasta) {
		validarConsultaAdministrativa(usuario, desde, hasta);
		String clase = tipo == null ? "" : tipo.toUpperCase();
		StringBuilder texto = new StringBuilder();
		if (clase.equals("ASISTENCIA")) {
			for (int i = 0; i < sesiones.size(); i++) {
				SesionDeEntrenamiento sesion = sesiones.get(i);
				if (enRango(sesion.getFecha(), desde, hasta)) {
					for (int j = 0; j < sesion.getAsistencias().size(); j++) {
						RegistroAsistencia registro = sesion.getAsistencias().get(j);
						texto.append(sesion.getFecha()).append(" | ").append(sesion.getInstalacion().getNombre()).append(": ")
							.append(registro.getSocio().getNombre()).append(" ").append(registro.getEstadoAsistencia()).append("\n");
					}
				}
			}
		} else if (clase.equals("RESULTADOS")) {
			for (int i = 0; i < partidosInternos.size(); i++) {
				PartidoInterno partido = partidosInternos.get(i);
				if (partido.getResultado() != null && enRango(partido.getFecha(), desde, hasta)) {
					int[] marcador = marcadorDe(partido.getResultado());
					texto.append(partido.getFecha()).append(" | ").append(partido.getEquipoLocal().getNombre()).append(" ").append(marcador[0])
						.append(" - ").append(marcador[1]).append(" ").append(partido.getEquipoVisitante().getNombre()).append("\n");
				}
			}
			for (int i = 0; i < competencias.size(); i++) {
				for (int j = 0; j < competencias.get(i).getPartidos().size(); j++) {
					PartidoDeCompetencia partido = competencias.get(i).getPartidos().get(j);
					if (partido.getResultado() != null && enRango(partido.getFecha(), desde, hasta)) {
						int[] marcador = Competencia.marcadorDelClub(partido);
						texto.append(partido.getFecha()).append(" | ").append(partido.getEquipoClub().getNombre()).append(" ").append(marcador[0])
							.append(" - ").append(marcador[1]).append(" ").append(partido.getInstitucionRival())
							.append(" (").append(competencias.get(i).getNombre()).append(")\n");
					}
				}
			}
			for (int i = 0; i < partidosPractica.size(); i++) {
				PartidoDeporteIndividual partido = partidosPractica.get(i);
				if (partido.tieneResultado() && enRango(partido.getFecha(), desde, hasta)) {
					texto.append(partido.getFecha()).append(" | ").append(nombresInscritos(partido.getParticipantes()))
						.append(": gano ").append(ganadorPractica(partido)).append("\n");
				}
			}
		} else if (clase.equals("EVOLUCION_ELO")) {
			List<InscripcionDisciplinaIndividual> jugadores = new ArrayList<InscripcionDisciplinaIndividual>();
			for (int i = 0; i < partidosPractica.size(); i++) {
				PartidoDeporteIndividual partido = partidosPractica.get(i);
				if (partido.tieneResultado() && enRango(partido.getFecha(), desde, hasta)) {
					for (int j = 0; j < partido.getParticipantes().size(); j++) {
						if (!jugadores.contains(partido.getParticipantes().get(j))) {
							jugadores.add(partido.getParticipantes().get(j));
						}
					}
				}
			}
			for (int i = 0; i < jugadores.size(); i++) {
				InscripcionDisciplinaIndividual jugador = jugadores.get(i);
				texto.append(jugador.getSocio().getNombre()).append(" (").append(jugador.getDisciplina()).append("): ")
					.append(jugador.getHistorialElo().get(0)).append(" -> ").append(jugador.getRankingElo()).append("\n");
			}
		} else {
			throw new IllegalArgumentException("Tipo de reporte invalido: " + tipo + ".");
		}
		if (texto.length() == 0) {
			return "No hay datos para ese periodo.";
		}
		return texto.toString();
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
			this.torneos = clubCargado.torneos;
			if (this.torneos == null) {
				this.torneos = new ArrayList<Torneo>();
			}
			if (clubCargado.pagosMensualidades == null) {
				clubCargado.pagosMensualidades = new ArrayList<PagoMensualidad>();
			}
			this.pagosMensualidades = clubCargado.pagosMensualidades;
			if (clubCargado.partidosPractica == null) {
				clubCargado.partidosPractica = new ArrayList<PartidoDeporteIndividual>();
			}
			this.partidosPractica = clubCargado.partidosPractica;
			if (clubCargado.competencias == null) {
				clubCargado.competencias = new ArrayList<Competencia>();
			}
			this.competencias = clubCargado.competencias;
			this.sugerencias = clubCargado.getSugerencias();
			this.solicitudesCambio = clubCargado.getSolicitudesCambio();
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

	public PartidoInterno programarPartidoInterno(Entrenador entrenador, Equipo local, Equipo visitante, LocalDate fecha) {
		if (entrenador == null || local == null || visitante == null || fecha == null) {
			throw new IllegalArgumentException("Faltan datos del partido.");
		}
		if (fecha.isBefore(LocalDate.now())) {
			throw new IllegalArgumentException("La fecha del partido no puede ser pasada.");
		}
		if (!local.isActivo() || !visitante.isActivo()) {
			throw new IllegalArgumentException("Ambos equipos deben estar activos.");
		}
		PartidoInterno partido = new PartidoInterno(fecha, local, visitante);
		validarMinimoHabilitados(local);
		validarMinimoHabilitados(visitante);
		partido.setEntrenador(entrenador);
		partidosInternos.add(partido);
		return partido;
	}

	private void validarMinimoHabilitados(Equipo equipo) {
		int faltan = equipo.getMinimoJugadoresHabilitados() - equipo.contarJugadoresHabilitados();
		if (faltan > 0) {
			throw new IllegalArgumentException("Al equipo " + equipo.getNombre() + " le faltan " + faltan + " jugadores habilitados.");
		}
	}

	public void registrarResultadoInterno(Entrenador entrenador, PartidoInterno partido, ResultadoPartidoEquipo resultado) {
		if (partido == null || !partidosInternos.contains(partido)) {
			throw new IllegalArgumentException("El partido no existe.");
		}
		if (partido.getEntrenador() != entrenador) {
			throw new IllegalArgumentException("El partido no fue programado por este entrenador.");
		}
		if (partido.isFinalizado()) {
			throw new IllegalStateException("El partido ya tiene resultado.");
		}
		if (partido.getFecha().isAfter(LocalDate.now())) {
			throw new IllegalStateException("El partido aun no se ha disputado.");
		}
		if (resultado == null) {
			throw new IllegalArgumentException("Debe indicar el marcador.");
		}
		partido.registrarResultado(resultado);
	}

	public String consultarEstadisticasEquipo(Socio socio, Equipo equipo) {
		InscripcionEquipo inscripcion = null;
		for (int i = 0; i < equipo.getInscripciones().size(); i++) {
			InscripcionEquipo candidata = equipo.getInscripciones().get(i);
			if (candidata.isActiva() && candidata.getSocio().equals(socio)) {
				inscripcion = candidata;
			}
		}
		if (inscripcion == null) {
			throw new IllegalArgumentException("El socio no pertenece al equipo " + equipo.getNombre() + ".");
		}
		List<PartidoDeEquipo> jugados = new ArrayList<PartidoDeEquipo>();
		List<Boolean> comoLocal = new ArrayList<Boolean>();
		for (int i = 0; i < partidosInternos.size(); i++) {
			PartidoInterno partido = partidosInternos.get(i);
			if (partido.getResultado() != null && (partido.getEquipoLocal() == equipo || partido.getEquipoVisitante() == equipo)) {
				jugados.add(partido);
				comoLocal.add(partido.getEquipoLocal() == equipo);
			}
		}
		for (int i = 0; i < competencias.size(); i++) {
			for (int j = 0; j < competencias.get(i).getPartidos().size(); j++) {
				PartidoDeCompetencia partido = competencias.get(i).getPartidos().get(j);
				if (partido.getResultado() != null && partido.getEquipoClub() == equipo) {
					jugados.add(partido);
					comoLocal.add(partido.isEsLocal());
				}
			}
		}
		int ganados = 0;
		int empatados = 0;
		int aFavor = 0;
		int enContra = 0;
		int[] propias = new int[2];
		for (int i = 0; i < jugados.size(); i++) {
			ResultadoPartidoEquipo resultado = jugados.get(i).getResultado();
			int[] marcador = marcadorDe(resultado);
			int propio = comoLocal.get(i) ? marcador[0] : marcador[1];
			int rival = comoLocal.get(i) ? marcador[1] : marcador[0];
			aFavor = aFavor + propio;
			enContra = enContra + rival;
			if (resultado.esEmpate()) {
				empatados = empatados + 1;
			} else if (propio > rival) {
				ganados = ganados + 1;
			}
			List<EstadisticasPartidoDeEquipo> mias = jugados.get(i).getEstadisticasDe(inscripcion);
			for (int j = 0; j < mias.size(); j++) {
				sumarEstadisticas(propias, mias.get(j));
			}
		}
		int perdidos = jugados.size() - ganados - empatados;
		String texto = "Equipo " + equipo.getNombre() + ": jugados " + jugados.size() + ", ganados " + ganados + ", empatados " + empatados
				+ ", perdidos " + perdidos + ", a favor " + aFavor + ", en contra " + enContra;
		if (jugados.isEmpty()) {
			texto = texto + ", no hay partidos jugados";
		}
		return texto + "\nMis estadisticas: " + describirEstadisticas(equipo.getDeporte(), propias);
	}

	private int[] marcadorDe(ResultadoPartidoEquipo resultado) {
		if (resultado instanceof ResultadoPorPuntos) {
			return new int[] { ((ResultadoPorPuntos) resultado).getPuntosLocal(), ((ResultadoPorPuntos) resultado).getPuntosVisitante() };
		}
		if (resultado instanceof ResultadoVoleibol) {
			return new int[] { ((ResultadoVoleibol) resultado).getSetsGanadosLocal(), ((ResultadoVoleibol) resultado).getSetsGanadosVisitante() };
		}
		return new int[] { 0, 0 };
	}

	private void sumarEstadisticas(int[] acumulado, EstadisticasPartidoDeEquipo estadistica) {
		if (estadistica instanceof EstadisticasPartidoFutbol) {
			acumulado[0] = acumulado[0] + ((EstadisticasPartidoFutbol) estadistica).getGoles();
			acumulado[1] = acumulado[1] + ((EstadisticasPartidoFutbol) estadistica).getAsistencias();
		} else if (estadistica instanceof EstadisticasPartidoBaloncesto) {
			acumulado[0] = acumulado[0] + ((EstadisticasPartidoBaloncesto) estadistica).getPuntos();
			acumulado[1] = acumulado[1] + ((EstadisticasPartidoBaloncesto) estadistica).getRebotes();
		} else if (estadistica instanceof EstadisticasPartidoVoleibol) {
			acumulado[0] = acumulado[0] + ((EstadisticasPartidoVoleibol) estadistica).getAces();
			acumulado[1] = acumulado[1] + ((EstadisticasPartidoVoleibol) estadistica).getBloqueos();
		}
	}

	private String describirEstadisticas(String deporte, int[] valores) {
		if (deporte.equalsIgnoreCase("BALONCESTO")) {
			return "puntos " + valores[0] + ", rebotes " + valores[1];
		}
		if (deporte.equalsIgnoreCase("VOLEIBOL")) {
			return "aces " + valores[0] + ", bloqueos " + valores[1];
		}
		return "goles " + valores[0] + ", asistencias " + valores[1];
	}

	public Equipo buscarEquipoActivo(String nombre, String deporte, String categoria) {
		for (int i = 0; i < equipos.size(); i++) {
			Equipo equipo = equipos.get(i);
			if (equipo.isActivo() && equipo.getNombre().equalsIgnoreCase(nombre) && equipo.getDeporte().equalsIgnoreCase(deporte) && equipo.getCategoria().equalsIgnoreCase(categoria)) {
				return equipo;
			}
		}
		return null;
	}

	public Equipo buscarEquipoActivoDeSocio(Socio socio, String deporte) {
		for (int i = 0; i < equipos.size(); i++) {
			Equipo equipo = equipos.get(i);
			if (equipo.isActivo() && equipo.getDeporte().equalsIgnoreCase(deporte)) {
				for (int j = 0; j < equipo.getInscripciones().size(); j++) {
					InscripcionEquipo inscripcion = equipo.getInscripciones().get(j);
					if (inscripcion.isActiva() && inscripcion.getSocio().equals(socio)) {
						return equipo;
					}
				}
			}
		}
		return null;
	}

	public void registrarCompetencia(Competencia competencia) {
		if (competencia != null && !competencias.contains(competencia)) {
			competencias.add(competencia);
		}
	}

	public Competencia buscarCompetencia(String nombre, int temporada) {
		for (int i = 0; i < competencias.size(); i++) {
			if (competencias.get(i).getNombre().equalsIgnoreCase(nombre) && competencias.get(i).getAnioTemporada() == temporada) {
				return competencias.get(i);
			}
		}
		return null;
	}

	public Competencia buscarCompetencia(String nombre) {
		for (int i = 0; i < competencias.size(); i++) {
			if (competencias.get(i).getNombre().equalsIgnoreCase(nombre)) {
				return competencias.get(i);
			}
		}
		return null;
	}

	public String consultarTablaPosiciones(String nombreCompetencia, String grupo) {
		Competencia competencia = buscarCompetencia(nombreCompetencia);
		if (competencia == null || !competencia.tieneResultados()) {
			return "No hay informacion de la competencia " + nombreCompetencia + ".";
		}
		if (grupo != null && competencia instanceof Ascun) {
			Ascun ascun = (Ascun) competencia;
			StringBuilder texto = new StringBuilder(describirTabla(ascun.getTablaGrupo(grupo)));
			texto.append("Clasifican:");
			List<Equipo> clasificados = ascun.getClasificados(grupo);
			for (int i = 0; i < clasificados.size(); i++) {
				texto.append(i == 0 ? " " : ", ").append(clasificados.get(i).getNombre());
			}
			return texto.toString();
		}
		return describirTabla(competencia.ordenarTabla(competencia.getNominas()));
	}

	public String consultarTablas(String deporte, String categoria) {
		StringBuilder texto = new StringBuilder();
		for (int i = 0; i < competencias.size(); i++) {
			Competencia competencia = competencias.get(i);
			boolean coincide = (deporte == null || competencia.getDeporte().equalsIgnoreCase(deporte))
					&& (categoria == null || competencia.getCategoriaHabilitada().equalsIgnoreCase(categoria));
			if (coincide && competencia.tieneResultados()) {
				texto.append(competencia.getNombre()).append("\n").append(describirTabla(competencia.ordenarTabla(competencia.getNominas())));
			}
		}
		if (texto.length() == 0) {
			return "No hay informacion para ese filtro.";
		}
		return texto.toString();
	}

	private String describirTabla(List<NominaCompetencia> tabla) {
		StringBuilder texto = new StringBuilder();
		for (int i = 0; i < tabla.size(); i++) {
			NominaCompetencia nomina = tabla.get(i);
			texto.append(i + 1).append(". ").append(nomina.getEquipo().getNombre()).append(" - ")
				.append(nomina.getPuntosAcumulados()).append(" pts | PJ ").append(nomina.getJugados())
				.append(", G ").append(nomina.getPartidosGanados()).append(", E ").append(nomina.getEmpatados())
				.append(", P ").append(nomina.getPerdidos()).append(", DIF ").append(nomina.getDiferencia()).append("\n");
		}
		return texto.toString();
	}

	public String consultarGoleadores(String nombreCompetencia) {
		return clasificacionIndividual(nombreCompetencia, true);
	}

	public String consultarDestacados(String nombreCompetencia) {
		return clasificacionIndividual(nombreCompetencia, false);
	}

	private String clasificacionIndividual(String nombreCompetencia, boolean goleadores) {
		Competencia competencia = buscarCompetencia(nombreCompetencia);
		if (competencia == null) {
			return "No hay informacion de la competencia " + nombreCompetencia + ".";
		}
		List<InscripcionEquipo> jugadores = new ArrayList<InscripcionEquipo>();
		List<Integer> valores = new ArrayList<Integer>();
		for (int i = 0; i < competencia.getNominas().size(); i++) {
			List<JugadorEnNomina> nomina = competencia.getNominas().get(i).getJugadores();
			for (int j = 0; j < nomina.size(); j++) {
				InscripcionEquipo jugador = nomina.get(j).getInscripcion();
				boolean tieneEstadisticas = false;
				int valor = 0;
				for (int k = 0; k < competencia.getPartidos().size(); k++) {
					List<EstadisticasPartidoDeEquipo> propias = competencia.getPartidos().get(k).getEstadisticasDe(jugador);
					for (int m = 0; m < propias.size(); m++) {
						tieneEstadisticas = true;
						valor = valor + valorIndividual(propias.get(m), goleadores);
					}
				}
				if (tieneEstadisticas) {
					int posicion = 0;
					while (posicion < valores.size() && valores.get(posicion) >= valor) {
						posicion = posicion + 1;
					}
					jugadores.add(posicion, jugador);
					valores.add(posicion, valor);
				}
			}
		}
		if (jugadores.isEmpty()) {
			return "No hay informacion de estadisticas en la competencia " + nombreCompetencia + ".";
		}
		String unidad = goleadores ? "goles" : unidadDestacado(competencia.getDeporte());
		StringBuilder texto = new StringBuilder();
		for (int i = 0; i < jugadores.size(); i++) {
			texto.append(i + 1).append(". ").append(jugadores.get(i).getSocio().getNombre()).append(" (")
				.append(jugadores.get(i).getEquipo().getNombre()).append(") - ").append(valores.get(i)).append(" ").append(unidad).append("\n");
		}
		return texto.toString();
	}

	private int valorIndividual(EstadisticasPartidoDeEquipo estadistica, boolean goleadores) {
		if (estadistica instanceof EstadisticasPartidoFutbol) {
			EstadisticasPartidoFutbol futbol = (EstadisticasPartidoFutbol) estadistica;
			return goleadores ? futbol.getGoles() : futbol.getAsistencias();
		}
		if (goleadores) {
			return 0;
		}
		if (estadistica instanceof EstadisticasPartidoBaloncesto) {
			return ((EstadisticasPartidoBaloncesto) estadistica).getPuntos();
		}
		if (estadistica instanceof EstadisticasPartidoVoleibol) {
			return ((EstadisticasPartidoVoleibol) estadistica).getAces();
		}
		return 0;
	}

	private String unidadDestacado(String deporte) {
		if (deporte.equalsIgnoreCase("BALONCESTO")) {
			return "puntos";
		}
		if (deporte.equalsIgnoreCase("VOLEIBOL")) {
			return "aces";
		}
		return "asistencias";
	}

	public Competencia buscarCompetenciaEnCursoDeJugador(InscripcionEquipo inscripcion) {
		for (int i = 0; i < competencias.size(); i++) {
			if (competencias.get(i).isEnCurso() && competencias.get(i).estaEnNomina(inscripcion)) {
				return competencias.get(i);
			}
		}
		return null;
	}

	public Competencia buscarCompetenciaEnCursoDeEquipo(Equipo equipo) {
		for (int i = 0; i < competencias.size(); i++) {
			if (competencias.get(i).isEnCurso() && competencias.get(i).buscarNomina(equipo) != null) {
				return competencias.get(i);
			}
		}
		return null;
	}

	public int contarPartidosPendientes(Equipo equipo) {
		int pendientes = 0;
		for (int i = 0; i < partidosInternos.size(); i++) {
			PartidoInterno partido = partidosInternos.get(i);
			boolean participa = partido.getEquipoLocal() == equipo || partido.getEquipoVisitante() == equipo;
			if (participa && partido.getResultado() == null) {
				pendientes = pendientes + 1;
			}
		}
		return pendientes;
	}

	public void agregarTorneo(Torneo torneo) {
		if (torneo == null) {
			throw new IllegalArgumentException("El torneo no puede ser nulo.");
		}
		if (buscarTorneo(torneo.getId()) != null) {
			throw new IllegalArgumentException("Ya existe un torneo con el id " + torneo.getId());
		}
		torneos.add(torneo);
	}

	public Torneo buscarTorneo(String id) {
		if (id == null) {
			return null;
		}
		for (int i = 0; i < torneos.size(); i++) {
			if (torneos.get(i).getId().equalsIgnoreCase(id)) {
				return torneos.get(i);
			}
		}
		return null;
	}

	public String generarIdTorneo() {
		int numero = torneos.size() + 1;
		while (buscarTorneo("T-" + numero) != null) {
			numero = numero + 1;
		}
		return "T-" + numero;
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
	public List<Torneo> getTorneos() { return torneos; }
	public List<PagoMensualidad> getPagosMensualidades() { return pagosMensualidades; }
	public List<ReservaInstalacionDeporteIndividual> getReservas() { return reservas; }
	public List<PartidoDeporteIndividual> getPartidosPractica() { return partidosPractica; }
	public List<Competencia> getCompetencias() { return competencias; }
	public TiendaDeportiva getTienda() { return tienda; }
	public Cafeteria getCafeteria() { return cafeteria; }
	public UbicacionInventario getBodegaCentral() { return bodegaCentral; }
}
