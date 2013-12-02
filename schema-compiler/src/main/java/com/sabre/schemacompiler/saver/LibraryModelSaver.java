/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.saver;

import java.util.List;

import org.opentravel.ns.ota2.librarymodel_v01_04.Library;

import com.sabre.schemacompiler.ioc.SchemaCompilerApplicationContext;
import com.sabre.schemacompiler.model.TLLibrary;
import com.sabre.schemacompiler.model.TLModel;
import com.sabre.schemacompiler.saver.impl.LibraryFileSaveHandler;
import com.sabre.schemacompiler.security.LibraryCrcCalculator;
import com.sabre.schemacompiler.transform.ObjectTransformer;
import com.sabre.schemacompiler.transform.SymbolResolver;
import com.sabre.schemacompiler.transform.TransformerFactory;
import com.sabre.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import com.sabre.schemacompiler.transform.symbols.SymbolTableFactory;
import com.sabre.schemacompiler.transform.tl2jaxb.TL2JaxbLibrarySymbolResolver;
import com.sabre.schemacompiler.transform.util.ChameleonFilter;
import com.sabre.schemacompiler.transform.util.LibraryPrefixResolver;
import com.sabre.schemacompiler.validate.ValidationFindings;
import com.sabre.schemacompiler.validate.save.TLModelSaveValidator;

/**
 * Orchestrates the saving of <code>TLModel</code> and <code>TLLibrary</code> objects back to their
 * original URL locations.  Unless an alternate <code>LibrarySaveHandler</code> implementation is
 * assigned, the model saver will assume URL locations to be resolvable onto the local file system.
 * 
 * @author S. Livezey
 */
public final class LibraryModelSaver {
	
	private LibrarySaveHandler saveHandler = new LibraryFileSaveHandler();
	
	/**
	 * Returns the <code>LibrarySaveHandler</code> instance that will be used to persist the
	 * JAXB library instances.
	 * 
	 * @return LibrarySaveHandler
	 */
	public LibrarySaveHandler getSaveHandler() {
		return saveHandler;
	}
	
	/**
	 * Assigns the <code>LibrarySaveHandler</code> instance that will be used to persist the
	 * JAXB library instances.
	 * 
	 * @param saveHandler  the save handler instance to assign
	 */
	public void setSaveHandler(LibrarySaveHandler saveHandler) {
		this.saveHandler = saveHandler;
	}
	
	/**
	 * Saves all of the user-defined libraries in the model to their original URL location.  Any
	 * user-defined libraries that are read-only will be skipped by this method without error.
	 * 
	 * @param model  the model containing the libraries to save
	 * @throws LibrarySaveException  thrown if a problem occurs during the save operation
	 */
	public ValidationFindings saveAllLibraries(TLModel model) throws LibrarySaveException {
		SymbolResolverTransformerContext context = new SymbolResolverTransformerContext();
		SymbolResolver symbolResolver = new TL2JaxbLibrarySymbolResolver(SymbolTableFactory.newSymbolTableFromModel(model));
		ValidationFindings findings = new ValidationFindings();
		
		context.setSymbolResolver(symbolResolver);
		
		for (TLLibrary library : model.getUserDefinedLibraries()) {
			if ((library != null) && !library.isReadOnly()) {
				symbolResolver.setPrefixResolver(new LibraryPrefixResolver(library));
				symbolResolver.setAnonymousEntityFilter(new ChameleonFilter(library));
				findings.addAll( saveLibrary(library, context) );
			}
		}
		return findings;
	}
	
	/**
	 * Saves all of the libraries in the given list to their original URL location.  Any
	 * user-defined libraries that are read-only will be skipped by this method without error.
	 * 
	 * @param libraryList  the list of libraries to save
	 * @throws LibrarySaveException  thrown if a problem occurs during the save operation
	 */
	public ValidationFindings saveLibraries(List<TLLibrary> libraryList) throws LibrarySaveException {
		ValidationFindings findings = new ValidationFindings();
		
		for (TLLibrary library : libraryList) {
			if ((library != null) && !library.isReadOnly()) {
				findings.addAll( saveLibrary(library) );
			}
		}
		return findings;
	}
	
