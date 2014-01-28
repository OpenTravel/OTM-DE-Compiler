package org.opentravel.schemacompiler.loader.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.opentravel.schemacompiler.loader.LibraryInputSource;
import org.opentravel.schemacompiler.loader.LibraryLoaderException;
import org.opentravel.schemacompiler.loader.LibraryModuleInfo;
import org.opentravel.schemacompiler.loader.LibraryModuleLoader;
import org.opentravel.schemacompiler.validate.ValidationFinding;
import org.opentravel.schemacompiler.validate.ValidationFindings;

/**
 * Library module loader that is capable of handling multiple versions of JAXB libraries from stream
 * input sources.
 * 
 * @author S. Livezey
 */
public class MultiVersionLibraryModuleLoader extends AbstractLibraryModuleLoader {

    private List<LibraryModuleLoader<InputStream>> moduleLoaders = new ArrayList<LibraryModuleLoader<InputStream>>();

    /**
     * Default constructor.
     */
    public MultiVersionLibraryModuleLoader() {
        // Assign the prioritized list of delegate module loaders
        moduleLoaders.add(new LibrarySchema1_4_ModuleLoader());
        moduleLoaders.add(new LibrarySchema1_3_ModuleLoader());
    }

    /**
     * @see org.opentravel.schemacompiler.loader.LibraryModuleLoader#loadLibrary(org.opentravel.schemacompiler.loader.LibraryInputSource,
     *      org.opentravel.schemacompiler.validate.ValidationFindings)
     */
    @Override
    public LibraryModuleInfo<Object> loadLibrary(LibraryInputSource<InputStream> inputSource,
            ValidationFindings validationFindings) throws LibraryLoaderException {
        ValidationFindings firstFindings = null;
        LibraryModuleInfo<Object> moduleInfo = null;

        for (LibraryModuleLoader<InputStream> delegateModuleLoader : moduleLoaders) {
            ValidationFindings delegateFindings = new ValidationFindings();

            moduleInfo = delegateModuleLoader.loadLibrary(inputSource, delegateFindings);

            if (firstFindings == null) {
                firstFindings = delegateFindings;
            }
            if (isSuccessfulLoad(delegateFindings)) {
                validationFindings.addAll(delegateFindings);
                break;
            }
        }

        // If none of the delegate loaders were successful, report the findings from the first
        // (preferred loader)
        if ((moduleInfo == null) && (firstFindings != null)) {
            validationFindings.addAll(firstFindings);
        }
        return moduleInfo;
    }

    /**
     * If the delegate loader's findings contains the error key for "UNREADABLE_SCHEMA_CONTENT",
     * this method will return false, indicating a failure. All other situations will return true,
     * regardless of the number and types of errors in the delegate's findings.
     * 
     * @param delegateFindings
     *            the validation findings from the delegate module loader
     * @return boolean
     */
    private boolean isSuccessfulLoad(ValidationFindings delegateFindings) {
        boolean success = true;

        for (ValidationFinding finding : delegateFindings.getAllFindingsAsList()) {
            if (ERROR_UNREADABLE_LIBRARY_CONTENT.equals(finding.getMessageKey())) {
                success = false;
                break;
            }
        }
        return success;
    }

}
