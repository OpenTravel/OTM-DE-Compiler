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

import org.opentravel.schemacompiler.model.ModelElement;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLContext;
import org.opentravel.schemacompiler.model.TLContextualFacet;
import org.opentravel.schemacompiler.model.TLDocumentationOwner;
import org.opentravel.schemacompiler.model.TLEnumValue;
import org.opentravel.schemacompiler.model.TLExtension;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLLibraryMember;
import org.opentravel.schemacompiler.model.TLListFacet;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLOperation;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.model.TLRole;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemacompiler.model.TLService;
import org.opentravel.schemacompiler.model.TLSimpleFacet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Static utility methods that assist with the construction of path strings for <code>TLDocumentationOwner</code>
 * elements within an OTM model.
 */
public class DocumentationPathBuilder {

    private static final List<PathBuilder<?>> pathBuilders;

    /**
     * Private constructor to prevent instantiation.
     */
    private DocumentationPathBuilder() {}

    /**
     * Returns the path for the given documentation owner within its owning library.
     * 
     * @param owner the documentation owner for which to return a path
     * @return String
     */
    public static String buildPath(TLDocumentationOwner owner) {
        BuilderContext context = new BuilderContext();
        ModelElement currentElement = owner;

        while (currentElement != null) {
            boolean buildable = false;

            for (PathBuilder<?> builder : pathBuilders) {
                if (builder.canBuild( currentElement )) {
                    currentElement = builder.buildNext( currentElement, context );
                    buildable = true;
                    break;
                }
            }
            if (!buildable) {
                break;
            }
        }
        return context.getPath();
    }

    /**
     * Encapsulates the context for a path-build operation.
     */
    private static class BuilderContext {

        private StringBuilder docPath = new StringBuilder();

        /**
         * Returns the documentation path that has been constructed.
         * 
         * @return String
         */
        public String getPath() {
            return docPath.toString();
        }

        /**
         * Adds the given path component to the beginning of the current documentation path.
         * 
         * @param pathComponent the path component to add
         */
        public void addPath(String pathComponent) {
            if (pathComponent != null) {
                docPath.insert( 0, pathComponent );
            }
        }

    }

    /**
     * Provides the contract for pre-pending a documentation path component for a particular type of documentation
     * owner.
     * 
     * @param <T> the type of the model element to which the builder applies
     */
    private interface PathBuilder<T extends ModelElement> {

        /**
         * Returns the type of documentation owner to which this handler applies.
         * 
         * @return Class&lt;T&gt;
         */
        Class<T> getOwnerType();

        /**
         * Builds the next component of the documentation path using the current model element provided. The next model
         * element to process is returned by this method.
         * 
         * @param currentElement the model element for which to construct
         * @param context the path builder context
         * @return ModelElement
         */
        ModelElement build(T currentElement, BuilderContext context);

        /**
         * Builds the next component of the documentation path using the current model element provided. The next model
         * element to process is returned by this method.
         * 
         * @param currentElement the model element for which to construct
         * @param context the path builder context
         * @return ModelElement
         */
        @SuppressWarnings("unchecked")
        default ModelElement buildNext(ModelElement currentElement, BuilderContext context) {
            return build( (T) currentElement, context );
        }

        /**
         * Returns true if this builder can process the given model element.
         * 
         * @param element the element to check whether this builder applies
         * @return boolean
         */
        default boolean canBuild(ModelElement element) {
            return (element != null) && getOwnerType().isAssignableFrom( element.getClass() );
        }

    }