	/**
	 * Saves the given library to its original URL location.  If the given library is read-only,
	 * an exception will be thrown by this method.
	 * 
	 * @param library  the library instance to save
	 * @throws LibrarySaveException  thrown if a problem occurs during the save operation
	 */
	public ValidationFindings saveLibrary(TLLibrary library) throws LibrarySaveException {
		SymbolResolverTransformerContext context = new SymbolResolverTransformerContext();
		SymbolResolver symbolResolver = new TL2JaxbLibrarySymbolResolver(
				SymbolTableFactory.newSymbolTableFromModel(library.getOwningModel()));
		ValidationFindings findings = null;
		
		context.setSymbolResolver(symbolResolver);
		
		if (library != null) {
			if (library.isReadOnly()) {
				throw new LibrarySaveException("Unable to save library '" + library.getName() +
						"' because it is read-only.");
			}
			symbolResolver.setPrefixResolver(new LibraryPrefixResolver(library));
			symbolResolver.setAnonymousEntityFilter(new ChameleonFilter(library));
			findings = saveLibrary(library, context);
		}
		return (findings == null) ? new ValidationFindings() : findings;
	}
	
	/**
	 * Saves the given library to its original URL location.
	 * 
	 * @param library  the library instance to save
	 * @param transformContext  the transformation context to utilize during save processing
	 * @throws LibrarySaveException  thrown if a problem occurs during the save operation
	 */
	private ValidationFindings saveLibrary(TLLibrary library, SymbolResolverTransformerContext transformContext) throws LibrarySaveException {
		// Do some preliminary validation checks before proceeding
		if (library == null) {
			throw new NullPointerException("Library instance cannot be null for save operations.");
		} else if (library.getLibraryUrl() == null) {
			throw new LibrarySaveException(
					"Unable to save - the resource URL of the library has not been assigned.");
		} else if (!saveHandler.canSave(library.getLibraryUrl())) {
			throw new LibrarySaveException(
					"Unable to save to the requested URL: " + library.getLibraryUrl().toExternalForm());
		}
		
		// Transform the library to JAXB and use the handler to save the file
		TransformerFactory<SymbolResolverTransformerContext> factory =
				TransformerFactory.getInstance(SchemaCompilerApplicationContext.SAVER_TRANSFORMER_FACTORY, transformContext);
		ObjectTransformer<TLLibrary,Library,SymbolResolverTransformerContext> libraryTransformer =
				factory.getTransformer(TLLibrary.class, Library.class);
		Library jaxbLibrary = libraryTransformer.transform(library);
		
		if (jaxbLibrary == null) {
			throw new LibrarySaveException("Unable to perform JAXB transformation for library '"
					+ library.getName() + "' during save operation.");
		}
		jaxbLibrary.setCrcValue( calculateCrcValue(library) );
		
		// Check for validation and JAXB problems before we save
		ValidationFindings findings = TLModelSaveValidator.validateModelElement(library);
		findings.addAll( saveHandler.validateLibraryContent(jaxbLibrary) );
		
		// Now attempt to save the file
		saveHandler.saveLibraryContent(library.getLibraryUrl(), jaxbLibrary);
		
		return findings;
	}
	
	/**
	 * Returns the CRC value for the given library, or null if a CRC is not required.
	 * 
	 * @param library  the library for which to calculate a CRC
	 * @return Long
	 */
	private Long calculateCrcValue(TLLibrary library) {
		Long crcValue = null;
		
		if (LibraryCrcCalculator.isCrcRequired(library)) {
			crcValue = LibraryCrcCalculator.calculate(library);
		}
		return crcValue;
	}
	
}
