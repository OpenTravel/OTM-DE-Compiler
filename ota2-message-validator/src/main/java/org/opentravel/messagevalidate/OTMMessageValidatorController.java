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

package org.opentravel.messagevalidate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.opentravel.schemacompiler.ioc.CompilerExtensionRegistry;
import org.opentravel.schemacompiler.task.CompileAllCompilerTask;
import org.opentravel.schemacompiler.util.SchemaCompilerException;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 * JavaFX controller class for the OTM-Diff application.
 */
public class OTMMessageValidatorController {
	
	public static final String FXML_FILE = "/ota2-message-validator.fxml";
	
	private Stage primaryStage;
	
	@FXML private TextField projectFilename;
	@FXML private TextField messageFilename;
	@FXML private TextArea validationOutput;
	@FXML private Button projectButton;
	@FXML private Button messageButton;
	@FXML private Button validateButton;
	@FXML private Label statusBarLabel;
	
	private File projectFile;
	private File messageFile;
	private File codegenFolder;
	
	private UserSettings userSettings;
	
	/**
	 * Called when the user clicks the button select the OTM project file.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void handleSelectProjectFile(ActionEvent event) {
		FileChooser chooser = newFileChooser( "Select OTM Project", userSettings.getProjectFolder(),
				new String[] { "otp" }, new String[] { "OTM Project Files" } );
		File selectedFile = chooser.showOpenDialog( primaryStage );
		
		if (selectedFile != null) {
			Runnable r = new BackgroundTask( "Loading Project: " + selectedFile.getName() ) {
				public void execute() throws Throwable {
					try {
						projectFile = selectedFile;
						setFilenameText( selectedFile.getName(), projectFilename );
						generateSchemas();
						
					} finally {
						userSettings.setProjectFolder( selectedFile.getParentFile() );
						userSettings.save();
					}
				}
			};
			
			new Thread( r ).start();
		}
	}
	
	/**
	 * Called when the user clicks the button select the OTM project file.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void handleSelectMessageFile(ActionEvent event) {
		FileChooser chooser = newFileChooser( "Select Message to Validate", userSettings.getMessageFolder(),
				new String[] { "json", "xml" }, new String[] { "JSON Message Files", "XML Message Files" } );
		File selectedFile = chooser.showOpenDialog( primaryStage );
		
		if (selectedFile != null) {
			Runnable r = new BackgroundTask( "Validating Message File: " + selectedFile.getName() ) {
				public void execute() throws Throwable {
					try {
						messageFile = selectedFile;
						setFilenameText( selectedFile.getName(), messageFilename );
						handleValidateMessage( null );
						
					} finally {
						userSettings.setMessageFolder( selectedFile.getParentFile() );
						userSettings.save();
					}
				}
			};
			
			new Thread( r ).start();
		}
	}
	
	/**
	 * Called when the user clicks the button select the OTM project file.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void handleValidateMessage(ActionEvent event) {
		Runnable r = new BackgroundTask( "Validating Message..." ) {
			public void execute() throws Throwable {
				ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
				
				new MessageValidator( codegenFolder, new PrintStream( bytesOut ) )
						.validate( messageFile );
				validationOutput.setText( new String( bytesOut.toByteArray() ) );
			}
		};
		
		new Thread( r ).start();
	}
	
	/**
	 * Generates XML and JSON schemas on the local file system from the selected
	 * OTM project file.
	 * 
	 * @throws SchemaCompilerException  thrown if an error occurs or the model
	 *									contains validation errors
	 */
	private void generateSchemas() throws SchemaCompilerException {
        CompileAllCompilerTask compilerTask = new CompileAllCompilerTask();
		ValidationFindings findings;
		
		CompilerExtensionRegistry.setActiveExtension( "OTA2" );
		
		codegenFolder = getOutputFolder();
        codegenFolder.mkdirs();
		compilerTask.applyTaskOptions( new ValidationCompileOptions( codegenFolder ) );
		findings = compilerTask.compileOutput( projectFile );
		
        if (findings.hasFinding(FindingType.ERROR)) {
        	System.out.println("ERROR MESSAGES:");
        	for (String message : findings.getValidationMessages( FindingType.ERROR, FindingMessageFormat.IDENTIFIED_FORMAT )) {
        		System.out.println("  " + message);
        	}
        	throw new SchemaCompilerException("Errors in OTM model (see console for details).");
        }
	}
	
	/**
	 * Returns the location of the schema generation output folder.  If the folder
	 * currently exists, it and its contents will be deleted by this method.
	 * 
	 * @return File
	 */
	private File getOutputFolder() {
		File projectFolder = projectFile.getParentFile();
		String folderName = projectFile.getName();
		int dotIdx = folderName.lastIndexOf( '.' );
		File outputFolder;
		
		if (dotIdx >= 0) {
			folderName = folderName.substring( 0, dotIdx );
		}
		folderName += "_ValidatorOutput";
		outputFolder = new File( projectFolder, folderName );
		deleteFolderContents( outputFolder );
		return outputFolder;
	}
	
