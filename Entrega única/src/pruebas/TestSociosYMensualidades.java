package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.YearMonth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import administracion.PagoMensualidad;
import club.ClubDeportivoSeneca;
import usuarios.Administrador;
import usuarios.Socio;

public class TestSociosYMensualidades {

	private ClubDeportivoSeneca club;
	private Administrador admin;
	private Socio socio;
	private YearMonth mes;

	@BeforeEach
	public void setUp() {
		club = new ClubDeportivoSeneca();
		admin = club.getAdministrador();
		socio = admin.registrarSocio("Laura Gomez", "1001", LocalDate.of(2000, 3, 15), "lgomez", "clave123");
		mes = YearMonth.of(2026, 11);
	}

	private void verificarRechazo(String motivo, Runnable accion) {
		int socios = club.getSocios().size();
		int pagos = club.getPagosMensualidades().size();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, accion::run);
		assertTrue(e.getMessage().toLowerCase().contains(motivo), e.getMessage());
		assertEquals(socios, club.getSocios().size());
		assertEquals(pagos, club.getPagosMensualidades().size());
	}

	@Test
	public void testA02_registroExitoso() {
		Socio nuevo = admin.registrarSocio("Pedro Rios", "2002", LocalDate.of(1998, 7, 1), "prios", "pw");

		assertEquals(nuevo, club.buscarSocio("prios"));
		assertEquals("2002", nuevo.getDocumento());
		assertTrue(nuevo.isActivo());
		assertEquals(nuevo, club.autenticarUsuario("prios", "pw"));
	}

	@Test
	public void testA02_loginODocumentoExistenteRechazado() {
		verificarRechazo("login", () -> admin.registrarSocio("Otro", "3003", LocalDate.of(1990, 1, 1), "lgomez", "pw"));
		verificarRechazo("documento", () -> admin.registrarSocio("Otro", "1001", LocalDate.of(1990, 1, 1), "otro", "pw"));
	}

	@Test
	public void testA02_datosFaltantesOFechaFuturaRechazado() {
		verificarRechazo("nombre", () -> admin.registrarSocio(" ", "3003", LocalDate.of(1990, 1, 1), "otro", "pw"));
		verificarRechazo("fecha", () -> admin.registrarSocio("Otro", "3003", null, "otro", "pw"));
		verificarRechazo("futura", () -> admin.registrarSocio("Otro", "3003", LocalDate.now().plusDays(1), "otro", "pw"));
	}

	@Test
	public void testA16_pagoExitoso() {
		PagoMensualidad pago = admin.registrarPagoMensualidad("lgomez", mes, 120000);

		assertEquals(socio, pago.getSocio());
		assertEquals(mes, pago.getMes());
		assertEquals(120000, pago.getValor());
		assertEquals(pago, club.buscarPagoMensualidad(socio, mes));
		assertEquals(1, club.getPagosMensualidades().size());
	}

	@Test
	public void testA16_mesYaPagadoRechazado() {
		admin.registrarPagoMensualidad("lgomez", mes, 120000);

		verificarRechazo("duplicado", () -> admin.registrarPagoMensualidad("lgomez", mes, 120000));
	}

	@Test
	public void testA16_valorInvalidoOSocioInexistenteRechazado() {
		verificarRechazo("valor", () -> admin.registrarPagoMensualidad("lgomez", mes, 0));
		verificarRechazo("no existe", () -> admin.registrarPagoMensualidad("noexiste", mes, 120000));
	}
}
