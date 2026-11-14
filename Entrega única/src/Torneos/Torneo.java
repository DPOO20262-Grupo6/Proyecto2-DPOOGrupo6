package Torneos;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import enumeraciones.EstadoTorneo;
import enumeraciones.TipoTorneo;

public abstract class Torneo implements Serializable {

	private String id;
	private String nombre;
	private String deporte;
	private String categoria;
	private int cuposTotales;
	private int cuposPreferenciales;
	protected int cuposPreferencialesOcupados;
	private boolean cuposPreferencialesLiberados;
	private TipoTorneo tipo;
	private EstadoTorneo estado;
	private LocalDate fechaInicio;
	private double tarifaInscripcion;
	private double valorBono;
	private String premioEntregado;

	public Torneo(String id, String nombre, String deporte, String categoria, int cuposTotales, TipoTorneo tipo, LocalDate fechaInicio, double tarifaInscripcion, double valorBono) throws Exception {
		if (id == null || id.trim().isEmpty()) {
			throw new Exception("El torneo debe tener un identificador.");
		}
		if (nombre == null || nombre.trim().isEmpty()) {
			throw new Exception("El torneo debe tener un nombre.");
		}
		if (deporte == null || deporte.trim().isEmpty()) {
			throw new Exception("El torneo debe tener un deporte.");
		}
		if (cuposTotales <= 0) {
			throw new Exception("El numero de cupos debe ser mayor que 0.");
		}
		if (tipo == null) {
			throw new Exception("Debe indicar si el torneo es RECREATIVO o COMPETITIVO.");
		}
		if (fechaInicio == null) {
			throw new Exception("Debe indicar la fecha de inicio del torneo.");
		}
		if (tarifaInscripcion < 0 || valorBono < 0) {
			throw new Exception("La tarifa y el bono no pueden ser negativos.");
		}

		this.id = id;
		this.nombre = nombre;
		this.deporte = deporte.toUpperCase();
		this.categoria = categoria;
		this.cuposTotales = cuposTotales;
		this.tipo = tipo;
		this.fechaInicio = fechaInicio;
		this.estado = EstadoTorneo.EN_CURSO;
		this.cuposPreferencialesOcupados = 0;
		this.cuposPreferencialesLiberados = false;
		this.cuposPreferenciales = calcularCuposPreferenciales();

		if (tipo == TipoTorneo.COMPETITIVO) {
			this.tarifaInscripcion = tarifaInscripcion;
			this.valorBono = 0;
		} else {
			this.tarifaInscripcion = 0;
			this.valorBono = valorBono;
		}
	}

	public int calcularCuposPreferenciales() {
		return (int) Math.ceil(cuposTotales * 0.2);
	}

	public int cuposDisponibles() {
		return cuposTotales - getNumeroInscritos();
	}

	public int cuposPreferencialesDisponibles() {
		return Math.max(0, cuposPreferenciales - cuposPreferencialesOcupados);
	}

	public int cuposRegularesDisponibles() {
		return Math.max(0, cuposDisponibles() - cuposPreferencialesDisponibles());
	}

	public boolean haIniciado() {
		return !LocalDate.now().isBefore(fechaInicio);
	}

	public void finalizar() {
		if (estado != EstadoTorneo.EN_CURSO) {
			throw new IllegalStateException("El torneo ya esta finalizado.");
		}
		if (!haIniciado()) {
			throw new IllegalStateException("No se puede finalizar un torneo que no ha iniciado.");
		}
		this.estado = EstadoTorneo.FINALIZADO;
		entregarPremio();
	}

	public void entregarPremio() {
		String nombreGanador = getNombreGanador();
		if (nombreGanador == null) {
			premioEntregado = "Sin ganador: no se entrego premio.";
			return;
		}
		if (tipo == TipoTorneo.RECREATIVO) {
			premioEntregado = "Bono de descuento por $" + valorBono + " para " + nombreGanador;
		} else {
			double bolsa = tarifaInscripcion * getNumeroInscritos();
			premioEntregado = "Premio en efectivo de $" + bolsa + " para " + nombreGanador;
		}
	}

	public EstadoTorneo getEstado() {
		return estado;
	}


	public String consultarHistorial() {
		StringBuilder sb = new StringBuilder();
		sb.append("Torneo ").append(id).append(" - ").append(nombre)
		  .append(" (").append(deporte).append(", ").append(categoria).append(", ").append(tipo).append(")\n");
		sb.append("Estado: ").append(estado).append(" | Inicio: ").append(fechaInicio).append("\n");

		sb.append("Participantes:\n");
		List<String> participantes = getNombresParticipantes();
		if (participantes.isEmpty()) {
			sb.append("  (sin participantes)\n");
		}
		for (int i = 0; i < participantes.size(); i++) {
			sb.append("  - ").append(participantes.get(i)).append("\n");
		}

		sb.append("Encuentros:\n");
		List<String> encuentros = getDescripcionEncuentros();
		if (encuentros.isEmpty()) {
			sb.append("  (sin encuentros)\n");
		}
		for (int i = 0; i < encuentros.size(); i++) {
			sb.append("  - ").append(encuentros.get(i)).append("\n");
		}

		sb.append("Posiciones:\n");
		List<String> tabla = getTablaPosiciones();
		for (int i = 0; i < tabla.size(); i++) {
			sb.append("  ").append(tabla.get(i)).append("\n");
		}

		if (premioEntregado != null) {
			sb.append("Premio: ").append(premioEntregado).append("\n");
		}
		return sb.toString();
	}

	public abstract List<String> getTablaPosiciones();

	public abstract Object getGanador();

	public abstract String getNombreGanador();

	public abstract int getNumeroInscritos();

	protected abstract List<String> getNombresParticipantes();

	protected abstract List<String> getDescripcionEncuentros();


	public String getId() { return id; }
	public String getNombre() { return nombre; }
	public void setNombre(String nombre) { this.nombre = nombre; }
	public String getDeporte() { return deporte; }
	public String getCategoria() { return categoria; }
	public void setCategoria(String categoria) { this.categoria = categoria; }
	public int getCuposTotales() { return cuposTotales; }
	public int getCuposPreferenciales() { return cuposPreferenciales; }
	public int getCuposPreferencialesOcupados() { return cuposPreferencialesOcupados; }
	public boolean isCuposPreferencialesLiberados() { return cuposPreferencialesLiberados; }
	public TipoTorneo getTipo() { return tipo; }
	public LocalDate getFechaInicio() { return fechaInicio; }
	public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
	public double getTarifaInscripcion() { return tarifaInscripcion; }
	public double getValorBono() { return valorBono; }
	public String getPremioEntregado() { return premioEntregado; }

	@Override
	public String toString() {
		return id + " - " + nombre + " (" + deporte + ", " + estado + ")";
	}
}
