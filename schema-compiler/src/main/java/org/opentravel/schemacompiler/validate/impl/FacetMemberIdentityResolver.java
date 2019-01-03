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

import javax.xml.namespace.QName;

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
			identity = getAttributeIdentity( (TLAttribute) entity );
			
		} else if (entity instanceof TLProperty) {
			identity = getElementIdentity( (TLProperty) entity );
			
		} else if (entity instanceof TLIndicator) {
			identity = ((TLIndicator) entity).getName();
			
			if ((identity != null) && !identity.endsWith( "Ind" )) {
				identity += "Ind";
			}
		}
		return identity;
	}

	/**
	 * Returns the identity name for the given attribute.
	 * 
	 * @param attribute  the attribute for which to return an identity
	 * @return String
	 */
	private String getAttributeIdentity(TLAttribute attribute) {
		String identity;
		String attrName = attribute.getName();
		
		// If the attribute's name is not defined, use its type name
		if (((attrName == null) || (attrName.length() == 0)) && (attribute.getType() != null)) {
			identity = attribute.getType().getLocalName();
		} else {
			identity = attribute.getName();
		}
		if ((identity != null) && attribute.isReference() && !identity.endsWith( "Ref" )) {
			identity += "Ref";
		}
		return identity;
	}

	/**
	 * Returns the identity name for the given element.
	 * 
	 * @param element  the element for which to return an identity
	 * @return String
	 */
	private String getElementIdentity(TLProperty element) {
		String identity;
		TLPropertyType elementType = PropertyCodegenUtils.resolvePropertyType( element.getType() );
		
		if (PropertyCodegenUtils.hasGlobalElement( elementType )) {
			QName identityName = XsdCodegenUtils.getGlobalElementName( elementType );
			identity = (identityName == null) ? null : identityName.getLocalPart();
			
		} else if ((element.getName() == null) || (element.getName().length() == 0)) {
			identity = (elementType == null) ? "" : elementType.getLocalName();
			
		} else {
			identity = element.getName();
		}
		
		if ((identity != null) && element.isReference() && !identity.endsWith( "Ref" )) {
			identity += "Ref";
		}
		return identity;
	}
	
}
