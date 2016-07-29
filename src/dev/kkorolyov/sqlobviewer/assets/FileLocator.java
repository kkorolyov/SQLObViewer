package dev.kkorolyov.sqlobviewer.assets;

import static dev.kkorolyov.sqlobviewer.assets.ApplicationProperties.Keys.ASSETS_FOLDER;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.jar.JarFile;

import dev.kkorolyov.randomutils.extractor.ResourceExtractor;
import dev.kkorolyov.simplelogs.Logger;
import dev.kkorolyov.simplelogs.Logger.Level;
import dev.kkorolyov.sqlobviewer.assets.ApplicationProperties.Config;

/**
 * Provides methods for locating application files from various sources.
 */
public class FileLocator {
	/** Relative path to the folder containing various sizes of the main application icon. */
	public static final String MAIN_ICON = "icons/main-icon/";

	private static final String EXTERNAL_ASSETS_FOLDER = Config.get(ASSETS_FOLDER),
															BUNDLED_ASSETS_FOLDER = getBundledAssetsFolder();
	private static final String JAR_PATH = getJarPath();
	
	private static final Logger log = Logger.getLogger(FileLocator.class.getName(), Level.DEBUG, (PrintWriter[]) null);
	
	/**
	 * Locates a file by sequentially checking in listed order:
	 * <ol>
	 * <li>The external assets folder</li>
	 * <li>The application .jar (if application launched from a .jar file)</li>
	 * <li>The loose-bundled assets folder (if application launched from a loose collection of files)</li>
	 * </ol>
	 * @param filename name of file to locate
	 * @return appropriate file
	 */
	public static File locateFile(String filename) {
		File file = getExternalFile(filename);
		
		if (file.exists())
			log.info("Located external " + filename + " at: " + file.getAbsolutePath());
		else if (JAR_PATH != null)
			file = getJarBundledFile(filename);
		else
			file = getLooseBundledFile(filename);
		
		return file;
	}
	
	private static File getExternalFile(String filename) {
		return new File(EXTERNAL_ASSETS_FOLDER + filename);
	}
	private static File getJarBundledFile(String filename) {
		log.debug("Extracting jar-bundled file: " + filename + "...");
		
		File extractedFile = null;
		
		try (JarFile jar = new JarFile(new File(JAR_PATH))) {
			File externalAssetsFolder = new File(EXTERNAL_ASSETS_FOLDER);
			if (!externalAssetsFolder.exists())
				externalAssetsFolder.mkdirs();
			
			ResourceExtractor.extractJarResources(jar, BUNDLED_ASSETS_FOLDER + filename, BUNDLED_ASSETS_FOLDER, externalAssetsFolder);
			
			extractedFile = new File(EXTERNAL_ASSETS_FOLDER + filename);
			
			log.info("Successfully extracted jar-bundled " + filename + " to: " + extractedFile.getAbsolutePath());
		} catch (IOException e) {
			log.severe("Failed to extract " + filename + " from application .jar");
			log.exception(e);
		}
		return extractedFile;
	}
	private static File getLooseBundledFile(String filename) {
		log.debug("Retrieving loose-bundled file: " + filename);
		
		File looseFile = null;
		
		try {
			looseFile = new File(FileLocator.class.getResource("/" + BUNDLED_ASSETS_FOLDER + filename).toURI());
			
			log.info("Successfully retrieved loose-bundled " + filename + " from: " + looseFile.getAbsolutePath());
		} catch(URISyntaxException e) {
			log.severe("Filed to retrieve loose-bundled file: " + filename);
			log.exception(e);
		}
		return looseFile;
	}
	
	private static String getBundledAssetsFolder() {
		return FileLocator.class.getPackage().getName().replaceAll("\\.", "/") + "/";
	}
	private static String getJarPath() {
		String jarPath = FileLocator.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		
		try (JarFile jar = new JarFile(new File(jarPath))) {
			// Try to open application .jar
		} catch (IOException e) {	// Application probably not launched from a .jar
			jarPath = null;
		}
		return jarPath;
	}
}
