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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLClosedEnumeration;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLOpenEnumeration;
import org.opentravel.schemacompiler.model.TLPropertyType;
import org.opentravel.schemacompiler.model.TLRoleEnumeration;
import org.opentravel.schemacompiler.model.TLSimple;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;
import org.opentravel.schemacompiler.model.XSDSimpleType;
import org.opentravel.schemacompiler.validate.FindingType;
import org.opentravel.schemacompiler.validate.ValidationFindings;
import org.opentravel.schemacompiler.validate.base.TLValueWithAttributesBaseValidator;
import org.opentravel.schemacompiler.validate.impl.CircularReferenceChecker;
import org.opentravel.schemacompiler.validate.impl.TLValidationBuilder;
import org.opentravel.schemacompiler.validate.impl.ValidatorUtils;

/**
 * Validator for the <code>TLValueWithAttributes</code> class.
 * 
 * @author S. Livezey
 */
public class TLValueWithAttributesCompileValidator extends TLValueWithAttributesBaseValidator {

	private static final String PARENT_TYPE = "parentType";
	private static final String ATTRIBUTES = "attributes";
	
	public static final String ERROR_ILLEGAL_EXTENSION_ATTRIBUTE = "ILLEGAL_EXTENSION_ATTRIBUTE";
    public static final String ERROR_EXTENSION_NAME_CONFLICT = "EXTENSION_NAME_CONFLICT";
    public static final String ERROR_INHERITANCE_TYPE_CONFLICT = "INHERITANCE_TYPE_CONFLICT";
    public static final String ERROR_INVALID_CIRCULAR_REFERENCE = "INVALID_CIRCULAR_REFERENCE";
    public static final String ERROR_MULTIPLE_ID_MEMBERS = "MULTIPLE_ID_MEMBERS";

    /**
     * @see org.opentravel.schemacompiler.validate.impl.TLValidatorBase#validateFields(org.opentravel.schemacompiler.validate.Validatable)
     */
    @Override
    protected ValidationFindings validateFields(TLValueWithAttributes target) {
        List<TLModelElement> inheritedMembers = ValidatorUtils.getInheritedMembers(target);
        TLValidationBuilder builder = newValidationBuilder(target);

        builder.setProperty("name", target.getName()).setFindingType(FindingType.ERROR)
                .assertNotNullOrBlank().assertPatternMatch(NAME_XML_PATTERN);

        if ((target.getParentType() == null) && (target.getParentTypeName() != null)) {
            builder.addFinding(FindingType.ERROR, PARENT_TYPE,
                    TLValidationBuilder.UNRESOLVED_NAMED_ENTITY_REFERENCE,
                    target.getParentTypeName());
        }

        builder.setEntityReferenceProperty(PARENT_TYPE, target.getParentType(),
                target.getParentTypeName())
                .setFindingType(FindingType.ERROR)
                .assertValidEntityReference(TLSimple.class, TLClosedEnumeration.class,
                        XSDSimpleType.class, TLOpenEnumeration.class, TLRoleEnumeration.class,
                        TLValueWithAttributes.class)
                .setFindingType(FindingType.WARNING)
                .assertNotDeprecated()
                .assertNotObsolete();

        builder.setProperty(ATTRIBUTES, target.getAttributes()).setFindingType(FindingType.ERROR)
                .assertNotNull().assertContainsNoNullElements();

        builder.setProperty(ATTRIBUTES, inheritedMembers).setFindingType(FindingType.WARNING)
                .assertMinimumSize(1);

        builder.setProperty("indicators", target.getIndicators()).setFindingType(FindingType.ERROR)
                .assertNotNull().assertContainsNoNullElements();

        checkEmptyValueType(target, target.getParentType(), PARENT_TYPE, builder);

        builder.setProperty("equivalents", target.getEquivalents())
                .setFindingType(FindingType.ERROR).assertNotNull().assertContainsNoNullElements();

        if (ValidatorUtils.hasMultipleIdMembers(target)) {
            builder.addFinding(FindingType.ERROR, "members", ERROR_MULTIPLE_ID_MEMBERS);
        }

        if (parentTypeIsOpenEnumeration(target) && hasExtensionAttribute(inheritedMembers)) {
            builder.addFinding(FindingType.ERROR, PARENT_TYPE, ERROR_ILLEGAL_EXTENSION_ATTRIBUTE);
        }

        // Check to see if any of the declared attribute/indicator names conflict with the
        // 'Extension'
        // attributes created for the open enumeration attributes.
        Set<String> openEnumerationAttributes = new HashSet<>();
        Set<String> attributeNames = new HashSet<>();

        for (TLModelElement member : inheritedMembers) {
            if (member instanceof TLAttribute) {
                TLAttribute attribute = (TLAttribute) member;

                if (isOpenEnumerationAttribute(attribute)) {
                    openEnumerationAttributes.add(attribute.getName());
                }
                attributeNames.add(attribute.getName());
            } else { // must be an indicator
                attributeNames.add(((TLIndicator) member).getName());
            }
        }
        for (String openEnumAttrName : openEnumerationAttributes) {
            if (attributeNames.contains(openEnumAttrName + "Extension")) {
                builder.addFinding(FindingType.ERROR, ATTRIBUTES,
                        ERROR_ILLEGAL_EXTENSION_ATTRIBUTE, openEnumAttrName);
                break;
            }
        }

        // Check to see if any duplicate attributes (assumed to be inherited from VWA attributes)
        // have the same
        // name but different type assignments.
        List<TLAttribute> attributesWithDuplicates = PropertyCodegenUtils
                .getInheritedAttributes(target);
        Map<String, TLPropertyType> attributeTypes = new HashMap<>();

        for (TLAttribute attribute : attributesWithDuplicates) {
        	TLPropertyType existingType = attributeTypes.get(attribute.getName());

            if (existingType == null) { // First time we have seen an attribute with this name
                attributeTypes.put(attribute.getName(), attribute.getType());

            } else if (existingType != attribute.getType()) { // Make sure the attribute types are
                                                              // identical
                builder.addFinding(FindingType.ERROR, ATTRIBUTES,
                        ERROR_INHERITANCE_TYPE_CONFLICT, attribute.getName());
                break; // stop after the first duplicate
            }
        }

        // Check for circular references
        if (CircularReferenceChecker.hasCircularReference(target)) {
            builder.addFinding(FindingType.ERROR, PARENT_TYPE, ERROR_INVALID_CIRCULAR_REFERENCE);
        }

        checkSchemaNamingConflicts(target, builder);
        validateVersioningRules(target, builder);

        return builder.getFindings();
    }

