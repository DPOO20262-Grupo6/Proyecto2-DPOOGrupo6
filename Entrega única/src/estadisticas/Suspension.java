package estadisticas;

import java.io.Serializable;
import deportesDeConjunto.PartidoDeEquipo;
import enumeraciones.MotivoSuspension;

public class Suspension implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private MotivoSuspension motivo;
	private boolean cumplida;
	private PartidoDeEquipo partidoCumplimiento;

	public Suspension(MotivoSuspension motivo) {
		this.motivo = motivo;
		this.cumplida = false;
	}

	public void cumplir() {
		this.cumplida = true;
	}

	public boolean seCumpleEn(PartidoDeEquipo partido) {
		if (!this.cumplida && partido != null) {
			this.cumplida = true;
			this.partidoCumplimiento = partido;
			return true;
		}
		if (partido != null && partido.equals(this.partidoCumplimiento)) {
			return true;
		}
		return false;
	}

	public MotivoSuspension getMotivo() { return motivo; }
	public void setMotivo(MotivoSuspension motivo) { this.motivo = motivo; }
	public boolean isCumplida() { return cumplida; }
	public void setCumplida(boolean cumplida) { this.cumplida = cumplida; }
	public PartidoDeEquipo getPartidoCumplimiento() { return partidoCumplimiento; }
	public void setPartidoCumplimiento(PartidoDeEquipo partidoCumplimiento) { this.partidoCumplimiento = partidoCumplimiento; }
}