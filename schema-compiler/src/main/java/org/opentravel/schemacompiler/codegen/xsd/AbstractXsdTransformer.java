/*
 * Copyright (c) 2011, Sabre Inc.
 */
package org.opentravel.schemacompiler.codegen.xsd;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;

import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.CodeGenerator;
import org.opentravel.schemacompiler.codegen.impl.AbstractCodegenTransformer;
import org.opentravel.schemacompiler.codegen.impl.AbstractJaxbCodeGenerator;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.impl.LegacySchemaExtensionFilenameBuilder;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenElements;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.ioc.SchemaDeclaration;
import org.opentravel.schemacompiler.ioc.SchemaDeclarations;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemacompiler.model.TLInclude;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.transform.AnonymousEntityFilter;
import org.opentravel.schemacompiler.util.URLUtils;
import org.springframework.context.ApplicationContext;
import org.w3._2001.xmlschema.FormChoice;
import org.w3._2001.xmlschema.Import;
import org.w3._2001.xmlschema.Include;
import org.w3._2001.xmlschema.Schema;

/**
 * Base class for all <code>ObjectTransformer</code> implementations that are part of the
 * XML schema code generation subsystem.
 * 
 * @param <S>  the source type of the object transformation
 * @param <T>  the target type of the object transformation
 * @author S. Livezey
 */
public abstract class AbstractXsdTransformer<S,T> extends AbstractCodegenTransformer<S,T> {
	
	protected static org.w3._2001.xmlschema.ObjectFactory jaxbObjectFactory = new org.w3._2001.xmlschema.ObjectFactory();
	
	/**
	 * Adds the schemas associated with the given compile-time dependency to the current
	 * list of dependencies maintained by the orchestrating code generator.
	 * 
	 * @param dependency  the compile-time dependency to add
	 */
	protected void addCompileTimeDependency(SchemaDependency dependency) {
		addCompileTimeDependency( dependency.getSchemaDeclaration() );
	}
	
	/**
	 * Adds the schemas associated with the given compile-time dependency to the current
	 * list of dependencies maintained by the orchestrating code generator.
	 * 
	 * @param schemaDeclaration  the compile-time schema declaration to add
	 */
	protected void addCompileTimeDependency(SchemaDeclaration schemaDeclaration) {
		CodeGenerator<?> codeGenerator = context.getCodeGenerator();
		
		if (codeGenerator instanceof AbstractJaxbCodeGenerator) {
			((AbstractJaxbCodeGenerator<?>) codeGenerator).addCompileTimeDependency( schemaDeclaration );
		}
	}
	
	/**
	 * Returns the list of compile-time schema dependencies that have been reported during code
	 * generation.
	 * 
	 * @return Collection<SchemaDeclaration>
	 */
	protected Collection<SchemaDeclaration> getCompileTimeDependencies() {
		CodeGenerator<?> codeGenerator = context.getCodeGenerator();
		Collection<SchemaDeclaration> dependencies;
		
		if (codeGenerator instanceof AbstractJaxbCodeGenerator) {
			dependencies = ((AbstractJaxbCodeGenerator<?>) codeGenerator).getCompileTimeDependencies();
		} else {
			dependencies = Collections.emptySet();
		}
		return dependencies;
	}
	
	/**
	 * Returns a new JAXB <code>Schema</code> instance using the namespace and version information
	 * provided.
	 * 
	 * @param targetNamespace  the target namespace of the schema
	 * @param version  the version identifier of the schema
	 * @return Schema
	 */
	protected Schema createSchema(String targetNamespace, String version) {
		Schema schema = new Schema();
		
		schema.setAttributeFormDefault(FormChoice.UNQUALIFIED);
		schema.setElementFormDefault(FormChoice.QUALIFIED);
		schema.setVersion(version);
		schema.setTargetNamespace(targetNamespace);
		return schema;
	}
	