    /**
     * Returns true if the given attribute's type is an open enumeration or a VWA whose base type is
     * an open enumeration.
     * 
     * @param attribute
     *            the attribute to analyze
     * @return boolean
     */
    private boolean isOpenEnumerationAttribute(TLAttribute attribute) {
    	TLPropertyType attributeType = attribute.getType();

        return (attributeType instanceof TLOpenEnumeration)
                || ((attributeType instanceof TLValueWithAttributes) && parentTypeIsOpenEnumeration((TLValueWithAttributes) attributeType));
    }

    /**
     * Returns true if the parent type of the VWA is a open (or role) enumeration. In the case where
     * the given VWA's parent type is another VWA, this method will recursively determine if the
     * final parent type is an open enumeration.
     * 
     * @param target
     *            the target VWA being validated
     * @return boolean
     */
    private boolean parentTypeIsOpenEnumeration(TLValueWithAttributes vwa) {
        return parentTypeIsOpenEnumeration(vwa, new HashSet<TLValueWithAttributes>());
    }

    /**
     * Recursive method that checks whether the parent type of the VWA is an open enumeration, while
     * protecting from infinite loops due to circular references.
     * 
     * @param target
     *            the target VWA being validated
     * @return boolean
     */
    private boolean parentTypeIsOpenEnumeration(TLValueWithAttributes vwa,
            Set<TLValueWithAttributes> visitedVwas) {
        NamedEntity parentType = vwa.getParentType();
        boolean isOpenEnum = false;

        if ((parentType instanceof TLOpenEnumeration) || (parentType instanceof TLRoleEnumeration)) {
            isOpenEnum = true;

        } else if (parentType instanceof TLValueWithAttributes) {
            TLValueWithAttributes parentVWA = (TLValueWithAttributes) parentType;

            if (!visitedVwas.contains(parentVWA)) {
                visitedVwas.add(parentVWA);
                isOpenEnum = parentTypeIsOpenEnumeration(parentVWA, visitedVwas);
            }
        }
        return isOpenEnum;
    }

    /**
     * Returns true if the given list of VWA members contains an attribute with the name
     * "extension". If the parent type is an open/role enumeration, the name will interfere with the
     * implied attribute used for un-declared enumeration values.
     * 
     * <p>
     * NOTE: Indicators with the name "extension" are not considered, since the compiler will
     * automatically append the "...Ind" suffix during schema generation.
     * 
     * @param inheritedMembers
     *            the list of attributes and indicators declared or inherited by the VWA
     * @return boolean
     */
    private boolean hasExtensionAttribute(List<TLModelElement> inheritedMembers) {
        boolean result = false;

        for (TLModelElement member : inheritedMembers) {
            if (member instanceof TLAttribute) {
                TLAttribute attr = (TLAttribute) member;

                if ((attr.getName() != null) && attr.getName().equals("extension")) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

}
