package Torneos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import deportesDeConjunto.Equipo;
import deportesDeConjunto.PartidoTorneo;
import deportesDeConjunto.ResultadoPartidoEquipo;
import enumeraciones.EstadoTorneo;
import enumeraciones.TipoCupoTorneo;
import enumeraciones.TipoTorneo;

public class TorneoEquipo extends Torneo {


	private List<Equipo> equipos;
	private List<PartidoTorneo> partidos;
	private Map<Equipo, TipoCupoTorneo> tipoCupoPorEquipo;

	public TorneoEquipo(String id, String nombre, String deporte, String categoria, int cuposTotales, TipoTorneo tipo, LocalDate fechaInicio, double tarifaInscripcion, double valorBono) throws Exception {
		super(id, nombre, deporte, categoria, cuposTotales, tipo, fechaInicio, tarifaInscripcion, valorBono);
		this.equipos = new ArrayList<Equipo>();
		this.partidos = new ArrayList<PartidoTorneo>();
		this.tipoCupoPorEquipo = new HashMap<Equipo, TipoCupoTorneo>();
	}

	// ---------------- Inscripcion (Integrante 2) ----------------

	public boolean inscribir(Equipo equipo) {
		try {
			return inscribir(equipo, false);
		} catch (RuntimeException e) {
			return false;
		}
	}
		
		
	public boolean inscribir(Equipo equipo, boolean tieneRanking) {	
		if (equipo == null) {
			throw new IllegalArgumentException("El equipo no puede estar vacio");
		}
		if (getEstado() != EstadoTorneo.EN_CURSO) {
			throw new IllegalStateException("El torneo no esta abierto para inscripciones.");
		}
		if (haIniciado()) {
			throw new IllegalStateException("El torneo ya ha iniciado.");
		}
		if(estaInscrito(equipo)) {
			throw new IllegalArgumentException("El equipo ya esta inscrito.");
		}
		if(!getDeporte().equalsIgnoreCase(equipo.getDeporte())) {
			throw new IllegalArgumentException("El equipo no pertenece al deporte del torneo.");
		}
		
		TipoCupoTorneo asignado;
		if (tieneRanking && cuposPreferencialesDisponibles() > 0) {
			asignado = TipoCupoTorneo.PREFERENCIAL;
			cuposPreferencialesOcupados++;
		} else if (cuposRegularesDisponibles() > 0) {
			asignado = TipoCupoTorneo.REGULAR;
		} else {
			throw new IllegalStateException("No hay cupos disponibles para este equipo.");
		}
		
		equipos.add(equipo);
		tipoCupoPorEquipo.put(equipo,  asignado);
		return true;
	}

	public boolean retirar(Equipo equipo) {
		if (equipo == null || haIniciado() || !estaInscrito(equipo)) {
			return false;
		}
		TipoCupoTorneo tipoAsignado = tipoCupoPorEquipo.remove(equipo);
		if (tipoAsignado == TipoCupoTorneo.PREFERENCIAL) {
			cuposPreferencialesOcupados--;
		}
		return equipos.remove(equipo);
	}

	public boolean estaInscrito(Equipo equipo) {
		return equipo != null && equipos.contains(equipo);
	}

	public TipoCupoTorneo getTipoCupo(Equipo equipo) {
		return tipoCupoPorEquipo.get(equipo);
	}

	public PartidoTorneo programarPartido(Equipo local, Equipo visitante, LocalDate fecha) {
		if (getEstado() != EstadoTorneo.EN_CURSO) {
			throw new IllegalStateException("No se pueden programar partidos en un torneo finalizado.");
		}
		if (!estaInscrito(local) || !estaInscrito(visitante)) {
			throw new IllegalArgumentException("Ambos equipos deben estar inscritos en el torneo.");
		}
		if (fecha == null || fecha.isBefore(getFechaInicio())) {
			throw new IllegalArgumentException("El partido no puede ser antes del inicio del torneo.");
		}
		PartidoTorneo partido = new PartidoTorneo(fecha, local, visitante, this);
		partidos.add(partido);
		return partido;
	}

	public void registrarResultado(PartidoTorneo partido, ResultadoPartidoEquipo resultado) {
		if (getEstado() != EstadoTorneo.EN_CURSO) {
			throw new IllegalStateException("El torneo ya esta finalizado.");
		}
		if (partido == null || !partidos.contains(partido)) {
			throw new IllegalArgumentException("El partido no pertenece a este torneo.");
		}
		if (resultado == null) {
			throw new IllegalArgumentException("El resultado no puede ser nulo.");
		}
		partido.registrarResultado(resultado);
	}


	public List<Equipo> getEquiposOrdenados() {
		List<Equipo> ordenados = new ArrayList<Equipo>(equipos);
		for (int i = 0; i < ordenados.size() - 1; i++) {
			for (int j = i + 1; j < ordenados.size(); j++) {
				Equipo e1 = ordenados.get(i);
				Equipo e2 = ordenados.get(j);
				if (e2.calcularPuntosLiga(partidos) > e1.calcularPuntosLiga(partidos)) {
					ordenados.set(i, e2);
					ordenados.set(j, e1);
				}
			}
		}
		return ordenados;
	}

	@Override
	public List<String> getTablaPosiciones() {
		List<String> tabla = new ArrayList<String>();
		List<Equipo> ordenados = getEquiposOrdenados();
		for (int i = 0; i < ordenados.size(); i++) {
			Equipo equipo = ordenados.get(i);
			tabla.add((i + 1) + ". " + equipo.getNombre() + " - " + equipo.calcularPuntosLiga(partidos) + " pts");
		}
		return tabla;
	}

	@Override
	public Equipo getGanador() {
		List<Equipo> ordenados = getEquiposOrdenados();
		if (ordenados.isEmpty() || !hayResultados()) {
			return null;
		}
		return ordenados.get(0);
	}

	@Override
	public String getNombreGanador() {
		Equipo ganador = getGanador();
		if (ganador == null) {
			return null;
		}
		return ganador.getNombre();
	}

	private boolean hayResultados() {
		for (int i = 0; i < partidos.size(); i++) {
			if (partidos.get(i).getResultado() != null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int getNumeroInscritos() {
		return equipos.size();
	}

	@Override
	protected List<String> getNombresParticipantes() {
		List<String> nombres = new ArrayList<String>();
		for (int i = 0; i < equipos.size(); i++) {
			nombres.add(equipos.get(i).getNombre());
		}
		return nombres;
	}

	@Override
	protected List<String> getDescripcionEncuentros() {
		List<String> encuentros = new ArrayList<String>();
		for (int i = 0; i < partidos.size(); i++) {
			encuentros.add(partidos.get(i).toString());
		}
		return encuentros;
	}

	public List<Equipo> getEquipos() { return equipos; }
	public List<PartidoTorneo> getPartidos() { return partidos; }
}
