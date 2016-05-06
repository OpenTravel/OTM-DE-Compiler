/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 * Write out the library index.
 *
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
