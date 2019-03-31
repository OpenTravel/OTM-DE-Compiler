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

package org.opentravel.schemacompiler.util;

import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.model.ModelElement;
import org.opentravel.schemacompiler.model.TLAbstractEnumeration;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLExtensionOwner;
import org.opentravel.schemacompiler.model.TLFacetOwner;
import org.opentravel.schemacompiler.model.TLFacetType;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLMemberFieldOwner;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemacompiler.model.TLService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Static utility methods that assist with the resolution of <code>TLDocumentationOwner</code> elements and
 * documentation patches within the OTM model.
 */
public class DocumentationPathResolver {

    private static final List<PathResolver<?>> pathResolvers;

    /**
     * Private constructor to prevent instantiation.
     */
    private DocumentationPathResolver() {}

    /**
     * Returns the documentation owner at the specified path within the given OTM library.
     * 
     * @param docPath the path of the documentation owner to return
     * @param library the OTM library from which to retrieve the documentation owner
     * @return TLDocumentationOwner
     */
    public static TLDocumentationOwner resolve(String docPath, TLLibrary library) {
        ResolverContext context = new ResolverContext( docPath );
        ModelElement currentElement = library;

        while ((currentElement != null) && context.nextPathPart()) {
            boolean resolvable = false;

            for (PathResolver<?> resolver : pathResolvers) {
                if (resolver.canResolve( currentElement )) {
                    currentElement = resolver.resolveNextElement( currentElement, context );
                    resolvable = true;
                    break;
                }
            }
            if (!resolvable) {
                break;
            }
        }
        return context.getDocOwner();
    }

    /**
     * Returns the identity string for the given integer list.
     * 
     * @param intList the integer list for which to return an identity
     * @return String
     */
    protected static String getIntegerListIdentity(List<Integer> intList) {
        StringBuilder identity = new StringBuilder();
        List<Integer> tempIntList = new ArrayList<>( intList );

        Collections.sort( tempIntList );

        for (Integer value : tempIntList) {
            identity.append( ":" + value );
        }
        return identity.toString();
    }

    /**
     * Context used for resolving a documentation owner from a path string.
     */
    private static class ResolverContext {

        private List<String> pathParts;
        private String currentPathPart;
        private TLDocumentationOwner docOwner;

        /**
         * Constructor that provides the documentation path to be resolved.
         * 
         * @param docPath the path of the documentation owner to resolve
         */
        public ResolverContext(String docPath) {
            String[] pathPartsArray =
                ((docPath == null) || (docPath.length() <= 1)) ? new String[0] : docPath.split( "\\|" );

            this.pathParts = new ArrayList<>( Arrays.asList( pathPartsArray ) );
            this.docOwner = null;
        }

        /**
         * Returns the final documentation owner that was resolved.
         *
         * @return TLDocumentationOwner
         */
        public TLDocumentationOwner getDocOwner() {
            return docOwner;
        }

        /**
         * If no further path components are available the given model element is assigned as the final resolved
         * documentation owner.
         *
         * @param element the model element to nominate as a possible final result
         */
        public void setResultIfDone(ModelElement element) {
            if (pathParts.isEmpty() && (element instanceof TLDocumentationOwner)) {
                this.docOwner = (TLDocumentationOwner) element;
            }
        }

        /**
         * Returns the current documentation path component being processed.
         * 
         * @return String
         */
        public String currentPathPart() {
            return currentPathPart;
        }

        /**
         * Advances to the next available path component. If no further path components are available, this method will
         * return false.
         * 
         * @return boolean
         */
        public boolean nextPathPart() {
            boolean hasNext = !pathParts.isEmpty();

            if (hasNext) {
                currentPathPart = pathParts.remove( 0 );
            }
            return hasNext;
        }

    }

    /**
     * Provides the contract for resolving a documentation path component for a particular type of documentation owner.
     * 
     * @param <T> the type of the model element to which the resolver applies
     */
    private interface PathResolver<T extends ModelElement> {

        /**
         * Returns the type of documentation owner to which this handler applies.
         * 
         * @return Class&lt;T&gt;
         */
        Class<T> getOwnerType();