	/**
	 * Recursively deletes the given folder and all of its contents.
	 * 
	 * @param folderLocation  the location of the folder to be deleted
	 */
	private void deleteFolderContents(File folderLocation) {
		if (folderLocation.exists()) {
			if (folderLocation.isDirectory()) {
				for (File folderMember : folderLocation.listFiles()) {
					deleteFolderContents( folderMember );
				}
			}
			folderLocation.delete();
		}
	}
	
	/**
	 * Updates the value of the specified filename text field.
	 * 
	 * @param filenameValue  the value to assign to the filename text field
	 * @param textField  the text field to which the value will be assigned
	 */
	private void setFilenameText(String filenameValue, TextField textField) {
		Platform.runLater( new Runnable() {
			public void run() {
				textField.setText( filenameValue );
			}
		});
	}
	
	/**
	 * Displays a message to the user in the status bar and optionally disables the
	 * interactive controls on the display.
	 * 
	 * @param message  the status bar message to display
	 * @param disableControls  flag indicating whether interactive controls should be disabled
	 */
	private void setStatusMessage(String message, boolean disableControls) {
		Platform.runLater( new Runnable() {
			public void run() {
				statusBarLabel.setText( message );
				projectFilename.disableProperty().set( disableControls );
				projectButton.disableProperty().set( disableControls );
				messageFilename.disableProperty().set( disableControls );
				messageButton.disableProperty().set( disableControls );
				validateButton.disableProperty().set( disableControls );
			}
		});
	}
	
	/**
	 * Returns a new file chooser that is configured for the selection of a sepecific
	 * type of file.
	 * 
	 * @param title  the title of the new file chooser
	 * @param initialDirectory  the initial directory location for the chooser
	 * @param fileExtension  the file extension of the chooser's filter
	 * @param extensionDescription  description of the specified file extension
	 * @return FileChooser
	 */
	private FileChooser newFileChooser(String title, File initialDirectory,
			String[] fileExtension, String[] extensionDescription) {
		FileChooser chooser = new FileChooser();
		File directory = initialDirectory;
		
		// Make sure the initial directory for the chooser exists
		while ((directory != null) && !directory.exists()) {
			directory = directory.getParentFile();
		}
		if (directory == null) {
			directory = new File( System.getProperty("user.home") );
		}
		ExtensionFilter[] filters = new ExtensionFilter[ fileExtension.length + 1 ];
		
		for (int i = 0; i < fileExtension.length; i++) {
			filters[i] = new ExtensionFilter(extensionDescription[i], "*." + fileExtension[i] );
		}
		filters[ filters.length - 1 ] = new FileChooser.ExtensionFilter("All Files", "*.*");
		
		chooser.setTitle( title );
		chooser.setInitialDirectory( directory );
		chooser.getExtensionFilters().addAll( filters );
		return chooser;
	}
	
	/**
	 * Updates the enabled/disables states of the visual controls based on the current
	 * state of user selections.
	 */
	private void updateControlStates() {
		Platform.runLater( new Runnable() {
			public void run() {
				boolean projectSelected = (projectFile != null) && projectFile.exists();
				boolean messageSelected = (messageFile != null) && messageFile.exists();
				
				validateButton.disableProperty().set( !projectSelected || !messageSelected );
			}
		});
	}
	
	/**
	 * Assigns the primary stage for the window associated with this controller.
	 *
	 * @param primaryStage  the primary stage for this controller
	 */
	public void setPrimaryStage(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.userSettings = UserSettings.load();
		updateControlStates();
	}
	
	/**
	 * Abstract class that executes a background task in a non-UI thread.
	 */
	private abstract class BackgroundTask implements Runnable {
		
		private String statusMessage;
		
		/**
		 * Constructor that specifies the status message to display during task execution.
		 * 
		 * @param statusMessage  the status message for the task
		 */
		public BackgroundTask(String statusMessage) {
			this.statusMessage = statusMessage;
		}
		
		/**
		 * Executes the sub-class specific task functions.
		 * 
		 * @throws Throwable  thrown if an error occurs during task execution
		 */
		protected abstract void execute() throws Throwable;

		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try {
				setStatusMessage( statusMessage, true );
				execute();
				
			} catch (Throwable t) {
				String errorMessage = (t.getMessage() != null) ? t.getMessage() : "See log output for details.";
				
				try {
					setStatusMessage( "ERROR: " + errorMessage, false );
					updateControlStates();
					t.printStackTrace( System.out );
					Thread.sleep( 1000 );
					
				} catch (InterruptedException e) {}
				
			} finally {
				setStatusMessage( null, false );
				updateControlStates();
			}
		}
		
	}
	
}
