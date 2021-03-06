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

package org.opentravel.schemacompiler.version.handlers;

import org.opentravel.schemacompiler.model.TLAction;
import org.opentravel.schemacompiler.model.TLActionFacet;
import org.opentravel.schemacompiler.model.TLActionRequest;
import org.opentravel.schemacompiler.model.TLActionResponse;
import org.opentravel.schemacompiler.model.TLLibrary;
import org.opentravel.schemacompiler.model.TLMemberField;
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLPatchableFacet;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.transform.util.ModelReferenceResolver;
import org.opentravel.schemacompiler.util.ModelElementCloner;
import org.opentravel.schemacompiler.version.VersionSchemeException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <code>VersionHandler</code> implementation for <code>TLResource</code> model entities.
 * 
 * @author S. Livezey
 */
public class TLResourceVersionHandler extends TLExtensionOwnerVersionHandler<TLResource> {

    /**
     * @see org.opentravel.schemacompiler.version.handlers.VersionHandler#createNewVersion(org.opentravel.schemacompiler.version.Versioned,
     *      org.opentravel.schemacompiler.model.TLLibrary)
     */
    @Override
    public TLResource createNewVersion(TLResource origVersion, TLLibrary targetLibrary) {
        ModelElementCloner cloner = getCloner( origVersion );
        TLResource newVersion = new TLResource();

        newVersion.setName( origVersion.getName() );
        newVersion.setBasePath( origVersion.getBasePath() );
        newVersion.setFirstClass( origVersion.isFirstClass() );
        newVersion.setDocumentation( cloner.clone( origVersion.getDocumentation() ) );
        newVersion.setBusinessObjectRef( origVersion.getBusinessObjectRef() );
        setExtension( newVersion, origVersion );

        targetLibrary.addNamedMember( newVersion );
        return newVersion;
    }

    /**
     * @see org.opentravel.schemacompiler.version.handlers.VersionHandler#rollupMinorVersion(org.opentravel.schemacompiler.version.Versioned,
     *      org.opentravel.schemacompiler.model.TLLibrary,
     *      org.opentravel.schemacompiler.version.handlers.RollupReferenceHandler)
     */
    @Override
    public TLResource rollupMinorVersion(TLResource minorVersion, TLLibrary majorVersionLibrary,
        RollupReferenceHandler referenceHandler) throws VersionSchemeException {
        TLResource majorVersion = retrieveExistingVersion( minorVersion, majorVersionLibrary );

        if (majorVersion == null) {
            majorVersion = getCloner( minorVersion ).clone( minorVersion );
            majorVersion.setBusinessObjectRef( minorVersion.getBusinessObjectRef() );
            assignBaseExtension( majorVersion, minorVersion );
            ModelElementCloner.addToLibrary( majorVersion, majorVersionLibrary );
            resolveParameterReferences( majorVersion );
            referenceHandler.captureRollupReferences( majorVersion );

        } else if (majorVersion instanceof TLResource) {
            rollupMinorVersion( minorVersion, majorVersion, referenceHandler );
        }
        return majorVersion;
    }

    /**
     * @see org.opentravel.schemacompiler.version.handlers.VersionHandler#rollupMinorVersion(org.opentravel.schemacompiler.version.Versioned,
     *      org.opentravel.schemacompiler.version.Versioned,
     *      org.opentravel.schemacompiler.version.handlers.RollupReferenceHandler)
     */
    @Override
    public void rollupMinorVersion(TLResource minorVersion, TLResource majorVersionTarget,
        RollupReferenceHandler referenceHandler) {
        ModelElementCloner cloner = getCloner( minorVersion );

        // Rollup the parent resource references
        List<TLResource> existingParentRefs = new ArrayList<>();

        for (TLResourceParentRef targetParentRef : majorVersionTarget.getParentRefs()) {
            existingParentRefs.add( targetParentRef.getParentResource() );
        }
        for (TLResourceParentRef sourceParentRef : minorVersion.getParentRefs()) {
            if (!existingParentRefs.contains( sourceParentRef.getParentResource() )) {
                TLResourceParentRef clone = cloner.clone( sourceParentRef );

                majorVersionTarget.addParentRef( clone );
                referenceHandler.captureRollupReferences( clone );
            }
        }

        // Rollup the business object reference
        if (majorVersionTarget.getBusinessObjectRef() != null) {
            majorVersionTarget.setBusinessObjectRef( minorVersion.getBusinessObjectRef() );
        }

        rollupActionFacets( minorVersion, majorVersionTarget, referenceHandler, cloner );
        rollupParameterGroups( minorVersion, majorVersionTarget, referenceHandler, cloner );
        rollupResourceActions( minorVersion, majorVersionTarget, referenceHandler, cloner );
    }

