/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.codegen.impl;

import com.sabre.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import com.sabre.schemacompiler.model.LibraryMember;

/**
 * Implementation of the <code>CodeGenerationFilenameBuilder</code> interface that can create
 * default filenames for <code>LibraryMember</code> entities.
 *
 * @param <S>  the soure type for which code is being generated
 * @author S. Livezey
 */
public class LibraryMemberFilenameBuilder<S extends LibraryMember> implements CodeGenerationFilenameBuilder<S> {
	
	/**
	 * @see com.sabre.schemacompiler.codegen.CodeGenerationFilenameBuilder#buildFilename(com.sabre.schemacompiler.model.TLModelElement, java.lang.String)
	 */
	@Override
	public String buildFilename(LibraryMember item, String fileExtension) {
		String fileExt = ((fileExtension == null) || (fileExtension.length() == 0)) ? "" : ("." + fileExtension);
		String filename = item.getLocalName();
		
		if (!filename.toLowerCase().endsWith(fileExt)) {
			filename += fileExt;
		}
		return filename;
	}
	
}
