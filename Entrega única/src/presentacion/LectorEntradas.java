package presentacion;

import java.io.InputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Lee y valida todo lo que el usuario escribe en una consola. Cada metodo repite la pregunta
 * hasta recibir un dato valido, asi que las consolas reciben siempre valores del tipo y el rango
 * que pidieron y no repiten codigo de validacion. Escribir "cancelar" abandona la operacion.
 */
public class LectorEntradas {

	public static final String PALABRA_CANCELAR = "cancelar";

	private Scanner scanner;
	private PrintStream salida;

	public LectorEntradas() {
		this(System.in, System.out);
	}

	/** Permite leer de otra fuente (por ejemplo un texto fijo en las pruebas). */
	public LectorEntradas(InputStream entrada, PrintStream salida) {
		this.scanner = new Scanner(entrada);
		this.salida = salida;
	}

	public void mostrar(String texto) {
		salida.println(texto);
	}

	private String leerLinea(String mensaje) {
		salida.print(mensaje);
		if (!scanner.hasNextLine()) {
			throw new EntradaCanceladaException("Se cerro la entrada.", true);
		}
		String linea = scanner.nextLine().trim();
		if (linea.equalsIgnoreCase(PALABRA_CANCELAR)) {
			throw new EntradaCanceladaException("Operacion cancelada.", false);
		}
		return linea;
	}

	/** Texto obligatorio: no acepta una respuesta vacia. */
	public String leerTexto(String mensaje) {
		while (true) {
			String texto = leerLinea(mensaje);
			if (!texto.isEmpty()) {
				return texto;
			}
			mostrar("Dato invalido: el campo es obligatorio.");
		}
	}

	/** Texto que puede quedar vacio. */
	public String leerTextoOpcional(String mensaje) {
		return leerLinea(mensaje);
	}

	/** Entero dentro del rango [minimo, maximo]. */
	public int leerEntero(String mensaje, int minimo, int maximo) {
		while (true) {
			String texto = leerLinea(mensaje);
			try {
				int valor = Integer.parseInt(texto);
				if (valor >= minimo && valor <= maximo) {
					return valor;
				}
				mostrar("Dato invalido: debe ser un numero entre " + minimo + " y " + maximo + ".");
			} catch (NumberFormatException e) {
				mostrar("Dato invalido: debe ser un numero entero.");
			}
		}
	}

	/** Entero mayor o igual que el minimo. */
	public int leerEntero(String mensaje, int minimo) {
		return leerEntero(mensaje, minimo, Integer.MAX_VALUE);
	}

	/** Numero decimal mayor o igual que el minimo. */
	public double leerDecimal(String mensaje, double minimo) {
		while (true) {
			String texto = leerLinea(mensaje).replace(',', '.');
			try {
				double valor = Double.parseDouble(texto);
				if (Double.isNaN(valor) || Double.isInfinite(valor)) {
					mostrar("Dato invalido: debe ser un numero.");
				} else if (valor < minimo) {
					mostrar("Dato invalido: debe ser un numero mayor o igual que " + minimo + ".");
				} else {
					return valor;
				}
			} catch (NumberFormatException e) {
				mostrar("Dato invalido: debe ser un numero.");
			}
		}
	}

	/** Fecha con formato AAAA-MM-DD. Rechaza fechas que no existen, como 2026-02-30. */
	public LocalDate leerFecha(String mensaje) {
		while (true) {
			String texto = leerLinea(mensaje);
			try {
				return LocalDate.parse(texto);
			} catch (DateTimeParseException e) {
				mostrar("Dato invalido: use una fecha real con el formato AAAA-MM-DD.");
			}
		}
	}

	/** Fecha igual o posterior a la fecha minima. */
	public LocalDate leerFechaDesde(String mensaje, LocalDate minima) {
		while (true) {
			LocalDate fecha = leerFecha(mensaje);
			if (!fecha.isBefore(minima)) {
				return fecha;
			}
			mostrar("Dato invalido: la fecha no puede ser anterior a " + minima + ".");
		}
	}

	/** Hora con formato HH:MM en 24 horas. */
	public LocalTime leerHora(String mensaje) {
		while (true) {
			String texto = leerLinea(mensaje);
			try {
				return LocalTime.parse(texto);
			} catch (DateTimeParseException e) {
				mostrar("Dato invalido: use una hora con el formato HH:MM (24 horas).");
			}
		}
	}

	/** Pregunta de si o no: acepta s, si, n o no. */
	public boolean leerConfirmacion(String mensaje) {
		while (true) {
			String texto = leerLinea(mensaje + " (s/n): ").toLowerCase();
			if (texto.equals("s") || texto.equals("si")) {
				return true;
			}
			if (texto.equals("n") || texto.equals("no")) {
				return false;
			}
			mostrar("Dato invalido: responda s o n.");
		}
	}

	/** Una de las opciones dadas, sin distinguir mayusculas. Devuelve la opcion tal como se declaro. */
	public String leerOpcion(String mensaje, String... opciones) {
		while (true) {
			String texto = leerLinea(mensaje);
			for (int i = 0; i < opciones.length; i++) {
				if (opciones[i].equalsIgnoreCase(texto)) {
					return opciones[i];
				}
			}
			mostrar("Dato invalido: las opciones son " + String.join(", ", opciones) + ".");
		}
	}

	/** Un valor de una enumeracion, escrito por su nombre (por ejemplo RECREATIVO o COMPETITIVO). */
	public <E extends Enum<E>> E leerEnumeracion(String mensaje, Class<E> tipo) {
		E[] valores = tipo.getEnumConstants();
		String[] nombres = new String[valores.length];
		for (int i = 0; i < valores.length; i++) {
			nombres[i] = valores[i].name();
		}
		String elegido = leerOpcion(mensaje, nombres);
		return Enum.valueOf(tipo, elegido);
	}
}
