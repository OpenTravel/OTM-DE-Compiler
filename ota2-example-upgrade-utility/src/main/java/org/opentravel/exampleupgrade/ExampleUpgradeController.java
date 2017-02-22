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

package org.opentravel.exampleupgrade;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.opentravel.schemacompiler.ioc.CompilerExtensionRegistry;
import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LibraryModelLoader;
import org.opentravel.schemacompiler.loader.impl.LibraryStreamInputSource;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.visitor.ModelNavigator;
import org.w3c.dom.Document;


/**
 * JavaFX controller class for the OTA2 Example Upgrade Utility application.
 */
public class ExampleUpgradeController {
	
	public static final String FXML_FILE = "/ota2-example-upgrade.fxml";
	
	private Stage primaryStage;
	
    @FXML private TextField libraryText;
    @FXML private Tooltip libraryTooltip;
    @FXML private Button libraryButton;
    @FXML private TextField exampleText;
    @FXML private Tooltip exampleTooltip;
    @FXML private Button exampleButton;
    @FXML private TextField rootElementPrefixText;
    @FXML private TextField rootElementNSText;
    @FXML private ChoiceBox<OTMObjectChoice> entityChoice;
    @FXML private Button strategyButton;
    @FXML private Button resetButton;
    @FXML private TreeView<DOMTreeNode> originalTreeView;
    @FXML private TreeView<?> upgradedTreeView;
    @FXML private TabPane tabPane;
    @FXML private AnchorPane previewTab;
    @FXML private AnchorPane originalTab;
    @FXML private AnchorPane autogenTab;
    @FXML private TableView<?> facetSelectionTableView;
    @FXML private TableColumn<?, ?> otmObjectColumn;
    @FXML private TableColumn<?, ?> facetSelectionColumn;
    @FXML private ChoiceBox<String> bindingStyleChoice;
    @FXML private Spinner<Integer> repeatCountSpinner;
    @FXML private Button saveButton;
    @FXML private Label statusBarLabel;
	private VirtualizedScrollPane<?> previewScrollPane;
	private CodeArea previewPane;
	
	private File modelFile;
	private File exampleFile;
	private File exampleFolder;
	private TLModel model;
	private SelectionStrategy selectionStrategy = SelectionStrategy.getDefault();
	private Document originalDocument;
	
	private Map<QName,List<OTMObjectChoice>> baseFamilyMatches = new HashMap<>();
	private Map<String,List<OTMObjectChoice>> allElementsByBaseNS = new HashMap<>();
	
	/**
	 * Called when the user clicks the button to load a new project or library file.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void selectLibrary(ActionEvent event) {
		File initialDirectory = (modelFile != null) ?
				modelFile.getParentFile() : UserSettings.load().getLastModelFile().getParentFile();
		FileChooser chooser = newFileChooser( "Select OTM Library or Project", initialDirectory,
				new FileChooser.ExtensionFilter( "OTM Projects", "*.otp" ),
				new FileChooser.ExtensionFilter( "OTM Libraries", "*.otm" ),
				new FileChooser.ExtensionFilter( "All Files", "*.*" ) );
		File selectedFile = chooser.showOpenDialog( primaryStage );
		
		if ((selectedFile != null) && selectedFile.exists()) {
			Runnable r = new BackgroundTask( "Loading Library: " + selectedFile.getName() ) {
				public void execute() throws Throwable {
					try {
						ValidationFindings findings;
						TLModel newModel = null;
						
						if (selectedFile.getName().endsWith(".otp")) {
							ProjectManager manager = new ProjectManager( false );
							
							findings = new ValidationFindings();
							manager.loadProject( selectedFile, findings );
							
							newModel = manager.getModel();
							
						} else { // assume OTM library file
					        LibraryInputSource<InputStream> libraryInput = new LibraryStreamInputSource( selectedFile );
					        LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>();
					        
					        findings = modelLoader.loadLibraryModel( libraryInput );
							newModel = modelLoader.getLibraryModel();
						}
						
						if ((findings == null) || !findings.hasFinding( FindingType.ERROR )) {
							QNameCandidateVisitor visitor = new QNameCandidateVisitor();
							
							model = newModel;
							modelFile = selectedFile;
							
							// Scan the model to pre-populate tables with lists of potential entity
							// selections for the example root element.
							new ModelNavigator( visitor ).navigate( model );
							baseFamilyMatches = visitor.getBaseFamilyMatches();
							allElementsByBaseNS = visitor.getAllElementsByBaseNS();
							
							rebuildEntityChoices();
							
						} else {
							System.out.println(selectedFile.getName() + " - Error/Warning Messages:");

							for (String message : findings.getAllValidationMessages(FindingMessageFormat.IDENTIFIED_FORMAT)) {
								System.out.println("  " + message);
							}
							throw new LibraryLoaderException("Validation errors detected in model (see log for details)");
						}
						
					} finally {
						updateControlStates();
					}
				}
			};
			
			new Thread( r ).start();
		}
	}
	
	/**
	 * Constructs a new file chooser instance using the information provided.
	 * 
	 * @param title  the title of the file chooser dialog
	 * @param initialDirectory  the initial directory for the chooser
	 * @param extensionFilters  the extension filters to include in the chooser
	 * @return FileChooser
	 */
	private FileChooser newFileChooser(String title, File initialDirectory, FileChooser.ExtensionFilter... extensionFilters) {
		FileChooser chooser = new FileChooser();
		File directory = initialDirectory;
		
		// Make sure the initial directory for the chooser exists
		while ((directory != null) && !directory.exists()) {
			directory = directory.getParentFile();
		}
		if (directory == null) {
			directory = new File( System.getProperty("user.home") );
		}
		
		chooser.setTitle( title );
		chooser.setInitialDirectory( directory );
		chooser.getExtensionFilters().addAll( extensionFilters );
		return chooser;
	}
	
