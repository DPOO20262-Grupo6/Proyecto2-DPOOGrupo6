package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import administracion.SolicitudCambioTurnoEmpleado;
import administracion.TurnoEmpleado;
import club.ClubDeportivoSeneca;
import enumeraciones.EstadoSolicitud;
import usuarios.Administrador;
import usuarios.EmpleadoDelClub;
import usuarios.Entrenador;
import usuarios.Fisioterapeuta;

public class TestTurnos {

	private static final LocalDate LUNES = LocalDate.of(2026, 11, 16);
	private static final LocalTime SEIS = LocalTime.of(6, 0);
	private static final LocalTime CATORCE = LocalTime.of(14, 0);

	private ClubDeportivoSeneca club;
	private Administrador admin;
	private Entrenador e1;
	private Entrenador e2;
	private Entrenador e3;
	private Entrenador e4;
	private Entrenador e5;
	private Fisioterapeuta f1;
	private Fisioterapeuta f2;

	@BeforeEach
	public void setUp() {
		club = new ClubDeportivoSeneca();
		admin = club.getAdministrador();
		e1 = registrar(new Entrenador("E1", "e1", "1"));
		e2 = registrar(new Entrenador("E2", "e2", "1"));
		e3 = registrar(new Entrenador("E3", "e3", "1"));
		e4 = registrar(new Entrenador("E4", "e4", "1"));
		e5 = registrar(new Entrenador("E5", "e5", "1"));
		f1 = registrar(new Fisioterapeuta("F1", "f1", "1"));
		f2 = registrar(new Fisioterapeuta("F2", "f2", "1"));
	}

	private <T extends EmpleadoDelClub> T registrar(T empleado) {
		club.registrarEmpleado(empleado);
		return empleado;
	}

	private List<TurnoEmpleado> programacionCompleta() {
		List<TurnoEmpleado> programacion = new ArrayList<TurnoEmpleado>();
		programacion.add(new TurnoEmpleado(e1, LUNES, SEIS, 8));
		programacion.add(new TurnoEmpleado(e2, LUNES, SEIS, 8));
		programacion.add(new TurnoEmpleado(f1, LUNES, SEIS, 8));
		programacion.add(new TurnoEmpleado(e3, LUNES, CATORCE, 8));
		programacion.add(new TurnoEmpleado(e4, LUNES, CATORCE, 8));
		programacion.add(new TurnoEmpleado(f2, LUNES, CATORCE, 8));
		return programacion;
	}

	private TurnoEmpleado publicarYObtenerTurnoDe(EmpleadoDelClub empleado) {
		club.publicarProgramacion(programacionCompleta());
		for (int i = 0; i < club.getTurnos().size(); i++) {
			if (club.getTurnos().get(i).getEmpleado() == empleado) {
				return club.getTurnos().get(i);
			}
		}
		return null;
	}

	private void verificarSolicitudRechazada(String motivo, Executable accion) {
		int solicitudes = club.getSolicitudesCambio().size();
		RuntimeException e = assertThrows(RuntimeException.class, accion);
		assertTrue(e.getMessage().toLowerCase().contains(motivo), e.getMessage());
		assertEquals(solicitudes, club.getSolicitudesCambio().size());
	}

	@Test
	public void testA11_programacionConCoberturaPublicada() {
		club.publicarProgramacion(programacionCompleta());

		assertEquals(6, club.getTurnos().size());
	}

	@Test
	public void testA11_bloqueSinCoberturaRechazado() {
		List<TurnoEmpleado> incompleta = programacionCompleta();
		incompleta.remove(4);

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> club.publicarProgramacion(incompleta));

