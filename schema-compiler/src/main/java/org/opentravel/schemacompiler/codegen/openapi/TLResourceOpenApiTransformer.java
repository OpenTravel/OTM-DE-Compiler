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

package org.opentravel.schemacompiler.codegen.openapi;

import org.opentravel.schemacompiler.codegen.CodeGenerationContext;
import org.opentravel.schemacompiler.codegen.CodeGenerationFilter;
import org.opentravel.schemacompiler.codegen.impl.CodeGenerationTransformerContext;
import org.opentravel.schemacompiler.codegen.impl.CodegenArtifacts;
import org.opentravel.schemacompiler.codegen.impl.QualifiedAction;
import org.opentravel.schemacompiler.codegen.json.JsonSchemaCodegenUtils;
import org.opentravel.schemacompiler.codegen.json.model.JsonLibraryInfo;
import org.opentravel.schemacompiler.codegen.json.model.JsonSchemaNamedReference;
import org.opentravel.schemacompiler.codegen.openapi.model.OpenApiComponents;
import org.opentravel.schemacompiler.codegen.openapi.model.OpenApiDocument;
import org.opentravel.schemacompiler.codegen.openapi.model.OpenApiInfo;
import org.opentravel.schemacompiler.codegen.openapi.model.OpenApiOperation;
import org.opentravel.schemacompiler.codegen.openapi.model.OpenApiOtmResource;
import org.opentravel.schemacompiler.codegen.openapi.model.OpenApiPathItem;
import org.opentravel.schemacompiler.codegen.openapi.model.OpenApiServer;
import org.opentravel.schemacompiler.codegen.util.FacetCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.LibraryMember;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLHttpMethod;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLModel;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.transform.ObjectTransformer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Performs the translation from <code>TLResource</code> objects to the OpenAPI model objects used to produce the
 * output.
 */
public class TLResourceOpenApiTransformer extends AbstractOpenApiCodegenTransformer<TLResource,OpenApiDocument> {

    /**
     * @see org.opentravel.schemacompiler.transform.ObjectTransformer#transform(java.lang.Object)
     */
    @Override
    public OpenApiDocument transform(TLResource source) {
        OpenApiDocument openapiDoc = new OpenApiDocument();

        buildInfo( source, openapiDoc );
        buildServer( source, openapiDoc );

        // Construct a map of operations indexed by path template and HTTP method
        ObjectTransformer<QualifiedAction,OpenApiOperation,CodeGenerationTransformerContext> actionTransformer =
            getTransformerFactory().getTransformer( QualifiedAction.class, OpenApiOperation.class );
        Map<String,Map<TLHttpMethod,OpenApiOperation>> operationMap = new HashMap<>();
        List<String> pathList = new ArrayList<>();

        for (QualifiedAction qAction : ResourceCodegenUtils.getQualifiedActions( source )) {
            if (qAction.getAction().isCommonAction()) {
                continue;
            }
            TLActionRequest actionRequest = qAction.getActionRequest();
            String pathTemplate = qAction.getPathTemplate();
            TLHttpMethod httpMethod = actionRequest.getHttpMethod();
            OpenApiOperation operation = actionTransformer.transform( qAction );
            Map<TLHttpMethod,OpenApiOperation> methodMap = operationMap.get( pathTemplate );

            if (methodMap == null) {
                methodMap = new EnumMap<>( TLHttpMethod.class );
                pathList.add( pathTemplate );
                operationMap.put( pathTemplate, methodMap );
            }
            methodMap.put( httpMethod, operation );
        }

        // Use the 'operationMap' to construct the path items for the OpenAPI document
        for (String pathTemplate : pathList) {
            Map<TLHttpMethod,OpenApiOperation> methodMap = operationMap.get( pathTemplate );
            OpenApiPathItem pathItem = new OpenApiPathItem();

            for (Entry<TLHttpMethod,OpenApiOperation> entry : methodMap.entrySet()) {
                switch (entry.getKey()) {
                    case GET:
                        pathItem.setGetOperation( entry.getValue() );
                        break;
                    case POST:
                        pathItem.setPostOperation( entry.getValue() );
                        break;
                    case PUT:
                        pathItem.setPutOperation( entry.getValue() );
                        break;
                    case DELETE:
                        pathItem.setDeleteOperation( entry.getValue() );
                        break;
                    case HEAD:
                        pathItem.setHeadOperation( entry.getValue() );
                        break;
                    case OPTIONS:
                        pathItem.setOptionsOperation( entry.getValue() );
                        break;
                    case PATCH:
                        pathItem.setPatchOperation( entry.getValue() );
                        break;
                    default:
                        // No default action
                }
            }
            pathItem.setPathTemplate( pathTemplate );
            openapiDoc.getPathItems().add( pathItem );
        }

        // If required, generate components for the OpenAPI document
        if (isSingleFileEnabled()) {
            openapiDoc.getComponents().getSchemas().addAll( buildJsonComponents( source.getOwningModel() ) );
        }

        applyBindingStyle( openapiDoc );

        return openapiDoc;
    }

    /**
     * Populates the info section of the OpenAPI document.
     * 
     * @param source the OTM resource supplying the info documentation
     * @param openapiDoc the OpenAPI document
     */
    private void buildInfo(TLResource source, OpenApiDocument openapiDoc) {
        JsonLibraryInfo libraryInfo = jsonUtils.getResourceInfo( source );
        OpenApiOtmResource openapiResource = new OpenApiOtmResource();
        OpenApiInfo info = new OpenApiInfo();

        openapiResource.setNamespace( source.getNamespace() );
        openapiResource.setLocalName( source.getLocalName() );
        openapiDoc.setOtmResource( openapiResource );
        info.setTitle( source.getName() );
        info.setLibraryInfo( libraryInfo );
        info.setVersion( libraryInfo.getLibraryVersion() );
        transformDocumentation( source, info );
        openapiDoc.setInfo( info );
    }