        /**
         * Resolves the next path component and returns the resulting model element.
         * 
         * @param currentElement the most recently resolved model element
         * @param context the context for the path being resolved
         * @return ModelElement
         */
        ModelElement resolve(T currentElement, ResolverContext context);

        /**
         * Resolves the next path component and returns the resulting model element.
         * 
         * @param currentElement the most recently resolved model element
         * @param context the context for the path being resolved
         * @return ModelElement
         */
        @SuppressWarnings("unchecked")
        default ModelElement resolveNextElement(ModelElement currentElement, ResolverContext context) {
            return resolve( (T) currentElement, context );
        }

        /**
         * Returns true if this resolver can process the given model element.
         * 
         * @param element the element to check whether resolution is possible
         * @return boolean
         */
        default boolean canResolve(ModelElement element) {
            return (element != null) && getOwnerType().isAssignableFrom( element.getClass() );
        }

    }

    /**
     * Handles resolution of path components for <code>TLLibrary</code> model elements.
     */
    private static class LibraryResolver implements PathResolver<TLLibrary> {

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathResolver.PathResolver#getOwnerType()
         */
        @Override
        public Class<TLLibrary> getOwnerType() {
            return TLLibrary.class;
        }

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathResolver.PathResolver#resolve(org.opentravel.schemacompiler.model.ModelElement,
         *      org.opentravel.schemacompiler.util.DocumentationPathResolver.ResolverContext)
         */
        @Override
        public ModelElement resolve(TLLibrary currentElement, ResolverContext context) {
            String pathPart = context.currentPathPart();
            ModelElement nextElement = null;

            if (pathPart.equals( "@SERVICE" )) {
                nextElement = currentElement.getService();
                context.setResultIfDone( nextElement );

            } else if (pathPart.startsWith( "@CONTEXT:" )) {
                nextElement = currentElement.getContext( pathPart.substring( 9 ) );
                context.setResultIfDone( nextElement );

            } else if (pathPart.startsWith( "@CONTEXTUAL:" )) {
                nextElement = currentElement.getNamedMember( pathPart.substring( 12 ) );
                context.setResultIfDone( nextElement );

            } else {
                nextElement = currentElement.getNamedMember( pathPart );
                context.setResultIfDone( nextElement );
            }
            return nextElement;
        }

    }

    /**
     * Handles resolution of path components for <code>TLMemberFieldOwner</code> model elements.
     */
    private static class MemberFieldResolver implements PathResolver<TLMemberFieldOwner> {

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathResolver.PathResolver#getOwnerType()
         */
        @Override
        public Class<TLMemberFieldOwner> getOwnerType() {
            return TLMemberFieldOwner.class;
        }

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathResolver.PathResolver#resolve(org.opentravel.schemacompiler.model.ModelElement,
         *      org.opentravel.schemacompiler.util.DocumentationPathResolver.ResolverContext)
         */
        @Override
        public ModelElement resolve(TLMemberFieldOwner currentElement, ResolverContext context) {
            String pathPart = context.currentPathPart();
            ModelElement nextElement = currentElement.getMemberField( pathPart );

            context.setResultIfDone( nextElement );
            return nextElement;
        }

    }

    /**
     * Handles resolution of path components for <code>TLAbstractEnumeration</code> model elements.
     */
    private static class EnumerationResolver implements PathResolver<TLAbstractEnumeration> {

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathResolver.PathResolver#getOwnerType()
         */
        @Override
        public Class<TLAbstractEnumeration> getOwnerType() {
            return TLAbstractEnumeration.class;
        }

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathResolver.PathResolver#resolve(org.opentravel.schemacompiler.model.ModelElement,
         *      org.opentravel.schemacompiler.util.DocumentationPathResolver.ResolverContext)
         */
        @Override
        public ModelElement resolve(TLAbstractEnumeration currentElement, ResolverContext context) {
            String pathPart = context.currentPathPart();

            if (pathPart.startsWith( "@EXTENSION" )) {
                context.setResultIfDone( currentElement.getExtension() );

            } else {
                for (TLEnumValue value : currentElement.getValues()) {
                    if (pathPart.equals( value.getLiteral() )) {
                        context.setResultIfDone( value );
                        break;
                    }
                }
            }
            return null;
        }

    }

    /**
     * Handles resolution of path components for <code>TLService</code> model elements.
     */
    private static class ServiceResolver implements PathResolver<TLService> {

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathResolver.PathResolver#getOwnerType()
         */
        @Override
        public Class<TLService> getOwnerType() {
            return TLService.class;
        }

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathResolver.PathResolver#resolve(org.opentravel.schemacompiler.model.ModelElement,
         *      org.opentravel.schemacompiler.util.DocumentationPathResolver.ResolverContext)
         */
        @Override
        public ModelElement resolve(TLService currentElement, ResolverContext context) {
            String pathPart = context.currentPathPart();
            ModelElement nextElement = currentElement.getOperation( pathPart );

            context.setResultIfDone( nextElement );
            return nextElement;
        }

    }

    /**
     * Handles resolution of path components for <code>TLResource</code> model elements.
     */
    private static class ResourceResolver implements PathResolver<TLResource> {

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathResolver.PathResolver#getOwnerType()
         */
        @Override
        public Class<TLResource> getOwnerType() {
            return TLResource.class;
        }

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathResolver.PathResolver#resolve(org.opentravel.schemacompiler.model.ModelElement,
         *      org.opentravel.schemacompiler.util.DocumentationPathResolver.ResolverContext)
         */
        @Override
        public ModelElement resolve(TLResource currentElement, ResolverContext context) {
            String pathPart = context.currentPathPart();
            ModelElement nextElement;

            if (pathPart.startsWith( "@PREF:" )) {
                nextElement = currentElement.getParentRef( pathPart.substring( 6 ) );
            } else if (pathPart.startsWith( "@PGRP:" )) {
                nextElement = currentElement.getParamGroup( pathPart.substring( 6 ) );
            } else if (pathPart.startsWith( "@ACTION:" )) {
                nextElement = currentElement.getAction( pathPart.substring( 8 ) );
            } else if (pathPart.startsWith( "@ACTIONFACET:" )) {
                nextElement = currentElement.getActionFacet( pathPart.substring( 13 ) );
            } else {
                nextElement = null;
            }
            context.setResultIfDone( nextElement );
            return nextElement;
        }

    }

    /**
     * Handles resolution of path components for <code>TLParamGroup</code> model elements.
     */
    private static class ParamGroupResolver implements PathResolver<TLParamGroup> {

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathResolver.PathResolver#getOwnerType()
         */
        @Override
        public Class<TLParamGroup> getOwnerType() {
            return TLParamGroup.class;
        }

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathResolver.PathResolver#resolve(org.opentravel.schemacompiler.model.ModelElement,
         *      org.opentravel.schemacompiler.util.DocumentationPathResolver.ResolverContext)
         */
        @Override
        public ModelElement resolve(TLParamGroup currentElement, ResolverContext context) {
            String pathPart = context.currentPathPart();

            context.setResultIfDone( currentElement.getParameter( pathPart ) );
            return null;
        }

    }

    /**
     * Handles resolution of path components for <code>TLAction</code> model elements.
     */
    private static class ActionResolver implements PathResolver<TLAction> {

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathResolver.PathResolver#getOwnerType()
         */
        @Override
        public Class<TLAction> getOwnerType() {
            return TLAction.class;
        }

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathResolver.PathResolver#resolve(org.opentravel.schemacompiler.model.ModelElement,
         *      org.opentravel.schemacompiler.util.DocumentationPathResolver.ResolverContext)
         */
        @Override
        public ModelElement resolve(TLAction currentElement, ResolverContext context) {
            String pathPart = context.currentPathPart();

            if (pathPart.equals( "@RQ" )) {
                context.setResultIfDone( currentElement.getRequest() );

            } else if (pathPart.startsWith( "@RS" )) {
                String responseId = pathPart.substring( 3 );

                for (TLActionResponse response : currentElement.getResponses()) {
                    if (responseId.equals( getIntegerListIdentity( response.getStatusCodes() ) )) {
                        context.setResultIfDone( response );
                        break;
                    }
                }
            }
            return null;
        }

    }

    /**
     * Handles resolution of path components for all model elements that are not handled by other resolvers.
     */
    private static class DefaultResolver implements PathResolver<ModelElement> {

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathResolver.PathResolver#getOwnerType()
         */
        @Override
        public Class<ModelElement> getOwnerType() {
            return ModelElement.class;
        }

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathResolver.PathResolver#resolve(org.opentravel.schemacompiler.model.ModelElement,
         *      org.opentravel.schemacompiler.util.DocumentationPathResolver.ResolverContext)
         */
        @Override
        public ModelElement resolve(ModelElement currentElement, ResolverContext context) {
            String pathPart = context.currentPathPart();
            ModelElement nextElement = null;

            if (pathPart.startsWith( "@EXTENSION" )) {
                if (currentElement instanceof TLExtensionOwner) {
                    context.setResultIfDone( ((TLExtensionOwner) currentElement).getExtension() );
                }

            } else if (pathPart.startsWith( "@SIMPLE" )) {
                if (currentElement instanceof TLCoreObject) {
                    context.setResultIfDone( ((TLCoreObject) currentElement).getSimpleFacet() );
                }

            } else if (pathPart.startsWith( "@FACET:" )) {
                TLFacetType facetType = getFacetType( pathPart.substring( 7 ) );

                if (currentElement instanceof TLFacetOwner) {
                    nextElement = FacetCodegenUtils.getFacetOfType( (TLFacetOwner) currentElement, facetType );
                    context.setResultIfDone( nextElement );
                }

            } else if (pathPart.startsWith( "@LISTFACET:" )) {
                TLFacetType facetType = getFacetType( pathPart.substring( 11 ) );

                if (currentElement instanceof TLCoreObject) {
                    TLListFacet listFacet = getListFacet( (TLCoreObject) currentElement, facetType );

                    context.setResultIfDone( listFacet );
                }

            } else if (pathPart.startsWith( "@ROLE:" )) {
                String roleName = pathPart.substring( 6 );
                TLRole role = findRole( ((TLCoreObject) currentElement).getRoleEnumeration(), roleName );

                context.setResultIfDone( role );
                nextElement = role;
            }
            return nextElement;
        }

        /**
         * Returns the list facet of the specified type from the given core object.
         * 
         * @param core the core object from which to return a list facet
         * @param facetType the type of list facet to return
         * @return TLListFacet
         */
        private TLListFacet getListFacet(TLCoreObject core, TLFacetType facetType) {
            TLListFacet listFacet = null;

            if (facetType == TLFacetType.SUMMARY) {
                listFacet = core.getSummaryListFacet();

            } else if (facetType == TLFacetType.DETAIL) {
                listFacet = core.getDetailListFacet();
            }
            return listFacet;
        }

        /**
         * Returns the role with the specified name or null if no such role is defined.
         * 
         * @param roleEnum the role enumeration from which to return a role
         * @param roleName the name of the role to return
         * @return TLRole
         */
        private TLRole findRole(TLRoleEnumeration roleEnum, String roleName) {
            TLRole theRole = null;

            for (TLRole role : roleEnum.getRoles()) {
                if (roleName.equals( role.getName() )) {
                    theRole = role;
                    break;
                }
            }
            return theRole;
        }

        /**
         * Returns the facet type indicated by the given string or null if no such facet type exists.
         * 
         * @param facetTypeStr the string representation of the facet type to return
         * @return TLFacetType
         */
        private static TLFacetType getFacetType(String facetTypeStr) {
            TLFacetType facetType = null;

            try {
                facetType = TLFacetType.valueOf( facetTypeStr );

            } catch (IllegalArgumentException e) {
                // Ignore and return null
            }
            return facetType;
        }

    }

    /**
     * Initializes the list of resolvers.
     */
    static {
        try {
            List<PathResolver<?>> resolvers = new ArrayList<>();

            resolvers.add( new LibraryResolver() );
            resolvers.add( new MemberFieldResolver() );
            resolvers.add( new EnumerationResolver() );
            resolvers.add( new ServiceResolver() );
            resolvers.add( new ResourceResolver() );
            resolvers.add( new ParamGroupResolver() );
            resolvers.add( new ActionResolver() );
            resolvers.add( new DefaultResolver() );
            pathResolvers = Collections.unmodifiableList( resolvers );

        } catch (Exception e) {
            throw new ExceptionInInitializerError( e );
        }
    }

}
