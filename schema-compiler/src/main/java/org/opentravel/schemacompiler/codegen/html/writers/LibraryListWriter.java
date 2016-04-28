package org.opentravel.schemacompiler.codegen.html.writers;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.codegen.html.Configuration;
import org.opentravel.schemacompiler.codegen.html.DocletAbortException;
import org.opentravel.schemacompiler.codegen.html.DocletConstants;
import org.opentravel.schemacompiler.codegen.html.Util;

/**
 * Write out the package index.
 *
 * This code is not part of an API.
 * It is implementation that is subject to change.
 * Do not use it as an API
 *
 * @see com.sun.javadoc.PackageDoc
 * @author Atul M Dambalkar
 */
public class LibraryListWriter extends PrintWriter {
	
    /**
     * Constructor.
     *
     * @param configuration the current configuration of the doclet.
     */
    public LibraryListWriter(Configuration configuration) throws IOException {
        super(Util.genWriter(configuration, configuration.destDirName,
            DocletConstants.LIBRARY_LIST_FILE_NAME, configuration.docencoding));
    }

    /**
     * Generate the package index.
     *
     * @param configuration the current configuration of the doclet.
     * @throws DocletAbortException
     */
    public static void generate(Configuration configuration) {
        LibraryListWriter packgen;
        try {
            packgen = new LibraryListWriter(configuration);
            packgen.generateLibraryListFile(configuration.model);
            packgen.close();
        } catch (IOException exc) {
            configuration.message.error("doclet.exception_encountered",
                exc.toString(), DocletConstants.LIBRARY_LIST_FILE_NAME);
            throw new DocletAbortException();
        }
    }

    protected void generateLibraryListFile(TLModel model) {
    	List<TLLibrary> libraries = model.getUserDefinedLibraries();
        for (TLLibrary lib : libraries ){
            println(lib.getName() + lib.getVersion());
        }
    }
}
