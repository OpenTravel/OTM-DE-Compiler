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

package org.opentravel.schemacompiler.codegen.xsd;

import org.opentravel.schemacompiler.codegen.CodeGenerationFilenameBuilder;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.CodeGenerator;
import org.opentravel.schemacompiler.codegen.CodeGeneratorFactory;
import org.opentravel.schemacompiler.codegen.impl.AbstractCodegenTransformer;
import org.opentravel.schemacompiler.codegen.impl.AbstractJaxbCodeGenerator;
import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.impl.LegacySchemaExtensionFilenameBuilder;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegate;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenDelegateFactory;
import org.opentravel.schemacompiler.codegen.xsd.facet.FacetCodegenElements;
import org.opentravel.schemacompiler.ioc.SchemaCompilerApplicationContext;
import org.opentravel.schemacompiler.ioc.SchemaDeclaration;
import org.opentravel.schemacompiler.ioc.SchemaDeclarations;
import org.opentravel.schemacompiler.ioc.SchemaDependency;
import org.opentravel.schemacompiler.model.AbstractLibrary;
import org.opentravel.schemacompiler.model.BuiltInLibrary;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLInclude;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.XSDLibrary;
import org.opentravel.schemacompiler.transform.AnonymousEntityFilter;
import org.opentravel.schemacompiler.transform.TransformerFactory;
import org.opentravel.schemacompiler.util.URLUtils;
import org.springframework.context.ApplicationContext;
import org.w3._2001.xmlschema.FormChoice;
import org.w3._2001.xmlschema.Import;
import org.w3._2001.xmlschema.Include;
import org.w3._2001.xmlschema.Schema;

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

/**
 * Base class for all <code>ObjectTransformer</code> implementations that are part of the XML schema code generation
 * subsystem.
 * 
 * @param <S> the source type of the object transformation
 * @param <T> the target type of the object transformation
 * @author S. Livezey
 */
public abstract class AbstractXsdTransformer<S, T> extends AbstractCodegenTransformer<S,T> {

    protected static org.w3._2001.xmlschema.ObjectFactory jaxbObjectFactory =
        new org.w3._2001.xmlschema.ObjectFactory();

    /**
     * Adds the schemas associated with the given compile-time dependency to the current list of dependencies maintained
     * by the orchestrating code generator.
     * 
     * @param dependency the compile-time dependency to add
     */
    protected void addCompileTimeDependency(SchemaDependency dependency) {
        addCompileTimeDependency( dependency.getSchemaDeclaration() );
    }

    /**
     * Adds the schemas associated with the given compile-time dependency to the current list of dependencies maintained
     * by the orchestrating code generator.
     * 
     * @param schemaDeclaration the compile-time schema declaration to add
     */
    protected void addCompileTimeDependency(SchemaDeclaration schemaDeclaration) {
        CodeGenerator<?> codeGenerator = context.getCodeGenerator();

        if (codeGenerator instanceof AbstractJaxbCodeGenerator) {
            ((AbstractJaxbCodeGenerator<?>) codeGenerator).addCompileTimeDependency( schemaDeclaration );
        }
    }

    /**
     * Returns the list of compile-time schema dependencies that have been reported during code generation.
     * 
     * @return Collection&lt;SchemaDeclaration&gt;
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
     * Returns a new JAXB <code>Schema</code> instance using the namespace and version information provided.
     * 
     * @param targetNamespace the target namespace of the schema
     * @param version the version identifier of the schema
     * @return Schema
     */
    protected Schema createSchema(String targetNamespace, String version) {
        Schema schema = new Schema();

        schema.setAttributeFormDefault( FormChoice.UNQUALIFIED );
        schema.setElementFormDefault( FormChoice.QUALIFIED );
        schema.setVersion( version );
        schema.setTargetNamespace( targetNamespace );
        return schema;
    }

