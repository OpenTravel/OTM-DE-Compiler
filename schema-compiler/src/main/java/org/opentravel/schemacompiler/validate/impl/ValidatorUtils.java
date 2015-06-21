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
package org.opentravel.schemacompiler.validate.impl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;

import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLExtensionPointFacet;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLValueWithAttributes;

/**
 * Static utility methods that are shared among several of the validator implementation classes.
 * 
 * @author S. Livezey
 */
public class ValidatorUtils {

    /**
     * Returns true if the given type reference is to the built-in boolean XML schema type.
     * 
     * @param referencedType
     *            the referenced type to analyze
     * @return boolean
     */
    public static boolean isBooleanType(NamedEntity referencedType) {
        return (referencedType != null)
                && XsdCodegenUtils.XSD_BOOLEAN_TYPE.getNamespaceURI().equals(
                        referencedType.getNamespace())
                && XsdCodegenUtils.XSD_BOOLEAN_TYPE.getLocalPart().equals(
                        referencedType.getLocalName());
    }

    /**
     * Returns true if the given named entity represents either the 'xsd:IDREF' or 'xsd:IDREFS'
     * attribute/property type.
     * 
     * @param type
     *            the entity type to analyze
     * @return boolean
     */
    public static boolean isXsdID(NamedEntity type) {
        return (type != null) && XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(type.getNamespace())
                && "ID".equals(type.getLocalName());
    }

    /**
     * Returns true if the given named entity represents either the 'xsd:IDREF' or 'xsd:IDREFS'
     * attribute/property type.
     * 
     * @param type
     *            the entity type to analyze
     * @return boolean
     */
    public static boolean isLegacyIDREF(NamedEntity type) {
        return (type != null) && XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(type.getNamespace())
                && ("IDREF".equals(type.getLocalName()) || "IDREFS".equals(type.getLocalName()));
    }

    /**
     * Returns the set of attributes, properties, and indicators that are owned by the given
     * value-with-attributes.
     * 
     * @param target
     *            the value-with-attributes being validated
     * @param includeIndicators
     *            flag indicating whether to include indicators in the set that is returned
     * @return List<TLModelElement>
     */
    public static List<TLModelElement> getMembers(TLValueWithAttributes target) {
        List<TLModelElement> members = new ArrayList<TLModelElement>();

        members.addAll(target.getAttributes());
        members.addAll(target.getIndicators());
        return members;
    }

    /**
     * Returns the set of attributes, properties, and indicators that are owned by the given
     * extension point facet.
     * 
     * @param target
     *            the extension point facet being validated
     * @param includeIndicators
     *            flag indicating whether to include indicators in the set that is returned
     * @return List<TLModelElement>
     */
    public static List<TLModelElement> getMembers(TLExtensionPointFacet target) {
        List<TLModelElement> members = new ArrayList<TLModelElement>();

        members.addAll(target.getAttributes());
        members.addAll(target.getElements());
        members.addAll(target.getIndicators());
        return members;
    }

    /**
     * Returns the set of attributes, properties, and indicators that are owned by the given facet.
     * 
     * @param target
     *            the target facet being validated
     * @param includeIndicators
     *            flag indicating whether to include indicators in the set that is returned
     * @return List<TLModelElement>
     */
    public static List<TLModelElement> getMembers(TLFacet target) {
        return getMembers(target, true);
    }

    /**
     * Returns the set of attributes, properties, and (optional) indicators that are owned by the
     * given facet.
     * 
     * @param target
     *            the target facet being validated
     * @param includeIndicators
     *            flag indicating whether to include indicators in the set that is returned
     * @return List<TLModelElement>
     */
    public static List<TLModelElement> getMembers(TLFacet target, boolean includeIndicators) {
        List<TLModelElement> facetMembers = new ArrayList<TLModelElement>();

        if (includeIndicators) {
            facetMembers.addAll(target.getIndicators());
        }
        facetMembers.addAll(target.getElements());
        facetMembers.addAll(target.getAttributes());
        return facetMembers;
    }

    /**
     * Returns the collection of all attributes and indicators for the given VWA.
     * 
     * @param target
     *            the target VWA being validated
     * @return List<TLModelElement>
     */
    public static List<TLModelElement> getInheritedMembers(TLValueWithAttributes target) {
        List<TLModelElement> memberList = new ArrayList<TLModelElement>();

        memberList.addAll(PropertyCodegenUtils.getInheritedAttributes(target));
        memberList.addAll(PropertyCodegenUtils.getInheritedIndicators(target));
        return memberList;
    }

    /**
     * Returns a collection of all attributes, properties, and indicators that are owned or
     * inherited by the given target facet.
     * 
     * @param target
     *            the target facet being validated
     * @return List<TLModelElement>
     */
    public static List<TLModelElement> getInheritedMembers(TLFacet target) {
        List<TLModelElement> inheritedMembers = new ArrayList<TLModelElement>();

        inheritedMembers.addAll(PropertyCodegenUtils.getInheritedAttributes(target));
        inheritedMembers.addAll(PropertyCodegenUtils.getInheritedProperties(target));
        inheritedMembers.addAll(PropertyCodegenUtils.getInheritedIndicators(target));
        return inheritedMembers;
    }

    /**
     * Returns true if the given named entity contains more than one ID attribute and/or element.
     * 
     * @param entity
     *            the named entity to analyze
     * @return boolean
     */
    public static boolean hasMultipleIdMembers(NamedEntity entity) {
        List<TLAttribute> attributeList = new ArrayList<TLAttribute>();
        List<TLProperty> propertyList = new ArrayList<TLProperty>();
        boolean declaresId = false;
        int idCount = 0;

        if (entity instanceof TLValueWithAttributes) {
            attributeList.addAll(PropertyCodegenUtils
                    .getInheritedAttributes((TLValueWithAttributes) entity));

        } else if (entity instanceof TLFacet) {
            attributeList.addAll(PropertyCodegenUtils.getInheritedAttributes((TLFacet) entity));
            propertyList.addAll(PropertyCodegenUtils.getInheritedProperties((TLFacet) entity));

        } else if (entity instanceof TLExtensionPointFacet) {
            attributeList.addAll(((TLExtensionPointFacet) entity).getAttributes());
            propertyList.addAll(((TLExtensionPointFacet) entity).getElements());
        }

        for (TLAttribute attribute : attributeList) {
            if (XsdCodegenUtils.isIdType(attribute.getType())) {
                declaresId |= (attribute.getAttributeOwner() == entity);
                idCount++;
            }
        }
        for (TLProperty property : propertyList) {
            if (XsdCodegenUtils.isIdType(property.getType())) {
                declaresId |= (property.getPropertyOwner() == entity);
                idCount++;
            }
        }
        return declaresId && (idCount > 1);
    }

}
