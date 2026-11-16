package pruebas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import enumeraciones.TipoTorneo;
import presentacion.EntradaCanceladaException;
import presentacion.LectorEntradas;
import presentacion.Menu;

/**
 * Pruebas de la validacion de entradas y del menu que comparten las consolas.
 * La entrada del usuario se simula con un texto fijo: cada linea es una respuesta.
 */
public class LectorEntradasTest {

	private ByteArrayOutputStream salida;

	private LectorEntradas lectorCon(String... lineas) {
		salida = new ByteArrayOutputStream();
		String entrada = lineas.length == 0 ? "" : String.join("\n", lineas) + "\n";
		return new LectorEntradas(new ByteArrayInputStream(entrada.getBytes()), new PrintStream(salida));
	}

	private String loMostrado() {
		return salida.toString();
	}

	// ---------------- Validacion de entradas ----------------

	@Test
	public void testLeerTextoRepiteHastaRecibirUnDatoNoVacio() {
		LectorEntradas lector = lectorCon("", "   ", "  Copa Tenis  ");

		assertEquals("Copa Tenis", lector.leerTexto("Nombre: "));
		assertTrue(loMostrado().contains("obligatorio"));
	}

	@Test
	public void testLeerTextoOpcionalAceptaVacio() {
		assertEquals("", lectorCon("").leerTextoOpcional("Filtro: "));
	}

	@Test
	public void testLeerEnteroRechazaTextoDecimalesYValoresFueraDeRango() {
		LectorEntradas lector = lectorCon("abc", "2.5", "9", "-1", "2");

		assertEquals(2, lector.leerEntero("Opcion: ", 0, 3));
		assertTrue(loMostrado().contains("numero entero"));
		assertTrue(loMostrado().contains("entre 0 y 3"));
	}

	@Test
	public void testLeerEnteroAceptaLosLimitesDelRango() {
		assertEquals(0, lectorCon("0").leerEntero("Opcion: ", 0, 3));
		assertEquals(3, lectorCon("3").leerEntero("Opcion: ", 0, 3));
	}

	@Test
	public void testLeerDecimalRechazaTextoYValoresMenoresAlMinimo() {
		LectorEntradas lector = lectorCon("mucho", "-5", "0", "12,5");

		assertEquals(12.5, lector.leerDecimal("Tarifa: ", 1));
		assertTrue(loMostrado().contains("mayor o igual que"));
	}

	@Test
	public void testLeerFechaRechazaFormatosYFechasInexistentes() {
		LectorEntradas lector = lectorCon("15/11/2026", "2026-02-30", "manana", "2026-11-20");

		assertEquals(LocalDate.of(2026, 11, 20), lector.leerFecha("Fecha: "));
		assertTrue(loMostrado().contains("AAAA-MM-DD"));
	}

	@Test
	public void testLeerFechaDesdeRechazaFechasAnterioresALaMinima() {
		LocalDate minima = LocalDate.of(2026, 11, 15);
		LectorEntradas lector = lectorCon("2026-11-14", "2026-11-15");

		assertEquals(minima, lector.leerFechaDesde("Fecha: ", minima));
		assertTrue(loMostrado().contains("anterior"));
	}

	@Test
	public void testLeerHoraRechazaHorasInvalidas() {
		LectorEntradas lector = lectorCon("25:00", "8am", "08:30");

		assertEquals(LocalTime.of(8, 30), lector.leerHora("Hora: "));
		assertTrue(loMostrado().contains("HH:MM"));
	}

	@Test
	public void testLeerConfirmacionSoloAceptaSiONo() {
		assertTrue(lectorCon("tal vez", "S").leerConfirmacion("Confirma?"));
		assertTrue(lectorCon("si").leerConfirmacion("Confirma?"));
		assertFalse(lectorCon("N").leerConfirmacion("Confirma?"));
		assertFalse(lectorCon("no").leerConfirmacion("Confirma?"));
	}

	@Test
	public void testLeerOpcionNoDistingueMayusculas() {
		LectorEntradas lector = lectorCon("ajedrez", "padel");

		assertEquals("PADEL", lector.leerOpcion("Disciplina: ", "TENIS", "PADEL", "TENIS_DE_MESA"));
		assertTrue(loMostrado().contains("TENIS, PADEL, TENIS_DE_MESA"));
	}