    /**
     * Adds the specified imports to the target XML schema.
     * 
     * @param schema the target XML schema that will receive the imports
     * @param sourceLibrary the source library for which the include directives are being created
     * @param filenameBuilder the filename builder to use for all schema locations
     * @param importFilter filter that only allows imported dependencies from the specified source library
     */
    protected void addImports(Schema schema, AbstractLibrary sourceLibrary,
        CodeGenerationFilenameBuilder<AbstractLibrary> filenameBuilder, CodeGenerationFilter importFilter) {
        String builtInFolder = getBuiltInSchemaOutputLocation();
        List<AbstractLibrary> importList = new ArrayList<>();
        String targetNamespace = schema.getTargetNamespace();
        TLModel model = sourceLibrary.getOwningModel();

        addImpliedBuiltIns( schema, sourceLibrary, importList, builtInFolder );

        // Identify libraries from the model that require imports
        for (AbstractLibrary importCandidate : model.getAllLibraries()) {
            String namespace = importCandidate.getNamespace();

            // Skip libraries that are in the target, chameleon, or the XML schema namespaces
            if (namespace.equals( targetNamespace ) || namespace.equals( XMLConstants.W3C_XML_SCHEMA_NS_URI )
                || namespace.equals( AnonymousEntityFilter.ANONYMOUS_PSEUDO_NAMESPACE )) {
                continue;
            }

            // Add each library to the list if the filter allows it
            if (!importList.contains( importCandidate )
                && ((importFilter == null) || importFilter.processLibrary( importCandidate ))) {
                importList.add( importCandidate );
            }
        }

        filterRedundantImports( importList );
        addImportStatements( schema, importList, builtInFolder, filenameBuilder, importFilter );
        addCompileTimeDependency( SchemaDeclarations.OTA2_APPINFO_SCHEMA );
    }

    /**
     * Identify imports for any compile-time dependencies that would not otherwise be covered by the list of explicitly
     * imported namespaces.
     * 
     * @param schema the schema to which implied imports will be added
     * @param sourceLibrary the source library for which imports are being added
     * @param importList the list of all imports being created
     * @param builtInFolder the relative path location of all built-in schema dependencies
     */
    private void addImpliedBuiltIns(Schema schema, AbstractLibrary sourceLibrary, List<AbstractLibrary> importList,
        String builtInFolder) {
        TLModel model = sourceLibrary.getOwningModel();

        for (SchemaDeclaration schemaDependency : getCompileTimeDependencies()) {
            AbstractLibrary builtInImport =
                model.getLibrary( schemaDependency.getNamespace(), schemaDependency.getName() );
            String schemaLocation =
                builtInFolder + schemaDependency.getFilename( CodeGeneratorFactory.XSD_TARGET_FORMAT );

            if (builtInImport != null) {
                if (builtInImport != sourceLibrary) {
                    importList.add( builtInImport );
                }

            } else {
                // For now, only add import statements for schemas that are not formally
                // identified as built-in libraries.
                if (schemaLocation.toLowerCase().endsWith( ".xsd" )) {
                    addImport( schema, schemaDependency.getNamespace(),
                        builtInFolder + schemaDependency.getFilename( CodeGeneratorFactory.XSD_TARGET_FORMAT ), false );
                }
            }
        }
    }

    /**
     * Removes redundant imports from the given list that would be covered by includes within the dependent files.
     * 
     * @param importList the list of imports to be filtered of redundancies
     */
    private void filterRedundantImports(List<AbstractLibrary> importList) {
        Map<AbstractLibrary,List<AbstractLibrary>> includeDependencies = getIncludeDependencies( importList );
        AbstractLibrary[] importArray = importList.toArray( new AbstractLibrary[importList.size()] );

        for (AbstractLibrary importedLibrary : importArray) {
            Iterator<AbstractLibrary> redundantCandidates = importList.iterator();

            while (redundantCandidates.hasNext()) {
                AbstractLibrary redundantCandidate = redundantCandidates.next();

                if (importedLibrary == redundantCandidate) {
                    continue;
                }
                if (isIncludedBy( importedLibrary, redundantCandidate, includeDependencies )) {
                    // protects against omissions when circular includes are defined
                    includeDependencies.remove( redundantCandidate );
                    redundantCandidates.remove();
                }
            }
        }
    }

