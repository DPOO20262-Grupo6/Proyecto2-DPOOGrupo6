package presentacion;

import usuarios.Socio;

/**
 * Consola de los socios del club. Cubre las historias de usuario del socio: academias y reservas,
 * partidos de practica y ranking, entrenamientos, su equipo y las competencias, los torneos
 * internos (incluidos estudiantes y personas externas) y las compras con puntos y bonos.
 * El inicio de sesion, la carga y el guardado los hace ConsolaBase.
 */
public class ConsolaSocio extends ConsolaBase<Socio> {

	public static void main(String[] args) {
		new ConsolaSocio().ejecutar();
	}

	@Override
	protected Class<Socio> getTipoUsuario() {
		return Socio.class;
	}

	@Override
	protected String getNombreConsola() {
		return "Socio";
	}

	@Override
	protected Menu construirMenuPrincipal() {
		AccionesDeportivasSocio deportivas = new AccionesDeportivasSocio(club, usuario, lector);
		AccionesTorneosSocio torneos = new AccionesTorneosSocio(club, usuario, lector);
		AccionesComprasSocio compras = new AccionesComprasSocio(club, usuario, lector);

		Menu principal = new Menu("Consola del Socio - " + usuario.getNombre(), "Guardar y salir");
		principal.agregarSubmenu("Academias y reservas", deportivas.construirMenuAcademiasYReservas(), lector);
		principal.agregarSubmenu("Partidos de practica y ranking", deportivas.construirMenuPartidosYRanking(), lector);
		principal.agregarSubmenu("Entrenamientos", deportivas.construirMenuEntrenamientos(), lector);
		principal.agregarSubmenu("Mi equipo y competencias", deportivas.construirMenuEquiposYCompetencias(), lector);
		principal.agregarSubmenu("Torneos", torneos.construirMenu(), lector);
		principal.agregarSubmenu("Compras y puntos", compras.construirMenu(), lector);
		principal.agregarOpcion("Mis datos", this::consultarMisDatos);
		return principal;
	}

	private void consultarMisDatos() {
		lector.mostrar("--- Mis datos ---");
		lector.mostrar("Nombre: " + usuario.getNombre());
		lector.mostrar("Login: " + usuario.getLogin());
		lector.mostrar("Documento: " + (usuario.getDocumento() == null ? "(sin registrar)" : usuario.getDocumento()));
		lector.mostrar("Puntos de fidelidad: " + usuario.getPuntosDeFidelidad());
		lector.mostrar("Bonos ganados: " + usuario.getBonos().size());
	}
}
