/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.codegen.impl;

import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.TLLibrary;

/**
 * Implementation of the <code>CodeGenerationFilenameBuilder</code> interface that can create
 * default filenames for the XML schema files associated with <code>AbstractLibrary</code>
 * instances.
 * 
 * @author S. Livezey
 */
public class LibraryFilenameBuilder<L extends AbstractLibrary> implements CodeGenerationFilenameBuilder<L> {
	
	/**
	 * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder#buildFilename(org.opentravel.schemacompiler.model.TLModelElement, java.lang.String)
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
