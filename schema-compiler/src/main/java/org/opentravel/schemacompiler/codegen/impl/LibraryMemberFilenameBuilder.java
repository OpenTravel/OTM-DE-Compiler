package org.opentravel.schemacompiler.codegen.impl;

import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.model.LibraryMember;

/**
 * Implementation of the <code>CodeGenerationFilenameBuilder</code> interface that can create
 * default filenames for <code>LibraryMember</code> entities.
 * 
 * @param <S>
 *            the soure type for which code is being generated
 * @author S. Livezey
 */
public class LibraryMemberFilenameBuilder<S extends LibraryMember> implements
        CodeGenerationFilenameBuilder<S> {

    /**
     * @see org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder#buildFilename(org.opentravel.schemacompiler.model.TLModelElement,
     *      java.lang.String)
     */
    @Override
    public String buildFilename(LibraryMember item, String fileExtension) {
        String fileExt = ((fileExtension == null) || (fileExtension.length() == 0)) ? ""
                : ("." + fileExtension);
        String filename = item.getLocalName();

        if (!filename.toLowerCase().endsWith(fileExt)) {
            filename += fileExt;
        }
        return filename;
    }

}
