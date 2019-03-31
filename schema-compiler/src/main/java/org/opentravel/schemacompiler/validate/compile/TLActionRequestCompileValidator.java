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

package org.opentravel.schemacompiler.validate.compile;

import org.opentravel.schemacompiler.codegen.util.ResourceCodegenUtils;
import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLHttpMethod;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLActionRequestBaseValidator;
import org.opentravel.schemacompiler.validate.impl.ResourceUrlValidator;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;
import org.opentravel.schemacompiler.version.MinorVersionHelper;
import org.opentravel.schemacompiler.version.VersionSchemeException;

import java.util.ArrayList;
import java.util.List;

/**
 * Validator for the <code>TLActionRequest</code> class.
 * 
 * @author S. Livezey
 */
public class TLActionRequestCompileValidator extends TLActionRequestBaseValidator {

    private static final String PAYLOAD_TYPE = "payloadType";
    private static final String PATH_TEMPLATE2 = "pathTemplate";
    private static final String PARAM_GROUP = "paramGroup";
    private static final String MIME_TYPES = "mimeTypes";
    private static final String HTTP_METHOD = "httpMethod";

    public static final String ERROR_PARAM_GROUP_REQUIRED = "PARAM_GROUP_REQUIRED";
    public static final String ERROR_INVALID_PARAM_GROUP = "INVALID_PARAM_GROUP";
    public static final String ERROR_CONFLICTING_PATH_TEMPLATE = "CONFLICTING_PATH_TEMPLATE";
    public static final String ERROR_MINOR_VERSION_PATH_TEMPLATE = "MINOR_VERSION_PATH_TEMPLATE";
    public static final String ERROR_GET_REQUEST_PAYLOAD = "GET_REQUEST_PAYLOAD";
    public static final String ERROR_INVALID_ACTION_FACET_REF = "INVALID_ACTION_FACET_REF";
    public static final String WARNING_PATCH_PARTIAL_SUPPORT = "PATCH_PARTIAL_SUPPORT";
    public static final String WARNING_DISCOURAGED_HTTP_METHOD = "DISCOURAGED_HTTP_METHOD";