	/**
	 * Rebuilds the contents of the entity selection maps.
	 */
	private void rebuildEntityChoices() {
		if ((originalDocument != null) && (model != null)) {
			QName rootName = HelperUtils.getElementName( originalDocument.getDocumentElement() );
			String rootBaseNS = HelperUtils.getBaseNamespace( rootName.getNamespaceURI() );
			List<OTMObjectChoice> candidateEntities = getCandidateEntities( rootName, rootBaseNS );
			if (candidateEntities == null) candidateEntities = new ArrayList<>();
			List<OTMObjectChoice> selectableObjects = new ArrayList<>();
			OTMObjectChoice exactMatch = null, _exactMatch;
			
			// Build the list of candidate entities
			for (OTMObjectChoice objectChoice : candidateEntities) {
				if (objectChoice.getOtmObjectName().equals( rootName )) {
					exactMatch = objectChoice;
				}
				selectableObjects.add( objectChoice );
			}
			
			if (exactMatch != null) {
				_exactMatch = exactMatch;
				
			} else {
				_exactMatch = (selectableObjects.size() > 0) ? selectableObjects.get( 0 ) : null;
			}
			
			Platform.runLater( () -> {
				entityChoice.setItems( FXCollections.observableArrayList( selectableObjects ) );
				
				if (_exactMatch != null) {
					entityChoice.setValue( _exactMatch );
				}
			});
		}
	}
	
	/**
	 * Returns the list of candidate entities to include in the OTM objects
	 * list of the display.
	 * 
	 * @param rootName  the qualified name of the root element of the DOM tree
	 * @param rootBaseNS  the base namespace of the root element
	 * @return List<OTMObjectChoice>
	 */
	private List<OTMObjectChoice> getCandidateEntities(QName rootName, String rootBaseNS) {
		List<OTMObjectChoice> candidates = null;
		
		switch (selectionStrategy.getStrategyType()) {
			case BASE_FAMILY:
				QName baseQName = new QName( rootBaseNS, rootName.getLocalPart() );
				candidates = baseFamilyMatches.get( baseQName );
				break;
			case EXAMPLE_NAMESPACE:
				candidates = allElementsByBaseNS.get( rootBaseNS );
				break;
			case USER_NAMESPACE:
				candidates = allElementsByBaseNS.get( selectionStrategy.getUserNamespace() );
				break;
		}
		if (candidates == null) {
			candidates = Collections.emptyList();
		}
		return candidates;
	}
	
