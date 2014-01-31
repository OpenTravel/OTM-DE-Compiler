/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opentravel.schemacompiler.admin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LibraryModuleInfo;
import org.opentravel.schemacompiler.loader.LibraryModuleLoader;
import org.opentravel.schemacompiler.loader.impl.LibraryStreamInputSource;
import org.opentravel.schemacompiler.loader.impl.MultiVersionLibraryModuleLoader;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.saver.LibraryModelSaver;
import org.opentravel.schemacompiler.security.LibrarySecurityHandler;
import org.opentravel.schemacompiler.security.ProtectedNamespaceCredentials;
import org.opentravel.schemacompiler.security.ProtectedNamespaceGroup;
import org.opentravel.schemacompiler.security.ProtectedNamespaceRegistry;
import org.opentravel.schemacompiler.security.SchemaCompilerSecurityException;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.util.URLUtils;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Allows users with the appropriate permissions to update a library (OTM) file with
 * the correct CRC.  If the library is in a protected namespace, the user must specify
 * valid credentials in the command-line parameters.  If the library is not in a protected
 * namespace, but a CRC is required for some other reason (e.g. the library has a status
 * of 'FINAL'), the CRC may be updated without any user credentials.
 * 
 * @author S. Livezey
 */
public class LibraryCrcManager {
	
	private static final String SCRIPT_WINDOWS = "updateCrc.bat";
	private static final String SCRIPT_BASH    = "updateCrc.sh";
	
	private static final String SCRIPT_NAME = System.getProperty("os.name").startsWith("Windows") ? SCRIPT_WINDOWS : SCRIPT_BASH;
	private static final String SCRIPT_SYNTAX = SCRIPT_NAME + " [options] <otm-library-file>";
	
	private ProtectedNamespaceRegistry namespaceRegistry = ProtectedNamespaceRegistry.getInstance();
	
	/**
	 * Main method invoked from the command-line.
	 * 
	 * @param args  the GNU-style command-line arguments
	 */
	public static void main(String[] args) {
		new LibraryCrcManager().execute(args);
	}
	
	/**
	 * Executes the compilation tasks using the command-line arguments provided.
	 * 
	 * @param args  the GNU-style command-line arguments
	 */
	public void execute(String[] args) {
		try {
			CommandLine commandLineArgs = new GnuParser().parse(getCommandLineOptions(), args);
			
			if (validateCommandLine(commandLineArgs)) {
				File libraryFile = Utils.getFileFromCommandLineArg(commandLineArgs.getArgs()[0]);
				Object jaxbLibrary = loadLibrary(libraryFile);
				TLLibrary library = transformLibrary(libraryFile, jaxbLibrary);
				ProtectedNamespaceCredentials credentials = null;
				
				// For protected namespaces, ensure the user's credentials allow write-access to the library
				if (namespaceRegistry.isProtectedNamespace(library.getNamespace())) {
					String userId = commandLineArgs.hasOption('u') ? commandLineArgs.getOptionValue('u') : null;
					String password = commandLineArgs.hasOption('p') ? commandLineArgs.getOptionValue('p') : null;
					
					if ((userId == null) || (password == null)) {
						throw new SchemaCompilerSecurityException(
								"User credentials are required to reset the CRC for libraries in a protected namespace.");
					}
					credentials = buildCredentials(userId, password);
					
					if (!namespaceRegistry.hasWriteAccess(library.getNamespace(), credentials)) {
						throw new SchemaCompilerSecurityException(
								"The userID and/or password provided are not valid for the library's protected namespace.");
					}
					LibrarySecurityHandler.setUserCredentials(credentials);
				}
				
				// Re-save the file (forces re-calculation of the CRC)
				new LibraryModelSaver().saveLibrary(library);
				System.out.println( MessageFormat.format(
						Utils.getMessageBundle().getString("crc.success"), libraryFile.getName()) );
				
			} else {
				displayHelp();
			}
			
		} catch (Throwable t) {
			Throwable rootCause = Utils.getRootCauseException(t);
			String errorMessage = MessageFormat.format(Utils.getMessageBundle().getString("crc.errorMessage"),
					((rootCause.getMessage() == null) ? rootCause.getClass().getSimpleName() : rootCause.getMessage()) );
			
			System.out.println(errorMessage);
		}
	}
	
