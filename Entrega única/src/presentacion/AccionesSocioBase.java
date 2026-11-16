package presentacion;

import java.util.List;
import java.util.function.Function;

import club.ClubDeportivoSeneca;
import usuarios.Socio;

/**
 * Base de los grupos de acciones de la consola del socio. Cada subclase agrupa las acciones de un
 * area (deportes, torneos, compras) y arma su propio menu; asi la consola principal no crece.
 */
public abstract class AccionesSocioBase {

	protected final ClubDeportivoSeneca club;
	protected final Socio socio;
	protected final LectorEntradas lector;

	protected AccionesSocioBase(ClubDeportivoSeneca club, Socio socio, LectorEntradas lector) {
		this.club = club;
		this.socio = socio;
		this.lector = lector;
	}

	/** Muestra una lista numerada y devuelve el elemento que el usuario elige. */
	protected <T> T elegir(String pregunta, List<T> elementos, Function<T, String> describir) {
		for (int i = 0; i < elementos.size(); i++) {
			lector.mostrar((i + 1) + ". " + describir.apply(elementos.get(i)));
		}
		int eleccion = lector.leerEntero(pregunta, 1, elementos.size());
		return elementos.get(eleccion - 1);
	}

	/** Muestra una lista numerada sin pedir una eleccion. */
	protected <T> void listar(List<T> elementos, Function<T, String> describir) {
		for (int i = 0; i < elementos.size(); i++) {
			lector.mostrar((i + 1) + ". " + describir.apply(elementos.get(i)));
		}
	}
}