    /**
     * Add import statements for all of the import dependencies in the given list to the schema provided.
     * 
     * @param schema the schema to which import statements will be added
     * @param importList the list of imported libraries for which to create imports
     * @param builtInFolder the relative path location of all built-in schema dependencies
     * @param filenameBuilder the filename builder that maps model libraries to schema filenames
     * @param importFilter code generation filter for the current operation
     */
    private void addImportStatements(Schema schema, List<AbstractLibrary> importList, String builtInFolder,
        CodeGenerationFilenameBuilder<AbstractLibrary> filenameBuilder, CodeGenerationFilter importFilter) {
        for (AbstractLibrary importedLib : importList) {
            String namespace = importedLib.getNamespace();
            String importFolder = "";

            if (importedLib instanceof BuiltInLibrary) {
                importFolder = getBuiltInSchemaOutputLocation();

            } else if (importedLib instanceof XSDLibrary) {
                importFolder = getLegacySchemaOutputLocation();
            }

            if ((importedLib instanceof XSDLibrary) && (importFilter != null)
                && importFilter.processExtendedLibrary( (XSDLibrary) importedLib )) {
                addImport( schema, namespace,
                    importFolder + new LegacySchemaExtensionFilenameBuilder<AbstractLibrary>( filenameBuilder )
                        .buildFilename( importedLib, "xsd" ),
                    true );
            } else {
                addImport( schema, namespace, importFolder + filenameBuilder.buildFilename( importedLib, "xsd" ),
                    false );
            }
        }
        addImport( schema, SchemaDeclarations.OTA2_APPINFO_SCHEMA.getNamespace(), builtInFolder
            + SchemaDeclarations.OTA2_APPINFO_SCHEMA.getFilename( CodeGeneratorFactory.XSD_TARGET_FORMAT ), false );
    }

