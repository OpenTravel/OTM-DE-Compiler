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

package org.opentravel.schemacompiler.codegen.util;

import org.opentravel.schemacompiler.model.ModelElement;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLAliasOwner;
import org.opentravel.schemacompiler.model.TLBusinessObject;
import org.opentravel.schemacompiler.model.TLChoiceObject;
import org.opentravel.schemacompiler.model.TLCoreObject;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLProperty;

import javax.xml.namespace.QName;

/**
 * Static utility methods used to obtain schema property and definition names for OTM entities.
 */
public class JsonSchemaNamingUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private JsonSchemaNamingUtils() {}

    /**
     * Returns the globally-accessible type name for the given entity in the JSON schema output.
     * 
     * <p>
     * NOTE: In some cases (e.g. action facets of abstract resources), the resulting value may be null. This indicates
     * that there is no global schema type name associated with the given model entity.
     * 
     * @param modelEntity the model entity for which to return the element name
     * @return String
     */
    public static String getGlobalDefinitionName(NamedEntity modelEntity) {
        String definitionName = getGlobalElementName( modelEntity, false );

        if (definitionName == null) {
            definitionName = XsdCodegenUtils.getGlobalTypeName( modelEntity );
        }
        return definitionName;
    }

    /**
     * Returns the globally-accessible type name for the given entity in the JSON schema output.
     * 
     * <p>
     * NOTE: In some cases (e.g. action facets of abstract resources), the resulting value may be null. This indicates
     * that there is no global schema type name associated with the given model entity.
     * 
     * @param modelEntity the model entity for which to return the element name
     * @param referencingProperty the property that references the given model entity
     * @return String
     */
    public static String getGlobalDefinitionName(NamedEntity modelEntity, TLProperty referencingProperty) {
        String definitionName = getGlobalElementName( modelEntity, false );

        if (definitionName == null) {
            definitionName = XsdCodegenUtils.getGlobalTypeName( modelEntity, referencingProperty );
        }
        return definitionName;
    }

    /**
     * Returns the globally-accessible type name for the given entity in the JSON schema output.
     * 
     * <p>
     * NOTE: In some cases (e.g. action facets of abstract resources), the resulting value may be null. This indicates
     * that there is no global schema type name associated with the given model entity.
     * 
     * @param modelEntity the model entity for which to return the element name
     * @return String
     */
    public static String getGlobalReferenceName(NamedEntity modelEntity) {
        String definitionName = getGlobalElementName( modelEntity, true );

        if (definitionName == null) {
            definitionName = XsdCodegenUtils.getGlobalTypeName( modelEntity );
        }
        return definitionName;
    }

    /**
     * Returns the global XML element name for the given entity or null if the entity does not have one.
     * 
     * @param modelEntity the model entity for which to return the element name
     * @param useReferenceName flag indicating whether the name is to be used for naming a definition or a type
     *        reference to a definition
     * @return String
     */
    private static String getGlobalElementName(NamedEntity modelEntity, boolean useReferenceName) {
        QName elementName = null;

        if (modelEntity instanceof TLAlias) {
            TLAlias alias = (TLAlias) modelEntity;

            elementName = getAliasGlobalElementName( alias, useReferenceName );

            // Last resort since all aliases must have a global element name
            if (elementName == null) {
                elementName = XsdCodegenUtils.getGlobalElementName( modelEntity );
            }

        } else {
            if (modelEntity instanceof TLFacet) {
                if (useReferenceName) {
                    elementName = XsdCodegenUtils.getGlobalElementName( modelEntity );

                } else {
                    elementName = XsdCodegenUtils.getSubstitutableElementName( (TLFacet) modelEntity );
                }

            } else {
                TLFacet entityFacet = getRootFacet( modelEntity );

                if (entityFacet != null) {
                    elementName = XsdCodegenUtils.getSubstitutableElementName( entityFacet );
                }
            }
        }
        return (elementName == null) ? null : elementName.getLocalPart();
    }

    /**
     * Returns the global element name for the given alias.
     * 
     * @param alias the alias for which to return a global element name
     * @return QName
     */
    private static QName getAliasGlobalElementName(TLAlias alias, boolean useReferenceName) {
        TLAliasOwner aliasOwner = alias.getOwningEntity();
        QName elementName = null;

        if (aliasOwner instanceof TLFacet) {
            if (useReferenceName) {
                elementName = XsdCodegenUtils.getGlobalElementName( alias );

            } else {
                elementName = XsdCodegenUtils.getSubstitutableElementName( alias );
            }

        } else {
            TLFacet facet = getRootFacet( aliasOwner );

            if (facet != null) {
                TLAlias facetAlias = AliasCodegenUtils.getFacetAlias( alias, facet.getFacetType() );

                elementName = XsdCodegenUtils.getSubstitutableElementName( facetAlias );
            }
        }
        return elementName;
    }

    /**
     * Returns the top-level root facet of the substitution group, or null if the given facet owner does not define a
     * root-level facet.
     * 
     * @param facetOwner the owner for which to return a root facet
     * @return TLFacet
     */
    private static TLFacet getRootFacet(ModelElement facetOwner) {
        TLFacet facet = null;

        if (facetOwner instanceof TLBusinessObject) {
            facet = ((TLBusinessObject) facetOwner).getIdFacet();

        } else if (facetOwner instanceof TLCoreObject) {
            facet = ((TLCoreObject) facetOwner).getSummaryFacet();

        } else if (facetOwner instanceof TLChoiceObject) {
            facet = ((TLChoiceObject) facetOwner).getSharedFacet();
        }
        return facet;
    }

    /**
     * Returns the globally-accessible type name for the given entity in the XML schema output.
     * 
     * <p>
     * NOTE: In some cases (e.g. action facets of abstract resources), the resulting value may be null. This indicates
     * that there is no global schema type name associated with the given model entity.
     * 
     * @param modelEntity the model entity for which to return the element name
     * @return String
     */
    public static String getGlobalPropertyName(NamedEntity modelEntity) {
        return getGlobalPropertyName( modelEntity, false );
    }

    /**
     * Returns the globally-accessible type name for the given entity in the XML schema output.
     * 
     * <p>
     * NOTE: In some cases (e.g. action facets of abstract resources), the resulting value may be null. This indicates
     * that there is no global schema type name associated with the given model entity.
     * 
     * @param modelEntity the model entity for which to return the element name
     * @param isReferenceProperty flag indicating whether the property name should represent an IDREF
     * @return String
     */
    public static String getGlobalPropertyName(NamedEntity modelEntity, boolean isReferenceProperty) {
        QName propertyName = PropertyCodegenUtils.getDefaultXmlElementName( modelEntity, isReferenceProperty );
        return (propertyName == null) ? null : propertyName.getLocalPart();
    }

}
