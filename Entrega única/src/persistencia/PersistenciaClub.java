package persistencia;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import club.ClubDeportivoSeneca;

/**
 * Carga los datos del club al iniciar una consola y los guarda al cerrarla, siempre
 * en la misma carpeta predeterminada: nunca se le pide al usuario el nombre de un archivo.
 * Las tres consolas usan esta clase, asi que todas leen y escriben los mismos archivos.
 */
public class PersistenciaClub {

	public static final String NOMBRE_CARPETA = "datos";
	public static final String NOMBRE_ARCHIVO = "club_seneca.dat";

	/** Carpeta del repositorio que contiene el proyecto ("Entrega unica", con tilde en la u). */
	private static final String CARPETA_ENTREGA = "Entrega única";
	private static final String CARPETA_TEMPORAL = "guardando.tmp";

	private File carpeta;

	/** Usa la carpeta de datos predeterminada, buscada desde el directorio donde corre el programa. */
	public PersistenciaClub() {
		this(resolverCarpetaPorDefecto(new File(".")));
	}

	public PersistenciaClub(File carpeta) {
		if (carpeta == null) {
			throw new IllegalArgumentException("Debe indicar la carpeta de datos.");
		}
		this.carpeta = carpeta;
	}

	/**
	 * El programa puede correr desde la raiz del repositorio o desde la carpeta de la entrega.
	 * Se prefiere la carpeta que ya tenga el archivo de datos; si ninguna lo tiene, la que exista.
	 */
	public static File resolverCarpetaPorDefecto(File directorioDeTrabajo) {
		File enTrabajo = new File(directorioDeTrabajo, NOMBRE_CARPETA);
		File enEntrega = new File(new File(directorioDeTrabajo, CARPETA_ENTREGA), NOMBRE_CARPETA);
		File[] candidatas = { enTrabajo, enEntrega };

		for (int i = 0; i < candidatas.length; i++) {
			if (new File(candidatas[i], NOMBRE_ARCHIVO).isFile()) {
				return candidatas[i];
			}
		}
		for (int i = 0; i < candidatas.length; i++) {
			if (candidatas[i].isDirectory()) {
				return candidatas[i];
			}
		}
		return enTrabajo;
	}

	public boolean existenDatos() {
		return getArchivoDatos().isFile();
	}

	/**
	 * Devuelve el club guardado o, si todavia no hay archivo, un club nuevo.
	 * Un archivo danado no se reemplaza por un club vacio: se informa el error para no perder los datos.
	 */
	public ClubDeportivoSeneca cargar() {
		File archivo = getArchivoDatos();
		if (!archivo.isFile()) {
			return new ClubDeportivoSeneca();
		}
		// El archivo se declara aparte para que se cierre aunque su contenido no sea un club valido.
		try (FileInputStream flujoArchivo = new FileInputStream(archivo);
				ObjectInputStream entrada = new ObjectInputStream(flujoArchivo)) {
			return (ClubDeportivoSeneca) entrada.readObject();
		} catch (IOException | ClassNotFoundException | ClassCastException e) {
			throw new PersistenciaException("No se pudieron leer los datos de " + archivo.getPath() + ": " + e, e);
		}
	}

	/**
	 * Escribe primero en una carpeta temporal y solo si todo sale bien reemplaza los archivos
	 * anteriores. Si el guardado falla, los datos que habia antes quedan intactos.
	 */
	public void guardar(ClubDeportivoSeneca club) {
		if (club == null) {
			throw new IllegalArgumentException("No hay un club para guardar.");
		}
		File temporal = new File(carpeta, CARPETA_TEMPORAL);
		try {
			if (!carpeta.isDirectory() && !carpeta.mkdirs()) {
				throw new IOException("no se pudo crear la carpeta " + carpeta.getPath());
			}
			borrar(temporal);
			club.guardarDatos(temporal.getPath());

			File[] nuevos = temporal.listFiles();
			if (nuevos == null || !new File(temporal, NOMBRE_ARCHIVO).isFile()) {
				throw new IOException("no se genero el archivo " + NOMBRE_ARCHIVO);
			}
			for (int i = 0; i < nuevos.length; i++) {
				File destino = new File(carpeta, nuevos[i].getName());
				Files.move(nuevos[i].toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException | RuntimeException e) {
			throw new PersistenciaException("No se pudieron guardar los datos en " + carpeta.getPath() + ": " + e.getMessage(), e);
		} finally {
			borrar(temporal);
		}
	}

	private void borrar(File archivo) {
		File[] hijos = archivo.listFiles();
		if (hijos != null) {
			for (int i = 0; i < hijos.length; i++) {
				borrar(hijos[i]);
			}
		}
		archivo.delete();
	}

	public File getCarpeta() {
		return carpeta;
	}

	public File getArchivoDatos() {
		return new File(carpeta, NOMBRE_ARCHIVO);
	}
}