	/**
	 * Loads the contents of the library from the specified file.
	 * 
	 * @param libraryFile  the library file to load
	 * @return Object
	 * @throws IOException  thrown if the specified file does not exist
	 * @throws LibraryLoaderException  thrown if an unexpected exception occurs while attempting
	 *								   to load the contents of the library
	 */
	protected Object loadLibrary(File libraryFile) throws IOException, LibraryLoaderException {
		if (!libraryFile.exists()) {
			throw new FileNotFoundException("The specified library file does not exist: " + libraryFile.getAbsolutePath());
		}
		LibraryModuleLoader<InputStream> loader = new MultiVersionLibraryModuleLoader();
		LibraryModuleInfo<Object> moduleInfo = loader.loadLibrary(new LibraryStreamInputSource(libraryFile), new ValidationFindings());
		Object jaxbLibrary = moduleInfo.getJaxbArtifact();
		
		if (jaxbLibrary == null) {
			throw new LibraryLoaderException("The specified file does not follow a valid OTM library file format.");
		}
		return jaxbLibrary;
	}
	
	/**
	 * Transforms the contents of the specified library into its <code>TLLibrary</code> representation.
	 * 
	 * @param libraryFile  the location of the library file to be transformed
	 * @param jaxbLibrary  the JAXB library instance to transform
	 * @return TLLibrary
	 */
	protected TLLibrary transformLibrary(File libraryFile, Object jaxbLibrary) {
		TransformerFactory<DefaultTransformerContext> transformerFactory =
				TransformerFactory.getInstance(SchemaCompilerApplicationContext.LOADER_TRANSFORMER_FACTORY,
						new DefaultTransformerContext());
		ObjectTransformer<Object,TLLibrary,DefaultTransformerContext> transformer =
				transformerFactory.getTransformer(jaxbLibrary, TLLibrary.class);
		TLLibrary library = transformer.transform(jaxbLibrary);
		
		library.setLibraryUrl( URLUtils.toURL(libraryFile) );
		return library;
	}
	
	/**
	 * Constructs the credentials instance using the information provided.
	 * 
	 * @param userId  the user ID to use when determining access to the protected namespace
	 * @param password  the password to use when determining access to the protected namespace
	 * @return ProtectedNamespaceCredentials
	 */
	protected ProtectedNamespaceCredentials buildCredentials(String userId, String password) {
		ProtectedNamespaceCredentials credentials = new ProtectedNamespaceCredentials();
		
		for (ProtectedNamespaceGroup nsGroup : namespaceRegistry.getProtectedNamespaces()) {
			credentials.setCredentials(nsGroup.getGroupId(), userId, password);
		}
		return credentials;
	}
	
	/**
	 * Returns the command-line options for the OTA2 compiler.
	 * 
	 * @return Options
	 */
	protected Options getCommandLineOptions() {
		ResourceBundle messageBundle = Utils.getMessageBundle();
		Options options = new Options();
		
		options.addOption("u", "user", true, messageBundle.getString("crc.user"));
		options.addOption("p", "password", true, messageBundle.getString("crc.password"));
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
		return (commandLineArgs.getArgs().length == 1); // library filename is required
	}
	
	/**
	 * Displays the command-line help information.
	 */
	protected void displayHelp() {
		new HelpFormatter().printHelp(SCRIPT_SYNTAX,
				Utils.getMessageBundle().getString("crc.helpHeader"),
				getCommandLineOptions(), null);
	}
	
}
