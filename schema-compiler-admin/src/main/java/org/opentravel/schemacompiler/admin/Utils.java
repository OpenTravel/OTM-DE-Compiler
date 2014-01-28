
package org.opentravel.schemacompiler.admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Static utility methods shared by the command-line tools in this module.
 * 
 * @author S. Livezey
 */
public class Utils {
	
	private static final String MESSAGE_RB = "/com/sabre/schemacompiler/admin/admin-messages.properties";
	private static final ResourceBundle messageBundle;
	
	/**
	 * Returns the resource bundle to be used for all user-displayable output.
	 * 
	 * @return ResourceBundle
	 */
	public static ResourceBundle getMessageBundle() {
		return messageBundle;
	}
	
	/**
	 * Returns a file handle for the specified file.
	 * 
	 * @param filename  the command-line name of the file (may be a relative or absolute path)
	 * @return File
	 */
	public static File getFileFromCommandLineArg(String filename) {
		File file;
		
		if (filename.startsWith("/") || ((filename.length() >= 2) && (filename.charAt(1) == ':'))) {
			// Absolute path for Linux or Windows
			file = new File(filename);
			
		} else {
			file = new File(System.getProperty("user.dir"), filename);
		}
		return file;
	}
	
	/**
	 * Returns the root-cause exception for the given throwable object.
	 * 
	 * @param t  the throwable for which to return the root cause
	 * @return Throwable
	 */
	public static Throwable getRootCauseException(Throwable t) {
		Throwable rootCause = t;
		
		while (rootCause.getCause() != null) {
			rootCause = rootCause.getCause();
		}
		return rootCause;
	}
	
	/**
	 * Backs up the contents of the specified file to a temporary file in the same directory.
	 * 
	 * @param originalFile  the original file to be backed up
	 * @throws IOException  thrown if the contents of the file cannot be backed up
	 */
	public static File createBackupFile(File originalFile) throws IOException {
		BufferedReader reader = null;
		PrintStream out = null;
		
		try {
			File backupFile = new File(originalFile.getAbsolutePath() + ".tmp");
			String line;
			
			if (backupFile.exists()) {
				if (!backupFile.delete()) {
					throw new IOException("Unable to delete the previous backup file located at: " +
							backupFile.getAbsolutePath());
				}
			}
			reader = new BufferedReader(new FileReader(originalFile));
			out = new PrintStream(new FileOutputStream(backupFile));
			
			while ((line = reader.readLine()) != null) {
				out.println(line);
			}
			out.flush();
			return backupFile;
			
		} finally {
			try {
				if (reader != null) reader.close();
			} catch (Throwable t) {}
			try {
				if (out != null) out.close();
			} catch (Throwable t) {}
		}
	}
	
	/**
	 * Restores the contents of the original file from the specified backup.
	 * 
	 * @param originalFile  the original file to be restored
	 * @param backupFile  the backup file that contains the original file content
	 * @throws IOException  thrown if the contents of the file cannot be restored
	 */
	public static void restoreOriginalFile(File originalFile, File backupFile) throws IOException {
		if (originalFile.exists()) {
			if (!originalFile.delete()) {
				throw new IOException("Unable to roll back the output file after a problem has occurred (the file is probably corrupted).");
			}
		}
		if (!backupFile.renameTo(originalFile)) {
			throw new IOException("Unable to restore contents of the original file file after an error.  Backup file located at: " + backupFile.getAbsolutePath());
		}
	}
	
	/**
	 * Initializes the resource bundle containing user-displayable messages.
	 */
	static {
		try {
			messageBundle = new PropertyResourceBundle(CredentialsManager.class.getResourceAsStream(MESSAGE_RB));
			
		} catch (Throwable t) {
			throw new ExceptionInInitializerError(t);
		}
	}
	
}
