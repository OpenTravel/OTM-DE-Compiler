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

package org.opentravel.schemacompiler.transform.tl2jaxb16;

import org.opentravel.ns.ota2.librarymodel_v01_06.BusinessObject;
import org.opentravel.ns.ota2.librarymodel_v01_06.ChoiceObject;
import org.opentravel.ns.ota2.librarymodel_v01_06.ContextDeclaration;
import org.opentravel.ns.ota2.librarymodel_v01_06.CoreObject;
import org.opentravel.ns.ota2.librarymodel_v01_06.EnumerationClosed;
import org.opentravel.ns.ota2.librarymodel_v01_06.EnumerationOpen;
import org.opentravel.ns.ota2.librarymodel_v01_06.ExtensionPointFacet;
import org.opentravel.ns.ota2.librarymodel_v01_06.FacetContextual;
import org.opentravel.ns.ota2.librarymodel_v01_06.Folder;
import org.opentravel.ns.ota2.librarymodel_v01_06.Library;
import org.opentravel.ns.ota2.librarymodel_v01_06.LibraryStatus;
import org.opentravel.ns.ota2.librarymodel_v01_06.NamespaceImport;
import org.opentravel.ns.ota2.librarymodel_v01_06.Resource;
import org.opentravel.ns.ota2.librarymodel_v01_06.Service;
import org.opentravel.ns.ota2.librarymodel_v01_06.Simple;
import org.opentravel.ns.ota2.librarymodel_v01_06.ValueWithAttributes;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFolder;
import org.opentravel.schemacompiler.model.TLInclude;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.model.TLNamespaceImport;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.SymbolResolverTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles the transformation of objects from the <code>TLLibrary</code> type to the <code>Library</code> type.
 * 
 * @author S. Livezey
 */
public class TLLibraryTransformer extends BaseTransformer<TLLibrary,Library,SymbolResolverTransformerContext> {

    private static final Map<Class<?>,Class<?>> model2JaxbClassMappings;

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public Library transform(TLLibrary source) {
        ObjectTransformer<TLContext,ContextDeclaration,SymbolResolverTransformerContext> contextTransformer =
            getTransformerFactory().getTransformer( TLContext.class, ContextDeclaration.class );
        ObjectTransformer<TLFolder,Folder,SymbolResolverTransformerContext> folderTransformer =
            getTransformerFactory().getTransformer( TLFolder.class, Folder.class );
        Library target = new Library();

        target.setName( trimString( source.getName(), false ) );
        target.setVersionScheme( trimString( source.getVersionScheme(), false ) );
        target.setPreviousVersionLocation( trimString( source.getPreviousVersionUri() ) );
        target.setStatus( transformStatus( source.getStatus() ) );
        target.setNamespace( trimString( source.getNamespace(), false ) );
        target.setPrefix( trimString( source.getPrefix() ) );
        target.setComments( trimString( source.getComments() ) );

        if (source.getAlternateCredentialsUrl() != null) {
            target.setAlternateCredentials( source.getAlternateCredentialsUrl().toExternalForm() );
        }

        for (TLContext context : source.getContexts()) {
            target.getContext().add( contextTransformer.transform( context ) );
        }

        if (source.getIncludes() != null) {
            target.getIncludes().addAll( getIncludePaths( source.getIncludes() ) );
        }

        for (TLNamespaceImport modelImport : source.getNamespaceImports()) {
            transformImport( modelImport, target );
        }

        // Perform transforms for all library members
        for (NamedEntity libraryMember : source.getNamedMembers()) {
            Class<?> targetType = model2JaxbClassMappings.get( libraryMember.getClass() );

            if (targetType != null) {
                ObjectTransformer<NamedEntity,?,SymbolResolverTransformerContext> transformer =
                    getTransformerFactory().getTransformer( libraryMember, targetType );
                Object jaxbTerm = transformer.transform( libraryMember );

                if (jaxbTerm instanceof Service) {
                    target.setService( (Service) jaxbTerm );

                } else {
                    target.getTerms().add( jaxbTerm );
                }
            }
        }

        // Perform transforms for the library's folder structure
        for (TLFolder folder : source.getFolders()) {
            target.getFolder().add( folderTransformer.transform( folder ) );
        }

        return target;
    }

