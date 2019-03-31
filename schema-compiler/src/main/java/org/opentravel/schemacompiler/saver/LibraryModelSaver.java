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

package org.opentravel.schemacompiler.saver;

import org.apache.commons.beanutils.BeanUtils;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.saver.impl.Library15FileSaveHandler;
import org.opentravel.schemacompiler.saver.impl.Library16FileSaveHandler;
import org.opentravel.schemacompiler.security.LibraryCrcCalculator;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.SymbolResolver;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import org.opentravel.schemacompiler.transform.symbols.SymbolTableFactory;
import org.opentravel.schemacompiler.transform.tl2jaxb.TL2JaxbLibrarySymbolResolver;
import org.opentravel.schemacompiler.transform.util.ChameleonFilter;
import org.opentravel.schemacompiler.transform.util.LibraryPrefixResolver;
import org.opentravel.schemacompiler.util.OTM16Upgrade;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.save.TLModelSaveValidator;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Orchestrates the saving of <code>TLModel</code> and <code>TLLibrary</code> objects back to their original URL
 * locations. Unless an alternate <code>LibrarySaveHandler</code> implementation is assigned, the model saver will
 * assume URL locations to be resolvable onto the local file system.
 * 
 * @author S. Livezey
 */
public final class LibraryModelSaver {

    private LibrarySaveHandler<?> saveHandler =
        OTM16Upgrade.otm16Enabled ? new Library16FileSaveHandler() : new Library15FileSaveHandler();

    /**
     * Returns the <code>LibrarySaveHandler</code> instance that will be used to persist the JAXB library instances.
     * 
     * @param <T> the type of the library to be saved by the handler
     * @return LibrarySaveHandler
     */
    @SuppressWarnings("unchecked")
    public <T> LibrarySaveHandler<T> getSaveHandler() {
        return (LibrarySaveHandler<T>) saveHandler;
    }

    /**
     * Assigns the <code>LibrarySaveHandler</code> instance that will be used to persist the JAXB library instances.
     * 
     * @param saveHandler the save handler instance to assign
     */
    public void setSaveHandler(LibrarySaveHandler<?> saveHandler) {
        this.saveHandler = saveHandler;
    }

    /**
     * Saves all of the user-defined libraries in the model to their original URL location. Any user-defined libraries
     * that are read-only will be skipped by this method without error.
     * 
     * @param model the model containing the libraries to save
     * @return ValidationFindings
     * @throws LibrarySaveException thrown if a problem occurs during the save operation
     */
    public ValidationFindings saveAllLibraries(TLModel model) throws LibrarySaveException {
        SymbolResolverTransformerContext context = new SymbolResolverTransformerContext();
        SymbolResolver symbolResolver =
            new TL2JaxbLibrarySymbolResolver( SymbolTableFactory.newSymbolTableFromModel( model ) );
        ValidationFindings findings = new ValidationFindings();

        context.setSymbolResolver( symbolResolver );

        for (TLLibrary library : model.getUserDefinedLibraries()) {
            if ((library != null) && !library.isReadOnly()) {
                symbolResolver.setPrefixResolver( new LibraryPrefixResolver( library ) );
                symbolResolver.setAnonymousEntityFilter( new ChameleonFilter( library ) );
                findings.addAll( saveLibrary( library, context ) );
            }
        }
        return findings;
    }

    /**
     * Saves all of the libraries in the given list to their original URL location. Any user-defined libraries that are
     * read-only will be skipped by this method without error.
     * 
     * @param libraryList the list of libraries to save
     * @return ValidationFindings
     * @throws LibrarySaveException thrown if a problem occurs during the save operation
     */
    public ValidationFindings saveLibraries(List<TLLibrary> libraryList) throws LibrarySaveException {
        ValidationFindings findings = new ValidationFindings();

        for (TLLibrary library : libraryList) {
            if ((library != null) && !library.isReadOnly()) {
                findings.addAll( saveLibrary( library ) );
            }
        }
        return findings;
    }

