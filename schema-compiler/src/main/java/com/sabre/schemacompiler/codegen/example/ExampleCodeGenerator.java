/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;

import org.w3c.dom.Document;

import com.sabre.schemacompiler.codegen.CodeGenerationContext;
import com.sabre.schemacompiler.codegen.CodeGenerationException;
import com.sabre.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import com.sabre.schemacompiler.codegen.example.ExampleGeneratorOptions.DetailLevel;
import com.sabre.schemacompiler.codegen.impl.AbstractCodeGenerator;
import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.LibraryElement;
import com.sabre.schemacompiler.model.NamedEntity;
import com.sabre.schemacompiler.model.TLFacet;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLModel;
import com.sabre.schemacompiler.model.TLModelElement;
import com.sabre.schemacompiler.model.TLOperation;
import com.sabre.schemacompiler.model.TLService;
import com.sabre.schemacompiler.model.XSDLibrary;
import com.sabre.schemacompiler.xml.XMLPrettyPrinter;

/**
 * Code generator that produces sample XML output for <code>NamedEntity</code> members of the generated libraries.
 * 
 * @author S. Livezey
 */
public class ExampleCodeGenerator extends AbstractCodeGenerator<TLModelElement> {

	/**
	 * @see com.sabre.schemacompiler.codegen.impl.AbstractCodeGenerator#doGenerateOutput(com.sabre.schemacompiler.model.TLModelElement, com.sabre.schemacompiler.codegen.CodeGenerationContext)
	 */
	@Override
	public void doGenerateOutput(TLModelElement source, CodeGenerationContext context) throws CodeGenerationException {
		OutputStream out = null;
		try {
			ExampleDocumentBuilder exampleBuilder =
					new ExampleDocumentBuilder( getOptions(context) ).setModelElement((NamedEntity) source);
			
			// Register the schema location for each library in the model
			registerSchemaLocations( exampleBuilder, source.getOwningModel(), context );
			
			// Generate the XML document and send formatted content to the output file
			Document domDocument = exampleBuilder.buildDomTree();
			File outputFile = getOutputFile(source, context);
			
			out = new FileOutputStream(outputFile);
			new XMLPrettyPrinter().formatDocument(domDocument, out);
			out.close();
			out = null;
			
			addGeneratedFile(outputFile);
			
		} catch (Throwable t) {
			throw new CodeGenerationException(t);
			
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (Throwable t) {}
		}
	}
	
	/**
	 * Constructs the <code>ExampleGeneratorOptions</code> using the code generation context provided.
	 * 
	 * @param context  the code generation context
	 * @return ExampleGeneratorOptions
	 */
	private ExampleGeneratorOptions getOptions(CodeGenerationContext context) {
		ExampleGeneratorOptions options = new ExampleGeneratorOptions();
		String detailLevel = context.getValue(CodeGenerationContext.CK_EXAMPLE_DETAIL_LEVEL);
		String exampleContext = context.getValue(CodeGenerationContext.CK_EXAMPLE_CONTEXT);
		Integer maxRepeat = context.getIntValue(CodeGenerationContext.CK_EXAMPLE_MAX_REPEAT);
		Integer maxDepth = context.getIntValue(CodeGenerationContext.CK_EXAMPLE_MAX_DEPTH);
		
		if (detailLevel != null) {
			if (detailLevel.equalsIgnoreCase("MINIMUM")) {
				options.setDetailLevel(DetailLevel.MINIMUM);
			}
		}
		if (exampleContext != null) {
			options.setExampleContext(exampleContext);
		}
		if (maxRepeat != null) {
			options.setMaxRepeat(maxRepeat.intValue());
		}
		if (maxDepth != null) {
			options.setMaxRecursionDepth(maxDepth.intValue());
		}
		return options;
	}
	
	/**
	 * Registers schema output locations for all libraries in the specified model.
	 * 
	 * @param builder  the builder for which schema locations should be registered
	 * @param model  the model containing all possible libraries to be resolved
	 * @param context  the code generation context
	 */
	private void registerSchemaLocations(ExampleDocumentBuilder builder, TLModel model, CodeGenerationContext context) {
		if (model != null) {
			// Register the schema locations of all libraries
			for (AbstractLibrary library : model.getAllLibraries()) {
				String schemaPath = context.getValue(CodeGenerationContext.CK_EXAMPLE_SCHEMA_RELATIVE_PATH);
				String schemaFilename = getFilenameBuilder().buildFilename(library, "xsd");
				String schemaLocation;

				if (library instanceof TLLibrary) {
					schemaLocation = schemaPath + schemaFilename;

				} else if (library instanceof XSDLibrary) {
					schemaLocation = schemaPath + getLegacySchemaOutputLocation(context) + "/" + schemaFilename;

				} else { // Built-in library
					schemaLocation = schemaPath + getBuiltInSchemaOutputLocation(context) + "/" + schemaFilename;
				}
				builder.addSchemaLocation(library.getNamespace(), schemaLocation);
			}
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.codegen.impl.AbstractCodeGenerator#getOutputFile(com.sabre.schemacompiler.model.TLModelElement, com.sabre.schemacompiler.codegen.CodeGenerationContext)
	 */
	@Override
	protected File getOutputFile(TLModelElement source, CodeGenerationContext context) {
		if (source == null) {
			throw new NullPointerException("Source model element cannot be null.");
		}
		AbstractLibrary library = getLibrary(source);
		URL libraryUrl = (library == null) ? null : library.getLibraryUrl();
		File outputFolder = getOutputFolder(context, libraryUrl);
		String filename = getFilenameBuilder().buildFilename(source, "xml");
		
		return new File(outputFolder, filename);
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.impl.AbstractCodeGenerator#getDefaultFilenameBuilder()
	 */
	@Override
	protected CodeGenerationFilenameBuilder<TLModelElement> getDefaultFilenameBuilder() {
		return new CodeGenerationFilenameBuilder<TLModelElement>() {

			public String buildFilename(TLModelElement item, String fileExtension) {
				String fileExt = ((fileExtension == null) || (fileExtension.length() == 0)) ? "" : ("." + fileExtension);
				String itemName;
				
				if ((item instanceof TLFacet) && (((TLFacet) item).getOwningEntity() instanceof TLOperation)) {
					TLFacet facetItem = (TLFacet) item;
					itemName = ((TLOperation) facetItem.getOwningEntity()).getName() + facetItem.getFacetType().getIdentityName();
					
				} else if (item instanceof NamedEntity) {
					itemName = ((NamedEntity) item).getLocalName();
				} else {
					itemName = "";
				}
				return itemName.replaceAll("_", "") + fileExt;
			}
			
		};
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.impl.AbstractCodeGenerator#getLibrary(com.sabre.schemacompiler.model.TLModelElement)
	 */
	@Override
	protected AbstractLibrary getLibrary(TLModelElement source) {
		return (source instanceof LibraryElement) ? ((LibraryElement) source).getOwningLibrary() : null;
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.impl.AbstractCodeGenerator#isSupportedSourceObject(com.sabre.schemacompiler.model.TLModelElement)
	 */
	@Override
	protected boolean isSupportedSourceObject(TLModelElement source) {
		return (source instanceof NamedEntity) && !(source instanceof TLService);
	}
	
}
