package inscripciones;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import enumeraciones.NivelHabilidad;
import usuarios.Socio;
import usuarios.Usuario;

public class InscripcionDisciplinaIndividual implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final double ELO_INICIAL = 1000.0;
	
	private String disciplina;
	private NivelHabilidad nivel;
	private double rankingElo;
	private Socio socio;
	private List<Double> historialElo;

	public InscripcionDisciplinaIndividual(Socio socio, String disciplina, NivelHabilidad nivel) {
		this.socio = socio;
		this.disciplina = disciplina.toUpperCase();
		this.nivel = nivel;
		this.historialElo = new ArrayList<Double>();
		this.rankingElo = ELO_INICIAL;
		this.historialElo.add(this.rankingElo);
	}

	public boolean esNivelCompatible(InscripcionDisciplinaIndividual otra) {
		if (otra == null) {
			return false;
		}
		int diferencia = Math.abs(this.nivel.ordinal() - otra.nivel.ordinal());
		return diferencia <= 1;
	}

	public void actualizarRankingElo(double rankingRival, boolean gano) {
		double k = 32.0;
		double esperado = 1.0 / (1.0 + Math.pow(10.0, (rankingRival - this.rankingElo) / 400.0));
		double resultadoReal = 0.0;
		
		if (gano) {
			resultadoReal = 1.0;
		}
		
		double nuevoElo = this.rankingElo + k * (resultadoReal - esperado);
		this.rankingElo = Math.round(nuevoElo * 100.0) / 100.0;
		this.historialElo.add(this.rankingElo);
	}

	public String getDisciplina() {
		return disciplina;
	}

	public void setDisciplina(String disciplina) {
		this.disciplina = disciplina;
	}

	public NivelHabilidad getNivel() {
		return nivel;
	}

	public void setNivel(NivelHabilidad nivel) {
		this.nivel = nivel;
	}

	public double getRankingElo() {
		return rankingElo;
	}

	public void setRankingElo(double rankingElo) {
		this.rankingElo = rankingElo;
	}

	public Socio getSocio() {
		return socio;
	}

	public void setSocio(Socio socio) {
		this.socio = socio;
	}

	public List<Double> getHistorialElo() {
		return historialElo;
	}

	public void setHistorialElo(List<Double> historialElo) {
		this.historialElo = historialElo;
	}

	// ---- Participacion en torneos: la inscripcion de un empleado redefine estos metodos ----

	/** Indica si esta inscripcion es la del usuario dado. */
	public boolean correspondeA(Usuario usuario) {
		return usuario != null && usuario.equals(socio);
	}

	/** Un socio paga la tarifa de inscripcion de los torneos competitivos. */
	public boolean pagaInscripcionTorneo() {
		return true;
	}

	/** Un socio puede recibir el premio en efectivo de un torneo competitivo. */
	public boolean recibePremioEnEfectivo() {
		return true;
	}
}
