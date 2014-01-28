package org.opentravel.schemacompiler.validate.impl;

import org.opentravel.schemacompiler.codegen.util.PropertyCodegenUtils;
import org.opentravel.schemacompiler.codegen.util.XsdCodegenUtils;
import org.opentravel.schemacompiler.model.TLAttribute;
import org.opentravel.schemacompiler.model.TLIndicator;
import org.opentravel.schemacompiler.model.TLModelElement;
import org.opentravel.schemacompiler.model.TLProperty;
import org.opentravel.schemacompiler.model.TLPropertyType;

/**
 * Returns the name of the member element if it is an attribute, element, or indicator instance.
 * 
 * @author S. Livezey
 */
public class FacetMemberIdentityResolver implements IdentityResolver<TLModelElement> {

    /**
     * @see org.opentravel.schemacompiler.validate.impl.IdentityResolver#getIdentity(java.lang.Object)
     */
    @Override
    public String getIdentity(TLModelElement entity) {
        String identity = null;

        if (entity instanceof TLAttribute) {
            TLAttribute attribute = (TLAttribute) entity;
            String attrName = attribute.getName();

            // If the attribute's name is not defined, use its type name
            if (((attrName == null) || (attrName.length() == 0)) && (attribute.getType() != null)) {
                identity = attribute.getType().getLocalName();
            } else {
                identity = attribute.getName();
            }

        } else if (entity instanceof TLProperty) {
            TLProperty element = (TLProperty) entity;
            TLPropertyType elementType = PropertyCodegenUtils.resolvePropertyType(
                    element.getPropertyOwner(), element.getType());

            if (PropertyCodegenUtils.hasGlobalElement(elementType)) {
                identity = XsdCodegenUtils.getGlobalElementName(elementType).getLocalPart();

            } else if ((element.getName() == null) || (element.getName().length() == 0)) {
                identity = (elementType == null) ? "" : elementType.getLocalName();

            } else {
                identity = element.getName();
            }
        } else if (entity instanceof TLIndicator) {
            identity = ((TLIndicator) entity).getName();

            if ((identity != null) && !identity.endsWith("Ind")) {
                identity += "Ind";
            }
        }
        return identity;
    }

}
