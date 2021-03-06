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

package org.opentravel.schemacompiler.transform.jaxb16_2tl;

import org.opentravel.ns.ota2.librarymodel_v01_06.ContextDeclaration;
import org.opentravel.ns.ota2.librarymodel_v01_06.Folder;
import org.opentravel.ns.ota2.librarymodel_v01_06.Library;
import org.opentravel.ns.ota2.librarymodel_v01_06.LibraryStatus;
import org.opentravel.ns.ota2.librarymodel_v01_06.NamespaceImport;
import org.opentravel.ns.ota2.librarymodel_v01_06.Service;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLFolder;
import org.opentravel.schemacompiler.model.TLInclude;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

/**
 * Handles the transformation of objects from the <code>Library</code> type to the <code>TLLibrary</code> type.
 * 
 * @author S. Livezey
 */
public class LibraryTransformer extends BaseTransformer<Library,TLLibrary,DefaultTransformerContext> {

    public static final String DEFAULT_CONTEXT_ID = "default";
    public static final String TARGET_LIBRARY_CONTEXT_ID = "targetLibrary";

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public TLLibrary transform(Library source) {
        ObjectTransformer<ContextDeclaration,TLContext,DefaultTransformerContext> contextTransformer =
            getTransformerFactory().getTransformer( ContextDeclaration.class, TLContext.class );
        ObjectTransformer<Folder,TLFolder,DefaultTransformerContext> folderTransformer =
            getTransformerFactory().getTransformer( Folder.class, TLFolder.class );
        String credentialsUrl = trimString( source.getAlternateCredentials() );
        TLLibrary target = new TLLibrary();

        target.setName( trimString( source.getName() ) );
        target.setVersionScheme( trimString( source.getVersionScheme() ) );
        target.setNamespace( getAdjustedNamespaceURI( trimString( source.getNamespace() ),
            trimString( source.getPatchLevel() ), target.getVersionScheme() ) );
        target.setPreviousVersionUri( trimString( source.getPreviousVersionLocation() ) );
        target.setStatus( transformStatus( source.getStatus() ) );
        target.setPrefix( trimString( source.getPrefix() ) );
        target.setComments( trimString( source.getComments() ) );

        if (credentialsUrl != null) {
            try {
                target.setAlternateCredentialsUrl( new URL( credentialsUrl ) );

            } catch (MalformedURLException e) {
                // Ignore exception - no credentials URL will be assigned
            }
        }

        transformImportsAndIncludes( source, target );

        for (ContextDeclaration sourceContext : source.getContext()) {
            target.addContext( contextTransformer.transform( sourceContext ) );
        }

        // Perform transforms for all library members
        for (Object sourceMember : source.getTerms()) {
            Set<Class<?>> targetTypes = getTransformerFactory().findTargetTypes( sourceMember );
            Class<LibraryMember> targetType =
                (Class<LibraryMember>) (targetTypes.isEmpty() ? null : targetTypes.iterator().next());

            if (targetType != null) {
                ObjectTransformer<Object,LibraryMember,DefaultTransformerContext> memberTransformer =
                    getTransformerFactory().getTransformer( sourceMember, targetType );

                if (memberTransformer != null) {
                    target.addNamedMember( memberTransformer.transform( sourceMember ) );
                }
            }
        }

        if (source.getService() != null) {
            ObjectTransformer<Service,TLService,DefaultTransformerContext> serviceTransformer =
                getTransformerFactory().getTransformer( Service.class, TLService.class );

            target.setService( serviceTransformer.transform( source.getService() ) );
        }

        // Perform transforms for the library's folder structure
        context.setContextCacheEntry( TARGET_LIBRARY_CONTEXT_ID, target );

        for (Folder folder : source.getFolder()) {
            target.addFolder( folderTransformer.transform( folder ) );
        }
        context.setContextCacheEntry( TARGET_LIBRARY_CONTEXT_ID, null );

        return target;
    }

    /**
     * Transforms the imports and includes of the source library.
     * 
     * @param source the source library being transformed
     * @param target the target library being created
     */
    private void transformImportsAndIncludes(Library source, TLLibrary target) {
        for (String includePath : trimStrings( source.getIncludes() )) {
            TLInclude include = new TLInclude();

            include.setPath( includePath );
            target.addInclude( include );
        }

        for (NamespaceImport nsImport : source.getImport()) {
            String[] fileHints = null;

            if ((nsImport.getFileHints() != null) && (nsImport.getFileHints().trim().length() > 0)) {
                fileHints = nsImport.getFileHints().split( "\\s+" );
            }
            target.addNamespaceImport( trimString( nsImport.getPrefix() ), trimString( nsImport.getNamespace() ),
                fileHints );
        }
    }

    /**
     * Converts the JAXB status enumeration value into its equivalent value for the TL model.
     * 
     * @param jaxbStatus the JAXB status enumeration value
     * @return TLLibraryStatus
     */
    private TLLibraryStatus transformStatus(LibraryStatus jaxbStatus) {
        TLLibraryStatus tlStatus;

        // Default value is DRAFT in the case of a null
        if (jaxbStatus == null) {
            jaxbStatus = LibraryStatus.DRAFT;
        }

        switch (jaxbStatus) {
            case UNDER_REVIEW:
                tlStatus = TLLibraryStatus.UNDER_REVIEW;
                break;
            case FINAL:
                tlStatus = TLLibraryStatus.FINAL;
                break;
            case OBSOLETE:
                tlStatus = TLLibraryStatus.OBSOLETE;
                break;
            case DRAFT:
            default:
                tlStatus = TLLibraryStatus.DRAFT;
        }
        return tlStatus;
    }

}