	/**
	 * Adds the specified imports to the target XML schema.
	 * 
	 * @param schema  the target XML schema that will receive the imports
	 * @param sourceLibrary  the source library for which the include directives are being created
	 * @param filenameBuilder  the filename builder to use for all schema locations
	 * @param importFilter  filter that only allows imported dependencies from the specified source library
	 */
	protected void addImports(Schema schema, AbstractLibrary sourceLibrary,
			CodeGenerationFilenameBuilder<AbstractLibrary> filenameBuilder, CodeGenerationFilter importFilter) {
		List<AbstractLibrary> importList = new ArrayList<AbstractLibrary>();
		String targetNamespace = schema.getTargetNamespace();
		TLModel model = sourceLibrary.getOwningModel();
		
		// Identify imports for any compile-time dependencies that would not otherwise be
		// covered by the list of explicitly imported namespaces
		String builtInFolder = getBuiltInSchemaOutputLocation();
		
		for (SchemaDeclaration schemaDependency : getCompileTimeDependencies()) {
			AbstractLibrary builtInImport = model.getLibrary(schemaDependency.getNamespace(), schemaDependency.getName());
			String schemaLocation = builtInFolder + schemaDependency.getFilename();
			
			if (schemaLocation.toLowerCase().endsWith(".xsd")) {
				if (builtInImport != null) {
					importList.add(builtInImport);
				} else {
					// For now, only add import statements for schemas that are not formally identified as
					// built-in libraries.
					addImport(schema, schemaDependency.getNamespace(), builtInFolder + schemaDependency.getFilename(), false);
				}
			}
		}
		
		// Identify libraries from the model that require imports
		for (AbstractLibrary importCandidate : model.getAllLibraries()) {
			String namespace = importCandidate.getNamespace();
			
			// Skip libraries that are in the target, chameleon, or the XML schema namespaces
			if (namespace.equals(targetNamespace) || namespace.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)
					|| namespace.equals(AnonymousEntityFilter.ANONYMOUS_PSEUDO_NAMESPACE)) {
				continue;
			}
			
			// Add each library to the list if the filter allows it
			if (!importList.contains(importCandidate)
					&& ((importFilter == null) || importFilter.processLibrary(importCandidate))) {
				importList.add(importCandidate);
			}
		}
		
		// Now, filter out redundant imports that would be covered by includes within the dependent files
		Map<AbstractLibrary,List<AbstractLibrary>> includeDependencies = getIncludeDependencies(importList);
		AbstractLibrary[] importArray = importList.toArray(new AbstractLibrary[importList.size()]);
		
		for (AbstractLibrary importedLibrary : importArray) {
			Iterator<AbstractLibrary> redundantCandidates = importList.iterator();
			
			while (redundantCandidates.hasNext()) {
				AbstractLibrary redundantCandidate = redundantCandidates.next();
				
				if (importedLibrary == redundantCandidate) {
					continue;
				}
				if (isIncludedBy(importedLibrary, redundantCandidate, includeDependencies)) {
					includeDependencies.remove(redundantCandidate); // protects against omissions when circular includes are defined
					redundantCandidates.remove();
				}
			}
		}
		
