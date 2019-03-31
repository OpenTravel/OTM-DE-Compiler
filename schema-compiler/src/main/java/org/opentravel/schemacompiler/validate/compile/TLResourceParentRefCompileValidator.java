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
import org.opentravel.schemacompiler.model.TLParamGroup;
import org.opentravel.schemacompiler.model.TLResource;
import org.opentravel.schemacompiler.model.TLResourceParentRef;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLResourceParentRefBaseValidator;
import org.opentravel.schemacompiler.validate.impl.CircularReferenceChecker;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;

import java.util.List;

/**
 * Validator for the <code>TLResourceParentRef</code> class.
 * 
 * @author S. Livezey
 */
public class TLResourceParentRefCompileValidator extends TLResourceParentRefBaseValidator {

    private static final String PARENT_RESOURCE = "parentResource";
    private static final String PARENT_PARAM_GROUP = "parentParamGroup";

    public static final String ERROR_INVALID_ABSTRACT_PARENT = "INVALID_ABSTRACT_PARENT";
    public static final String ERROR_INVALID_PARAM_GROUP = "INVALID_PARAM_GROUP";
    public static final String ERROR_ID_PARAM_GROUP_REQUIRED = "ID_PARAM_GROUP_REQUIRED";
    public static final String ERROR_CONFLICTING_PATH_TEMPLATE = "CONFLICTING_PATH_TEMPLATE";
    public static final String ERROR_INVALID_CIRCULAR_PARENT_REF = "INVALID_CIRCULAR_PARENT_REF";

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLResourceParentRef target) {
        TLValidationBuilder builder = newValidationBuilder( target );
        TLResource parentResource = target.getParentResource();
        TLParamGroup parentParamGroup = target.getParentParamGroup();

        builder.setEntityReferenceProperty( PARENT_RESOURCE, parentResource, target.getParentResourceName() )
            .setFindingType( FindingType.ERROR ).assertNotNull();

        if ((parentResource != null) && parentResource.isAbstract()) {
            builder.addFinding( FindingType.ERROR, PARENT_RESOURCE, ERROR_INVALID_ABSTRACT_PARENT );
        }

        if (CircularReferenceChecker.hasCircularParentRef( target )) {
            builder.addFinding( FindingType.ERROR, PARENT_RESOURCE, ERROR_INVALID_CIRCULAR_PARENT_REF );
        }

        if (parentParamGroup == null) {
            String paramGroupName = target.getParentParamGroupName();

            if ((paramGroupName == null) || paramGroupName.equals( "" )) {
                builder.addFinding( FindingType.ERROR, PARENT_PARAM_GROUP,
                    TLValidationBuilder.MISSING_NAMED_ENTITY_REFERENCE );
            } else {
                builder.addFinding( FindingType.ERROR, PARENT_PARAM_GROUP,
                    TLValidationBuilder.UNRESOLVED_NAMED_ENTITY_REFERENCE, paramGroupName );
            }
        } else if (parentResource != null) {
            List<TLParamGroup> inheritedParamGroup = ResourceCodegenUtils.getInheritedParamGroups( parentResource );

            if (!inheritedParamGroup.contains( parentParamGroup )) {
                builder.addFinding( FindingType.ERROR, PARENT_PARAM_GROUP, ERROR_INVALID_PARAM_GROUP,
                    parentParamGroup.getName() );
            }
            if (!parentParamGroup.isIdGroup()) {
                builder.addFinding( FindingType.ERROR, PARENT_PARAM_GROUP, ERROR_ID_PARAM_GROUP_REQUIRED,
                    parentParamGroup.getName() );
            }
        }

        builder.setProperty( "pathTemplate", target.getPathTemplate() ).setFindingType( FindingType.ERROR )
            .assertNotNullOrBlank();
        validatePathTemplate( target.getPathTemplate(), parentParamGroup, builder );
        checkConflictingPathTemplate( target, builder );

        return builder.getFindings();
    }

    /**
     * Determines whether the path template for the target request conflicts with any other parent resource references
     * of the owning resource.
     * 
     * @param target the parent resource reference being validated
     * @param builder the validation builder to which any findings will be posted
     */
    private void checkConflictingPathTemplate(TLResourceParentRef target, TLValidationBuilder builder) {
        TLResource owningResource = (target.getOwner() == null) ? null : target.getOwner();
        String targetTestPath = buildTestPath( target.getPathTemplate() );

        if ((owningResource != null) && (targetTestPath != null)) {
            for (TLResourceParentRef request : owningResource.getParentRefs()) {
                if (request == target) {
                    continue;
                }
                String testPath = buildTestPath( request.getPathTemplate() );

                if (targetTestPath.equals( testPath )) {
                    builder.addFinding( FindingType.ERROR, "pathTemplate", ERROR_CONFLICTING_PATH_TEMPLATE,
                        target.getPathTemplate() );
                }
            }
        }
    }

}
