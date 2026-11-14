package Torneos;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import deportesIndividuales.PartidoDeporteIndividual;
import deportesIndividuales.PartidoPadel;
import deportesIndividuales.PartidoTenis;
import deportesIndividuales.PartidoTenisDeMesa;
import enumeraciones.EstadoTorneo;
import enumeraciones.Modalidad;
import enumeraciones.TipoTorneo;
import estadisticas.EstadisticasPartidoIndividual;
import inscripciones.InscripcionDisciplinaIndividual;
import instalaciones.CanchaDeporteIndividual;

public class TorneoIndividual extends Torneo {

	private static final long serialVersionUID = 1L;

	private Modalidad modalidad;
	private List<InscripcionDisciplinaIndividual> participantes;
	private List<PartidoDeporteIndividual> partidos;

	public TorneoIndividual(String id, String nombre, String deporte, Modalidad modalidad, String categoria, int cuposTotales, TipoTorneo tipo, LocalDate fechaInicio, double tarifaInscripcion, double valorBono) throws Exception {
		super(id, nombre, deporte, categoria, cuposTotales, tipo, fechaInicio, tarifaInscripcion, valorBono);
		if (modalidad == null) {
			throw new IllegalArgumentException("Debe indicar la modalidad del torneo (INDIVIDUAL o DOBLES).");
		}
		this.modalidad = modalidad;
		this.participantes = new ArrayList<InscripcionDisciplinaIndividual>();
		this.partidos = new ArrayList<PartidoDeporteIndividual>();
	}

	// ---------------- Inscripcion (Integrante 2) ----------------

	public boolean inscribir(InscripcionDisciplinaIndividual participante) {
		// Version basica: el Integrante 2 completa las reglas de cupos preferenciales y pagos.
		if (participante == null || getEstado() != EstadoTorneo.EN_CURSO || haIniciado()) {
			return false;
		}
		if (estaInscrito(participante) || cuposDisponibles() <= 0) {
			return false;
		}
		if (!participante.getDisciplina().equalsIgnoreCase(getDeporte())) {
			return false;
		}
		participantes.add(participante);
		return true;
	}

	public boolean retirar(InscripcionDisciplinaIndividual participante) {
		if (participante == null || haIniciado()) {
			return false;
		}
		return participantes.remove(participante);
	}

	public boolean estaInscrito(InscripcionDisciplinaIndividual participante) {
		return participante != null && participantes.contains(participante);
	}

	// ---------------- Partidos (HU-A06) ----------------

	public PartidoDeporteIndividual programarPartido(List<InscripcionDisciplinaIndividual> jugadores, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, CanchaDeporteIndividual cancha) {
		if (getEstado() != EstadoTorneo.EN_CURSO) {
			throw new IllegalStateException("No se pueden programar partidos en un torneo finalizado.");
		}
		if (jugadores == null || jugadores.isEmpty()) {
			throw new IllegalArgumentException("Debe indicar los participantes del partido.");
		}
		for (int i = 0; i < jugadores.size(); i++) {
			if (!estaInscrito(jugadores.get(i))) {
				throw new IllegalArgumentException("Todos los participantes deben estar inscritos en el torneo.");
			}
		}
		if (cancha == null || !cancha.getDisciplina().equalsIgnoreCase(getDeporte())) {
			throw new IllegalArgumentException("La cancha no corresponde al deporte del torneo.");
		}
		if (fecha == null || fecha.isBefore(getFechaInicio())) {
			throw new IllegalArgumentException("El partido no puede ser antes del inicio del torneo.");
		}

		PartidoDeporteIndividual partido = crearPartido(fecha, horaInicio, horaFin, cancha);
		if (jugadores.size() != partido.getCantidadJugadoresRequerida()) {
			throw new IllegalArgumentException("La modalidad " + modalidad + " requiere " + partido.getCantidadJugadoresRequerida() + " jugadores.");
		}
		for (int i = 0; i < jugadores.size(); i++) {
			partido.agregarParticipante(jugadores.get(i));
		}
		partidos.add(partido);
		return partido;
	}

	private PartidoDeporteIndividual crearPartido(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, CanchaDeporteIndividual cancha) {
		String deporte = getDeporte();
		if (deporte.equals("TENIS")) {
			return new PartidoTenis(fecha, horaInicio, horaFin, modalidad, cancha, false);
		} else if (deporte.equals("PADEL")) {
			return new PartidoPadel(fecha, horaInicio, horaFin, modalidad, cancha, false);
		} else if (deporte.equals("TENIS_DE_MESA")) {
			return new PartidoTenisDeMesa(fecha, horaInicio, horaFin, modalidad, cancha, false);
		}
		throw new IllegalArgumentException("Deporte individual no valido: " + deporte);
	}