    /**
     * Rolls up the action facets for the given minor version resource.
     * 
     * @param minorVersion the minor version to be rolled up
     * @param majorVersionTarget the target resource version that will receive the rolled up content
     * @param referenceHandler the rollup reference handler instance
     * @param cloner used for cloning model element as required
     */
    private void rollupActionFacets(TLResource minorVersion, TLResource majorVersionTarget,
        RollupReferenceHandler referenceHandler, ModelElementCloner cloner) {
        for (TLActionFacet sourceFacet : minorVersion.getActionFacets()) {
            TLActionFacet targetFacet = majorVersionTarget.getActionFacet( sourceFacet.getName() );

            if (targetFacet == null) {
                targetFacet = new TLActionFacet();
                targetFacet.setName( sourceFacet.getName() );
                targetFacet.setReferenceFacetName( sourceFacet.getReferenceFacetName() );
                targetFacet.setReferenceRepeat( sourceFacet.getReferenceRepeat() );
                targetFacet.setReferenceType( sourceFacet.getReferenceType() );
                targetFacet.setBasePayload( sourceFacet.getBasePayload() );
                majorVersionTarget.addActionFacet( targetFacet );
                referenceHandler.captureRollupReferences( targetFacet );
            }
            if (targetFacet.getDocumentation() == null) {
                targetFacet.setDocumentation( cloner.clone( sourceFacet.getDocumentation() ) );
            }
        }
    }

    /**
     * Rolls up the parameter groups for the given minor version resource.
     * 
     * @param minorVersion the minor version to be rolled up
     * @param majorVersionTarget the target resource version that will receive the rolled up content
     * @param referenceHandler the rollup reference handler instance
     * @param cloner used for cloning model element as required
     */
    private void rollupParameterGroups(TLResource minorVersion, TLResource majorVersionTarget,
        RollupReferenceHandler referenceHandler, ModelElementCloner cloner) {
        for (TLParamGroup sourceParamGroup : minorVersion.getParamGroups()) {
            TLParamGroup targetParamGroup = majorVersionTarget.getParamGroup( sourceParamGroup.getName() );

            if (targetParamGroup == null) {
                targetParamGroup = new TLParamGroup();
                targetParamGroup.setName( sourceParamGroup.getName() );
                targetParamGroup.setIdGroup( sourceParamGroup.isIdGroup() );
                targetParamGroup.setFacetRef( sourceParamGroup.getFacetRef() );
                majorVersionTarget.addParamGroup( targetParamGroup );
                referenceHandler.captureRollupReferences( targetParamGroup );
            }
            if (targetParamGroup.getDocumentation() == null) {
                targetParamGroup.setDocumentation( cloner.clone( sourceParamGroup.getDocumentation() ) );
            }

            // Rollup the parameters of each group
            rollupParameters( sourceParamGroup, targetParamGroup, referenceHandler, cloner );
        }
        resolveParameterReferences( majorVersionTarget );
    }