    /**
     * Handles building of path components for <code>TLService</code> model elements.
     */
    private static class ServiceBuilder implements PathBuilder<TLService> {

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#getOwnerType()
         */
        @Override
        public Class<TLService> getOwnerType() {
            return TLService.class;
        }

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#build(org.opentravel.schemacompiler.model.ModelElement,
         *      org.opentravel.schemacompiler.util.DocumentationPathBuilder.BuilderContext)
         */
        @Override
        public ModelElement build(TLService currentElement, BuilderContext context) {
            context.addPath( "@SERVICE" );
            return null;
        }

    }

    /**
     * Handles building of path components for <code>TLLibraryMember</code> model elements.
     */
    private static class LibraryMemberBuilder implements PathBuilder<TLLibraryMember> {

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#getOwnerType()
         */
        @Override
        public Class<TLLibraryMember> getOwnerType() {
            return TLLibraryMember.class;
        }

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#build(org.opentravel.schemacompiler.model.ModelElement,
         *      org.opentravel.schemacompiler.util.DocumentationPathBuilder.BuilderContext)
         */
        @Override
        public ModelElement build(TLLibraryMember currentElement, BuilderContext context) {
            context.addPath( currentElement.getLocalName() );
            return null;
        }

    }

    /**
     * Handles building of path components for <code>TLSimpleFacet</code> model elements.
     */
    private static class SimpleFacetBuilder implements PathBuilder<TLSimpleFacet> {

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#getOwnerType()
         */
        @Override
        public Class<TLSimpleFacet> getOwnerType() {
            return TLSimpleFacet.class;
        }

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#build(org.opentravel.schemacompiler.model.ModelElement,
         *      org.opentravel.schemacompiler.util.DocumentationPathBuilder.BuilderContext)
         */
        @Override
        public ModelElement build(TLSimpleFacet currentElement, BuilderContext context) {
            context.addPath( "|@SIMPLE" );
            return currentElement.getOwningEntity();
        }

    }

    /**
     * Handles building of path components for <code>TLContextualFacet</code> model elements.
     */
    private static class ContextualFacetBuilder implements PathBuilder<TLContextualFacet> {

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#getOwnerType()
         */
        @Override
        public Class<TLContextualFacet> getOwnerType() {
            return TLContextualFacet.class;
        }

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#build(org.opentravel.schemacompiler.model.ModelElement,
         *      org.opentravel.schemacompiler.util.DocumentationPathBuilder.BuilderContext)
         */
        @Override
        public ModelElement build(TLContextualFacet currentElement, BuilderContext context) {
            context.addPath( "@CONTEXTUAL:" + currentElement.getLocalName() );
            return null;
        }

    }

    /**
     * Handles building of path components for <code>TLFacet</code> model elements.
     */
    private static class FacetBuilder implements PathBuilder<TLFacet> {

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#getOwnerType()
         */
        @Override
        public Class<TLFacet> getOwnerType() {
            return TLFacet.class;
        }

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#build(org.opentravel.schemacompiler.model.ModelElement,
         *      org.opentravel.schemacompiler.util.DocumentationPathBuilder.BuilderContext)
         */
        @Override
        public ModelElement build(TLFacet currentElement, BuilderContext context) {
            context.addPath( "|@FACET:" + currentElement.getFacetType().toString() );
            return currentElement.getOwningEntity();
        }

    }

    /**
     * Handles building of path components for <code>TLListFacet</code> model elements.
     */
    private static class ListFacetBuilder implements PathBuilder<TLListFacet> {

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#getOwnerType()
         */
        @Override
        public Class<TLListFacet> getOwnerType() {
            return TLListFacet.class;
        }

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#build(org.opentravel.schemacompiler.model.ModelElement,
         *      org.opentravel.schemacompiler.util.DocumentationPathBuilder.BuilderContext)
         */
        @Override
        public ModelElement build(TLListFacet currentElement, BuilderContext context) {
            context.addPath( "|@LISTFACET:" + currentElement.getFacetType().toString() );
            return currentElement.getOwningEntity();
        }

    }

    /**
     * Handles building of path components for <code>TLMemberField</code> model elements.
     */
    @SuppressWarnings("rawtypes")
    private static class MemberFieldBuilder implements PathBuilder<TLMemberField> {

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#getOwnerType()
         */
        @Override
        public Class<TLMemberField> getOwnerType() {
            return TLMemberField.class;
        }

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#build(org.opentravel.schemacompiler.model.ModelElement,
         *      org.opentravel.schemacompiler.util.DocumentationPathBuilder.BuilderContext)
         */
        @Override
        public ModelElement build(TLMemberField currentElement, BuilderContext context) {
            context.addPath( "|" + currentElement.getName() );
            return currentElement.getOwner();
        }

    }

    /**
     * Handles building of path components for <code>TLEnumValue</code> model elements.
     */
    private static class EnumValueBuilder implements PathBuilder<TLEnumValue> {

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#getOwnerType()
         */
        @Override
        public Class<TLEnumValue> getOwnerType() {
            return TLEnumValue.class;
        }

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#build(org.opentravel.schemacompiler.model.ModelElement,
         *      org.opentravel.schemacompiler.util.DocumentationPathBuilder.BuilderContext)
         */
        @Override
        public ModelElement build(TLEnumValue currentElement, BuilderContext context) {
            context.addPath( "|" + currentElement.getLiteral() );
            return currentElement.getOwningEnum();
        }

    }

    /**
     * Handles building of path components for <code>TLRole</code> model elements.
     */
    private static class RoleBuilder implements PathBuilder<TLRole> {

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#getOwnerType()
         */
        @Override
        public Class<TLRole> getOwnerType() {
            return TLRole.class;
        }

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#build(org.opentravel.schemacompiler.model.ModelElement,
         *      org.opentravel.schemacompiler.util.DocumentationPathBuilder.BuilderContext)
         */
        @Override
        public ModelElement build(TLRole currentElement, BuilderContext context) {
            TLRoleEnumeration roleEnum = currentElement.getRoleEnumeration();

            context.addPath( "|@ROLE:" + currentElement.getName() );
            return (roleEnum == null) ? null : roleEnum.getOwningEntity();
        }

    }

    /**
     * Handles building of path components for <code>TLExtension</code> model elements.
     */
    private static class ExtensionBuilder implements PathBuilder<TLExtension> {

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#getOwnerType()
         */
        @Override
        public Class<TLExtension> getOwnerType() {
            return TLExtension.class;
        }

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#build(org.opentravel.schemacompiler.model.ModelElement,
         *      org.opentravel.schemacompiler.util.DocumentationPathBuilder.BuilderContext)
         */
        @Override
        public ModelElement build(TLExtension currentElement, BuilderContext context) {
            context.addPath( "|@EXTENSION" );
            return currentElement.getOwner();
        }

    }

    /**
     * Handles building of path components for <code>TLResourceParentRef</code> model elements.
     */
    private static class ResourceParentRefBuilder implements PathBuilder<TLResourceParentRef> {

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#getOwnerType()
         */
        @Override
        public Class<TLResourceParentRef> getOwnerType() {
            return TLResourceParentRef.class;
        }

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#build(org.opentravel.schemacompiler.model.ModelElement,
         *      org.opentravel.schemacompiler.util.DocumentationPathBuilder.BuilderContext)
         */
        @Override
        public ModelElement build(TLResourceParentRef currentElement, BuilderContext context) {
            context.addPath(
                "|@PREF:" + currentElement.getParentResourceName() + "/" + currentElement.getParentParamGroupName() );
            return currentElement.getOwner();
        }

    }

    /**
     * Handles building of path components for <code>TLParamGroup</code> model elements.
     */
    private static class ParamGroupBuilder implements PathBuilder<TLParamGroup> {

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#getOwnerType()
         */
        @Override
        public Class<TLParamGroup> getOwnerType() {
            return TLParamGroup.class;
        }

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#build(org.opentravel.schemacompiler.model.ModelElement,
         *      org.opentravel.schemacompiler.util.DocumentationPathBuilder.BuilderContext)
         */
        @Override
        public ModelElement build(TLParamGroup currentElement, BuilderContext context) {
            context.addPath( "|@PGRP:" + currentElement.getName() );
            return currentElement.getOwner();
        }

    }

    /**
     * Handles building of path components for <code>TLParameter</code> model elements.
     */
    private static class ParameterBuilder implements PathBuilder<TLParameter> {

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#getOwnerType()
         */
        @Override
        public Class<TLParameter> getOwnerType() {
            return TLParameter.class;
        }

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#build(org.opentravel.schemacompiler.model.ModelElement,
         *      org.opentravel.schemacompiler.util.DocumentationPathBuilder.BuilderContext)
         */
        @Override
        public ModelElement build(TLParameter currentElement, BuilderContext context) {
            TLMemberField<?> fieldRef = currentElement.getFieldRef();
            String fieldName = (fieldRef == null) ? currentElement.getFieldRefName() : fieldRef.getName();

            context.addPath( "|" + fieldName );
            return currentElement.getOwner();
        }

    }

    /**
     * Handles building of path components for <code>TLAction</code> model elements.
     */
    private static class ActionBuilder implements PathBuilder<TLAction> {

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#getOwnerType()
         */
        @Override
        public Class<TLAction> getOwnerType() {
            return TLAction.class;
        }

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#build(org.opentravel.schemacompiler.model.ModelElement,
         *      org.opentravel.schemacompiler.util.DocumentationPathBuilder.BuilderContext)
         */
        @Override
        public ModelElement build(TLAction currentElement, BuilderContext context) {
            context.addPath( "|@ACTION:" + currentElement.getActionId() );
            return currentElement.getOwner();
        }

    }

    /**
     * Handles building of path components for <code>TLActionRequest</code> model elements.
     */
    private static class ActionRequestBuilder implements PathBuilder<TLActionRequest> {

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#getOwnerType()
         */
        @Override
        public Class<TLActionRequest> getOwnerType() {
            return TLActionRequest.class;
        }

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#build(org.opentravel.schemacompiler.model.ModelElement,
         *      org.opentravel.schemacompiler.util.DocumentationPathBuilder.BuilderContext)
         */
        @Override
        public ModelElement build(TLActionRequest currentElement, BuilderContext context) {
            context.addPath( "|@RQ" );
            return currentElement.getOwner();
        }

    }

    /**
     * Handles building of path components for <code>TLActionResponse</code> model elements.
     */
    private static class ActionResponseBuilder implements PathBuilder<TLActionResponse> {

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#getOwnerType()
         */
        @Override
        public Class<TLActionResponse> getOwnerType() {
            return TLActionResponse.class;
        }

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#build(org.opentravel.schemacompiler.model.ModelElement,
         *      org.opentravel.schemacompiler.util.DocumentationPathBuilder.BuilderContext)
         */
        @Override
        public ModelElement build(TLActionResponse currentElement, BuilderContext context) {
            context.addPath(
                "|@RS" + DocumentationPathResolver.getIntegerListIdentity( currentElement.getStatusCodes() ) );
            return currentElement.getOwner();
        }

    }

    /**
     * Handles building of path components for <code>TLActionFacet</code> model elements.
     */
    private static class ActionFacetBuilder implements PathBuilder<TLActionFacet> {

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#getOwnerType()
         */
        @Override
        public Class<TLActionFacet> getOwnerType() {
            return TLActionFacet.class;
        }

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#build(org.opentravel.schemacompiler.model.ModelElement,
         *      org.opentravel.schemacompiler.util.DocumentationPathBuilder.BuilderContext)
         */
        @Override
        public ModelElement build(TLActionFacet currentElement, BuilderContext context) {
            context.addPath( "|@ACTIONFACET:" + currentElement.getName() );
            return currentElement.getOwningResource();
        }

    }

    /**
     * Handles building of path components for <code>TLOperation</code> model elements.
     */
    private static class OperationBuilder implements PathBuilder<TLOperation> {

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#getOwnerType()
         */
        @Override
        public Class<TLOperation> getOwnerType() {
            return TLOperation.class;
        }

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#build(org.opentravel.schemacompiler.model.ModelElement,
         *      org.opentravel.schemacompiler.util.DocumentationPathBuilder.BuilderContext)
         */
        @Override
        public ModelElement build(TLOperation currentElement, BuilderContext context) {
            context.addPath( "|" + currentElement.getName() );
            return currentElement.getOwningService();
        }

    }

    /**
     * Handles building of path components for <code>TLContext</code> model elements.
     */
    private static class ContextBuilder implements PathBuilder<TLContext> {

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#getOwnerType()
         */
        @Override
        public Class<TLContext> getOwnerType() {
            return TLContext.class;
        }

        /**
         * @see org.opentravel.schemacompiler.util.DocumentationPathBuilder.PathBuilder#build(org.opentravel.schemacompiler.model.ModelElement,
         *      org.opentravel.schemacompiler.util.DocumentationPathBuilder.BuilderContext)
         */
        @Override
        public ModelElement build(TLContext currentElement, BuilderContext context) {
            context.addPath( "@CONTEXT:" + currentElement.getContextId() );
            return null;
        }

    }

    /**
     * Initializes the list of resolvers.
     */
    static {
        try {
            List<PathBuilder<?>> builders = new ArrayList<>();

            builders.add( new ServiceBuilder() );
            builders.add( new LibraryMemberBuilder() );
            builders.add( new SimpleFacetBuilder() );
            builders.add( new ContextualFacetBuilder() );
            builders.add( new FacetBuilder() );
            builders.add( new ListFacetBuilder() );
            builders.add( new MemberFieldBuilder() );
            builders.add( new EnumValueBuilder() );
            builders.add( new RoleBuilder() );
            builders.add( new ExtensionBuilder() );
            builders.add( new ResourceParentRefBuilder() );
            builders.add( new ParamGroupBuilder() );
            builders.add( new ParameterBuilder() );
            builders.add( new ActionBuilder() );
            builders.add( new ActionRequestBuilder() );
            builders.add( new ActionResponseBuilder() );
            builders.add( new ActionFacetBuilder() );
            builders.add( new OperationBuilder() );
            builders.add( new ContextBuilder() );
            pathBuilders = Collections.unmodifiableList( builders );

        } catch (Exception e) {
            throw new ExceptionInInitializerError( e );
        }
    }
}