	/**
	 * Called when the user clicks the button to load a new example file to be upgraded.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void selectExampleFile(ActionEvent event) {
		File initialDirectory = (exampleFolder != null) ?
				exampleFolder : UserSettings.load().getLastExampleFolder();
		FileChooser chooser = newFileChooser( "Select Example File", initialDirectory,
				new FileChooser.ExtensionFilter( "XML Files", "*.xml" ),
				new FileChooser.ExtensionFilter( "JSON Files", "*.json" ) );
		File selectedFile = chooser.showOpenDialog( primaryStage );
		
		if ((selectedFile != null) && selectedFile.exists()) {
			Runnable r = new BackgroundTask( "Loading Example Document: " + selectedFile.getName() ) {
				public void execute() throws Throwable {
					try {
						if (selectedFile.getName().endsWith(".xml")) {
							DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
							dbFactory.setNamespaceAware( true );
							DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
							
							originalDocument = dBuilder.parse( selectedFile );
							populateExampleContent();
							
						} else if (selectedFile.getName().endsWith(".json")) {
							throw new Exception("JSON documents not yet supported.");
							
						} else {
							throw new Exception("Unknown example file format: " + selectedFile.getName());
						}
						exampleFile = selectedFile;
						exampleFolder = exampleFile.getParentFile();
						
					} finally {
						updateControlStates();
					}
				}
			};
			
			new Thread( r ).start();
		}
	}
	
	/**
	 * Populates the contents of the visual controls associated with example content.
	 */
	private void populateExampleContent() {
		rebuildEntityChoices();
		Platform.runLater( () -> {
			rootElementPrefixText.setText( originalDocument.getDocumentElement().getNodeName() );
			rootElementNSText.setText( HelperUtils.getElementName( originalDocument.getDocumentElement() ).toString() );
			originalTreeView.setRoot( DOMTreeNode.createTree( originalDocument.getDocumentElement() ) );
		});
	}
	
	/**
	 * Called when the user clicks the button to modify the OTM Object selection strategy.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void selectStrategy(ActionEvent event) {
		try {
			List<String> userNamespaces = new ArrayList<>( allElementsByBaseNS.keySet() );
			FXMLLoader loader = new FXMLLoader( ExampleUpgradeController.class.getResource(
					SelectionStrategyController.FXML_FILE ) );
			SelectionStrategyController controller;
			AnchorPane page = loader.load();
			Stage dialogStage = new Stage();
			Scene scene = new Scene( page );
			
			dialogStage.setTitle( "OTM Object Selection Strategy" );
			dialogStage.initModality( Modality.WINDOW_MODAL );
			dialogStage.initOwner( primaryStage );
			dialogStage.setScene( scene );
			
			controller = loader.getController();
			controller.setDialogStage( dialogStage );
			Collections.sort( userNamespaces );
			controller.initialize( selectionStrategy, userNamespaces );
			controller.showAndWait();
			
			if (controller.isOkSelected()) {
				selectionStrategy = controller.getStrategy();
				rebuildEntityChoices();
			}
			
		} catch (IOException e) {
			e.printStackTrace( System.out );
		}
	}
	
	/**
	 * Resets all manual updates and restores the upgraded example to its original default
	 * state.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void resetContent(ActionEvent event) {
		System.out.println("ExampleUpgradeController.resetContent()");
	}
	
	/**
	 * Called when the user clicks the button to save the current example output to file.
	 * 
	 * @param event  the action event that triggered this method call
	 */
	@FXML public void saveExampleOutput(ActionEvent event) {
		System.out.println("ExampleUpgradeController.saveExampleOutput()");
	}
	
	/**
	 * Called when the user changes the default binding style.
	 */
	private void handleBindingStyleChange() {
		String selectedStyle = (String) bindingStyleChoice.getValue();
		String currentStyle = CompilerExtensionRegistry.getActiveExtension();

		if ((selectedStyle != null) && !selectedStyle.equals(currentStyle)) {
			CompilerExtensionRegistry.setActiveExtension(selectedStyle);
		}
	}
	