    /**
     * Adds an import to the specified schema using the information provided.
     * 
     * @param schema the target XML schema that will receive the import
     * @param namespace the namespace to be imported
     * @param defaultSchemaLocation the schema location of the file to import if one has not been specified in the
     *        <code>ImportSchemaLocations</code>
     * @param isLegacyExtensionImport indicates that this is an import of a legacy extension schema
     */
    private void addImport(Schema schema, String namespace, String defaultSchemaLocation,
        boolean isLegacyExtensionImport) {
        ImportSchemaLocations importLocations =
            ((AbstractXsdCodeGenerator<?>) context.getCodeGenerator()).getImportSchemaLocations();
        Import jaxbImport = new Import();
        String schemaLocation = defaultSchemaLocation;

        if ((importLocations != null) && !isLegacyExtensionImport) {
            File schemaFile = importLocations.getSchemaLocation( namespace );

            if (schemaFile != null) {
                URL outputFolderUrl = URLUtils.toURL( getBaseOutputFolder() );
                URL schemaUrl = URLUtils.toURL( schemaFile );

                schemaLocation = URLUtils.getRelativeURL( outputFolderUrl, schemaUrl, false );
                importLocations.importAddedForNamespace( namespace );
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
        return XsdCodegenUtils.getBaseOutputFolder( context.getCodegenContext() );
    }

    /**
     * Returns a map that associates each of the given libraries with zero or more other libraries that are declared as
     * includes. If no includes are defined for a library, the value side of the resulting map will be an empty list.
     * 
     * @param libraryList the list of libraries to include in the dependency analysis
     * @return Map&lt;AbstractLibrary,List&lt;AbstractLibrary&gt;&gt;
     */
    private Map<AbstractLibrary,List<AbstractLibrary>> getIncludeDependencies(List<AbstractLibrary> libraryList) {
        Map<AbstractLibrary,List<AbstractLibrary>> includeDependencies = new HashMap<>();
        ApplicationContext appContext = SchemaCompilerApplicationContext.getContext();

        for (AbstractLibrary library : libraryList) {
            List<AbstractLibrary> includedLibraries = new ArrayList<>();

            if (library instanceof BuiltInLibrary) {
                includedLibraries.addAll( getBuiltInIncludes( (BuiltInLibrary) library, appContext ) );

            } else {
                List<URL> includeUrls = getIncludeUrls( library );

                for (AbstractLibrary includeCandidate : libraryList) {
                    if (includeUrls.contains( includeCandidate.getLibraryUrl() )) {
                        includedLibraries.add( includeCandidate );
                    }
                }
            }
            includeDependencies.put( library, includedLibraries );
        }
        return includeDependencies;
    }

    /**
     * Returns the include dependencies of the given built-in library.
     * 
     * @param builtIn the built-in library for which to return include dependencies
     * @param includedLibraries the list of included libraries that have been collected
     * @param appContext the spring application context from which the schema dependencies are obtained
     */
    private List<AbstractLibrary> getBuiltInIncludes(BuiltInLibrary builtIn, ApplicationContext appContext) {
        List<AbstractLibrary> includedLibraries = new ArrayList<>();

        // Search the list of dependencies for the underlying schema declaration
        for (String builtInDependency : builtIn.getSchemaDeclaration().getDependencies()) {
            SchemaDeclaration dependency = (SchemaDeclaration) appContext.getBean( builtInDependency );

            // The dependency is an include if both namespaces are the same
            if (dependency.getNamespace().equals( builtIn.getNamespace() )) {
                AbstractLibrary dependentLib =
                    builtIn.getOwningModel().getLibrary( dependency.getNamespace(), dependency.getName() );

                if (dependentLib != null) {
                    includedLibraries.add( dependentLib );
                }
            }
        }
        return includedLibraries;
    }

    /**
     * Returns true if the 'includeCandidate' is included by the given 'sourceLibrary'.
     * 
     * @param sourceLibrary the source library of the potential include dependency
     * @param includeCandidate the candidate target library of the include
     * @param includeDependencies the pre-calculated include dependencies
     * @return boolean
     */
    private boolean isIncludedBy(AbstractLibrary sourceLibrary, AbstractLibrary includeCandidate,
        Map<AbstractLibrary,List<AbstractLibrary>> includeDependencies) {
        List<AbstractLibrary> includedLibs = includeDependencies.get( sourceLibrary );

        return (includedLibs != null) && includedLibs.contains( includeCandidate );
    }

    /**
     * Returns the list of URL's for all of the included libraries. NOTE: Built-in libraries cannot be processed by this
     * routine.
     * 
     * @param library the user-defined (.otm) or legacy (.xsd) library
     * @return List&lt;URL&gt;
     */
    private List<URL> getIncludeUrls(AbstractLibrary library) {
        URL libraryFolder = URLUtils.getParentURL( library.getLibraryUrl() );
        List<URL> includeUrls = new ArrayList<>();

        for (TLInclude include : library.getIncludes()) {
            try {
                includeUrls.add( URLUtils.getResolvedURL( include.getPath(), libraryFolder ) );

            } catch (MalformedURLException e) {
                // should never happen - ignore and move on
            }
        }
        return includeUrls;
    }

    /**
     * Adds includes for all model libraries assigned to the same namespace as the source library provided.
     * 
     * @param schema the target XML schema that will receive the includes
     * @param sourceLibrary the source library for which the include directives are being created
     * @param filenameBuilder the filename builder to use for all schema locations
     * @param includeFilter filter that only allows included dependencies from the specified source library
     */
    protected void addIncludes(Schema schema, AbstractLibrary sourceLibrary,
        CodeGenerationFilenameBuilder<AbstractLibrary> filenameBuilder, CodeGenerationFilter includeFilter) {
        List<AbstractLibrary> compileTimeDependencies = new ArrayList<>();
        List<AbstractLibrary> includeList = getIncludedLibraries( sourceLibrary, compileTimeDependencies );

        // Add all of the chameleon libraries as candidates for the include list. If a chameleon is
        // not needed by the local library, we are relying on the filter to omit it from the list
        // of includes.
        includeList.addAll( sourceLibrary.getOwningModel()
            .getLibrariesForNamespace( AnonymousEntityFilter.ANONYMOUS_PSEUDO_NAMESPACE ) );

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

            if ((includeFilter == null) || includeFilter.processLibrary( includedLib )
                || compileTimeDependencies.contains( includedLib )) {
                addInclude( schema, includeFolder + filenameBuilder.buildFilename( includedLib, "xsd" ) );
            }
            if ((includedLib instanceof XSDLibrary) && (includeFilter != null)
                && includeFilter.processExtendedLibrary( (XSDLibrary) includedLib )) {
                addInclude( schema,
                    includeFolder + new LegacySchemaExtensionFilenameBuilder<AbstractLibrary>( filenameBuilder )
                        .buildFilename( includedLib, "xsd" ) );
            }
        }
    }

    /**
     * Find all of the libraries identified as compile-time dependencies that are assigned to the source library's local
     * namespace.
     * 
     * @param sourceLibrary the library for which to return included dependencies
     * @param compileTimeDependencies the list of all compile-time dependencies that have been collected
     * @return List&lt;AbstractLibrary&gt;
     */
    private List<AbstractLibrary> getIncludedLibraries(AbstractLibrary sourceLibrary,
        List<AbstractLibrary> compileTimeDependencies) {
        List<AbstractLibrary> includeList;
        includeList = sourceLibrary.getOwningModel().getLibrariesForNamespace( sourceLibrary.getNamespace() );

        for (SchemaDeclaration schemaDependency : getCompileTimeDependencies()) {
            if (sourceLibrary.getNamespace().equals( schemaDependency.getNamespace() )) {
                AbstractLibrary includedLib = sourceLibrary.getOwningModel()
                    .getLibrary( schemaDependency.getNamespace(), schemaDependency.getName() );

                if (includedLib != null) {
                    compileTimeDependencies.add( includedLib );
                }
            }
        }
        return includeList;
    }

    /**
     * Adds an include to the specified schema using the information provided.
     * 
     * @param schema the target XML schema that will receive the include
     * @param schemaLocation the schema location of the file to include
     */
    private void addInclude(Schema schema, String schemaLocation) {
        Include incl = new Include();

        incl.setSchemaLocation( schemaLocation );
        schema.getIncludeOrImportOrRedefine().add( incl );
    }

    /**
     * Correlates the global element definitions for the given source object and returns the consolidated list of
     * generated schema artifacts.
     * 
     * @param source the source entity for which the artifacts were generated
     * @param codegenElements the schema elements that were generated for the source entity
     * @param codegenArtifacts the non-element schema artifacts that were generated for the source entity
     * @return CodegenArtifacts
     */
    protected CodegenArtifacts buildCorrelatedArtifacts(NamedEntity source, FacetCodegenElements codegenElements,
        CodegenArtifacts codegenArtifacts) {
        List<NamedEntity> entityList = new ArrayList<>();
        CodegenArtifacts correlatedArtifacts = new CodegenArtifacts();

        if (source instanceof TLAliasOwner) {
            entityList.addAll( ((TLAliasOwner) source).getAliases() );
        }
        entityList.add( 0, source );

        for (NamedEntity entity : entityList) {
            correlatedArtifacts.addArtifact( codegenElements.getSubstitutionGroupElement( entity ) );
            correlatedArtifacts.addAllArtifacts( codegenElements.getFacetElements( entity ) );
        }
        correlatedArtifacts.addAllArtifacts( codegenArtifacts );
        return correlatedArtifacts;
    }

    /**
     * Recursively generates schema artifacts for all contextual facets in the given list.
     * 
     * @param facetList the list of contextual facets
     * @param delegateFactory the facet code generation delegate factory
     * @param elementArtifacts the container for all generated schema elements
     * @param otherArtifacts the container for all generated non-element schema artifacts
     */
    protected void generateContextualFacetArtifacts(List<TLContextualFacet> facetList,
        FacetCodegenDelegateFactory delegateFactory, FacetCodegenElements elementArtifacts,
        CodegenArtifacts otherArtifacts) {
        CodeGenerationFilter filter = getCodegenFilter();

        for (TLContextualFacet facet : facetList) {
            if ((filter != null) && !filter.processEntity( facet )) {
                continue;
            }
            if (facet.isLocalFacet()) {
                List<TLContextualFacet> ghostFacets = FacetCodegenUtils.findGhostFacets( facet, facet.getFacetType() );

                generateFacetArtifacts( delegateFactory.getDelegate( facet ), elementArtifacts, otherArtifacts, false );
                generateContextualFacetArtifacts( facet.getChildFacets(), delegateFactory, elementArtifacts,
                    otherArtifacts );
                generateContextualFacetArtifacts( ghostFacets, delegateFactory, elementArtifacts, otherArtifacts );
            }
        }
    }

    /**
     * Utility method that generates both element and non-element schema content for the source facet of the given
     * delegate.
     * 
     * @param facetDelegate the facet code generation delegate
     * @param elementArtifacts the container for all generated schema elements
     * @param otherArtifacts the container for all generated non-element schema artifacts
     * @param forceGeneration flag indicating whether the artifacts should generated regardless of the code generation
     *        filter status
     */
    protected void generateFacetArtifacts(FacetCodegenDelegate<? extends TLFacet> facetDelegate,
        FacetCodegenElements elementArtifacts, CodegenArtifacts otherArtifacts, boolean forceGeneration) {
        CodeGenerationFilter filter = forceGeneration ? null : getCodegenFilter();

        if ((filter == null) || filter.processEntity( facetDelegate.getSourceFacet() )) {
            elementArtifacts.addAll( facetDelegate.generateElements() );
            otherArtifacts.addAllArtifacts( facetDelegate.generateArtifacts() );
        }
    }

    /**
     * Returns the code generation filter (if any) that is associated with the current transformation context.
     * 
     * @return CodeGenerationFilter
     */
    protected CodeGenerationFilter getCodegenFilter() {
        TransformerFactory<CodeGenerationTransformerContext> factory = getTransformerFactory();
        CodeGenerationTransformerContext context = (factory == null) ? null : factory.getContext();
        CodeGenerator<?> codeGenerator = (context == null) ? null : context.getCodeGenerator();

        return (codeGenerator == null) ? null : codeGenerator.getFilter();
    }

}
