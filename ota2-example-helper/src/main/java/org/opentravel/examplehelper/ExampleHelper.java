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
package org.opentravel.examplehelper;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractCellEditor;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.xml.namespace.QName;

import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.codegen.example.ExampleBuilder;
import org.opentravel.schemacompiler.codegen.example.ExampleDocumentBuilder;
import org.opentravel.schemacompiler.codegen.example.ExampleGeneratorOptions;
import org.opentravel.schemacompiler.codegen.example.ExampleJsonBuilder;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import org.opentravel.schemacompiler.ioc.CompilerExtensionRegistry;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LibraryModelLoader;
import org.opentravel.schemacompiler.loader.impl.LibraryStreamInputSource;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLReferenceType;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.repository.ProjectManager;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.validate.FindingMessageFormat;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationException;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.version.VersionScheme;
import org.opentravel.schemacompiler.version.VersionSchemeException;
import org.opentravel.schemacompiler.version.VersionSchemeFactory;
import org.opentravel.schemacompiler.xml.XMLPrettyPrinter;
import org.w3c.dom.Document;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Application that allows the user to specify specific OTM facets for
 * substitution groups in examples generated by the OTM compiler.
 */
public class ExampleHelper extends JFrame {

	private static final long serialVersionUID = 4428849155191945103L;

	private static FacetCodegenDelegateFactory facetDelegateFactory = new FacetCodegenDelegateFactory(null);

	private File modelFile;
	private File exampleFolder;
	private TLModel model;
	private NamedEntity selectedObject;
	private Map<QName, SubstitutionGroupTableModel> sgModelMap = new HashMap<>();

	private JTextField filenameText;
	private JButton selectFileButton;
	private JComboBox<String> bindingStyleCombo;
	private JComboBox<OTMObjectItem> objectCombo;
	private JSpinner repeatCountSpinner;
	private JTable sgTable;
	private ExamplePane previewPane;
	private JScrollPane previewScroll;
	private int previewLineCount;
	private JLabel statusLabel;
	private JRadioButton xmlRadio;
	private JRadioButton jsonRadio;

	/**
	 * Default constructor.
	 */
	public ExampleHelper() {
		init();
	}