    /**
     * Saves the given library to its original URL location. If the given library is read-only, an exception will be
     * thrown by this method.
     * 
     * @param library the library instance to save
     * @return ValidationFindings
     * @throws LibrarySaveException thrown if a problem occurs during the save operation
     */
    public ValidationFindings saveLibrary(TLLibrary library) throws LibrarySaveException {
        SymbolResolverTransformerContext context = new SymbolResolverTransformerContext();
        SymbolResolver symbolResolver =
            new TL2JaxbLibrarySymbolResolver( SymbolTableFactory.newSymbolTableFromModel( library.getOwningModel() ) );
        ValidationFindings findings = new ValidationFindings();

        context.setSymbolResolver( symbolResolver );

        if (library.isReadOnly()) {
            throw new LibrarySaveException(
                "Unable to save library '" + library.getName() + "' because it is read-only." );
        }
        symbolResolver.setPrefixResolver( new LibraryPrefixResolver( library ) );
        symbolResolver.setAnonymousEntityFilter( new ChameleonFilter( library ) );
        findings.addAll( saveLibrary( library, context ) );
        return findings;
    }

    /**
     * Saves the given library to its original URL location.
     * 
     * @param library the library instance to save
     * @param transformContext the transformation context to utilize during save processing
     * @throws LibrarySaveException thrown if a problem occurs during the save operation
     */
    private <T> ValidationFindings saveLibrary(TLLibrary library, SymbolResolverTransformerContext transformContext)
        throws LibrarySaveException {
        // Do some preliminary validation checks before proceeding
        if (library == null) {
            throw new NullPointerException( "Library instance cannot be null for save operations." );
        } else if (library.getLibraryUrl() == null) {
            throw new LibrarySaveException( "Unable to save - the resource URL of the library has not been assigned." );
        } else if (!saveHandler.canSave( library.getLibraryUrl() )) {
            throw new LibrarySaveException(
                "Unable to save to the requested URL: " + library.getLibraryUrl().toExternalForm() );
        }

        // Transform the library to JAXB and use the handler to save the file
        LibrarySaveHandler<T> handler = getSaveHandler();
        TransformerFactory<SymbolResolverTransformerContext> factory = TransformerFactory
            .getInstance( SchemaCompilerApplicationContext.SAVER_TRANSFORMER_FACTORY, transformContext );
        ObjectTransformer<TLLibrary,T,SymbolResolverTransformerContext> libraryTransformer =
            factory.getTransformer( TLLibrary.class, handler.getTargetFormat() );
        T jaxbLibrary = libraryTransformer.transform( library );

        if (jaxbLibrary == null) {
            throw new LibrarySaveException( "Unable to perform JAXB transformation for library '" + library.getName()
                + "' during save operation." );
        }

        // Calculate the CRC value for any final libraries
        try {
            Long crcValue = calculateCrcValue( library );

            if (crcValue != null) {
                BeanUtils.setProperty( jaxbLibrary, "crcValue", crcValue );
            }

        } catch (IllegalAccessException | InvocationTargetException e) {
            // Should never happen, but ignore the exception if the JAXB library does not contain
            // a 'crcValue' property.
        }

        // Check for validation and JAXB problems before we save
        ValidationFindings findings = TLModelSaveValidator.validateModelElement( library );
        findings.addAll( handler.validateLibraryContent( jaxbLibrary ) );

        // Now attempt to save the file
        handler.saveLibraryContent( library.getLibraryUrl(), jaxbLibrary );

        return findings;
    }

    /**
     * Returns the CRC value for the given library, or null if a CRC is not required.
     * 
     * @param library the library for which to calculate a CRC
     * @return Long
     */
    private Long calculateCrcValue(TLLibrary library) {
        Long crcValue = null;

        if (LibraryCrcCalculator.isCrcRequired( library )) {
            crcValue = LibraryCrcCalculator.calculate( library );
        }
        return crcValue;
    }

}
