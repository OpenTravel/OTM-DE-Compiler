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

package org.opentravel.schemacompiler.codegen.impl;

import org.opentravel.schemacompiler.model.TLParameter;
import org.opentravel.schemacompiler.model.TLResourceParentRef;

/**
 * Represents the pairing of zero or more <code>TLResourceParentRefs</code> and a
 * <code>TLParameter</code> for the purposes of generating an API specification.
 */
public class QualifiedParameter {
	
	private TLResourceParentRef parentRef;
	private TLParameter parameter;
	
	/**
	 * Full constructor.
	 * 
	 * @param parentRef  the parent reference for the the qualified action
	 * @param parameter  the OTM parameter for which an API parameter will be generated
	 */
	public QualifiedParameter(TLResourceParentRef parentRef, TLParameter parameter) {
		this.parentRef = parentRef;
		this.parameter = parameter;
	}
	
	/**
	 * Returns theparent reference for the the qualified action.
	 *
	 * @return TLResourceParentRef
	 */
	public TLResourceParentRef getParentRef() {
		return parentRef;
	}
	
	/**
	 * Returns the OTM parameter for which an API parameter will be generated.
	 *
	 * @return TLParameter
	 */
	public TLParameter getParameter() {
		return parameter;
	}
	
	/**
	 * Returns the qualified name of this parameter.
	 * 
	 * @return String
	 */
	public String getParameterName() {
		StringBuilder nameBuilder = new StringBuilder();
		
		if (parentRef != null) {
			nameBuilder.append( parentRef.getParentResource().getName() + "_" );
		}
		nameBuilder.append( parameter.getFieldRef().getName() );
		return nameBuilder.toString();
	}
	
}
