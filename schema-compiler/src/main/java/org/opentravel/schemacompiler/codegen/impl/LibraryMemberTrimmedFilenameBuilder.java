
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
 * @param M  the type of the library member for which a filename will be generated
 * @author S. Livezey
 */
public class LibraryMemberTrimmedFilenameBuilder<M extends LibraryMember> implements CodeGenerationFilenameBuilder<M> {
	
	private String memberFilename;
	private boolean includeVersionInfo;
	
	/**
	 * Constructor that specifies the service with which each generated library will be affiliated.
	 * 
	 * @param libraryMember  the service affiliation for all generated schema files
	 */
	public LibraryMemberTrimmedFilenameBuilder(LibraryMember libraryMember) {
		this(libraryMember, true);
	}

	/**
	 * Constructor that specifies the service with which each generated library will be affiliated.
	 * 
	 * @param libraryMember  the service affiliation for all generated schema files
	 */
	public LibraryMemberTrimmedFilenameBuilder(LibraryMember libraryMember, boolean includeVersionInfo) {
		this.memberFilename = new LibraryMemberFilenameBuilder<LibraryMember>().buildFilename(libraryMember, null);
		this.includeVersionInfo = includeVersionInfo;
	}

	/**
	 * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder#buildFilename(java.lang.Object, java.lang.String)
	 */
	@Override
	public String buildFilename(LibraryMember item, String fileExtension) {
		String fileExt = ((fileExtension == null) || (fileExtension.length() == 0)) ? "" : ("." + fileExtension);
		AbstractLibrary library = item.getOwningLibrary();
		String filename;
		
		if (library instanceof TLLibrary) {
			if (includeVersionInfo) {
				filename = memberFilename + "_Trim_" + library.getName() + "_" + ((TLLibrary) library).getVersion().replaceAll("\\.", "_");
			} else {
				filename = memberFilename;
			}
		} else {
			filename = library.getName();
		}
		if (!filename.toLowerCase().endsWith(fileExt)) {
			filename += fileExt;
		}
		return filename;
	}
	
}