	@Test
	public void testLeerEnumeracionDevuelveElValor() {
		LectorEntradas lector = lectorCon("amistoso", "competitivo");

		assertEquals(TipoTorneo.COMPETITIVO, lector.leerEnumeracion("Tipo: ", TipoTorneo.class));
	}

	@Test
	public void testEscribirCancelarAbandonaLaOperacion() {
		EntradaCanceladaException e = assertThrows(EntradaCanceladaException.class,
				() -> lectorCon("Cancelar").leerEntero("Cupos: ", 1));

		assertFalse(e.esFinDeEntrada());
	}

	@Test
	public void testSinMasEntradaSeInformaElFin() {
		EntradaCanceladaException e = assertThrows(EntradaCanceladaException.class,
				() -> lectorCon().leerTexto("Nombre: "));

		assertTrue(e.esFinDeEntrada());
	}

	// ---------------- Menu ----------------

	@Test
	public void testMenuEjecutaLaOpcionElegidaHastaSalir() {
		List<String> ejecutadas = new ArrayList<String>();
		Menu menu = new Menu("Prueba", "Salir");
		menu.agregarOpcion("Primera", () -> ejecutadas.add("primera"));
		menu.agregarOpcion("Segunda", () -> ejecutadas.add("segunda"));

		menu.ejecutar(lectorCon("2", "1", "2", "0"));

		assertEquals(3, ejecutadas.size());
		assertEquals("segunda", ejecutadas.get(0));
		assertEquals("primera", ejecutadas.get(1));
		assertTrue(loMostrado().contains("1. Primera"));
		assertTrue(loMostrado().contains("0. Salir"));
	}

	@Test
	public void testMenuRechazaOpcionesQueNoExisten() {
		List<String> ejecutadas = new ArrayList<String>();
		Menu menu = new Menu("Prueba", "Salir");
		menu.agregarOpcion("Unica", () -> ejecutadas.add("unica"));

		menu.ejecutar(lectorCon("7", "x", "1", "0"));

		assertEquals(1, ejecutadas.size());
		assertTrue(loMostrado().contains("entre 0 y 1"));
	}

	@Test
	public void testUnRechazoDeLaLogicaMuestraElMotivoYElMenuContinua() {
		List<String> ejecutadas = new ArrayList<String>();
		Menu menu = new Menu("Prueba", "Salir");
		menu.agregarOpcion("Falla", () -> { throw new IllegalStateException("Tiene un turno ese dia."); });
		menu.agregarOpcion("Funciona", () -> ejecutadas.add("funciona"));

		menu.ejecutar(lectorCon("1", "2", "0"));

		assertTrue(loMostrado().contains("No se pudo completar la operacion: Tiene un turno ese dia."));
		assertEquals(1, ejecutadas.size());
	}

	@Test
	public void testCancelarUnaOperacionRegresaAlMenu() {
		List<Integer> leidos = new ArrayList<Integer>();
		LectorEntradas lector = lectorCon("1", "cancelar", "1", "8", "0");
		Menu menu = new Menu("Prueba", "Salir");
		menu.agregarOpcion("Pedir cupos", () -> leidos.add(lector.leerEntero("Cupos: ", 1)));

		menu.ejecutar(lector);

		assertEquals(1, leidos.size());
		assertEquals(8, leidos.get(0));
		assertTrue(loMostrado().contains("Operacion cancelada"));
	}

	@Test
	public void testSubmenuRegresaAlMenuPrincipal() {
		List<String> ejecutadas = new ArrayList<String>();
		LectorEntradas lector = lectorCon("1", "1", "0", "2", "0");
		Menu submenu = new Menu("Torneos", "Volver");
		submenu.agregarOpcion("Dentro", () -> ejecutadas.add("dentro"));
		Menu principal = new Menu("Principal", "Salir");
		principal.agregarSubmenu("Torneos", submenu, lector);
		principal.agregarOpcion("Fuera", () -> ejecutadas.add("fuera"));

		principal.ejecutar(lector);

		assertEquals(2, ejecutadas.size());
		assertEquals("dentro", ejecutadas.get(0));
		assertEquals("fuera", ejecutadas.get(1));
	}
}
