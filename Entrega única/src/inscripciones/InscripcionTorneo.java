package inscripciones;

import java.io.Serializable;
import usuarios.Usuario;
import enumeraciones.TipoCupoTorneo;

public class InscripcionTorneo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Usuario usuario;
	private TipoCupoTorneo tipoCupo;
	private boolean esPareja;
	private Usuario parejaUsuario;
	
	public InscripcionTorneo(Usuario usuario, TipoCupoTorneo tipoCupo) {
		this.usuario = usuario;
		this.tipoCupo = tipoCupo;
		this.esPareja = false;
	}
	
	public InscripcionTorneo(Usuario usuario, Usuario parejaUsuario, TipoCupoTorneo tipoCupo) {
		this.usuario = usuario;		
		this.parejaUsuario = parejaUsuario;
		this.tipoCupo = tipoCupo;
		this.esPareja = true;
	}
	
	public Usuario getUsuario() {
		return usuario;
	}
	
	public Usuario getParejaUsuario() {
		return parejaUsuario;
	}
	
	public TipoCupoTorneo getTipoCupo() {
		return tipoCupo;
	}
	
	public boolean esPareja() {
		return esPareja;
	}
	
	
}
