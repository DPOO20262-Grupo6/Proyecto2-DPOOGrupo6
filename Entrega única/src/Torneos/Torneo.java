package Torneos;


import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import articulosYVentas.Bono;
import deportesDeConjunto.Equipo;
import enumeraciones.EstadoTorneo;
import enumeraciones.TipoTorneo;
import inscripciones.InscripcionDisciplinaIndividual;
import inscripciones.InscripcionEquipo;
import inscripciones.InscripcionTorneo;
import usuarios.Socio;

public abstract class Torneo implements Serializable {
	
	private static final long serialVersionUID = 1L;


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
	private boolean bonoOtorgado;

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
		if (cuposPreferencialesLiberados) {
			return 0;
		}
		return Math.max(0, cuposPreferenciales - cuposPreferencialesOcupados);
	}

	public int cuposRegularesDisponibles() {
		int regularesTotal;
		if (cuposPreferencialesLiberados) {
			regularesTotal = cuposTotales - cuposPreferencialesOcupados;
		} else {
			regularesTotal = cuposTotales - cuposPreferenciales;
		}
		int ocupadosRegulares = getNumeroInscritos() - cuposPreferencialesOcupados;
				
		return Math.max(0, regularesTotal - ocupadosRegulares);
	}
	
	public void liberarCuposPreferenciales() {
		this.cuposPreferencialesLiberados = true;
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
			entregarBonos();
		} else {
			double bolsa = getTotalRecaudado();
			String beneficiario = getNombreBeneficiarioPremioEfectivo();
			if (beneficiario == null) {
				premioEntregado = "Premio en efectivo de $" + bolsa + " sin entregar: el ganador " + nombreGanador
						+ " no recibe premio en efectivo y no hay otro participante que pueda recibirlo.";
			} else if (beneficiario.equals(nombreGanador)) {
				premioEntregado = "Premio en efectivo de $" + bolsa + " para " + nombreGanador;
			} else {
				premioEntregado = "Premio en efectivo de $" + bolsa + " para " + beneficiario
						+ " (el ganador " + nombreGanador + " no recibe premio en efectivo)";
			}
		}
	}

	/** Inscritos que pagaron la tarifa. Los empleados participan gratis y no cuentan. */
	public int getNumeroInscritosQuePagan() {
		return getNumeroInscritos();
	}

	/** Total recaudado por inscripciones: es la bolsa del premio en efectivo de un torneo competitivo. */
	public double getTotalRecaudado() {
		return tarifaInscripcion * getNumeroInscritosQuePagan();
	}

	/**
	 * Nombre de quien recibe el premio en efectivo. Normalmente es el ganador; si el ganador
	 * no puede recibirlo (es empleado), es el mejor ubicado que si pueda, o null si no hay ninguno.
	 */
	public String getNombreBeneficiarioPremioEfectivo() {
		return getNombreGanador();
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
	
	/**
	 * Entrega el bono de un torneo recreativo a quienes ganaron. Se ejecuta una sola vez, al finalizar.
	 * El valor del bono que define el administrador es un valor en pesos que se descuenta de una compra.
	 */
	private void entregarBonos() {
		if (bonoOtorgado || valorBono <= 0) {
			return;
		}
		bonoOtorgado = true;
		List<Object> ganadores = getGanadoresDelBono();
		for (int i = 0; i < ganadores.size(); i++) {
			otorgarBonoGanador(ganadores.get(i));
		}
	}

	/** Ganadores que reciben bono: normalmente el ganador del torneo. Los torneos de parejas agregan a la pareja. */
	protected List<Object> getGanadoresDelBono() {
		List<Object> ganadores = new ArrayList<Object>();
		Object ganador = getGanador();
		if (ganador != null) {
			ganadores.add(ganador);
		}
		return ganadores;
	}

	private Bono nuevoBono(String sufijo) {
		return Bono.porValor("BONO-" + id + "-" + sufijo + "-" + System.nanoTime(), valorBono);
	}

	protected void otorgarBonoGanador (Object ganador) {
		if (ganador instanceof Socio) {
			((Socio) ganador).agregarBono(nuevoBono("S"));
		} else if (ganador instanceof InscripcionDisciplinaIndividual) {
			((InscripcionDisciplinaIndividual) ganador).recibirBono(nuevoBono("J"));
		} else if (ganador instanceof Equipo) {
			Equipo eq = (Equipo) ganador;
			for (InscripcionEquipo ie : eq.getInscripciones()) {
				if (ie.isActiva() && ie.getSocio() != null) {
					ie.getSocio().agregarBono(nuevoBono("E"));
				}
			}
		} else if (ganador instanceof InscripcionTorneo) {
			InscripcionTorneo it = (InscripcionTorneo) ganador;
			if (it.getUsuario() instanceof Socio) {
				((Socio) it.getUsuario()).agregarBono(nuevoBono("A"));
			}
			if (it.getParejaUsuario() instanceof Socio) {
				((Socio) it.getParejaUsuario()).agregarBono(nuevoBono("B"));
			}
		}
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


