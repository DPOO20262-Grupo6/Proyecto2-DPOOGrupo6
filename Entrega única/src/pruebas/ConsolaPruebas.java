package pruebas;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import administracion.SolicitudCambioTurnoEmpleado;
import administracion.Sugerencia;
import administracion.TurnoEmpleado;
import articulosYVentas.Articulo;
import articulosYVentas.ArticuloDeportivo;
import articulosYVentas.Bebida;
import articulosYVentas.Snack;
import articulosYVentas.VentaCafeteria;
import articulosYVentas.VentaTienda;
import club.ClubDeportivoSeneca;
import deportesDeConjunto.Equipo;
import deportesDeConjunto.NominaCompetencia;
import deportesDeConjunto.PartidoDeCompetencia;
import deportesDeConjunto.PartidoInterno;
import deportesDeConjunto.ResultadoPorPuntos;
import deportesDeConjunto.TorneoDeLosCerros;
import deportesIndividuales.PartidoTenis;
import deportesIndividuales.ReservaInstalacionDeporteIndividual;
import entrenamiento.SesionDeEntrenamiento;
import enumeraciones.EstadoAsistencia;
import enumeraciones.Modalidad;
import enumeraciones.NivelHabilidad;
import enumeraciones.TipoTarjeta;
import estadisticas.EstadisticasPartidoBaloncesto;
import estadisticas.EstadisticasPartidoFutbol;
import estadisticas.EstadisticasPartidoTenis;
import inscripciones.InscripcionDisciplinaIndividual;
import inscripciones.InscripcionEquipo;
import inscripciones.InscripcionEquipoFutbol;
import instalaciones.CanchaPadel;
import instalaciones.CanchaTenis;
import usuarios.Administrador;
import usuarios.Entrenador;
import usuarios.Fisioterapeuta;
import usuarios.Socio;
import usuarios.Usuario;

public class ConsolaPruebas {