	/**
	 * Registra las estadisticas en el mismo orden de los participantes del partido
	 * y llama a partido.finalizar(), que actualiza el ELO.
	 */
	public void registrarResultado(PartidoDeporteIndividual partido, List<EstadisticasPartidoIndividual> estadisticas) {
		if (getEstado() != EstadoTorneo.EN_CURSO) {
			throw new IllegalStateException("El torneo ya esta finalizado.");
		}
		if (partido == null || !partidos.contains(partido)) {
			throw new IllegalArgumentException("El partido no pertenece a este torneo.");
		}
		if (!partido.getEstadisticas().isEmpty()) {
			throw new IllegalStateException("El partido ya tiene un resultado registrado.");
		}
		if (estadisticas == null || estadisticas.size() != partido.getParticipantes().size()) {
			throw new IllegalArgumentException("Debe registrar una estadistica por cada participante.");
		}
		for (int i = 0; i < estadisticas.size(); i++) {
			partido.registrarEstadistica(estadisticas.get(i));
		}
		partido.finalizar();
	}

	// ---------------- Posiciones y ganador (HU-A03, HU-A05, HU-A06) ----------------

	public int contarVictorias(InscripcionDisciplinaIndividual participante) {
		int victorias = 0;
		for (int i = 0; i < partidos.size(); i++) {
			PartidoDeporteIndividual partido = partidos.get(i);
			int posicion = partido.getParticipantes().indexOf(participante);
			if (posicion >= 0 && posicion < partido.getEstadisticas().size()
					&& partido.getEstadisticas().get(posicion).esGanador()) {
				victorias = victorias + 1;
			}
		}
		return victorias;
	}

	public List<InscripcionDisciplinaIndividual> getParticipantesOrdenados() {
		List<InscripcionDisciplinaIndividual> ordenados = new ArrayList<InscripcionDisciplinaIndividual>(participantes);
		for (int i = 0; i < ordenados.size() - 1; i++) {
			for (int j = i + 1; j < ordenados.size(); j++) {
				InscripcionDisciplinaIndividual p1 = ordenados.get(i);
				InscripcionDisciplinaIndividual p2 = ordenados.get(j);
				int v1 = contarVictorias(p1);
				int v2 = contarVictorias(p2);
				// Desempate por ranking ELO
				if (v2 > v1 || (v2 == v1 && p2.getRankingElo() > p1.getRankingElo())) {
					ordenados.set(i, p2);
					ordenados.set(j, p1);
				}
			}
		}
		return ordenados;
	}

	@Override
	public List<String> getTablaPosiciones() {
		List<String> tabla = new ArrayList<String>();
		List<InscripcionDisciplinaIndividual> ordenados = getParticipantesOrdenados();
		for (int i = 0; i < ordenados.size(); i++) {
			InscripcionDisciplinaIndividual p = ordenados.get(i);
			tabla.add((i + 1) + ". " + p.getSocio().getNombre() + " - " + contarVictorias(p) + " victorias (ELO " + p.getRankingElo() + ")");
		}
		return tabla;
	}

	@Override
	public InscripcionDisciplinaIndividual getGanador() {
		List<InscripcionDisciplinaIndividual> ordenados = getParticipantesOrdenados();
		if (ordenados.isEmpty() || contarVictorias(ordenados.get(0)) == 0) {
			return null;
		}
		return ordenados.get(0);
	}

	@Override
	public String getNombreGanador() {
		InscripcionDisciplinaIndividual ganador = getGanador();
		if (ganador == null) {
			return null;
		}
		return ganador.getSocio().getNombre();
	}

	@Override
	public int getNumeroInscritos() {
		return participantes.size();
	}

	@Override
	protected List<String> getNombresParticipantes() {
		List<String> nombres = new ArrayList<String>();
		for (int i = 0; i < participantes.size(); i++) {
			nombres.add(participantes.get(i).getSocio().getNombre());
		}
		return nombres;
	}

	@Override
	protected List<String> getDescripcionEncuentros() {
		List<String> encuentros = new ArrayList<String>();
		for (int i = 0; i < partidos.size(); i++) {
			PartidoDeporteIndividual partido = partidos.get(i);
			StringBuilder sb = new StringBuilder();
			for (int j = 0; j < partido.getParticipantes().size(); j++) {
				if (j > 0) {
					sb.append(" vs ");
				}
				sb.append(partido.getParticipantes().get(j).getSocio().getNombre());
			}
			sb.append(" (").append(partido.getFecha()).append(")");
			if (!partido.getEstadisticas().isEmpty()) {
				for (int j = 0; j < partido.getEstadisticas().size(); j++) {
					if (partido.getEstadisticas().get(j).esGanador()) {
						sb.append(" Gano: ").append(partido.getParticipantes().get(j).getSocio().getNombre());
						break;
					}
				}
			} else {
				sb.append(" Pendiente");
			}
			encuentros.add(sb.toString());
		}
		return encuentros;
	}

	public Modalidad getModalidad() { return modalidad; }
	public List<InscripcionDisciplinaIndividual> getParticipantes() { return participantes; }
	public List<PartidoDeporteIndividual> getPartidos() { return partidos; }
}