    /**
     * Rollup the parameters of the given source parameter group.
     * 
     * @param sourceParamGroup the parameter group to be rolled up
     * @param targetParamGroup the group that will receive the rolled up content
     * @param referenceHandler the rollup reference handler instance
     * @param cloner used for cloning model element as required
     */
    private void rollupParameters(TLParamGroup sourceParamGroup, TLParamGroup targetParamGroup,
        RollupReferenceHandler referenceHandler, ModelElementCloner cloner) {
        for (TLParameter sourceParam : sourceParamGroup.getParameters()) {
            TLMemberField<?> sourceFieldRef = sourceParam.getFieldRef();

            if (sourceFieldRef != null) {
                TLParameter targetParam = targetParamGroup.getParameter( sourceFieldRef.getName() );

                if (targetParam == null) {
                    targetParam = cloner.clone( sourceParam ); // TODO: After cloning, targetParam.fieldRef is null
                    targetParamGroup.addParameter( targetParam );
                    referenceHandler.captureRollupReferences( targetParam );
                }
                if (targetParam.getDocumentation() == null) {
                    targetParam.setDocumentation( cloner.clone( sourceParam.getDocumentation() ) );
                }
            }
        }
    }

    /**
     * Rolls up the actons for the given minor version resource.
     * 
     * @param minorVersion the minor version to be rolled up
     * @param majorVersionTarget the target resource version that will receive the rolled up content
     * @param referenceHandler the rollup reference handler instance
     * @param cloner used for cloning model element as required
     */
    private void rollupResourceActions(TLResource minorVersion, TLResource majorVersionTarget,
        RollupReferenceHandler referenceHandler, ModelElementCloner cloner) {
        for (TLAction sourceAction : minorVersion.getActions()) {
            TLAction targetAction = majorVersionTarget.getAction( sourceAction.getActionId() );

            if (targetAction == null) {
                targetAction = new TLAction();
                targetAction.setActionId( sourceAction.getActionId() );
                targetAction.setCommonAction( sourceAction.isCommonAction() );
                majorVersionTarget.addAction( targetAction );
            }
            if (targetAction.getDocumentation() == null) {
                targetAction.setDocumentation( cloner.clone( sourceAction.getDocumentation() ) );
            }

            // Rollup the first request we encounter
            if ((targetAction.getRequest() == null) && (sourceAction.getRequest() != null)) {
                TLActionRequest clone = cloner.clone( sourceAction.getRequest() );

                targetAction.setRequest( clone );
                referenceHandler.captureRollupReferences( clone );
            }

            // Rollup the responses of each action
            Set<Integer> existingStatusCodes = new HashSet<>();

            for (TLActionResponse targetResponse : targetAction.getResponses()) {
                existingStatusCodes.addAll( targetResponse.getStatusCodes() );
            }
            for (TLActionResponse sourceResponse : sourceAction.getResponses()) {
                Set<Integer> uniqueStatusCodes = new HashSet<>( sourceResponse.getStatusCodes() );

                uniqueStatusCodes.removeAll( existingStatusCodes );

                if (!uniqueStatusCodes.isEmpty()) {
                    TLActionResponse clone = cloner.clone( sourceResponse );

                    clone.setStatusCodes( new ArrayList<>( uniqueStatusCodes ) );
                    targetAction.addResponse( clone );
                    referenceHandler.captureRollupReferences( clone );
                }
            }
        }
    }

    /**
     * After the major version of a resource has been processed, its parameter references need to be resolved since the
     * cloning process does not handle it.
     * 
     * @param majorVersion the resource for which to resolve parameter references
     */
    private void resolveParameterReferences(TLResource majorVersion) {
        boolean hasUnresolvedReferences = false;

        for (TLParamGroup paramGroup : majorVersion.getParamGroups()) {
            hasUnresolvedReferences = (paramGroup.getFacetRef() == null);

            for (TLParameter param : paramGroup.getParameters()) {
                hasUnresolvedReferences = (param.getFieldRef() == null);
            }
        }

        if (hasUnresolvedReferences) {
            ModelReferenceResolver.resolveReferences( majorVersion.getOwningModel() );
        }
    }

    /**
     * @see org.opentravel.schemacompiler.version.handlers.VersionHandler#getPatchableFacets(org.opentravel.schemacompiler.version.Versioned)
     */
    @Override
    public List<TLPatchableFacet> getPatchableFacets(TLResource entity) {
        return Collections.emptyList();
    }

}
