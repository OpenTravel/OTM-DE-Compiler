/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.codegen.impl;

import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLLibrary;

/**
 * Implementation of the <code>CodeGenerationFilenameBuilder</code> interface that can create
 * default filenames for the XML schema files associated with <code>AbstractLibrary</code>
 * instances for a specific service.
 *
 * @author S. Livezey
 */
public class LibraryTrimmedFilenameBuilder implements CodeGenerationFilenameBuilder<AbstractLibrary> {
	
	private LibraryFilenameBuilder<AbstractLibrary> libraryFilenameBuilder = new LibraryFilenameBuilder<AbstractLibrary>();
	private String memberFilename;
	
	/**
	 * Constructor that specifies the service with which each generated library will be affiliated.
	 * 
	 * @param libraryMember  the service affiliation for all generated schema files
	 */
	public LibraryTrimmedFilenameBuilder(LibraryMember libraryMember) {
		memberFilename = (libraryMember == null) ? null
				: new LibraryMemberFilenameBuilder<LibraryMember>().buildFilename(libraryMember, null);
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder#buildFilename(java.lang.Object, java.lang.String)
	 */
	@Override
	public String buildFilename(AbstractLibrary item, String fileExtension) {
		String fileExt = ((fileExtension == null) || (fileExtension.length() == 0)) ? "" : ("." + fileExtension);
		String filename;
		
		if (item instanceof TLLibrary) {
			if (memberFilename != null) {
				filename = memberFilename + "_Trim_" + item.getName() + "_" + ((TLLibrary) item).getVersion().replaceAll("\\.", "_");
			} else {
				filename = libraryFilenameBuilder.buildFilename(item, "") + "_Trim";
			}
		} else {
			filename = item.getName();
		}
		if (!filename.toLowerCase().endsWith(fileExt)) {
			filename += fileExt;
		}
		return filename;
	}
	
}