    /**
     * Transforms the given namespace import.
     * 
     * @param source the source library being transformed
     * @param target the target JAXB library instance
     */
    private void transformImport(TLNamespaceImport modelImport, Library target) {
        NamespaceImport jaxbImport = new NamespaceImport();

        if ((modelImport.getFileHints() != null) && !modelImport.getFileHints().isEmpty()) {
            StringBuilder fileHints = new StringBuilder();

            for (String fileHint : modelImport.getFileHints()) {
                if (fileHint != null) {
                    if (fileHints.length() > 0) {
                        fileHints.append( ' ' );
                    }
                    fileHints.append( fileHint );
                }
            }
            jaxbImport.setFileHints( fileHints.toString() );
        }
        jaxbImport.setPrefix( trimString( modelImport.getPrefix() ) );
        jaxbImport.setNamespace( trimString( modelImport.getNamespace() ) );
        target.getImport().add( jaxbImport );
    }

    /**
     * Extracts the list of include paths from the given list of <code>TLInclude</code> entities and returns them as a
     * simple list of strings.
     * 
     * @param includes the list of include entities to process
     * @return List&lt;String&gt;
     */
    private List<String> getIncludePaths(List<TLInclude> includes) {
        List<String> roleNames = new ArrayList<>();

        for (TLInclude include : includes) {
            if (include.getPath() != null) {
                roleNames.add( trimString( include.getPath() ) );
            }
        }
        return roleNames;
    }

    /**
     * Converts the JAXB status enumeration value into its equivalent value for the TL model.
     * 
     * @param jaxbStatus the JAXB status enumeration value
     * @return TLLibraryStatus
     */
    private LibraryStatus transformStatus(TLLibraryStatus tlStatus) {
        LibraryStatus jaxbStatus;

        // Default value is DRAFT in the case of a null
        if (tlStatus == null) {
            tlStatus = TLLibraryStatus.DRAFT;
        }

        switch (tlStatus) {
            case UNDER_REVIEW:
                jaxbStatus = LibraryStatus.UNDER_REVIEW;
                break;
            case FINAL:
                jaxbStatus = LibraryStatus.FINAL;
                break;
            case OBSOLETE:
                jaxbStatus = LibraryStatus.OBSOLETE;
                break;
            case DRAFT:
            default:
                jaxbStatus = LibraryStatus.DRAFT;
        }
        return jaxbStatus;
    }

    /**
     * Initializes the JAXB-to-TLModel class mappings required to obtain an <code>ObjectTransformer</code> for arbitrary
     * JAXB objects.
     */
    static {
        try {
            Map<Class<?>,Class<?>> classMappings = new HashMap<>();

            classMappings.put( TLService.class, Service.class );
            classMappings.put( TLBusinessObject.class, BusinessObject.class );
            classMappings.put( TLChoiceObject.class, ChoiceObject.class );
            classMappings.put( TLCoreObject.class, CoreObject.class );
            classMappings.put( TLContextualFacet.class, FacetContextual.class );
            classMappings.put( TLClosedEnumeration.class, EnumerationClosed.class );
            classMappings.put( TLOpenEnumeration.class, EnumerationOpen.class );
            classMappings.put( TLResource.class, Resource.class );
            classMappings.put( TLSimple.class, Simple.class );
            classMappings.put( TLValueWithAttributes.class, ValueWithAttributes.class );
            classMappings.put( TLExtensionPointFacet.class, ExtensionPointFacet.class );
            model2JaxbClassMappings = Collections.unmodifiableMap( classMappings );

        } catch (Exception e) {
            throw new ExceptionInInitializerError( e );
        }
    }

}