	/**
	 * Opens the specified OTM library or project file and uses it to populate
	 * the content of the visible controls. If a file is already open, it is
	 * closed and replaced by the one specified here.
	 * 
	 * @param file
	 *            the OTM library or project file to open
	 */
	public void setLibraryFile(final File file) {
		Runnable job = new Runnable() {
			public void run() {
				String errorMessage = null;
				TLModel newModel = null;
				
				if (file.exists()) {
					try {
						ValidationFindings findings = null;
						
						setEnableState(false, "Loading - " + file.getName());

						// Load differently depending on whether the file is a
						// library or project
						if (file.getName().toLowerCase().endsWith(".otm")) {
							LibraryModelLoader<InputStream> modelLoader = new LibraryModelLoader<InputStream>();

							findings = modelLoader.loadLibraryModel(new LibraryStreamInputSource(file));
							newModel = modelLoader.getLibraryModel();

						} else if (file.getName().toLowerCase().endsWith(".otp")) {
							try {
								ProjectManager projectManager = new ProjectManager( false );

								projectManager.loadProject(file, (findings = new ValidationFindings()));
								newModel = projectManager.getModel();

							} catch (RepositoryException e) {
								errorMessage = e.getMessage();
							}
						}

						// Report validation findings if errors were found
						if ((findings != null)
								&& findings.hasFinding(FindingType.ERROR)) {
							errorMessage = "The file could not be opened because it contains one or more validation errors (see console for details).";
							System.out.println(file.getName() + " - Error/Warning Messages:");

							for (String message : findings.getAllValidationMessages(FindingMessageFormat.IDENTIFIED_FORMAT)) {
								System.out.println("  " + message);
							}
						}

					} catch (LibraryLoaderException e) {
						errorMessage = e.getMessage();

					} finally {
						setEnableState(true, null);
					}

				} else {
					for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
						System.out.println(ste.toString());
					}
					errorMessage = "The specified file does not exist: " + file.getName();
				}

				// Populate the controls with content from the new library or
				// report an error
				// in case of failre
				if (errorMessage == null) {
					modelFile = file;
					model = newModel;
					sgModelMap.clear();
					updateControls();

				} else {
					JOptionPane.showMessageDialog(ExampleHelper.this,
							errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		};

		// If the current thread is an event dispatcher, spawn a new thread for
		// the load operation
		if (SwingUtilities.isEventDispatchThread()) {
			new Thread(job).start();
		} else {
			job.run();
		}
	}

	/**
	 * Launches a file dialog to select an OTM library or project file.
	 */
	protected void selectLibraryFile() {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setFileFilter(new FileFilter() {
			public String getDescription() {
				return "OTM Files (*.otm / *.otp)";
			}

			public boolean accept(File f) {
				String filename = f.getName().toLowerCase();
				return (f.isDirectory() || filename.endsWith(".otm")
						|| filename.endsWith(".otp"));
			}
		});
		if (modelFile != null) {
			fc.setSelectedFile(modelFile);
		}

		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			setLibraryFile(fc.getSelectedFile());
		}
	}

	/**
	 * Called when the user changes the default binding style.
	 */
	protected void selectBindingStyle() {
		String selectedStyle = (String) bindingStyleCombo.getSelectedItem();
		String currentStyle = CompilerExtensionRegistry.getActiveExtension();

		if ((selectedStyle != null) && !selectedStyle.equals(currentStyle)) {
			CompilerExtensionRegistry.setActiveExtension(selectedStyle);

			if ((modelFile != null) && (model != null)) {
				setLibraryFile(modelFile);
			}
		}
	}

	/**
	 * Called when the user selected a new OTM object from the combo box.
	 */
	protected void selectOtmObject() {
		NamedEntity selectedObject = objectCombo.getItemAt(
				objectCombo.getSelectedIndex()).getOtmObject();

		if (this.selectedObject != selectedObject) {
			QName objectName = new QName(selectedObject.getNamespace(),
					selectedObject.getLocalName());
			SubstitutionGroupTableModel tableModel = sgModelMap.get(objectName);

			if (tableModel == null) {
				tableModel = newTableModel(selectedObject);
				sgModelMap.put(objectName, tableModel);
			}
			sgTable.setModel(tableModel);
			this.selectedObject = selectedObject;
			refreshExample();
		}
	}

	/**
	 * Constructs a new <code>SubstitutionGroupTableModel</code> for the given
	 * entity.
	 * 
	 * @param entity
	 *            the OTM object for which to construct a table model
	 * @return SubstitutionGroupTableModel
	 */
	private SubstitutionGroupTableModel newTableModel(NamedEntity entity) {
		List<SubstitutionGroupItem> itemList = new ArrayList<>();
		Set<TLFacetOwner> objectList = SGFinder.findSubstitutionGroupReferences(entity);

		for (TLFacetOwner obj : objectList) {
			SubstitutionGroupItem item = new SubstitutionGroupItem(obj);

			if (item.getAvailableFacets().size() > 1) {
				itemList.add(item);
			}
		}
		return new SubstitutionGroupTableModel(itemList);
	}

	/**
	 * Updates the contents of the visible controls based on information from
	 * the OTM model.
	 */
	private void updateControls() {
		List<OTMObjectItem> selectableObjects = new ArrayList<>();

		// Collect the selectable objects for the combo-box
		if (model != null) {
			for (TLLibrary library : model.getUserDefinedLibraries()) {
				for (TLBusinessObject bo : library.getBusinessObjectTypes()) {
					selectableObjects.add(new OTMObjectItem(bo));
				}
				for (TLCoreObject core : library.getCoreObjectTypes()) {
					selectableObjects.add(new OTMObjectItem(core));
				}
				for (TLChoiceObject choice : library.getChoiceObjectTypes()) {
					selectableObjects.add(new OTMObjectItem(choice));
				}
				for (TLResource resource : library.getResourceTypes()) {
					for (TLActionFacet actionFacet : resource.getActionFacets()) {
						if (actionFacet.getReferenceType() != TLReferenceType.NONE) {
							NamedEntity basePayload = actionFacet.getBasePayload();
							
							if ((basePayload instanceof TLCoreObject)
									|| (basePayload instanceof TLCoreObject)
									|| (actionFacet.getReferenceRepeat() > 1)) {
								selectableObjects.add(new OTMObjectItem(actionFacet));
							}
						}
					}
				}
				if (library.getService() != null) {
					for (TLOperation op : library.getService().getOperations()) {
						if (facetDelegateFactory.getDelegate(op.getRequest()).hasContent()) {
							selectableObjects.add(new OTMObjectItem(op.getRequest()));
						}
						if (facetDelegateFactory.getDelegate(op.getResponse()).hasContent()) {
							selectableObjects.add(new OTMObjectItem(op.getResponse()));
						}
						if (facetDelegateFactory.getDelegate(op.getNotification()).hasContent()) {
							selectableObjects.add(new OTMObjectItem(op.getNotification()));
						}
					}
				}
			}
		}
		
		// Sort the objects in alphabetical order according to their display label
		Collections.sort( selectableObjects, new Comparator<OTMObjectItem>() {
			public int compare(OTMObjectItem item1, OTMObjectItem item2) {
				return item1.toString().compareTo( item2.toString() );
			}
		});
		
		// Perform final updates to the visual controls
		if (model != null) {
			filenameText.setText((modelFile == null) ? "" : modelFile.getName());
		}
		objectCombo.setModel(new DefaultComboBoxModel<>(
				selectableObjects.toArray(new OTMObjectItem[selectableObjects.size()])));

		if (selectableObjects.size() > 0) {
			objectCombo.setSelectedIndex(0);
		}
	}

	/**
	 * Sets the enabled state of all visual controls and optionally assigns a
	 * status message at the bottom of the frame.
	 * 
	 * @param enabled
	 *            flag indicating whether controls should be enabled or disabled
	 * @param statusMessage
	 *            the status message to display (may be null)
	 */
	private void setEnableState(final boolean enabled,
			final String statusMessage) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					filenameText.setEnabled(enabled);
					selectFileButton.setEnabled(enabled);
					bindingStyleCombo.setEnabled(enabled);
					objectCombo.setEnabled(enabled);
					repeatCountSpinner.setEnabled(enabled);
					sgTable.setEnabled(enabled);
					previewScroll.setEnabled(enabled);
					statusLabel.setText((statusMessage == null) ? " " : statusMessage);
				}
			});

		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace(System.out);
		}
	}

	/**
	 * Refreshes the example view when a user selection has changed.
	 */
	private void refreshExample() {
		try {
			if (sgTable.getModel() instanceof SubstitutionGroupTableModel) {
				SubstitutionGroupTableModel tableModel = (SubstitutionGroupTableModel) sgTable.getModel();
				List<SubstitutionGroupItem> tableItems = tableModel.getItems();
				ExampleGeneratorOptions options = new ExampleGeneratorOptions();
				JScrollBar sb = previewScroll.getVerticalScrollBar();
				final int topPreviewLine;

				// Find the top line of the preview pane so we can restore it
				// after the refresh
				if ((sb != null) && (previewLineCount > 0) && (sb.getMaximum() > 0)) {
					topPreviewLine = sb.getValue() / (sb.getMaximum() / previewLineCount);
				} else {
					topPreviewLine = 0;
				}

				// Configure the selections as preferred facets in the example
				// generation options
				for (SubstitutionGroupItem tableItem : tableItems) {
					options.setPreferredFacet(
							tableItem.getSubstitutionObject(),
							tableItem.getSelectedFacet());
				}
				options.setMaxRepeat((Integer) repeatCountSpinner.getValue());

				// Generate the example and update the preview pane
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				TLModelElement element = null;
				String exampleFormat;
				
				if (null == element) {
					element = (TLModelElement) selectedObject;
				}
				if (xmlRadio.isSelected()) {
					ExampleBuilder<Document> builder = new ExampleDocumentBuilder( options ).setModelElement( selectedObject );
					Document domDocument = builder.buildTree();
					
					new XMLPrettyPrinter().formatDocument(domDocument, out);
					exampleFormat = ExamplePane.XML_FORMAT;
					
				} else { // json selected
					ExampleJsonBuilder exampleBuilder = new ExampleJsonBuilder( options );
					ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
					JsonNode node;
					
					exampleBuilder.setModelElement( selectedObject );
					node = exampleBuilder.buildTree();
					mapper.writeValue(out, node);
					exampleFormat = ExamplePane.JSON_FORMAT;
				}
				previewPane.setExampleFormat( exampleFormat );
				previewPane.setExampleContent(new String(out.toByteArray(), "UTF-8"));
				previewLineCount = previewPane.getExampleContent().split("[\n|\r]").length;

				// Restore the scroll position
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JScrollBar sb = previewScroll.getVerticalScrollBar();

						if ((sb != null) && (previewLineCount > 0)) {
							int maxValue = sb.getMaximum();
							final int scrollValue = Math.min(topPreviewLine
									* (maxValue / previewLineCount), maxValue);

							sb.setValue(scrollValue);
						}
					}
				});
			}

		} catch (ValidationException | CodeGenerationException | IOException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(),
					"Example Generation Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Saves the contents of the preview pane to a file selected by the user.
	 */
	private void saveXml() {
		String entityName = (selectedObject == null) ? "unknown" : selectedObject.getLocalName();
		final boolean xmlFormat = xmlRadio.isSelected();
		final String fileExtension = (xmlFormat ? ".xml" : ".json");
		JFileChooser fc = new JFileChooser();
		File exampleFile;

		// Compute the default name and location of the example file
		if (exampleFolder == null) {
			exampleFolder = new File(System.getProperty("user.dir"));
		}
		exampleFile = new File(exampleFolder, entityName + fileExtension);

		// Configure the file chooser
		fc.setSelectedFile(exampleFile);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setFileFilter(new FileFilter() {
			
			private String description = xmlFormat ? "XML Files (*.xml)" : "JSON Files (*.json)";
			
			public String getDescription() {
				return description;
			}

			public boolean accept(File f) {
				String filename = f.getName().toLowerCase();
				return (f.isDirectory() || filename.endsWith( fileExtension ));
			}
		});

		// If approved, save the file
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			boolean canSave = true;

			exampleFile = fc.getSelectedFile();
			exampleFolder = exampleFile.getParentFile();

			if (exampleFile.exists()) {
				canSave = JOptionPane
						.showConfirmDialog(this,
								"A file with the same name already exists.  Overwrite?",
								"Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
			}
			if (canSave) {
				try (Writer writer = new FileWriter(exampleFile)) {
					writer.write(previewPane.getExampleContent());

				} catch (IOException e) {
					JOptionPane.showMessageDialog(this, e.getMessage(),
							"Save Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
	}

	/**
	 * Called when the frame window is closing to save appliction settings for
	 * the next session.
	 */
	private void saveUserSettings() {
		UserSettings settings = new UserSettings();

		settings.setWindowPosition(getLocation());
		settings.setWindowSize(getSize());
		settings.setRepeatCount((Integer) repeatCountSpinner.getValue());
		settings.setLastModelFile(modelFile);
		settings.setLastExampleFolder(exampleFolder);
		settings.save();
	}
	
	/**
	 * Returns a display name label for the given OTM entity.
	 * 
	 * @param entity  the entity for which to return a display name
	 * @return String
	 */
	private static String getDisplayName(NamedEntity entity, boolean showVersion) {
		StringBuilder displayName = new StringBuilder();
		String objectVersion = null;
		
		if (showVersion) {
			try {
				TLLibrary library = (TLLibrary) entity.getOwningLibrary();
				VersionScheme vScheme = VersionSchemeFactory.getInstance().getVersionScheme( library.getVersionScheme() );
				
				objectVersion = vScheme.getVersionIdentifier( library.getNamespace() );
				
			} catch (VersionSchemeException e) {
				objectVersion = "?";
			}
		}
		displayName.append( XsdCodegenUtils.getGlobalElementName(entity).getLocalPart() );
		
		if (objectVersion != null) {
			displayName.append( " (" ).append( objectVersion ).append( ")" ).toString();
		}
		return displayName.toString();
	}

	/**
	 * Initializes the visible controls of the frame.
	 */
	private void init() {
		Container contentPane = getContentPane();
		JPanel leftPanel = new JPanel();
		JPanel rightPanel = new JPanel();
		GridBagConstraints gbc = null;

		// Construct the left side panel that contains all of the user-editable
		// controls
		filenameText = new JTextField(20);
		filenameText.setEditable(false);
		selectFileButton = new JButton("...");
		bindingStyleCombo = new JComboBox<>();
		objectCombo = new JComboBox<>();
		repeatCountSpinner = new JSpinner();
		repeatCountSpinner.setModel(new SpinnerNumberModel(2, 1, 3, 1));
		repeatCountSpinner.setPreferredSize(new Dimension(40, 25));
		TableModel tableModel = new DefaultTableModel();
		TableColumnModel columnModel = new DefaultTableColumnModel();
		TableColumn sgObjectColumn = new TableColumn(0);
		TableColumn sgFacetColumn = new TableColumn(1);
		sgObjectColumn.setHeaderValue("Object Type");
		sgFacetColumn.setHeaderValue("Facet Selection");
		sgFacetColumn.setCellEditor(new FacetCellEditor());
		columnModel.addColumn(sgObjectColumn);
		columnModel.addColumn(sgFacetColumn);
		sgTable = new JTable(tableModel, columnModel);
		sgTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		sgTable.setPreferredScrollableViewportSize(new Dimension(350, 200));
		sgTable.setCellSelectionEnabled(false);
		sgTable.setRowSelectionAllowed(true);

		leftPanel.setLayout(new GridBagLayout());
		leftPanel.add(new JLabel("OTM Library:"), newGBC(0, 0, 1, GridBagConstraints.LINE_START));
		leftPanel.add(filenameText, newGBC(1, 0, 1, GridBagConstraints.LINE_START));
		leftPanel.add(selectFileButton, newGBC(2, 0, 1, GridBagConstraints.LINE_START));
		leftPanel.add(new JLabel("Binding Style:"), newGBC(0, 1, 1, GridBagConstraints.LINE_START));
		gbc = newGBC(1, 1, 2, GridBagConstraints.LINE_START);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		leftPanel.add(bindingStyleCombo, gbc);
		leftPanel.add(new JLabel("OTM Object:"), newGBC(0, 2, 1, GridBagConstraints.LINE_START));
		gbc = newGBC(1, 2, 2, GridBagConstraints.LINE_START);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		leftPanel.add(objectCombo, gbc);
		leftPanel.add(new JLabel("Repeat Count:"), newGBC(0, 3, 1, GridBagConstraints.LINE_START));
		leftPanel.add(repeatCountSpinner, newGBC(1, 3, 1, GridBagConstraints.LINE_START));
		
		xmlRadio = new JRadioButton("XML");
		xmlRadio.setSelected(true);
		xmlRadio.setBounds(0, 0, 1, 1);
		jsonRadio = new JRadioButton("JSON");
		ButtonGroup bG = new ButtonGroup();
		bG.add(xmlRadio);
		bG.add(jsonRadio);
		leftPanel.add(new JLabel("Display Type:"),
				newGBC(0, 4, 1, GridBagConstraints.LINE_START));
		Box radioBox = Box.createHorizontalBox();
		radioBox.add(xmlRadio);
		radioBox.add(jsonRadio);
		leftPanel.add(radioBox, newGBC(1, 4, 1, GridBagConstraints.LINE_START));
		
		leftPanel.add(new JLabel("Substitution Groups:"), newGBC(0, 5, 3, GridBagConstraints.LINE_START));
		gbc = newGBC(0, 6, 3, GridBagConstraints.LINE_START);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = Integer.MAX_VALUE;
		gbc.insets.left += 10;
		JScrollPane sgScroll = new JScrollPane(sgTable);
		leftPanel.add(sgScroll, gbc);

		// Construct the right-side panel that contains the XML example preview
		// pane
		JButton saveButton = new JButton("Save...");

		previewPane = new ExamplePane();
		previewPane.setEditable(false);
		previewPane.setBorder(LineBorder.createBlackLineBorder());
		previewScroll = new JScrollPane(previewPane);
		rightPanel.setLayout(new BorderLayout(4, 4));
		rightPanel.add(new JLabel("Example Preview:"), BorderLayout.PAGE_START);
		rightPanel.add(saveButton, BorderLayout.PAGE_END);
		rightPanel.add(previewScroll, BorderLayout.CENTER);

		// Add listeners to all visible controls that require them
		selectFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectLibraryFile();
			}
		});
		bindingStyleCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectBindingStyle();
			}
		});
		objectCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectOtmObject();
			}
		});
		repeatCountSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				refreshExample();
			}
		});
		xmlRadio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshExample();
			}
		});
		jsonRadio.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshExample();
			}
		});
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveXml();
			}
		});
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				saveUserSettings();
			}

		});

		// Finish up and return
		UserSettings userSettings = UserSettings.load();

		contentPane.add(leftPanel, BorderLayout.LINE_START);
		contentPane.add(rightPanel, BorderLayout.CENTER);
		contentPane.add(statusLabel = new JLabel(" "), BorderLayout.PAGE_END);

		repeatCountSpinner.setValue(userSettings.getRepeatCount());
		modelFile = userSettings.getLastModelFile();
		exampleFolder = userSettings.getLastExampleFolder();

		pack();
		setTitle("OTM-DE Example Helper");
		initScreenPosition(userSettings);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		initBindingStyles();
		updateControls(); // initial configuration
	}

	/**
	 * Assigns the screen position and window size of the frame using
	 * information provided in the <code>UserSettings</code>.
	 * 
	 * @param settings
	 *            the user settings from the last application session
	 */
	private void initScreenPosition(UserSettings settings) {
		Rectangle screenBounds = getGraphicsConfiguration().getBounds();
		Point position = settings.getWindowPosition();
		Dimension size = settings.getWindowSize();
		Rectangle windowBounds = new Rectangle(position.x, position.y,
				size.width, size.height);

		if (!screenBounds.contains(windowBounds)) {
			position = new Point(0, 0);
			size = UserSettings.getDefaultSettings().getWindowSize();
		}
		setLocation(position);
		setSize(size);
	}

	/**
	 * Populates the combo-box with the available binding styles.
	 */
	private void initBindingStyles() {
		List<String> bindingStyles = CompilerExtensionRegistry
				.getAvailableExtensionIds();

		bindingStyleCombo.setModel(new DefaultComboBoxModel<>(bindingStyles
				.toArray(new String[bindingStyles.size()])));
		bindingStyleCombo.setSelectedIndex(0);
	}

	/**
	 * Convenience method for creating common <code>GridBagConstraints</code>.
	 */
	private GridBagConstraints newGBC(int gridX, int gridY, int gridWidth,
			int anchor) {
		GridBagConstraints gbc = new GridBagConstraints();

		gbc.gridx = gridX;
		gbc.gridy = gridY;
		gbc.gridwidth = gridWidth;
		gbc.anchor = anchor;
		gbc.insets = new Insets(2, 2, 2, 2);
		return gbc;
	}

	/**
	 * Main method invoked from the command-line. If arguments are provided, the
	 * first one will be assumed to be the file location of an OTM library or
	 * project. All other command-line arguments are ignored.
	 * 
	 * @param args
	 *            the command-line arguments
	 */
	public static void main(String[] args) {
		try {
			ExampleHelper frame = new ExampleHelper();

			frame.setVisible(true);

			if (args.length > 0) {
				frame.setLibraryFile(new File(args[0]));
			}

		} catch (Throwable t) {
			t.printStackTrace(System.out);
		}
	}

	/**
	 * Provides a list item for a combo-box that can be used to display and
	 * select OTM objects.
	 */
	private static class OTMObjectItem {

		private NamedEntity otmObject;

		/**
		 * Constructor that provides the OTM object.
		 * 
		 * @param otmObject
		 *            the OTM object instance
		 */
		public OTMObjectItem(NamedEntity otmObject) {
			this.otmObject = otmObject;
		}

		/**
		 * Returns the OTM object instance.
		 *
		 * @return NamedEntity
		 */
		public NamedEntity getOtmObject() {
			return otmObject;
		}

		/**
		 * Returns the display name for the OTM object in the combo-box.
		 *
		 * @return String
		 */
		public String toString() {
			return getDisplayName( otmObject, true );
		}

	}

	/**
	 * Represents a single item that can be configured in the substitution group
	 * table.
	 */
	private static class SubstitutionGroupItem {

		private TLFacetOwner substitutionObject;
		private List<TLFacet> availableFacets;
		private JComboBox<String> facetCombo;
		private TLFacet selectedFacet;

		public SubstitutionGroupItem(TLFacetOwner substitutionObject) {
			this.substitutionObject = substitutionObject;

			if (substitutionObject instanceof TLBusinessObject) {
				TLBusinessObject bo = (TLBusinessObject) substitutionObject;

				this.selectedFacet = bo.getSummaryFacet();
				this.availableFacets = getAvailableFacets(bo);

			} else if (substitutionObject instanceof TLCoreObject) {
				TLCoreObject core = (TLCoreObject) substitutionObject;

				this.selectedFacet = core.getSummaryFacet();
				this.availableFacets = getAvailableFacets(core);
				
			} else if (substitutionObject instanceof TLChoiceObject) {
				TLChoiceObject choice = (TLChoiceObject) substitutionObject;

				this.availableFacets = getAvailableFacets(choice);
				this.selectedFacet = this.availableFacets.isEmpty()
						? null : this.availableFacets.get( 0 );
			}
			this.facetCombo = new JComboBox<>(
					getFacetLabels(this.availableFacets));
		}

		/**
		 * Returns the list of available facets for the substitution group.
		 * 
		 * @param businessObject
		 *            the business object for which to return available facets
		 * @return List<TLFacet>
		 */
		private List<TLFacet> getAvailableFacets(TLBusinessObject businessObject) {
			List<TLFacet> facetList = new ArrayList<>();

			addIfContentExists(businessObject.getIdFacet(), facetList);
			addIfContentExists(businessObject.getSummaryFacet(), facetList);
			addIfContentExists(businessObject.getDetailFacet(), facetList);
			
			for (TLFacet facet : businessObject.getCustomFacets()) {
				addIfContentExists(facet, facetList);
			}
	        for (TLFacet ghostFacet :
	        	FacetCodegenUtils.findGhostFacets(businessObject, TLFacetType.CUSTOM)) {
				addIfContentExists(ghostFacet, facetList);
	        }
			return facetList;
		}

		/**
		 * Returns the list of available facets for the substitution group.
		 * 
		 * @param coreObject
		 *            the core object for which to return available facets
		 * @return List<TLFacet>
		 */
		private List<TLFacet> getAvailableFacets(TLCoreObject coreObject) {
			List<TLFacet> facetList = new ArrayList<>();

			addIfContentExists(coreObject.getSummaryFacet(), facetList);
			addIfContentExists(coreObject.getDetailFacet(), facetList);
			return facetList;
		}

		/**
		 * Returns the list of available facets for the substitution group.
		 * 
		 * @param choiceObject
		 *            the choice object for which to return available facets
		 * @return List<TLFacet>
		 */
		private List<TLFacet> getAvailableFacets(TLChoiceObject choiceObject) {
			List<TLFacet> facetList = new ArrayList<>();

			for (TLFacet facet : choiceObject.getChoiceFacets()) {
				addIfContentExists(facet, facetList);
			}
	        for (TLFacet ghostFacet :
	        	FacetCodegenUtils.findGhostFacets(choiceObject, TLFacetType.CHOICE)) {
				addIfContentExists(ghostFacet, facetList);
	        }
			return facetList;
		}

		/**
		 * Returns the display labels for each of the facets in the list
		 * provided.
		 * 
		 * @param facetList
		 *            the list of facets
		 * @return String[]
		 */
		private String[] getFacetLabels(List<TLFacet> facetList) {
			List<String> facetLabels = new ArrayList<>();

			for (TLFacet facet : facetList) {
				facetLabels.add( getDisplayName( facet, false ) );
			}
			return facetLabels.toArray(new String[facetLabels.size()]);
		}

		/**
		 * If the given facet declares or inherits fields, this method will add
		 * it to the list provided.
		 * 
		 * @param facet
		 *            the facet to verify and add
		 * @param facetList
		 *            the list of facets to which the given one may be appended
		 */
		private void addIfContentExists(TLFacet facet, List<TLFacet> facetList) {
			if (facetDelegateFactory.getDelegate(facet).hasContent()) {
				facetList.add(facet);
			}
		}

		/**
		 * Returns the value of the 'selectedFacet' field.
		 *
		 * @return TLFacet
		 */
		public TLFacet getSelectedFacet() {
			return selectedFacet;
		}

		/**
		 * Assigns the value of the 'selectedFacet' field.
		 *
		 * @param selectedFacet
		 *            the field value to assign
		 */
		public void setSelectedFacet(TLFacet selectedFacet) {
			this.selectedFacet = selectedFacet;
		}

		/**
		 * Returns the list of available facets that can be selected for the
		 * substitution group.
		 *
		 * @return List<TLFacet>
		 */
		public List<TLFacet> getAvailableFacets() {
			return availableFacets;
		}

		/**
		 * Returns the combo-box control that should be used to select a facet
		 * for the substitution group.
		 *
		 * @return JComboBox<String>
		 */
		public JComboBox<String> getFacetCombo() {
			return facetCombo;
		}

		/**
		 * Returns the value of the 'substitutionObject' field.
		 *
		 * @return TLFacetOwner
		 */
		public TLFacetOwner getSubstitutionObject() {
			return substitutionObject;
		}

	}

	/**
	 * Table model used to view and edit the contents of the substitution group
	 * selections.
	 */
	private class SubstitutionGroupTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 4026779411130199292L;

		private List<SubstitutionGroupItem> items;

		/**
		 * Constructor that supplies the list of items to be displayed and
		 * edited.
		 * 
		 * @param items
		 *            the list of substitution group items
		 */
		public SubstitutionGroupTableModel(List<SubstitutionGroupItem> items) {
			this.items = items;
		}

		/**
		 * Returns the list of items for the table.
		 *
		 * @return List<SubstitutionGroupItem>
		 */
		public List<SubstitutionGroupItem> getItems() {
			return items;
		}

		/**
		 * @see javax.swing.table.DefaultTableModel#getRowCount()
		 */
		@Override
		public int getRowCount() {
			return (items == null) ? 0 : items.size();
		}

		/**
		 * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
		 */
		@Override
		public Object getValueAt(int row, int column) {
			SubstitutionGroupItem item = null;
			Object value = null;

			if (row < items.size()) {
				item = items.get(row);

				if (column == 0) {
					value = getDisplayName( item.getSubstitutionObject(), true );
				} else {
					value = getDisplayName( item.getSelectedFacet(), false );
				}
			}
			return value;
		}

		/**
		 * @see javax.swing.table.DefaultTableModel#setValueAt(java.lang.Object,
		 *      int, int)
		 */
		@Override
		public void setValueAt(Object aValue, int row, int column) {
			if (row < items.size()) {
				SubstitutionGroupItem item = items.get(row);
				int selectedIndex = item.getFacetCombo().getSelectedIndex();
				TLFacet selectedFacet = item.getAvailableFacets().get(
						selectedIndex);

				item.setSelectedFacet(selectedFacet);
				refreshExample();
			}
		}

	}

	/**
	 * Cell editor that provides a different combo-box depending on which row is
	 * being edited.
	 */
	private class FacetCellEditor extends AbstractCellEditor implements
			TableCellEditor {

		private static final long serialVersionUID = 8826121666000658618L;

		private JComboBox<String> facetCombo;

		/**
		 * @see javax.swing.CellEditor#getCellEditorValue()
		 */
		@Override
		public Object getCellEditorValue() {
			return (facetCombo == null) ? null : facetCombo.getSelectedItem();
		}

		/**
		 * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable,
		 *      java.lang.Object, boolean, int, int)
		 */
		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			SubstitutionGroupTableModel tableModel = (SubstitutionGroupTableModel) sgTable
					.getModel();
			List<SubstitutionGroupItem> tableItems = tableModel.getItems();

			if (row >= 0) {
				SubstitutionGroupItem selectedItem = tableItems.get(row);

				if (selectedItem != null) {
					facetCombo = selectedItem.getFacetCombo();
				}
			}
			return facetCombo;
		}

	}

}