    private static ResourceUrlValidator urlValidator = new ResourceUrlValidator( true );

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLActionRequest target) {
        TLResource owningResource = (target.getOwner() == null) ? null : target.getOwner().getOwner();
        TLParamGroup paramGroup = target.getParamGroup();
        TLValidationBuilder builder = newValidationBuilder( target );

        builder.setProperty( HTTP_METHOD, target.getHttpMethod() ).setFindingType( FindingType.ERROR ).assertNotNull();

        if (target.getHttpMethod() == TLHttpMethod.PATCH) {
            builder.addFinding( FindingType.WARNING, HTTP_METHOD, WARNING_PATCH_PARTIAL_SUPPORT );
        }
        if ((target.getHttpMethod() == TLHttpMethod.HEAD) || (target.getHttpMethod() == TLHttpMethod.OPTIONS)) {
            builder.addFinding( FindingType.WARNING, HTTP_METHOD, WARNING_DISCOURAGED_HTTP_METHOD,
                target.getHttpMethod() );
        }

        if (paramGroup == null) {
            String paramGroupName = target.getParamGroupName();

            if ((paramGroupName != null) && !paramGroupName.equals( "" )) {
                builder.addFinding( FindingType.ERROR, PARAM_GROUP,
                    TLValidationBuilder.UNRESOLVED_NAMED_ENTITY_REFERENCE, paramGroupName );

            } else if ((target.getPathTemplate() != null)
                && !urlValidator.getPathParameters( target.getPathTemplate() ).isEmpty()) {
                builder.addFinding( FindingType.ERROR, PARAM_GROUP, ERROR_PARAM_GROUP_REQUIRED );
            }

        } else if (owningResource != null) {
            List<TLParamGroup> inheritedParamGroup = ResourceCodegenUtils.getInheritedParamGroups( owningResource );

            if (!inheritedParamGroup.contains( paramGroup )) {
                builder.addFinding( FindingType.ERROR, PARAM_GROUP, ERROR_INVALID_PARAM_GROUP, paramGroup.getName() );
            }
        }

        builder.setProperty( PATH_TEMPLATE2, target.getPathTemplate() ).setFindingType( FindingType.ERROR )
            .assertNotNullOrBlank();
        validatePathTemplate( target.getPathTemplate(), target.getParamGroup(), builder );
        checkConflictingPathTemplate( target, builder );
        checkMinorVersionPathTemplateChange( target, builder );

        validatePayloadType( target, owningResource, builder );

        return builder.getFindings();
    }

    /**
     * Validates the payload type of the request.
     * 
     * @param target the action request being validated
     * @param owningResource the owning resource for the request
     * @param builder the validation builder where errors and warnings will be reported
     */
    private void validatePayloadType(TLActionRequest target, TLResource owningResource, TLValidationBuilder builder) {
        if (target.getPayloadType() != null) {
            TLActionFacet payloadType = target.getPayloadType();

            if (target.getHttpMethod() == TLHttpMethod.GET) {
                builder.addFinding( FindingType.ERROR, PAYLOAD_TYPE, ERROR_GET_REQUEST_PAYLOAD );
            }
            if ((owningResource != null) && !isDeclaredOrInheritedFacet( owningResource, payloadType )) {
                builder.addFinding( FindingType.ERROR, PAYLOAD_TYPE, ERROR_INVALID_ACTION_FACET_REF,
                    payloadType.getName() );
            }
            builder.setProperty( MIME_TYPES, target.getMimeTypes() ).setFindingType( FindingType.ERROR )
                .assertMinimumSize( 1 );

        } else {
            String payloadTypeName = target.getPayloadTypeName();

            if ((payloadTypeName != null) && (payloadTypeName.length() > 0)) {
                builder.addFinding( FindingType.ERROR, PAYLOAD_TYPE,
                    TLValidationBuilder.UNRESOLVED_NAMED_ENTITY_REFERENCE, payloadTypeName );
                builder.setProperty( MIME_TYPES, target.getMimeTypes() ).setFindingType( FindingType.ERROR )
                    .assertMinimumSize( 1 );
            } else {
                builder.setProperty( MIME_TYPES, target.getMimeTypes() ).setFindingType( FindingType.WARNING )
                    .assertMaximumSize( 0 );
            }
        }
    }

    /**
     * Determines whether the path template for the target request conflicts with any of the action requests declared by
     * the owning resource. Two path templates will only be considered conflicting if they also have the same HTTP
     * method assigned.
     * 
     * @param target the action request being validated
     * @param builder the validation builder to which any findings will be posted
     */
    private void checkConflictingPathTemplate(TLActionRequest target, TLValidationBuilder builder) {
        TLResource owningResource = (target.getOwner() == null) ? null : target.getOwner().getOwner();
        String targetTestPath = buildTestPath( target.getPathTemplate() );
        TLHttpMethod targetMethod = target.getHttpMethod();

        if ((owningResource != null) && (targetTestPath != null)) {
            List<TLActionRequest> resourceRequests = getResourceRequests( owningResource );

            for (TLActionRequest request : resourceRequests) {
                if ((request == target) || (request.getHttpMethod() != targetMethod)) {
                    continue; // skip requests that cannot create a conflict
                }
                String testPath = buildTestPath( request.getPathTemplate() );

                if (targetTestPath.equals( testPath )) {
                    builder.addFinding( FindingType.ERROR, PATH_TEMPLATE2, ERROR_CONFLICTING_PATH_TEMPLATE,
                        target.getPathTemplate(), target.getHttpMethod() );
                }
            }
        }
    }

    /**
     * Checks the same-name action in a prior minor version of the owning resource (if one exists) to ensure that the
     * path template did not change.
     * 
     * @param target the action request being validated
     * @param builder the validation builder to which any findings will be posted
     */
    private void checkMinorVersionPathTemplateChange(TLActionRequest target, TLValidationBuilder builder) {
        TLResource owningResource = (target.getOwner() == null) ? null : target.getOwner().getOwner();
        String targetActionId = (target.getOwner() == null) ? null : target.getOwner().getActionId();
        String targetPathTemplate = target.getPathTemplate();

        if ((owningResource != null) && (targetActionId != null) && (targetPathTemplate != null)) {
            MinorVersionHelper versionHelper = new MinorVersionHelper();
            TLResource priorVersionResource = owningResource;
            TLActionRequest priorVersionRequest = null;

            // Start by finding the same-name action in a prior minor version
            // of the resource (if one exists).
            try {
                while ((priorVersionRequest == null)
                    && (priorVersionResource = versionHelper.getVersionExtension( priorVersionResource )) != null) {
                    TLAction priorVersionAction = priorVersionResource.getAction( targetActionId );

                    if (priorVersionAction != null) {
                        priorVersionRequest = ResourceCodegenUtils.getDeclaredOrInheritedRequest( priorVersionAction );
                    }
                }

            } catch (VersionSchemeException e) {
                // Ignore - cannot validate until this error is fixed
            }

            // If a request for the same-name action was found, verify that the path template
            // did not change.
            if ((priorVersionRequest != null) && !targetPathTemplate.equals( priorVersionRequest.getPathTemplate() )) {
                builder.addFinding( FindingType.ERROR, PATH_TEMPLATE2, ERROR_MINOR_VERSION_PATH_TEMPLATE,
                    target.getPathTemplate() );
            }
        }
    }

    /**
     * Returns the list of actions declared or inherited by the given resource.
     * 
     * @param resource the resource for which to return the associated actions
     * @return List&lt;TLActionRequest&gt;
     */
    @SuppressWarnings("unchecked")
    private List<TLActionRequest> getResourceRequests(TLResource resource) {
        String cacheKey = resource.getNamespace() + ":" + resource.getLocalName() + ":resourceRequests";
        List<TLActionRequest> requestList = (List<TLActionRequest>) getContextCacheEntry( cacheKey );

        if (requestList == null) {
            requestList = new ArrayList<>();

            for (TLAction action : ResourceCodegenUtils.getInheritedActions( resource )) {
                TLActionRequest request = ResourceCodegenUtils.getDeclaredOrInheritedRequest( action );

                if (request != null) {
                    requestList.add( request );
                }
            }
            setContextCacheEntry( cacheKey, requestList );
        }
        return requestList;
    }

}