	public static void main(String[] args) {
		String rutaDatos = "./datos";
		if (args.length > 0) {
			rutaDatos = args[0];
		}

		ClubDeportivoSeneca club = new ClubDeportivoSeneca();
		Administrador administrador = club.getAdministrador();

		List<Socio> sociosA = new ArrayList<Socio>();
		List<Socio> sociosB = new ArrayList<Socio>();

		for (int i = 1; i <= 8; i++) {
			Socio socioA = new Socio("Socio A" + i, "socioA" + i, "clave" + i, LocalDate.of(2004, 5, i));
			Socio socioB = new Socio("Socio B" + i, "socioB" + i, "clave" + i, LocalDate.of(2004, 8, i));
			club.registrarSocio(socioA);
			club.registrarSocio(socioB);
			sociosA.add(socioA);
			sociosB.add(socioB);
		}

		System.out.println("Prueba 1: Equipos e inscripcion de jugadores");
		Equipo equipoA = administrador.crearEquipo("FUTBOL", "Seneca A", "MAYORES", 8);
		Equipo equipoB = administrador.crearEquipo("FUTBOL", "Seneca B", "MAYORES", 8);
		List<InscripcionEquipo> inscripcionesA = new ArrayList<InscripcionEquipo>();
		List<InscripcionEquipo> inscripcionesB = new ArrayList<InscripcionEquipo>();

		for (int i = 0; i < 7; i++) {
			inscripcionesA.add(administrador.inscribirJugador(equipoA, sociosA.get(i), "Titular", i + 1));
			inscripcionesB.add(administrador.inscribirJugador(equipoB, sociosB.get(i), "Titular", i + 1));
		}
		System.out.println("Equipo creado: " + equipoA.getNombre() + " con " + equipoA.contarJugadoresHabilitados() + " jugadores");

		try {
			Socio socioMenor = new Socio("Menor", "menor1", "123", LocalDate.of(2015, 1, 1));
			administrador.inscribirJugador(equipoA, socioMenor, "Delantero", 99);
		} catch (Exception e) {
			System.out.println("Error esperado: " + e.getMessage());
		}

		System.out.println("\nPrueba 2: Partidos internos");
		PartidoInterno partidoInterno = administrador.programarPartidoInterno(equipoA, equipoB, LocalDate.now());
		for (int i = 0; i < inscripcionesA.size(); i++) {
			partidoInterno.registrarParticipacion(inscripcionesA.get(i));
			partidoInterno.registrarParticipacion(inscripcionesB.get(i));
		}
		administrador.registrarResultado(partidoInterno, new ResultadoPorPuntos(2, 1));
		System.out.println("Partido registrado: " + partidoInterno.toString());

		try {
		    Equipo equipoVacio = administrador.crearEquipo("FUTBOL", "Seneca C", "MAYORES", 8);
		    PartidoInterno partidoVacio = administrador.programarPartidoInterno(equipoA, equipoVacio, LocalDate.now());
		    administrador.registrarResultado(partidoVacio, new ResultadoPorPuntos(0, 0));
		} catch (Exception e) {
		    System.out.println("Error esperado: " + e.getMessage());
		}

		System.out.println("\nPrueba 3: Participacion y estadisticas");
		EstadisticasPartidoFutbol estadisticasJugador1 = new EstadisticasPartidoFutbol();
		estadisticasJugador1.registrarGol();
		estadisticasJugador1.registrarAsistencia();
		partidoInterno.registrarEstadistica(inscripcionesA.get(0), estadisticasJugador1);
		System.out.println("Estadisticas jugador 1: " + estadisticasJugador1.getResumen());

		try {
			partidoInterno.registrarEstadistica(inscripcionesA.get(0), new EstadisticasPartidoBaloncesto());
		} catch (Exception e) {
			System.out.println("Error esperado: " + e.getMessage());
		}

		System.out.println("\nPrueba 4: Sanciones");
		EstadisticasPartidoFutbol estadisticasTarjetaRoja = new EstadisticasPartidoFutbol();
		estadisticasTarjetaRoja.registrarTarjeta(TipoTarjeta.ROJA, 40);
		partidoInterno.registrarEstadistica(inscripcionesA.get(1), estadisticasTarjetaRoja);
		InscripcionEquipoFutbol inscripcionFutbol = (InscripcionEquipoFutbol) inscripcionesA.get(1);
		System.out.println("Jugador sancionado suspendido: " + inscripcionFutbol.estaSuspendidoInterno());

		administrador.inscribirJugador(equipoA, sociosA.get(7), "Suplente", 20);
		PartidoInterno partidoInterno2 = administrador.programarPartidoInterno(equipoA, equipoB, LocalDate.now().plusDays(3));
		try {
			partidoInterno2.registrarParticipacion(inscripcionesA.get(1));
		} catch (Exception e) {
			System.out.println("Error esperado: " + e.getMessage());
		}

		System.out.println("\nPrueba 5: Competencias");
		TorneoDeLosCerros torneo = new TorneoDeLosCerros("Torneo Cerros", 2026, "FUTBOL", "MAYORES", 7);
		NominaCompetencia nomina = administrador.inscribirEquipoEnCompetencia(equipoA, torneo, inscripcionesA);
		PartidoDeCompetencia partidoCompetencia = torneo.getFormatoLiga().programarPartido(equipoA, "UniRival", LocalDate.now(), "Cancha Central");
		partidoCompetencia.registrarParticipacion(inscripcionesA.get(0));
		EstadisticasPartidoFutbol estadisticasCompetencia = new EstadisticasPartidoFutbol();
		estadisticasCompetencia.registrarGol();
		partidoCompetencia.registrarEstadistica(inscripcionesA.get(0), estadisticasCompetencia);
		partidoCompetencia.registrarResultado(new ResultadoPorPuntos(1, 0));
		torneo.getFormatoLiga().actualizarTabla(partidoCompetencia);
		System.out.println("Puntos en competencia: " + nomina.getPuntosAcumulados());

		try {
			partidoCompetencia.registrarParticipacion(inscripcionesB.get(0));
		} catch (Exception e) {
			System.out.println("Error esperado: " + e.getMessage());
		}

		System.out.println("\nPrueba 6: Reservas de instalaciones");
		CanchaPadel canchaPadel = new CanchaPadel("Padel 1", true);
		CanchaTenis canchaTenis = new CanchaTenis("Tenis 1");
		List<Socio> cuatroSocios = new ArrayList<Socio>();
		cuatroSocios.add(sociosA.get(0));
		cuatroSocios.add(sociosA.get(1));
		cuatroSocios.add(sociosA.get(2));
		cuatroSocios.add(sociosA.get(3));

		ReservaInstalacionDeporteIndividual reserva1 = club.reservarInstalacion(cuatroSocios, canchaPadel, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(11, 0), Modalidad.DOBLES);
		System.out.println(reserva1.toString());

		List<Socio> dosSocios = new ArrayList<Socio>();
		dosSocios.add(sociosB.get(0));
		dosSocios.add(sociosB.get(1));
		ReservaInstalacionDeporteIndividual reserva2 = club.reservarInstalacion(dosSocios, canchaPadel, LocalDate.now(), LocalTime.of(11, 0), LocalTime.of(12, 0), Modalidad.SENCILLOS);
		System.out.println(reserva2.toString());

		System.out.println("\nPrueba 7: Partidos individuales y ranking ELO");
		InscripcionDisciplinaIndividual individual1 = sociosA.get(0).inscribirseDisciplina("TENIS", NivelHabilidad.PRINCIPIANTE);
		club.registrarInscripcionIndividual(individual1);
		InscripcionDisciplinaIndividual individual2 = sociosA.get(1).inscribirseDisciplina("TENIS", NivelHabilidad.INTERMEDIO);
		club.registrarInscripcionIndividual(individual2);
		InscripcionDisciplinaIndividual individual3 = sociosA.get(2).inscribirseDisciplina("TENIS", NivelHabilidad.AVANZADO);
		club.registrarInscripcionIndividual(individual3);

		PartidoTenis partidoTenis = new PartidoTenis(LocalDate.now(), LocalTime.of(14, 0), LocalTime.of(15, 0), Modalidad.SENCILLOS, canchaTenis, false);
		partidoTenis.agregarParticipante(individual1);
		partidoTenis.agregarParticipante(individual2);
		partidoTenis.registrarEstadistica(new EstadisticasPartidoTenis(2, 12, 3, 2, true));
		partidoTenis.registrarEstadistica(new EstadisticasPartidoTenis(0, 5, 1, 6, false));
		partidoTenis.finalizar();
		System.out.println("Nuevo ELO jugador 1: " + individual1.getRankingElo() + ", jugador 2: " + individual2.getRankingElo());

		try {
			PartidoTenis partidoInvalido = new PartidoTenis(LocalDate.now(), LocalTime.of(16, 0), LocalTime.of(17, 0), Modalidad.SENCILLOS, canchaTenis, false);
			partidoInvalido.agregarParticipante(individual1);
			partidoInvalido.agregarParticipante(individual3);
		} catch (Exception e) {
			System.out.println("Error esperado: " + e.getMessage());
		}

		System.out.println("\nPrueba 8: Sesiones de entrenamiento");
		Entrenador entrenador1 = new Entrenador("Carlos", "ent1", "123");
		Entrenador entrenador2 = new Entrenador("Diana", "ent2", "123");
		Fisioterapeuta fisioterapeuta = new Fisioterapeuta("Laura", "fisio1", "123");
		club.registrarEmpleado(entrenador1);
		club.registrarEmpleado(entrenador2);
		club.registrarEmpleado(fisioterapeuta);

		SesionDeEntrenamiento sesion = entrenador1.planearSesion(LocalDate.now(), LocalTime.of(8, 0), 60, canchaTenis, dosSocios);
		club.getSesiones().add(sesion);
		entrenador1.registrarAsistencia(sesion, dosSocios.get(0), EstadoAsistencia.ASISTIO, 5, "Buen entrenamiento");
		System.out.println("Asistencia registrada: " + sesion.getAsistencias().get(0).toString());

		try {
			entrenador1.registrarAsistencia(sesion, dosSocios.get(1), EstadoAsistencia.ASISTIO, 10, "Nota invalida");
		} catch (Exception e) {
			System.out.println("Error esperado: " + e.getMessage());
		}

		System.out.println("\nPrueba 9: Compra en tienda");
		ArticuloDeportivo raqueta = new ArticuloDeportivo("Raqueta", 200000, "Tenis");
		ArticuloDeportivo balon = new ArticuloDeportivo("Balon", 80000, "Futbol");
		club.getTienda().agregarArticuloAlCatalogo(raqueta);
		club.getTienda().agregarArticuloAlCatalogo(balon);
		administrador.reabastecer(raqueta, club.getTienda().getUbicacionTienda(), 5);
		administrador.reabastecer(balon, club.getTienda().getUbicacionTienda(), 5);

		VentaTienda ventaTienda = new VentaTienda(LocalDate.now(), sociosA.get(0));
		ventaTienda.agregarDetalle(raqueta, 1);
		club.getTienda().procesarVenta(ventaTienda, null);
		System.out.println(ventaTienda.toString());

		try {
			VentaTienda ventaExceso = new VentaTienda(LocalDate.now(), sociosA.get(0));
			ventaExceso.agregarDetalle(raqueta, 20);
			club.getTienda().procesarVenta(ventaExceso, null);
		} catch (Exception e) {
			System.out.println("Error esperado: " + e.getMessage());
		}

		System.out.println("\nPrueba 10: Descuentos de empleado");
		VentaTienda ventaEmpleado = new VentaTienda(LocalDate.now(), entrenador1);
		ventaEmpleado.agregarDetalle(balon, 1);
		club.getTienda().procesarVenta(ventaEmpleado, null);
		System.out.println("Compra empleado (15%): " + ventaEmpleado.toString());

		VentaTienda ventaConCodigo = new VentaTienda(LocalDate.now(), sociosA.get(1));
		ventaConCodigo.agregarDetalle(balon, 1);
		club.getTienda().procesarVenta(ventaConCodigo, entrenador1.getCodigoDescuento());
		System.out.println("Compra socio con codigo (8%): " + ventaConCodigo.toString());

		try {
			VentaTienda ventaInvalida = new VentaTienda(LocalDate.now(), entrenador1);
			ventaInvalida.agregarDetalle(balon, 1);
			club.getTienda().procesarVenta(ventaInvalida, entrenador2.getCodigoDescuento());
		} catch (Exception e) {
			System.out.println("Error esperado: " + e.getMessage());
		}

		System.out.println("\nPrueba 11: Cafeteria y bebidas calientes");
		Bebida cafe = new Bebida("Cafe caliente", 5000, true);
		Bebida agua = new Bebida("Agua fria", 4000, false);
		List<String> alergenos = new ArrayList<String>();
		alergenos.add("Mani");
		Snack barraCereal = new Snack("Barra de cereal", 6000, alergenos);
		club.getCafeteria().agregarProducto(cafe);
		club.getCafeteria().agregarProducto(agua);
		club.getCafeteria().agregarProducto(barraCereal);

		List<Articulo> listaProductos = new ArrayList<Articulo>();
		listaProductos.add(barraCereal);
		System.out.println(club.getCafeteria().informarAlergenos(listaProductos));

		VentaCafeteria ventaCafeteria = new VentaCafeteria(LocalDate.now(), sociosA.get(0));
		ventaCafeteria.agregarDetalle(agua, 1);
		ventaCafeteria.agregarDetalle(barraCereal, 1);
		club.getCafeteria().procesarVenta(ventaCafeteria, canchaTenis);
		System.out.println(ventaCafeteria.toString());

		try {
			VentaCafeteria ventaCaliente = new VentaCafeteria(LocalDate.now(), sociosA.get(0));
			ventaCaliente.agregarDetalle(cafe, 1);
			club.getCafeteria().procesarVenta(ventaCaliente, canchaPadel);
		} catch (Exception e) {
			System.out.println("Error esperado: " + e.getMessage());
		}

		System.out.println("\nPrueba 12: Puntos de fidelidad");
		System.out.println("Puntos actuales socio: " + sociosA.get(0).getPuntosDeFidelidad());
		VentaCafeteria ventaRedimida = new VentaCafeteria(LocalDate.now(), sociosA.get(0));
		ventaRedimida.agregarDetalle(agua, 1);
		ventaRedimida.agregarDetalle(barraCereal, 1);
		ventaRedimida.redimirPuntos(2000);
		club.getCafeteria().procesarVenta(ventaRedimida, canchaTenis);
		System.out.println("Venta redimiendo puntos: " + ventaRedimida.toString() + " | Puntos restantes: " + sociosA.get(0).getPuntosDeFidelidad());

		try {
			VentaCafeteria ventaSinPuntos = new VentaCafeteria(LocalDate.now(), sociosA.get(0));
			ventaSinPuntos.agregarDetalle(agua, 1);
			ventaSinPuntos.redimirPuntos(900000);
			club.getCafeteria().procesarVenta(ventaSinPuntos, canchaTenis);
		} catch (Exception e) {
			System.out.println("Error esperado: " + e.getMessage());
		}

		System.out.println("\nPrueba 13: Inventario y sugerencias");
		administrador.reabastecer(raqueta, club.getBodegaCentral(), 10);
		administrador.trasladarArticulo(raqueta, club.getBodegaCentral(), club.getTienda().getUbicacionTienda(), 3);
		Sugerencia sugerencia = entrenador1.proponerSugerenciaArticulo("Cuerda", 30000, "Accesorios");
		administrador.evaluarSugerencia(sugerencia, true);
		System.out.println("Stock bodega: " + club.getBodegaCentral().obtenerCantidad(raqueta) + " | Sugerencia: " + sugerencia.getEstado());

		try {
			administrador.trasladarArticulo(raqueta, club.getBodegaCentral(), club.getTienda().getUbicacionTienda(), 100);
		} catch (Exception e) {
			System.out.println("Error esperado: " + e.getMessage());
		}

		System.out.println("\nPrueba 14: Turnos administrativos");
		Entrenador entrenador3 = new Entrenador("Andres", "ent3", "123");
		club.registrarEmpleado(entrenador3);
		LocalDate fechaActual = LocalDate.now();
		LocalTime horaActual = LocalTime.of(9, 0);
		TurnoEmpleado turno1 = administrador.asignarTurno(entrenador1, fechaActual, horaActual, 4);
		administrador.asignarTurno(entrenador2, fechaActual, horaActual, 4);
		TurnoEmpleado turno3 = administrador.asignarTurno(entrenador3, fechaActual, horaActual, 4);
		administrador.asignarTurno(fisioterapeuta, fechaActual, horaActual, 4);

		SolicitudCambioTurnoEmpleado solicitud1 = entrenador3.solicitarCambioTurno(turno3, fechaActual.plusDays(1), horaActual);
		administrador.evaluarSolicitudCambio(solicitud1);
		System.out.println("Solicitud 1: " + solicitud1.getEstado());

		SolicitudCambioTurnoEmpleado solicitud2 = entrenador1.solicitarCambioTurno(turno1, fechaActual.plusDays(1), horaActual);
		try {
			administrador.evaluarSolicitudCambio(solicitud2);
		} catch (Exception e) {
			System.out.println("Error esperado: " + e.getMessage());
		}

		System.out.println("\nPrueba 15: Reportes");
		System.out.println(club.generarReporteVentas("TODOS", fechaActual.minusDays(1), fechaActual.plusDays(1)));
		System.out.println(club.generarReporteGoleadores(torneo));
		System.out.println(club.generarReporteAsistencia(fechaActual.minusDays(1), fechaActual.plusDays(1)));
		System.out.println(club.generarReporteEvolucionElo("TENIS"));
		System.out.println(club.generarReporteVentas("TIENDA", LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 2)));

		System.out.println("\nPrueba 16: Persistencia y autenticacion");
		club.guardarDatos(rutaDatos);
		ClubDeportivoSeneca clubCargado = new ClubDeportivoSeneca();
		clubCargado.cargarDatos(rutaDatos);
		Usuario usuarioAutenticado = clubCargado.autenticarUsuario("socioA1", "clave1");
		System.out.println("Usuario autenticado: " + usuarioAutenticado.getNombre() + " | Equipos cargados: " + clubCargado.getEquipos().size());

		try {
			clubCargado.autenticarUsuario("socioA1", "malaclave");
		} catch (Exception e) {
			System.out.println("Error esperado: " + e.getMessage());
		}
	}
}