		assertTrue(e.getMessage().contains("2026-11-16 14:00"), e.getMessage());
		assertTrue(club.getTurnos().isEmpty());
	}

	@Test
	public void testA11_turnoTraslapadoRechazado() {
		admin.asignarTurno(e1, LUNES, SEIS, 8);

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> admin.asignarTurno(e1, LUNES, LocalTime.of(10, 0), 2));

		assertTrue(e.getMessage().contains("traslape"));
		assertEquals(1, club.getTurnos().size());
	}

	@Test
	public void testA12_solicitudAprobadaActualizaElTurno() {
		club.publicarProgramacion(programacionCompleta());
		TurnoEmpleado extra = admin.asignarTurno(e5, LUNES, SEIS, 8);
		SolicitudCambioTurnoEmpleado solicitud = club.solicitarCambioTurno(e5, extra, LUNES, LocalTime.of(10, 0));

		admin.evaluarSolicitudCambio(solicitud);

		assertEquals(EstadoSolicitud.APROBADA, solicitud.getEstado());
		assertEquals(LocalTime.of(10, 0), extra.getHora());
	}

	@Test
	public void testA12_cambioSinCoberturaQuedaRechazadoConMotivo() {
		TurnoEmpleado turno = publicarYObtenerTurnoDe(e1);
		SolicitudCambioTurnoEmpleado solicitud = club.solicitarCambioTurno(e1, turno, LUNES.plusDays(1), SEIS);

		assertThrows(IllegalStateException.class, () -> admin.evaluarSolicitudCambio(solicitud));

		assertEquals(EstadoSolicitud.RECHAZADA, solicitud.getEstado());
		assertTrue(solicitud.getMotivoRechazo().contains("2026-11-16 06:00"));
		assertEquals(LUNES, turno.getFecha());
		assertEquals(SEIS, turno.getHora());
	}

	@Test
	public void testA12_solicitudYaResueltaRechazada() {
		club.publicarProgramacion(programacionCompleta());
		TurnoEmpleado extra = admin.asignarTurno(e5, LUNES, SEIS, 8);
		SolicitudCambioTurnoEmpleado solicitud = club.solicitarCambioTurno(e5, extra, LUNES, LocalTime.of(10, 0));
		admin.evaluarSolicitudCambio(solicitud);

		IllegalStateException e = assertThrows(IllegalStateException.class, () -> admin.evaluarSolicitudCambio(solicitud));

		assertTrue(e.getMessage().contains("ya fue resuelta"));
		assertEquals(LocalTime.of(10, 0), extra.getHora());
	}

	@Test
	public void testE02_consultarMisTurnos() {
		club.publicarProgramacion(programacionCompleta());

		assertTrue(club.consultarTurnos(e1).contains("lunes 2026-11-16 06:00-14:00"));
	}

	@Test
	public void testE02_sinTurnos() {
		assertTrue(club.consultarTurnos(e5).contains("No tiene turnos programados"));
	}

	@Test
	public void testE03_solicitudQuedaPendienteYTurnoVigente() {
		TurnoEmpleado turno = publicarYObtenerTurnoDe(e1);

		SolicitudCambioTurnoEmpleado solicitud = club.solicitarCambioTurno(e1, turno, LUNES, LocalTime.of(8, 0));

		assertEquals(EstadoSolicitud.PENDIENTE, solicitud.getEstado());
		assertTrue(club.getSolicitudesCambio().contains(solicitud));
		assertEquals(SEIS, turno.getHora());
	}

	@Test
	public void testE03_turnoAjenoRechazado() {
		TurnoEmpleado turno = publicarYObtenerTurnoDe(e1);

		verificarSolicitudRechazada("no pertenece", () -> club.solicitarCambioTurno(e2, turno, LUNES, LocalTime.of(8, 0)));
	}

	@Test
	public void testE03_horarioFueraDeAtencionOIgualRechazado() {
		TurnoEmpleado turno = publicarYObtenerTurnoDe(e1);

		verificarSolicitudRechazada("fuera del horario", () -> club.solicitarCambioTurno(e1, turno, LUNES, LocalTime.of(5, 0)));
		verificarSolicitudRechazada("fuera del horario", () -> club.solicitarCambioTurno(e1, turno, LUNES, LocalTime.of(15, 0)));
		verificarSolicitudRechazada("igual al actual", () -> club.solicitarCambioTurno(e1, turno, LUNES, SEIS));
	}

	@Test
	public void testE03_turnoConSolicitudPendienteRechazado() {
		TurnoEmpleado turno = publicarYObtenerTurnoDe(e1);
		club.solicitarCambioTurno(e1, turno, LUNES, LocalTime.of(8, 0));

		verificarSolicitudRechazada("pendiente", () -> club.solicitarCambioTurno(e1, turno, LUNES, LocalTime.of(9, 0)));
	}

	@Test
	public void testE04_consultarEstadoDeMisSolicitudes() {
		TurnoEmpleado turnoE1 = publicarYObtenerTurnoDe(e1);
		TurnoEmpleado turnoE2 = turnoDe(e2);
		SolicitudCambioTurnoEmpleado rechazada = club.solicitarCambioTurno(e1, turnoE1, LUNES.plusDays(1), SEIS);
		assertThrows(IllegalStateException.class, () -> admin.evaluarSolicitudCambio(rechazada));
		TurnoEmpleado extra = admin.asignarTurno(e5, LUNES, SEIS, 8);
		admin.evaluarSolicitudCambio(club.solicitarCambioTurno(e5, extra, LUNES, LocalTime.of(10, 0)));
		club.solicitarCambioTurno(e2, turnoE2, LUNES, LocalTime.of(7, 0));

		assertTrue(club.consultarSolicitudes(e1).contains("RECHAZADA (motivo: "));
		assertTrue(club.consultarSolicitudes(e5).contains("APROBADA"));
		assertTrue(club.consultarSolicitudes(e2).contains("PENDIENTE"));
	}

	@Test
	public void testE04_sinSolicitudes() {
		assertTrue(club.consultarSolicitudes(e3).contains("No tiene solicitudes"));
	}

	private TurnoEmpleado turnoDe(EmpleadoDelClub empleado) {
		for (int i = 0; i < club.getTurnos().size(); i++) {
			if (club.getTurnos().get(i).getEmpleado() == empleado) {
				return club.getTurnos().get(i);
			}
		}
		return null;
	}
}