	/**
	 * Updates the enabled/disables states of the visual controls based on the current
	 * state of user selections.
	 */
	private void updateControlStates() {
		Platform.runLater( () -> {
			boolean exDisplayDisabled = (originalDocument == null);
			boolean exControlsDisabled = (model == null) || (originalDocument == null);
			
			libraryText.setText( (modelFile == null) ? "" : modelFile.getName() );
			libraryTooltip.setText( (modelFile == null) ? "" : modelFile.getAbsolutePath() );
			exampleText.setText( (exampleFile == null) ? "" : exampleFile.getName() );
			exampleTooltip.setText( (exampleFile == null) ? "" : exampleFile.getAbsolutePath() );
			
			rootElementPrefixText.disableProperty().set( exDisplayDisabled );
			rootElementNSText.disableProperty().set( exDisplayDisabled );
			originalTreeView.disableProperty().set( exDisplayDisabled );
			
			entityChoice.disableProperty().set( exControlsDisabled );
			strategyButton.disableProperty().set( exControlsDisabled );
			resetButton.disableProperty().set( exControlsDisabled );
			saveButton.disableProperty().set( exControlsDisabled );
			upgradedTreeView.disableProperty().set( exControlsDisabled );
			previewPane.disableProperty().set( exControlsDisabled );
		} );
	}
	
	/**
	 * Displays a message to the user in the status bar and optionally disables the
	 * interactive controls on the display.
	 * 
	 * @param message  the status bar message to display
	 * @param disableControls  flag indicating whether interactive controls should be disabled
	 */
	private void setStatusMessage(String message, boolean disableControls) {
		Platform.runLater( () -> {
			statusBarLabel.setText( message );
			
			libraryText.disableProperty().set( disableControls );
			libraryButton.disableProperty().set( disableControls );
			exampleText.disableProperty().set( disableControls );
			exampleButton.disableProperty().set( disableControls );
			rootElementPrefixText.disableProperty().set( disableControls );
			rootElementNSText.disableProperty().set( disableControls );
			entityChoice.disableProperty().set( disableControls );
			strategyButton.disableProperty().set( disableControls );
			resetButton.disableProperty().set( disableControls );
			saveButton.disableProperty().set( disableControls );
			upgradedTreeView.disableProperty().set( disableControls );
			originalTreeView.disableProperty().set( disableControls );
			bindingStyleChoice.disableProperty().set( disableControls );
			repeatCountSpinner.disableProperty().set( disableControls );
			facetSelectionTableView.disableProperty().set( disableControls );
			previewPane.disableProperty().set( disableControls );
		});
	}
	
	/**
	 * Assigns the primary stage for the window associated with this controller.
	 *
	 * @param primaryStage  the primary stage for this controller
	 * @param userSettings  provides user setting information from the last application session
	 */
	public void initialize(Stage primaryStage, UserSettings settings) {
		List<String> bindingStyles = CompilerExtensionRegistry.getAvailableExtensionIds();
		String defaultStyle = CompilerExtensionRegistry.getActiveExtension();
		
		// Since the preview pane is a custom component, we have to configure it manually
		previewPane = new CodeArea();
		previewPane.setEditable( false );
		previewScrollPane = new VirtualizedScrollPane<>( previewPane );
		Node pane = new StackPane( previewScrollPane );
		previewTab.getChildren().add( pane );
		AnchorPane.setTopAnchor(pane, 0.0D);
		AnchorPane.setBottomAnchor(pane, 0.0D);
		AnchorPane.setLeftAnchor(pane, 0.0D);
		AnchorPane.setRightAnchor(pane, 0.0D);
		
		// Configure listeners to capture interactions with the visual controls
		bindingStyleChoice.setItems( FXCollections.observableArrayList( bindingStyles ) );
		bindingStyleChoice.setValue( defaultStyle );
		bindingStyleChoice.valueProperty().addListener(
				(observable, oldValue, newValue) -> handleBindingStyleChange() );
		
		repeatCountSpinner.setValueFactory(
				new IntegerSpinnerValueFactory( 1, 3, settings.getRepeatCount(), 1 ) );
		
		this.primaryStage = primaryStage;
		this.primaryStage.getScene().getStylesheets().add(
				ExampleUpgradeController.class.getResource( "/styles/xml-highlighting.css" ).toExternalForm() );
		this.primaryStage.getScene().getStylesheets().add(
				ExampleUpgradeController.class.getResource( "/styles/json-highlighting.css" ).toExternalForm() );
		updateControlStates();
	}
	
	/**
	 * Allows the controller to save any updates to the user settings prior to application
	 * close.
	 * 
	 * @param settings  the user settings to be updated
	 */
	public void updateUserSettings(UserSettings settings) {
		if (modelFile != null) {
			settings.setLastModelFile( modelFile );
		}
		if (exampleFolder != null) {
			settings.setLastExampleFolder( exampleFolder );
		}
		settings.setRepeatCount( repeatCountSpinner.getValue() );
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