		// Now add import statements for all of the import dependencies in the final list
		for (AbstractLibrary importedLib : importList) {
			String namespace = importedLib.getNamespace();
			String importFolder = "";
			
			if (importedLib instanceof BuiltInLibrary) {
				importFolder = getBuiltInSchemaOutputLocation();
				
			} else if (importedLib instanceof XSDLibrary) {
				importFolder = getLegacySchemaOutputLocation();
			}
			
			if ((importedLib instanceof XSDLibrary) && (importFilter != null)
					&& importFilter.processExtendedLibrary((XSDLibrary) importedLib)) {
				addImport(schema, namespace,
						importFolder + new LegacySchemaExtensionFilenameBuilder<AbstractLibrary>(filenameBuilder)
							.buildFilename(importedLib, "xsd"), true);
			} else {
				addImport(schema, namespace, importFolder + filenameBuilder.buildFilename(importedLib, "xsd"), false);
			}
		}
		addImport(schema, SchemaDeclarations.OTA2_APPINFO_SCHEMA.getNamespace(),
				builtInFolder + SchemaDeclarations.OTA2_APPINFO_SCHEMA.getFilename(), false);
		addCompileTimeDependency(SchemaDeclarations.OTA2_APPINFO_SCHEMA);
	}
	
	/**
	 * Adds an import to the specified schema using the information provided.
	 * 
	 * @param schema  the target XML schema that will receive the import
	 * @param namespace  the namespace to be imported
	 * @param defaultSchemaLocation  the schema location of the file to import if one has not been specified in the <code>ImportSchemaLocations</code>
	 * @param isLegacyExtensionImport  indicates that this is an import of a legacy extension schema
	 */
	private void addImport(Schema schema, String namespace, String defaultSchemaLocation, boolean isLegacyExtensionImport) {
		ImportSchemaLocations importLocations = ((AbstractXsdCodeGenerator<?>) context.getCodeGenerator()).getImportSchemaLocations();
		Import jaxbImport = new Import();
		String schemaLocation = defaultSchemaLocation;
		
		if ((importLocations != null) && !isLegacyExtensionImport) {
			File schemaFile = importLocations.getSchemaLocation(namespace);
			
			if (schemaFile != null) {
				URL outputFolderUrl = URLUtils.toURL( getBaseOutputFolder() );
				URL schemaUrl = URLUtils.toURL( schemaFile );
				
				schemaLocation = URLUtils.getRelativeURL(outputFolderUrl, schemaUrl, false);
				importLocations.importAddedForNamespace(namespace);
			}
		}
		jaxbImport.setNamespace( namespace );
		jaxbImport.setSchemaLocation( schemaLocation );
		schema.getIncludeOrImportOrRedefine().add( jaxbImport );
	}
	
	/**
	 * Returns the location of the base output folder for the schema that is being generated.
	 * 
	 * @return File
	 */
	protected File getBaseOutputFolder() {
		return XsdCodegenUtils.getBaseOutputFolder(context.getCodegenContext());
	}
	
	/**
	 * Returns a map that associates each of the given libraries with zero or more other libraries
	 * that are declared as includes.  If no includes are defined for a library, the value side of
	 * the resulting map will be an empty list.
	 * 
	 * @param libraryList  the list of libraries to include in the dependency analysis
	 * @return Map<AbstractLibrary,List<AbstractLibrary>>
	 */
	private Map<AbstractLibrary,List<AbstractLibrary>> getIncludeDependencies(List<AbstractLibrary> libraryList) {
		Map<AbstractLibrary,List<AbstractLibrary>> includeDependencies = new HashMap<AbstractLibrary,List<AbstractLibrary>>();
		ApplicationContext appContext = SchemaCompilerApplicationContext.getContext();
		
		for (AbstractLibrary library : libraryList) {
			List<AbstractLibrary> includedLibraries = new ArrayList<AbstractLibrary>();
			
			if (library instanceof BuiltInLibrary) {
				BuiltInLibrary builtIn = (BuiltInLibrary) library;
				
				// Search the list of dependencies for the underlying schema declaration
				for (String builtInDependency : builtIn.getSchemaDeclaration().getDependencies()) {
					SchemaDeclaration dependency = (SchemaDeclaration) appContext.getBean(builtInDependency);
					
					// The dependency is an include if both namespaces are the same
					if (dependency.getNamespace().equals(builtIn.getNamespace())) {
						AbstractLibrary dependentLib = library.getOwningModel().getLibrary( dependency.getNamespace(), dependency.getName() );
						
						if (dependentLib != null) {
							includedLibraries.add(dependentLib);
						}
					}
				}
			} else {
				List<URL> includeUrls = getIncludeURLs(library);
				
				for (AbstractLibrary includeCandidate : libraryList) {
					if (includeUrls.contains(includeCandidate.getLibraryUrl())) {
						includedLibraries.add( includeCandidate );
					}
				}
			}
			includeDependencies.put(library, includedLibraries);
		}
		return includeDependencies;
	}
	
	/**
	 * Returns true if the 'includeCandidate' is included by the given 'sourceLibrary'.
	 * 
	 * @param sourceLibrary  the source library of the potential include dependency
	 * @param includeCandidate  the candidate target library of the include
	 * @param includeDependencies  the pre-calculated include dependencies
	 * @return boolean
	 */
	private boolean isIncludedBy(AbstractLibrary sourceLibrary, AbstractLibrary includeCandidate, Map<AbstractLibrary,List<AbstractLibrary>> includeDependencies) {
		List<AbstractLibrary> includedLibs = includeDependencies.get(sourceLibrary);
		
		return (includedLibs != null) && includedLibs.contains(includeCandidate);
	}
	
	/**
	 * Returns the list of URL's for all of the included libraries.  NOTE: Built-in libraries
	 * cannot be processed by this routine.
	 * 
	 * @param library  the user-defined (.otm) or legacy (.xsd) library
	 * @return List<URL>
	 */
	private List<URL> getIncludeURLs(AbstractLibrary library) {
		URL libraryFolder = URLUtils.getParentURL(library.getLibraryUrl());
		List<URL> includeUrls = new ArrayList<URL>();
		
		for (TLInclude include : library.getIncludes()) {
			try {
				includeUrls.add( URLUtils.getResolvedURL(include.getPath(), libraryFolder) );
				
			} catch (MalformedURLException e) {
				// should never happen - ignore and move on
			}
		}
		return includeUrls;
	}
	
	/**
	 * Adds includes for all model libraries assigned to the same namespace as the source library
	 * provided.
	 * 
	 * @param schema  the target XML schema that will receive the includes
	 * @param sourceLibrary  the source library for which the include directives are being created
	 * @param filenameBuilder  the filename builder to use for all schema locations
	 * @param includeFilter  filter that only allows included dependencies from the specified source library
	 */
	protected void addIncludes(Schema schema, AbstractLibrary sourceLibrary,
			CodeGenerationFilenameBuilder<AbstractLibrary> filenameBuilder, CodeGenerationFilter includeFilter) {
		List<AbstractLibrary> compileTimeDependencies = new ArrayList<AbstractLibrary>();
		List<AbstractLibrary> includeList = sourceLibrary.getOwningModel().getLibrariesForNamespace(
				sourceLibrary.getNamespace());
		
		// Find all of the libraries identified as compile-time dependencies that are assigned
		// to the source library's local namespace
		for (SchemaDeclaration schemaDependency : getCompileTimeDependencies()) {
			if (sourceLibrary.getNamespace().equals(schemaDependency.getNamespace())) {
				AbstractLibrary includedLib = sourceLibrary.getOwningModel().getLibrary(
						schemaDependency.getNamespace(), schemaDependency.getName());
				
				if (includedLib != null) {
					compileTimeDependencies.add(includedLib);
				}
			}
		}
		
		// Add all of the chameleon libraries as candidates for the include list.  If a chameleon is
		// not needed by the local library, we are relying on the filter to omit it from the list
		// of includes.
		includeList.addAll( sourceLibrary.getOwningModel().getLibrariesForNamespace( AnonymousEntityFilter.ANONYMOUS_PSEUDO_NAMESPACE ) );
		
		for (AbstractLibrary includedLib : includeList) {
			if (includedLib == sourceLibrary) {
				continue;
			}
			String includeFolder = "";
			
			if (includedLib instanceof BuiltInLibrary) {
				includeFolder = getBuiltInSchemaOutputLocation();
				
			} else if (includedLib instanceof XSDLibrary) {
				includeFolder = getLegacySchemaOutputLocation();
			}
			
			if ((includeFilter == null) || includeFilter.processLibrary(includedLib)
					|| compileTimeDependencies.contains(includedLib)) {
				addInclude(schema, includeFolder + filenameBuilder.buildFilename(includedLib, "xsd"));
			}
			if ((includedLib instanceof XSDLibrary) && (includeFilter != null)
					&& includeFilter.processExtendedLibrary((XSDLibrary) includedLib)) {
				addInclude(schema,
						includeFolder + new LegacySchemaExtensionFilenameBuilder<AbstractLibrary>(filenameBuilder)
							.buildFilename(includedLib, "xsd"));
			}
		}
	}
	
	/**
	 * Adds an include to the specified schema using the information provided.
	 * 
	 * @param schema  the target XML schema that will receive the include
	 * @param schemaLocation  the schema location of the file to include
	 */
	private void addInclude(Schema schema, String schemaLocation) {
		Include incl = new Include();
		
		incl.setSchemaLocation( schemaLocation );
		schema.getIncludeOrImportOrRedefine().add( incl );
	}
	
	/**
	 * Correlates the global element definitions for the given source object and returns the consolidated
	 * list of generated schema artifacts.
	 * 
	 * @param source  the source entity for which the artifacts were generated
	 * @param codegenElements  the schema elements that were generated for the source entity
	 * @param codegenArtifacts  the non-element schema artifacts that were generated for the source entity
	 * @return CodegenArtifacts
	 */
	protected CodegenArtifacts buildCorrelatedArtifacts(NamedEntity source, FacetCodegenElements codegenElements, CodegenArtifacts codegenArtifacts) {
		List<NamedEntity> entityList = new ArrayList<NamedEntity>();
		CodegenArtifacts correlatedArtifacts = new CodegenArtifacts();
		
		if (source instanceof TLAliasOwner) {
			entityList.addAll( ((TLAliasOwner) source).getAliases() );
		}
		entityList.add(0, source);
		
		for (NamedEntity entity : entityList) {
			correlatedArtifacts.addArtifact( codegenElements.getSubstitutionGroupElement(entity) );
			correlatedArtifacts.addAllArtifacts( codegenElements.getFacetElements(entity) );
		}
		correlatedArtifacts.addAllArtifacts(codegenArtifacts);
		return correlatedArtifacts;
	}
	
}
