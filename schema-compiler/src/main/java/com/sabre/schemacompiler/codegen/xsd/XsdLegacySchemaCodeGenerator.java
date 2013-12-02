/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.xsd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import com.sabre.schemacompiler.codegen.CodeGenerationContext;
import com.sabre.schemacompiler.codegen.CodeGenerationException;
import com.sabre.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import com.sabre.schemacompiler.codegen.CodeGenerationFilter;
import com.sabre.schemacompiler.codegen.impl.AbstractCodeGenerator;
import com.sabre.schemacompiler.codegen.impl.LibraryFilenameBuilder;
import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.XSDLibrary;
import com.sabre.schemacompiler.util.URLUtils;

/**
 * Code generator for legacy XML schemas referenced in a library meta-model.  The behavior of
 * this code generator is to simply copy the content of the file from its source URL to the
 * proper output location.
 * 
 * @author S. Livezey
 */
public class XsdLegacySchemaCodeGenerator extends AbstractCodeGenerator<XSDLibrary>{
	
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	/**
	 * @see com.sabre.schemacompiler.codegen.impl.AbstractCodeGenerator#getLibrary(java.lang.Object)
	 */
	@Override
	protected AbstractLibrary getLibrary(XSDLibrary source) {
		return source;
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.impl.AbstractCodeGenerator#doGenerateOutput(java.lang.Object, com.sabre.schemacompiler.codegen.CodeGenerationContext)
	 */
	@Override
	public void doGenerateOutput(XSDLibrary source, CodeGenerationContext context) throws CodeGenerationException {
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			File outputFile = getOutputFile(source, context);
			String line = null;
			
			if (URLUtils.isFileURL(source.getLibraryUrl())) {
				reader = new BufferedReader( new FileReader( URLUtils.toFile(source.getLibraryUrl()) ) );
			} else {
				reader = new BufferedReader( new InputStreamReader( source.getLibraryUrl().openStream() ) );
			}
			writer = new BufferedWriter( new FileWriter(outputFile) );
			
			while ((line = reader.readLine()) != null) {
				writer.write(line);
				writer.write(LINE_SEPARATOR);
			}
			addGeneratedFile(outputFile);
			
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
	 * @see com.sabre.schemacompiler.codegen.impl.AbstractCodeGenerator#getOutputFile(com.sabre.schemacompiler.model.TLModelElement, com.sabre.schemacompiler.codegen.CodeGenerationContext)
	 */
	@Override
	protected File getOutputFile(XSDLibrary source, CodeGenerationContext context) {
		File outputFolder = getOutputFolder(context, source.getLibraryUrl());
		String filename = context.getValue(CodeGenerationContext.CK_SCHEMA_FILENAME);
		
		if ((filename == null) || filename.trim().equals("")) {
			filename = getFilenameBuilder().buildFilename(source, "xsd");
		}
		return new File(outputFolder, filename);
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.impl.AbstractCodeGenerator#getOutputFolder(com.sabre.schemacompiler.codegen.CodeGenerationContext, java.net.URL)
	 */
	@Override
	protected File getOutputFolder(CodeGenerationContext context, URL libraryUrl) {
		File outputFolder = super.getOutputFolder(context, libraryUrl);
		String legacySchemaFolder = getLegacySchemaOutputLocation(context);
		
		if (legacySchemaFolder != null) {
			outputFolder = new File(outputFolder, legacySchemaFolder);
			if (!outputFolder.exists()) outputFolder.mkdirs();
		}
		return outputFolder;
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.impl.AbstractCodeGenerator#isSupportedSourceObject(java.lang.Object)
	 */
	@Override
	protected boolean isSupportedSourceObject(XSDLibrary source) {
		return (source != null);
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.impl.AbstractCodeGenerator#canGenerateOutput(java.lang.Object, com.sabre.schemacompiler.codegen.CodeGenerationContext, com.sabre.schemacompiler.validate.ValidationFindings)
	 */
	@Override
	protected boolean canGenerateOutput(XSDLibrary source, CodeGenerationContext context) {
		CodeGenerationFilter filter = getFilter();
		
		return super.canGenerateOutput(source, context)
				&& ((filter == null) || filter.processLibrary(source) || filter.processExtendedLibrary(source));
	}

	/**
	 * @see com.sabre.schemacompiler.codegen.impl.AbstractCodeGenerator#getDefaultFilenameBuilder()
	 */
	@Override
	protected CodeGenerationFilenameBuilder<XSDLibrary> getDefaultFilenameBuilder() {
		return new LibraryFilenameBuilder<XSDLibrary>();
	}

}
