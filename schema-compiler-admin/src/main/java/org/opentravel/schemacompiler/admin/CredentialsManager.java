/*
 * Copyright (c) 2012, Sabre Corporation and affiliates.
 * All Rights Reserved.
 * Use is subject to license agreement.
 */
package org.opentravel.schemacompiler.admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.opentravel.schemacompiler.security.PasswordHelper;

/**
 * Command-line utility used to perform the tasks required to manage the credentials file
 * for a protected namespace.
 * 
 * @author S. Livezey
 */
public class CredentialsManager {
	
	private static final String SCRIPT_WINDOWS = "ota2credentials.bat";
	private static final String SCRIPT_BASH    = "ota2credentials.sh";
	
	private static final String SCRIPT_NAME = System.getProperty("os.name").startsWith("Windows") ? SCRIPT_WINDOWS : SCRIPT_BASH;
	private static final String SCRIPT_SYNTAX = SCRIPT_NAME + " [options] <credentials-file>";
	
	/**
	 * Main method invoked from the command-line.
	 * 
	 * @param args  the GNU-style command-line arguments
	 */
	public static void main(String[] args) {
		new CredentialsManager().execute(args);
	}
	
	/**
	 * Executes the compilation tasks using the command-line arguments provided.
	 * 
	 * @param args  the GNU-style command-line arguments
	 */
	public void execute(String[] args) {
		try {
			CommandLine commandLineArgs = new GnuParser().parse(getCommandLineOptions(), args);
			boolean validArgs = validateCommandLine(commandLineArgs);
			File credentialsFile = validArgs ? Utils.getFileFromCommandLineArg(commandLineArgs.getArgs()[0]) : null;
			
			if (validArgs && (credentialsFile != null)) {
				String userId = commandLineArgs.getOptionValue('u');
				String password = commandLineArgs.hasOption('p') ? commandLineArgs.getOptionValue('p') : null;
				boolean removeUserId = commandLineArgs.hasOption('r');
				
				updateCredentialsFile(credentialsFile, userId, password, removeUserId);
				
			} else {
				displayHelp();
			}
		} catch (Throwable t) {
			Throwable rootCause = Utils.getRootCauseException(t);
			String errorMessage = MessageFormat.format(Utils.getMessageBundle().getString("credentials.errorMessage"),
					((rootCause.getMessage() == null) ? rootCause.getClass().getSimpleName() : rootCause.getMessage()) );
			
			System.out.println(errorMessage);
		}
	}
	
	/**
	 * Updates the contents of the credentials file using the information provided.
	 * 
	 * @param credentialsFile  the credentials file to update
	 * @param userId  the user ID to be added, updated, or removed
	 * @param password  the password to be assigned to the requested user
	 * @param removeUserId  indicates that the user's credentials are to be removed
	 * @throws IOException  thrown if a error prevents the update operation from succeeding
	 */
	protected void updateCredentialsFile(File credentialsFile, String userId, String password, boolean removeUserId) throws IOException {
		BufferedReader reader = null;
		PrintStream out = null;
		boolean success = false;
		File backupFile = null;
		
		try {
			// Read the contents of the existing file (if one exists)
			List<String> fileEntries = new ArrayList<String>();
			
			if (credentialsFile.exists()) {
				// Backup the existing file before we start
				backupFile = Utils.createBackupFile(credentialsFile);
				
				reader = new BufferedReader(new FileReader(credentialsFile));
				String line;
				
				while ((line = reader.readLine()) != null) {
					fileEntries.add(line);
				}
				reader.close();
				reader = null;
			}
			
			// Write the contents of the file back out, making sure the appropriate action was taken
			// for the entry that corresponds to the request user ID
			boolean userEntryProcessed = false;
			
			out = new PrintStream(new FileOutputStream(credentialsFile));
			
			for (String fileEntry : fileEntries) {
				if (fileEntry.trim().startsWith("#")) {
					out.println(fileEntry);
				} else {
					String[] entryParts = fileEntry.split(":");
					String entryUserId = (entryParts.length >= 1) ? entryParts[0] : "";
					
					if ((entryUserId != null) && entryUserId.equals(userId)) {
						if (!removeUserId) {
							StringBuilder newEntry = new StringBuilder();
							
							newEntry.append(userId).append(':').append( PasswordHelper.encrypt(password) );
							
							for (int i = 2; i < entryParts.length; i++) {
								newEntry.append(':').append(entryParts[i]);
							}
							out.println(newEntry.toString());
						}
						userEntryProcessed = true;
						
					} else {
						out.println(fileEntry);
					}
				}
			}
			
			// Handle case of a new user entry
			if (!userEntryProcessed) {
				out.println(userId + ":" + PasswordHelper.encrypt(password));
			}
			
			out.flush();
			out.close();
			out = null;
			
			if (backupFile != null) {
				backupFile.delete();
			}
			success = true;
			
		} finally {
			if (!success) {
				try {
					if (backupFile != null) {
						Utils.restoreOriginalFile(credentialsFile, backupFile);
					}
				} catch (IOException e) {
					System.out.println("ERROR: " + e.getMessage());
				}
			}
			try {
				if (reader != null) reader.close();
			} catch (Throwable t) {}
			try {
				if (out != null) out.close();
			} catch (Throwable t) {}
		}
	}
	
	/**
	 * Returns the command-line options for the OTA2 compiler.
	 * 
	 * @return Options
	 */
	protected Options getCommandLineOptions() {
		ResourceBundle messageBundle = Utils.getMessageBundle();
		Options options = new Options();
		
		options.addOption("u", "user", true, messageBundle.getString("credentials.user"));
		options.addOption("p", "password", true, messageBundle.getString("credentials.password"));
		options.addOption("r", "remove", false, messageBundle.getString("credentials.remove"));
		return options;
	}
	
	/**
	 * Returns true if the given command-line arguments are valid for use with this compiler instance
	 * (false otherwise).
	 * 
	 * @param commandLineArgs  the command-line arguments for the compilation
	 * @return boolean
	 */
	public boolean validateCommandLine(CommandLine commandLineArgs) {
		boolean isValid = true;
		
		isValid &= (commandLineArgs.getArgs().length == 1); // credentials filename is required
		isValid &= commandLineArgs.hasOption('u');
		
		// Password option is required unless the userID is being deleted
		if (!commandLineArgs.hasOption('r')) {
			isValid &= commandLineArgs.hasOption('p');
		}
		return isValid;
	}
	
	/**
	 * Displays the command-line help information.
	 */
	protected void displayHelp() {
		new HelpFormatter().printHelp(SCRIPT_SYNTAX,
				Utils.getMessageBundle().getString("credentials.helpHeader"),
				getCommandLineOptions(), null);
	}
	
}
