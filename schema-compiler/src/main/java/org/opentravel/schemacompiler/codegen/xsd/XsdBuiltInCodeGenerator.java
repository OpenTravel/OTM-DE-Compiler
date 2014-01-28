/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.codegen.xsd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationException;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator;
import org.opentravel.schemacompiler.codegen.impl.CodegenNamespacePrefixMapper;
import org.opentravel.schemacompiler.codegen.impl.LibraryFilenameBuilder;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.validate.ValidationException;

/**
 * Code generator for built-in XML schemas referenced in a library meta-model.  The behavior of
 * this code generator is to simply copy the content of the file from its source location in the
 * local classpath to the proper output location.
 * 
 * @author S. Livezey
 */
public class XsdBuiltInCodeGenerator extends AbstractCodeGenerator<BuiltInLibrary> {

	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	private ImportSchemaLocations importSchemaLocations;
	
	/**
	 * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getLibrary(java.lang.Object)
	 */
	@Override
	protected AbstractLibrary getLibrary(BuiltInLibrary source) {
		return source;
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#doGenerateOutput(java.lang.Object, org.opentravel.schemacompiler.codegen.CodeGenerationContext)
	 */
	@Override
	public void doGenerateOutput(BuiltInLibrary source, CodeGenerationContext context) throws CodeGenerationException {
		switch (source.getBuiltInType()) {
			case TLLIBRARY_BUILTIN:
				generateUserLibraryOutput(source, context);
				break;
			case XSD_BUILTIN:
				generateXsdLibraryOutput(source, context);
				break;
			default:
				// skip code generation of the schema-for-schemas built-in
		}
	}
	
	/**
	 * Generates output for user-defined (<code>TLLibrary</code>) built-in libraries.
	 * 
	 * @param source  the built-in library for which to generate output
	 * @param context  the code generation context
	 * @throws CodeGenerationException  thrown if an error occurs during code generation
	 */
	protected void generateUserLibraryOutput(BuiltInLibrary source, CodeGenerationContext context) throws CodeGenerationException {
		try {
			XsdUserBuiltInCodeGenerator delegateCodeGenerator = new XsdUserBuiltInCodeGenerator(this);
			
			delegateCodeGenerator.setFilenameBuilder(getFilenameBuilder());
			delegateCodeGenerator.setImportSchemaLocations(importSchemaLocations);
			addGeneratedFiles( delegateCodeGenerator.generateOutput(source, context) );
			
		} catch (ValidationException e) {
			throw new CodeGenerationException("Validation error encountered while generating schema for built-in library.");
		}
	}
	
	/**
	 * Generates output for XSD built-in libraries.
	 * 
	 * @param source  the built-in library for which to generate output
	 * @param context  the code generation context
	 * @throws CodeGenerationException  thrown if an error occurs during code generation
	 */
	protected void generateXsdLibraryOutput(BuiltInLibrary source, CodeGenerationContext context) throws CodeGenerationException {
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			if (!source.getNamespace().equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)) {
				File outputFile = getOutputFile(source, context);
				String line = null;
				
				reader = new BufferedReader( new InputStreamReader( source.getSchemaDeclaration().getContent() ) );
				writer = new BufferedWriter( new FileWriter(outputFile) );
				
				while ((line = reader.readLine()) != null) {
					writer.write(line);
					writer.write(LINE_SEPARATOR);
				}
				addGeneratedFile(outputFile);
			}
		} catch (IOException e) {
			throw new CodeGenerationException(e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (Throwable t) {}
			}
			if (writer != null) {
				try {
					writer.close();
				} catch (Throwable t) {}
			}
		}
	}

	/**
	 * Returns the schema locations to use when generating import declarations for the output schema.  If
	 * no schema locations are explicitly assigned for the code generator, default locations will be assumed
	 * for each of the import declarations.
	 * 
	 * @return ImportSchemaLocations
	 */
	public ImportSchemaLocations getImportSchemaLocations() {
		return importSchemaLocations;
	}
	
	/**
	 * Assigns the schema locations to use when generating import declarations for the output schema.  If
	 * no schema locations are explicitly assigned for the code generator, default locations will be assumed
	 * for each of the import declarations.
	 * 
	 * @param importSchemaLocations  the collection of import schema locations
	 */
	public void setImportSchemaLocations(ImportSchemaLocations importSchemaLocations) {
		this.importSchemaLocations = importSchemaLocations;
	}
	
	/**
	 * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getOutputFile(org.opentravel.schemacompiler.model.TLModelElement, org.opentravel.schemacompiler.codegen.CodeGenerationContext)
	 */
	@Override
	protected File getOutputFile(BuiltInLibrary source, CodeGenerationContext context) {
		File outputFolder = getOutputFolder(context, null);
		String filename = context.getValue(CodeGenerationContext.CK_SCHEMA_FILENAME);
		
		if ((filename == null) || filename.trim().equals("")) {
			filename = getFilenameBuilder().buildFilename(source, "xsd");
		}
		return new File(outputFolder, filename);
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getOutputFolder(org.opentravel.schemacompiler.codegen.CodeGenerationContext, java.net.URL)
	 */
	@Override
	protected File getOutputFolder(CodeGenerationContext context, URL libraryUrl) {
		File outputFolder = super.getOutputFolder(context, libraryUrl);
		String builtInSchemaFolder = getBuiltInSchemaOutputLocation(context);
		
		if (builtInSchemaFolder != null) {
			outputFolder = new File(outputFolder, builtInSchemaFolder);
			if (!outputFolder.exists()) outputFolder.mkdirs();
		}
		return outputFolder;
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#isSupportedSourceObject(java.lang.Object)
	 */
	@Override
	protected boolean isSupportedSourceObject(BuiltInLibrary source) {
		return (source != null);
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#canGenerateOutput(org.opentravel.schemacompiler.model.TLModelElement, org.opentravel.schemacompiler.codegen.CodeGenerationContext)
	 */
	@Override
	protected boolean canGenerateOutput(BuiltInLibrary source, CodeGenerationContext context) {
		CodeGenerationFilter filter = getFilter();
		
		return super.canGenerateOutput(source, context)
				&& ((filter == null) || filter.processLibrary(source));
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getDefaultFilenameBuilder()
	 */
	@Override
	protected CodeGenerationFilenameBuilder<BuiltInLibrary> getDefaultFilenameBuilder() {
		return new LibraryFilenameBuilder<BuiltInLibrary>();
	}
	
	
	/**
	 * Handles the generation of XSD output for user-defined (<code>TLLibrary</code>) built-ins on behalf of
	 * the owning built-in code generator.
	 *
	 * @author S. Livezey
	 */
	private static class XsdUserBuiltInCodeGenerator extends AbstractXsdCodeGenerator<BuiltInLibrary> {
		
		private XsdBuiltInCodeGenerator builtInCodeGenerator;
		
		/**
		 * Constructor that assigns the owning built-in code generator.
		 * 
		 * @param builtInCodeGenerator  the built-in code generator that owns this instance
		 */
		public XsdUserBuiltInCodeGenerator(XsdBuiltInCodeGenerator builtInCodeGenerator) {
			this.builtInCodeGenerator = builtInCodeGenerator;
		}
		
		/**
		 * @see org.opentravel.schemacompiler.codegen.impl.AbstractJaxbCodeGenerator#getMarshaller(org.opentravel.schemacompiler.model.TLModelElement, org.w3._2001.xmlschema.Schema)
		 */
		@Override
		protected Marshaller getMarshaller(BuiltInLibrary source, org.w3._2001.xmlschema.Schema schema) throws JAXBException {
			Marshaller m = jaxbContext.createMarshaller();
			
			m.setSchema(validationSchema);
			m.setProperty("com.sun.xml.bind.namespacePrefixMapper",
					new CodegenNamespacePrefixMapper(source, false, this, schema));
			return m;
		}

		/**
		 * @see org.opentravel.schemacompiler.codegen.xsd.AbstractXsdCodeGenerator#getOutputFile(org.opentravel.schemacompiler.model.TLModelElement, org.opentravel.schemacompiler.codegen.CodeGenerationContext)
		 */
		@Override
		protected File getOutputFile(BuiltInLibrary source, CodeGenerationContext context) {
			return builtInCodeGenerator.getOutputFile(source, context);
		}

		/**
		 * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getDefaultFilenameBuilder()
		 */
		@Override
		protected CodeGenerationFilenameBuilder<BuiltInLibrary> getDefaultFilenameBuilder() {
			return new LibraryFilenameBuilder<BuiltInLibrary>();
		}

		/**
		 * @see org.opentravel.schemacompiler.codegen.impl.AbstractCodeGenerator#getLibrary(java.lang.Object)
		 */
		@Override
		protected AbstractLibrary getLibrary(BuiltInLibrary source) {
			return source;
		}

	}
	
}