    /**
     * Creates the server URL for the OpenAPI document.
     * 
     * @param source the OTM resource supplying the API version number
     * @param openapiDoc the OpenAPI document
     */
    private void buildServer(TLResource source, OpenApiDocument openapiDoc) {
        String serverUrl = context.getCodegenContext().getValue( CodeGenerationContext.CK_RESOURCE_BASE_URL );
        OpenApiServer server = new OpenApiServer();

        if (serverUrl == null) {
            serverUrl = "/";
        }
        if (!serverUrl.endsWith( "/" )) {
            serverUrl += "/";
        }
        serverUrl += "{apiVersion}";
        server.setUrl( serverUrl );
        server.getVariables().put( "apiVersion", getApiVersion( source ) );
        openapiDoc.getServers().add( server );
    }

    /**
     * Returns the API version that will be appended to the server URL path of the OpenAPI document.
     * 
     * @param source the resource being transformed
     * @return String
     */
    private String getApiVersion(TLResource source) {
        String[] versionParts = source.getVersion().split( "\\." );
        StringBuilder suffix = new StringBuilder();

        if (versionParts.length >= 1) {
            suffix.append( "v" ).append( versionParts[0] );
        }
        return suffix.toString();
    }

    /**
     * Add any extensions provided by the OpenAPI code generation binding style.
     * 
     * @param openapiDoc the OpenAPI document to which binding styles will be applied
     */
    private void applyBindingStyle(OpenApiDocument openapiDoc) {
        if (openapiBindings != null) {
            OpenApiComponents components = openapiDoc.getComponents();

            components.getParameters().addAll( openapiBindings.getGlobalParameters() );
            components.getHeaders().addAll( openapiBindings.getGlobalResponseHeaders() );
            components.getResponses().addAll( openapiBindings.getGlobalResponses() );
            components.getSecuritySchemes().addAll( openapiBindings.getSecuritySchemes() );
        }
    }

    /**
     * Builds the set of all components that should be included in the OpenAPI document. The components that are
     * included are based on the current code generation filter.
     * 
     * @param model the model from which to generate JSON components
     * @return List&lt;JsonSchemaNamedReference&gt;
     */
    private List<JsonSchemaNamedReference> buildJsonComponents(TLModel model) {
        List<JsonSchemaNamedReference> definitions = new ArrayList<>();
        CodeGenerationFilter filter = context.getCodeGenerator().getFilter();

        // Add definitions for all of the OTM entities that are within the
        // scope of the current filter
        for (TLLibrary library : model.getUserDefinedLibraries()) {
            if (!filter.processLibrary( library )) {
                continue;
            }

            for (LibraryMember member : getLibraryMembers( library )) {
                if (member instanceof TLResource) {
                    List<TLActionFacet> actionFacets =
                        ResourceCodegenUtils.getInheritedActionFacets( (TLResource) member );

                    for (TLActionFacet actionFacet : actionFacets) {
                        transformEntity( actionFacet, definitions );
                    }

                } else {
                    transformEntity( member, definitions );
                }
            }
        }
        return definitions;
    }

    /**
     * Returns a list of all library members. This includes declared members as well as any non-local ghost facets that
     * might exist for the library.
     * 
     * @param library the library for which to return the list of all members
     * @return List&lt;LibraryMember&gt;
     */
    private List<LibraryMember> getLibraryMembers(TLLibrary library) {
        List<LibraryMember> allMembers = new ArrayList<>();

        allMembers.addAll( JsonSchemaCodegenUtils.getLatestMinorVersionMembers( library ) );
        allMembers.addAll( FacetCodegenUtils.findNonLocalGhostFacets( library ) );
        return allMembers;
    }

    /**
     * Transforms the given OTM entity and adds it JSON definition(s) the the list provided.
     * 
     * @param entity the OTM entity to be transformed
     * @param definitions the list of JSON definitions being constructed
     */
    private void transformEntity(NamedEntity entity, List<JsonSchemaNamedReference> definitions) {
        ObjectTransformer<NamedEntity,CodegenArtifacts,CodeGenerationTransformerContext> transformer =
            getTransformerFactory().getTransformer( entity, CodegenArtifacts.class );
        CodeGenerationFilter filter = context.getCodeGenerator().getFilter();

        if ((transformer != null) && ((filter == null) || filter.processEntity( entity ))) {
            CodegenArtifacts artifacts = transformer.transform( entity );

            if (artifacts != null) {
                for (JsonSchemaNamedReference memberDef : artifacts
                    .getArtifactsOfType( JsonSchemaNamedReference.class )) {
                    definitions.add( memberDef );
                }
            }
        }
    }

    /**
     * Returns true if single-file Swagger document generation is enabled.
     * 
     * @return boolean
     */
    private boolean isSingleFileEnabled() {
        CodeGenerationContext cgContext = (context == null) ? null : context.getCodegenContext();
        boolean result = false;

        if (cgContext != null) {
            result = "true".equalsIgnoreCase( cgContext.getValue( CodeGenerationContext.CK_ENABLE_SINGLE_FILE ) );
        }
        return result;
    }

}
