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
package org.opentravel.schemacompiler.transform.jaxb15_2tl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import org.opentravel.ns.ota2.librarymodel_v01_05.ContextDeclaration;
import org.opentravel.ns.ota2.librarymodel_v01_05.Library;
import org.opentravel.ns.ota2.librarymodel_v01_05.LibraryStatus;
import org.opentravel.ns.ota2.librarymodel_v01_05.NamespaceImport;
import org.opentravel.ns.ota2.librarymodel_v01_05.Service;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLInclude;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.transform.ObjectTransformer;
import org.opentravel.schemacompiler.transform.symbols.DefaultTransformerContext;
import org.opentravel.schemacompiler.transform.util.BaseTransformer;

/**
 * Handles the transformation of objects from the <code>Library</code> type to the
 * <code>TLLibrary</code> type.
 * 
 * @author S. Livezey
 */
public class LibraryTransformer extends
        BaseTransformer<Library, TLLibrary, DefaultTransformerContext> {

    public static final String DEFAULT_CONTEXT_ID = "default";
    
    private static final String OBSOLETE_BUILTIN_NS = "http://opentravel.org/common/v02";
    private static final String CURRENT_BUILTIN_NS  = "http://www.opentravel.org/OTM/Common/v0";

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public TLLibrary transform(Library source) {
        ObjectTransformer<ContextDeclaration, TLContext, DefaultTransformerContext> contextTransformer = getTransformerFactory()
                .getTransformer(ContextDeclaration.class, TLContext.class);
        String credentialsUrl = trimString(source.getAlternateCredentials());
        TLLibrary target = new TLLibrary();

        target.setName(trimString(source.getName()));
        target.setVersionScheme(trimString(source.getVersionScheme()));
        target.setNamespace(getAdjustedNamespaceURI(trimString(source.getNamespace()),
                trimString(source.getPatchLevel()), target.getVersionScheme()));
        target.setPreviousVersionUri(trimString(source.getPreviousVersionLocation()));
        target.setStatus(transformStatus(source.getStatus()));
        target.setPrefix(trimString(source.getPrefix()));
        target.setComments(trimString(source.getComments()));

        if (credentialsUrl != null) {
            try {
                target.setAlternateCredentialsUrl(new URL(credentialsUrl));

            } catch (MalformedURLException e) {
                // Ignore exception - no credentials URL will be assigned
            }
        }

        for (String _include : trimStrings(source.getIncludes())) {
            TLInclude include = new TLInclude();

            include.setPath(_include);
            target.addInclude(include);
        }

        for (NamespaceImport nsImport : source.getImport()) {
        	if (!handleObsoleteBuiltIn( nsImport, target )) {
                String[] fileHints = null;

                if ((nsImport.getFileHints() != null) && (nsImport.getFileHints().trim().length() > 0)) {
                    fileHints = nsImport.getFileHints().split("\\s+");
                }
                target.addNamespaceImport(trimString(nsImport.getPrefix()),
                        trimString(nsImport.getNamespace()), fileHints);
        	}
        }

        for (ContextDeclaration sourceContext : source.getContext()) {
            target.addContext(contextTransformer.transform(sourceContext));
        }

        // Perform transforms for all library members
        for (Object sourceMember : source.getTerms()) {
            Set<Class<?>> targetTypes = getTransformerFactory().findTargetTypes(sourceMember);
            Class<LibraryMember> targetType = (Class<LibraryMember>) ((targetTypes.size() == 0) ? null
                    : targetTypes.iterator().next());

            if (targetType != null) {
                ObjectTransformer<Object, LibraryMember, DefaultTransformerContext> memberTransformer = getTransformerFactory()
                        .getTransformer(sourceMember, targetType);

                if (memberTransformer != null) {
                    target.addNamedMember(memberTransformer.transform(sourceMember));
                }
            }
        }
        if (source.getService() != null) {
            ObjectTransformer<Service, TLService, DefaultTransformerContext> serviceTransformer = getTransformerFactory()
                    .getTransformer(Service.class, TLService.class);

            target.setService(serviceTransformer.transform(source.getService()));
        }

        return target;
    }

    /**
     * Converts the JAXB status enumeration value into its equivalent value for the TL model.
     * 
     * @param jaxbStatus
     *            the JAXB status enumeration value
     * @return TLLibraryStatus
     */
    private TLLibraryStatus transformStatus(LibraryStatus jaxbStatus) {
        TLLibraryStatus tlStatus;

        // Default value is DRAFT in the case of a null
        if (jaxbStatus == null) {
            jaxbStatus = LibraryStatus.DRAFT;
        }

        switch (jaxbStatus) {
            case FINAL:
                tlStatus = TLLibraryStatus.FINAL;
                break;

            case DRAFT:
            default:
                tlStatus = TLLibraryStatus.DRAFT;
        }
        return tlStatus;
    }
    
    /**
     * If the given include represents the obsolete built-in namespace (see below),
     * it will be mapped to the current built-in namespace.
     * 
     * <ul>
     *   <li>Obsolete Namespace: <code>http://opentravel.org/common/v02</code></li>
     *   <li>Current Namespace: <code>http://www.opentravel.org/OTM/Common/v0</code></li>
     * </ul>
     * 
     * @param nsImport  the OTM import to process
     * @param target  the target library that will contain the namespace reference
     * @return boolean (true if the mapping was processed; false otherwise)
     */
    private boolean handleObsoleteBuiltIn(NamespaceImport nsImport, TLLibrary target) {
    	String importNS = (nsImport == null) ? null : trimString( nsImport.getNamespace() );
    	String fileHints = (nsImport == null) ? null : trimString( nsImport.getFileHints() );
    	boolean mappingProcessed = false;
    	
    	if ((fileHints == null) && OBSOLETE_BUILTIN_NS.equals( importNS )) {
    		if (target.getNamespace().equals(CURRENT_BUILTIN_NS)) {
    			// If the library is already in the target namespace of the built-in, no
    			// import/include is required since the built-in is automatically loaded
    			// by the compiler.
    			
    		} else {
                target.addNamespaceImport( trimString( nsImport.getPrefix() ),
                		CURRENT_BUILTIN_NS, null );
    		}
    		mappingProcessed = true;
    	}
    	return mappingProcessed;
    }
    
}
