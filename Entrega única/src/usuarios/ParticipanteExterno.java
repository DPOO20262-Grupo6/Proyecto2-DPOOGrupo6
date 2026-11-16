package usuarios;

public class ParticipanteExterno extends Usuario{
	private static final long serialVersionUID = 1L;
	private String tipoParticipante; 
	
	public ParticipanteExterno(String nombre, String login, String password, String tipoParticipante) {
		super(nombre, login, password);
		this.tipoParticipante = tipoParticipante;
	}
	
	public String getTipoParticipante() {
		return tipoParticipante;
	}
	
}
