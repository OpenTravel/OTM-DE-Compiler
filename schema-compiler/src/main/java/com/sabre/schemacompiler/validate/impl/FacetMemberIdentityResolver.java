/*
 * Copyright (c) 2011, Sabre Inc.
 */
package com.sabre.schemacompiler.validate.impl;

import com.sabre.schemacompiler.codegen.util.PropertyCodegenUtils;
import com.sabre.schemacompiler.codegen.util.XsdCodegenUtils;
import com.sabre.schemacompiler.model.TLAttribute;
import com.sabre.schemacompiler.model.TLIndicator;
import com.sabre.schemacompiler.model.TLModelElement;
import com.sabre.schemacompiler.model.TLProperty;
import com.sabre.schemacompiler.model.TLPropertyType;

/**
 * Returns the name of the member element if it is an attribute, element, or indicator instance.
 * 
 * @author S. Livezey
 */
public class FacetMemberIdentityResolver implements IdentityResolver<TLModelElement> {
	
	/**
	 * @see com.sabre.schemacompiler.validate.impl.IdentityResolver#getIdentity(java.lang.Object)
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
			TLPropertyType elementType = PropertyCodegenUtils.resolvePropertyType(element.getPropertyOwner(), element.getType());
			
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
