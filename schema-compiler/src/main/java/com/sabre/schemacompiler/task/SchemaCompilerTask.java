/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.task;

import java.io.File;
import java.util.Collection;

import com.sabre.schemacompiler.codegen.CodeGenerationContext;
import com.sabre.schemacompiler.codegen.impl.LibraryFilenameBuilder;
import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.LibraryMember;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.XSDLibrary;
import com.sabre.schemacompiler.util.SchemaCompilerException;

/**
 * Compiler task used to generate full (non-trimmed) schema output for the libraries of a model.
 * 
 * @author S. Livezey
 */
public class SchemaCompilerTask extends AbstractSchemaCompilerTask implements SchemaCompilerTaskOptions {
	
	
	/**
	 * Constructor that specifies the filename of the project for which schemas are being
	 * compiled.
	 * 
	 * @param projectFilename  the name of the project (.otp) file
	 */
	public SchemaCompilerTask(String projectFilename) {
		this.projectFilename = projectFilename;
	}
	
	/**
	 * @see com.sabre.schemacompiler.task.AbstractCompilerTask#generateOutput(java.util.Collection, java.util.Collection)
	 */
	@Override
	protected void generateOutput(Collection<TLLibrary> userDefinedLibraries, Collection<XSDLibrary> legacySchemas) throws SchemaCompilerException {
		CodeGenerationContext context = createContext();
		
		// Generate schemas for all of the user-defined libraries
		compileXmlSchemas(userDefinedLibraries, legacySchemas, context, null, null);
		
		// Generate example files if required
		if (isGenerateExamples()) {
			generateExampleArtifacts(userDefinedLibraries, context, new LibraryFilenameBuilder<AbstractLibrary>(), null);
		}
	}
	
	/**
	 * @see com.sabre.schemacompiler.task.AbstractSchemaCompilerTask#getExampleOutputFolder(com.sabre.schemacompiler.model.LibraryMember, com.sabre.schemacompiler.codegen.CodeGenerationContext)
	 */
	@Override
	protected String getExampleOutputFolder(LibraryMember libraryMember, CodeGenerationContext context) {
		String libraryFolderName = "examples/" +
				new LibraryFilenameBuilder<AbstractLibrary>().buildFilename(libraryMember.getOwningLibrary(), "");
		String rootOutputFolder = context.getValue(CodeGenerationContext.CK_OUTPUT_FOLDER);
		
		if (rootOutputFolder == null) {
			rootOutputFolder = System.getProperty("user.dir");
		}
		return new File(rootOutputFolder, libraryFolderName).getAbsolutePath();
	}

	/**
	 * @see com.sabre.schemacompiler.task.AbstractSchemaCompilerTask#getSchemaRelativeFolderPath(com.sabre.schemacompiler.model.LibraryMember, com.sabre.schemacompiler.codegen.CodeGenerationContext)
	 */
	@Override
	protected String getSchemaRelativeFolderPath(LibraryMember libraryMember, CodeGenerationContext context) {
		return "../../";
	}
	
}
