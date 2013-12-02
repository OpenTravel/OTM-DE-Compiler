/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.impl;

import com.sabre.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import com.sabre.schemacompiler.model.AbstractLibrary;
import com.sabre.schemacompiler.model.TLLibrary;

/**
 * Implementation of the <code>CodeGenerationFilenameBuilder</code> interface that can create
 * default filenames for the XML schema files associated with <code>AbstractLibrary</code>
 * instances.
 * 
 * @author S. Livezey
 */
public class LibraryFilenameBuilder<L extends AbstractLibrary> implements CodeGenerationFilenameBuilder<L> {
	
	/**
	 * @see com.sabre.schemacompiler.codegen.CodeGenerationFilenameBuilder#buildFilename(com.sabre.schemacompiler.model.TLModelElement, java.lang.String)
	 */
	@Override
	public String buildFilename(L item, String fileExtension) {
		String fileExt = (fileExtension.length() == 0) ? "" : ("." + fileExtension);
		String filename = item.getName();
		
		if (item instanceof TLLibrary) {
			filename += "_" + ((TLLibrary) item).getVersion().replaceAll("\\.", "_");
		}
		if (!filename.toLowerCase().endsWith(fileExt)) {
			filename += fileExt;
		}
		return filename;
	}
	
}
