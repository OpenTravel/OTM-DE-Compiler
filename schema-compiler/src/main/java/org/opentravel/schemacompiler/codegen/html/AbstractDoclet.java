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
package org.opentravel.schemacompiler.codegen.html;

import org.opentravel.schemacompiler.model.TLModel;

import org.opentravel.schemacompiler.codegen.html.writers.LibraryListWriter;

/**
 * An abstract implementation of a Doclet.
 *
 */
public abstract class AbstractDoclet {

    /**
     * The global configuration information for this run.
     */
    public Configuration configuration;

    /**
     * The only doclet that may use this toolkit is {@value}
     */
    private static final String OTM_DOCLET_NAME = new
        HtmlDoclet().getClass().getName();

    /**
     * Verify that the only doclet that is using this toolkit is
     * {@value #OTM_DOCLET_NAME}.
     */
    private boolean isValidDoclet(AbstractDoclet doclet) {
        if (! doclet.getClass().getName().equals(OTM_DOCLET_NAME)) {
            configuration.message.error("doclet.Toolkit_Usage_Violation",
                OTM_DOCLET_NAME);
            return false;
        }
        return true;
    }

    /**
     * The method that starts the execution of the doclet.
     *
     * @param doclet the doclet to start the execution for.
     * @param root   the {@link RootDoc} that points to the source to document.
     * @return true if the doclet executed without error.  False otherwise.
     */
    public boolean start(AbstractDoclet doclet, TLModel model) {
        configuration = configuration();
        configuration.setModel(model);
        if (! isValidDoclet(doclet)) {
            return false;
        }
        try {
            doclet.startGeneration(model);
        } catch (Exception exc) {
        	exc.printStackTrace(System.out);
            return false;
        }
        return true;
    }


    /**
     * Create the configuration instance and returns it.
     * @return the configuration of the doclet.
     */
    public abstract Configuration configuration();

    /**
     * Start the generation of files. Call generate methods in the individual
     * writers, which will in turn genrate the documentation files. Call the
     * TreeWriter generation first to ensure the Class Hierarchy is built
     * first and then can be used in the later generation.
     *
     * @see org.opentravel.schemacompiler.codegen.html.RootDoc
     */
    private void startGeneration(TLModel model) throws Exception {
        if (model.getUserDefinedLibraries().size() == 0) {
            configuration.message.notice("doclet.No_Libraries_To_Document");
            return;
        }
        configuration.getDocletSpecificMsg().notice("doclet.build_version",
            configuration.getDocletSpecificBuildDate());

        LibraryListWriter.generate(configuration);
        generateLibraryFiles(model);

        generateOtherFiles(model);
    }

    /**
     * 
     * @param manager
     * @throws Exception 
     */
    protected abstract void generateOtherFiles(TLModel model) throws Exception;
  
    /**
     * Generate the library documentation.
     *
     */
    protected abstract void generateLibraryFiles(TLModel model) throws Exception;


}
